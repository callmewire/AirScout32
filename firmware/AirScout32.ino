
/**
 * AirScout32 - Air Quality Monitoring System
 * 
 * Measures CO2, temperature, humidity (SCD41), and VOC/CO (MICS5524).
 * Transmits data via Bluetooth as JSON. Provides LED and audio warnings.
 * 
 * Author:  Paul Uhlmann
 * Date:    2025-11-25
 * Version: 1.2.40
 * Board:   ESP32
 */

#include <Arduino.h>
#include <SensirionI2cScd4x.h>
#include <Wire.h>
#include "DFRobot_MICS.h"
#include "BluetoothSerial.h"    // v1.1.0
#include "ArduinoJson.h"        // v7.4.1

BluetoothSerial SerialBT;

// Battery config (Li-Ion via 1:2 voltage divider on GPIO 33)
const int ubPin = 2;
const float Ubmax = 4.2;  // Max voltage (full)
const float Ubmin = 3.5;  // Min voltage (empty)

// MICS5524 gas sensor pins
#define CALIBRATION_TIME   2
#define ADC_PIN            32
#define POWER_PIN          35
#define ANALOG_PIN         33

DFRobot_MICS_ADC mics(ADC_PIN, POWER_PIN);

#ifdef NO_ERROR
#undef NO_ERROR
#endif
#define NO_ERROR 0

// LED pins
const int rotLed = 15;    // Red - danger
const int gelbLed = 4;    // Yellow - warning
const int grünLed = 16;   // Green - status

// Global variables
int ubValue = 0;
int analogPin = 33;
int sensorValue = 0;
float analogVoltage = 0.00;
const int Lautsprecher_Pin = 17;

SensirionI2cScd4x sensor;
static char errorMessage[64];
static int16_t error;

// Print 64-bit hex value (for sensor serial number)
void PrintUint64(uint64_t& value) {
  Serial.print("0x");
  Serial.print((uint32_t)(value >> 32), HEX);
  Serial.print((uint32_t)(value & 0xFFFFFFFF), HEX);
}

void setup() {
  Serial.begin(115200);
  while (!Serial) {
    delay(100);
  }
  
  Wire.begin();
  sensor.begin(Wire, SCD41_I2C_ADDR_62);

  uint64_t serialNumber = 0;
  delay(30);
  
  // Initialize SCD41 sensor
  error = sensor.wakeUp();
  if (error != NO_ERROR) {
    Serial.print("Error wakeUp(): ");
    errorToString(error, errorMessage, sizeof errorMessage);
    Serial.println(errorMessage);
  }
  
  error = sensor.stopPeriodicMeasurement();
  if (error != NO_ERROR) {
    Serial.print("Error stopPeriodicMeasurement(): ");
    errorToString(error, errorMessage, sizeof errorMessage);
    Serial.println(errorMessage);
  }
  
  error = sensor.reinit();
  if (error != NO_ERROR) {
    Serial.print("Error reinit(): ");
    errorToString(error, errorMessage, sizeof errorMessage);
    Serial.println(errorMessage);
  }
  
  error = sensor.getSerialNumber(serialNumber);
  if (error != NO_ERROR) {
    Serial.print("Error getSerialNumber(): ");
    errorToString(error, errorMessage, sizeof errorMessage);
    Serial.println(errorMessage);
    return;
  }
  Serial.print("Serial number: ");
  PrintUint64(serialNumber);
  Serial.println();

  // Initialize Bluetooth
  SerialBT.begin("Sensorsender_2");
  Serial.println("Bluetooth ready. Device: Sensorsender_2");

  // Initialize LEDs and speaker
  pinMode(rotLed, OUTPUT);
  pinMode(gelbLed, OUTPUT);
  pinMode(grünLed, OUTPUT);
  
  tone(Lautsprecher_Pin, 800, 500);
  
  // Warm-up sequence (~30s)
  for (int i = 0; i <= 10; i++) {
       digitalWrite(grünLed, HIGH);
       delay(500);
       digitalWrite(grünLed, LOW);
       delay(100);
       digitalWrite(gelbLed, HIGH);
       delay(500);
       digitalWrite(gelbLed, LOW);
       delay(100);
       digitalWrite(rotLed, HIGH);
       delay(500);
       digitalWrite(rotLed, LOW);
       delay(100);
       digitalWrite(gelbLed, HIGH);
       delay(500);
       digitalWrite(gelbLed, LOW);
       delay(100);
    digitalWrite(grünLed, HIGH);
  }
  
  // Ready melody
  tone(Lautsprecher_Pin, 1000, 1000);
  tone(Lautsprecher_Pin, 800, 1000);
  tone(Lautsprecher_Pin, 1000, 1000);
  tone(Lautsprecher_Pin, 800, 1000);
  
  Serial.println("Initialization complete.");
}

void loop() {
  uint16_t co2Concentration = 0;
  float temperature = 0;
  float relativeHumidity = 0;

  // Wake sensor and take measurement
  error = sensor.wakeUp();
  if (error != NO_ERROR) {
    Serial.print("Error wakeUp(): ");
    errorToString(error, errorMessage, sizeof errorMessage);
    Serial.println(errorMessage);
    return;
  }
  
  // Discard first measurement after wake-up
  error = sensor.measureSingleShot();
  if (error != NO_ERROR) {
    Serial.print("Error measureSingleShot(): ");
    errorToString(error, errorMessage, sizeof errorMessage);
    Serial.println(errorMessage);
    return;
  }
  
  error = sensor.measureAndReadSingleShot(co2Concentration, temperature, relativeHumidity);
  if (error != NO_ERROR) {
    Serial.print("Error measureAndReadSingleShot(): ");
    errorToString(error, errorMessage, sizeof errorMessage);
    Serial.println(errorMessage);
    return;
  }
  
  // Debug output
  Serial.print("CO2 [ppm]: ");
  Serial.println(co2Concentration);
  Serial.print("Temperature [°C]: ");
  Serial.println(temperature, 1);
  Serial.print("Humidity [%RH]: ");
  Serial.println(relativeHumidity, 0);
  
  delay(30);
  
  // Read MICS5524 (VOC/CO)
  int analogValue = analogRead(ADC_PIN);
  Serial.print("VOC (raw): ");
  Serial.println(analogValue);
  
  // Read battery voltage (1:2 divider, 3.74V ref, 12-bit ADC)
  int ubvalue = analogRead(ANALOG_PIN);
  analogVoltage = ubvalue * 2 * (3.74 / 4095.0);
  Serial.print("Battery: ");
  Serial.print(analogVoltage);
  Serial.println("V");
  
  // Calculate battery percentage (4.2V=100%, 3.5V=0%)
  float percent = constrain((analogVoltage - Ubmin) / (Ubmax - Ubmin) * 100.0, 0.0, 100.0);
  Serial.print("Battery: ");
  Serial.print(percent, 0);
  Serial.println("%");

  // Send JSON via Bluetooth
  StaticJsonDocument<200> doc;
  doc["CO2"] = (float)co2Concentration;
  doc["tmp"] = temperature;
  doc["hum"] = relativeHumidity;
  doc["VOC+CO"] = (float)analogValue;
  doc["Akku"] = percent;
  
  String output;
  serializeJson(doc, output);
  SerialBT.println(output);

  // CO2 warning (1000ppm = warning, 1500ppm = alarm)
  if (co2Concentration >= 1500) {
    digitalWrite(gelbLed, HIGH);
    tone(Lautsprecher_Pin, 820, 200);
    tone(Lautsprecher_Pin, 660, 200);
    tone(Lautsprecher_Pin, 820, 200);
    tone(Lautsprecher_Pin, 660, 200);
  } else if (co2Concentration >= 1000) {
    digitalWrite(gelbLed, HIGH);
    delay(500);
    digitalWrite(gelbLed, LOW);
    delay(500);
  } else {
    digitalWrite(gelbLed, LOW);
  }

  // VOC/CO warning (150 = warning, 200 = alarm)
  if (analogValue >= 200) {
    digitalWrite(rotLed, HIGH);
    tone(Lautsprecher_Pin, 820, 200);
    tone(Lautsprecher_Pin, 660, 200);
    tone(Lautsprecher_Pin, 820, 200);
    tone(Lautsprecher_Pin, 660, 200);
  } else if (analogValue >= 150) {
    digitalWrite(rotLed, HIGH);
    delay(500);
    digitalWrite(rotLed, LOW);
    delay(500);
  } else {
    digitalWrite(rotLed, LOW);
  }


  // Detailed gas readings from MICS5524
  float coConcentration = mics.getGasData(CO);
  float ch4Concentration = mics.getGasData(CH4);
  float c2h5ohConcentration = mics.getGasData(C2H5OH);
  float h2Concentration = mics.getGasData(H2);
  float nh3Concentration = mics.getGasData(NH3);
  float no2Concentration = mics.getGasData(NO2);

  Serial.print("CO: ");
  Serial.print(coConcentration, 1);
  Serial.println(" PPM");
  Serial.print("CH4: ");
  Serial.print(ch4Concentration, 1);
  Serial.println(" PPM");
  Serial.print("C2H5OH: ");
  Serial.print(c2h5ohConcentration, 1);
  Serial.println(" PPM");
  Serial.print("H2: ");
  Serial.print(h2Concentration, 1);
  Serial.println(" PPM");
  Serial.print("NH3: ");
  Serial.print(nh3Concentration, 1);
  Serial.println(" PPM");
  Serial.print("NO2: ");
  Serial.print(no2Concentration, 1);
  Serial.println(" PPM");
  Serial.println();

  delay(1000);
}
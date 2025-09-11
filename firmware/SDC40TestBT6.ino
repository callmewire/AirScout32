/*
 * AirScout32 - ESP32 Air Quality Monitor
 * Project:  Multi-parameter air quality monitoring with CO2, VOC/CO sensors
 * Author:   Paul Uhlmann
 * Date:     Created 2025
 * Version:  V2.0
 * IDE:      Arduino IDE 2.3.6
 * Board:    ESP32 by Espressif Systems V2.0.x
 * 
 * Connections:
 * VCC         3.3V             
 * GND         GND        
 * I2C SCL     GPIO 22         
 * I2C SDA     GPIO 21    
 * Buzzer      GPIO 17 (PWM)
 * Battery     GPIO 33 (ADC)
 * VOC/CO      GPIO 32 (ADC)
 */

#include <Arduino.h>
#include <SensirionI2cScd4x.h>
#include <Wire.h>
#include <BluetoothSerial.h>
#include <ArduinoJson.h>

BluetoothSerial SerialBT;

// Battery monitoring
const int BATTERY_PIN = 33;
const float BATTERY_MAX = 4.2;  // V max battery
const float BATTERY_MIN = 3.5;  // V min battery

// VOC/CO sensor (MiCS-5524)
const int VOC_CO_PIN = 32;
int vocBaseline = 0;

// Buzzer for alarms
const int BUZZER_PIN = 17;

// LED indicators
const int LED_RED = 15;
const int LED_YELLOW = 4;
const int LED_GREEN = 16;

// CO2 sensor
SensirionI2cScd4x sensor;
static char errorMessage[64];
static int16_t error;

// Timing
unsigned long lastMeasurement = 0;
const unsigned long MEASUREMENT_INTERVAL = 5000; // 5 seconds

void setup() {
  Serial.begin(115200);
  while (!Serial) {
    delay(100);
  }
  
  // Initialize I2C
  Wire.begin();
  sensor.begin(Wire, SCD41_I2C_ADDR_62);
  
  // Initialize LEDs
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_YELLOW, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  
  // Initialize buzzer
  pinMode(BUZZER_PIN, OUTPUT);
  
  // Initialize CO2 sensor
  initializeCO2Sensor();
  
  // Initialize Bluetooth
  SerialBT.begin("AirScout32");
  Serial.println("AirScout32 ready. Connect via Bluetooth.");
  
  // Calibrate VOC/CO sensor baseline
  calibrateVOCBaseline();
  
  // Startup indication
  digitalWrite(LED_GREEN, HIGH);
  delay(1000);
  digitalWrite(LED_GREEN, LOW);
}

void loop() {
  if (millis() - lastMeasurement >= MEASUREMENT_INTERVAL) {
    takeMeasurements();
    lastMeasurement = millis();
  }
  
  delay(100); // Small delay to prevent busy loop
}

void initializeCO2Sensor() {
  // Wake up sensor
  error = sensor.wakeUp();
  if (error != NO_ERROR) {
    Serial.println("Error waking up CO2 sensor");
    return;
  }
  
  // Stop any ongoing measurements
  error = sensor.stopPeriodicMeasurement();
  if (error != NO_ERROR) {
    Serial.println("Error stopping periodic measurement");
  }
  
  // Reinitialize sensor
  error = sensor.reinit();
  if (error != NO_ERROR) {
    Serial.println("Error reinitializing sensor");
  }
  
  Serial.println("CO2 sensor initialized successfully");
}

void calibrateVOCBaseline() {
  Serial.println("Calibrating VOC/CO baseline...");
  digitalWrite(LED_YELLOW, HIGH);
  
  // Take 20 readings and average them
  long sum = 0;
  for (int i = 0; i < 20; i++) {
    sum += analogRead(VOC_CO_PIN);
    delay(100);
  }
  vocBaseline = sum / 20;
  
  digitalWrite(LED_YELLOW, LOW);
  Serial.print("VOC/CO baseline set to: ");
  Serial.println(vocBaseline);
}

void takeMeasurements() {
  // CO2, temperature, humidity measurement
  uint16_t co2Concentration = 0;
  float temperature = 0;
  float relativeHumidity = 0;
  
  // Wake up CO2 sensor
  error = sensor.wakeUp();
  if (error != NO_ERROR) {
    Serial.println("Error waking up sensor");
    return;
  }
  
  // Take measurement
  error = sensor.measureAndReadSingleShot(co2Concentration, temperature, relativeHumidity);
  if (error != NO_ERROR) {
    Serial.println("Error reading CO2 sensor");
    return;
  }
  
  // VOC/CO measurement (relative to baseline)
  int vocRaw = analogRead(VOC_CO_PIN);
  float vocPercent = ((float)(vocRaw - vocBaseline) / vocBaseline) * 100.0;
  if (vocPercent < 0) vocPercent = 0; // Don't show negative values
  
  // Battery measurement
  int batteryRaw = analogRead(BATTERY_PIN);
  float batteryVoltage = batteryRaw * 2 * (3.3 / 4095.0); // Voltage divider compensation
  float batteryPercent = constrain((batteryVoltage - BATTERY_MIN) / (BATTERY_MAX - BATTERY_MIN) * 100.0, 0.0, 100.0);
  
  // Check for alarms
  checkAlarms(co2Concentration, vocPercent);
  
  // Print to serial
  printMeasurements(co2Concentration, temperature, relativeHumidity, vocPercent, batteryVoltage, batteryPercent);
  
  // Send via Bluetooth
  sendBluetoothData(co2Concentration, temperature, relativeHumidity, vocPercent, batteryPercent);
}

void checkAlarms(uint16_t co2, float voc) {
  bool alarm = false;
  
  // CO2 alarm (> 2000 ppm is concerning)
  if (co2 > 2000) {
    alarm = true;
    digitalWrite(LED_RED, HIGH);
  }
  
  // VOC alarm (> 100% increase from baseline)
  if (voc > 100.0) {
    alarm = true;
    digitalWrite(LED_RED, HIGH);
  }
  
  if (alarm) {
    // Sound alarm
    for (int i = 0; i < 3; i++) {
      tone(BUZZER_PIN, 1000, 200);
      delay(300);
    }
  } else {
    digitalWrite(LED_RED, LOW);
    digitalWrite(LED_GREEN, HIGH);
    delay(100);
    digitalWrite(LED_GREEN, LOW);
  }
}

void printMeasurements(uint16_t co2, float temp, float hum, float voc, float batV, float batP) {
  Serial.println("=== AirScout32 Measurements ===");
  Serial.print("CO2: "); Serial.print(co2); Serial.println(" ppm");
  Serial.print("Temperature: "); Serial.print(temp, 1); Serial.println(" °C");
  Serial.print("Humidity: "); Serial.print(hum, 0); Serial.println(" %RH");
  Serial.print("VOC/CO: +"); Serial.print(voc, 1); Serial.println(" % from baseline");
  Serial.print("Battery: "); Serial.print(batV, 2); Serial.print(" V ("); Serial.print(batP, 0); Serial.println(" %)");
  Serial.println();
}

void sendBluetoothData(uint16_t co2, float temp, float hum, float voc, float battery) {
  StaticJsonDocument<200> doc;
  
  doc["tmp"] = temp;
  doc["hum"] = hum;
  doc["gas1"] = co2;
  doc["gas2"] = voc;
  doc["akku"] = battery;
  
  String output;
  serializeJson(doc, output);
  SerialBT.println(output);
}
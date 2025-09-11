# AirScout32 ESP32 Firmware

Arduino firmware for the AirScout32 air quality monitoring device.

## Hardware Requirements

### Microcontroller
- **ESP32 DevKit V1** (30-pin version)
- **Arduino IDE 2.3.6** or newer

### Sensors
- **SCD40**: CO₂, temperature, humidity (I2C)
- **MiCS-5524**: VOC/CO detection (analog)

### Additional Components
- **18650 Li-Ion battery** with protection circuit
- **Voltage divider** for battery monitoring
- **Buzzer** for audio alarms
- **LEDs** for status indication

## Pin Configuration

| Component | ESP32 Pin | Description |
|-----------|-----------|-------------|
| SCD40 SDA | GPIO 21 | I2C data line |
| SCD40 SCL | GPIO 22 | I2C clock line |
| MiCS-5524 | GPIO 32 | Analog VOC/CO input |
| Battery Monitor | GPIO 33 | Battery voltage (via divider) |
| Buzzer | GPIO 17 | PWM audio output |
| LED Red | GPIO 15 | Alarm indicator |
| LED Yellow | GPIO 4 | Status indicator |
| LED Green | GPIO 16 | Normal operation |

## Required Libraries

Install these libraries through Arduino IDE Library Manager:
```
Sensirion I2C SCD4x by Sensirion AG (latest version)
ArduinoJson by Benoit Blanchon (v6.x or v7.x)
BluetoothSerial (included with ESP32 board package)
```

## Installation

1. **Install ESP32 Board Package**:
   - File → Preferences → Additional Board Manager URLs
   - Add: `https://dl.espressif.com/dl/package_esp32_index.json`
   - Tools → Board → Boards Manager → Search "ESP32" → Install

2. **Install Libraries**:
   - Sketch → Include Library → Manage Libraries
   - Install the required libraries listed above

3. **Upload Firmware**:
   - Connect ESP32 via USB
   - Select Board: "ESP32 Dev Module"
   - Select correct COM Port
   - Upload sketch

## Configuration

### Bluetooth Settings
- **Device Name**: "AirScout32"
- **Profile**: SPP (Serial Port Profile)
- **Auto-connect**: Disabled (manual pairing required)

### Sensor Calibration
- **CO₂ Sensor**: Factory calibrated, no user action needed
- **VOC/CO Sensor**: Automatic baseline calibration on startup
- **Calibration Time**: 60 seconds in clean air

### Alarm Thresholds
- **CO₂**: >2000 ppm (concerning level)
- **VOC/CO**: >100% increase from baseline
- **Battery**: <10% (low battery warning)

## Data Output

### JSON Format
```json
{
  "tmp": 23.5,
  "hum": 45.2,
  "gas1": 450,
  "gas2": 25.3,
  "akku": 87.2
}
```

### Field Descriptions
- `tmp`: Temperature in °C
- `hum`: Relative humidity in %
- `gas1`: CO₂ concentration in ppm
- `gas2`: VOC/CO level as % above baseline
- `akku`: Battery charge in %

## Operating Modes

### Normal Operation
- Green LED blinks every 5 seconds
- Continuous measurement and Bluetooth transmission
- Battery monitoring active

### Alarm Mode
- Red LED solid on
- Audio alarm (3 beeps)
- Triggered by high CO₂ or VOC levels

### Calibration Mode
- Yellow LED on during VOC baseline calibration
- Occurs automatically on startup
- Requires clean air environment

## Power Management

### Battery Life
- **Typical**: 8-10 hours continuous operation
- **Standby**: Deep sleep not implemented (continuous monitoring required)
- **Charging**: Via USB while operating

### Power Consumption
- **Active**: ~200mA average
- **CO₂ sensor**: ~20mA
- **VOC/CO sensor**: ~50mA
- **ESP32**: ~80-120mA (depending on Bluetooth activity)

## Troubleshooting

### Common Issues

**Bluetooth Not Connecting**:
- Ensure device is not paired with multiple devices
- Restart ESP32 and try pairing again
- Check if device name "AirScout32" appears in scan

**Sensor Readings Unstable**:
- Allow 2-3 minutes warm-up time after power on
- Ensure stable power supply (battery >20%)
- Check I2C connections (SDA/SCL)

**VOC Values Always Zero**:
- VOC sensor needs 30-60 seconds calibration in clean air
- Restart device in well-ventilated area
- Check analog pin connection (GPIO 32)

**Battery Percentage Wrong**:
- Check voltage divider circuit
- Verify battery voltage range (3.5V-4.2V)
- Calibrate voltage measurement if needed

### Serial Monitor Debug

Enable serial monitor (115200 baud) to see:
- Sensor initialization status
- Real-time measurements
- Error messages
- Calibration progress

### LED Status Indicators

| LED Color | Status | Meaning |
|-----------|--------|---------|
| Green Blink | Normal | System operating normally |
| Yellow Solid | Calibrating | VOC sensor calibration in progress |
| Red Solid | Alarm | Air quality threshold exceeded |
| All Off | Error | Check power and connections |

## Customization

### Modifying Thresholds
Edit these values in the code:
```cpp
// In checkAlarms() function
if (co2 > 2000) // Change CO₂ threshold
if (voc > 100.0) // Change VOC threshold percentage
```

### Changing Measurement Interval
```cpp
const unsigned long MEASUREMENT_INTERVAL = 5000; // milliseconds
```

### Adding New Sensors
1. Define new pin constants
2. Add initialization in `setup()`
3. Read sensor in `takeMeasurements()`
4. Add to JSON output in `sendBluetoothData()`

## Version History

- **V2.0**: Cleaned up code, improved error handling, better documentation
- **V1.2**: Added VOC baseline calibration, alarm system
- **V1.0**: Initial release with basic sensor reading

## License

Part of the AirScout32 project - see main LICENSE file.
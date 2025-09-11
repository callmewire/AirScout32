# AirScout32 - Mobile Air Quality Monitoring System

A complete open-source air quality monitoring solution combining ESP32-based hardware sensors with an Android application for real-time data visualization and analysis.

## 🌟 Project Overview

AirScout32 is a cost-effective, portable air quality measurement device developed as part of a Bachelor's thesis in Media Informatics. The system addresses the gap between expensive professional equipment and basic consumer devices by providing reliable multi-parameter air quality monitoring for under €100.

**Project Name**: AirScout32 reflects three core aspects:
- **Air**: Focus on air quality measurement
- **Scout**: Mobile reconnaissance and warning functionality  
- **32**: Based on the ESP32 microcontroller platform

## 📁 Repository Structure
```
AirScout32/
├── android-app/          # Android application source code
├── firmware/             # ESP32 Arduino firmware
├── hardware/             # 3D models, schematics, and assembly guides
├── docs/                 # Documentation and user manuals
└── README.md            # This file
```

## 🔧 Hardware Components

### Sensors
- **SCD40**: CO₂ measurement (400-5000 ppm) via NDIR technology
- **MiCS-5524**: VOC/CO detection with baseline calibration
- **Integrated**: Temperature and humidity monitoring

### Core System
- **ESP32 DevKit**: Dual-core microcontroller with Bluetooth/WiFi
- **18650 Li-Ion**: 8-10 hour battery life with charging management
- **Custom Enclosure**: 3D-printed dual-chamber design for thermal isolation

### Key Features
- **Autonomous alarms**: Independent safety warnings without smartphone
- **Transport stable**: No recalibration needed after movement
- **External mounting**: Prevents measurement interference from user breathing
- **Modular design**: Easy sensor expansion via I2C/analog interfaces

## 📱 Android Application

### Real-time Monitoring
- Live data visualization with interactive charts
- Bluetooth connectivity via Classic Bluetooth (SPP)
- Current values for all sensor parameters and battery status
- Configurable chart limits for performance optimization

### Session Management
- Session-based data collection with custom names
- Complete data storage independent of chart display limits
- CSV export for scientific analysis
- Metadata capture (start time, duration, data point count)

### Flexible Configuration
- Dynamic JSON mapping for different ESP32 sensor configurations
- Customizable parameter names without app recompilation
- Chart data point limitation (50-1000 points)
- User-friendly settings interface

## 🏗️ Technical Architecture

### Hardware-Software Integration
```
ESP32 Sensors → Bluetooth → Android App → Local Database → CSV Export
```

### Communication Protocol
- **Bluetooth Classic (SPP)**: Reliable connection for mobile use
- **JSON data format**: Human-readable and extensible
- **5-second intervals**: Balance between responsiveness and battery life

### Android MVVM Pattern
```
View (Fragments) ↔ ViewModel ↔ Repository (Database + Bluetooth)
```

## 📊 Supported Measurements

### Air Quality Parameters
- **CO₂**: 400-5000 ppm (±50 ppm accuracy)
- **VOCs**: Relative to baseline (MOX sensor)
- **CO**: 1-1000 ppm detection range
- **Temperature**: Integrated environmental monitoring
- **Humidity**: Relative humidity measurement

### Standard JSON Format
```json
{
  "tmp": 25.5,
  "hum": 60.2,
  "gas1": 450,
  "gas2": 320,
  "akku": 3.7
}
```

## 🚀 Getting Started

### Hardware Setup
1. **3D print enclosure** using provided STL files
2. **Assemble electronics** following wiring diagram
3. **Flash firmware** using Arduino IDE
4. **Calibrate sensors** in clean air environment

### Software Setup
1. **Install Android app** from releases or build from source
2. **Pair ESP32** in Android Bluetooth settings
3. **Configure JSON mapping** according to sensor setup
4. **Connect and start monitoring**

### Quick Start
1. Power on AirScout32 device
2. Open Android app and connect via Bluetooth
3. Monitor real-time values in Realtime tab
4. Save measurement sessions for later analysis
5. Export data as CSV for scientific evaluation

## 🔬 Scientific Applications

### Use Cases
- **Indoor air quality assessment** in homes and offices
- **Renovation monitoring** for VOC emissions
- **Workplace safety** in industrial environments
- **Educational demonstrations** of air quality concepts
- **Research data collection** for environmental studies

### Research Contributions
- Cost-effective multi-parameter air quality monitoring
- Mobile sensor platform with transport stability
- Open-source design for reproducible research
- Modular architecture for custom sensor integration

## 💾 Data Management

### Export Features
- Session metadata in CSV headers
- Timestamps in Unix and human-readable formats
- All parameters including battery status
- Android sharing for direct data transfer

### Privacy & Security
- Local data storage only (no cloud dependency)
- User-controlled data export
- Minimal permission requirements

## 📈 Performance & Reliability

### Battery Life
- **8-10 hours** continuous operation
- **2.5 hours** charging time
- **Intelligent power management**

### Measurement Accuracy
- **Factory-calibrated sensors** for CO₂
- **Baseline calibration** for relative VOC measurements
- **Transport-stable** readings without recalibration

### Build Quality
- **Dual-chamber design** prevents thermal interference
- **Heat-set inserts** for professional assembly
- **IP54-rated** for normal indoor use

## 🛠️ Development

### Building Android App
```bash
# Clone repository
git clone https://github.com/username/AirScout32
cd AirScout32/android-app

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test
```

### Flashing ESP32 Firmware
```bash
# Open firmware/AirScout32.ino in Arduino IDE
# Install required libraries (listed in firmware/README.md)
# Select ESP32 board and flash
```

### 3D Printing
- **Material**: PETG recommended for final version
- **Settings**: 0.2mm layer height, 30% infill
- **Post-processing**: Install heat-set inserts for assembly

## 🤝 Contributing

This project welcomes contributions:
- **Hardware improvements**: Sensor integration, enclosure design
- **Software features**: New visualization modes, data analysis
- **Documentation**: User guides, assembly instructions
- **Testing**: Real-world validation in different environments

## 📚 Technologies Used

### Hardware
- **ESP32**: Espressif Systems microcontroller
- **Arduino IDE**: Firmware development environment
- **Fusion 360**: 3D CAD modeling
- **PETG/PLA**: 3D printing materials

### Software
- **Kotlin**: Android development language
- **Android Jetpack**: Modern Android architecture
- **Room Database**: Local data persistence
- **MPAndroidChart**: Real-time data visualization
- **Bluetooth Classic**: ESP32 communication

## 📄 Academic Context

Developed as part of a Bachelor's thesis in Media Informatics demonstrating:
- **IoT system integration** with mobile applications
- **Cost-effective sensor solutions** for environmental monitoring
- **User-centered design** for scientific instruments
- **Open-source hardware/software** development practices

### Thesis Contributions
- Market analysis of portable air quality monitors
- Technical evaluation of low-cost sensor technologies
- Mobile app architecture for real-time sensor data
- Validation through real-world testing scenarios

## 📊 Project Results

### Cost Analysis
- **€100 prototype cost** vs €800-2000 commercial alternatives
- **75% cost reduction** in small-scale production
- **Open-source licensing** eliminates proprietary costs

### Performance Validation
- **8.5 hour average** battery life in field tests
- **Transport-stable** measurements without recalibration
- **3.5 minute setup** time for non-technical users
- **Reliable Bluetooth** connection up to 12 meters indoors

## 📧 Contact & License

Developed for educational purposes as part of a Bachelor's thesis in Media Informatics.

**Project**: Mobile Air Quality Monitoring with ESP32 and Android  
**Institution**: Berliner Hochschule für Technik
**Year**: 2025

This project is open-source - see individual component licenses for details.
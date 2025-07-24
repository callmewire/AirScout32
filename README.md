# AirScout32 - ESP32 Air Quality Monitoring App

An Android application for monitoring air quality parameters via Bluetooth connection with ESP32-based sensors.

## 📱 Features

### Real-time Monitoring
- **Live data visualization** with interactive charts
- **Bluetooth connectivity** to ESP32 devices via Classic Bluetooth (SPP)
- **Current values** for temperature, humidity, gas sensors, and battery status
- **Configurable chart limits** for performance optimization

### Session Management
- **Session-based data collection** with custom names
- **Complete data storage** independent of chart display limits
- **CSV export** for scientific analysis
- **Metadata capture** (start time, duration, data point count)

### Flexible Configuration
- **Dynamic JSON mapping** for different ESP32 sensor configurations
- **Customizable parameter names** without app recompilation
- **Chart data point limitation** (10-1000 points)
- **User-friendly settings**

### Data Management
- **Local SQLite database** with Room framework
- **Session archiving** with search functions
- **Bulk export functions**
- **Data sharing** via Android standard APIs

## 🏗️ Technical Architecture

### MVVM Pattern
```
View (Fragments) ←→ ViewModel ←→ Repository (Database + Bluetooth)
```

### Main Components
- **BluetoothService**: ESP32 communication via SPP
- **AirDataViewModel**: Central business logic and data coordination
- **Room Database**: Local persistence with AirSensorData and SessionData
- **JsonMappingConfig**: Dynamic JSON parameter configuration
- **ChartSettingsConfig**: Performance settings for data visualization

### Data Flow
1. **ESP32** sends JSON data via Bluetooth
2. **BluetoothService** parses data with configurable mapping
3. **ViewModel** manages both chart display and complete session data
4. **UI** shows live charts and enables session saving
5. **Database** persists sessions for later analysis

## 📊 Supported Data Types

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

### Configurable Parameters
- **Temperature**: `tmp`, `temp`, `temperature`, etc.
- **Humidity**: `hum`, `humidity`, `rh`, etc.
- **Gas Sensors**: `gas1`, `gas2`, `co2`, `voc`, etc.
- **Battery**: `akku`, `battery`, `bat`, `voltage`, etc.

## 🔧 Setup and Configuration

### ESP32 Requirements
- **Bluetooth Classic** (SPP Profile)
- **JSON output** via Serial Bluetooth
- **UUID**: `00001101-0000-1000-8000-00805F9B34FB`

### Android Requirements
- **Android 6.0+** (API Level 23)
- **Bluetooth Permissions**: BLUETOOTH_CONNECT, BLUETOOTH_SCAN
- **Location Permission**: ACCESS_FINE_LOCATION (for Bluetooth scan)
- **Storage Permission**: For CSV export

### Getting Started
1. **Pair ESP32** in Android Bluetooth settings
2. **Start app** and navigate to "Settings"
3. **Configure JSON mapping** according to ESP32 parameters
4. **Set chart limit** based on device performance
5. **Connect Bluetooth** in "Realtime" tab
6. **Start data collection** and save sessions

## 📱 Navigation and Usage

### Realtime Tab
- **Live charts** for all sensor values
- **Current values** in numerical display
- **Bluetooth connection** management
- **Save session** with custom name
- **Reset data** for new measurements

### Historical Tab
- **Browse saved sessions**
- **Session details** display (date, duration, data points)
- **CSV export** for individual sessions
- **Delete session** or delete all sessions
- **Long-press** for context menu

### Settings Tab
- **JSON mapping** for ESP32 compatibility
- **Chart settings** for performance optimization
- **Reset functions** for current data
- **Save/reset configuration**

## 💾 Data Export

### CSV Format
```csv
Session: Lab Measurement 1
Start: 15.12.2023 14:30:15
End: 15.12.2023 15:45:30
Duration: 75 minutes
Data Points: 450

Timestamp,Date,Time,Temperature,Humidity,Gas1,Gas2,Battery
1702649415000,2023-12-15,14:30:15,23.5,58.2,420,380,3.72
...
```

### Export Features
- **Session metadata** in CSV header
- **Timestamps** in Unix format and human-readable
- **All parameters** including battery status
- **Android sharing** for direct sending
- **File provider** for secure file sharing

## 🔒 Security and Privacy

### Local Data Storage
- **No cloud connection** required
- **SQLite encryption** possible (configurable)
- **App-internal storage** prevents external access

### Permission Management
- **Granular permissions** only when needed
- **Runtime permissions** for user control
- **Minimal access** to system resources

## 🚀 Performance Optimizations

### Memory Management
- **FIFO principle** for chart data
- **Separate lists** for display and storage
- **Garbage collection** through limited live data

### Battery Optimization
- **Foreground service** avoided
- **Efficient Bluetooth** without background scanning
- **Adaptive sampling** during inactivity (planned)

## 🔄 Development and Extension

### Architecture Advantages
- **Modular structure** enables easy extensions
- **Dependency injection** for testability
- **Clean architecture** with clear layer separation

### Planned Features
- **Bluetooth Low Energy** support
- **Cloud synchronization** (optional)
- **Alarm system** for critical values
- **Advanced analytics** with trends and patterns
- **Multi-device support** for multiple ESP32 devices

### Development
```bash
# Build Debug APK
./gradlew assembleDebug

# Build Release APK
./gradlew assembleRelease

# Run Tests
./gradlew test
```

## 📚 Technologies Used

### Android Framework
- **Kotlin** - Main programming language
- **Android Jetpack** - Architecture Components
- **View Binding** - Type-safe UI references
- **Room Database** - SQLite abstraction
- **Coroutines** - Asynchronous programming

### UI/UX Libraries
- **Material Design** - Google Design Language
- **MPAndroidChart** - Data visualization
- **Bottom Navigation** - Intuitive navigation

### Bluetooth & Connectivity
- **Classic Bluetooth** - ESP32 compatibility
- **RFCOMM/SPP** - Serial Port Profile
- **JSON Parsing** - Gson Library

### Data Management
- **SQLite** - Local database
- **SharedPreferences** - Configuration storage
- **File Provider** - Secure file sharing

## 🤝 Bachelor Thesis Context

This application was developed as part of a Bachelor's thesis in Media Informatics. It demonstrates:

- **IoT integration** in mobile applications
- **Modern Android development** with current best practices
- **User-centered design** for scientific applications
- **Performance optimization** for embedded system communication
- **Data management** for scientific evaluations

### Scientific Contribution
- **Flexible sensor integration** without hardcoding
- **Real-time data processing** with limited resources
- **User experience** for technical applications
- **Data quality** and integrity in mobile apps

## 📄 License

This project was developed for educational purposes as part of a Bachelor's thesis.

## 📧 Contact

Developed as part of Bachelor's Thesis in Media Informatics
- **Project**: ESP32 Air Quality Measurement Device with Android App
- **Year**: 2024
# AirScout32 Measurement Data

This directory contains the raw measurement data collected during the evaluation phase of the AirScout32 project. All datasets are provided in CSV format for maximum compatibility and reproducibility.

## Dataset Overview

| Filename | Description | Duration | Sensor Type | Test Conditions |
|----------|-------------|----------|-------------|-----------------|
| `session_A_20250914_150134.csv` | Baseline measurement (empty room) | 34 minutes | SCD40 + MiCS-5524 | No occupants, closed room |
| `session_B_20250914_153851.csv` | Two-person occupancy test | 38 minutes | SCD40 + MiCS-5524 | 2 people, closed room |
| `MH-Z19_validation_data.csv` | MH-Z19 sensor validation | 22 hours | MH-Z19 + MiCS-5524 | Long-term stability test |

## Data Format

All CSV files follow the same structure exported from the AirScout32 Android application:
```csv
Session: [Session Name]
Start: [Date Time]
End: [Date Time]  
Duration: [X minutes]
Data Points: [Count]

Timestamp,Date,Time,Temperature,Humidity,Gas1,Gas2,Battery
[Unix timestamp],[Date],[Time],[°C],[%RH],[ppm/index],[index/ppm],[%]
```

### Field Descriptions

- **Timestamp**: Unix timestamp in milliseconds
- **Date**: Measurement date (YYYY-MM-DD)
- **Time**: Measurement time (HH:MM:SS)
- **Temperature**: Ambient temperature in degrees Celsius
- **Humidity**: Relative humidity in percent
- **Gas1**: CO₂ concentration in ppm (SCD40/MH-Z19 sensor)
- **Gas2**: VOC/CO index relative to baseline (MiCS-5524 sensor)
- **Battery**: Battery charge level in percent

## Test Conditions

### Session A - Baseline Measurement
- **Location**: Indoor room (4m × 5m × 2.5m = 50m³)
- **Occupancy**: Empty room
- **Ventilation**: Closed windows and doors
- **Purpose**: Establish baseline air quality values
- **Initial Conditions**: Room previously occupied, CO₂ levels stabilizing

### Session B - Occupancy Test
- **Location**: Same room as Session A
- **Occupancy**: 2 people (light activity: reading, computer work)
- **Ventilation**: Closed windows and doors
- **Purpose**: Demonstrate CO₂ accumulation with human presence
- **Termination**: Test stopped at 1973 ppm CO₂ (approaching 2000 ppm alarm threshold)

### MH-Z19 Validation Data
- **Purpose**: Long-term sensor validation and stability assessment
- **Sensor**: MH-Z19 NDIR CO₂ sensor for reference comparison
- **Duration**: 22 hours (1320 minutes) with sparse data collection
- **Conditions**: Extended operation test
- **Note**: Only Gas1 (CO₂) values recorded (473-816 ppm range), other parameters set to 0.0

## Data Usage

These datasets can be used to:
- Reproduce the analysis presented in the thesis
- Validate the AirScout32 system performance
- Compare different sensor technologies (SCD40 vs MH-Z19)
- Analyze indoor air quality patterns
- Develop or test data analysis algorithms

## Data Quality Notes

- Measurement interval: Approximately 10-11 seconds between data points
- Sensor warm-up: First 2-3 minutes may show stabilization behavior
- VOC baseline: MiCS-5524 sensor uses relative measurements (baseline = 100)
- CO₂ accuracy: ±50 ppm (manufacturer specification for SCD40 and MH-Z19)

## Reproducibility

To reproduce the measurements:
1. Use AirScout32 hardware with SCD40 and MiCS-5524 sensors
2. Follow test conditions as described above
3. Use the Android application for data collection
4. Export sessions in CSV format

## Citation

When using this data, please cite:
```
Uhlmann, P. (2024). AirScout32 Measurement Data. 
Bachelor's Thesis: Mobile Air Quality Monitoring with ESP32 and Android.
Available at: https://github.com/callmewire/AirScout32
```

## Contact

For questions about the data or measurement procedures, please refer to the main thesis document or open an issue in the repository.

## License

This data is provided under the same license as the main AirScout32 project. See the main LICENSE file for details.
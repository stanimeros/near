# Near

Near is a social networking application developed as part of an undergraduate thesis at the University of Macedonia. It prioritizes privacy using the Two Hop Privacy algorithm and offers location-based services within a specified range.

#### [New Version in Flutter](https://github.com/stanimeros/near-flutter)

## Features

- **Privacy Focus**: Utilizes the Two Hop Privacy algorithm to enhance user privacy.
- **Location-based Services**: Provides functionalities based on the user's proximity to the University of Macedonia (within 100km).
- **Multiple Data Handling Methods**: Supports various methods for efficient data handling and retrieval, tailored to different dataset sizes and computational capabilities.

## Requirements

- **Minimum API Level**: API 21

## Configuration

### MainActivity.java Parameters

- **Algorithm Execution Method**: Select from predefined options for algorithm execution.
- **k-anonymity (k)**: Adjustable parameter for enhancing privacy.
- **kmFile**: Specifies dataset granularity (1km, 5km, 25km, or 100km).
- **starting_km**: Initial bounding box value with a default of 0.05.
- **Tree Parameters (KD-Tree and Quad Tree)**:
  - **tree max points**: Maximum number of points for tree structures.
  - **tree leaf max points**: Maximum points per leaf (bucket).

### Supported Methods

- **linear**: General-purpose method suitable for various scenarios, albeit slower with large datasets.
- **sqlite_default**: Universal compatibility with a longer initial setup time.
- **sqlite_spatialite**: Fast loading for smaller datasets (1km, 5km) from assets.
- **sqlserver**: Efficient for datasets up to 25km but not suitable for 100km.
- **kd and quad**: Memory-efficient once data is loaded, suitable for smaller datasets.
- **rtree**: Efficient post-loading but unable to handle the largest dataset (100km).
- **direct**: Rapid data retrieval from Overpass API with quad tree implementation (sub-300ms).

## Additional Notes

- **100km Dataset**: Not included in assets due to size; handled separately.
- **Feed.java**: Monitors GPS for location changes and initiates relevant processes.
- **MyLocation.java**: Executes the selected method for location-based operations.

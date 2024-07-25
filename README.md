# Near

Near is a social networking android application developed as part of an undergraduate thesis at the University of Macedonia. It prioritizes privacy through the implementation of the Two Hop Privacy algorithm. Users can view nearby friends based on shared points of interest (POIs) as their location. It serves as an experimental environment to explore efficient methods, including SQLite, Spatialite, KD-Tree, R-Tree, QD-Tree and web service, for integrating and querying location data while prioritizing user privacy.

#### [New Version in Flutter](https://github.com/stanimeros/near-flutter)

## Features

- **Social Networking**: Users can send friend requests and build friendships within the app.
- **Location-Based Friend Finder**: View friends who are nearby based on shared points of interest (POIs).
- **Privacy-First**: Implements the Two Hop Privacy algorithm to prioritize user privacy.
- **OpenStreetMap Integration**: Utilizes points of interest (POIs) data from OpenStreetMap (OSM).

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

<p align="center">
<img src="https://github.com/user-attachments/assets/82a38dce-d459-4f26-b4bc-e5f6615dd564" width="30%"></img> 
<img src="https://github.com/user-attachments/assets/a61f2024-3c2b-40c6-83c0-b8dfc0d8ed38" width="30%"></img> 
<img src="https://github.com/user-attachments/assets/91583066-e6f4-4051-9c5e-8695bc12b144" width="30%"></img> 
</p>
<p align="center">
<img src="https://github.com/user-attachments/assets/eba43212-982a-4113-9041-5cdfb4f7aa96" width="30%"></img> 
<img src="https://github.com/user-attachments/assets/3f9e6599-6604-40ce-92ad-9749e2cd9c86" width="30%"></img> 
<img src="https://github.com/user-attachments/assets/14d80355-19da-4d60-96fe-69459d437f4c" width="30%"></img>
</p>
<br />
<br />
<img src="https://github.com/user-attachments/assets/494005cc-ce86-4a50-b10f-bfd903c73da6" width="97%"></img> 






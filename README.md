# Near
This application was created as part of my undergraduate thesis at the University of Macedonia. It is a social networking application with a focus on privacy based on the Two Hop Privacy algorithm.

## Configuration
- **API Requirements:** API 21+, designed on API 26 x86
- **Permissions:** Does not request storage, call, or contacts permissions yet.
- **Mobile Location:** The mobile location must be within 100km of the University of Macedonia.

In the MainActivity.java, the following parameters are present:
- Method parameter for selecting the algorithm execution method (predefined options).
- Parameter 'k' representing k-anonymity, which can take any value.
- Parameter 'kmFile' indicating which table or database to look at in the project. It can strictly be 1km, 5km, 25km, or 100km, but does not work with all methods.
- Parameter 'starting_km' representing the initial value for the bounding box with a default value of 0.05.
- For trees (KD-Tree and Quad Tree), there are tree max points and the corresponding tree leaf max points, representing the bucket max. A large number (e.g., 2 million) for tree max points will not activate group management, while a small number (e.g., 100k) will create as many trees as needed with the help of the group helper. The numbers are specific because the largest dataset (100km) has 1.6 million points.

## Methods
For the smooth operation of the application, the following parameter combinations are available:
- **linear:** Works for all combinations but delays with a large volume of data.
- **sqlite_default:** Works for all combinations but takes a few minutes to create the database on the first run. //Not time-efficient.
- **sqlite_spatialite:** Works for all combinations, takes a few minutes to create the 25km and 100km databases, while the 1km, 5km databases are loaded directly from the assets.
- **sqlserver:** Works well for the uploaded 1km, 5km, and 25km datasets. Will not work for 100km.
- **kd and quad:** Work well once loaded into memory. Takes a few seconds. The 1km, 5km, and even 25km can be handled with one tree without the group manager. Beyond that, it burdens the memory significantly. Group helpers are a solution starting when dataset points > tree max points.
- **rtree:** Works perfectly once loaded into memory. Cannot handle 100km.
- **direct:** This method has been added. It downloads data from the Overpass API, creates a quad tree, and finds nearby points in under 300ms.

## Additional Information
- The 100km dataset is not in the assets in the project due to its size.
- The MainActivity.java file contains basic parameters.
- In the Feed.java file, GPS detects a change in location and initiates the process.
- In the MyLocation.java file, the selected method begins.

## Server
- The search for nearby points is done through a service, while all other operations (friendships, requests, friend lists) are done through JDBC.

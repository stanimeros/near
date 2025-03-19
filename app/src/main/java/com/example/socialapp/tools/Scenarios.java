package com.example.socialapp.tools;
import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.example.socialapp.GeoPoint;
import com.example.socialapp.MainActivity;
import com.example.socialapp.MyLocation;
import com.example.socialapp.methods.RTreeHelper;
import com.example.socialapp.methods.SQLiteDefault;
import com.example.socialapp.methods.SQLiteSpatialite;
import com.example.socialapp.methods.kd.KDTreeGroup;
import com.example.socialapp.methods.quad.QuadTreeGroup;

public class Scenarios {
    public Context context;
    // Test points around the organization
    private final GeoPoint[] points = new GeoPoint[] {
        new GeoPoint("22.9346", "40.6396"),
        new GeoPoint("22.9453", "40.6431"),
        new GeoPoint("22.9575", "40.6392"),
        new GeoPoint("22.9703", "40.6207"),
        new GeoPoint("22.9592", "40.6078")
    };

    // Dataset configurations
    private final int[] datasetSizes = {5, 25, 100}; // kilometers
    private final int[] kValues = {5, 25, 100};      // number of nearest neighbors
    private final String[] methods = {
        "brute_force",    // Linear search
        "kdtree",         // KD-Tree implementation
        "quadtree",       // QuadTree implementation
        "rtree",          // R-Tree implementation
        "sqlite",         // Basic SQLite
        "spatialite",     // SQLite with Spatialite
        "mariadb"         // External MariaDB proxy
    };

    private static final int NUM_RUNS = 5; // Number of times to run each experiment

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void runAllScenarios() {
        // 1. Fixed k=25 with varying dataset sizes
        for (int datasetSize : datasetSizes) {
            MainActivity.k = 25;
            MainActivity.kmNum = datasetSize;

            try {
                SQLiteDefault sqLiteDefault = new SQLiteDefault(context);
                SQLiteSpatialite sqLiteSpatialite = new SQLiteSpatialite(context);

                KDTreeGroup.Initialize(MainActivity.treeMaxPoints, MainActivity.KDTreeLeafMaxPoints, MainActivity.kmNum + "km_sorted.txt", context);
                QuadTreeGroup.Initialize(MainActivity.treeMaxPoints, MainActivity.QuadTreeLeafMaxPoints, MainActivity.kmNum + "km_sorted.txt", context);
                RTreeHelper.createRTree(context);
            } catch (Exception e) {
                e.printStackTrace();
            }

            
            for (String method : methods) {
                runExperiment(method, datasetSize, 25);
            }
        }

        // 2. Fixed dataset size (25km) with varying k
        for (int k : kValues) {
            MainActivity.k = k;
            MainActivity.kmNum = 25;

            try {
                SQLiteDefault sqLiteDefault = new SQLiteDefault(context);
                SQLiteSpatialite sqLiteSpatialite = new SQLiteSpatialite(context);

                KDTreeGroup.Initialize(MainActivity.treeMaxPoints,MainActivity.KDTreeLeafMaxPoints,MainActivity.kmNum + "km_sorted.txt", context);
                QuadTreeGroup.Initialize(MainActivity.treeMaxPoints,MainActivity.QuadTreeLeafMaxPoints,MainActivity.kmNum + "km_sorted.txt", context);
                RTreeHelper.createRTree(context);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (String method : methods) {
                runExperiment(method, 25, k);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void runExperiment(String method, int datasetSize, int k) {
        List<Long> times = new ArrayList<>();
        MyLocation myLocation = new MyLocation();
        
        for (int run = 0; run < NUM_RUNS; run++) {
            long startTime = System.currentTimeMillis();
            
            // Run the specific method implementation
            for (GeoPoint point : points) {

                switch (method) {
                    case "brute_force":
                        myLocation.setMyPointOfInterestLinearSearch(point, "phone", k, context);
                        break;
                    case "kdtree":
                        myLocation.setMyPointOfInterestKDTreeSearch(point, "phone", k, context);
                        break;
                    case "quadtree":
                        myLocation.setMyPointOfInterestQuadTreeSearch(point, "phone", k, context);
                        break;
                    case "rtree":
                        myLocation.setMyPointOfInterestRTreeSearch(point, "phone", k);
                        break;
                    case "sqlite":
                        myLocation.setMyPointOfInterestSQLiteDefaultSearch(point, "phone", k, context);
                        break;
                    case "spatialite":
                        myLocation.setMyPointOfInterestSQLiteSpatialiteSearch(point, "phone", k, context);
                        break;
                    // case "mariadb":
                    //     myLocation.setMyPointOfInterestSQLServerSearch(point, "phone", k);
                    //     break;
                }
            }
            
            long endTime = System.currentTimeMillis();
            times.add(endTime - startTime);
        }
        
        // Calculate and store average time
        double avgTime = times.stream().mapToLong(Long::longValue).average().orElse(0.0);
        System.out.printf("Method: %s, Dataset: %dkm, k: %d, Avg Time: %.2fms%n", 
                         method, datasetSize, k, avgTime);
    }
}

package com.example.socialapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import jsqlite.Database;
import jsqlite.Stmt;

public class SQLiteSpatialite extends SQLiteOpenHelper {
    Context context;
    private static final String DB_NAME = "db_geopoints";
    private static final String TABLE_NAME = "geopoints";

    public SQLiteSpatialite(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            System.out.println("CREATING DB..");

            String query = "CREATE TABLE " + TABLE_NAME + " ("
                    + "lon REAL,"
                    + "lat REAL)";

            db.execSQL(query);
            System.out.println("TABLE CREATED!");

            System.out.println("INSERTING FROM TEXT FILE..");
            BufferedReader file = new BufferedReader(new InputStreamReader(context.getAssets().open(MainActivity.unsorted_input)));
            String line = file.readLine();
            while (line != null) {
                int br = line.indexOf("-");
                String lon = line.substring(0, br);
                String lat = line.substring(br + 1);

                query = "INSERT INTO " + TABLE_NAME + " VALUES ("
                        + lon + ","
                        + lat + ")";

                db.execSQL(query);
                line = file.readLine();
            }
            file.close();
            System.out.println("SQLITE DB CREATED SUCCESSFULLY!");
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Error while loading from text file!");
        }
    }

    public void Initialize(Context context){
        try {
            File dbFile = context.getDatabasePath(DB_NAME);

            Database db = new jsqlite.Database();
            db.open(dbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);

            String query;
            Stmt stmt;

            System.out.println(queryVersions(db));

            query = "SELECT InitSpatialMetaData(1);";
            System.out.println(query);
            stmt = db.prepare(query);
            while( stmt.step() ) {
                System.out.println(stmt.column_string(0));
            }

            query = "SELECT AddGeometryColumn('geopoints', 'loc', 4326, 'POINT');";
            //query = "SELECT AddGeometryColumn('geopoints', 'loc', 4326, 'POINT','XY');";
            System.out.println(query);
            stmt = db.prepare(query);
            while( stmt.step() ) {
                System.out.println(stmt.column_string(0));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void createSpatialColumn(Context context){
        try {
            File dbFile = context.getDatabasePath(DB_NAME);

            Database db = new jsqlite.Database();
            db.open(dbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);

            String query;
            Stmt stmt;

            query = "UPDATE geopoints " +
                    "SET loc = MakePoint(lon, lat, 4326);";
            System.out.println(query);
            stmt = db.prepare(query);
            while( stmt.step() ) {
                System.out.println(stmt.column_string(0));
            }

            query = "SELECT CreateSpatialIndex('geopoints', 'loc');";
            System.out.println(query);
            stmt = db.prepare(query);
            while( stmt.step() ) {
                System.out.println(stmt.column_string(0));
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<GeoPoint> getKNearestList(int k, GeoPoint target, double distanceInKm, Context context) {
        ArrayList<GeoPoint> kNearestList = new ArrayList<>();
        try {
            /*
            SQLiteDatabase db = this.getWritableDatabase();
            Database db_spatialite = new Database();

            db_spatialite.open(":memory:", Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE);
            System.out.println("ATTACH DATABASE '" + db.getPath() + "' AS attached");
            Stmt stmt = db_spatialite.prepare("ATTACH DATABASE '" + db.getPath() + "' AS attached"); //then -> FROM attached.db_name
            while (stmt.step()){
                System.out.println(stmt.column_string(0));
            }
            stmt.close();
             */
            File dbFile = context.getDatabasePath(DB_NAME);

            Database db = new jsqlite.Database();
            db.open(dbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);

            final double sqrt2 = Math.sqrt(2);

            while (kNearestList.size()<k){
                Stmt stmt = null;
                System.out.println("Executing query ..");
                float distance = (float) (distanceInKm/100);

                /*
                String lon_lat_spatial_with_distance = "SELECT lon, lat, ST_Distance(ST_GeomFromText('POINT("+target.getLon()+" "+target.getLat()+")',4326),ST_GeomFromText('POINT(' || lon || ' ' || lat || ')',4326),1) AS distance " +
                        "FROM geopoints " +
                        "WHERE ST_Within(ST_GeomFromText('POINT(' || lon || ' ' || lat || ')',4326),ST_Buffer(ST_GeomFromText('POINT("+target.getLon()+" "+target.getLat()+")', 4326),"+distance+")) " +
                        "ORDER BY distance " +
                        "ASC LIMIT " + k; //THIS QUERY NEEDS ONLY LON AND LAT

                String loc_spatial_with_distance = "SELECT ST_X(loc) AS longitude, ST_Y(loc)AS latitude, ST_Distance(ST_GeomFromText('POINT("+target.getLon()+" "+target.getLat()+")', 4326),loc,1) AS distance " +
                        "FROM geopoints " +
                        "WHERE ST_Within(loc,ST_Buffer(ST_GeomFromText('POINT("+target.getLon()+" "+target.getLat()+")', 4326),"+distance+")) " +
                        "ORDER BY distance " +
                        "ASC LIMIT " + k; //THIS QUERY NEEDS LOC COLUMN

                String haversine_with_distance = "SELECT lon, lat, " +
                        "6371 * 2 * ASIN(SQRT(POWER(SIN((RADIANS(lat - " + target.getLat() + ")) / 2), 2) + " +
                        "COS(RADIANS(" + target.getLat() + ")) * COS(RADIANS(lat)) * POWER(SIN((RADIANS(lon - " + target.getLon() + ")) / 2), 2))) " +
                        "AS distance " +
                        "FROM " + TABLE_NAME + " " +
                        "ORDER BY distance ASC " +
                        "ASC LIMIT " + k; //THIS QUERY NEEDS ONLY LON AND LAT

                String haversine = "SELECT lon, lat " +
                        "FROM " + TABLE_NAME + " " +
                        "ORDER BY 6371 * 2 * ASIN(SQRT(POWER(SIN((RADIANS(lat - " + target.getLat() + ")) / 2), 2) + " +
                        "COS(RADIANS(" + target.getLat() + ")) * COS(RADIANS(lat)) * POWER(SIN((RADIANS(lon - " + target.getLon() + ")) / 2), 2))) ASC " +
                        "ASC LIMIT " + k; //THIS QUERY NEEDS ONLY LON AND LAT

                String lon_lat_spatial = "SELECT lon, lat " +
                        "FROM geopoints " +
                        "WHERE ST_Within(ST_GeomFromText('POINT(' || lon || ' ' || lat || ')',4326),ST_Buffer(ST_GeomFromText('POINT("+target.getLon()+" "+target.getLat()+")', 4326),"+distance+")) " +
                        "ORDER BY ST_Distance(ST_GeomFromText('POINT("+target.getLon()+" "+target.getLat()+")',4326),ST_GeomFromText('POINT(' || lon || ' ' || lat || ')',4326),1) " +
                        "ASC LIMIT " + k;  //THIS QUERY NEEDS ONLY LON AND LAT

                        String loc_spatial = "SELECT ST_X(loc) AS longitude, ST_Y(loc)AS latitude " +
                        "FROM geopoints " +
                        "WHERE ST_Within(loc,ST_Buffer(ST_GeomFromText('POINT("+target.getLon()+" "+target.getLat()+")', 4326),"+distance+")) " +
                        "ORDER BY ST_Distance(ST_GeomFromText('POINT("+target.getLon()+" "+target.getLat()+")', 4326),loc,1) " +
                        "ASC LIMIT " + k; //THIS QUERY NEEDS LOC COLUMN

                 */

                String loc_spatial = "SELECT lon AS longitude, lat AS latitude " +
                        "FROM geopoints " +
                        "WHERE ST_Within(loc,ST_Buffer(MakePoint("+target.getLon()+","+target.getLat()+", 4326),"+distance+")) " +
                        "AND ROWID IN (" +
                        "SELECT ROWID " +
                        "FROM SpatialIndex " +
                        "WHERE f_table_name = 'geopoints' " +
                        "AND f_geometry_column = 'loc' " +
                        "AND search_frame = ST_Buffer(MakePoint("+target.getLon()+","+target.getLat()+", 4326),"+distance+")) " +
                        "ORDER BY ST_Distance(MakePoint("+target.getLon()+","+target.getLat()+", 4326),loc,1) " +
                        "ASC LIMIT " + k;

                System.out.println(loc_spatial);
                stmt = db.prepare(loc_spatial);

                while( stmt.step() ) {
                    kNearestList.add(new GeoPoint(stmt.column_string(0),stmt.column_string(1)));
                    //System.out.println(stmt.column_string(0));
                    //System.out.println(stmt.column_string(0) + ", "+ stmt.column_string(1));
                    //System.out.println(stmt.column_string(0) + ", "+ stmt.column_string(1) + ", "+ Double.parseDouble(stmt.column_string(2))*1000); //*1000 on haversine distance
                }

                /*
                // HAVERSINE METHOD NEEDS THAT
                if (kNearestList.size()>0){
                    //break;
                }
                 */
                if (kNearestList.size()<k){
                    System.out.println("Results are "+kNearestList.size()+" < k: "+k);
                    distanceInKm = distanceInKm * sqrt2;
                    kNearestList.removeAll(kNearestList);
                }/*else { //CIRCLE SECURITY
                    if (kNearestList.get(kNearestList.size() - 1).distanceTo(target) > distanceInKm * 1000) {
                        System.out.println("Got point in the corner! Try again ..");
                        distanceInKm = distanceInKm * sqrt2;
                        kNearestList.removeAll(kNearestList);
                    }
                }
                */
                stmt.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kNearestList;
    }

    private String queryVersions(Database db) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Check versions..\n");

        Stmt stmt01 = db.prepare("SELECT spatialite_version();");
        if (stmt01.step()) {
            sb.append("\t").append("SPATIALITE_VERSION: " + stmt01.column_string(0));
            sb.append("\n");
        }

        stmt01 = db.prepare("SELECT proj4_version();");
        if (stmt01.step()) {
            sb.append("\t").append("PROJ4_VERSION: " + stmt01.column_string(0));
            sb.append("\n");
        }

        stmt01 = db.prepare("SELECT geos_version();");
        if (stmt01.step()) {
            sb.append("\t").append("GEOS_VERSION: " + stmt01.column_string(0));
            sb.append("\n");
        }
        stmt01.close();

        sb.append("Done..\n");
        return sb.toString();
    }

    public int getCount() {
        try {
            System.loadLibrary("sqliteX");
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " AS COUNT", null);
            cursor.moveToFirst();
            int result = cursor.getInt(0);
            System.out.println("TABLE SIZE: "+ result);
            cursor.close();
            db.close();
            return result;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public ArrayList<String> getColumnsNames() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " LIMIT 0", null);
        String[] columnNames = cursor.getColumnNames();
        cursor.close();
        db.close();
        return new ArrayList<String>(Arrays.asList(columnNames));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}

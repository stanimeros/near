package com.example.socialapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import jsqlite.Database;
import jsqlite.Stmt;

public class SQLiteSpatialiteDirect extends SQLiteOpenHelper {
    Context context;

    public SQLiteSpatialiteDirect(Context context) {
        super(context, MainActivity.database_SQLite, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            System.out.println("CREATING DB..");

            String query = "CREATE TABLE " + MainActivity.table + " (" +
                    "id INTEGER PRIMARY KEY)";
            db.execSQL(query);

            query = "CREATE TABLE " + MainActivity.table + "_center_points (" +
                    "id INTEGER PRIMARY KEY," +
                    "lon REAL," +
                    "lat REAL)";
            db.execSQL(query);

            query = "CREATE UNIQUE INDEX unique_location ON "+MainActivity.table+"_center_points (lon,lat)";
            db.execSQL(query);

            System.out.println("TABLE CREATED!");
            System.out.println("SQLITE DB CREATED SUCCESSFULLY!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void Initialize(Context context){
        try {
            File dbFile = context.getDatabasePath(MainActivity.database_SQLite);

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

            query = "SELECT AddGeometryColumn('"+MainActivity.table+"', 'loc', 4326, 'POINT');";
            //query = "SELECT AddGeometryColumn('geopoints', 'loc', 4326, 'POINT','XY');";
            System.out.println(query);
            stmt = db.prepare(query);
            while( stmt.step() ) {
                System.out.println(stmt.column_string(0));
            }

            query = "CREATE UNIQUE INDEX unique_loc ON "+MainActivity.table+"(loc);";
            System.out.println(query);
            stmt = db.prepare(query);
            while( stmt.step() ) {
                System.out.println(stmt.column_string(0));
            }

            query = "SELECT CreateSpatialIndex('"+MainActivity.table+"', 'loc');";
            System.out.println(query);
            stmt = db.prepare(query);
            while( stmt.step() ) {
                System.out.println(stmt.column_string(0));
            }
            stmt.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addGeoPoint(GeoPoint geoPoint, Context context){
        try {
            File dbFile = context.getDatabasePath(MainActivity.database_SQLite);

            Database db = new jsqlite.Database();
            db.open(dbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);

            String query;
            Stmt stmt;

            query = "INSERT OR IGNORE INTO " + MainActivity.table + " (loc) VALUES " +
                    "(MakePoint("+geoPoint.getLon()+", "+geoPoint.getLat()+", 4326));";
            stmt = db.prepare(query);
            while( stmt.step() ) {
                System.out.println(stmt.column_string(0));
            }
            stmt.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void addPreviousPoint(GeoPoint geoPoint, Context context){
        try {
            File dbFile = context.getDatabasePath(MainActivity.database_SQLite);

            Database db = new jsqlite.Database();
            db.open(dbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);

            String query;
            Stmt stmt;

            query = "INSERT OR IGNORE INTO " + MainActivity.table + "_center_points (lon,lat) VALUES " +
                    "("+geoPoint.getLon()+", "+geoPoint.getLat()+");";
            System.out.println(query);
            stmt = db.prepare(query);
            while( stmt.step() ) {
                System.out.println(stmt.column_string(0));
            }
            stmt.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isNearPreviousPoint(GeoPoint geoPoint, Context context){
        try {
            File dbFile = context.getDatabasePath(MainActivity.database_SQLite);

            Database db = new jsqlite.Database();
            db.open(dbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);

            String query;
            Stmt stmt;

            query = "SELECT COUNT(*) FROM " + MainActivity.table + "_center_points " +
                    "WHERE ST_Distance(MakePoint("+geoPoint.getLon()+","+geoPoint.getLat()+", 4326),MakePoint(lon,lat,4326),1)<" + MainActivity.starting_km*1000/2; //500 Meters / 2
            stmt = db.prepare(query);
            System.out.println(query);
            int count = 0;
            while( stmt.step() ) {
                count = Integer.parseInt(stmt.column_string(0));
            }
            stmt.close();
            db.close();

            System.out.println(count);
            if (count> 0){
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<GeoPoint> getKNearestList(int k, GeoPoint target, double distanceInKm, Context context) {
        ArrayList<GeoPoint> kNearestList = new ArrayList<>();
        try {
            File dbFile = context.getDatabasePath(MainActivity.database_SQLite);

            Database db = new jsqlite.Database();
            db.open(dbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);

            final double sqrt2 = Math.sqrt(2);

            while (kNearestList.size()<k){
                Stmt stmt = null;
                System.out.println("Executing query ..");
                float distance = (float) (distanceInKm/100);

                String search_frame = "SELECT ST_X(loc) AS longitude, ST_Y(loc) AS latitude " +
                        "FROM " + MainActivity.table + " "+
                        "WHERE ROWID IN (" +
                        "SELECT ROWID " +
                        "FROM SpatialIndex " +
                        "WHERE f_table_name = '"+MainActivity.table+"' " +
                        "AND f_geometry_column = 'loc' " +
                        "AND search_frame = ST_Buffer(MakePoint("+target.getLon()+","+target.getLat()+", 4326),"+distance+")) " +
                        "ORDER BY ST_Distance(MakePoint("+target.getLon()+","+target.getLat()+", 4326),loc,1) " +
                        "ASC LIMIT " + k;

                //System.out.println(search_frame);
                stmt = db.prepare(search_frame);

                while( stmt.step() ) {
                    kNearestList.add(new GeoPoint(stmt.column_string(0),stmt.column_string(1)));
                }

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
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + MainActivity.table + " AS COUNT", null);
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
        Cursor cursor = db.rawQuery("SELECT * FROM " + MainActivity.table + " LIMIT 0", null);
        String[] columnNames = cursor.getColumnNames();
        cursor.close();
        db.close();
        return new ArrayList<String>(Arrays.asList(columnNames));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MainActivity.table);
        onCreate(db);
    }
}

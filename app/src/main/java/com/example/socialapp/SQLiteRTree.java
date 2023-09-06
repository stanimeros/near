package com.example.socialapp;

import org.sqlite.database.sqlite.SQLiteDatabase;
import org.sqlite.database.sqlite.SQLiteOpenHelper;

import android.content.Context;
import android.database.Cursor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
public class SQLiteRTree extends SQLiteOpenHelper { //THIS METHOD HAS FAILED DUE TO READING REAL/TEXT PROBLEM
    Context context;
    private static final String DB_NAME = "db_geopoints";
    private static final String TABLE_NAME = "geopoints";
    public SQLiteRTree(Context context){
        super(context, DB_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            System.out.println("CREATING DB..");
            System.loadLibrary("sqliteX");
            //db.execSQL("CREATE TABLE geopoints (id INTEGER PRIMARY KEY,lon TEXT,lat TEXT);"); //WORKS
            db.execSQL("CREATE TABLE geopoints (id INTEGER PRIMARY KEY,lon REAL,lat REAL);");
            //db.execSQL("CREATE VIRTUAL TABLE geopoints USING rtree(id INTEGER PRIMARY KEY, lon REAL, lat REAL);"); //TEST

            //TEST
            String query = "INSERT INTO geopoints VALUES(1,10.123456,10.123456)";
            db.execSQL(query);
            query = "INSERT INTO geopoints VALUES(2,10.12345,10.12345)";
            db.execSQL(query);
            query = "INSERT INTO geopoints VALUES(3,10.1234,10.1234)";
            db.execSQL(query);
            //END TEST

            int id = 4;
            System.out.println("INSERTING FROM TEXT FILE..");
            BufferedReader file = new BufferedReader(new InputStreamReader(context.getAssets().open(MainActivity.unsorted_input)));
            String line = file.readLine();
            while (line != null) {
                int br = line.indexOf("-");
                String lon = line.substring(0, br);
                String lat = line.substring(br + 1);

                query = "INSERT INTO geopoints VALUES("+ id + "," + lon + "," + lat + ")";
                db.execSQL(query);
                line = file.readLine();
                id++;
            }
            file.close();
            System.out.println("SQLITE DB CREATED SUCCESSFULLY!");

            String test = "SELECT lon FROM geopoints WHERE lon <= 12"; //PRINTS 1) 10.1235 2)

            System.out.println(test);
            Cursor cursor = db.rawQuery(test, null);
            if (cursor.moveToFirst()) {
                do {
                    System.out.println(cursor.getString(0));
                } while (cursor.moveToNext());
            }
            cursor.close();


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public ArrayList<GeoPoint> getKNearestList(int k, GeoPoint target, double distanceInKm) {
        System.loadLibrary("sqliteX");
        ArrayList<GeoPoint> kNearestList = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            double sqrt2 = Math.sqrt(2);
            while (kNearestList.size()<k){
                float minLon = (float) (target.getLon() - (distanceInKm / (111.11 * Math.cos(Math.toRadians(target.getLat())))));
                float maxLon = (float) (target.getLon() + (distanceInKm / (111.11 * Math.cos(Math.toRadians(target.getLat())))));
                float minLat = (float) (target.getLat() - (distanceInKm/111.11));
                float maxLat = (float) (target.getLat() + (distanceInKm/111.11));

                String test ="SELECT lon, lat " +
                        "FROM geopoints " +
                        "WHERE lon >= "+minLon+" AND lon <= "+maxLon+" " +
                        "AND lat >= "+minLat+" AND lat <= "+maxLat+";";

                System.out.println(test);
                Cursor cursor = db.rawQuery(test, null);
                if (cursor.moveToFirst()) {
                    do {
                        //System.out.println(cursor.getString(0)+ ", " +cursor.getString(1));
                        kNearestList.add(new GeoPoint(
                                (cursor.getString(0)),
                                (cursor.getString(1))
                        ));
                    } while (cursor.moveToNext());
                }
                cursor.close();

                if (kNearestList.size()<k){
                    System.out.println("Results are "+kNearestList.size()+" < k: "+k);
                    distanceInKm = distanceInKm*sqrt2;
                    kNearestList.removeAll(kNearestList);
                }
            }
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        Collections.sort(Objects.requireNonNull(kNearestList), (o1, o2) -> {
            Float f1 = o1.distanceTo(target);
            Float f2 = o2.distanceTo(target);
            return f1.compareTo(f2);
        });
        return new ArrayList<>(kNearestList.subList(0, k));
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int version) {
        System.loadLibrary("sqliteX");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}

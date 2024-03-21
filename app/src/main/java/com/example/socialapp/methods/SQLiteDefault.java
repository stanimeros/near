package com.example.socialapp.methods;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.socialapp.GeoPoint;
import com.example.socialapp.MainActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class SQLiteDefault extends SQLiteOpenHelper {
    Context context;
    String table = "geopoints_" + MainActivity.kmNum + "km";

    public SQLiteDefault(Context context) {
        super(context,"geopoints_" + MainActivity.kmNum + "km", null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            System.out.println("CREATING DB..");

            String query = "CREATE TABLE " + table + " ("
                    + "sin_lat TEXT,"
                    + "sin_lon TEXT,"
                    + "cos_lat TEXT,"
                    + "cos_lon TEXT)";

            db.execSQL(query);
            System.out.println("TABLE CREATED!");

            System.out.println("INSERTING FROM TEXT FILE..");
            BufferedReader file = new BufferedReader(new InputStreamReader(context.getAssets().open(MainActivity.kmNum + "km.txt")));
            String line = file.readLine();
            while (line != null) {
                int br = line.indexOf("-");
                String st_lon = line.substring(0, br);
                String st_lat = line.substring(br + 1);

                query = "INSERT INTO " + table + " (sin_lat,sin_lon,cos_lat,cos_lon) VALUES ("
                        + Math.sin(Math.toRadians(Double.parseDouble(st_lat))) + ","
                        + Math.sin(Math.toRadians(Double.parseDouble(st_lon))) + ","
                        + Math.cos(Math.toRadians(Double.parseDouble(st_lat))) + ","
                        + Math.cos(Math.toRadians(Double.parseDouble(st_lon))) + ")";

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

    public ArrayList<GeoPoint> getKNearestList(int k, GeoPoint target) {
        ArrayList<GeoPoint> kNearestList = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            double lat = Math.toRadians(target.getLat()); //IMPORTANT
            double lon = Math.toRadians(target.getLon()); //IMPORTANT

            String euclidean = "SELECT sin_lon,sin_lat," +
                    "(rad_lat - "+ lat +") * (rad_lat - "+ lat +") + " +
                    "(rad_lon - "+ lon +") * (rad_lon - "+ lon +") AS distance " +
                    "FROM " + table + " ORDER BY distance ASC LIMIT "+k;

            String haversine_with_distance = "SELECT sin_lon,sin_lat, 6371.009 * 6371.009 * 0.5 * " +
                    "(1 - cos_lat * "+Math.cos(lat)+" * (cos_lon*"+Math.cos(lon)+" + sin_lon*"+Math.sin(lon)+") - sin_lat * "+Math.sin(lat)+")" +
                    "AS distance_sq FROM " + table + " ORDER BY distance_sq ASC LIMIT "+k;

            String haversine = "SELECT sin_lon,sin_lat " +
                    "FROM " + table + " " +
                    "ORDER BY 6371.009 * 6371.009 * 0.5 * " +
                    "(1 - cos_lat * "+Math.cos(lat)+" * (cos_lon*"+Math.cos(lon)+" + sin_lon*"+Math.sin(lon)+") - sin_lat * "+Math.sin(lat)+") ASC LIMIT "+k;


            System.out.println(haversine);
            Cursor cursor = db.rawQuery(haversine, null);
            if (cursor.moveToFirst()) {
                //System.out.println("LON AND LAT BEFORE CONVERTING: " + cursor.getString(0) + " , " + cursor.getString(1)); //+ " Distance:"+cursor.getString(2));
                do {
                    kNearestList.add(new GeoPoint(
                            String.valueOf(Math.toDegrees(Math.asin(Float.parseFloat(cursor.getString(0))))),
                            String.valueOf(Math.toDegrees(Math.asin(Float.parseFloat(cursor.getString(1))))))
                    );
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return kNearestList;
    }

    public int getCount() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + table + " AS COUNT", null);
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

    public int getColumnsCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + table + " LIMIT 0", null);
        int columnCount = cursor.getColumnCount();
        cursor.close();
        db.close();
        return columnCount;
    }

    public ArrayList<String> getColumnsNames() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + table + " LIMIT 0", null);
        String[] columnNames = cursor.getColumnNames();
        cursor.close();
        db.close();
        return new ArrayList<String>(Arrays.asList(columnNames));
    }

    public void addNewGeoPoint(GeoPoint geoPoint) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("lat", geoPoint.getLat());
        values.put("lon", geoPoint.getLon());

        db.insert(table, null, values);
        System.out.println("Added successfully!");
        db.close();
    }

    public void dropTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DROP TABLE " + table;

        db.execSQL(query);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
        onCreate(db);
    }
}

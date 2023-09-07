package com.example.socialapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class SQLiteCopyFromAssets extends SQLiteAssetHelper {
    Context context;
    private static final String DB_NAME = MainActivity.SQLite_database;
    private static final String TABLE_NAME = "geopoints";
    public SQLiteCopyFromAssets(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
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
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}

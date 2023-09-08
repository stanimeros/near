package com.example.socialapp;

import android.content.Context;
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

    public void getWritableDb() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}

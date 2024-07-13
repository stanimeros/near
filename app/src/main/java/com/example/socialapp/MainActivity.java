package com.example.socialapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.socialapp.methods.RTreeHelper;
import com.example.socialapp.methods.SQLiteDefault;
import com.example.socialapp.methods.SQLiteRTree;
import com.example.socialapp.methods.SQLiteSpatialite;
import com.example.socialapp.methods.SQLiteSpatialiteDirect;
import com.example.socialapp.methods.kd.KDTreeGroup;
import com.example.socialapp.methods.quad.QuadTreeGroup;
import com.example.socialapp.tools.SQLiteCopyFromAssets;
import com.github.davidmoten.rtree.InternalStructure;
import com.github.davidmoten.rtree.Serializer;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //METHOD CONFIGURATION
    public static int treeMaxPoints = 50000*1000; //CAN BE MODIFIED --BIG(*1000) FOR ONE TREE --SMALL(*1,2,3+) FOR MULTIPLE TREES
    public static int KDTreeLeafMaxPoints = 64; //CAN BE MODIFIED
    public static int QuadTreeLeafMaxPoints = 16; //CAN BE MODIFIED

    public static int k = 25; //CAN BE MODIFIED

    public static int kmNum = 5;

    public static String method = "sqlite_spatialite";  //1:linear 2:sqlite_default, sqlite_rtree, sqlite_spatialite 3:sqlserver 4:kd 5:quad 6:rtree 7:directSpatialite 8:directQuadTree
    public static double starting_km = 0.05; //CAN BE MODIFIED
    public static int time = 0;

    public static int count = 0;

    //LOCAL VARIABLES
    private Thread thread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HttpHelper.context = getBaseContext();
        try {
            if (Objects.equals(method,"sqlite_rtree")){
                thread = new Thread(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        SQLiteRTree sqLiteRTree = new SQLiteRTree(getApplicationContext());
                        sqLiteRTree.getCount();
                        long endTime = System.currentTimeMillis();
                        long millis = endTime - startTime;
                        System.out.println("========= DB TOOK =========");
                        System.out.println(millis);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                thread.start();
            }else if (Objects.equals(method,"sqlite_spatialite")){
                thread = new Thread(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        if (doesDatabaseExistInAssets("geopoints_" + kmNum + "km")){
                           SQLiteCopyFromAssets sqLiteCopyFromAssets = new SQLiteCopyFromAssets(getApplicationContext());
                            sqLiteCopyFromAssets.getWritableDb();
                        }
                        SQLiteSpatialite sqLiteSpatialite = new SQLiteSpatialite(getApplicationContext());
                        sqLiteSpatialite.getCount();

                        ArrayList<String> names = sqLiteSpatialite.getColumnsNames();
                        if (!names.contains("loc")) {
                            sqLiteSpatialite.Initialize(getApplicationContext());
                            sqLiteSpatialite.createSpatialColumn(getApplicationContext());
                        }
                        long endTime = System.currentTimeMillis();
                        long millis = endTime - startTime;
                        System.out.println("========= DB TOOK =========");
                        System.out.println(millis);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                thread.start();
            }else if (Objects.equals(method,"sqlite_default")){
                thread = new Thread(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        SQLiteDefault sqLiteDefault = new SQLiteDefault(getApplicationContext());
                        sqLiteDefault.getCount();

                        long endTime = System.currentTimeMillis();
                        long millis = endTime - startTime;
                        System.out.println("========= DB TOOK =========");
                        System.out.println(millis);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                thread.start();
            }else if (Objects.equals(method,"directSpatialite")){
                thread = new Thread(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        SQLiteSpatialiteDirect sqLiteSpatialiteDirect = new SQLiteSpatialiteDirect(getApplicationContext());
                        sqLiteSpatialiteDirect.getCount();

                        ArrayList<String> names = sqLiteSpatialiteDirect.getColumnsNames();
                        if (!names.contains("loc")) {
                            sqLiteSpatialiteDirect.Initialize(getApplicationContext());
                        }

                        long endTime = System.currentTimeMillis();
                        long millis = endTime - startTime;
                        System.out.println("========= DB TOOK =========");
                        System.out.println(millis);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                thread.start();
            }else if (Objects.equals(method, "kd")){
                thread = new Thread(() -> {
                    KDTreeGroup.Initialize(MainActivity.treeMaxPoints,MainActivity.KDTreeLeafMaxPoints,MainActivity.kmNum + "km_sorted.txt",this);
                });
                thread.start();
            }else if (Objects.equals(method, "quad")){
                thread = new Thread(() -> {
                    QuadTreeGroup.Initialize(MainActivity.treeMaxPoints,MainActivity.QuadTreeLeafMaxPoints,MainActivity.kmNum + "km_sorted.txt",this);
                });
                thread.start();
            }else if (Objects.equals(method, "rtree")){
                thread = new Thread(() -> {
                    try {
                        File file = new File(this.getFilesDir(), "rtree.bin");
                        if (file.exists()) {
                            System.out.println("File exists.");

                            InputStream is = new FileInputStream(file);
                            int lengthBytes = (int) file.length();

                            Serializer<String, Point> serializer = Serializers.flatBuffers().utf8();
                            RTreeHelper.rtree = serializer.read(is, lengthBytes, InternalStructure.SINGLE_ARRAY);
                        } else {
                            System.out.println("File does not exist.");
                            RTreeHelper.createRTree(this);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                });
                thread.start();
            }

            Button permissionsButton = findViewById(R.id.button_permissions);
            permissionsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkPermissions();
                }
            });

            checkPermissions();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void goToNextActivity(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.contains("phone")){
            String phone = prefs.getString("phone","Error");
            String username = prefs.getString("username","Error");
            String joinDate = prefs.getString("joinDate","Error");
            int image = prefs.getInt("image",0);

            User user = new User(phone, username, image);
            user.setJoinDate(joinDate);
            goToFeed(user, thread);
        }else{
            goToSignUp();
        }
    }

    private void goToSignUp()
    {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    private void goToFeed(User user, Thread thread)
    {
        Intent intent = new Intent(this, Feed.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        if (thread!=null) { bundle.putLong("threadId",thread.getId()); }
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    @Override
    public void onBackPressed() {}
    public boolean doesDatabaseExistInAssets(String fileName) {
        AssetManager assetManager = getApplicationContext().getAssets();
        try {
            // Attempt to open the file
            InputStream inputStream = assetManager.open("databases/"+fileName);
            // If successful, the file exists
            inputStream.close();
            System.out.println("Database found on Assets!");
            return true;
        } catch (Exception e) {
            // The file doesn't exist or there was an error
            return false;
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }else{
                goToNextActivity();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Permission granted!");
            goToNextActivity();
        } else {
            System.out.println("Try again!");
        }
    }
}
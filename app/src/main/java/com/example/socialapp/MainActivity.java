package com.example.socialapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
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

    //MariaDB AND POSTGRESQL CONFIGURATION
    private static final String MariaDBClassName = "org.mariadb.jdbc.Driver";
    private static final String PostgreSQLClassName = "org.postgresql.Driver";
    private static final String MariaDBJDBCName = "mariadb";
    private static final String PostgreSQLJDBCName = "postgresql";
    private static final String MariaDBPort = "3306";
    private static final String PostgreSQLPort = "5432";
    private static final String SQLDatabase= "social";
    private static final String MariaDBUser = "admin";
    private static final String PostgreSQLUser = "postgres";
    private static final String PostgreSQLIP = "10.0.2.2";
    private static final String PostgreSQLPassword = "admin";
    //MY SERVER
    private static final String MyServerIP = "89.117.169.103";
    private static final String MyServerDatabase = "u223212146_social";
    private static final String MyServerUser = "u223212146_stanimeros2"; //ONLY SELECT INSERT UPDATE DELETE
    private static final String MyServerPassword= "Stan1meross2";
    //OKEANOS
    private static final String okeanos = "snf-38501.ok-kno.grnetcloud.net";
    private static final String okeanosUser = "near_users"; //ONLY SELECT INSERT UPDATE DELETE
    private static final String okeanosPassword = "stanimeros";

    //APPLICATION CONNECTION
    public static String className = MariaDBClassName;
    public static String jdbcName = MariaDBJDBCName;
    public static String serverIp = okeanos;
    public static String port = MariaDBPort;
    public static String database = SQLDatabase;
    public static String user = okeanosUser;
    public static String password = okeanosPassword;

    //METHOD CONFIGURATION
    public static int treeMaxPoints = 50000*1000; //CAN BE MODIFIED --BIG(*1000) FOR ONE TREE --SMALL(*1,2,3+) FOR MULTIPLE TREES
    public static int KDTreeLeafMaxPoints = 64; //CAN BE MODIFIED
    public static int QuadTreeLeafMaxPoints = 16; //CAN BE MODIFIED

    public static int k = 5; //CAN BE MODIFIED
    public static String kmFile ="5km"; //1km 5km 25km 100km
    public static String method = "sqlite_spatialite";  //1:linear 2:sqlite_default, sqlite_rtree, sqlite_spatialite 3:sqlserver 4:kd 5:quad 6:rtree 7:directSpatialite 8:directQuadTree
    public static double starting_km = 0.05; //CAN BE MODIFIED

    //FILE CONFIGURATION -- CHANGE ONLY kmFile
    public static String sorted_input = kmFile + "_sorted.txt"; //CHANGE ONLY kmFile
    public static String unsorted_input = kmFile + ".txt"; //CHANGE ONLY kmFile

    public static String table = "geopoints_" + kmFile; //CHANGE ONLY kmFile
    public static String database_SQLite = "geopoints_" + kmFile; //CHANGE ONLY kmFile

    //CLASS VARIABLES
    private String phone;
    private String username;
    private String joinDate;
    private Integer image;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        method = prefs.getString("method",method);
        k = prefs.getInt("k-anonymity",k);
        starting_km = Double.parseDouble(prefs.getString("starting_km", String.valueOf(starting_km)));
        kmFile = prefs.getString("kmFile",kmFile);
        treeMaxPoints = prefs.getInt("treeMaxPoints",treeMaxPoints);
        KDTreeLeafMaxPoints = prefs.getInt("KDTreeLeafMaxPoints",KDTreeLeafMaxPoints);
        QuadTreeLeafMaxPoints = prefs.getInt("QuadTreeLeafMaxPoints",QuadTreeLeafMaxPoints);
        thread = new Thread(() -> {
            try {
                //ServerSQL.getSettings(getApplicationContext()); //GET SETTINGS FROM SERVER # COMMENT THIS IF YOU WANT TO SET THEM MANUALLY
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        thread.start();
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
                        if (doesDatabaseExistInAssets(database_SQLite)){
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
                        SQLiteSpatialiteDirectFromOSM sqLiteSpatialiteDirectFromOSM = new SQLiteSpatialiteDirectFromOSM(getApplicationContext());
                        sqLiteSpatialiteDirectFromOSM.getCount();

                        ArrayList<String> names = sqLiteSpatialiteDirectFromOSM.getColumnsNames();
                        if (!names.contains("loc")) {
                            sqLiteSpatialiteDirectFromOSM.Initialize(getApplicationContext());
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
                    KDTreeGroup.Initialize(MainActivity.treeMaxPoints,MainActivity.KDTreeLeafMaxPoints,MainActivity.sorted_input,this);
                });
                thread.start();
            }else if (Objects.equals(method, "quad")){
                thread = new Thread(() -> {
                    QuadTreeGroup.Initialize(MainActivity.treeMaxPoints,MainActivity.QuadTreeLeafMaxPoints,MainActivity.sorted_input,this);
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

            if (prefs.contains("phone")){
                phone = prefs.getString("phone","");
                username = prefs.getString("username","");
                joinDate = prefs.getString("joinDate","");
                image = prefs.getInt("image",0);
                goToFeed();
            }else{
                goToSignUp();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void goToSignUp()
    {
        Intent intent = new Intent(this, SignUp.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        finish();
    }

    private void goToFeed()
    {
        Intent intent = new Intent(this, Feed.class);
        Bundle bundle = new Bundle();
        bundle.putString("phone",phone);
        bundle.putString("username",username);
        bundle.putString("joinDate",joinDate);
        bundle.putInt("image",image);
        if (thread!=null){
            bundle.putLong("threadId",thread.getId());
        }
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
}
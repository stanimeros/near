package com.example.socialapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
    private static final String MyServerUser = "u223212146_stanimeros2";
    private static final String MyServerPassword= "Stan1meross2";

    //APPLICATION CONNECTION
    public static String className = MariaDBClassName;
    public static String jdbcName = MariaDBJDBCName;
    public static String serverIp = MyServerIP;
    public static String port = MariaDBPort;
    public static String database = MyServerDatabase;
    public static String user = MyServerUser;
    public static String password = MyServerPassword;

    //METHOD CONFIGURATION
    public static int treeMaxPoints = 100000*100;
    public static int KDTreeLeafMaxPoints = 50;
    public static int QuadTreeLeafMaxPoints = 16;

    public static int k = 100;
    public static String kmFile ="25km";
    public static String sorted_input = kmFile + "_sorted.txt";
    public static String unsorted_input = kmFile + ".txt";
    public static String choice = "rtree";  //1:linear 2:sqlite_default, sqlite_rtree, sqlite_spatialite 3:sqlserver 4:kd 5:quad 6:rtree
    public static double starting_km = 0.05;

    public static String table = "geopoints_" + kmFile;

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
        try {
            if (Objects.equals(choice,"sqlite_rtree")){
                thread = new Thread(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        SQLiteRTree sqLiteRTree = new SQLiteRTree(getBaseContext());
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
            }else if (Objects.equals(choice,"sqlite_spatialite")){
                thread = new Thread(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        SQLiteSpatialite sqLiteSpatialite = new SQLiteSpatialite(getBaseContext());
                        sqLiteSpatialite.getCount();
                        ArrayList<String> names = sqLiteSpatialite.getColumnsNames();
                        if (!names.contains("loc")) {
                            sqLiteSpatialite.Initialize(getBaseContext());
                            sqLiteSpatialite.createSpatialColumn(getBaseContext());
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

            }else if (Objects.equals(choice,"sqlite_default")){
                thread = new Thread(() -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        SQLiteDefault sqLiteDefault = new SQLiteDefault(getBaseContext());
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
            }else if (Objects.equals(choice, "kd")){
                thread = new Thread(() -> {
                    new KDTreeGroup(MainActivity.treeMaxPoints,MainActivity.KDTreeLeafMaxPoints,MainActivity.sorted_input,this);
                });
                thread.start();
            }else if (Objects.equals(choice, "quad")){
                thread = new Thread(() -> {
                    new QuadTreeGroup(MainActivity.treeMaxPoints,MainActivity.QuadTreeLeafMaxPoints,MainActivity.sorted_input,this);
                });
                thread.start();
            }else if (Objects.equals(choice, "rtree")){
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

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            phone = prefs.getString("phone","");
            if (phone.isEmpty()){
                goToSignUp();
            }else{
                username = prefs.getString("username","");
                joinDate = prefs.getString("joinDate","");
                image = prefs.getInt("image",0);
                goToFeed();
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
}
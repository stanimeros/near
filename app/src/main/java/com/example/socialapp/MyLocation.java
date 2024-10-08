package com.example.socialapp;
import android.content.Context;

import com.example.socialapp.methods.LinearSearch;
import com.example.socialapp.methods.RTreeHelper;
import com.example.socialapp.methods.SQLiteDefault;
import com.example.socialapp.methods.SQLiteRTree;
import com.example.socialapp.methods.SQLiteSpatialite;
import com.example.socialapp.methods.SQLiteSpatialiteDirect;
import com.example.socialapp.methods.kd.KDTreeGroup;
import com.example.socialapp.methods.quad.QuadTree;
import com.example.socialapp.methods.quad.QuadTreeGroup;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class MyLocation {
    private GeoPoint myPointOfInterest;
    GeoPoint pamak = new GeoPoint((float)22.96017,(float)40.625041);

    long tempMillis;
    ArrayList<GeoPoint> tempList;

    public void setMyPointOfInterestSearch(GeoPoint target, String phone,Context context){
        String method = MainActivity.method;
        int k = MainActivity.k;

        if (target.distanceTo(pamak)>MainActivity.kmNum*1000 + 1000 && !method.contains("direct")){
            System.out.println("Phone is too far from pamak!"); //Prevent calculations far away from dataset's center!
            MainActivity.method = "directQuadTree";
            directDownloadQuadTree(target,phone,k);

            HttpHelper.setLocation(myPointOfInterest,phone);
            return;
        }

        if (Objects.equals(method, "linear")){
            setMyPointOfInterestLinearSearch(target,phone,k,context);
        }else if(Objects.equals(method, "sqlite_default")) {
            setMyPointOfInterestSQLiteDefaultSearch(target, phone, k, context);
        }else if(Objects.equals(method, "sqlite_rtree")) {
            setMyPointOfInterestSQLiteRTreeSearch(target, phone, k, context);
        }else if(Objects.equals(method, "sqlite_spatialite")){
            setMyPointOfInterestSQLiteSpatialiteSearch(target,phone,k,context);
        }else if(Objects.equals(method, "sqlserver")){
            setMyPointOfInterestSQLServerSearch(target,phone,k);
        }else if (Objects.equals(method, "kd")){
            setMyPointOfInterestKDTreeSearch(target,phone,k,context);
        }else if (Objects.equals(method, "quad")){
            setMyPointOfInterestQuadTreeSearch(target,phone,k,context);
        }else if (Objects.equals(method, "rtree")){
            setMyPointOfInterestRTreeSearch(target,phone,k);
        }else if (Objects.equals(method, "directQuadTree")){
            directDownloadQuadTree(target,phone,k);
        }else if (Objects.equals(method, "directSpatialite")){
            directDownloadSpatialite(target,phone,k,context);
        }

//        ServerSQL.uploadResults(tempMillis,phone);
        MainActivity.time+=tempMillis;
        MainActivity.count++;

        //int query_id = Integer.parseInt(ServerSQL.getAutoIncrement());
        //ServerSQL.uploadExperiment(target,tempList,tempMillis,query_id);

        //ServerSQL.setLocation(myPointOfInterest,phone);
        HttpHelper.setLocation(myPointOfInterest,phone); //set real location too


        System.out.println("=== AVG TIME ("+MainActivity.count+") === \n"+ MainActivity.time/MainActivity.count +"\n=== === ===");
    }
    private void printMyList(ArrayList<GeoPoint> list,GeoPoint target){
        for (int i=0;i<list.size();i++){
            //System.out.println((i+1)+") Lon: "+list.get(i).getLon() +" Lat: "+list.get(i).getLat()+ " distance "+target.distanceTo(list.get(i)));
        }
        System.out.println("Last point of list: "+ (list.size())+") Lon: "+list.get(list.size()-1).getLon() +" Lat: "+list.get(list.size()-1).getLat()+ " distance "+target.distanceTo(list.get(list.size()-1)));
    }
    public void setMyPointOfInterestLinearSearch(GeoPoint target, String phone, int k, Context context) {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Generating new location using Linear Search!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = Objects.requireNonNull(LinearSearch.getKNearestList(target, 1, context)).get(0);
            System.out.println("Nearest point was " + nearestPoint.distanceTo(target) + "m away!");

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = LinearSearch.getKNearestList(nearestPoint,k,context);
            assert kNearestList != null;
            printMyList(kNearestList,nearestPoint);

            System.out.println("Selecting one of K points randomly..");
            Random rand = new Random();
            int c = rand.nextInt(kNearestList.size());

            System.out.println("Point updated successfully!");

            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= IT TOOK =========");
            System.out.println(millis);

            myPointOfInterest = new GeoPoint(kNearestList.get(c).getLon(),kNearestList.get(c).getLat());

            tempMillis = millis;
            tempList = kNearestList;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterestSQLiteDefaultSearch(GeoPoint target, String phone, int k,Context context) {
        try {
            long startTime = System.currentTimeMillis();
            SQLiteDefault sqLiteDefault = new SQLiteDefault(context);
            System.out.println("Generating new location using SQLite!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = sqLiteDefault.getKNearestList(1,target).get(0);
            System.out.println("Nearest point was " + nearestPoint.distanceTo(target) + "m away!");

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = sqLiteDefault.getKNearestList(k,nearestPoint);
            printMyList(kNearestList,nearestPoint);

            System.out.println("Selecting one of K points randomly..");
            Random rand = new Random();
            int c = rand.nextInt(kNearestList.size());

            System.out.println("Point updated successfully!");

            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= IT TOOK =========");
            System.out.println(millis);

            myPointOfInterest = new GeoPoint(kNearestList.get(c).getLon(),kNearestList.get(c).getLat());

            tempMillis = millis;
            tempList = kNearestList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterestSQLiteRTreeSearch(GeoPoint target, String phone, int k,Context context) { //FAILED
        try {
            long startTime = System.currentTimeMillis();
            SQLiteRTree sqLiteRTree = new SQLiteRTree(context);
            System.out.println("Generating new location using SQLite RTree!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = sqLiteRTree.getKNearestList(1,target,MainActivity.starting_km).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println("Nearest point was " + nearestPoint.distanceTo(target) + "m away!");
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = sqLiteRTree.getKNearestList(k,nearestPoint,distance/1000);
            printMyList(kNearestList,nearestPoint);

            System.out.println("Selecting one of K points randomly..");
            Random rand = new Random();
            int c = rand.nextInt(kNearestList.size());

            System.out.println("Point updated successfully!");

            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= IT TOOK =========");
            System.out.println(millis);

            myPointOfInterest = new GeoPoint(kNearestList.get(c).getLon(),kNearestList.get(c).getLat());

            tempMillis = millis;
            tempList = kNearestList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterestSQLiteSpatialiteSearch(GeoPoint target, String phone, int k,Context context) {
        try {
            long startTime = System.currentTimeMillis();
            SQLiteSpatialite sqLiteSpatialite = new SQLiteSpatialite(context);
            System.out.println("Generating new location using Spatialite!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = sqLiteSpatialite.getKNearestList(1,target,MainActivity.starting_km,context).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println("Nearest point was " + nearestPoint.distanceTo(target) + "m away!");
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = sqLiteSpatialite.getKNearestList(k,nearestPoint,distance/1000,context);
            printMyList(kNearestList,nearestPoint);

            System.out.println("Selecting one of K points randomly..");
            Random rand = new Random();
            int c = rand.nextInt(kNearestList.size());

            System.out.println("Point updated successfully!");

            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= IT TOOK =========");
            System.out.println(millis);

            myPointOfInterest = new GeoPoint(kNearestList.get(c).getLon(),kNearestList.get(c).getLat());

            tempMillis = millis;
            tempList = kNearestList;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterestSQLServerSearch(GeoPoint target, String phone, int k) {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Generating new location using SQLServer!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = HttpHelper.getKNearestList(target, 1, MainActivity.starting_km).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println("Nearest point was " + nearestPoint.distanceTo(target) + "m away!");
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = HttpHelper.getKNearestList(nearestPoint,k,distance/1000);
            printMyList(kNearestList,nearestPoint);

            System.out.println("Selecting one of K points randomly..");
            Random rand = new Random();
            int c = rand.nextInt(kNearestList.size());

            System.out.println("Point updated successfully!");

            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= IT TOOK =========");
            System.out.println(millis);

            myPointOfInterest = new GeoPoint(kNearestList.get(c).getLon(),kNearestList.get(c).getLat());

            tempMillis = millis;
            tempList = kNearestList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterestKDTreeSearch(GeoPoint target, String phone, int k, Context context){
        try {
            long startTime = System.currentTimeMillis();
            //KDTreeGroup group = new KDTreeGroup(MainActivity.treeMaxPoints,MainActivity.KDTreeLeafMaxPoints,MainActivity.sorted_input,context);
            System.out.println("Generating new location using KD-Tree Search!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = KDTreeGroup.getKNearestList(target,1,MainActivity.starting_km).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println("Nearest point was " + nearestPoint.distanceTo(target) + "m away!");
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = KDTreeGroup.getKNearestList(nearestPoint,k,distance/1000);
            printMyList(kNearestList,nearestPoint);

            System.out.println("Selecting one of K points randomly..");
            Random rand = new Random();
            int c = rand.nextInt(kNearestList.size());

            System.out.println("Point updated successfully!");

            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= IT TOOK =========");
            System.out.println(millis);

            myPointOfInterest = new GeoPoint(kNearestList.get(c).getLon(),kNearestList.get(c).getLat());

            tempMillis = millis;
            tempList = kNearestList;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterestQuadTreeSearch(GeoPoint target, String phone, int k, Context context){
        try {
            long startTime = System.currentTimeMillis();
            //QuadTreeGroup group = new QuadTreeGroup(MainActivity.treeMaxPoints,MainActivity.QuadTreeLeafMaxPoints,MainActivity.sorted_input,context);
            System.out.println("Generating new location using Quad-Tree Search!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = QuadTreeGroup.getKNearestList(target,1,MainActivity.starting_km).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println("Nearest point was " + nearestPoint.distanceTo(target) + "m away!");
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = QuadTreeGroup.getKNearestList(nearestPoint,k,distance/1000);
            printMyList(kNearestList,nearestPoint);

            System.out.println("Selecting one of K points randomly..");
            Random rand = new Random();
            int c = rand.nextInt(kNearestList.size());

            System.out.println("Point updated successfully!");

            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= IT TOOK =========");
            System.out.println(millis);

            myPointOfInterest = new GeoPoint(kNearestList.get(c).getLon(),kNearestList.get(c).getLat());

            tempMillis = millis;
            tempList = kNearestList;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterestRTreeSearch(GeoPoint target, String phone, int k){
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Generating new location using RTree Search!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = RTreeHelper.getPointsFromRange(target, 1, MainActivity.starting_km).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println("Nearest point was " + nearestPoint.distanceTo(target) + "m away!");
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = RTreeHelper.getPointsFromRange(nearestPoint,k,distance/1000);
            printMyList(kNearestList,nearestPoint);

            System.out.println("Selecting one of K points randomly..");
            Random rand = new Random();
            int c = rand.nextInt(kNearestList.size());

            System.out.println("Point updated successfully!");

            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= IT TOOK =========");
            System.out.println(millis);

            myPointOfInterest = new GeoPoint(kNearestList.get(c).getLon(),kNearestList.get(c).getLat());

            tempMillis = millis;
            tempList = kNearestList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void directDownloadQuadTree(GeoPoint target, String phone, int k){
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Generating new location using OSM Direct Download and Quad Tree!");
            ArrayList<GeoPoint> points = new ArrayList<>();
            double sqrt2 = Math.sqrt(2);
            double tempDistance = MainActivity.starting_km;
            while (points.size()<k){
                points = HttpHelper.getPointsFromOSM(target,tempDistance); //ERROR IF NOT FOUND NOTHING\
                tempDistance *= sqrt2;
            }
            QuadTree tree = new QuadTree(points,MainActivity.KDTreeLeafMaxPoints);

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = tree.getKNearestList(target,1, MainActivity.starting_km).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println("Nearest point was " + nearestPoint.distanceTo(target) + "m away!");
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = tree.getKNearestList(nearestPoint,k,distance/1000);
            printMyList(kNearestList,nearestPoint);

            System.out.println("Selecting one of K points randomly..");
            Random rand = new Random();
            int c = rand.nextInt(kNearestList.size());

            System.out.println("Point updated successfully!");

            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= IT TOOK =========");
            System.out.println(millis);

            myPointOfInterest = new GeoPoint(kNearestList.get(c).getLon(),kNearestList.get(c).getLat());

            tempMillis = millis;
            tempList = kNearestList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void directDownloadSpatialite(GeoPoint target, String phone, int k, Context context){
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Generating new location using Test Method Search!");
            SQLiteSpatialiteDirect sqLiteSpatialiteDirect = new SQLiteSpatialiteDirect(context);

            if (!sqLiteSpatialiteDirect.isNearPreviousPoint(target,context)){
                ArrayList<GeoPoint> points = HttpHelper.getPointsFromOSM(target,MainActivity.starting_km); //ERROR IF NOT FOUND NOTHING //500 Meters
                for (int i=0;i<points.size();i++){
                    sqLiteSpatialiteDirect.addGeoPoint(points.get(i),context);
                }
            }

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = sqLiteSpatialiteDirect.getKNearestList(1,target, MainActivity.starting_km,context).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println("Nearest point was " + nearestPoint.distanceTo(target) + "m away!");
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = sqLiteSpatialiteDirect.getKNearestList(k,nearestPoint,distance/1000,context);
            printMyList(kNearestList,nearestPoint);

            System.out.println("Selecting one of K points randomly..");
            Random rand = new Random();
            int c = rand.nextInt(kNearestList.size());

            System.out.println("Point updated successfully!");

            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= IT TOOK =========");
            System.out.println(millis);

            myPointOfInterest = new GeoPoint(kNearestList.get(c).getLon(),kNearestList.get(c).getLat());

            sqLiteSpatialiteDirect.addPreviousPoint(myPointOfInterest,context);
            tempMillis = millis;
            tempList = kNearestList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterest(GeoPoint myPointOfInterest) {
        this.myPointOfInterest = myPointOfInterest;
    }
}

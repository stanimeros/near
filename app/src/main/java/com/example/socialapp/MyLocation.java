package com.example.socialapp;

import android.content.Context;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class MyLocation {
    private GeoPoint myPointOfInterest;

    public void setMyPointOfInterestSearch(GeoPoint target, String phone,Context context){
        String choice = MainActivity.choice;
        int k = MainActivity.k;
        if (Objects.equals(choice, "linear")){
            setMyPointOfInterestLinearSearch(target,phone,k,context);
        }else if(Objects.equals(choice, "sqlite_default")) {
            setMyPointOfInterestSQLiteDefaultSearch(target, phone, k, context);
        }else if(Objects.equals(choice, "sqlite_rtree")) {
            setMyPointOfInterestSQLiteRTreeSearch(target, phone, k, context);
        }else if(Objects.equals(choice, "sqlite_spatialite")){
            setMyPointOfInterestSQLiteSpatialiteSearch(target,phone,k,context);
        }else if(Objects.equals(choice, "sqlserver")){
            setMyPointOfInterestSQLServerSearch(target,phone,k);
        }else if (Objects.equals(choice, "kd")){
            setMyPointOfInterestKDTreeSearch(target,phone,k,context);
        }else if (Objects.equals(choice, "quad")){
            setMyPointOfInterestQuadTreeSearch(target,phone,k,context);
        }else if (Objects.equals(choice, "rtree")){
            setMyPointOfInterestRTreeSearch(target,phone,k);
        }
    }
    private void printMyList(ArrayList<GeoPoint> list,GeoPoint target){
        for (int i=0;i<list.size();i++){
            System.out.println((i+1)+") Lon: "+list.get(i).getLon() +" Lat: "+list.get(i).getLat()+ " distance "+target.distanceTo(list.get(i)));
        }
        //System.out.println("Last point of list: "+ (list.size())+") Lon: "+list.get(list.size()-1).getLon() +" Lat: "+list.get(list.size()-1).getLat()+ " distance "+target.distanceTo(list.get(list.size()-1)));
    }
    private void setMyPointOfInterestLinearSearch(GeoPoint target, String phone, int k, Context context) {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Generating new location using Linear Search!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = Objects.requireNonNull(LinearSearch.kNearestLinearSearch(target, 1, context)).get(0);
            System.out.println(nearestPoint.distanceTo(target));

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = LinearSearch.kNearestLinearSearch(nearestPoint,k,context);
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
            ServerSQL.setLocation(myPointOfInterest,phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMyPointOfInterestSQLiteDefaultSearch(GeoPoint target, String phone, int k,Context context) {
        try {
            long startTime = System.currentTimeMillis();
            SQLiteDefault sqLiteDefault = new SQLiteDefault(context);
            System.out.println("Generating new location using SQLite!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = sqLiteDefault.getKNearestList(1,target).get(0);
            System.out.println(nearestPoint.distanceTo(target));

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
            ServerSQL.setLocation(myPointOfInterest,phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMyPointOfInterestSQLiteRTreeSearch(GeoPoint target, String phone, int k,Context context) { //FAILED
        try {
            long startTime = System.currentTimeMillis();
            SQLiteRTree sqLiteRTree = new SQLiteRTree(context);
            System.out.println("Generating new location using SQLite RTree!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = sqLiteRTree.getKNearestList(1,target,MainActivity.starting_km).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println(nearestPoint.distanceTo(target));
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
            ServerSQL.setLocation(myPointOfInterest,phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMyPointOfInterestSQLiteSpatialiteSearch(GeoPoint target, String phone, int k,Context context) {
        try {
            long startTime = System.currentTimeMillis();
            SQLiteSpatialite sqLiteSpatialite = new SQLiteSpatialite(context);
            System.out.println("Generating new location using Spatialite!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = sqLiteSpatialite.getKNearestList(1,target,MainActivity.starting_km,context).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println(nearestPoint.distanceTo(target));
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
            ServerSQL.setLocation(myPointOfInterest,phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMyPointOfInterestSQLServerSearch(GeoPoint target, String phone, int k) {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Generating new location using SQLServer!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = ServerSQL.getPointsFromRange(target, 1, MainActivity.starting_km).get(0);
            System.out.println(nearestPoint.distanceTo(target));
            double distance = nearestPoint.distanceTo(target);
            System.out.println(nearestPoint.distanceTo(target));
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = ServerSQL.getPointsFromRange(nearestPoint,k,distance/1000);
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
            ServerSQL.setLocation(myPointOfInterest,phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterestKDTreeSearch(GeoPoint target, String phone, int k, Context context){
        try {
            long startTime = System.currentTimeMillis();
            KDTreeGroup group = new KDTreeGroup(MainActivity.treeMaxPoints,MainActivity.KDTreeLeafMaxPoints,MainActivity.sorted_input,context);
            System.out.println("Generating new location using KD-Tree Search!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = group.findKNearestNeighbors(target,1,MainActivity.starting_km).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println(nearestPoint.distanceTo(target));
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = group.findKNearestNeighbors(nearestPoint,k,distance/1000);
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
            ServerSQL.setLocation(myPointOfInterest,phone);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterestQuadTreeSearch(GeoPoint target, String phone, int k, Context context){
        try {
            long startTime = System.currentTimeMillis();
            QuadTreeGroup group = new QuadTreeGroup(MainActivity.treeMaxPoints,MainActivity.QuadTreeLeafMaxPoints,MainActivity.sorted_input,context);
            System.out.println("Generating new location using Quad-Tree Search!");

            System.out.println("Finding nearest point of interest..");
            GeoPoint nearestPoint = group.findKNearestNeighbors(target,1,MainActivity.starting_km).get(0);
            double distance = nearestPoint.distanceTo(target);
            System.out.println(nearestPoint.distanceTo(target));
            distance+=10; //ADDING 10 METERS TO AVOID ZERO

            System.out.println("Finding K nearest to the nearest point of interest..");
            ArrayList<GeoPoint> kNearestList = group.findKNearestNeighbors(nearestPoint,k,distance/1000);
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
            ServerSQL.setLocation(myPointOfInterest,phone);
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
            System.out.println(nearestPoint.distanceTo(target));
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
            ServerSQL.setLocation(myPointOfInterest,phone);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMyPointOfInterest(GeoPoint myPointOfInterest) {
        this.myPointOfInterest = myPointOfInterest;
    }
}

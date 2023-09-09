package com.example.socialapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class KDTreeGroup {
    private static String input;
    private static Context context;
    private static int treeMaxPoints;
    private static int leafMaxPoints;
    private static ArrayList<Double> treeEdges = new ArrayList<>();
    private static KDTree mainTree=null;
    private static KDTree kdTrees[] =  new KDTree[1]; //UNFINISHED
    private static int keyTree= -1;

    public static void Initialize(int treeMaxPoints, int leafMaxPoints, String input, Context context) {
        try {
            KDTreeGroup.context = context;
            KDTreeGroup.input = input;
            KDTreeGroup.treeMaxPoints = treeMaxPoints;
            KDTreeGroup.leafMaxPoints = leafMaxPoints;
            Gson gson = new Gson();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String json = prefs.getString("KDTrees", "");
            String[] tempStringArray = gson.fromJson(json,String[].class);
            if (tempStringArray==null){
                System.out.println("Generating map..");
                treeEdges = generateMap(countFileLines());
                if (treeEdges.size()==1){
                    mainTree = generateTree(1);
                    keyTree = 1;
                }
                saveTreeGroup(treeEdges);
                System.out.println("Map with "+treeEdges.size()+" trees saved successfully!");
            }else{
                ArrayList<String> tempArrayList= new ArrayList<>(Arrays.asList(tempStringArray));
                for (int i=0;i<tempArrayList.size();i++){
                    treeEdges.add(Double.parseDouble(tempArrayList.get(i)));
                }
                System.out.println("Map with "+treeEdges.size()+" trees found locally!");

                if (treeEdges.size()==1){
                    mainTree = getTree(1);
                    keyTree = 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<GeoPoint> findKNearestNeighbors(GeoPoint target, int k, double distanceInKm){
        try {
            if (mainTree==null || keyTree!=getKeyTree(target)){
                mainTree = Objects.requireNonNull(getMainTree(target));
                keyTree = getKeyTree(target);
            }
            ArrayList<GeoPoint> neighbors = mainTree.findKNearestNeighbors(target,k,distanceInKm);

            double radius = neighbors.get(neighbors.size()-1).distanceTo(target);
            System.out.println("Searched in main-tree and found "+neighbors.size()+" point(s) in range "+(int) (radius)+"m");

            ArrayList<KDTree> treesInRange= Objects.requireNonNull(getTreesInRange(target,radius));
            if (treesInRange.size()>1){
                System.out.println("Range includes "+treesInRange.size()+" trees!");
                neighbors.removeAll(neighbors);

                //Starting a thread for every tree!
                Thread[] threads = new Thread[treesInRange.size()];
                for (int i=0;i<treesInRange.size();i++){
                    double finalRadius = radius;
                    int finalI = i;
                    threads[i] = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Thread.currentThread().setName("Thread-TreeSearch"+finalI);
                            ArrayList<GeoPoint> tempList = treesInRange.get(finalI).findNearestNeighborsInRange(target,(finalRadius /1000));
                            System.out.println(Thread.currentThread().getName()+ " found " + tempList.size()+ " points!");
                            neighbors.addAll(tempList);
                        }
                    });
                    threads[i].start();
                }
                for (Thread thread : threads) {
                    thread.join();
                    System.out.println(thread.getName() + " finished!");
                }
                //All threads finished!

                //Without Threads
                //for (int i=0;i<treesInRange.size();i++){
                //    neighbors.addAll(treesInRange.get(i).findNearestNeighborsInRange(target,(radius/1000)));
                //}

                Collections.sort(Objects.requireNonNull(neighbors), (o1, o2) -> {
                    Float f1 = o1.distanceTo(target);
                    Float f2 = o2.distanceTo(target);
                    return f1.compareTo(f2);
                });

                if (neighbors.size()<k){
                    return neighbors;
                }else{
                    radius = neighbors.get(k-1).distanceTo(target);
                    System.out.println("Total points found are "+neighbors.size()+" and "+k+" point(s) are in range "+(int) (radius)+"m");
                    return new ArrayList<>(neighbors.subList(0, k));
                }
            }else{
                System.out.println("No other trees are included in that range!");
                return neighbors;
            }
        }catch (Exception e){
            System.out.println("Error in findKNearestNeighbors!");
            e.printStackTrace();
            return null;
        }
    }

    private static KDTree getMainTree(GeoPoint point){
        try {
            for (int i=0;i<treeEdges.size();i++){
                if (treeEdges.get(i)<point.getLon()){
                    return getTree(i+1);
                }
            }
            kdTrees =  new KDTree[treeEdges.size()];
            return getTree(treeEdges.size());
        }catch (Exception e){
            System.out.println("Error in getMainTree(point)!");
            e.printStackTrace();
            return null;
        }
    }

    private static int getKeyTree(GeoPoint point){
        try {
            for (int i=0;i<treeEdges.size();i++){
                if (treeEdges.get(i)<point.getLon()){
                    return i+1;
                }
            }
            return treeEdges.size();
        }catch (Exception e){
            System.out.println("Error in getKeyTree(point)!");
            e.printStackTrace();
            return -1;
        }
    }

    private static ArrayList<KDTree> getTreesInRange(GeoPoint point, double range){
        try {
            int last = 0;
            ArrayList<KDTree> treesInRange = new ArrayList<>();

            for (int i=0;i<treeEdges.size();i++){
                if (new GeoPoint(treeEdges.get(i).floatValue(),point.getLat()).distanceTo(point)<range){
                    treesInRange.add(getTree(i+1));
                    last=i;
                }
            }
            if (last!=0 && last<treeEdges.size()-1){
                treesInRange.add(getTree(last+2));
            }
            return treesInRange;
        }catch (Exception e){
            System.out.println("Error in getTreesInRange!");
            e.printStackTrace();
            return null;
        }
    }

    @SuppressLint("DefaultLocale")
    private static void printSize(String json, String key){
        byte[] byteArray;
        byteArray = json.getBytes(StandardCharsets.UTF_8);
        float size = byteArray.length;
        size = size/1024/1024;
        if (size!=0){
            System.out.println(key+" loaded in memory with size:" + String.format("%.2f",size)+"MB");
        }
    }

    private static KDTree generateTree(int n){
        try {
            BufferedReader file = new BufferedReader(new InputStreamReader(context.getAssets().open(input)));
            for (int i=0;i<(n-1)*treeMaxPoints;i++){
                file.readLine();
            }
            String line = file.readLine();
            ArrayList<GeoPoint> points = new ArrayList<>();
            for (int i=0; i<treeMaxPoints; i++) {
                int br = line.indexOf("-");
                String lon = line.substring(0, br);
                String lat = line.substring(br + 1);
                points.add(new GeoPoint(lon, lat));

                line = file.readLine();
                if (line == null) {
                    break;
                }
            }
            file.close();
            KDTree tree = new KDTree(points,leafMaxPoints);
            saveTree(n,tree);
            return tree;
        }catch (Exception e){
            System.out.println("Error in generateTree!");
            e.printStackTrace();
            return null;
        }
    }

    private static ArrayList<Double> generateMap(int lineCount){ //Split the file to pieces
        try {
            int pieces;
            String line = null;
            String next;
            ArrayList<Double> trees = new ArrayList<>();

            if (treeMaxPoints > lineCount) {
                pieces = 1;
            } else {
                pieces = lineCount / treeMaxPoints;
                if (lineCount % treeMaxPoints != 0) {
                    pieces++;
                }
            }

            BufferedReader file = new BufferedReader(new InputStreamReader(context.getAssets().open(input)));
            next = file.readLine();
            for (int i=0; i<pieces; i++) {
                for (int j=0; j<treeMaxPoints; j++) {
                    line = next;
                    next = file.readLine();
                    if (next==null) {
                        break;
                    }
                }
                assert line != null;
                int br = line.indexOf("-");
                char[] chars = line.toCharArray();
                char[] lon = Arrays.copyOfRange(chars, 0, br);
                trees.add(Double.parseDouble(String.valueOf(lon)));
            }
            file.close();
            return trees;
        }catch (Exception e) {
            System.out.println("Error in generateMap!");
            e.printStackTrace();
            return null;
        }
    }

    private static int countFileLines() {
        try {
            int count = 0;
            BufferedReader file = new BufferedReader(new InputStreamReader(context.getAssets().open(input)));
            while ((file.readLine()) != null) {
                count++;
            }
            file.close();
            System.out.println("File includes "+count+" points!");
            return count;
        } catch (Exception e) {
            System.out.println("Counting lines error!");
            e.printStackTrace();
            return -1;
        }
    }

    private static KDTree getTree(int n){ //Get the tree with the number n
        try {

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String key = "KDTree"+n;
            String json = prefs.getString(key, "");
            if (json.isEmpty()){
                return generateTree(n);
            }
            printSize(json,key);
            return new Gson().fromJson(json, KDTree.class);

            /*
            String fileName = "KDTree" + n + ".tree";
            FileInputStream fis = context.openFileInput(fileName);
            ObjectInputStream is = new ObjectInputStream(fis);
            KDTree tree = (KDTree) is.readObject();
            is.close();
            fis.close();
            return tree;

             */
        }catch (Exception e){
            System.out.println("Error in getTree(n)!");
            e.printStackTrace();
            return generateTree(n);
        }
    }

    private static void saveTree(int n, KDTree tree) {
        try{
            new Thread(() -> {
                try {
                    System.out.println("Generating tree " +n+ "..");
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    Gson gson = new Gson();
                    String json = gson.toJson(tree);
                    String key = "KDTree"+n;
                    prefsEditor.putString(key, json);
                    prefsEditor.apply();

                    /*
                    // write object to file
                    String fileName = "KDTree" + n + ".tree";
                    FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                    ObjectOutputStream os = new ObjectOutputStream(fos);
                    os.writeObject(tree);
                    os.close();
                    fos.close();

                     */

                    System.out.println("Tree " + n + " saved locally!");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }).start();
        }catch (Exception e){
            System.out.println("Error in saveTree!");
            e.printStackTrace();
        }
    }

    private static void saveTreeGroup(ArrayList<Double> trees){
        new Thread(() -> {
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor prefsEditor = prefs.edit();
                String json = new Gson().toJson(trees);
                prefsEditor.putString("KDTrees", json);
                prefsEditor.apply();
            }catch (Exception e){
                System.out.println("Error in saveTreeGroup!");
                e.printStackTrace();
            }
        }).start();
    }
}
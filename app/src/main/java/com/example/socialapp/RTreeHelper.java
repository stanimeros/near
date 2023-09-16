package com.example.socialapp;

import android.annotation.SuppressLint;
import android.content.Context;
import com.github.davidmoten.grumpy.core.Position;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Serializer;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import rx.Observable;
import rx.functions.Func1;

public class RTreeHelper{
    public static RTree<String,Point> rtree;
    public static void createRTree(Context context){
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("Creating RTree ..");
            rtree = RTree.star().create();
            //rtree = RTree.star().maxChildren(8).create();
            //rtree = RTree.star().splitter(new SplitterQuadratic()).create();
            System.out.println("Inserting points ..");
            BufferedReader file = new BufferedReader(new InputStreamReader(context.getAssets().open(MainActivity.unsorted_input)));
            String line = file.readLine();
            while (line != null) {
                int br = line.indexOf("-");
                String lon = line.substring(0, br);
                String lat = line.substring(br + 1);
                rtree = rtree.add("", Geometries.pointGeographic(Double.parseDouble(lon), Double.parseDouble(lat)));
                line = file.readLine();
            }
            file.close();
            System.out.println("RTree created successfully ..");
            saveRTree(context); //saving locally
            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            System.out.println("========= RTREE CREATION TOOK =========");
            System.out.println(millis);
        }catch (Exception e){
            System.out.println("Error while creating RTree!");
            e.printStackTrace();
        }
    }

    @SuppressLint("DefaultLocale")
    public static ArrayList<GeoPoint> getPointsFromRange(GeoPoint geoPoint, int k, double distanceInKm) {
        List<Entry<String, Point>> list = null;
        final double sqrt2 = Math.sqrt(2);
        while (list==null || list.size()<k){
            System.out.println("A query is running now .." + String.format("%.2f", distanceInKm*1000) + "m");

            list = RTreeHelper.search(rtree, Geometries.pointGeographic(geoPoint.getLon(), geoPoint.getLat()), distanceInKm)
                    .toList().toBlocking().single();

            if (list.size()<k){
                System.out.println("Results are "+list.size()+" < k: "+k);
                distanceInKm = distanceInKm*sqrt2;
            }
        }

        ArrayList<GeoPoint> results = new ArrayList<>();
        for (int i=0;i<list.size();i++){
            GeoPoint point = new GeoPoint((float) list.get(i).geometry().x(), (float) list.get(i).geometry().y());
            //System.out.println(i+1 +")" + point.distanceTo(geoPoint));
            results.add(point);
        }

        Collections.sort(Objects.requireNonNull(results), (o1, o2) -> {
            Float f1 = o1.distanceTo(geoPoint);
            Float f2 = o2.distanceTo(geoPoint);
            return f1.compareTo(f2);
        });
        return new ArrayList<>(results.subList(0, k));
    }

    public static ArrayList<GeoPoint> getPointsFromRange(GeoPoint geoPoint, int k, double distanceInKm,RTree<String,Point> rtree) {
        List<Entry<String, Point>> list = null;
        final double sqrt2 = Math.sqrt(2);
        while (list==null || list.size()<k){
            System.out.println("A query is running now .." + String.format("%.2f", distanceInKm*1000) + "m");

            list = RTreeHelper.search(rtree, Geometries.pointGeographic(geoPoint.getLon(), geoPoint.getLat()), distanceInKm)
                    .toList().toBlocking().single();

            if (list.size()<k){
                System.out.println("Results are "+list.size()+" < k: "+k);
                distanceInKm = distanceInKm*sqrt2;
            }
        }

        ArrayList<GeoPoint> results = new ArrayList<>();
        for (int i=0;i<list.size();i++){
            GeoPoint point = new GeoPoint((float) list.get(i).geometry().x(), (float) list.get(i).geometry().y());
            results.add(point);
        }

        Collections.sort(Objects.requireNonNull(results), (o1, o2) -> {
            Float f1 = o1.distanceTo(geoPoint);
            Float f2 = o2.distanceTo(geoPoint);
            return f1.compareTo(f2);
        });
        return new ArrayList<>(results.subList(0, k));
    }

    private static <T> Observable<Entry<T, Point>> search(RTree<T, Point> tree, Point lonLat, final double distanceKm) {
        // First we need to calculate an enclosing lat long rectangle for this
        // distance then we refine on the exact distance
        final Position from = Position.create(lonLat.y(), lonLat.x());
        Rectangle bounds = createBounds(from, distanceKm);

        return tree
                // do the first search using the bounds
                .search(bounds)
                // refine using the exact distance
                .filter(new Func1<Entry<T, Point>, Boolean>() {
                    @Override
                    public Boolean call(Entry<T, Point> entry) {
                        Point p = entry.geometry();
                        Position position = Position.create(p.y(), p.x());
                        return from.getDistanceToKm(position) < distanceKm;
                    }
                });
    }

    private static Rectangle createBounds(final Position from, final double distanceKm) {
        Position north = from.predict(distanceKm, 0);
        Position south = from.predict(distanceKm, 180);
        Position east = from.predict(distanceKm, 90);
        Position west = from.predict(distanceKm, 270);

        return Geometries.rectangleGeographic(west.getLon(), south.getLat(), east.getLon(), north.getLat());
    }

    private static void saveRTree(Context context){
        try {
            System.out.println("Saving locally ..");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Serializer<String, Point> serializer = Serializers.flatBuffers().utf8();
            serializer.write(rtree, baos);
            byte[] serializedData = baos.toByteArray();

            File of = new File(context.getFilesDir(), "rtree.bin");
            OutputStream os = new FileOutputStream(of);
            os.write(serializedData);
            os.flush();
            System.out.println("Saved successfully!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

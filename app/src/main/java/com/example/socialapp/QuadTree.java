package com.example.socialapp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class QuadTree implements Serializable{
    public final QuadNode root ;
    public QuadTree(ArrayList<GeoPoint> geoList, int bucketMax) {
        root = new QuadNode(0,bucketMax);
        root.insertList(geoList);
    }
    public ArrayList<GeoPoint> getKNearestList(GeoPoint target,int k,double distanceInKm) {
        try {
            double radiusInKm = distanceInKm;
            final double sqrt2 = Math.sqrt(2);
            ArrayList<GeoPoint> kNearestNeighborsList = new ArrayList<>();
            while (kNearestNeighborsList.size()<k){
                kNearestNeighborsList = getNearestNeighbors(target,radiusInKm);
                if (kNearestNeighborsList.size()<k){
                    radiusInKm = radiusInKm*sqrt2;
                }
            }
            System.out.println("K is "+k+",found "+kNearestNeighborsList.size()+" point(s) in radius "+(int) (radiusInKm*1000)+"m");
            return new ArrayList<>(kNearestNeighborsList.subList(0, k));
        }catch (Exception e){
            System.out.println("Error in findKNearestNeighbors!");
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<GeoPoint> getNearestNeighbors(GeoPoint target,double radiusInKm) {
        try {
            double meters = radiusInKm / 111.32;
            double rectMinLat = (target.getLat()-meters);
            double rectMaxLat = (target.getLat()+meters);

            double rectMinLon = (target.getLon()-meters / Math.cos(rectMinLat * 0.01745));
            double rectMaxLon = (target.getLon()+meters / Math.cos(rectMaxLat * 0.01745));

            if (root.getMinLon()>rectMaxLon || root.getMaxLon()<rectMinLon){
                System.out.println("Target is out of area!");
                return null;
            }
            if (root.getMinLat()>rectMaxLat || root.getMaxLat()<rectMinLat){
                System.out.println("Target is out of area!");
                return null;
            }

            //Starting a thread for every node!
            ArrayList<GeoPoint> kNearestNeighborsList = new ArrayList<>();
            int countNodes = countNodes(root);
            if (countNodes>=85){
                ArrayList<QuadNode> nodesInDepth = getNodesInSpecificDepth(1);
                assert nodesInDepth != null;
                Thread[] threads = new Thread[nodesInDepth.size()];

                for (int i=0;i<nodesInDepth.size();i++){
                    int finalI = i;
                    ArrayList<GeoPoint> finalKNearestNeighborsList = kNearestNeighborsList;
                    threads[i] = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Thread.currentThread().setName("Thread-NodeSearch"+ finalI);
                            ArrayList<GeoPoint> tempList = getPointsInsideRectangle(rectMinLat,rectMaxLat,rectMinLon,rectMaxLon, nodesInDepth.get(finalI));
                            //System.out.println(Thread.currentThread().getName()+ " found " + tempList.size()+ " points!");
                            assert tempList != null;
                            finalKNearestNeighborsList.addAll(tempList);
                        }
                    });
                    threads[i].start();
                }

                for (Thread thread : threads) {
                    thread.join();
                }
            }else{
                kNearestNeighborsList = getPointsInsideRectangle(rectMinLat,rectMaxLat,rectMinLon,rectMaxLon,root);
            }

            Collections.sort(Objects.requireNonNull(kNearestNeighborsList), (o1, o2) -> {
                Float f1 = o1.distanceTo(target);
                Float f2 = o2.distanceTo(target);
                return f1.compareTo(f2);
            });

            //return getPointsInsideCircle(kNearestNeighborsList,target,radiusInKm); //CIRCLE SECURITY
            return kNearestNeighborsList;
        }catch (Exception e){
            System.out.println("Error in findNearestNeighborsInradius!");
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<GeoPoint> getPointsInsideRectangle(double rectMinLat, double rectMaxLat, double rectMinLon, double rectMaxLon, QuadNode node){
        try {
            if (node.hasData()){
                ArrayList<GeoPoint> geoList = new ArrayList<>();

                if (node.getMinLon()>rectMaxLon || node.getMaxLon()<rectMinLon){
                    //System.out.println("Out of radius!");
                    return geoList;
                }
                if (node.getMinLat()>rectMaxLat || node.getMaxLat()<rectMinLat){
                    //System.out.println("Out of radius!");
                    return geoList;
                }

                ArrayList<GeoPoint> bucket = node.getBucket();
                for (int i=0;i<bucket.size();i++){
                    if (bucket.get(i).getLat()>rectMinLat && bucket.get(i).getLat()<rectMaxLat){
                        if (bucket.get(i).getLon()>rectMinLon && bucket.get(i).getLon()<rectMaxLon){
                            geoList.add(bucket.get(i));
                        }
                    }
                }
                //System.out.println("Thread "+Thread.currentThread().getName() + " got "+ geoList.size() + "/" + bucket.size() +
                //        " points from this node in depth "+ node.getDepth());
                return geoList;
            }

            if (node.hasChildren()){
                ArrayList<GeoPoint> geoList = new ArrayList<>();

                if (node.getMinLon()>rectMaxLon || node.getMaxLon()<rectMinLon){
                    //System.out.println("Out of radius!");
                    return geoList;
                }
                if (node.getMinLat()>rectMaxLat || node.getMaxLat()<rectMinLat){
                    //System.out.println("Out of radius!");
                    return geoList;
                }

                if (rectMaxLat < node.getMedianLat()){ //SW - SE
                    if (rectMaxLon < node.getMedianLon()){ //SW
                        return getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.SWChild);
                    }else if (rectMinLon > node.getMedianLon()){ //SE
                        return getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.SEChild);
                    }else{ //SW AND SE
                        geoList.addAll(Objects.requireNonNull(getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.SWChild))) ;
                        geoList.addAll(Objects.requireNonNull(getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.SEChild))) ;
                        return geoList;
                    }
                }else if(rectMinLat > node.getMedianLat()){ //NW - NE
                    if (rectMaxLon < node.getMedianLon()){ //NW
                        return getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.NWChild);
                    }else if (rectMinLon > node.getMedianLon()){ //NE
                        return getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.NEChild);
                    }else{ //NW AND NE
                        geoList.addAll(Objects.requireNonNull(getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.NWChild))) ;
                        geoList.addAll(Objects.requireNonNull(getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.NEChild))) ;
                        return geoList;
                    }
                }else{ // ALL REGIONS
                    geoList.addAll(Objects.requireNonNull(getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.SWChild))) ;
                    geoList.addAll(Objects.requireNonNull(getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.SEChild))) ;
                    geoList.addAll(Objects.requireNonNull(getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.NWChild))) ;
                    geoList.addAll(Objects.requireNonNull(getPointsInsideRectangle(rectMinLat, rectMaxLat, rectMinLon, rectMaxLon, node.NEChild))) ;
                    return geoList;
                }
            }else{
                return new ArrayList<>();
            }
        }catch (Exception e){
            System.out.println("Error in getPointsInsideRectangle!");
            e.printStackTrace();
            return null;
        }
    }
    private ArrayList<GeoPoint> getPointsInsideCircle(ArrayList<GeoPoint> geoList,GeoPoint target,double radiusInKm){
        try {
            if (geoList.size()==0){
                return geoList;
            }
            if (geoList.get(geoList.size()-1).distanceTo(target)<radiusInKm*1000){
                return geoList;
            }

            for (int i=geoList.size()-1;i>=0;i--){
                if (target.distanceTo(geoList.get(i))>radiusInKm*1000){
                    geoList.remove(geoList.get(i));
                }else{
                    return geoList;
                }
            }
            return geoList;
        }catch (Exception e){
            System.out.println("Error in getPointsInsideCircle!");
            e.printStackTrace();
            return null;
        }
    }
    private ArrayList<QuadNode> getNodesInSpecificDepth(int specificDepth){
        try {
            ArrayList<QuadNode> nodes = new ArrayList<>();
            nodes.add(root);
            while (nodes.get(0).getDepth()<specificDepth){
                ArrayList<QuadNode> temp = new ArrayList<>();
                for (int i=0;i<nodes.size();i++){
                    temp.add(nodes.get(i).SWChild);
                    temp.add(nodes.get(i).SEChild);
                    temp.add(nodes.get(i).NWChild);
                    temp.add(nodes.get(i).NEChild);
                }
                nodes = temp;
            }
            return nodes;
        }catch (Exception e){
            System.out.println("Error in getNodesInSpecificDepth!");
            e.printStackTrace();
            return null;
        }
    }
    private int countNodes(QuadNode node) {
        if (node == null) {
            return 0;
        }
        return 1 + countNodes(node.SEChild) + countNodes(node.SWChild) + countNodes(node.NEChild) + countNodes(node.NWChild);
    }
}
package com.example.socialapp.methods.kd;

import com.example.socialapp.GeoPoint;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class KDNode implements Serializable {
    private final ArrayList<GeoPoint> bucket = new ArrayList<>();
    public KDNode RChild = null;
    public KDNode LChild = null;
    private double median;
    private float minLon;
    private float maxLon;
    private float minLat;
    private float maxLat;
    private final int depth;
    private final int bucketMax;

    public KDNode(int depth, int bucketMax){ //Only when creating tree!
        this.depth = depth;
        this.bucketMax = bucketMax;
    }

    public void insertList(ArrayList<GeoPoint> geoList){
        try {
            if (geoList.size()<=bucketMax && !hasChildren()){
                if (geoList.size()>0){
                    bucket.addAll(geoList);
                    setMinAndMax(geoList);
                }
            }else{
                setMinAndMax(geoList);
                median = getMedian(geoList);
                ArrayList<GeoPoint> LChildList = new ArrayList<>();
                ArrayList<GeoPoint> RChildList = new ArrayList<>();
                for (int i=0;i< geoList.size();i++){
                    if (depth%2==0){
                        if(geoList.get(i).getLat()<median){
                            LChildList.add(geoList.get(i));
                        }else{
                            RChildList.add(geoList.get(i));
                        }
                    }else{
                        if(geoList.get(i).getLon()<median){
                            LChildList.add(geoList.get(i));
                        }else{
                            RChildList.add(geoList.get(i));
                        }
                    }
                }
                LChild = new KDNode(depth+1,bucketMax);
                RChild = new KDNode(depth+1,bucketMax);

                LChild.insertList(LChildList);
                RChild.insertList(RChildList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private double getMedian(ArrayList<GeoPoint> geoList){
        try {
            ArrayList<Float> list = new ArrayList<>();

            if (depth%2==0){
                for (int i = 0; i<geoList.size(); i++) {
                    list.add(geoList.get(i).getLat());
                }
            }else{
                for (int i = 0; i<geoList.size(); i++) {
                    list.add(geoList.get(i).getLon());
                }
            }

            Collections.sort(list);
            if (list.size() % 2 == 1)
                return list.get((list.size() + 1) / 2 - 1);
            else {
                double lower = list.get(list.size() / 2 - 1);
                double upper = list.get(list.size() / 2);
                return  (lower + upper) / 2;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    public boolean contains(GeoPoint geoPoint){
        if (hasData()){
            for (int i=0;i<bucket.size();i++){
                if (bucket.get(i).getLat()==geoPoint.getLat() && bucket.get(i).getLon()==geoPoint.getLon()){
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private void setMinAndMax(ArrayList<GeoPoint> geoList){
        try {
            float min = geoList.get(0).getLon();
            float max = geoList.get(0).getLon();

            for (int i = 1; i<geoList.size(); i++) {
                if (geoList.get(i).getLon() > max) {
                    max = geoList.get(i).getLon();
                }
                if (geoList.get(i).getLon() < min) {
                    min = geoList.get(i).getLon();
                }
            }
            minLon = min;
            maxLon = max;

            min = geoList.get(0).getLat();
            max = geoList.get(0).getLat();

            for (int i = 1; i<geoList.size(); i++) {
                if (geoList.get(i).getLat() > max) {
                    max = geoList.get(i).getLat();
                }
                if (geoList.get(i).getLat() < min) {
                    min = geoList.get(i).getLat();
                }
            }
            minLat = min;
            maxLat = max;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean hasData(){
        return bucket.size()>0;
    }

    public boolean hasChildren(){
        return RChild != null || LChild != null;
    }

    public double getMedian() {
        return median;
    }

    public int getDepth() {
        return depth;
    }

    public float getMinLon() {
        return minLon;
    }

    public float getMaxLon() {
        return maxLon;
    }

    public float getMinLat() {
        return minLat;
    }

    public float getMaxLat() {
        return maxLat;
    }

    public ArrayList<GeoPoint> getBucket() {
        return bucket;
    }
}

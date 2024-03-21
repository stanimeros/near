package com.example.socialapp.methods.quad;

import com.example.socialapp.GeoPoint;

import java.util.ArrayList;
public class QuadNode {
    private final ArrayList<GeoPoint> bucket = new ArrayList<>();
    public QuadNode SWChild = null;
    public QuadNode SEChild = null;
    public QuadNode NWChild = null;
    public QuadNode NEChild = null;
    private double medianLon;
    private double medianLat;
    private final int depth;
    private final int bucketMax;
    private float minLon;
    private float maxLon;
    private float minLat;
    private float maxLat;

    public QuadNode(int depth, int bucketMax){ //Only when creating tree!
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
                medianLon = (maxLon-minLon)/2 + minLon;
                medianLat = (maxLat-minLat)/2 + minLat;

                ArrayList<GeoPoint> SWChildList = new ArrayList<>();
                ArrayList<GeoPoint> SEChildList = new ArrayList<>();
                ArrayList<GeoPoint> NWChildList = new ArrayList<>();
                ArrayList<GeoPoint> NEChildList = new ArrayList<>();

                for (int i=0;i< geoList.size();i++){
                    if (geoList.get(i).getLon()<medianLon){
                        if (geoList.get(i).getLat()<medianLat){
                            SWChildList.add(geoList.get(i));
                        }else{
                            NWChildList.add(geoList.get(i));
                        }
                    }else{
                        if (geoList.get(i).getLat()<medianLat){
                            SEChildList.add(geoList.get(i));
                        }else{
                            NEChildList.add(geoList.get(i));
                        }
                    }
                }
                SWChild = new QuadNode(depth+1,bucketMax);
                SEChild = new QuadNode(depth+1,bucketMax);
                NWChild = new QuadNode(depth+1,bucketMax);
                NEChild = new QuadNode(depth+1,bucketMax);

                SWChild.insertList(SWChildList);
                SEChild.insertList(SEChildList);
                NWChild.insertList(NWChildList);
                NEChild.insertList(NEChildList);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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

    public boolean hasData(){
        return bucket.size()>0;
    }

    public boolean hasChildren(){
        return SWChild != null || SEChild != null || NWChild != null || NEChild != null;
    }

    public double getMedianLon() {
        return medianLon;
    }

    public double getMedianLat() {
        return medianLat;
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

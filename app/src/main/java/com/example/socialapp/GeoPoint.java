package com.example.socialapp;

import android.location.Location;
import java.io.Serializable;

public class GeoPoint implements Serializable {
    private final float lat;
    private final float lon;

    public GeoPoint(float lon, float lat){
        this.lat = lat;
        this.lon = lon;
    }

    public GeoPoint(String lon,String lat){
        this.lat = Float.parseFloat(lat);
        this.lon = Float.parseFloat(lon);
    }

    float distanceTo(GeoPoint geoPoint){
        Location thisLocation = new Location("thisLocation");
        thisLocation.setLatitude(lat);
        thisLocation.setLongitude(lon);

        Location geoPointLocation = new Location("geoPointLocation");
        geoPointLocation.setLatitude(geoPoint.getLat());
        geoPointLocation.setLongitude(geoPoint.getLon());

        return thisLocation.distanceTo(geoPointLocation);
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }
}

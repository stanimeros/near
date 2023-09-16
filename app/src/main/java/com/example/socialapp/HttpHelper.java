package com.example.socialapp;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class HttpHelper {
    public static ArrayList<GeoPoint> getKNearestList(GeoPoint geoPoint,int k,double distanceInKm) {
        ArrayList <GeoPoint> kNearestList = new ArrayList<>();
        try {
            String okeanos = "http://snf-38501.ok-kno.grnetcloud.net/service/";
            String stanimeros = "https://stanimeros.site/service/";
            String postgres = "http://localhost/service/";

            GeoPoint pamak = new GeoPoint((float)22.96017,(float)40.625041);


            String url = okeanos + "getKNearestList.php?" +
                    "k=" + k + "&" +
                    "lon=" + geoPoint.getLon() + "&" +
                    "lat=" + geoPoint.getLat() + "&" +
                    "distanceInKm=" + distanceInKm + "&" +
                    "table_name=" + MainActivity.table;

            String jsonResponse;

            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    jsonResponse = response.toString();
                }
            } else {
                System.out.println("HTTP request failed with response code: " + responseCode);
                jsonResponse = "HTTP request failed with response code: " + responseCode;
            }

            JSONArray array = new JSONArray(jsonResponse);

            for (int i = 0; i < array.length(); i++) {
                kNearestList.add(new GeoPoint(array.getJSONObject(i).getString("longitude"),
                        array.getJSONObject(i).getString("latitude")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kNearestList;
    }

    public static ArrayList<GeoPoint> getPointsFromOSM(GeoPoint geoPoint,double distanceInKm) {
        ArrayList <GeoPoint> list = new ArrayList<>();
        try {
            float minLon = (float) (geoPoint.getLon() - (distanceInKm / (111.11 * Math.cos(Math.toRadians(geoPoint.getLat())))));
            float maxLon = (float) (geoPoint.getLon() + (distanceInKm / (111.11 * Math.cos(Math.toRadians(geoPoint.getLat())))));
            float minLat = (float) (geoPoint.getLat() - (distanceInKm/111.11));
            float maxLat = (float) (geoPoint.getLat() + (distanceInKm/111.11));
            String url = "http://overpass-api.de/api/interpreter?data=[out:json];(node("+minLat+","+minLon+","+maxLat+","+maxLon+"););out;";
            System.out.println(url);

            String jsonResponse;
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    jsonResponse = response.toString();
                }
            } else {
                System.out.println("HTTP request failed with response code: " + responseCode);
                jsonResponse = "HTTP request failed with response code: " + responseCode;
            }
            JSONObject jsonObject = new JSONObject(jsonResponse);
            JSONArray elementsArray = jsonObject.getJSONArray("elements");
            for (int i = 0; i < elementsArray.length(); i++) {
                JSONObject element = elementsArray.getJSONObject(i);
                if (element.has("type") && element.getString("type").equals("node")) {
                    list.add(new GeoPoint(element.getString("lon"),
                            element.getString("lat")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
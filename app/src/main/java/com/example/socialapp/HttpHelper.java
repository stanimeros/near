package com.example.socialapp;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class HttpHelper {
    static Context context;
    private static final String apiURL = "https://stanimeros.com/near/api/";

    public static ArrayList<GeoPoint> getKNearestList(GeoPoint geoPoint,int k,double distanceInKm) {
        try {
            String endpoint = apiURL + "get_k_nearest_list.php?" +
                    "k=" + k + "&" +
                    "lon=" + geoPoint.getLon() + "&" +
                    "lat=" + geoPoint.getLat() + "&" +
                    "distanceInKm=" + distanceInKm + "&" +
                    "table_name=" + "geopoints_" +MainActivity.kmNum + "km";

            JSONObject jsonObject = getResponse(endpoint);
            if (jsonObject != null) {
                ArrayList <GeoPoint> kNearestList = new ArrayList<>();
                JSONArray array = jsonObject.getJSONArray("points");

                for (int i = 0; i < array.length(); i++) {
                    kNearestList.add(new GeoPoint(array.getJSONObject(i).getString("longitude"),
                        array.getJSONObject(i).getString("latitude")));
                }

                return kNearestList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<GeoPoint> getPointsFromOSM(GeoPoint geoPoint,double distanceInKm) {
        try {
            float minLon = (float) (geoPoint.getLon() - (distanceInKm / (111.11 * Math.cos(Math.toRadians(geoPoint.getLat())))));
            float maxLon = (float) (geoPoint.getLon() + (distanceInKm / (111.11 * Math.cos(Math.toRadians(geoPoint.getLat())))));
            float minLat = (float) (geoPoint.getLat() - (distanceInKm/111.11));
            float maxLat = (float) (geoPoint.getLat() + (distanceInKm/111.11));
            String endpoint = "http://overpass-api.de/api/interpreter?data=[out:json];(node("+minLat+","+minLon+","+maxLat+","+maxLon+"););out;";

            JSONObject jsonObject = getResponse(endpoint);
            if (jsonObject != null) {
                ArrayList <GeoPoint> list = new ArrayList<>();
                JSONArray elementsArray = jsonObject.getJSONArray("elements");
                for (int i = 0; i < elementsArray.length(); i++) {
                    JSONObject element = elementsArray.getJSONObject(i);
                    if (element.has("type") && element.getString("type").equals("node")) {
                        list.add(new GeoPoint(element.getString("lon"),
                                element.getString("lat")));
                    }
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean signUp(String phone, String username, String password) {
        if (phone.isEmpty() || username.isEmpty() || password.isEmpty() ||
                phone.length()<8 || username.length()<=3 || password.length()<=3){
            return false; //Something is wrong!
        }

        String endpoint = apiURL + "sign_up.php?" +
                "phone=" + phone + "&" +
                "username=" + username + "&" +
                "password=" + password;

        JSONObject jsonObject = getResponse(endpoint);
        return jsonObject != null;
    }

    public static User signIn(String phone,String password) {
        try {
            if (phone.isEmpty()) return null;
            String endpoint = apiURL + "sign_in.php?" +
                    "phone=" + phone + "&" +
                    "password=" + password;

            JSONObject jsonObject = getResponse(endpoint);
            if (jsonObject != null) {
                JSONObject userObject = jsonObject.getJSONObject("user");
                String result_phone = userObject.getString("phone");
                String username = userObject.getString("username");
                String image = userObject.getString("image");
                String joinDate = userObject.getString("joinDate");

                User user = new User(result_phone, username, Integer.parseInt(image));
                user.setJoinDate(joinDate);
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean sendFriendRequest(String phone,String friendUsername) {
        String endpoint = apiURL + "send_friend_request.php?" +
                "phone=" + phone + "&" +
                "username=" + friendUsername;

        JSONObject jsonObject = getResponse(endpoint);
        return jsonObject != null;
    }

    public static ArrayList<User> getFriendRequests(String phone) {
        String endpoint = apiURL + "get_friend_requests.php?" +
                "phone=" + phone;
        try {
            JSONObject jsonObject = getResponse(endpoint);
            if (jsonObject != null) {
                ArrayList<User> users = new ArrayList<>();
                JSONArray requestsArray = jsonObject.getJSONArray("requests");
                for (int i = 0; i < requestsArray.length(); i++) {
                    JSONObject userObject = requestsArray.getJSONObject(i);
                    String result_phone = userObject.getString("phone");
                    String username = userObject.getString("username");
                    String image = userObject.getString("image");
                    users.add(new User(result_phone, username, Integer.parseInt(image)));
                }

                return users;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(endpoint);
        }
        return null;
    }

    public static boolean acceptFriendRequest(String phone,String username) {
        String endpoint = apiURL + "accept_friend_request.php?" +
                "phone=" + phone + "&" +
                "username=" + username;

        JSONObject jsonObject = getResponse(endpoint);
        return jsonObject != null;
    }

    public static boolean removeFriendOrRequest(String phone,String username) {
        String endpoint = apiURL + "remove_friend.php?" +
                "phone=" + phone + "&" +
                "username=" + username;

        JSONObject jsonObject = getResponse(endpoint);
        return jsonObject != null;
    }

    public static ArrayList<User> getFriends(String phone){
        String endpoint = apiURL + "get_friends.php?" +
                "phone=" + phone;
        try {
            JSONObject jsonObject = getResponse(endpoint);
            if (jsonObject != null){
                ArrayList<User> friends = new ArrayList<>();
                JSONArray friendsArray = jsonObject.getJSONArray("friends");
                for (int i = 0; i < friendsArray.length(); i++) {
                    JSONObject userObject = friendsArray.getJSONObject(i);

                    String friend_phone = userObject.getString("phone");
                    String username = userObject.getString("username");
                    String image = userObject.getString("image");
                    String joinDate = userObject.getString("joinDate");
                    String updateDatetime = userObject.getString("updateTime");
                    String longitude = userObject.getString("longitude");
                    String latitude = userObject.getString("latitude");
                    String distance = userObject.getString("distance");

                    User user = new User(friend_phone, username, Integer.parseInt(image));
                    user.setJoinDate(joinDate);
                    user.setUpdateDatetime(updateDatetime);
                    user.setLocation(new GeoPoint(longitude, latitude));
                    user.setMetersAway(Float.parseFloat(distance));
                    friends.add(user);
                }

                return friends;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(endpoint);
        }
        return null;
    }

    public static MyLocation getMyLocation(String phone){
        MyLocation myLocation = new MyLocation();
        myLocation.setMyPointOfInterest(new GeoPoint((float) 22.94,(float) 40.62)); //Port of Thessaloniki

        try {
            String endpoint = apiURL + "get_location.php?" +
                    "phone=" + phone;

            JSONObject jsonObject = getResponse(endpoint);
            if (jsonObject != null && !jsonObject.getString("lon").isEmpty() && !jsonObject.getString("lat").isEmpty()){
                myLocation.setMyPointOfInterest(new GeoPoint(jsonObject.getString("lon"), jsonObject.getString("lat")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return myLocation;
    }

    public static boolean setLocation(GeoPoint myPointOfInterest,String phone) {
        String endpoint = apiURL + "set_location.php?" +
                "phone=" + phone + "&" +
                "lon=" + myPointOfInterest.getLon() + "&" +
                "lat=" + myPointOfInterest.getLat();

        JSONObject jsonObject = getResponse(endpoint);
        return jsonObject != null;
    }

    public static boolean setUsername(String phone,String username) {
        String endpoint = apiURL + "set_username.php?" +
                "phone=" + phone + "&" +
                "username=" + username;

        JSONObject jsonObject = getResponse(endpoint);
        return jsonObject != null;
    }

    public static boolean setImage(String phone,Integer image) {
        String endpoint = apiURL + "set_image.php?" +
                "phone=" + phone + "&" +
                "image=" + image;

        JSONObject jsonObject = getResponse(endpoint);
        return jsonObject != null;
    }

    private static JSONObject getResponse(String endpoint){
        try{
            HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    JSONObject jsonObject = new JSONObject(response.toString());
                    if (endpoint.contains("/near/")){ //MY NEAR API -- RETURNS STATUS
                        if (jsonObject.getString("status").equals("success")){
                            return jsonObject;
                        }else{
                            System.out.println("Near API failed");
                            String message = jsonObject.getString("message");
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
                        }
                    }else{ //OTHER API
                        return jsonObject;
                    }
                }
            } else {
                System.out.println("HTTP request failed with response code: " + responseCode);
                System.out.println(endpoint);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(endpoint);
        }
        return null;
    }
}
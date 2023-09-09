package com.example.socialapp;

import android.annotation.SuppressLint;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ServerSQL {
    private static boolean executed;

    public static void ThreadStart(ServerSQLConnection serverSQL) {
        Thread t = new Thread(serverSQL);
        try {
            t.start();
            t.join(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static boolean signUp(String phone, String username, String password) {
        if (elementExists("phone",phone)){
            return false; //Phone exists!
        }

        if (phone.isEmpty() || username.isEmpty() || password.isEmpty() ||
                phone.length()!=10 || username.length()<=3 || password.length()<=3){
            return false; //Something is wrong!
        }
        String joinDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        //System.out.println(joinDate);

        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Insert("INSERT INTO users (phone,username,password,joinDate) VALUES " +
                "('" + phone + "','" + username + "','" + password + "','" + joinDate + "')");
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.isExecuted(); //SQL statement executed or not
    }

    public static boolean signIn(String phone,String password) {
        if (phone.isEmpty()) return false;
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Select("SELECT phone,password FROM users WHERE phone='" + phone + "'");
        ServerSQL.ThreadStart(serverSQLConnection);
        if (serverSQLConnection.getResults().size() != 0) {
            return serverSQLConnection.getResults().get(1).equals(password);
        }
        return false; //Password or phone is incorrect
    }

    public static boolean elementExists(String element,String value) {
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Select("SELECT "+element+" FROM users WHERE "+element+" = '" +value+ "'");
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.getResults().size() != 0;
    }

    public static boolean requestExists(String phone,String username) {
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        if (MainActivity.jdbcName.equals("postgresql")){
            serverSQLConnection.Select("SELECT friends.id " +
                    "FROM friends " +
                    "INNER JOIN users t1 ON friends.friendId=t1.id " +
                    "INNER JOIN users t2 ON friends.userId=t2.id " +
                    "WHERE (t1.phone='"+phone+"' AND t2.username='"+username+"') OR (t2.phone='"+phone+"' AND t1.username='"+username+"');");
        }else{
            serverSQLConnection.Select("SELECT friends.id " +
                    "FROM friends INNER JOIN users t1 INNER JOIN users t2 ON friends.friendId=t1.id AND friends.userId=t2.id " +
                    "WHERE (t1.phone='"+phone+"' AND t2.username='"+username+"') OR (t2.phone='"+phone+"' AND t1.username='"+username+"')");
        }
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.getResults().size() != 0;
    }

    public static boolean sendFriendRequest(String phone,String friendUsername) {
        if (requestExists(phone,friendUsername)){
            return false;
        }
        if (!elementExists("username",friendUsername)){
            return false;
        }
        if (Objects.equals(friendUsername, getUsername(phone))) {
            return false;
        }
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Insert("INSERT INTO friends (friendId, userId) " +
                "SELECT t2.id , t1.id " +
                "FROM users t1,users t2 " +
                "WHERE t1.phone = '"+phone+"' AND t2.username='"+friendUsername+"'");
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.isExecuted();
    }

    public static ArrayList<User> getFriendRequests(String phone) {
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Select("SELECT t2.username,t2.image " +
                "FROM friends "+
                "INNER JOIN users t1 ON friends.friendId=t1.id " +
                "INNER JOIN users t2 ON friends.userId=t2.id " +
                "WHERE t1.phone='"+phone+"' AND friends.accepted=0");
        ServerSQL.ThreadStart(serverSQLConnection);
        setExecuted(serverSQLConnection.isExecuted());

        ArrayList<String> results = serverSQLConnection.getResults();
        ArrayList<User> users = new ArrayList<>();
        for (int i=0;i<results.size();i=i+2) {
            User user = new User(results.get(i), results.get(i+1));
            users.add(user);
        }
        return users;
    }

    public static boolean acceptFriendRequest(String phone,String username) {
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        if (MainActivity.jdbcName.equals("postgresql")){
            serverSQLConnection.Update("UPDATE friends " +
                    "SET accepted = 1 " +
                    "WHERE friendId IN (SELECT id FROM users WHERE phone = '"+phone+"') " +
                    "AND userId IN (SELECT id FROM users WHERE username = '"+username+"');");
        }else {
            serverSQLConnection.Update("UPDATE friends " +
                    "INNER JOIN users t1 ON friends.friendId=t1.id " +
                    "INNER JOIN users t2 ON friends.userId=t2.id " +
                    "SET friends.accepted = 1 " +
                    "WHERE t1.phone='"+phone+"' AND t2.username='"+username+"'");
        }
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.isExecuted();
    }

    public static boolean removeFriendOrRequest(String phone,String username) {
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        if (MainActivity.jdbcName.equals("postgresql")){
            serverSQLConnection.Update("DELETE FROM friends " +
                    "USING users t1, users t2 " +
                    "WHERE " +
                    "((t1.phone='"+phone+"' AND t2.username='"+username+"') OR " +
                    "(t2.phone='"+phone+"' AND t1.username='"+username+"')) AND " +
                    "(friends.friendId=t1.id AND friends.userId=t2.id);");
        }else {
            serverSQLConnection.Update("DELETE friends.* FROM friends " +
                    "INNER JOIN users t1 ON friends.friendId=t1.id " +
                    "INNER JOIN users t2 ON friends.userId=t2.id " +
                    "WHERE (t1.phone='" + phone + "' AND t2.username='" + username + "') OR (t2.phone='" + phone + "' AND t1.username='" + username + "')");
        }
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.isExecuted();
    }

    public static ArrayList<User> getFriends(String phone){
        ArrayList<User> friends = new ArrayList<>();

        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        if (MainActivity.jdbcName.equals("postgresql")){
            serverSQLConnection.Select("SELECT users.phone, users.username, users.image, users.updateTime, " +
                    "ST_X(users.location::geometry) as longitude, ST_Y(users.location::geometry) as latitude, " +
                    "ST_DistanceSphere(location::geometry, ST_SetSRID( " +
                    "ST_MakePoint( " +
                    "(SELECT ST_X(location::geometry) FROM users WHERE phone = '"+phone+"'), " +
                    "(SELECT ST_Y(location::geometry) FROM users WHERE phone = '"+phone+"')), " +
                    "4326)::geometry) AS distance " +
                    "FROM users " +
                    "INNER JOIN friends ON (friends.friendId = (SELECT id FROM users WHERE phone = '"+phone+"') AND friends.userId = users.id) " +
                    "OR (friends.userId = (SELECT id FROM users WHERE phone = '"+phone+"') AND friends.friendId = users.id) " +
                    "WHERE friends.accepted = 1 " +
                    "ORDER BY distance ASC");
        }else {
            serverSQLConnection.Select("SELECT users.phone, users.username, users.image, users.updateTime, " +
                    "ST_X(users.location) as longitude, ST_Y(users.location) as latitude, " +
                    "ST_Distance_Sphere(location, ST_GeomFromText(" +
                    "CONCAT('POINT(', " +
                    "(SELECT ST_X(location) FROM users WHERE phone = '" + phone + "'), ' ', " +
                    "(SELECT ST_Y(location) FROM users WHERE phone = '" + phone + "'), ')') " +
                    ", 4326)) AS distance " +
                    "FROM users " +
                    "INNER JOIN friends ON (friends.friendId = (SELECT id FROM users WHERE phone = '" + phone + "') AND friends.userId = users.id) " +
                    "OR (friends.userId = (SELECT id FROM users WHERE phone = '" + phone + "') AND friends.friendId = users.id) " +
                    "WHERE friends.accepted = 1 " +
                    "ORDER BY distance ASC");
        }
        ServerSQL.ThreadStart(serverSQLConnection);

        setExecuted(serverSQLConnection.isExecuted());
        ArrayList<String> results = serverSQLConnection.getResults();

        for (int i=0;i<results.size();i=i+7) {
            User user = new User(results.get(i), results.get(i + 1),
                    results.get(i + 2), results.get(i + 3), new GeoPoint(results.get(i + 4), results.get(i + 5)));
            user.setMetersAway(Float.parseFloat(results.get(i+6)));
            friends.add(user);
        }
        return friends;
    }

    public static MyLocation getMyLocation(String phone){
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        if (MainActivity.jdbcName.equals("postgresql")){
            serverSQLConnection.Select("SELECT ST_X(users.location::geometry) as longitude,ST_Y(users.location::geometry) as latitude FROM users WHERE phone='"+phone+"'");
        }else {
            serverSQLConnection.Select("SELECT ST_X(users.location) as longitude,ST_Y(users.location) as latitude FROM users WHERE phone='"+phone+"'");
        }
        ServerSQL.ThreadStart(serverSQLConnection);

        setExecuted(serverSQLConnection.isExecuted());
        ArrayList<String> results = serverSQLConnection.getResults();
        MyLocation myLocation = new MyLocation();
        if (results.isEmpty()){
            myLocation.setMyPointOfInterest(new GeoPoint((float) 22.94,(float) 40.62)); //Port of thessaloniki
        }else{
            myLocation.setMyPointOfInterest(new GeoPoint(results.get(0),results.get(1)));
        }
        return myLocation;
    }

    public static ArrayList<User> getSuggestedFriends(String phone,ArrayList<PhoneContact> contacts) {
        ArrayList<User> suggestedFriendList = new ArrayList<>();
        if (contacts.isEmpty()){
            setExecuted(true);
            return suggestedFriendList;
        }

        StringBuilder myContacts= new StringBuilder();
        for (int i=0;i<contacts.size()-1;i++){
            myContacts.append(" t1.phone=").append(contacts.get(i).getPhone()).append(" OR");
        }
        myContacts.append(" t1.phone=").append(contacts.get(contacts.size() - 1).getPhone());

        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Select("SELECT t1.username,t1.image " +
                "FROM users t1 " +
                "WHERE NOT EXISTS " +
                "(SELECT friends.id FROM friends INNER JOIN users t2 ON " +
                "(t1.id=friendId AND t2.id=userId) OR (t2.id=friendId AND t1.id=userId) WHERE t2.phone='"+phone+"') " +
                "AND ("+myContacts+")");
        ServerSQL.ThreadStart(serverSQLConnection);

        setExecuted(serverSQLConnection.isExecuted());
        ArrayList<String> results = serverSQLConnection.getResults();

        for (int i=0;i<results.size();i=i+2) {
            User user = new User(results.get(i), results.get(i + 1));
            suggestedFriendList.add(user);
        }
        return suggestedFriendList;
    }

    @SuppressLint("SimpleDateFormat")
    public static boolean setLocation(GeoPoint myPointOfInterest,String phone) {
        String updateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //System.out.println(updateTime);

        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        if (MainActivity.jdbcName.equals("postgresql")){
            serverSQLConnection.Update("UPDATE users SET updateTime='"+updateTime+"',location=ST_SetSRID(ST_MakePoint("+myPointOfInterest.getLon()+","+myPointOfInterest.getLat()+"),4326)::point WHERE phone = '" + phone + "'");
        }else{
            serverSQLConnection.Update("UPDATE users SET updateTime='"+updateTime+"',location=ST_GeomFromText('POINT("+myPointOfInterest.getLon()+" "+myPointOfInterest.getLat()+")') WHERE phone = '" + phone + "'");
        }

        ServerSQL.ThreadStart(serverSQLConnection);

        return serverSQLConnection.isExecuted();
    }

    public static boolean setUsername(String phone,String username) {
        if (elementExists("username",username)){
            return false;
        }
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Update("UPDATE users SET username='" +username+ "' WHERE phone = '" +phone+ "'");
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.isExecuted();
    }

    public static boolean setImage(String phone,Integer image) {
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Update("UPDATE users SET image = "+image+" WHERE phone = '" +phone+ "'");
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.isExecuted();
    }

    public static boolean uploadResults(Long millis,String phone) {
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Update("INSERT INTO results(method, time, phone,api,version,device,manufacturer) " +
                "VALUES ('"+MainActivity.choice+"','"+millis+"','"+phone+"','"+Build.VERSION.SDK_INT+"','"+Build.VERSION.RELEASE+"','"+Build.MODEL+"','"+Build.MANUFACTURER+"')");
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.isExecuted();
    }

    public static String getUsername(String phone) {
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Select("SELECT username FROM users WHERE phone = '" + phone + "'");
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.getResult();
    }

    public static String getJoinDate(String phone) {
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Select("SELECT joinDate FROM users WHERE phone = '" + phone + "'");
        ServerSQL.ThreadStart(serverSQLConnection);
        return serverSQLConnection.getResult();
    }

    public static int getImage(String phone) {
        ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
        serverSQLConnection.Select("SELECT image FROM users WHERE phone = '" + phone + "'");
        ServerSQL.ThreadStart(serverSQLConnection);
        return Integer.parseInt(serverSQLConnection.getResult());
    }

    public static ArrayList<GeoPoint> getPointsFromRange(GeoPoint geoPoint,int k,double distanceInKm) {
        ArrayList<String> strings = new ArrayList<>();
        final double sqrt2 = Math.sqrt(2);
        final int columns = 2;
        while (strings.size()/columns<k){
            System.out.println("A query is running now ..");
            ServerSQLConnection serverSQLConnection = new ServerSQLConnection();
            if (MainActivity.jdbcName.equals("postgresql")){
                String st_functions_query = "SELECT ST_X(location::geometry) AS longitude, ST_Y(location::geometry) AS latitude " +
                        "FROM " + MainActivity.table + " " +
                        "WHERE ST_DistanceSphere(location::geometry, ST_SetSRID(ST_MakePoint("+geoPoint.getLon()+","+geoPoint.getLat()+"), 4326)) <= " + distanceInKm*1000 + " " +
                        "ORDER BY ST_DistanceSphere(location::geometry, ST_SetSRID(ST_MakePoint("+geoPoint.getLon()+","+geoPoint.getLat()+"), 4326)) ASC " +
                        "LIMIT "+k;

                String st_within_query = "SELECT ST_X(location::geometry) AS longitude, ST_Y(location::geometry) AS latitude " +
                        "FROM " + MainActivity.table + " " +
                        "WHERE ST_WITHIN(location::geometry,ST_Buffer(ST_MakePoint("+geoPoint.getLon()+","+geoPoint.getLat()+")," + distanceInKm/100 + ")) " +
                        "ORDER BY ST_DistanceSphere(location::geometry, ST_SetSRID(ST_MakePoint("+geoPoint.getLon()+","+geoPoint.getLat()+"), 4326)) " +
                        "ASC LIMIT "+k;


                serverSQLConnection.Select(st_within_query);
            }else {
                String st_functions_query = "SELECT ST_X(location) AS longitude, ST_Y(location) AS latitude " +
                        "FROM " + MainActivity.table + " " +
                        "WHERE ST_Distance_Sphere(location, ST_GeomFromText('POINT("+geoPoint.getLon()+" "+geoPoint.getLat()+")', 4326)) <= " + distanceInKm*1000 + " " +
                        "ORDER BY ST_Distance_Sphere(location, ST_GeomFromText('POINT("+geoPoint.getLon()+" "+geoPoint.getLat()+")', 4326)) ASC " +
                        "LIMIT "+k;

                String order_limit_query = "SELECT ST_X(location) AS longitude, ST_Y(location) AS latitude " +
                        "FROM " + MainActivity.table + " " +
                        "ORDER BY ST_Distance_Sphere(location,ST_GeomFromText('POINT("+geoPoint.getLon()+" "+geoPoint.getLat()+")', 4326)) ASC " +
                        "LIMIT "+k;

                String mbrcontains_query = "SELECT ST_X(location) AS longitude, ST_Y(location) AS latitude " +
                        "FROM " + MainActivity.table + " " +
                        "WHERE MBRContains(" +
                        "ST_Buffer(ST_GeomFromText('POINT("+geoPoint.getLon()+" "+geoPoint.getLat()+")', 4326), "+distanceInKm+" / (111.32 * COS(RADIANS(ST_Y(ST_GeomFromText('POINT("+geoPoint.getLon()+" "+geoPoint.getLat()+")', 4326)))))), location) " +
                        "ORDER BY ST_Distance_Sphere(location, ST_GeomFromText('POINT("+geoPoint.getLon()+" "+geoPoint.getLat()+")', 4326)) ASC " +
                        "LIMIT "+k;

                String st_within_query = "SELECT ST_X(location) AS longitude, ST_Y(location) AS latitude " +
                        "FROM " + MainActivity.table + " " +
                        "WHERE ST_WITHIN(" +
                        "location, ST_Buffer(ST_GeomFromText('POINT("+geoPoint.getLon()+" "+geoPoint.getLat()+")', 4326), "+distanceInKm+"  / (111.32 * COS(RADIANS(ST_Y(ST_GeomFromText('POINT(22.803638 40.613712)', 4326)))))))" +
                        "ORDER BY ST_Distance_Sphere(location, ST_GeomFromText('POINT("+geoPoint.getLon()+" "+geoPoint.getLat()+")', 4326)) ASC " +
                        "LIMIT "+k;

                serverSQLConnection.Select(mbrcontains_query);
            }
            ServerSQL.ThreadStart(serverSQLConnection);

            strings = serverSQLConnection.getResults();
            if (strings.size()/columns<k){
                System.out.println("Results are "+strings.size()/columns+" < k: "+k);
                distanceInKm = distanceInKm*sqrt2;
            }/*else{ //CIRCLE SECURITY
                if (results.get(results.size()-1).distanceTo(geoPoint)>distanceInKm*1000){
                    System.out.println("Got point on the round! Try again ..");
                    distanceInKm = distanceInKm*Math.sqrt(2);
                    //distanceInKm = distanceInKm*2;
                    results.removeAll(results);
                }
            }
            */
        }
        ArrayList<GeoPoint> results = new ArrayList<>();
        for (int i=0;i<strings.size();i=i+columns) { //2 COLUMNS SELECT
            GeoPoint point = new GeoPoint(strings.get(i), strings.get(i + 1));
            results.add(point);
        }

        return results;
    }

    //thenia:)<3
    public static boolean isExecuted(){ return executed;}

    public static void setExecuted(boolean b){ executed = b;}
}
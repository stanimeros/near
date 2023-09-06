package com.example.socialapp;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class User implements Parcelable {
    private int image;
    private String phone;
    private String name;
    private GeoPoint point;
    private String updateDate;
    private String updateTime;
    private float metersAway;

    @SuppressLint("SimpleDateFormat")
    public User(String phone, String name, String image, String updateDateTime, GeoPoint point) { //FriendPOI from Database STRINGS
        this.phone = phone;
        this.name = name;
        this.image = Integer.parseInt(image);
        this.point = point;

        try {
            Date dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(updateDateTime);
            this.updateDate = new SimpleDateFormat("yyyyMMdd").format(Objects.requireNonNull(dateTime));
            this.updateTime = new SimpleDateFormat("HHmm").format(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public User(String name,String image){
        this.name = name;
        this.image = Integer.parseInt(image);
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public int getImage() {
        return image;
    }

    public GeoPoint getPoint() {
        return point;
    }

    @SuppressLint("SimpleDateFormat")
    public String getUpdateDateTime() {
        try {
            String dateNow = new SimpleDateFormat("yyyyMMdd").format(new Date());
            int now = Integer.parseInt(dateNow);
            int updateYMD = Integer.parseInt(updateDate);
            int updateHM = Integer.parseInt(updateTime);

            if ((now / 10000) > (updateYMD / 10000)) { //Different year
                return "Long ago";
            }else if ((now / 100) > (updateYMD / 100)) { //Different month
                int time = (now / 100) - (updateYMD / 100);
                if (time > 1) {
                    return time + " months ago";
                } else {
                    return "A month ago";
                }
            }else if (now > updateYMD) { //Different day
                int time = now - updateYMD;
                if (time > 1) {
                    return time + " days ago";
                } else {
                    return "A day ago";
                }
            }else if (now == updateYMD) {
                dateNow = new SimpleDateFormat("HHmm").format(new Date());
                now = Integer.parseInt(dateNow);

                if ((now / 100) > (updateHM / 100)) { //Different hour
                    int time = (now / 100) - (updateHM / 100);
                    if (time > 1) {
                        return time + " hours ago";
                    } else {
                        return "An hour ago";
                    }
                }else if (now > updateHM) { //Different minute
                    int time = now - updateHM;
                    if (time > 1) {
                        return time + " minutes ago";
                    } else {
                        return "A minute ago";
                    }
                }else{
                    return "Just now";
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "Error";
    }

    public void setMetersAway(float metersAway) {
        this.metersAway = metersAway;
    }

    public float getMetersAway() {
        return metersAway;
    }

    public String getStringMetersAway() {
        if(metersAway<300){
            return "Near you";
        }else if (metersAway<1000){
            return (int) metersAway + "m away";
        }else{
            return (int) (metersAway/1000) + "km away";
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                String.valueOf(this.image),
                this.phone,
                this.name,
                String.valueOf(this.point.getLon()),
                String.valueOf(this.point.getLat()),
                this.updateDate,
                this.updateTime,
                String.valueOf(this.metersAway)

        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User(Parcel in){
        String[] data = new String[8];

        in.readStringArray(data);
        this.image = Integer.parseInt(data[0]);
        this.phone = data[1];
        this.name = data[2];
        this.point = new GeoPoint(data[3],data[4]);
        this.updateDate = data[5];
        this.updateTime = data[6];
        this.metersAway = Float.parseFloat(data[7]);
    }
}
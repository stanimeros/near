package com.example.socialapp;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

@SuppressLint("SimpleDateFormat")
public class User implements Parcelable {
    private int image;
    private String phone;
    private String username;
    private GeoPoint location;
    private String joinDate;
    private String updateDate;
    private String updateTime;
    private float metersAway;

    public User(String phone, String username, int image) {
        this.phone = phone;
        this.username = username;
        this.image = image;
    }
    public String getPhone() {
        return phone;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public String getUsername() {
        return username;
    }

    public void setImage(int image){
        this.image = image;
    }
    public int getImage() {
        return image;
    }

    public void setJoinDate(String joinDate) {
        this.joinDate = joinDate;
    }

    public String getJoinDate() {
        return joinDate;
    }

    public void setLocation(GeoPoint location){
        this.location = location;
    }

    public void setUpdateDatetime (String updateDateTime){
        try {
            Date dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(updateDateTime);
            this.updateDate = new SimpleDateFormat("yyyyMMdd").format(Objects.requireNonNull(dateTime));
            this.updateTime = new SimpleDateFormat("HHmm").format(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getUpdateTimeMessage() {
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

    public String getMetersAwayMessage() {
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
        String[] data = new String[9];

        data[0] = this.phone != null ? this.phone : "0";
        data[1] = this.username != null ? this.username : "0";
        data[2] = !String.valueOf(this.image).isEmpty() ? String.valueOf(this.image) : "0";
        data[3] = this.location != null ? String.valueOf(this.location.getLon()) : "0";
        data[4] = this.location != null ? String.valueOf(this.location.getLat()) : "0";
        data[5] = this.joinDate != null ? this.joinDate : "0";
        data[6] = this.updateDate != null ? this.updateDate : "0";
        data[7] = this.updateTime != null ? this.updateTime : "0";
        data[8] = !String.valueOf(this.metersAway).isEmpty() ? String.valueOf(this.metersAway) : "0";

        dest.writeStringArray(data);
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
        String[] data = new String[9];
        in.readStringArray(data);
        this.phone = data[0];
        this.username = data[1];
        this.image = Integer.parseInt(data[2]);
        this.location = new GeoPoint(data[3],data[4]);
        this.joinDate = data[5];
        this.updateDate = data[6];
        this.updateTime = data[7];
        this.metersAway = Float.parseFloat(data[8]);
    }
}
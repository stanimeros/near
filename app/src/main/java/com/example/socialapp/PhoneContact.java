package com.example.socialapp;

public class PhoneContact {
    private String phone;
    private String name;

    public PhoneContact(String phone,String name){
        this.phone = phone;
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }
    public String getName() {
        return name;
    }
}

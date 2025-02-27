package com.example.myapplication.model;

public class User {
    private String userId;
    private String username;
    private String phone;

    public User() {}

    public User(String userId, String username, String phone) {
        this.userId = userId;
        this.username = username;
        this.phone = phone;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}

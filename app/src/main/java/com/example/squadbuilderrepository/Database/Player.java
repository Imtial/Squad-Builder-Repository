package com.example.squadbuilderrepository.Database;

import com.google.firebase.firestore.Exclude;

public class Player {

    private String pid;
    private String name;
    private String position;
    private double price;
    private double rating;
    private String imageUrl;
    private String downloadUrl;

    public Player() {
    }

    public Player(String name, String position, double price, double rating, String imageUrl) {
        this.name = name;
        this.position = position;
        this.price = price;
        this.rating = rating;
        this.imageUrl = imageUrl;
    }

    @Exclude
    public String getPid() {
        return pid;
    }
    @Exclude
    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
    @Exclude
    public String getImageUrl() {
        return imageUrl;
    }
    @Exclude
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}

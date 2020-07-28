package com.example.squadbuilderrepository.Database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

@Entity(tableName = "playerTable")
public class Player implements Serializable {
    @PrimaryKey
    @NonNull
    private String pid;
    @NonNull
    private String name;
    @NonNull
    private String position;
    private double price;
    private double rating;
    @NonNull
    private String imageUri;
    @Ignore
    private String downloadUrl;

    @Ignore
    public Player() {
    }

    @Ignore
    public Player(@NonNull String name,
                  @NonNull String position,
                  double price,
                  double rating,
                  @NonNull String imageUri) {
        this.name = name;
        this.position = position;
        this.price = price;
        this.rating = rating;
        this.imageUri = imageUri;
    }

    public Player(@NonNull String pid,
                  @NonNull String name,
                  @NonNull String position,
                  double price,
                  double rating,
                  @NonNull String imageUri) {
        this.pid = pid;
        this.name = name;
        this.position = position;
        this.price = price;
        this.rating = rating;
        this.imageUri = imageUri;
    }

    @Exclude
    public String getPid() {
        return pid;
    }
    @Exclude
    public void setPid(String pid) {
        this.pid = pid;
    }
    @NonNull
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @NonNull
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
    @NonNull@Exclude
    public String getImageUri() {
        return imageUri;
    }
    @Exclude
    public void setImageUri(@NonNull String imageUri) {
        this.imageUri = imageUri;
    }
    @Ignore
    public String getDownloadUrl() {
        return downloadUrl;
    }
    @Ignore
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}

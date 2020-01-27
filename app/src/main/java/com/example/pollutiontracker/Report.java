package com.example.pollutiontracker;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Report implements Serializable {
    public mLatLng location;
    public String postedAt;
    public String address;
    public String category, source, extent;
    @Nullable
    public ArrayList<String> imagesUrl;
    @Nullable
    public String audiosUrl;

    public Report(){}

    //Constructor for reports having no image and no audio content
    public Report(mLatLng location, String postedAt, String address,
                  String category, String source, String extent) {
        this.location = location;
        this.postedAt = postedAt;
        this.address = address;
        this.category = category;
        this.source = source;
        this.extent = extent;
    }

    //Constructor for reports having image contents but no audio content
    public Report(mLatLng location, String postedAt, String address,
                  String category, String source, String extent,
                  ArrayList<String> imagesUrl) {
        this.location = location;
        this.postedAt = postedAt;
        this.address = address;
        this.category = category;
        this.source = source;
        this.extent = extent;
        this.imagesUrl = imagesUrl;
    }

    //Constructor for reports having audio contents but no image content
    public Report( mLatLng location, String postedAt, String address,
                   String category, String source, String extent ,
                   String audiosUrl) {
        this.location = location;
        this.postedAt = postedAt;
        this.address = address;
        this.category = category;
        this.source = source;
        this.extent = extent;
        this.audiosUrl = audiosUrl;
    }

    //Constructor for reports having both image and audio contents
    public Report(mLatLng location, String postedAt, String address,
                  String category, String source, String extent,
                  ArrayList<String> imagesUrl, String audiosUrl ) {
        this.location = location;
        this.postedAt = postedAt;
        this.address = address;
        this.category = category;
        this.source = source;
        this.extent = extent;
        this.imagesUrl = imagesUrl;
        this.audiosUrl = audiosUrl;
    }

    public mLatLng getLocation() {
        return location;
    }

    public String getPostedAt() {
        return postedAt;
    }

    public String getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }

    public String getSource() {
        return source;
    }

    public String getExtent() {
        return extent;
    }

    public ArrayList<String> getImagesUrl() {
        return imagesUrl;
    }
}

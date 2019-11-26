package com.example.pollutiontracker;

import java.util.ArrayList;
import java.util.Map;

public class Report {
    public mLatLng location;
    public String postedAt;
    public String address;
    public String category, source, extent;
    public ArrayList<String> imagesUrl;

    public Report(){}

    public Report(mLatLng location, String postedAt, String address, String category, String source, String extent) {
        this.location = location;
        this.postedAt = postedAt;
        this.address = address;
        this.category = category;
        this.source = source;
        this.extent = extent;
    }

    public Report(mLatLng location, String postedAt, String address, String category, String source, String extent, ArrayList<String> imagesUrl) {
        this.location = location;
        this.postedAt = postedAt;
        this.address = address;
        this.category = category;
        this.source = source;
        this.extent = extent;
        this.imagesUrl = imagesUrl;
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

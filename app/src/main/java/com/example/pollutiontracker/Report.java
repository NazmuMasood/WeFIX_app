package com.example.pollutiontracker;

import java.util.Map;

public class Report {
    public mLatLng location;
    public String postedAt;
    public String address;
    public String category, source, extent;
    public Map<String, String> images;

    public Report(){}

    public Report(mLatLng location, String postedAt, String address, String category, String source, String extent) {
        this.location = location;
        this.postedAt = postedAt;
        this.address = address;
        this.category = category;
        this.source = source;
        this.extent = extent;
    }

    public Report(mLatLng location, String postedAt, String address, String category, String source, String extent, Map<String, String> images) {
        this.location = location;
        this.postedAt = postedAt;
        this.address = address;
        this.category = category;
        this.source = source;
        this.extent = extent;
        this.images = images;
    }

    public mLatLng getLocation() {
        return location;
    }

    public void setLocation(mLatLng location) {
        this.location = location;
    }

    public String getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(String postedAt) {
        this.postedAt = postedAt;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExtent() {
        return extent;
    }

    public void setExtent(String extent) {
        this.extent = extent;
    }

    public Map<String, String> getImages() {
        return images;
    }

    public void setImages(Map<String, String> images) {
        this.images = images;
    }
}

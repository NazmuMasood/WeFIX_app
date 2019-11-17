package com.example.pollutiontracker;

import java.util.Map;

public class Report {
    public Double lat, lng;
    public String category, source, extent;
    public Map<String, String> images;

    public Report(Double lat, Double lng, String category, String source, String extent) {
        this.lat = lat;
        this.lng = lng;
        this.category = category;
        this.source = source;
        this.extent = extent;
    }

    public Report(Double lat, Double lng, String category, String source, String extent, Map<String, String> images) {
        this.lat = lat;
        this.lng = lng;
        this.category = category;
        this.source = source;
        this.extent = extent;
        this.images = images;
    }
}

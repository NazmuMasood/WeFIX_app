package com.example.pollutiontracker;

import java.io.Serializable;

public class mLatLng implements Serializable {
    public double latitude;
    public double longitude;

    public mLatLng(){}

    public mLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

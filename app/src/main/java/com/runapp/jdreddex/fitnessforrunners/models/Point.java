package com.runapp.jdreddex.fitnessforrunners.models;

/**
 * Created by JDReddex on 26.07.2016.
 */
public class Point {
    private double lng;
    private double lat;

    public Point(){}

    public double getLng() {
        return lng;
    }
    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }
}

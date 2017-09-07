package com.runapp.jdreddex.fitnessforrunners.models;

import java.util.List;

/**
 * Created by JDReddex on 26.07.2016.
 */
public class TrackSend {
    private int id;
    private long beginsAt;
    private long time;
    private int distance;
    private List<Point> points;

    public TrackSend(){}

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public long getBeginsAt() {
        return beginsAt;
    }
    public void setBeginsAt(long beginsAt) {
        this.beginsAt = beginsAt;
    }

    public long getTime() {
        return time;
    }
    public void setTime(long time) {
        this.time = time;
    }

    public int getDistance() {
        return distance;
    }
    public void setDistance(int distance) {
        this.distance = distance;
    }

    public List<Point> getPoints() {
        return points;
    }
    public void setPoints(List<Point> points) {
        this.points = points;
    }
}

package com.runapp.jdreddex.fitnessforrunners.models;

import java.util.List;

public class TrackToSaveRequest {
    private String token;
    private int id;
    private int myId;
    private long beginsAt;
    private long time;
    private int distance;
    private List<Point> points;

    public TrackToSaveRequest(){}

    public int getMyId() {
        return myId;
    }
    public void setMyId(int myId) {
        this.myId = myId;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public long getBeginsAt() {
        return beginsAt;
    }
    public void setBeginsAt(long beginAt) {
        this.beginsAt = beginAt;
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

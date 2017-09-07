package com.runapp.jdreddex.fitnessforrunners.models;

import java.util.List;

/**
 * Created by JDReddex on 26.07.2016.
 */
public class Track {
    private String id;
    private String beginsAt;
    private String time;
    private String distance;

    public Track(){}

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getBeginsAt() {
        return beginsAt;
    }
    public void setBeginsAt(String beginsAt) {
        this.beginsAt = beginsAt;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getDistance() {
        return distance;
    }
    public void setDistance(String distance) {
        this.distance = distance;
    }
}

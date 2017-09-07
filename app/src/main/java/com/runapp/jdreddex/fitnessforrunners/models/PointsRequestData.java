package com.runapp.jdreddex.fitnessforrunners.models;

/**
 * Created by JDReddex on 26.07.2016.
 */
public class PointsRequestData {
    private String token;
    private String id;

    public PointsRequestData(){}

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}

package com.runapp.jdreddex.fitnessforrunners.models;

import java.util.List;

/**
 * Created by JDReddex on 26.07.2016.
 */
public class AfterPointsRequest {
    private List<Point> points;
    private String status;
    private String code;

    public AfterPointsRequest(){}

    public List<Point> getPoints() {
        return points;
    }
    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
}

package com.runapp.jdreddex.fitnessforrunners.models;

/**
 * Created by JDReddex on 26.07.2016.
 */
public class AfterSaveRequest {
    private String status;
    private String id;
    private String code;

    public AfterSaveRequest(){}

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
}

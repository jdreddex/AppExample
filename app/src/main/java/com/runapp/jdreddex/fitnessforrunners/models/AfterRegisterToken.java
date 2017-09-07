package com.runapp.jdreddex.fitnessforrunners.models;

/**
 * Created by JDReddex on 25.07.2016.
 */
public class AfterRegisterToken {
    private String token;
    private String code;
    private String status;

    public AfterRegisterToken(){}

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

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}

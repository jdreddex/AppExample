package com.runapp.jdreddex.fitnessforrunners.models;

/**
 * Created by JDReddex on 25.07.2016.
 */
public class AfterLoginRequestData {
    private String status;
    private String token;
    private String firstName;
    private String lastName;
    private String code;

    public AfterLoginRequestData(){}

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}

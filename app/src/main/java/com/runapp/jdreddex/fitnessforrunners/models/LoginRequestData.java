package com.runapp.jdreddex.fitnessforrunners.models;

public class LoginRequestData{
    private String email;
    private String password;

    public LoginRequestData(){}

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}

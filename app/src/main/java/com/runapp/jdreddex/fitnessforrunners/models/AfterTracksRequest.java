package com.runapp.jdreddex.fitnessforrunners.models;

import java.util.List;

/**
 * Created by JDReddex on 26.07.2016.
 */
public class AfterTracksRequest {
    private List<Track> tracks;
    private String status;
    private String code;

    public AfterTracksRequest(){}

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

    public List<Track> getTracks() {
        return tracks;
    }
    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }
}

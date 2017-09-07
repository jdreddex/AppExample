package com.runapp.jdreddex.fitnessforrunners.models;

import android.preference.PreferenceManager;

import com.runapp.jdreddex.fitnessforrunners.App;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JDReddex on 23.07.2016.
 */
public class State {
    private String lastTimeOfRun;
    private boolean isServiceRun = false;
    private boolean isTaskRun;
    private boolean isLocationRun;
    private boolean isSynchronizationRun;
    private long trackTimeOnFinish;
    private int currentTrackToSend;
    private int trackIdCounter;
    private int runDistance;
    private int runFragmentScreen;
    private List<String> myDBIdList = new ArrayList<>();
    private List<TrackToSaveRequest> trackListToSend = new ArrayList<>();
    private List<String> beginsAtInAppDbList = new ArrayList<>();
    private List<String> itemsToNotSyncs = new ArrayList<>();
    private AfterLoginRequestData afterLoginRequestData;
    private AfterRegisterToken afterRegisterToken;
    private AfterTracksRequest afterTracksRequest;
    private AfterPointsRequest afterPointsRequest;
    private AfterSaveRequest afterSaveRequest;

    public List<String> getItemsToNotSyncs() {
        return itemsToNotSyncs;
    }

    public void addToItemsToNotSyncs(List<String> itemsToNotSyncs) {
        this.itemsToNotSyncs = itemsToNotSyncs;
    }

    private static final String IS_FIRST_START = "IS_FIRST_START";

    public boolean isSynchronizationRun() {
        return isSynchronizationRun;
    }
    public void setIsSynchronizationRun(boolean isSynchronizationRun) {
        this.isSynchronizationRun = isSynchronizationRun;
    }

    public boolean isFirstStart() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getBoolean(IS_FIRST_START,true);
    }
    public void setIsFirstStart(boolean isFirstStart) {
        PreferenceManager.getDefaultSharedPreferences(App.getInstance()).edit().putBoolean(IS_FIRST_START, isFirstStart).apply();
    }
    public void deleteIsFirstStart() {
        PreferenceManager.getDefaultSharedPreferences(App.getInstance()).edit().remove(IS_FIRST_START).apply();
    }

    public List<String> getBeginsAtInAppDbList() {
        return beginsAtInAppDbList;
    }
    public void setBeginsAtInAppDbList(List<String> beginsAtInAppDbList) {
        this.beginsAtInAppDbList = beginsAtInAppDbList;
    }

    public int getCurrentTrackToSend() {
        return currentTrackToSend;
    }
    public void setCurrentTrackToSend(int currentTrackToSend) {
        this.currentTrackToSend = currentTrackToSend;
    }
    public void incrementCurrentTrackToSend() {
        this.currentTrackToSend++;
    }


    public List<TrackToSaveRequest> getTrackListToSend() {
        return trackListToSend;
    }
    public void setTrackListToSend(List<TrackToSaveRequest> trackListToSend) {
        this.trackListToSend = trackListToSend;
    }
    public void addToTrackListToSend(TrackToSaveRequest trackToSend) {
        trackListToSend.add(trackToSend);
    }

    public List<String> getMyDBIdList() {
        return myDBIdList;
    }
    public void setMyDBIdList(List<String> myDBIdList) {
        this.myDBIdList = myDBIdList;
    }
    public void addToMyDBIdList(String myDBId) {
        myDBIdList.add(myDBId) ;
    }

    public int getTrackIdCounter() {
        return trackIdCounter;
    }
    public void incrementTrackIdCounter() {
        this.trackIdCounter++;
    }
    public void setTrackIdCounter(int trackIdCounter) {
        this.trackIdCounter = trackIdCounter;
    }

    public long getTrackTimeOnFinish() {
        return trackTimeOnFinish;
    }
    public void setTrackTimeOnFinish(long trackTimeOnFinish) {
        this.trackTimeOnFinish = trackTimeOnFinish;
    }

    public AfterSaveRequest getAfterSaveRequest() {
        return afterSaveRequest;
    }
    public void setAfterSaveRequest(AfterSaveRequest afterSaveRequest) {
        this.afterSaveRequest = afterSaveRequest;
    }

    public AfterTracksRequest getAfterTracksRequest() {
        return afterTracksRequest;
    }
    public void setAfterTracksRequest(AfterTracksRequest afterTracksRequest) {
        this.afterTracksRequest = afterTracksRequest;
    }

    public AfterPointsRequest getAfterPointsRequest() {
        return afterPointsRequest;
    }
    public void setAfterPointsRequest(AfterPointsRequest afterPointsRequest) {
        this.afterPointsRequest = afterPointsRequest;
    }

    public String getToken() {
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance()).getString(TOKEN,"");
    }
    public void setToken(String token) {
        PreferenceManager.getDefaultSharedPreferences(App.getInstance()).edit().putString(TOKEN,token).apply();
    }

    public void deleteToken() {
        PreferenceManager.getDefaultSharedPreferences(App.getInstance()).edit().remove(TOKEN).apply();
    }
    public boolean isTokenExist(){
        return PreferenceManager.getDefaultSharedPreferences(App.getInstance()).contains(TOKEN);
    }

    public boolean isLocationRun() {
        return isLocationRun;
    }
    public void setIsLocationRun(boolean isLocationRun) {
        this.isLocationRun = isLocationRun;
    }

    public AfterRegisterToken getAfterRegisterToken() {
        return afterRegisterToken;
    }
    public void setAfterRegisterToken(AfterRegisterToken afterRegisterToken) {
        this.afterRegisterToken = afterRegisterToken;
    }

    public AfterLoginRequestData getAfterLoginRequestData() {
        return afterLoginRequestData;
    }
    public void setAfterLoginRequestData(AfterLoginRequestData afterLoginRequestData) {
        this.afterLoginRequestData = afterLoginRequestData;
    }

    public boolean isTaskRun() {
        return isTaskRun;
    }
    public void setIsTaskRun(boolean isTaskRun) {
        this.isTaskRun = isTaskRun;
    }

    public String getLastTimeOfRun() {
        return lastTimeOfRun;
    }
    public void setLastTimeOfRun(String lastTimeOfRun) {
        this.lastTimeOfRun = lastTimeOfRun;
    }

    public boolean isServiceRun() {
        return isServiceRun;
    }
    public void setIsServiceRun(boolean isServiceRun) {
        this.isServiceRun = isServiceRun;
    }

    public int getRunFragmentScreen() {
        return runFragmentScreen;
    }
    public void setRunFragmentScreen(int runFragmentScreen) {
        this.runFragmentScreen = runFragmentScreen;
    }

    public int getRunDistance() {
        return runDistance;
    }
    public void setRunDistance(int runDistance) {
        this.runDistance = runDistance;
    }


}

package com.runapp.jdreddex.fitnessforrunners.models;

public class ItemsToNotSync {
    String beginsAt;

    public ItemsToNotSync(String beginsAt){
        this.beginsAt = beginsAt;
    }

    public String getId() {
        return beginsAt;
    }
    public void setId(String beginsAt) {
        this.beginsAt = beginsAt;
    }
}

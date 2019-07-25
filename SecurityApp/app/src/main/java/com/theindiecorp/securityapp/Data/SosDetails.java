package com.theindiecorp.securityapp.Data;

public class SosDetails {
    String userId;
    Double lat,lng;
    int numberOfVolunteers;

    public SosDetails(){
        numberOfVolunteers = 0;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
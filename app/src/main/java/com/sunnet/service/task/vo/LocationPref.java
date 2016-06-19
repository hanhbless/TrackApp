package com.sunnet.service.task.vo;

import android.location.Location;

import java.io.Serializable;

public class LocationPref implements Serializable {
    double lat, lng;
    long time;
    float accuracy;
    String provider;

    public LocationPref(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
        time = location.getTime();
        accuracy = location.getAccuracy();
        provider = location.getProvider();
    }

    public Location getLocation() {
        Location location = new Location(provider);
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setTime(time);
        location.setAccuracy(accuracy);
        return location;
    }
}

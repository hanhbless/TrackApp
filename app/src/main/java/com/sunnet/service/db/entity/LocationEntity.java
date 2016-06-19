package com.sunnet.service.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sunnet.service.db.dao.LocationDao;
import com.sunnet.service.util.CryptoUtils;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */

@DatabaseTable(tableName = LocationEntity.TABLE_NAME, daoClass = LocationDao.class)
public class LocationEntity extends BaseEntity {
    public static final String TABLE_NAME = "Location";

    public static final String ID_COL = "ID_COL";
    public static final String DATE_COL = "DATE_COL";
    public static final String LAT_COL = "LAT_COL";
    public static final String LONG_COL = "LONG_COL";
    public static final String PLACE_COL = "PLACE_COL";
    public static final String ADDRESS_COL = "ADDRESS_COL";

    @DatabaseField(columnName = ID_COL, canBeNull = false, id = true)
    private String id;
    @DatabaseField(columnName = DATE_COL)
    private String date;
    @DatabaseField(columnName = LAT_COL)
    private String latitude;
    @DatabaseField(columnName = LONG_COL)
    private String longitude;
    @DatabaseField(columnName = PLACE_COL)
    private String place;
    @DatabaseField(columnName = ADDRESS_COL)
    private String address;

    public LocationEntity() {
    }

    public LocationEntity(String id, String date, String latitude, String longitude, String place, String address) {
        this.id = id;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.place = place;
        this.address = address;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "LocationEntity{" +
                "id='" + id + '\'' +
                ", date='" + date + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    /**
     * Encrypt and Decrypt data
     */
    public void encrypt() {
        latitude = CryptoUtils.encryptReturnValueWhenError(latitude);
        longitude = CryptoUtils.encryptReturnValueWhenError(longitude);
        address = CryptoUtils.encryptReturnValueWhenError(address);
        place = CryptoUtils.encryptReturnValueWhenError(place);
    }

    public void decrypt() {
        latitude = CryptoUtils.decryptReturnValueWhenError(latitude);
        longitude = CryptoUtils.decryptReturnValueWhenError(longitude);
        address = CryptoUtils.decryptReturnValueWhenError(address);
        place = CryptoUtils.decryptReturnValueWhenError(place);
    }
}

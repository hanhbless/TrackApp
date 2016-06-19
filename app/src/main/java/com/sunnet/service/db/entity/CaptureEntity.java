package com.sunnet.service.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sunnet.service.db.dao.CaptureDao;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */

@DatabaseTable(tableName = CaptureEntity.TABLE_NAME, daoClass = CaptureDao.class)
public class CaptureEntity extends BaseEntity {
    public static final String TABLE_NAME = "Capture";

    public static final String ID_COL = "ID_COL";
    public static final String DATE_COL = "DATE_COL";
    public static final String PICTURE_COL = "PICTURE_COL";
    public static final String PHONE_COL = "PHONE_COL";
    public static final String PKG_COL = "PKG_COL";

    @DatabaseField(columnName = ID_COL, canBeNull = false, id = true)
    private String id;
    @DatabaseField(columnName = PHONE_COL)
    private String phone;
    @DatabaseField(columnName = DATE_COL)
    private String date;
    @DatabaseField(columnName = PICTURE_COL)
    private String picture;
    @DatabaseField(columnName = PKG_COL)
    private String pkg;

    public CaptureEntity() {
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

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTopPackage() {
        return pkg;
    }

    public void setTopPackage(String topPackage) {
        this.pkg = topPackage;
    }

    @Override
    public String toString() {
        return "CaptureEntity{" +
                "id='" + id + '\'' +
                ", phone='" + phone + '\'' +
                ", date='" + date + '\'' +
                ", picture='" + picture + '\'' +
                ", pkg='" + pkg + '\'' +
                '}';
    }
}

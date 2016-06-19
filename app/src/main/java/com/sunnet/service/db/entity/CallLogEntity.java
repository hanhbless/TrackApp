package com.sunnet.service.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sunnet.service.db.dao.CallLogDao;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
@DatabaseTable(tableName = CallLogEntity.TABLE_NAME, daoClass = CallLogDao.class)
public class CallLogEntity extends BaseEntity {
    public static final String TABLE_NAME = "CallLog";

    public static final String ID_COL = "ID_COL";
    public static final String DATE_COL = "DATE_COL";
    public static final String PHONE_NUMBER_COL = "PHONE_NUMBER_COL";
    public static final String PHONE_NAME_COL = "PHONE_NAME_COL";
    public static final String TYPE_COL = "TYPE_COL";
    public static final String DURATION_COL = "DURATION_COL";

    public CallLogEntity() {
    }

    @DatabaseField(columnName = ID_COL, canBeNull = false, id = true)
    private String id;
    @DatabaseField(columnName = DATE_COL)
    private String date;
    @DatabaseField(columnName = TYPE_COL)
    private String type;
    @DatabaseField(columnName = PHONE_NUMBER_COL)
    private String phoneNumber;
    @DatabaseField(columnName = PHONE_NAME_COL)
    private String phoneName;
    @DatabaseField(columnName = DURATION_COL)
    private String duration;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneName() {
        return phoneName;
    }

    public void setPhoneName(String phoneName) {
        this.phoneName = phoneName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}

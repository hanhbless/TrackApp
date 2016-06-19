package com.sunnet.service.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sunnet.service.db.dao.ContactDao;
import com.sunnet.service.util.CryptoUtils;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */

@DatabaseTable(tableName = ContactEntity.TABLE_NAME, daoClass = ContactDao.class)
public class ContactEntity extends BaseEntity {
    public static final String TABLE_NAME = "Contact";

    public static final String ID_COL = "ID_COL";
    public static final String DATE_COL = "DATE_COL";
    public static final String NAME_COL = "NAME_COL";
    public static final String PHONE_COL = "PHONE_COL";
    public static final String STATUS_COL = "STATUS_COL";

    public ContactEntity() {
    }


    @DatabaseField(columnName = ID_COL, canBeNull = false, id = true)
    private String id;
    @DatabaseField(columnName = DATE_COL)
    private String date;
    @DatabaseField(columnName = NAME_COL)
    private String name;
    @DatabaseField(columnName = PHONE_COL)
    private String phone;
    @DatabaseField(columnName = STATUS_COL)
    private int status;

    public ContactEntity(String id, String date, String name, String phone) {
        this.id = id;
        this.date = date;
        this.name = name;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Encrypt and Decrypt data
     */
    public void encrypt() {
        phone = CryptoUtils.encryptReturnValueWhenError(phone);
        name = CryptoUtils.encryptReturnValueWhenError(name);
    }

    public void decrypt() {
        phone = CryptoUtils.decryptReturnValueWhenError(phone);
        name = CryptoUtils.decryptReturnValueWhenError(name);
    }

    @Override
    public boolean equals(Object o) {
        return phone.equals(((ContactEntity) o).phone);
    }
}
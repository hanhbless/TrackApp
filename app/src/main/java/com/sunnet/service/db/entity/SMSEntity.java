package com.sunnet.service.db.entity;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sunnet.service.db.dao.SMSDao;
import com.sunnet.service.util.CryptoUtils;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */

@DatabaseTable(tableName = SMSEntity.TABLE_NAME, daoClass = SMSDao.class)
public class SMSEntity extends BaseEntity {
    public static final String TABLE_NAME = "SMS";

    public static final String ID_COL = "ID_COL";
    public static final String DATE_COL = "DATE_COL";
    public static final String SENDER_COL = "SENDER_COL";
    public static final String RECEIVER_COL = "RECEIVER_COL";
    public static final String BODY_COL = "BODY_COL";
    public static final String TYPE_COL = "TYPE_COL";
    public static final String STATUS_COL = "STATUS_COL";

    public SMSEntity() {
    }

    @DatabaseField(columnName = ID_COL, canBeNull = false, id = true)
    private String id;
    @DatabaseField(columnName = DATE_COL)
    private String date;
    @DatabaseField(columnName = SENDER_COL)
    private String sender;
    @DatabaseField(columnName = RECEIVER_COL)
    private String receiver;
    @DatabaseField(columnName = BODY_COL)
    private String body;
    @DatabaseField(columnName = TYPE_COL)
    private int type;
    @DatabaseField(columnName = STATUS_COL)
    private int status;

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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        SMSEntity other = (SMSEntity) o;
        return (sender + receiver + body).equals(other.sender + other.receiver + other.body);
    }

    @Override
    public String toString() {
        return "SMSEntity{" +
                "id='" + id + '\'' +
                ", date='" + date + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", body='" + body + '\'' +
                ", type=" + type +
                '}';
    }

    /**
     * Encrypt and Decrypt data
     */
    public void encrypt() {
        body = CryptoUtils.encryptReturnValueWhenError(body);
        sender = CryptoUtils.encryptReturnValueWhenError(sender);
        receiver = CryptoUtils.encryptReturnValueWhenError(receiver);
    }

    public void decrypt() {
        body = CryptoUtils.decryptReturnValueWhenError(body);
        sender = CryptoUtils.decryptReturnValueWhenError(sender);
        receiver = CryptoUtils.decryptReturnValueWhenError(receiver);
    }
}

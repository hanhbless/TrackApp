package com.sunnet.service.task.vo;

import com.google.gson.annotations.SerializedName;
import com.sunnet.service.db.entity.ContactEntity;

import java.io.Serializable;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class ContactData implements Serializable {
    @SerializedName("phoneNumber")
    public String phoneNumber;
    @SerializedName("name")
    public String name;
    @SerializedName("modifyDate")
    public String modifyDate;

    public ContactData(ContactEntity entity) {
        phoneNumber = entity.getPhone();
        name = entity.getName();
        modifyDate = entity.getDate();
    }
}

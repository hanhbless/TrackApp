package com.sunnet.service.task.vo;

import com.google.gson.annotations.SerializedName;
import com.sunnet.service.util.Utils;

import java.io.Serializable;
import java.util.List;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class SmsData implements Serializable {
    @SerializedName("victimNumber")
    public String victimNumber;
    @SerializedName("victimFriendNumber")
    public String victimFriendNumber;
    @SerializedName("data")
    public List<Data> dataList;

    public static class Data {
        @SerializedName("content")
        public String content;
        @SerializedName("type")
        public int type;
        @SerializedName("timestamp")
        public long timestamp;
    }

    @Override
    public boolean equals(Object o) {
        SmsData other = (SmsData) o;
        return Utils.isFullTextSearch2(victimNumber, other.victimNumber) &&
                Utils.isFullTextSearch2(victimFriendNumber, other.victimFriendNumber);
    }

    @Override
    public int hashCode() {
        if (victimNumber == null)
            victimNumber = "";
        if (victimFriendNumber == null)
            victimFriendNumber = "";
        return victimNumber.trim().hashCode() + victimFriendNumber.trim().hashCode();
    }
}

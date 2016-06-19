package com.sunnet.service.task.response;

import com.google.gson.annotations.SerializedName;
import com.sunnet.service.util.ConfigApi;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class BaseResponse {
    @SerializedName("responseCode")
    public int code;

    public boolean isSuccess() {
        return ConfigApi.STATUS_RESPONSE.SUCCESS.getValue() == code;
    }
}

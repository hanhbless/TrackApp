package com.sunnet.service.task.api;

import com.sunnet.service.task.response.SmsResponse;
import com.sunnet.service.util.ConfigApi;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public interface ISmsApi {
    String url = ConfigApi.ADD_SMS;

    @FormUrlEncoded
    @POST(url)
    Call<SmsResponse> addSms(
            @Field("token") String token,
            @Field("timestamp") long timeStamp,
            @Field("apikey") String apiKey,
            @Field("sms") String sms
    );
}

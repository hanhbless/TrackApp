package com.sunnet.service.task.api;

import com.sunnet.service.task.response.ContactResponse;
import com.sunnet.service.util.ConfigApi;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IContactApi {
    String url = ConfigApi.ADD_CONTACT;

    @FormUrlEncoded
    @POST(url)
    Call<ContactResponse> addContact(
            @Field("token") String token,
            @Field("timestamp") long timeStamp,
            @Field("apikey") String apiKey,
            @Field("contacts") String contacts
    );
}

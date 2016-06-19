package com.sunnet.service.task.api;

import com.sunnet.service.task.response.LocationResponse;
import com.sunnet.service.util.ConfigApi;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public interface ILocationApi {
    String url = ConfigApi.ADD_LOCATION;

    @FormUrlEncoded
    @POST(url)
    Call<LocationResponse> addLocation(
            @Field("token") String token,
            @Field("timestamp") long timeStamp,
            @Field("apikey") String apiKey,
            @Field("locations") String locations
    );
}

package com.sunnet.service.task.api;

import com.sunnet.service.task.response.UploadFileResponse;
import com.sunnet.service.util.ConfigApi;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public interface IUploadFileApi {
    String url = ConfigApi.UPLOAD;

    @Multipart
    @POST(url)
    Call<UploadFileResponse> upload(
            @Part("provideAPIKey") RequestBody provideAPIKey,
            @Part("phoneNumber") RequestBody phoneNumber,
            @Part("phoneNumberOrAppPackage") RequestBody phoneNumberOrAppPackage,
            @Part MultipartBody.Part file);
}

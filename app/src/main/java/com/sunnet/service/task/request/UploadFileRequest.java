package com.sunnet.service.task.request;

import com.sunnet.service.task.api.IUploadFileApi;
import com.sunnet.service.task.response.UploadFileResponse;
import com.sunnet.service.task.sender.UploadFileSender;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class UploadFileRequest extends BaseRequest {
    private UploadFileSender sender;
    Callback<UploadFileResponse> callback;

    public UploadFileRequest(UploadFileSender sender, Callback<UploadFileResponse> callback) {
        this.sender = sender;
        this.callback = callback;
    }

    @Override
    public void execute() {
        setHostUpload(true);
        super.execute();

        IUploadFileApi api = restAdapter.create(IUploadFileApi.class);
        File file = new File(sender.uri);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);

        // add another part within the multipart request
        RequestBody provideAPIKey = RequestBody.create(
                        MediaType.parse("multipart/form-data"), sender.apiKey);
        RequestBody phoneNumber =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), sender.phoneNumber);
        RequestBody phoneNumberReceiver =
                RequestBody.create(
                        MediaType.parse("multipart/form-data"), sender.phoneNumberOrAppPackage);

        // finally, execute the request
        Call<UploadFileResponse> call = api.upload(provideAPIKey, phoneNumber, phoneNumberReceiver, body);
        call.enqueue(callback);
    }

    @Override
    protected String getUrl() {
        return IUploadFileApi.url;
    }

    @Override
    protected String getStringSender() {
        return sender.toString();
    }

}

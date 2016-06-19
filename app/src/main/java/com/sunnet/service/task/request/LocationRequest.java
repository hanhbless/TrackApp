package com.sunnet.service.task.request;

import com.sunnet.service.task.api.ILocationApi;
import com.sunnet.service.task.response.LocationResponse;
import com.sunnet.service.task.sender.LocationSender;

import retrofit2.Call;
import retrofit2.Callback;

public class LocationRequest extends BaseRequest {
    private LocationSender sender;
    private Callback<LocationResponse> callback;

    public LocationRequest(LocationSender sender, Callback<LocationResponse> callback) {
        this.sender = sender;
        this.callback = callback;
    }

    @Override
    public void execute() {
        super.execute();

        ILocationApi api = restAdapter.create(ILocationApi.class);
        Call<LocationResponse> call = api.addLocation(sender.token, sender.time, sender.apiKey, sender.locations);
        call.enqueue(callback);
    }

    @Override
    protected String getUrl() {
        return ILocationApi.url;
    }

    @Override
    protected String getStringSender() {
        return sender.toString();
    }
}

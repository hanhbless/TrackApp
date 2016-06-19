package com.sunnet.service.task.request;

import com.sunnet.service.task.api.ISmsApi;
import com.sunnet.service.task.response.SmsResponse;
import com.sunnet.service.task.sender.SmsSender;

import retrofit2.Call;
import retrofit2.Callback;

public class SmsRequest extends BaseRequest {
    private SmsSender sender;
    private Callback<SmsResponse> callback;

    public SmsRequest(SmsSender sender, Callback<SmsResponse> callback) {
        this.sender = sender;
        this.callback = callback;
    }

    @Override
    public void execute() {
        super.execute();

        ISmsApi api = restAdapter.create(ISmsApi.class);
        Call<SmsResponse> call = api.addSms(sender.token, sender.time, sender.apiKey, sender.sms);
        call.enqueue(callback);
    }

    @Override
    protected String getUrl() {
        return ISmsApi.url;
    }

    @Override
    protected String getStringSender() {
        return sender.toString();
    }
}

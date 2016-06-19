package com.sunnet.service.task.request;

import com.sunnet.service.task.api.IContactApi;
import com.sunnet.service.task.response.ContactResponse;
import com.sunnet.service.task.sender.ContactSender;

import retrofit2.Call;
import retrofit2.Callback;

public class ContactRequest extends BaseRequest {
    private ContactSender sender;
    private Callback<ContactResponse> callback;

    public ContactRequest(ContactSender sender, Callback<ContactResponse> callback) {
        this.sender = sender;
        this.callback = callback;
    }

    @Override
    public void execute() {
        super.execute();

        IContactApi api = restAdapter.create(IContactApi.class);
        Call<ContactResponse> call = api.addContact(sender.token, sender.time, sender.apiKey, sender.contacts);
        call.enqueue(callback);
    }

    @Override
    protected String getUrl() {
        return IContactApi.url;
    }

    @Override
    protected String getStringSender() {
        return sender.toString();
    }
}

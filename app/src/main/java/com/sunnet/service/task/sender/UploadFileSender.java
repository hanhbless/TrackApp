package com.sunnet.service.task.sender;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class UploadFileSender extends BaseSender {
    public String uri;
    public String phoneNumber;
    public String phoneNumberOrAppPackage;

    @Override
    public String toString() {
        return "UploadFileSender{" +
                "uri='" + uri + '\'' +
                super.toString() +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", phoneNumberOrAppPackage='" + phoneNumberOrAppPackage + '\'' +
                '}';    }
}

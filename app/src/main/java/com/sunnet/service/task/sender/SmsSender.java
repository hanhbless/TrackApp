package com.sunnet.service.task.sender;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class SmsSender extends BaseSender {
    public String sms;

    @Override
    public String toString() {
        return "SmsSender{" +
                super.toString() +
                "sms='" + sms + '\'' +
                '}';
    }
}

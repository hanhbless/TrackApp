package com.sunnet.service.task.sender;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class ContactSender extends BaseSender {
    public String contacts;

    @Override
    public String toString() {
        return "ContactSender{" +
                super.toString() +
                "contacts='" + contacts + '\'' +
                '}';
    }
}

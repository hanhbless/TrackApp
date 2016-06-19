package com.sunnet.service.task.sender;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class LocationSender extends BaseSender {
    public String locations;

    @Override
    public String toString() {
        return "LocationSender{" +
                super.toString() +
                "locations='" + locations + '\'' +
                '}';
    }
}

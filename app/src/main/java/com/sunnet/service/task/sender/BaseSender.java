package com.sunnet.service.task.sender;

/**
 * All software created will be owned by
 * Patient Doctor Technologies, Inc. in USA
 */
public class BaseSender {
    public String token;
    public long time;
    public String apiKey;

    @Override
    public String toString() {
        return "token='" + token + '\'' +
                ", time='" + time + '\'' +
                ", apiKey='" + apiKey + '\'';
    }
}

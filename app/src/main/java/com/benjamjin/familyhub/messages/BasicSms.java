package com.benjamjin.familyhub.messages;

/**
 * Created by benjamin on 08/08/2017.
 */

class BasicSms {
    protected String senderAddress;
    protected String timestampSent;
    protected String body;

    public String toString() {
        return String.format("%s, %s, %s", timestampSent, senderAddress, body);
    }
}

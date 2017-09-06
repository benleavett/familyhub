package com.benjamjin.familyhub.messages;

class BasicSms {
    Long timestampSent;
    String senderAddress;
    String body;
    String id;
    boolean isRead = false;

    // Initialise as may never be set elsewhere but we may want to use it in toString()
    String friendlySenderName = "";

    BasicSms() {}

    BasicSms(Long timestampSent, String senderAddress, String body, String id) {
        this.timestampSent = timestampSent;
        this.senderAddress = senderAddress;
        this.body = body;
        this.id = id;
    }

    public String toString() {
        return String.format("[%s] %d, %s (%s), %s, %b", id, timestampSent, senderAddress, friendlySenderName, body, isRead);
    }
}

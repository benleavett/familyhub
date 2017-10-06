package com.bivaca.familyhub.messages;

final class BasicSms {
    Long timestampSent;
    String senderAddress;
    String friendlySenderName;
    String body;
    String id;
    boolean isRead;
    boolean isRepliedTo;

    private BasicSms() {}

    BasicSms(Long timestampSent, String senderAddress, String friendlySenderName, String body, String id) {
        this(timestampSent, senderAddress, friendlySenderName, body, id, false);
    }

    BasicSms(Long timestampSent, String senderAddress, String friendlySenderName, String body, String id, boolean isRead) {
        this.timestampSent = timestampSent;
        this.senderAddress = senderAddress;
        this.friendlySenderName = friendlySenderName;
        this.body = body;
        this.id = id;
        this.isRead = isRead;

        this.isRepliedTo = false;
    }

    public String toString() {
        return String.format("[%s] %d, %s (%s), %s, %b/%b", id, timestampSent, senderAddress, friendlySenderName, body, isRead, isRepliedTo);
    }
}

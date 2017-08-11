package com.benjamjin.familyhub.messages;

import android.database.Cursor;
import android.util.Log;

import java.util.LinkedList;

/**
 * Created by benjamin on 19/08/2017.
 */

class Inbox {

    private static Inbox mInstance;

    private static LinkedList<BasicSms> inbox = null;
    private static int mInboxIterator = -1;

    static Inbox getInstance() {
        if (mInstance == null) {
            mInstance = new Inbox();
        }
        return mInstance;
    }

    private Inbox() {}

    /*
        Order of messages is:
        0 ..................... n
        Earliest message        Latest message
     */
    static void initInboxFromCursor(Cursor cursor) {
        if (inbox == null) {
            inbox = new LinkedList<>();

            if (cursor.moveToFirst()) {
                do {
                    BasicSms sms = new BasicSms();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        if (cursor.getColumnName(i).equals("body")) {
                            sms.body = cursor.getString(i);
                        } else if (cursor.getColumnName(i).equals("address")) {
                            sms.senderAddress = cursor.getString(i);
                        } else if (cursor.getColumnName(i).equals("date_sent")) {
                            sms.timestampSent = cursor.getString(i);
                        }
                    }

                    if (AcceptedContacts.getInstance().isAcceptedContact(sms.senderAddress)) {
                        inbox.push(sms);
                        Log.d("BEN", "Family SMS: " + sms);
                    }

                } while (cursor.moveToNext());

                resetInboxIterator();

            } else {
                Log.i("BEN", "No SMS");
            }
        } else {
            resetInboxIterator();
        }
    }

    private static void resetInboxIterator() {
        if (inbox.size() > 0) {
            mInboxIterator = inbox.size() - 1;
        }
    }

    static BasicSms getCurrentMessage() {
        Log.d("BEN", mInboxIterator + "");
        if (mInboxIterator >= 0) {
            return inbox.get(mInboxIterator);
        } else {
            return null;
        }
    }

    static BasicSms moveCursorToLaterMessage() {
        if (hasLaterMessage()) {
            mInboxIterator++;
            return getCurrentMessage();
        } else {
            return null;
        }
    }

    static BasicSms moveCursorToEarlierMessage() {
        if (hasEarlierMessage()) {
            mInboxIterator--;
            return getCurrentMessage();
        } else {
            return null;
        }
    }

    static boolean hasLaterMessage() {
        return mInboxIterator+1 < inbox.size();
    }

    static boolean hasEarlierMessage() {
        return mInboxIterator > 0;
    }
}

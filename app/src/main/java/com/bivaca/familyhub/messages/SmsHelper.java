package com.bivaca.familyhub.messages;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

class SmsHelper {
    private static final String TAG = SmsHelper.class.getSimpleName();

    private static final String SMS_COLUMN_DB_ID = "_id";

    private static final String[] ALL_SMS_MESSAGES_PROJECTION = {
            Telephony.Sms.BODY,
            Telephony.Sms.DATE_SENT,
            Telephony.Sms.READ,
            Telephony.Sms.ADDRESS,
            SMS_COLUMN_DB_ID};

    static Uri insertMessageToInbox(Context context, String senderAddress, String body, long timestamp) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues cv = new ContentValues();

        cv.put(Telephony.Sms.Inbox.ADDRESS, senderAddress);
        cv.put(Telephony.Sms.Inbox.BODY, body);
        cv.put(Telephony.Sms.Inbox.DATE_SENT, timestamp);

        return contentResolver.insert(Telephony.Sms.CONTENT_URI, cv);
    }

    static boolean markMessageAsRead(Context context, final BasicSms sms) {
        ContentResolver contentResolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        values.put(Telephony.Sms.READ, true);
        values.put(Telephony.Sms.SEEN, true);

        String selection = String.format("%s=? AND %s=? AND %s=?",
                Telephony.Sms.ADDRESS,
                Telephony.Sms.DATE_SENT,
                Telephony.Sms.BODY);
        String[] selectionArgs = {sms.senderAddress, sms.timestampSent.toString(), sms.body};

        int numRowsUpdates = contentResolver.update(Telephony.Sms.CONTENT_URI, values, selection, selectionArgs);
        Log.d(TAG, "Rows updated: " + numRowsUpdates);
        return numRowsUpdates == 1;
    }

    private static Cursor getContentCursorForAllMessages(Context context) {
        String sortOrder = Telephony.Sms.DATE_SENT + " ASC";
        return context.getContentResolver().query(Telephony.Sms.CONTENT_URI, ALL_SMS_MESSAGES_PROJECTION, null, null, sortOrder);
    }

//    private static Cursor getContentCursorForMessage(Context context, BasicSms sms) {
//        String selection = String.format("%s=? AND %s=? AND %s=?",
//                Telephony.Sms.ADDRESS,
//                Telephony.Sms.DATE_SENT,
//                Telephony.Sms.BODY);
//        String[] selectionArgs = {sms.senderAddress, sms.timestampSent.toString(), sms.body};
//
//        return context.getContentResolver().query(Telephony.Sms.CONTENT_URI, ALL_SMS_MESSAGES_PROJECTION, selection, selectionArgs, null);
//    }

//    private String getMsgIdFromSmsDatabase(Context context, final BasicSms sms) {
//        String msgId = null;
//        Cursor cursor = null;
//
//        try {
//            cursor = SmsHelper.getContentCursorForMessage(context, sms);
//
//            if (cursor != null && cursor.moveToFirst()) {
//                do {
//                    // Columns read must match be in columns in ALL_SMS_MESSAGES_PROJECTION
//                    for (int i = 0; i < cursor.getColumnCount(); i++) {
//                        if (cursor.getColumnName(i).equals(Telephony.Sms.BODY)) {
//                            String body = cursor.getString(i);
//
//                            // Check this is the right SMS by matching the body text (we already know that the address, date_sent and read state match)
//                            if (sms.body.startsWith(body)) {
//                                msgId = cursor.getString(cursor.getColumnIndex(SMS_COLUMN_DB_ID));
//                                Log.d(TAG, "Got ID: " + msgId);
//                                break;
//                            }
//                        }
//                    }
//                } while (cursor.moveToNext() && msgId == null);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, e.toString(), e);
//        }
//        finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//
//        return msgId;
//    }

    static List<BasicSms> getAllMessages(Context context) {
        ArrayList<BasicSms> allMessages = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = SmsHelper.getContentCursorForAllMessages(context);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String body = "";
                    String senderAddress = "";
                    long timestampSent = 0;
                    boolean isRead = false;
                    String id = "";

                    // Columns read must match exactly columns in ALL_SMS_MESSAGES_PROJECTION
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        if (cursor.getColumnName(i).equals(Telephony.Sms.BODY)) {
                            body = cursor.getString(i);
                        } else if (cursor.getColumnName(i).equals(Telephony.Sms.ADDRESS)) {
                            senderAddress = cursor.getString(i);
                        } else if (cursor.getColumnName(i).equals(Telephony.Sms.DATE_SENT)) {
                            timestampSent = Long.parseLong(cursor.getString(i));
                        } else if (cursor.getColumnName(i).equals(Telephony.Sms.READ)) {
                            isRead = cursor.getString(i).equals("1");
                        } else if (cursor.getColumnName(i).equals(SMS_COLUMN_DB_ID)) {
                            id = cursor.getString(i);
                        }
                    }

                    if (AcceptedContacts.getInstance().isAcceptedContact(senderAddress)) {
                        final String contactName = AcceptedContacts.getInstance().getContactName(senderAddress);
                        BasicSms sms = new BasicSms(timestampSent, senderAddress, contactName, body, id, isRead);

                        allMessages.add(sms);

                        Log.d(TAG, "Adding SMS to inbox: " + sms);
                    }

                } while (cursor.moveToNext());

            } else {
                Log.w(TAG, "No SMS that match query");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return allMessages;
    }

    static String getIdFromUri(String uri) {
        Log.e("BEN", "FIXME " + uri);
        //TODO
        return "-1";
    }

    public static Uri insertMessageToOutbox(Context context, String mSenderAddress, String message) {
        Calendar cal = Calendar.getInstance();

        ContentValues values = new ContentValues();
        values.put("address", mSenderAddress);
        values.put("body", message);
        values.put("date", cal.getTimeInMillis());
        values.put("read", 1);
        values.put("type", Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX);

        return context.getContentResolver().insert(Telephony.Sms.CONTENT_URI, values);
    }

    public static int clearInbox(Context context) {
        return context.getContentResolver().delete(Telephony.Sms.CONTENT_URI, null, null);
    }

//    public static BasicSms getSmsFromUri(Context context, Uri uri) {
//        long messageId = -1;
//        Cursor cursor = null;
//
//        try {
//            cursor = context.getContentResolver().query(uri, new String[]{SmsHelper.SMS_COLUMN_DB_ID},
//                    null, null, null);
//            if (cursor.moveToFirst()) {
//                messageId = cursor.getLong(cursor.getColumnIndexOrThrow(SmsHelper.SMS_COLUMN_DB_ID));
//            }
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//    }
}

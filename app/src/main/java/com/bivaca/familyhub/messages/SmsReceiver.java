package com.bivaca.familyhub.messages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.bivaca.familyhub.FirebaseEventLogger;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = SmsReceiver.class.getSimpleName();

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_DELIVER";
    private static final String SMS_BUNDLE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Intent received: " + intent.getAction());

        if (intent.getAction().equals(SMS_RECEIVED)) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                Object[] pdus = (Object[]) extras.get(SMS_BUNDLE);
                if (pdus != null) {
                    final SmsMessage[] message = new SmsMessage[pdus.length];

                    for (int i = 0; i < pdus.length; i++) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            String format = extras.getString("format");
                            message[i] = SmsMessage.createFromPdu((byte[])pdus[i], format);
                        } else {
                            message[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }
                    }

                    if (message.length > -1) {
                        Log.d(TAG, String.format("SMS received (length: %d): %s", message.length, message[0].getMessageBody()));

                        saveNewSmsAndNotify(context, message);
                    }
                }
            }
        }
    }

    private void saveNewSmsAndNotify(Context context, final SmsMessage[] message) {
        final long timestamp = message[0].getTimestampMillis();
        final String senderAddress = message[0].getOriginatingAddress();
        final String body = getMessageBodyAsString(message);

        Uri uriResult = SmsHelper.insertMessageToInbox(context, senderAddress, body, timestamp);

        if (AcceptedContacts.getInstance().isAcceptedContact(senderAddress)) {
            final String contactName = AcceptedContacts.getInstance().getContactName(senderAddress);

            BasicSms sms = new BasicSms(timestamp, senderAddress, contactName, body, uriResult.toString());
            Log.d(TAG, "Stored SMS from accepted contact " + uriResult + ": " + sms);

            notifyActivityOfNewSms(context, uriResult, sms);

            playSoundNewMessage(context);

            FirebaseEventLogger.logMessageReceived(context, sms.senderAddress, sms.friendlySenderName, sms.id);
        }
    }

    private void playSoundNewMessage(Context context) {
        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        MediaPlayer mediaPlayer = new MediaPlayer();
        AudioAttributes.Builder builder = new AudioAttributes.Builder();
        builder.setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT);

        try {
            mediaPlayer.setDataSource(context, defaultRingtoneUri);
            mediaPlayer.setAudioAttributes(builder.build());
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            mediaPlayer.start();
        } catch (Exception ex) {
            Log.e(TAG, ex.getStackTrace().toString());
            FirebaseCrash.report(ex);
        }
    }

    private void notifyActivityOfNewSms(Context context, Uri uri, BasicSms sms) {
        Log.d(TAG, "Sending intent to InboxActivity for sms: " + sms);

        Intent i = new Intent(context, InboxActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra(InboxActivity.INTENT_KEY_URI, uri.toString());
        i.putExtra(InboxActivity.INTENT_KEY_BODY, sms.body);
        i.putExtra(InboxActivity.INTENT_KEY_SENDER_ADDRESS, sms.senderAddress);
        i.putExtra(InboxActivity.INTENT_KEY_TIMESTAMP, sms.timestampSent);
        i.setAction(InboxActivity.INTENT_ACTION_NAME_NEW_SMS);

        context.startActivity(i);
    }

    private static String getMessageBodyAsString(SmsMessage[] message) {
        StringBuilder stringBuilder = new StringBuilder();
        for (SmsMessage msgPart : message) {
            stringBuilder.append(msgPart.getMessageBody());
        }
        return stringBuilder.toString();
    }
}

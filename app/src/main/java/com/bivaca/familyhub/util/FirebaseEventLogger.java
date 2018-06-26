package com.bivaca.familyhub.util;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseEventLogger {

    public static void logMessageSent(Context context, String recipientAddress, String friendlyName, String messageId) {
        Bundle bundle = new Bundle();
        bundle.putString("RecipientAddress", recipientAddress);
        bundle.putString("RecipientName", friendlyName);
        bundle.putString("OriginalMessageID", messageId);

        FirebaseAnalytics.getInstance(context).logEvent("message_sent", bundle);
    }

    public static void logMessageReceived(Context context, String senderAddress, String friendlyName, String messageId) {
        Bundle bundle = new Bundle();
        bundle.putString("SenderAddress", senderAddress);
        bundle.putString("SenderName", friendlyName);
        bundle.putString("MessageID", messageId);

        FirebaseAnalytics.getInstance(context).logEvent("message_received", bundle);
    }

    public static void logVocalisation(Context context) {
        FirebaseAnalytics.getInstance(context).logEvent("vocalisation", null);
    }

    public static void logPhotoFeatureExplicitlyLaunched(Context context) {
        FirebaseAnalytics.getInstance(context).logEvent("photos_launch_action", null);
    }

    public static void logMessagesFeatureExplicitlyLaunched(Context context) {
        FirebaseAnalytics.getInstance(context).logEvent("messages_launch_action", null);
    }

    public static void logSettingsLaunched(Context context) {
        FirebaseAnalytics.getInstance(context).logEvent("settings_launch_action", null);
    }

    public static void logResetLauncher(Context context) {
        FirebaseAnalytics.getInstance(context).logEvent("reset_action", null);
    }
}

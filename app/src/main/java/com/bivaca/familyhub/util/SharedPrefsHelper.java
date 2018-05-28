package com.bivaca.familyhub.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bivaca.familyhub.R;
import com.bivaca.familyhub.messages.InboxActivity;

import java.util.Map;

/**
 * Created by benjamin on 30/09/2017.
 */

public class SharedPrefsHelper {
    private static final String SHARED_PREFS_MESSAGE_REPLIED_STATE_PREFIX = "sp_message_replied_state_for_id";

    public static void writeMessageRepliedStateToDisk(Context context, String messageId) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(getSharedPrefsMessageRepliedStateKey(messageId), true);
        editor.commit();
    }

    public static boolean getMessageRepliedStateFromDisk(Context context, String messageId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(getSharedPrefsMessageRepliedStateKey(messageId), false);
    }

    private static String getSharedPrefsMessageRepliedStateKey(final String messageId) {
        return SHARED_PREFS_MESSAGE_REPLIED_STATE_PREFIX + "_" + messageId;
    }

    public static void deleteAllMessageRepliedStatesOnDisk(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        Map<String, ?> allSharedPrefs = sp.getAll();
        for (String key : allSharedPrefs.keySet()) {
            if (key.startsWith(SHARED_PREFS_MESSAGE_REPLIED_STATE_PREFIX)) {
                editor.remove(key);
            }
        }

        editor.commit();
    }

    public static boolean isKeepScreenOnEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.sp_name_keep_screen_on), context.getResources().getBoolean(R.bool.pref_default_keep_screen_on_default));
    }

    public static boolean isFullscreenEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.sp_name_enable_fullscreen), true);
    }

    public static boolean isHideMessageWhenReplied(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.sp_name_pref_hide_msg_when_replied), false);
    }

    public static boolean isAutoPlaySlides(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("pref_auto_play_slides", context.getResources().getBoolean(R.bool.pref_default_auto_play_slides));
    }

    public static int getSlideTransitionFrequencyMilliseconds(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        // This is such bullshit. Android EditTextPreference can only ever take a string
        String spValue = sp.getString("pref_transition_frequency", context.getString(R.string.pref_default_transition_frequency_seconds));
        return Integer.valueOf(spValue) * 1000;
    }

    public static long getAutoPlayDelayMilliseconds(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String spValue = sp.getString("pref_delay_initiate_auto_play", context.getString(R.string.pref_default_delay_initiate_auto_play_seconds));
        return Integer.valueOf(spValue) * 1000;
    }

    public static boolean isPhotosEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("pref_name_enable_photos", context.getResources().getBoolean(R.bool.pref_default_enable_photos));
    }

    public static boolean isRepliesEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.sp_name_enable_replies), context.getResources().getBoolean(R.bool.pref_default_replies_enabled));
    }
}

package com.bivaca.familyhub;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
        return sp.getBoolean(context.getString(R.string.sp_name_keep_screen_on), context.getResources().getBoolean(R.bool.keep_screen_on_default));
    }

    public static boolean isFullscreenEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getString(R.string.sp_name_enable_fullscreen), true);
    }
}

package com.bivaca.familyhub;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    private static final String[] SMS_PERMISSIONS = { Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS };

    private VocaliserService mVocaliser;
    private Boolean mIsFullscreen = null;

    @Override
    public void onCreate() {
        super.onCreate();

        mVocaliser = new VocaliserService(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);

        //FIXME need to unregister at some point (needs moving to an Activity)
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(mVocaliser);
    }

    public void doVocalise(String text) {
        mVocaliser.doVocalise(text);
    }

    /**
     * Checks if we have the required SMS permissions. If not request them from the user
     * and return asynchronously the current, pre-request permission state.
     * @return True if we have the required SMS permissions already, false if not.
     */
    public boolean verifySmsPermissions(Activity activity) {
        List<String> missingPermissionsNeeded = getMissingSmsPermissions(activity);

        if (!missingPermissionsNeeded.isEmpty()) {
            requestSmsPermissions(activity, missingPermissionsNeeded);
            return false;
        } else {
            return true;
        }
    }
    private List<String> getMissingSmsPermissions(Activity activity) {
        List<String> missingSmsPermissionsNeeded = new ArrayList<>();

        int result;
        for (String p : SMS_PERMISSIONS) {
            result = ContextCompat.checkSelfPermission(activity, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingSmsPermissionsNeeded.add(p);
            }
        }

        return missingSmsPermissionsNeeded;
    }

    private void requestSmsPermissions(Activity activity, List<String> missingPermissionsNeeded) {
        int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

        ActivityCompat.requestPermissions(
                activity,
                missingPermissionsNeeded.toArray(new String[missingPermissionsNeeded.size()]),
                REQUEST_ID_MULTIPLE_PERMISSIONS);
    }
}



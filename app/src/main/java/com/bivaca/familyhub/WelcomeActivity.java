package com.bivaca.familyhub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class WelcomeActivity extends MyActivity {

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If you haven't seen the welcome before, you'll see it. If you're in debug mode and it hasn't been shown this app-run, you'll see it.
//        if (!hasWelcomeBeenShown() || BuildConfig.DEBUG) {
//            setContentView(R.layout.activity_welcome);
//
//            configureCheckBoxFullscreenEnabled();
//
//            setHasWelcomeBeenShownPref(true);
//        } else {
             Log.d(TAG, "Skipping welcome screen");

             startNextActivity(null);
//         }
    }

    public void startNextActivity(View v) {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void configureCheckBoxFullscreenEnabled() {
        CheckBox cb = (CheckBox)findViewById(R.id.cb_is_fullscreen_enabled);
        cb.setChecked(Util.isFullscreenModeEnabled(this));

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isEnabled) {
                setFullscreenEnabledPref(isEnabled);
            }
        });

    }

    private void setFullscreenEnabledPref(boolean isEnabled) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(getString(R.string.sp_name_enable_fullscreen), isEnabled);
        editor.apply();
    }

    private void setHasWelcomeBeenShownPref(boolean hasWelcomeBeenShown) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(getString(R.string.sp_name_is_welcome_shown), hasWelcomeBeenShown);
        editor.apply();
    }

    private boolean hasWelcomeBeenShown() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean(getString(R.string.sp_name_is_welcome_shown), false);
    }
}

package com.bivaca.familyhub;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bivaca.familyhub.messages.Inbox;
import com.bivaca.familyhub.photos.SlideshowActivity;
import com.bivaca.familyhub.util.FirebaseEventLogger;
import com.bivaca.familyhub.util.SharedPrefsHelper;
import com.bivaca.familyhub.util.Util;

import com.bivaca.familyhub.messages.InboxActivity;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends MyActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MAKE_DEFAULT_SMS_PROMPT = 1;
    private static final int MAKE_DEFAULT_LAUNCHER_PROMPT = 2;
    private static final String VERSION_STRING = "v" + BuildConfig.VERSION_CODE;

    private Handler runSlideshowHandler = null;

    private final Runnable showPhotosRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Timer - started");
            showPhotosActivity(null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        setClearInboxVisibleState();

        if (!SharedPrefsHelper.isDevModeEnabled(this)) {
            //TODO check if SMS function is enabled
            getMyApplication().verifySmsPermissions(this);
        }
    }

    private void updateHeaderTextForDevMode() {
        TextView versionView = findViewById(R.id.version_text);
        if (SharedPrefsHelper.isDevModeEnabled(this)) {
            versionView.setText(String.format(getString(R.string.dev_mode_header_string_format), VERSION_STRING));
        } else {
            versionView.setText(VERSION_STRING);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateHeaderTextForDevMode();

        if (!SharedPrefsHelper.isDevModeEnabled(this)) {
            if (!Util.isDefaultMessagingApp(this)) {
                promptUserToMakeDefaultMessagingApp();
            }

            if (!hasDeviceActiveSim()) {
                new AlertDialog.Builder(this)
                        .setTitle(getTitle())
                        .setMessage(getString(R.string.dialog_text_no_sim_card))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .show();
            }
        }

        // If we had to prompt to make home then we'll set the timer later, if not then set it here
        if (SharedPrefsHelper.isDevModeEnabled(this) || !promptToMakeHomeLauncher()) {
            if (SharedPrefsHelper.isPhotosEnabled(this) && SharedPrefsHelper.isAutoPlaySlides(this)) {
                setTimerToLoadSlideshow();
            }
        }

        // Enable photos button only if the pref says we should
        Button photosButton = findViewById(R.id.show_photos_button);
        photosButton.setEnabled(SharedPrefsHelper.isPhotosEnabled(this));
    }

    private void cancelTimerToLoadSlideshow() {
        Log.d(TAG, "Timer - cancelling");
        if (runSlideshowHandler != null) {
            runSlideshowHandler.removeCallbacks(showPhotosRunnable);
        }
    }

    private void setTimerToLoadSlideshow() {
        Log.d(TAG, "Timer - setting");
        runSlideshowHandler = new Handler();

        runSlideshowHandler.postDelayed(showPhotosRunnable, SharedPrefsHelper.getAutoPlayDelayMilliseconds(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!super.handleRequestPermissionsResult(requestCode, permissions, grantResults)) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MAKE_DEFAULT_SMS_PROMPT) {
            if (resultCode != RESULT_OK) {
                Log.w(TAG, "User declined to set as default SMS app");

                if (!BuildConfig.DEBUG) {
                    finish();
                }
            }
        } else if (requestCode == MAKE_DEFAULT_LAUNCHER_PROMPT) {
            if (resultCode == RESULT_OK && SharedPrefsHelper.isAutoPlaySlides(this)) {
                setTimerToLoadSlideshow();
            }
        }
    }

    private void setClearInboxVisibleState() {
        if (BuildConfig.DEBUG || SharedPrefsHelper.isDevModeEnabled(this)) {
            Button clearInboxBtn = findViewById(R.id.clear_inbox_btn);
            clearInboxBtn.setVisibility(View.VISIBLE);
        }
    }

    private void promptUserToMakeDefaultMessagingApp() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
        startActivityForResult(intent, MAKE_DEFAULT_SMS_PROMPT);
    }

    public void showMessagesActivity(View v) {
        cancelTimerToLoadSlideshow();
        FirebaseEventLogger.logMessagesFeatureExplicitlyLaunched(this);
        startActivity(new Intent(this, InboxActivity.class));
    }

    public void showPhotosActivity(View v) {
        cancelTimerToLoadSlideshow();
        FirebaseEventLogger.logPhotoFeatureExplicitlyLaunched(this);
        startActivity(new Intent(this, SlideshowActivity.class));
    }

    public void showSettingsActivity(View v) {
        cancelTimerToLoadSlideshow();
        FirebaseEventLogger.logSettingsLaunched(this);
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void clearInbox(View v) {
        String message;
        if (!Inbox.getInstance().clearInbox(this)) {
            message = "Failed to clear inbox";
            Log.e(TAG, message);

        } else {
            message = "All messages deleted from device";
            Log.w(TAG, message);
        }

        LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
        Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
    }

    private boolean hasDeviceActiveSim() {
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        return tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT;
    }

//    private void launchHomeActivity() {
//        Intent selector = new Intent(Intent.ACTION_MAIN);
//        selector.addCategory(Intent.CATEGORY_HOME);
//        selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(selector);
//
//        getPackageManager().setComponentEnabledSetting(getComponentName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);
//    }

    public void resetLauncher(View v) {
        FirebaseEventLogger.logResetLauncher(this);

        if (Util.isDefault(this)) {
            getPackageManager().clearPackagePreferredActivities(getPackageName());
            startActivity(Util.homeScreenIntent());
            finish();
        }
    }

    private boolean promptToMakeHomeLauncher() {
        if (Util.isNotHomeLauncher(this)) {
            if (Util.isLollipopOrAbove()) {
                chooseHomeScreenLollipop();
            } else {
                chooseHomeScreenDefault();
            }
            return true;
        } else {
            return false;
        }
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        Log.d(TAG, "DISPATCHING");
//        return super.dispatchTouchEvent(ev);
//    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void chooseHomeScreenLollipop() {
        final PackageManager pm = getPackageManager();
        final ResolveInfo info = pm.resolveActivity(Util.homeScreenIntent(), 0);

        new AlertDialog.Builder(this)
                .setTitle(getTitle())
                .setMessage(String.format(
                        getString(R.string.dialog_text_change_home_lollipop_above),
                        info.activityInfo.loadLabel(pm),
                        getString(R.string.app_name)))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS), MAKE_DEFAULT_LAUNCHER_PROMPT);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .show();
    }

    private void chooseHomeScreenDefault() {
        final PackageManager pm = getPackageManager();
        final ResolveInfo info = pm.resolveActivity(Util.homeScreenIntent(), 0);

        new AlertDialog.Builder(this)
                .setTitle(getTitle())
                .setMessage(String.format(getString(R.string.dialog_text_change_home_default), info.activityInfo.loadLabel(pm)))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // A home screen is set - take us to that
                        Intent settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        settings.setData(Uri.parse("package:" + info.activityInfo.packageName));
                        startActivityForResult(settings, MAKE_DEFAULT_LAUNCHER_PROMPT);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .show();
    }
}

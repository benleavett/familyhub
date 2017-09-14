package com.bivaca.familyhub;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import com.bivaca.familyhub.messages.InboxActivity;

public class MainActivity extends MyActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int MAKE_DEFAULT_SMS_PROMPT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);

        //TODO check if SMS function is enabled
        getMyApplication().verifySmsPermissions(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!isDefaultMessagingApp()) {
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

        promptToMakeHome();
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
            }
        }
    }

    private void promptUserToMakeDefaultMessagingApp() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
        startActivityForResult(intent, MAKE_DEFAULT_SMS_PROMPT);
    }

    private boolean isDefaultMessagingApp() {
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);
        return defaultSmsPackage != null && defaultSmsPackage.equals(getPackageName());
    }

    public void showMessagesActivity(View v) {
        startActivity(new Intent(this, InboxActivity.class));
    }

    public void showPhotosActivity(View v) {
//        startActivity(new Intent(this, PhotosActivity.class));
    }

    public void showSettingsActivity(View v) {
        startActivity(new Intent(this, SettingsActivity.class));
        //TODO wtf - testing snackbar
//        LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
//        Snackbar snackBar = Snackbar.make(layout, "You've received a new message", Snackbar.LENGTH_LONG);
//        snackBar.show();
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
        if (Util.isDefault(this)) {
            getPackageManager().clearPackagePreferredActivities(getPackageName());
            startActivity(Util.homeScreenIntent());
            finish();
        }
    }

    private void promptToMakeHome() {
        if (!Util.noDefaultSet(this) && !Util.isDefault(this)) {
            if (Util.isLollipopOrAbove()) {
                chooseHomeScreenLollipop();
            } else {
                chooseHomeScreenDefault();
            }
        }
    }

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
                        startActivity(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS));
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
                        startActivity(settings);
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

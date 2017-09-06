package com.benjamjin.familyhub;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.benjamjin.familyhub.messages.InboxActivity;

import java.util.List;

public class MainActivity extends MyActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //TODO check if SMS function is enabled
        ((MyApplication)getApplication()).verifySmsPermissions(this);

        if(!isDefaultMessagingApp()) {
            promptUserToMakeDefaultMessagingApp();
        }
    }

    public void showMessagesActivity(View v) {
        startActivity(new Intent(this, InboxActivity.class));
    }

    public void showPhotosActivity(View v) {
//        startActivity(new Intent(this, PhotosActivity.class));
    }

    public void showSettingsActivity(View v) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void resetLauncher(View v) {
        //TODO
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!super.handleRequestPermissionsResult(requestCode, permissions, grantResults)) {
            finish();
        }
    }

    private void promptUserToMakeDefaultMessagingApp() {
        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
        this.startActivity(intent);
    }

    private boolean isDefaultMessagingApp() {
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this);
        return defaultSmsPackage != null && defaultSmsPackage.equals(getPackageName());
    }
}

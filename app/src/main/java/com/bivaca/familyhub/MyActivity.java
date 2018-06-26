package com.bivaca.familyhub;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.bivaca.familyhub.util.SharedPrefsHelper;
import com.bivaca.familyhub.util.Util;

import java.util.HashMap;

public class MyActivity extends AppCompatActivity {
    private static final String TAG = MyActivity.class.getSimpleName();

    @Override
    protected void onResume() {
        super.onResume();

        setWindowFullscreenState();
        setKeepScreenOnState();
    }

    private void setKeepScreenOnState() {
        //FIXME register receiver to get changes to charging state
        // Only allow screen to stay on if we're charging
        if (SharedPrefsHelper.isKeepScreenOnEnabled(this) && Util.isChargerPluggedIn(this)) {
            Log.d(TAG, "Keep screen on state: ENABLED");
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            Log.d(TAG, "Keep screen on state: DISABLED");
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    protected MyApplication getMyApplication() {
        return (MyApplication)getApplication();
    }

    public void showPreviousActivity(View v) {
        finish();
    }

    protected boolean handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int index = 0;
        HashMap<String, Integer> PermissionsMap = new HashMap<>();
        for (String permission : permissions) {
            PermissionsMap.put(permission, grantResults[index]);
            index++;
        }

        if (PermissionsMap.get(Manifest.permission.RECEIVE_SMS) != 0 ||
                PermissionsMap.get(Manifest.permission.READ_SMS) != 0) {
            Toast.makeText(this, "SMS permissions are required", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void setWindowFullscreenState() {
        if (SharedPrefsHelper.isFullscreenEnabled(this)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}

package com.bivaca.familyhub;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.HashMap;

public class MyActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();

        setWindowFullscreenState();
        setKeepScreenOnState();
    }

    private void setKeepScreenOnState() {
        //FIXME register receiver to get changes to charging state
        // Only allow screen to stay on if we're charging
        if (SharedPrefsHelper.isKeepScreenOnEnabled(this) && Util.isCharging(this)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
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

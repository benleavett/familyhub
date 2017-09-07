package com.benjamjin.familyhub;

import android.Manifest;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
    }

    protected MyApplication getMyApplication() {
        return (MyApplication)getApplication();
    }

    public void showPreviousActivity(View v) {
        finish();
    }

    public boolean handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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

    private boolean isFullscreenEnabled() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean(getString(R.string.sp_name_enable_fullscreen), true);
    }

    public void setWindowFullscreenState() {
        if (isFullscreenEnabled()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}

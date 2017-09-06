package com.benjamjin.familyhub;

import android.Manifest;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;

public class MyActivity extends AppCompatActivity {

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
}

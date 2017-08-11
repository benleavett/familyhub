package com.benjamjin.familyhub;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by benjamin on 09/08/2017.
 */

public class MyActivity extends AppCompatActivity {

    protected void vocaliseText(String text) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getBoolean(getString(R.string.sp_name_vocalisation_enabled), false)) {
            MyApplication.getInstance().vocaliseText(text);
        }
    }
}

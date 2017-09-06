package com.benjamjin.familyhub;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class SettingsActivity extends MyActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setTitle(getString(R.string.app_name) + " settings");
    }

    //FIXME doesn't work (so commented out)
//    @Override
//    public boolean onPreferenceClick(Preference preference) {
//        Log.d("BEN", "HERE " + preference.getKey());
//        if (preference.getKey().equals(getString(R.string.sp_name_pref_choose_speech_rate))) {
//            doVocalise(getString(R.string.choose_speech_rate_title) + " selected");
//        }
//        return false;
//    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
        }
    }
}

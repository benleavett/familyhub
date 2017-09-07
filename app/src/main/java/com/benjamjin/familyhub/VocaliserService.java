package com.benjamjin.familyhub;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

class VocaliserService implements OnInitListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = VocaliserService.class.getSimpleName();

    private final TextToSpeech mTTS;
    private boolean mIsTTSReady = false;
    private boolean mIsEnableVocalisation;
    private final Locale mCachedLocalePref;
    private final Context mContext;

    VocaliserService(Context context) {
        this(context, Locale.getDefault());
    }

    private VocaliserService(Context context, Locale userLocalePref) {
        mTTS = new TextToSpeech(context, this);
        mContext = context;
        mIsEnableVocalisation = false;

        Log.d(TAG, "User locale: " + userLocalePref);
        mCachedLocalePref = userLocalePref;
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            Log.e(TAG, "Error initialising TextToSpeech engine");
        } else {
            mIsTTSReady = true;
        }

        mTTS.setLanguage(mCachedLocalePref);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String spKey) {
        if (spKey.equals(mContext.getString(R.string.sp_name_pref_choose_speech_rate))) {
            setSpeechRate(sp.getString(spKey, ""));
        } else if (spKey.equals(mContext.getString(R.string.sp_name_vocalisation_enabled))) {
            setEnableVocalisation(sp.getBoolean(spKey, false));
        }
    }

    void doVocalise(String text) {
        if (mIsTTSReady) {
            if (mIsEnableVocalisation) {
                Log.d(TAG, "Vocalising: " + text);
                if (Util.isLollipopOrAbove()) {
                    mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, null);
                } else {
                    mTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
                }
            }
        }
        else {
            Log.w(TAG, "TextToSpeech service not yet initialised");
        }
    }

    void doVocalise(String text, UtteranceProgressListener progressListener) {
        mTTS.setOnUtteranceProgressListener(progressListener);
        doVocalise(text);
    }

    void cancelVocalising() {
        mTTS.stop();
    }

    private void setEnableVocalisation(boolean isEnable) {
        mIsEnableVocalisation = isEnable;
        Log.i(TAG, String.format("Vocalisation %s", isEnable ? "enabled" : "disabled"));
    }

    private void setSpeechRate(String speechRateStr) {
        float rate = Float.parseFloat(speechRateStr);
        mTTS.setSpeechRate(rate);

        Log.d(TAG, "Speech rate: " + rate);
    }

//    void refreshLocaleFromPrefs(String localeString) {
//        Log.i(TAG, String.format("Setting vocalisation locale to %s", localeString));
//        mCachedLocalePref = getLocaleFromString(localeString);
//        mTTS.setLanguage(mCachedLocalePref);
//    }
//
//    private static Locale getLocaleFromString(String localeString) {
//        List<String> toks = Arrays.asList(localeString.split("_"));
//        if (toks.size() == 2) {
//            return new Locale(toks.get(0), toks.get(1));
//        }
//        else {
//            Log.d(TAG, "Couldn't find requested locale so setting to default");
//            return Locale.getDefault();
//        }
//    }
}

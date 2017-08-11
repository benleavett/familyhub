package com.benjamjin.familyhub;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

/**
 * Created by benjamin on 09/08/2017.
 */

public class VocaliserService implements OnInitListener {

    private static final String TAG = VocaliserService.class.getSimpleName();

    private TextToSpeech mTTS;
    private boolean mIsTTSReady = false;
    private boolean mIsEnableVocalisation;
    private Locale mCachedLocalePref;

    public VocaliserService(Context context) {
        this(context, Locale.getDefault());
    }

    public VocaliserService(Context context, Locale userLocalePref) {
        mTTS = new TextToSpeech(context, this);
        mIsEnableVocalisation = false;

        Log.d(TAG, "User locale: " + userLocalePref);
        mCachedLocalePref = userLocalePref;
    }

    public void doVocalise(String text) {
        if (mIsTTSReady) {
            if (mIsEnableVocalisation) {
                Log.d(TAG, "Vocalising: " + text);
                mTTS.speak(text, TextToSpeech.QUEUE_ADD, null, null);
            }
        }
        else {
            Log.w(TAG, "TextToSpeech service not yet initialised");
        }
    }

    public void doVocalise(String text, UtteranceProgressListener progressListener) {
        mTTS.setOnUtteranceProgressListener(progressListener);
        doVocalise(text);
    }

    public void stopVocalising() {
        mTTS.stop();
    }

    public void setEnableVocalisation(boolean isEnable) {
        mIsEnableVocalisation = isEnable;
        Log.i(TAG, String.format("Vocalisation %s", isEnable ? "enabled" : "disabled"));
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            Log.e(TAG, "Error initialising TextToSpeech engine");
        } else {
            mIsTTSReady = true;
        }

        mTTS.setLanguage(mCachedLocalePref);
        // Normal rate is 1.0f, this slows it down
        mTTS.setSpeechRate(0.75f);
    }

    public void setLocale(String localeString) {
        Log.i(TAG, String.format("Setting vocalisation locale to %s", localeString));
        mCachedLocalePref = getLocaleFromString(localeString);
        mTTS.setLanguage(mCachedLocalePref);
    }

    private static Locale getLocaleFromString(String localeString) {
        List<String> toks = Arrays.asList(localeString.split("_"));
        if (toks.size() == 2) {
            return new Locale(toks.get(0), toks.get(1));
        }
        else {
            Log.d(TAG, "Couldn't find requested locale so setting to default");
            return Locale.getDefault();
        }
    }

    private static List<Locale> getSupportedLocales() {
        return Arrays.asList(Locale.getAvailableLocales());
    }
}

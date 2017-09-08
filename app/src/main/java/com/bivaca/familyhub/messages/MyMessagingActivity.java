package com.bivaca.familyhub.messages;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.bivaca.familyhub.MyActivity;

public class MyMessagingActivity extends MyActivity {

    private static final String TAG = MyMessagingActivity.class.getSimpleName();

    GestureDetector mGestureDetector;

    protected boolean handleFling(MotionEvent event1, android.view.MotionEvent event2, float velocityX, float velocityY) {
        int dx = (int) (event2.getX() - event1.getX());
        // Don't accept the fling if it's too short as it may conflict with a button push
        if (Math.abs(dx) > 50 && Math.abs(velocityX) > Math.abs(velocityY)) {
            if (velocityX > 0) {
                Log.d(TAG, "Drag right!");
                finish();
            }
            return true;
        } else {
            return false;
        }
    }
}

package com.benjamjin.familyhub.messages;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.benjamjin.familyhub.MyActivity;
import com.benjamjin.familyhub.R;

/**
 * Created by benjamin on 09/08/2017.
 */

public class MyMessagingActivity extends MyActivity {

    private static final String TAG = MyMessagingActivity.class.getSimpleName();

    GestureDetector mGestureDetector;

    protected String getSmsBodyFromTextView() {
        TextView view = (TextView) findViewById(R.id.message_body_text);
        return view.getText().toString();
    }

    protected void populateMesssageViewLayout(String senderName, String sentTime, String body) {
        TextView senderView = (TextView) findViewById(R.id.message_sender_text);
        SpannableString senderNameSpan = new SpannableString(
                String.format("From: %s", senderName));

        senderNameSpan.setSpan(new StyleSpan(Typeface.ITALIC), 0, senderNameSpan.length(), 0);
        senderView.setText(senderNameSpan);

        TextView timestampView = (TextView) findViewById(R.id.message_datetime_text);
        SpannableString sentTimeSpan = new SpannableString(sentTime);
        sentTimeSpan.setSpan(new StyleSpan(Typeface.ITALIC), 0, sentTimeSpan.length(), 0);
        timestampView.setText(sentTimeSpan);

        TextView previewView = (TextView) findViewById(R.id.message_body_text);
        previewView.setText(body);
    }

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

    public void showPreviousActivity(View v) {
        finish();
    }
}

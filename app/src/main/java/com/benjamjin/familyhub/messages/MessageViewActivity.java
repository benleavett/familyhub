package com.benjamjin.familyhub.messages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.benjamjin.familyhub.IntentExtraFields;
import com.benjamjin.familyhub.R;

public class MessageViewActivity extends MyMessagingActivity {

    private static final String TAG = MessageViewActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_view);

        mGestureDetector = new GestureDetector(this, simpleOnGestureListener);

        Intent intent = getIntent();

        populateMesssageViewLayout(
                intent.getStringExtra(IntentExtraFields.SMS_SENDER_NAME),
                intent.getStringExtra(IntentExtraFields.SMS_SENT_TIME),
                intent.getStringExtra(IntentExtraFields.SMS_BODY));

        setResponseButtonsVisibility();

        //TODO add fling gesture to entire layout (if possible)
//        LinearLayout layout = (LinearLayout)findViewById(R.id.message_view_layout);
//        layout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                return mGestureDetector.onTouchEvent(event);
//            }
//        });
    }

    private void setResponseButtonsVisibility() {
        Button btn1 = (Button)findViewById(R.id.response_button_1);
        Button btn2 = (Button)findViewById(R.id.response_button_2);
        Button btn3 = (Button)findViewById(R.id.response_button_3);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isResponseEnabled = sp.getBoolean(getString(R.string.sp_name_enable_replies), false);

        btn1.setVisibility(isResponseEnabled ? View.VISIBLE : View.GONE);
        btn2.setVisibility(isResponseEnabled ? View.VISIBLE : View.GONE);
        btn3.setVisibility(isResponseEnabled ? View.VISIBLE : View.GONE);
    }

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            vocaliseText(getSmsBodyFromTextView());
            super.onLongPress(e);
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            return handleFling(event1, event2, velocityX, velocityY);
        }
    };
}

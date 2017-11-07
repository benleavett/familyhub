package com.bivaca.familyhub.messages;

import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.bivaca.familyhub.R;
import com.google.firebase.analytics.FirebaseAnalytics;

public class ConfirmMessageSentActivity extends AppCompatActivity {
    private static final String TAG = ConfirmMessageSentActivity.class.getSimpleName();

    private String mMessageId;
    private TextView mCountdownText;

    private final int COUNTDOWN_INCREMENTS_MS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_message_sent);

        if (getIntent() != null) {
            String action = getIntent().getAction();

            if (action != null && action.equals(ReplyActivity.MESSAGE_REPLY_INTENT_ACTION_NAME)) {
                mMessageId = getIntent().getStringExtra(InboxActivity.INTENT_KEY_MESSAGE_ID);
            }
        }

        startReturnToInboxTimer();
    }

    private void startReturnToInboxTimer() {
        mCountdownText = (TextView) findViewById(R.id.countdown_timer);
        final String timerText = getString(R.string.countdown_timer_text);

        new CountDownTimer(getResources().getInteger(R.integer.countdown_start_time_ms), COUNTDOWN_INCREMENTS_MS) {
            public void onTick(long msUntilFinished) {
                mCountdownText.setText(String.format(timerText, msUntilFinished / COUNTDOWN_INCREMENTS_MS));
            }

            public void onFinish() {
                mCountdownText.setVisibility(View.INVISIBLE);

                Intent intent = new Intent(getApplicationContext(), InboxActivity.class);
                // Tell InboxActivity which message was replied to
                //FIXME shouldn't really need this check - investigate why is sometimes null
                if (mMessageId != null) {
                    intent.setData(Uri.parse(mMessageId));

                    //FIXME remove when resolved issue. Should match logEvent in InboxActivity
                    FirebaseAnalytics.getInstance(getApplicationContext()).logEvent("message_id_null_confirm", null);
                }
                setResult(InboxActivity.RESULT_REPLY_SENT_OK, intent);
                finish();
            }
        }.start();
    }
}

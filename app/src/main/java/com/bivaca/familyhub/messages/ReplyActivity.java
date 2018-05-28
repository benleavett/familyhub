package com.bivaca.familyhub.messages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bivaca.familyhub.util.FirebaseEventLogger;
import com.bivaca.familyhub.MyActivity;
import com.bivaca.familyhub.R;

import java.util.ArrayList;

public class ReplyActivity extends MyActivity {
    private static final String TAG = ReplyActivity.class.getSimpleName();

    final static String MESSAGE_REPLY_INTENT_ACTION_NAME = "com.bivaca.familyhub.MESSAGE_REPLY_INTENT_ACTION_NAME";
    final static int CONFIRM_SENT_COMPLETE_REQUEST_CODE = 1;

    private String mSenderAddress;
    private String mMessageId;
    private String mFriendlyName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reply);

        setReplyButtonLabels();

        if (getIntent() != null) {
            String action = getIntent().getAction();

            if (action != null && action.equals(MESSAGE_REPLY_INTENT_ACTION_NAME)) {
                mSenderAddress = getIntent().getStringExtra(InboxActivity.INTENT_KEY_SENDER_ADDRESS);
                mMessageId = getIntent().getStringExtra(InboxActivity.INTENT_KEY_MESSAGE_ID);
                mFriendlyName = getIntent().getStringExtra(InboxActivity.FRIENDLY_NAME_KEY);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == CONFIRM_SENT_COMPLETE_REQUEST_CODE) {
            if (resultCode == InboxActivity.RESULT_REPLY_SENT_OK) {
                // Pass result intent straight back to InboxActivity
                setResult(InboxActivity.RESULT_REPLY_SENT_OK, intent);
                finish();
            }
        }
    }

    private void setReplyButtonLabels() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        ArrayList<String> replies = new ArrayList<>();
        replies.add(sp.getString(getString(R.string.sp_name_reply_A), null));
        replies.add(sp.getString(getString(R.string.sp_name_reply_B), null));
        replies.add(sp.getString(getString(R.string.sp_name_reply_C), null));

        Log.d(TAG, replies + "");

        int replyBtnsUsed = 0;
        for (String reply : replies) {
            if (reply != null) {
                TextView btn;

                if (replyBtnsUsed == 0) {
                    btn = (TextView)findViewById(R.id.response_button_1);
                } else if (replyBtnsUsed == 1) {
                    btn = (TextView)findViewById(R.id.response_button_2);
                } else {
                    btn = (TextView)findViewById(R.id.response_button_3);
                }
                btn.setText(reply);
                btn.setVisibility(reply.length() > 0 ? View.VISIBLE : View.GONE);
                replyBtnsUsed++;
            }
        }
    }

    public void sendSmsWithResponse(View v) {
        TextView buttonSelected = (TextView)v;

        String message = buttonSelected.getText().toString();

        //FIXME check that we *actually* inserted the message
        Uri uri = SmsHelper.insertMessageToOutbox(this, mSenderAddress, message);

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(mSenderAddress, null, message, null, null);

        FirebaseEventLogger.logMessageSent(this, mSenderAddress, mFriendlyName, mMessageId);

        notifyUserSmsSent();
    }

    private void notifyUserSmsSent() {
        Intent intent = new Intent(this, ConfirmMessageSentActivity.class);
        intent.setAction(ReplyActivity.MESSAGE_REPLY_INTENT_ACTION_NAME);
        intent.putExtra(InboxActivity.INTENT_KEY_MESSAGE_ID, mMessageId);

        startActivityForResult(intent, CONFIRM_SENT_COMPLETE_REQUEST_CODE);
    }
}

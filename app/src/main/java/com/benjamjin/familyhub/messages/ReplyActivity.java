package com.benjamjin.familyhub.messages;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.benjamjin.familyhub.MyActivity;
import com.benjamjin.familyhub.R;
import com.benjamjin.familyhub.Util;

import java.util.ArrayList;

public class ReplyActivity extends MyActivity {

    private static final String TAG = ReplyActivity.class.getSimpleName();

    final static String MESSAGE_REPLY_INTENT_ACTION_NAME = "com.benjamjin.familyhub.MESSAGE_REPLY_INTENT_ACTION_NAME";
    private String mSenderAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reply);

        setReplyButtonLabels();

        if (getIntent() != null) {
            String action = getIntent().getAction();

            if (action != null && action.equals(MESSAGE_REPLY_INTENT_ACTION_NAME)) {
                mSenderAddress = getIntent().getStringExtra("senderAddress");
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

        if (!verifyRequiredSmsFields()) {
            return;
        }

        Uri uri = SmsHelper.insertMessageToOutbox(this, mSenderAddress, message);

        Util.log(TAG, "Placed message in outbox", uri);

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(mSenderAddress, null, message, null, null);
    }

    private boolean verifyRequiredSmsFields() {
        //TODO
        return true;
    }
}

package com.bivaca.familyhub.messages;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bivaca.familyhub.MyActivity;
import com.bivaca.familyhub.R;
import com.bivaca.familyhub.Util;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

public class InboxActivity extends MyActivity implements View.OnLongClickListener, OnInboxLoadedListener {

    private static final String TAG = InboxActivity.class.getSimpleName();

    final static String NEW_SMS_INTENT_ACTION_NAME = "com.benjamjin.familyhub.NEW_SMS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inbox);

        // Check we have right permissions
        if (getMyApplication().verifySmsPermissions(this)) {
            Inbox.getInstance().initInbox(this);
        }

        // If we're creating this activity in response to a new SMS...
        if (getIntent() != null) {
            Log.d(TAG, "onCreate for new intent");
            handleNewSmsReceived(getIntent(), true);
        }

        refreshResponseButtonsVisibilityFromPrefs();
        setLongPressListeners();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");

        // Add new SMS but don't update current selected message (as user may be viewing another SMS)
        handleNewSmsReceived(intent, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (super.handleRequestPermissionsResult(requestCode, permissions, grantResults)) {
            Inbox.getInstance().initInbox(this);
        } else {
            finish();
        }
    }

    @Override
    public boolean onLongClick(View view) {
        final int id = view.getId();

        if (id == R.id.message_body_view || id == R.id.message_sender_text) {
            BasicSms sms = Inbox.getInstance().getCurrentMessage();
            final String textToVocalise = String.format("From %s. %s", sms.friendlySenderName, sms.body);
            Log.d(TAG, "Sending text to vocalise: " + textToVocalise);
            getMyApplication().doVocalise(textToVocalise);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void showPreviousActivity(View v) {
        markCurrentMessageAsRead();

        super.showPreviousActivity(v);
    }

    @Override
    public void onInboxLoaded() {
        Log.d(TAG, "Inbox loaded");
        viewCurrentMessage();
    }

    private void handleNewSmsReceived(Intent intent, boolean isResetCurrentMessageToLatest) {
        String action = intent.getAction();
        if (action != null && action.equals(NEW_SMS_INTENT_ACTION_NAME)) {

            BasicSms sms = new BasicSms(
                    intent.getLongExtra("timestamp", -1),
                    intent.getStringExtra("senderAddress"),
                    intent.getStringExtra("body"),
                    SmsHelper.getIdFromUri(intent.getStringExtra("uri")));

            Log.d(TAG, String.format("Handling new sms (%s) - %s", sms, isResetCurrentMessageToLatest));

            if (Inbox.getInstance().handleNewSmsReceived(sms)) {
                // When we show the inbox, show the newly added sms
                if (isResetCurrentMessageToLatest) {
                    Inbox.getInstance().resetInboxIteratorToLatest();
                }

                viewCurrentMessage();

                notifyUserNewSms();
            }
        }
    }

    private void notifyUserNewSms() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.inbox_layout);
        Snackbar.make(layout, "You've received a new message", Snackbar.LENGTH_LONG).show();
    }

    private void setLongPressListeners() {
        TextView body = (TextView) findViewById(R.id.message_body_view);
        body.setOnLongClickListener(this);

        TextView sender = (TextView) findViewById(R.id.message_sender_text);
        sender.setOnLongClickListener(this);
    }

    private void viewCurrentMessage() {
        final BasicSms currentSms = Inbox.getInstance().getCurrentMessage();

        if (currentSms == null) {
            populateMessageViewLayoutForEmptyInbox();
        } else {
            Log.d(TAG, "Current SMS: " + currentSms);

            populateMessageViewLayout(currentSms);

            refreshMessageSelectorButtonsVisibility();
        }
    }

    private void populateMessageViewLayoutForEmptyInbox() {
        TextView isUnreadState = (TextView) findViewById(R.id.message_status);
        isUnreadState.setVisibility(View.INVISIBLE);

        TextView senderView = (TextView) findViewById(R.id.message_sender_text);
        senderView.setVisibility(View.INVISIBLE);

        TextView timestampView = (TextView) findViewById(R.id.message_datetime_text);
        timestampView.setVisibility(View.INVISIBLE);

        TextView previewView = (TextView) findViewById(R.id.message_body_view);
        previewView.setText(getString(R.string.empty_inbox_message));

        Button earlierBtn = (Button) findViewById(R.id.select_earlier_message_button);
        earlierBtn.setVisibility(View.GONE);
        Button laterBtn = (Button) findViewById(R.id.select_later_message_button);
        laterBtn.setVisibility(View.GONE);

        Button replyBtn = (Button) findViewById(R.id.reply_button);
        replyBtn.setVisibility(View.GONE);
    }

    private void setMessageStatusIndicator(final BasicSms sms) {
        TextView messageStatusView = (TextView) findViewById(R.id.message_status);

        if (!sms.isRead) {
            messageStatusView.setText(getString(R.string.message_unread_text));
            messageStatusView.setVisibility(View.VISIBLE);
            if (Util.isLollipopOrAbove()) {
                messageStatusView.setBackground(getDrawable(R.drawable.unread_state_background));
            } else {
                ResourcesCompat.getDrawable(getResources(), R.drawable.unread_state_background, null);
            }
        }/* else if (sms.hasReplied) {
        //TODO handle 'has replied' message state
            messageStatusView.setText(getString(R.string.message_replied_text));
            messageStatusView.setVisibility(View.VISIBLE);
            messageStatusView.setBackground(getDrawable(R.drawable.replied_state_background));
        }*/ else {
            messageStatusView.setVisibility(View.GONE);
        }
    }

    private void populateMessageViewLayout(final BasicSms sms) {
        setMessageStatusIndicator(sms);

        // Set sender name
        TextView senderView = (TextView) findViewById(R.id.message_sender_text);
        // Italicise
        SpannableString senderNameSpan = new SpannableString(String.format("From: %s", sms.friendlySenderName));
        senderNameSpan.setSpan(new StyleSpan(Typeface.ITALIC), 0, senderNameSpan.length(), 0);
        senderView.setText(senderNameSpan);
        senderView.setVisibility(View.VISIBLE);

        // Set message sent time
        TextView timestampView = (TextView) findViewById(R.id.message_datetime_text);
        final String sentTime = getPrettyStringFromTimestamp(sms.timestampSent);
        // Italicise
        SpannableString sentTimeSpan = new SpannableString(sentTime);
        sentTimeSpan.setSpan(new StyleSpan(Typeface.ITALIC), 0, sentTimeSpan.length(), 0);
        timestampView.setText(sentTimeSpan);
        timestampView.setVisibility(View.VISIBLE);

        // Set message body
        TextView previewView = (TextView) findViewById(R.id.message_body_view);
        previewView.setText(sms.body);
    }

    private void setMessageSelectorButtonEnabled(boolean isEnabled, int id) {
        Button btn = (Button) findViewById(id);
        btn.setEnabled(isEnabled);
    }

    private boolean isMessageSelectorButtonDisabled(int id) {
        Button btn = (Button) findViewById(id);
        return !btn.isEnabled();
    }

    private static String getPrettyStringFromTimestamp(Long timestamp) {
        return new PrettyTime().format(new Date(timestamp));
    }

    public void previewLaterMessage(View v) {
        if (isMessageSelectorButtonDisabled(R.id.select_earlier_message_button)) {
            setMessageSelectorButtonEnabled(true, R.id.select_earlier_message_button);
        }

        markCurrentMessageAsRead();

        Inbox.getInstance().moveCursorToLaterMessage();
        viewCurrentMessage();
    }

    public void previewEarlierMessage(View v) {
        if (isMessageSelectorButtonDisabled(R.id.select_later_message_button)) {
            setMessageSelectorButtonEnabled(true, R.id.select_later_message_button);
        }

        markCurrentMessageAsRead();

        Inbox.getInstance().moveCursorToEarlierMessage();
        viewCurrentMessage();
    }

    private void markCurrentMessageAsRead() {
        Inbox.getInstance().markMessageAsRead(this, Inbox.getInstance().getCurrentMessage());
    }

    private void refreshMessageSelectorButtonsVisibility() {
        setMessageSelectorButtonEnabled(Inbox.getInstance().hasEarlierMessage(), R.id.select_earlier_message_button);
        setMessageSelectorButtonEnabled(Inbox.getInstance().hasLaterMessage(), R.id.select_later_message_button);
    }

    private void refreshResponseButtonsVisibilityFromPrefs() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isResponseEnabled = sp.getBoolean(getString(R.string.sp_name_enable_replies), false);

        Button replyBtn = (Button) findViewById(R.id.reply_button);
        replyBtn.setVisibility(isResponseEnabled ? View.VISIBLE : View.GONE);
    }

    public void showReplyActivity(View v) {
        markCurrentMessageAsRead();

        Intent intent = new Intent(this, ReplyActivity.class);
        intent.setAction(ReplyActivity.MESSAGE_REPLY_INTENT_ACTION_NAME);
        intent.putExtra("senderAddress", Inbox.getInstance().getCurrentMessage().senderAddress);
        startActivity(intent);
    }
}


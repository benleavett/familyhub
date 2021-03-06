package com.bivaca.familyhub.messages;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.bivaca.familyhub.MyActivity;
import com.bivaca.familyhub.R;
import com.bivaca.familyhub.util.SharedPrefsHelper;
import com.bivaca.familyhub.util.Util;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

public class InboxActivity extends MyActivity implements View.OnLongClickListener, OnInboxLoadedListener {

    private static final String TAG = InboxActivity.class.getSimpleName();

    final static String INTENT_ACTION_NAME_NEW_SMS = "com.benjamjin.familyhub.NEW_SMS";
    final static String INTENT_KEY_SENDER_ADDRESS = "sender_address";
    final static String INTENT_KEY_URI = "uri";
    final static String INTENT_KEY_BODY = "body";
    final static String INTENT_KEY_TIMESTAMP = "timestamp";
    final static String INTENT_KEY_MESSAGE_ID = "msg_id";

    final static String FRIENDLY_NAME_KEY = "friendly_name";

    final static int REPLY_COMPLETE_REQUEST_CODE = 1;
    final static int RESULT_REPLY_SENT_OK = 100;

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

        setLongPressListeners();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");

        handleNewSmsReceived(intent, true);
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
        super.showPreviousActivity(v);

        markCurrentMessageAsRead();
    }

    @Override
    public void onInboxLoaded() {
        Log.d(TAG, "Inbox loaded");
        viewCurrentMessage();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REPLY_COMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_REPLY_SENT_OK) {
                String messageId = intent.getData().toString();

                if (Inbox.getInstance().getCurrentMessage().id.equals(messageId)) {
                    Inbox.getInstance().markCurrentMessageAsReplied(this);
                }
            }

            if (!Inbox.getInstance().isEmpty()) {
                populateMessageViewLayout(Inbox.getInstance().getCurrentMessage());
            } else {
                populateMessageViewLayoutForEmptyInbox();
            }
        }
    }

    private void handleNewSmsReceived(Intent intent, boolean isShowLatestMessage) {
        String action = intent.getAction();
        if (action != null && action.equals(INTENT_ACTION_NAME_NEW_SMS)) {

            final String senderAddress = intent.getStringExtra(INTENT_KEY_SENDER_ADDRESS);

            if (AcceptedContacts.getInstance().isAcceptedContact(senderAddress)) {
                final String contactName = AcceptedContacts.getInstance().getContactName(senderAddress);

                final BasicSms sms = new BasicSms(
                        intent.getLongExtra(INTENT_KEY_TIMESTAMP, -1),
                        senderAddress,
                        contactName,
                        intent.getStringExtra(INTENT_KEY_BODY),
                        SmsHelper.getIdFromUri(intent.getStringExtra(INTENT_KEY_URI)));

                Log.d(TAG, String.format("Handling new sms (%s) - %s", sms, isShowLatestMessage));

                Inbox.getInstance().addNewSms(sms);

                // When we show the inbox, show the newly added sms
                if (isShowLatestMessage) {
                    Inbox.getInstance().resetInboxIteratorToLatest();
                }

                viewCurrentMessage();
            } else {
                Log.d(TAG, "Received message from unknown sender");
            }
        }
    }

    private void setLongPressListeners() {
        TextView body = findViewById(R.id.message_body_view);
        body.setOnLongClickListener(this);

        TextView sender = findViewById(R.id.message_sender_text);
        sender.setOnLongClickListener(this);
    }

    private void viewCurrentMessage() {
        final BasicSms currentSms = Inbox.getInstance().getCurrentMessage();

        if (currentSms == null) {
            populateMessageViewLayoutForEmptyInbox();
        } else {
            Log.d(TAG, "Current SMS: " + currentSms);

            populateMessageViewLayout(currentSms);
        }
    }

    private void populateMessageViewLayoutForEmptyInbox() {
        setUnreadStatusIndicator(false);

        TextView senderView = findViewById(R.id.message_sender_text);
        senderView.setVisibility(View.INVISIBLE);

        TextView timestampView = findViewById(R.id.message_datetime_text);
        timestampView.setVisibility(View.INVISIBLE);

        TextView previewView = findViewById(R.id.message_body_view);
        previewView.setText(getString(R.string.empty_inbox_message));

        Button earlierBtn = findViewById(R.id.select_earlier_message_button);
        earlierBtn.setVisibility(View.GONE);
        Button laterBtn = findViewById(R.id.select_later_message_button);
        laterBtn.setVisibility(View.GONE);

        setReplyButtonStateForMessage(null);
    }

    private void setUnreadStatusIndicator(final boolean isUnread) {
        TextView isUnreadState = findViewById(R.id.unread_state);

        if (isUnread) {
            isUnreadState.setText(getString(R.string.message_unread_text));
            isUnreadState.setVisibility(View.VISIBLE);
            if (Util.isLollipopOrAbove()) {
                isUnreadState.setBackground(getDrawable(R.drawable.unread_state_background));
            } else {
                ResourcesCompat.getDrawable(getResources(), R.drawable.unread_state_background, null);
            }

            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(750);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            isUnreadState.startAnimation(anim);

            // Hide this status indicator when user interacts with the view
            View view = findViewById(R.id.scroller_message_view);
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    Log.d(TAG, "TOUCHED");
                    setUnreadStatusIndicator(false);
                    return false;
                }
            });

        } else {
            isUnreadState.setVisibility(View.GONE);
            isUnreadState.clearAnimation();

            // Remove touch listener
            View view = findViewById(R.id.scroller_message_view);
            view.setOnTouchListener(null);
        }
    }

    private void populateMessageViewLayout(@NonNull final BasicSms sms) {
        if (sms == null) {
            Log.e(TAG, "populateMessageViewLayout called with null BasicSms");
        }

        setUnreadStatusIndicator(!sms.isRead);

        // Set sender name
        TextView senderView = findViewById(R.id.message_sender_text);
        // Italicise
        SpannableString senderNameSpan = new SpannableString(String.format("From %s", sms.friendlySenderName));
        senderNameSpan.setSpan(new StyleSpan(Typeface.ITALIC), 0, senderNameSpan.length(), 0);
        senderView.setText(senderNameSpan);
        senderView.setVisibility(View.VISIBLE);

        // Set message sent time
        TextView timestampView = findViewById(R.id.message_datetime_text);
        final String sentTime = getPrettyStringFromTimestamp(sms.timestampSent);
        // Italicise
        SpannableString sentTimeSpan = new SpannableString(sentTime);
        sentTimeSpan.setSpan(new StyleSpan(Typeface.ITALIC), 0, sentTimeSpan.length(), 0);
        timestampView.setText(sentTimeSpan);
        timestampView.setVisibility(View.VISIBLE);

        // Set message body
        TextView previewView = findViewById(R.id.message_body_view);
        previewView.setText(sms.body);

        Button earlierBtn = findViewById(R.id.select_earlier_message_button);
        earlierBtn.setVisibility(View.VISIBLE);
        Button laterBtn = findViewById(R.id.select_later_message_button);
        laterBtn.setVisibility(View.VISIBLE);

        refreshMessageSelectorButtonsEnabled();

        setReplyButtonStateForMessage(sms);
    }

    private void setMessageSelectorButtonEnabled(boolean isEnabled, int id) {
        Button btn = findViewById(id);
        btn.setEnabled(isEnabled);
    }

    private boolean isMessageSelectorButtonDisabled(int id) {
        Button btn = findViewById(id);
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

    private void refreshMessageSelectorButtonsEnabled() {
        setMessageSelectorButtonEnabled(Inbox.getInstance().hasEarlierMessage(), R.id.select_earlier_message_button);
        setMessageSelectorButtonEnabled(Inbox.getInstance().hasLaterMessage(), R.id.select_later_message_button);
    }

    private void setReplyButtonStateForMessage(final BasicSms sms) {
        // Hide reply button if we haven't got an SMS to show for it, otherwise show button if appropriate depending on shared-pref state
        final boolean isShowReplyButton = sms != null && SharedPrefsHelper.isRepliesEnabled(this);

        Button replyBtn = findViewById(R.id.reply_button);

        replyBtn.setVisibility(isShowReplyButton ? View.VISIBLE : View.GONE);

        if (sms != null) {
            // Disable button if message has been replied to (but still show)
            replyBtn.setEnabled(!sms.isRepliedTo);
            // Show 'replied-to' text if necessary, otherwise use default
            replyBtn.setText(sms.isRepliedTo ? R.string.reply_button_text_replied : R.string.reply_button_text_default);
        }
    }

    public void showReplyActivity(View v) {
        markCurrentMessageAsRead();

        final BasicSms sms = Inbox.getInstance().getCurrentMessage();

        Log.d(TAG, "Replying to: " + sms);

        Intent intent = new Intent(this, ReplyActivity.class);
        intent.setAction(ReplyActivity.MESSAGE_REPLY_INTENT_ACTION_NAME);
        intent.putExtra(INTENT_KEY_SENDER_ADDRESS, sms.senderAddress);
        intent.putExtra(INTENT_KEY_MESSAGE_ID, sms.id);
        intent.putExtra(FRIENDLY_NAME_KEY, sms.friendlySenderName);

        //FIXME remove when investigated why message ids become null
        if (sms.id == null) {
            Bundle bundle = new Bundle();
            bundle.putString("friendlySenderName", sms.friendlySenderName);
            FirebaseAnalytics.getInstance(this).logEvent("message_id_null_inbox", bundle);
        }

        startActivityForResult(intent, REPLY_COMPLETE_REQUEST_CODE);
    }
}


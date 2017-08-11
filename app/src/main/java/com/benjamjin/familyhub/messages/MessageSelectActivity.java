package com.benjamjin.familyhub.messages;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.benjamjin.familyhub.IntentExtraFields;
import com.benjamjin.familyhub.R;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MessageSelectActivity extends MyMessagingActivity {

    private static final String TAG = MessageSelectActivity.class.getSimpleName();

    private static String[] SMS_PERMISSIONS = new String[]{
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS};

    private BasicSms mSelectedSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message_selection);

        if (doRequestPermissions()) {
            loadInboxAndInitView();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int index = 0;
        HashMap<String, Integer> PermissionsMap = new HashMap<>();
        for (String permission : permissions) {
            PermissionsMap.put(permission, grantResults[index]);
            index++;
        }

        if (PermissionsMap.get(Manifest.permission.RECEIVE_SMS) != 0
                || PermissionsMap.get(Manifest.permission.READ_SMS) != 0) {
            Toast.makeText(this, "SMS permissions are required", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            loadInboxAndInitView();
        }
    }

    private void loadInboxAndInitView() {

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        // Load inbox
        Inbox.getInstance().initInboxFromCursor(cursor);

        viewSelectedMessage(Inbox.getInstance().getCurrentMessage());
    }

    private void viewSelectedMessage(BasicSms sms) {

        if (sms != null) {
            mSelectedSms = sms;

            Log.d(TAG, "Latest SMS: " + mSelectedSms);

            populateMesssageViewLayout(
                    AcceptedContacts.getInstance().getContactName(sms.senderAddress),
                    getPrettyStringFromTimestamp(sms.timestampSent),
                    sms.body);

            refreshMessageSelectorButtonsVisibility();
        }
    }

    private void setMessageSelectorButtonEnabled(boolean isEnabled, int id) {
        Button btn = (Button)findViewById(id);
        btn.setEnabled(isEnabled);
    }

    private boolean isMessageSelectorButtonEnabled(int id) {
        Button btn = (Button)findViewById(id);
        return btn.isEnabled();
    }

    private static String getPrettyStringFromTimestamp(String timestampStr) {
        PrettyTime p = new PrettyTime();
        return p.format(new Date(Long.parseLong(timestampStr)));
    }

    public void previewLaterMessage(View v) {
        if (!isMessageSelectorButtonEnabled(R.id.select_earlier_message_button)) {
            setMessageSelectorButtonEnabled(true, R.id.select_earlier_message_button);
        }

        viewSelectedMessage(Inbox.getInstance().moveCursorToLaterMessage());
    }

    public void previewEarlierMessage(View v) {
        if (!isMessageSelectorButtonEnabled(R.id.select_later_message_button)) {
            setMessageSelectorButtonEnabled(true, R.id.select_later_message_button);
        }

        viewSelectedMessage(Inbox.getInstance().moveCursorToEarlierMessage());
    }

    private void refreshMessageSelectorButtonsVisibility() {

        if (!Inbox.getInstance().hasEarlierMessage()) {
            setMessageSelectorButtonEnabled(false, R.id.select_earlier_message_button);
        }

        if (!Inbox.getInstance().hasLaterMessage()) {
            setMessageSelectorButtonEnabled(false, R.id.select_later_message_button);
        }
    }

    public void showViewMessageActivity(View v) {
        Intent messageViewIntent = new Intent(this, MessageViewActivity.class);

        messageViewIntent.putExtra(
                IntentExtraFields.SMS_SENDER_NAME, AcceptedContacts.getInstance().getContactName(mSelectedSms.senderAddress));

        messageViewIntent.putExtra(
                IntentExtraFields.SMS_SENT_TIME, getPrettyStringFromTimestamp(mSelectedSms.timestampSent));

        messageViewIntent.putExtra(
                IntentExtraFields.SMS_BODY, mSelectedSms.body);

        startActivity(messageViewIntent);
    }

    /**
     * @return False if this triggers a permission request, else true (ie we have the permissions we need).
     */
    private boolean doRequestPermissions() {
        int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
        List<String> listPermissionsNeeded = new ArrayList<>();

        int result;
        for (String p : SMS_PERMISSIONS) {
            result = ContextCompat.checkSelfPermission(this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (listPermissionsNeeded.isEmpty()) {
            return true;
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
    }
}


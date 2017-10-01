package com.bivaca.familyhub.messages;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bivaca.familyhub.R;
import com.bivaca.familyhub.SharedPrefsHelper;

import java.util.LinkedList;
import java.util.List;

public class Inbox {
    private static final String TAG = Inbox.class.getSimpleName();

    private static final Inbox mInstance = new Inbox();

    private LinkedList<BasicSms> mMessages = new LinkedList<>();
    private int mInboxIndex = -1;

    private Inbox() {}

    public static Inbox getInstance() { return mInstance; }

    /*
        Order of messages is:
        0 ..................... n
        Earliest message        Latest message
     */
    void initInbox(Context context) {//ContentResolver contentResolver) {
        Log.d(TAG, "Init");
        OnInboxLoadedListener listener = (OnInboxLoadedListener)context;

        if (mInboxIndex == -1) {
            Log.d(TAG, "Init - actual");
            mMessages.clear();

            collectSmsAndAddToInbox(context, listener);
        } else {
            resetInboxIteratorToLatest();

            listener.onInboxLoaded();
        }
    }

    void resetInboxIteratorToLatest() {
        Log.d(TAG, "RESETTING ITERATOR");
        if (isEmpty()) {
            mInboxIndex = -1;
        } else {
            mInboxIndex = mMessages.size() - 1;
        }
    }

    BasicSms getCurrentMessage() {
        if (mInboxIndex >= 0) {
            return mMessages.get(mInboxIndex);
        } else {
            return null;
        }
    }

    void moveCursorToLaterMessage() {
        if (hasLaterMessage()) {
            mInboxIndex++;
        }
    }

    void moveCursorToEarlierMessage() {
        if (hasEarlierMessage()) {
            mInboxIndex--;
        }
    }

    boolean hasLaterMessage() {
        return mInboxIndex+1 < mMessages.size();
    }

    boolean hasEarlierMessage() {
        return mInboxIndex > 0;
    }

    boolean handleNewSmsReceived(BasicSms sms) {
        final String friendlyName = AcceptedContacts.getInstance().getContactName(sms.senderAddress);

        if (friendlyName != null) {
            sms.friendlySenderName = friendlyName;

            // Add to end of list (latest messages at end of list)
            mMessages.add(sms);

            Log.d(TAG, "ADDED new SMS to local store " + sms);
            return true;
        }
        else {
            Log.d(TAG, "Found new SMS from invalid contact " + sms);
            return false;
        }
    }

    void markMessageAsRead(final Activity activity, final BasicSms sms) {
        if (sms == null || sms.isRead) {
            return;
        }

        final int inboxIndex = mInboxIndex;
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... a) {
//        final String msgId = getMsgIdFromSmsDatabase(contentResolver, currentSms);
                if (!sms.isRead) {
                    if (!SmsHelper.markMessageAsRead(activity.getApplicationContext(), sms)) {
                        return false;
                    }
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean isSuccess) {
                if (isSuccess) {
                    sms.isRead = true;
                    mMessages.set(inboxIndex, sms);

                    // If we're still looking at the message we just marked as 'read'...
                    if (mInboxIndex == inboxIndex) {
                        // Show unread state if appropriate
                        TextView isUnreadState = activity.findViewById(R.id.unread_state);
                        isUnreadState.setVisibility(View.GONE);
                    }

                    Log.d(TAG, "Marked message as read. " + sms.id);
                } else {
                    Log.e(TAG, "Failed to set message as read. " + sms.id);
                }
            }
        }.execute();
    }

    private void collectSmsAndAddToInbox(final Context context, OnInboxLoadedListener listener) {
        new InboxLoadTask(listener) {
            @Override
            protected List<BasicSms> doInBackground(Void... voids) {
                return SmsHelper.getAllMessages(context);
            }

            protected void onPostExecute(List<BasicSms> result) {
                setInboxContents(context, result);

                resetInboxIteratorToLatest();

                super.onPostExecute(result);
            }
        }.execute();
    }

    private void setInboxContents(Context context, List<BasicSms> result) {
        for (BasicSms sms : result) {
            sms.isRepliedTo = SharedPrefsHelper.getMessageRepliedStateFromDisk(context, sms.id);
        }

        mMessages = new LinkedList<>(result);
    }

    public boolean clearInbox(final Context context) {
        if (!isEmpty()) {
            if (SmsHelper.clearInbox(context) == 0) {
                return false;
            }

            mMessages.clear();
            resetInboxIteratorToLatest();

            SharedPrefsHelper.deleteAllMessageRepliedStatesOnDisk(context);
        }
        return true;
    }

    public void markCurrentMessageAsReplied(Context context) {
        BasicSms sms = mMessages.get(mInboxIndex);
        if (sms != null) {
            sms.isRepliedTo = true;
            mMessages.set(mInboxIndex, sms);

            SharedPrefsHelper.writeMessageRepliedStateToDisk(context, sms.id);
        }
    }

    public boolean isEmpty() {
        return mMessages.size() == 0;
    }
}

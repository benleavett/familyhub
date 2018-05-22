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

    private boolean mHideRepliedToMessages = false;

    private Inbox() {}

    public static Inbox getInstance() { return mInstance; }

    /*
        Order of messages is:
        0 ..................... n
        Earliest message        Latest message
     */
    void initInbox(Context context, boolean hideRepliedToMessages) {
        Log.d(TAG, "Init");
        OnInboxLoadedListener listener = (OnInboxLoadedListener)context;

        if (mInboxIndex == -1 || hideRepliedToMessages != mHideRepliedToMessages) {
            Log.d(TAG, "Init - actual");

            mHideRepliedToMessages = hideRepliedToMessages;
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

    void addNewSms(BasicSms sms) {
        // Add to end of list (latest messages at end of list)
        mMessages.add(sms);

        Log.d(TAG, "ADDED new SMS to local store " + sms);
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
        mMessages = new LinkedList<>();

        for (BasicSms sms : result) {
            sms.isRepliedTo = SharedPrefsHelper.getMessageRepliedStateFromDisk(context, sms.id);

            if (mHideRepliedToMessages) {
                // Only add to inbox if the message hasn't been replied to yet
                if (!sms.isRepliedTo) {
                    mMessages.push(sms);
                } else {
                    Log.d(TAG, "Hiding replied-to message from inbox " + sms.id);
                }
            } else {
                // Always add message to inbox
                mMessages.push(sms);
            }
        }
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
            // Mark message as read
            sms.isRepliedTo = true;
            mMessages.set(mInboxIndex, sms);

            SharedPrefsHelper.writeMessageRepliedStateToDisk(context, sms.id);

            // Hide message and show previous if this shared-pref is enabled
            if (mHideRepliedToMessages) {
                Log.d(TAG, "Hiding replied-to message from inbox " + sms.id);

                removeCurrentMessage();
            }
        }
    }

    public boolean isEmpty() {
        return mMessages.size() == 0;
    }

    public void removeCurrentMessage() {
        if (mInboxIndex >= 0) {
            mMessages.remove(mInboxIndex);
        }

        mInboxIndex--;
    }
}

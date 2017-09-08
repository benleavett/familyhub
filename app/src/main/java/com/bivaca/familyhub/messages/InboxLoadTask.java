package com.bivaca.familyhub.messages;

import android.os.AsyncTask;

import java.util.List;

class InboxLoadTask extends AsyncTask<Void, Void, List<BasicSms>> {
    private final OnInboxLoadedListener mListener;

    InboxLoadTask(OnInboxLoadedListener listener) {
        mListener = listener;
    }

    @Override
    protected List<BasicSms> doInBackground(Void... voids) {
        return null;
    }

    @Override
    protected void onPostExecute(List<BasicSms> result) {
        mListener.onInboxLoaded();
    }
}

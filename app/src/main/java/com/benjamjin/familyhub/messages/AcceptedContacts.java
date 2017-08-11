package com.benjamjin.familyhub.messages;

import android.util.Log;

import java.util.HashMap;

/**
 * Created by benjamin on 19/08/2017.
 */

class AcceptedContacts {

    private static AcceptedContacts mInstance;
    private static HashMap<String, String> mAcceptedContacts = new HashMap<>();

    private AcceptedContacts() {
        load();
    }

    protected static AcceptedContacts getInstance() {
        if (mInstance == null) {
            mInstance = new AcceptedContacts();
        }
        return mInstance;
    }

    private static void load() {
        mAcceptedContacts.put("+447870757202", "Andrew");
        mAcceptedContacts.put("+44", "Richard");
        mAcceptedContacts.put("+447733024940", "Jenny");
        mAcceptedContacts.put("+447754741953", "Ben");
        mAcceptedContacts.put("+4915737625757", "Ruth");

//        Log.d(TAG, mAcceptedContacts.toString());
    }

    protected static String getContactName(String phoneNumber) {
        return mAcceptedContacts.get(phoneNumber);
    }

    protected static boolean isAcceptedContact(String phoneNumber) {
        return mAcceptedContacts.containsKey(phoneNumber);
    }
}

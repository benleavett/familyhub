package com.bivaca.familyhub.messages;

import java.util.HashMap;

class AcceptedContacts {

    private static final AcceptedContacts mInstance = new AcceptedContacts();

    private final HashMap<String, String> mAcceptedContacts = new HashMap<>();

    private AcceptedContacts() {
        load();
    }

    static AcceptedContacts getInstance() { return mInstance; }

    private void load() {
        mAcceptedContacts.put("+447870757202", "Andrew");
        mAcceptedContacts.put("+44", "Richard");
        mAcceptedContacts.put("+447733024940", "Jenny");
        mAcceptedContacts.put("+447754741953", "Ben");
        mAcceptedContacts.put("+4915737625757", "Ruth");
    }

    String getContactName(String phoneNumber) {
        return mAcceptedContacts.get(phoneNumber);
    }

    boolean isAcceptedContact(String phoneNumber) {
        return mAcceptedContacts.containsKey(phoneNumber);
    }

//    void addContact(String phoneNumber, String friendlyName) {
//        mAcceptedContacts.put(phoneNumber.replace(" ", "").trim(), friendlyName);
//    }
//
//    void removeContact(String phoneNumber) {
//        if (mAcceptedContacts.remove(phoneNumber.replace(" ", "").trim()) == null) {
//            //FIXME throw exception
////            throw new Exception("Failed to find (or remove) specified contact");
//        }
//    }
}

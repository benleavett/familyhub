package com.bivaca.familyhub.photos;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by benjamin on 27/05/2018.
 */

class PhotoQueue extends ArrayList<File> {
    private static final String TAG = PhotoQueue.class.getSimpleName();

    private HashMap<File, FileStatus> fileStatus = new HashMap<>();

    enum FileStatus { ACTIVE, MARK_FOR_DELETION }

    @Override
    public File get(int iter) {
//        if (size() > 0) {
            final int itemIndex = iter % size();
            Log.d(TAG, "Returning item from queue: " + itemIndex);
            return super.get(itemIndex);
//        } else {
//            return null;
//        }
    }

    //TODO use this method to decide which image to show next
    public File getRandom() {
        final int itemIndex = new Random().nextInt() % size();
        Log.d(TAG, "Returning random item from queue: " + itemIndex);
        return super.get(itemIndex);
    }

    @Override
    public boolean add(File file) {
        Log.d(TAG, "Adding file to queue: " + file);
        fileStatus.put(file, FileStatus.ACTIVE);
        return super.add(file);
    }

    public void markFileForDeletion(File file) {
        Log.d(TAG, "Marking file for deletion: " + file);
        fileStatus.put(file, FileStatus.MARK_FOR_DELETION);
    }

    public boolean isMarkedForDeletion(File file) {
        return fileStatus.get(file) == FileStatus.MARK_FOR_DELETION;
    }

//    @Override
//    public int size() {
//        int count = 0;
//        for (FileStatus status : fileStatus.values()) {
//            if (status == FileStatus.ACTIVE) {
//                count++;
//            }
//        }
//
//        return count;
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return size() == 0;
//    }
}

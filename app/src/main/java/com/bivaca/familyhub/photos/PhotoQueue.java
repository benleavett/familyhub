package com.bivaca.familyhub.photos;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by benjamin on 27/05/2018.
 */

class PhotoQueue extends ArrayList<File> {
    private static final String TAG = PhotoQueue.class.getSimpleName();

    private HashMap<File, FileStatus> fileStatus = new HashMap<>();
    private int currentIndex = 0;

    enum FileStatus { ACTIVE, MARK_FOR_DELETION }

    @Override
    public File get(int iter) {
        final int itemIndex = iter % size();
        Log.d(TAG, "Returning item from queue: " + itemIndex);
        return super.get(itemIndex);
    }

    public File getNext() {
        return get(currentIndex++);
    }

    public File getNextRandom() {
        int randomIndex = new Random().nextInt(size());
        return get(randomIndex);
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
}

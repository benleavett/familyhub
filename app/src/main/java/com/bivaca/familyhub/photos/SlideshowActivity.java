package com.bivaca.familyhub.photos;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bivaca.familyhub.R;
import com.bivaca.familyhub.MyActivity;
import com.bivaca.familyhub.util.SharedPrefsHelper;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.google.common.io.Files;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SlideshowActivity extends MyActivity {
    private static final String TAG = SlideshowActivity.class.getSimpleName();

    private static final String LOCAL_DOWNLOAD_DIR_NAME = "photos";
    private static final ArrayList<String> SUPPORTED_IMAGE_FORMATS = new ArrayList<>(Arrays.asList("jpg"));

    private static final AtomicReference<PhotoQueue> photoQueue = new AtomicReference<>();
    private static final AtomicBoolean terminateSlideShow = new AtomicBoolean(false);

    enum LoadingState { DOWNLOADING, NO_PHOTOS_AVAILABLE, PHOTOS_AVAILABLE}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_slideshow);
        super.onCreate(savedInstanceState);

        setTypefaces();

        photoQueue.set(new PhotoQueue());
    }

    private void setTypefaces() {
        Typeface typeface = Typeface.createFromAsset(getAssets(), "MuseoSans_500.otf");

        TextView dateDescription = findViewById(R.id.photo_time_description);
        dateDescription.setTypeface(typeface);

        TextView loadingText = findViewById(R.id.photos_loading_text);
        loadingText.setTypeface(typeface);

        TextView errorText = findViewById(R.id.no_photos_available);
        errorText.setTypeface(typeface);
    }

    private void loadSlideshow() {
        // Reset
        terminateSlideShow.getAndSet(false);

        loadQueueFromLocalRootDir();

        startAsyncTask(new DropboxPhotosDownloadTask(this));

        startAsyncTask(new StartSlideshowTask(this));
    }

    private void startAsyncTask(AsyncTask asyncTask) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        else
            asyncTask.execute("");
    }

    private void loadQueueFromLocalRootDir() {
        addPhotosToQueueFromLocalDir(new File(getFilesDir(), LOCAL_DOWNLOAD_DIR_NAME));
    }

    private void addPhotosToQueueFromLocalDir(File dir) {
        File[] subFiles = dir.listFiles();
        Log.d(TAG, "Loading files from: " + dir.toString());

        if (subFiles != null) {
            for (File file : subFiles) {
                Log.d(TAG, "Found local file: " + file.toString());
                if (file.isDirectory()) {
                    addPhotosToQueueFromLocalDir(file);
                } else if (SUPPORTED_IMAGE_FORMATS.contains(Files.getFileExtension(file.getName()))) {
                    photoQueue.get().add(file);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        loadSlideshow();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        terminateSlideShow.getAndSet(true);
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        terminateSlideShow.getAndSet(true);
        super.onPause();
    }

    @Override
    public void onRestart() {
        terminateSlideShow.getAndSet(true);
        super.onRestart();
    }

    private static void updateSlideshowLayoutState(final Activity activity, final LoadingState loadingState) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Updating layout state: " + loadingState);

                LinearLayout loadingLayout = activity.findViewById(R.id.photos_loading_layout);
                LinearLayout errorLayout = activity.findViewById(R.id.photos_loading_error_layout);
                LinearLayout photosLayout = activity.findViewById(R.id.photos_layout);

                switch (loadingState) {
                    case DOWNLOADING:
                        loadingLayout.setVisibility(View.VISIBLE);
                        errorLayout.setVisibility(View.GONE);
                        photosLayout.setVisibility(View.GONE);
                        break;
                    case NO_PHOTOS_AVAILABLE:
                        loadingLayout.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.VISIBLE);
                        photosLayout.setVisibility(View.GONE);
                        break;
                    case PHOTOS_AVAILABLE:
                        loadingLayout.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.GONE);
                        photosLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });
    }

    // ************************ //
    // ** StartSlideshowTask ** //
    // ************************ //

    private static class StartSlideshowTask extends AsyncTask<Object, Void, Void> {
        private Activity activity;

        StartSlideshowTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(Object... params) {
            int currentSlideIndex = 0;
            final int transitionFreqMs = SharedPrefsHelper.getSlideTransitionFrequencyMilliseconds(activity);

            Log.d(TAG, "STARTING SLIDESHOW");

            while (!terminateSlideShow.get()) {
                synchronized (photoQueue) {
                    if (!photoQueue.get().isEmpty()) {

                        final File currentPhoto;
                        if (SharedPrefsHelper.isShowPhotosRandomOrder(activity)) {
                            currentPhoto = photoQueue.get().getNextRandom();
                        } else {
                            currentPhoto = photoQueue.get().getNext();
                        }

                        if (currentPhoto != null) {
                            if (photoQueue.get().isMarkedForDeletion(currentPhoto)) {
                                Log.d(TAG, "Deleting photo: " + currentPhoto);
                                photoQueue.get().remove(currentPhoto);
                                currentPhoto.delete();
                            } else {
                                showPhoto(activity, currentPhoto);

                                updateSlideshowLayoutState(activity, LoadingState.PHOTOS_AVAILABLE);
                            }
                        }
                    } else {
                        Log.d(TAG, "No photos available to show");
                    }
                }

                try {
                    Log.d(TAG, String.format("Waiting %d ms", transitionFreqMs));
                    Thread.sleep(transitionFreqMs);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.toString(), e);
                }
            }
            Log.d(TAG, "FINISHED SHOWING SLIDES");
            return null;
        }

        private void showPhoto(final Activity activity, File localPath) {
            final Bitmap bitmap = BitmapFactory.decodeFile(localPath.getAbsolutePath());
            final Date timeTaken = getDatePhotoCaptured(localPath);

            Log.d(TAG, String.format("Showing photo %s (%s)", localPath, new PrettyTime().format(timeTaken)));

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ImageView imageView = activity.findViewById(R.id.photos_main_view);
                    imageView.setImageBitmap(bitmap);

                    TextView dateDescription = activity.findViewById(R.id.photo_time_description);
                    if (timeTaken != null) {
                        dateDescription.setText(new PrettyTime().format(timeTaken));
                    } else {
                        dateDescription.setText("");
                    }
                }
            });
        }

        private static Date getDatePhotoCaptured(File localPath) {
//        com.drew.metadata.Metadata metadata = null;
//        Date date = null;
//
//        try {
////            InputStream inputStream = new FileInputStream(localPath);
////            BufferedInputStream bis = new BufferedInputStream(inputStream);
//            metadata = JpegMetadataReader.readMetadata(localPath);
//
//            if (metadata != null) {
//                // obtain the Exif directory
//                ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
//
//                // query the tag's value
//                date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
//            }
////            bis.close();
//        } catch (JpegProcessingException e) {
//            Log.e(TAG, e.toString(), e);
//            return null;
//        } catch (IOException e) {
//            Log.e(TAG, e.toString(), e);
//            return null;
//        }
//        return date;
            return new Date();
        }
    }

    // *********************************** //
    // ** DropboxPhotosDownloadTask ** //
    // *********************************** //

    private static class DropboxPhotosDownloadTask extends AsyncTask<Object, Void, Integer> {
//        private static final String DROPBOX_ACCESS_TOKEN_STAGING = "PL700l5eeYIAAAAAAAALpXbnXzOHVLawoihxDh2AUV-PPzh2DFyT-pw1PTwoxL1z";
        private static final String DROPBOX_ACCESS_TOKEN = "PL700l5eeYIAAAAAAAAK7xjOe_13PbLD6ioaI9-S0Vazd3tDsQznACZ3A9txLw0s";

        private Activity activity;

        DropboxPhotosDownloadTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected Integer doInBackground(Object... params) {
            updateSlideshowLayoutState(activity, LoadingState.DOWNLOADING);

            DbxRequestConfig config =
                    DbxRequestConfig.newBuilder("bivaca/family-hub-android")
                            .withAutoRetryEnabled()
                            .withUserLocale("en_US")
                            .build();

            DbxClientV2 client = new DbxClientV2(config, DROPBOX_ACCESS_TOKEN);

            HashSet<File> latestPhotosSet = null;
            try {
                latestPhotosSet = downloadPhotos(activity, client, "");
            } catch (DbxException ex) {
                Log.e(TAG, ex.getMessage(), ex);
            }

            if (latestPhotosSet != null) {
                markOldPhotosForDeletion(latestPhotosSet);
                return latestPhotosSet.size();
            } else {
                return -1;
            }
        }

        private static void markOldPhotosForDeletion(final HashSet<File> latestPhotosSet) {
            for (File photo : photoQueue.get()) {
                if (!latestPhotosSet.contains(photo)) {
                    photoQueue.get().markFileForDeletion(photo);
                }
            }
        }

        @Override
        protected void onPostExecute(Integer sizeNewSlideshowFromServer) {
            if (sizeNewSlideshowFromServer < 0) {
                //TODO notify of error
                Log.e(TAG,"Something bad happened when downloading the photos");
            } else {
                Log.d(TAG, "Photos in slideshow: " + sizeNewSlideshowFromServer);
                if (sizeNewSlideshowFromServer == 0 ) {
                    updateSlideshowLayoutState(activity, LoadingState.NO_PHOTOS_AVAILABLE);
                }
            }
        }

        private HashSet<File> downloadPhotos(Context context, DbxClientV2 client, String dirPath) throws DbxException {
            HashSet<File> latestPhotosSet = new HashSet<>();

            final File localDownloadDir = new File(context.getFilesDir(), LOCAL_DOWNLOAD_DIR_NAME);

            if (!localDownloadDir.exists()) {
                Log.d(TAG, "Creating new root directory");
                localDownloadDir.mkdir();
            }

            // Get files and folder metadata from Dropbox root directory
            ListFolderResult result = client.files().listFolder(dirPath);

            do {
                for (Metadata metadata : result.getEntries()) {
                    final String remotePath = metadata.getPathLower();
                    Log.d(TAG, "New file at path: " + remotePath);

                    if (isDropboxFolder(remotePath)) {
                        // Add all photos returned from this folder
                        latestPhotosSet.addAll(downloadPhotos(context, client, remotePath));
                    } else {
                        File localPath = new File(localDownloadDir, remotePath);
                        latestPhotosSet.add(localPath);

                        // Don't re-download this file if it exists already
                        if (localPath.exists()) {
                            Log.d(TAG, "Skipping download of file " + localPath);
                        } else {
                            try {
                                // Create parent directly if necessary
                                if (!localPath.getParentFile().exists()) {
                                    localPath.getParentFile().mkdirs();
                                }

                                FileOutputStream out = new FileOutputStream(localPath);

                                // Download file
                                DbxDownloader<FileMetadata> downloader = client.files().download(remotePath);
                                downloader.download(out);
                                out.close();

                                Log.d(TAG, String.format("Downloaded %d bytes to %s", downloader.getResult().getSize(), localPath.getAbsolutePath()));

                                photoQueue.get().add(localPath);
                            } catch (DbxException e) {
                                Log.e(TAG, e.getMessage(), e);
                                return null;
                            } catch (FileNotFoundException e) {
                                Log.e(TAG, e.getMessage(), e);
                                return null;
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage(), e);
                                return null;
                            }
                        }
                    }
                }

                result = client.files().listFolderContinue(result.getCursor());
            } while (result.getHasMore() && !terminateSlideShow.get());

            Log.d(TAG, String.format("No more files in remote directory '%s'", dirPath));
            return latestPhotosSet;
        }

        private static boolean isDropboxFolder(String filePath) {
            //FIXME find better way to determine if file or folder
            return filePath.startsWith("/") && filePath.charAt(filePath.length()-4) != '.';
        }
    }
}

package com.benjamjin.familyhub;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MessageSelectionActivity extends AppCompatActivity {
    private static final String TAG = "BEN";
    GestureDetector mGestureDetector;
    private ListIterator<BasicSms> mInboxIterator = null;
    private int previousDirection = -1;

    private static String[] SMS_PERMISSIONS = new String[]{
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS};

    private HashMap<String, String> familyMembers;
    private LinkedList<BasicSms> inbox;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
//    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
//    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
//
//    /**
//     * Some older devices needs a small delay between UI widget updates
//     * and a change of the status and navigation bar.
//     */
//    private static final int UI_ANIMATION_DELAY = 300;
//    private final Handler mHideHandler = new Handler();
//    private View mContentView;
//    private final Runnable mHidePart2Runnable = new Runnable() {
//        @SuppressLint("InlinedApi")
//        @Override
//        public void run() {
//            // Delayed removal of status and navigation bar
//
//            // Note that some of these constants are new as of API 16 (Jelly Bean)
//            // and API 19 (KitKat). It is safe to use them, as they are inlined
//            // at compile-time and do nothing on earlier devices.
//            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        }
//    };
//    private View mControlsView;
//    private final Runnable mShowPart2Runnable = new Runnable() {
//        @Override
//        public void run() {
//            // Delayed display of UI elements
//            ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.show();
//            }
//            mControlsView.setVisibility(View.VISIBLE);
//        }
//    };
//    private boolean mVisible;
//    private final Runnable mHideRunnable = new Runnable() {
//        @Override
//        public void run() {
//            hide();
//        }
//    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
//    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            if (AUTO_HIDE) {
//                delayedHide(AUTO_HIDE_DELAY_MILLIS);
//            }
//            return false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message_selection);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mGestureDetector = new GestureDetector(this, simpleOnGestureListener);

        TextView messagePreview = (TextView)findViewById(R.id.message_preview_text);
        messagePreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        loadFamilyMembers();

        if (doRequestPermissions()) {
            loadInboxAndInitView();
        }

//        mVisible = true;
//        mControlsView = findViewById(R.id.fullscreen_content_controls);
//        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
//        mContentView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                toggle();
//            }
//        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
//        findViewById(R.id.up_button).setOnTouchListener(mDelayHideTouchListener);
//        findViewById(R.id.message_preview_text).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
////                startActivity(new Intent(this, MessageContentView.class));
//                return false;
//            }
//        });
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        int index = 0;
        HashMap<String, Integer> PermissionsMap = new HashMap<>();
        for (String permission : permissions){
            PermissionsMap.put(permission, grantResults[index]);
            index++;
        }

        if(PermissionsMap.get(Manifest.permission.RECEIVE_SMS) != 0
                || PermissionsMap.get(Manifest.permission.READ_SMS) != 0){
            Toast.makeText(this, "SMS permissions are required", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            loadInboxAndInitView();
        }
    }

    private void loadInboxAndInitView() {
        loadInbox();
        previewMessage(0);
    }

    private void previewMessage(int direction) {

//        Log.d(TAG, "Showing message " + direction);

        BasicSms sms = null;

        if (mInboxIterator == null) {
            mInboxIterator = inbox.listIterator(inbox.size());

            direction = 1;
            previousDirection = direction;
        }

//        Log.d(TAG, String.format("prev %d, next %d", mInboxIterator.previousIndex(), mInboxIterator.nextIndex()));

        if (direction > 0 && mInboxIterator.hasPrevious()) {
            Log.d(TAG, "Showing previous " + direction + " " + mInboxIterator.previousIndex());
            sms = mInboxIterator.previous();
        } else if (direction < 0 && mInboxIterator.hasNext()) {
            Log.d(TAG, "Showing next " + direction + " " + mInboxIterator.nextIndex());
            sms = mInboxIterator.next();
        }

        if (direction != previousDirection) {
            previousDirection = direction;
            previewMessage(direction);
            return;
        }

        if (sms != null) {
            Log.d(TAG, "Latest SMS: " + sms);

            TextView previewView = (TextView) findViewById(R.id.message_preview_text);
            previewView.setText(sms.body);

            TextView senderView = (TextView) findViewById(R.id.message_sender_text);
            String senderName = familyMembers.get(sms.senderAddress);
            senderView.setText(senderName);

            TextView smsSentView = (TextView) findViewById(R.id.message_datetime_text);
            String smsSentText = getPrettyStringFromTimestamp(sms.timestampSent);
            smsSentView.setText(String.format("(%s)", smsSentText));
        }
    }

    private static String getPrettyStringFromTimestamp(String timestampStr) {
        PrettyTime p = new PrettyTime();
        return p.format(new Date(Long.parseLong(timestampStr)));
    }

    public void previewOlderMessage(View v) {
        previewMessage(1);
    }

    public void previewNewerMessage(View v) {
        previewMessage(-1);
    }

    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            int dx = (int) (event2.getX() - event1.getX());
            // don't accept the fling if it's too short as it may conflict with a button push
            if (Math.abs(dx) > 50 && Math.abs(velocityX) > Math.abs(velocityY)) {
                if (velocityX > 0) {
                    Log.d(TAG, "Drag right!");
                    finish();
                }
//                else {
//                    Log.d(TAG, "Drag left!");
//                }
                return true;
            } else {
                return false;
            }
        }
    };

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
//        delayedHide(100);
    }

    private void loadFamilyMembers() {
        familyMembers = new HashMap<>();

        familyMembers.put("+447870757202", "Andrew");
        familyMembers.put("+44", "Richard");
        familyMembers.put("+447733024940", "Jenny");
        familyMembers.put("+447754741953", "Ben");
        familyMembers.put("+4915737625757", "Ruth");

        Log.d(TAG, familyMembers.toString());
    }

    /**
     *
     * @return False if this triggers a permission request, else true (ie we have the permissions we need).
     */
    private boolean doRequestPermissions()
    {
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

    private void loadInbox() {
        inbox = new LinkedList<>();

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                BasicSms sms = new BasicSms();
                for (int i=0; i<cursor.getColumnCount(); i++) {
                    if (cursor.getColumnName(i).equals("body")) {
                        sms.body = cursor.getString(i);
                    } else if (cursor.getColumnName(i).equals("address")) {
                        sms.senderAddress = cursor.getString(i);
                    } else if (cursor.getColumnName(i).equals("date_sent")) {
                        sms.timestampSent = cursor.getString(i);
                    }
                }

                if (familyMembers.containsKey(sms.senderAddress)) {
                    inbox.push(sms);
                    Log.d(TAG, "Family SMS: " + sms);
                }


            } while (cursor.moveToNext());

        } else {
            Log.i("BEN", "No SMS");
        }
    }

//    private void toggle() {
//        if (mVisible) {
//            hide();
//        } else {
//            show();
//        }
//    }

//    private void hide() {
//        // Hide UI first
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.hide();
//        }
//        mControlsView.setVisibility(View.GONE);
//        mVisible = false;
//
//        // Schedule a runnable to remove the status and navigation bar after a delay
//        mHideHandler.removeCallbacks(mShowPart2Runnable);
//        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
//    }

//    @SuppressLint("InlinedApi")
//    private void show() {
//        // Show the system bar
//        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//        mVisible = true;
//
//        // Schedule a runnable to display UI elements after a delay
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
//    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
//    private void delayedHide(int delayMillis) {
//        mHideHandler.removeCallbacks(mHideRunnable);
//        mHideHandler.postDelayed(mHideRunnable, delayMillis);
//    }
}


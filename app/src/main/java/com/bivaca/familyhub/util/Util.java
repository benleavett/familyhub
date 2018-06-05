package com.bivaca.familyhub.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Telephony;
import android.util.Log;

import com.bivaca.familyhub.BuildConfig;

public class Util {

    private static final String COMMON_TAG = "FamilyHub";

    public static void log(Object... args) {
        Log.d(COMMON_TAG, buildString(args));
    }

    private static String buildString(Object... args) {
        StringBuilder builder = new StringBuilder();
        for (Object obj : args) {
            builder.append(obj);
        }
        return builder.toString();
    }

    /**
     * @return true if we are the default home screen
     */
    public static boolean isDefault(Context context) {
        ResolveInfo info = context.getPackageManager().resolveActivity(homeScreenIntent(), 0);
        return BuildConfig.APPLICATION_ID.equals(info.activityInfo.packageName);
    }

    /**
     * @return true if there is no default home screen set
     */
    private static boolean noDefaultSet(Context context) {
        final PackageManager pm = context.getPackageManager();
        final ResolveInfo info = pm.resolveActivity(Util.homeScreenIntent(), 0);
        return info.match == 0 || "com.android.internal.app.ResolverActivity".equals(info.activityInfo.targetActivity);
    }

    public static boolean isNotHomeLauncher(Context context) {
        return !noDefaultSet(context) && !isDefault(context);
    }

    public static Intent homeScreenIntent() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return home;
    }

    public static boolean isLollipopOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isNougatOrAbove() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean isCharging(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int chargingData = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        switch (chargingData) {
            case BatteryManager.BATTERY_PLUGGED_AC:
            case BatteryManager.BATTERY_PLUGGED_USB:
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                return true;
            default:
                return false;
        }
    }

    public static boolean isDefaultMessagingApp(Context context) {
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context);
        return defaultSmsPackage != null && defaultSmsPackage.equals(context.getPackageName());
    }
}

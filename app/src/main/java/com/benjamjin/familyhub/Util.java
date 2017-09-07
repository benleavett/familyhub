package com.benjamjin.familyhub;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

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
    static boolean isDefault(Context context) {
        ResolveInfo info = context.getPackageManager().resolveActivity(homeScreenIntent(), 0);
        return BuildConfig.APPLICATION_ID.equals(info.activityInfo.packageName);
    }

    /**
     * @return true if there is no default home screen set
     */
    public static boolean noDefaultSet(Context context) {
        final PackageManager pm = context.getPackageManager();
        final ResolveInfo info = pm.resolveActivity(Util.homeScreenIntent(), 0);
        return info.match == 0 || "com.android.internal.app.ResolverActivity".equals(info.activityInfo.targetActivity);
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
}

package com.daim.topactivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ljm
 * @description
 * @date
 */
public class PackageInfoUtil {
    private static final String TAG = "PackageInfoUtil";

    /**
     * 获取应用信息 需要权限：android.permission.QUERY_ALL_PACKAGES
     * @param context
     */
    public static void getPackageInfo(Context context) {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        List<ResolveInfo> mApps = context.getPackageManager().queryIntentActivities(mainIntent, 0);
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        List<String> packageList = new ArrayList<>();
        for (ResolveInfo appInfo: mApps) {
            Log.d("ljm123", "getPackageInfo: " + appInfo.activityInfo.packageName);
            if (packageList.contains(appInfo.activityInfo.packageName)) {
                continue;
            }
            packageList.add(appInfo.activityInfo.packageName);
            String appLabel = appInfo.loadLabel(packageManager).toString();
            String packageName = appInfo.activityInfo.packageName;
            String startAct = appInfo.activityInfo.name;
            String appIcon = appInfo.loadIcon(packageManager).toString();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(packageName,0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "get package info error : " + e.getMessage());
            }
        }
        Log.d(TAG, "getPackageInfo: " + packageList);
    }
}

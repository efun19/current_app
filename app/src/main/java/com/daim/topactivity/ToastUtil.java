package com.daim.topactivity;

import android.content.Context;
import android.widget.Toast;

/**
 * @author ljm
 * @description
 * @date
 */
public class ToastUtil {
    public static void showOverlayToast(boolean isRequestPermission, Context context) {
        if (isRequestPermission) {
            Toast.makeText(context, "悬浮窗权限已开启", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "请开启悬浮窗权限", Toast.LENGTH_SHORT).show();
        }
    }

    public static void showUsageStatsToast(boolean isRequestPermission, Context context) {
        if (isRequestPermission) {
            Toast.makeText(context, "使用统计权限已开启", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "请开启使用统计权限", Toast.LENGTH_SHORT).show();
        }
    }
}

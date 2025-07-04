package com.daim.topactivity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

/**
 * @author ljm
 * @description
 * @date
 */
public class FloatingWindowService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private Handler handler = new Handler(Looper.getMainLooper());
    /**
     * 通知ID
     */
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "floating_window_channel";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundNotification();
        createFloatingWindow();
        updateFloatingWindow();
    }

    /**
     * 创建通知渠道（Android 8.0+ 必需）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "悬浮窗服务",
                    // 低优先级不发出声音
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("悬浮窗服务运行中");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    /**
     * 启动前台服务的核心方法
     */
    @SuppressLint("ForegroundServiceType")
    private void startForegroundNotification() {
        Notification notification = buildNotification();
        startForeground(NOTIFICATION_ID, notification);
    }

    /**
     * 构建通知
     */
    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("悬浮窗服务")
                .setContentText("正在后台运行")
                // 必须设置有效图标
                .setSmallIcon(R.drawable.ic_notification)
                // 低优先级
                .setPriority(NotificationCompat.PRIORITY_LOW)
                // 设置为持续通知
                .setOngoing(true)
                // 用户不能手动清除
                .setAutoCancel(false)
                .build();
    }

    private void createFloatingWindow() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // 1. 初始化悬浮窗视图
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null);

        // 2. 设置悬浮窗参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        // 3. 设置悬浮窗位置
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 100;

        // 4. 添加视图到窗口
        windowManager.addView(floatingView, params);

        // 5. 设置拖拽事件
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int)(event.getRawX() - initialTouchX);
                        params.y = initialY + (int)(event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void updateFloatingWindow() {
        Runnable checkForegroundAppRunnable = new Runnable() {
            @Override
            public void run() {
                getCurrentActivity();
                Log.d("ljm123", "run: ");
                handler.postDelayed(this, 1000); // 每 500 毫秒检查一次
            }
        };
        handler.post(checkForegroundAppRunnable);
    }

    private void getCurrentActivity() {
        // 获取当前正在运行的 Activity 类名
        String currentActivityName = getTopActivity();
        if (currentActivityName!= null) {
            TextView textView = floatingView.findViewById(R.id.floating_text);
            textView.setText(currentActivityName);
        }
    }
    private String getTopActivity() {
        // 替代方案(需要API 21+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

            long endTime = System.currentTimeMillis();
            // 查询最近1分钟的数据
            long beginTime = endTime - 1000 * 60;
            UsageEvents queryEvents = usageStatsManager.queryEvents(beginTime, endTime);
            UsageEvents.Event event = new UsageEvents.Event();
            String packageName = null;
            String activityName = null;
            while (queryEvents.hasNextEvent()) {
                queryEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                        event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                    packageName = event.getPackageName();
                    // 获取Activity类名（需要API 29+）
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        activityName = event.getClassName();
                    }
                }
            }
            return packageName + "\n" + activityName;
        }

        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
        handler.removeCallbacksAndMessages(null);
    }
}

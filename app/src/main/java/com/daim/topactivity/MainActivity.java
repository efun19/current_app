package com.daim.topactivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

/**
 * @author ljm
 */
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_OVERLAY = 1001;
    private static final int REQUEST_CODE_USAGE_STATS = 1002;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        transparentStatusBar();
        setContentView(R.layout.activity_main);
        initView();
    }

    private void transparentStatusBar() {
        // 使状态栏透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    private void initView() {
        setToolbar();
        setButton();
    }

    private void  setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.main);
        setSupportActionBar(toolbar);
    }

    private void setButton() {
        Button button = findViewById(R.id.button_float);
        if (isFloatingWindowShowing(this)) {
            button.setText("关闭悬浮窗");
        }
        button.setOnClickListener(v -> {
            if (isFloatingWindowShowing(this)) {
                stopFloatingWindow();
                button.setText("启动悬浮窗");
            } else {
                if (hasAllPermissions()) {
                    startFloatingWindow();
                    button.setText("关闭悬浮窗");
                }
            }
        });
    }

    private boolean hasAllPermissions() {
        // 1. 检查权限
        if (!PermissionUtil.hasOverlayPermission(this)) {
            ToastUtil.showOverlayToast(false, this);
            return false;
        }

        if (!PermissionUtil.hasUsageStatsPermission(this)) {
            ToastUtil.showUsageStatsToast(false, this);
            return false;
        }
        return true;
    }

    /**
     * 启动悬浮窗服务
     */
    public void startFloatingWindow() {
        // 2. 启动服务（Android 8.0+需使用前台服务）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, FloatingWindowService.class));
        } else {
            startService(new Intent(this, FloatingWindowService.class));
        }
    }

    /**
     * 停止悬浮窗服务
     */
    public void stopFloatingWindow() {
        stopService(new Intent(this, FloatingWindowService.class));
    }

    /**
     * 检查悬浮窗服务是否正在运行
     *
     * @param context
     * @return
     */
    public static boolean isFloatingWindowShowing(Context context) {
        // 检查服务是否运行
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (FloatingWindowService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理权限请求结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OVERLAY) {
            ToastUtil.showOverlayToast(PermissionUtil.hasOverlayPermission(this), this);
        }
        if (requestCode == REQUEST_CODE_USAGE_STATS) {
            ToastUtil.showUsageStatsToast(PermissionUtil.hasUsageStatsPermission(this), this);
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.item_overlay) {
            if (!PermissionUtil.hasOverlayPermission(this)) {
                PermissionUtil.requestOverlayPermission(this, REQUEST_CODE_OVERLAY);
            } else {
                ToastUtil.showOverlayToast(true, this);
            }
        } else if (itemId == R.id.item_usage) {
            if (!PermissionUtil.hasUsageStatsPermission(this)) {
                PermissionUtil.requestUsageStatsPermission(this, REQUEST_CODE_USAGE_STATS);
            } else {
                ToastUtil.showUsageStatsToast(true, this);
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
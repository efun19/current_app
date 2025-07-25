package com.daim.topactivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * @author ljm
 */
public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void openAppByPackageName(String packageName) {
        PackageManager packageManager = getPackageManager();
        // 获取指定包名的启动 Intent
        Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            // 如果找到了对应的应用，则启动它
            try {
                startActivity(launchIntent);
            } catch (Exception e) {
                // 处理启动异常，例如应用被禁用或出现未知错误
                Toast.makeText(this, "无法启动应用：" + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            // 没有找到对应的应用
            Toast.makeText(this, "未找到包名为 '" + packageName + "' 的应用", Toast.LENGTH_LONG).show();
        }
    }
}
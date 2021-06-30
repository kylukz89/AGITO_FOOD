package com.kylukz.agitofoods;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.kylukz.agitofoods.Animation.Animatoo;
import com.kylukz.agitofoods.Toolbox.ServiceW;
import com.kylukz.agitofoods.View.MainActivity;

public class SplashActivity extends AppCompatActivity {
    public static Context ctx;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide(); // remove barra de título
       View view =  getWindow().getDecorView();
        setContentView(R.layout.activity_splash);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        startService(new Intent(this, ServiceW.class));
        Animatoo.animateSwipeRight(this);
        //////////////////////// 2º PLANO //////////////////////
        String manufacturer = "xiaomi";
        if (manufacturer.equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
            //this will open auto start screen where user can enable permission for your app
            Intent intent1 = new Intent();
            intent1.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            //startActivity(intent1);
        }
        ////////////////////////////////////////////////////////




        //AutoStartPermissionHelper.getInstance().getAutoStartPermission(Splash.this);

        Thread geradorThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        geradorThread.start();
    }
}

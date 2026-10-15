package com.ffabhi.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SidePanelService extends Service {

    private WindowManager windowManager;
    private View handleView;
    private View panelView;
    private boolean isPanelOpen = false;
    private Handler handler = new Handler();
    private float initialX;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        handleView = LayoutInflater.from(this).inflate(R.layout.handle_layout, null);
        WindowManager.LayoutParams handleParams = new WindowManager.LayoutParams(
            12, 200,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        handleParams.gravity = Gravity.LEFT | Gravity.TOP;
        handleParams.y = 300;

        handleView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = event.getRawX();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (event.getRawX() - initialX > 80 && !isPanelOpen) {
                        openPanel();
                    }
                    return true;
            }
            return false;
        });

        windowManager.addView(handleView, handleParams);
        updateStats();
    }

    private void openPanel() {
        if (isPanelOpen) return;
        isPanelOpen = true;

        panelView = LayoutInflater.from(this).inflate(R.layout.panel_layout, null);
        WindowManager.LayoutParams panelParams = new WindowManager.LayoutParams(
            600,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        panelParams.gravity = Gravity.LEFT;

        SeekBar brightnessSlider = panelView.findViewById(R.id.brightnessSlider);
        try {
            int currentBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            brightnessSlider.setProgress(currentBrightness);
        } catch (Exception e) {}
        brightnessSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && Settings.System.canWrite(SidePanelService.this)) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SeekBar volumeSlider = panelView.findViewById(R.id.volumeSlider);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        volumeSlider.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSlider.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Button btnClean = panelView.findViewById(R.id.btnCleanRam);
        btnClean.setOnClickListener(v -> startRamCleanAnimation());

        Button btnClose = panelView.findViewById(R.id.btnClosePanel);
        btnClose.setOnClickListener(v -> closePanel());

        windowManager.addView(panelView, panelParams);
    }

    private void startRamCleanAnimation() {
        if (panelView == null) return;

        ProgressBar progress = panelView.findViewById(R.id.cleanProgress);
        TextView status = panelView.findViewById(R.id.cleanStatus);
        TextView ramInfo = panelView.findViewById(R.id.ramInfo);
        Button btnClean = panelView.findViewById(R.id.btnCleanRam);

        btnClean.setEnabled(false);
        progress.setProgress(0);
        status.setText("🧹 Cleaning...");

        final int[] progressValue = {0};
        Handler animHandler = new Handler();
        Runnable animRunnable = new Runnable() {
            @Override
            public void run() {
                if (progressValue[0] <= 100) {
                    progress.setProgress(progressValue[0]);
                    progressValue[0] += 5;
                    animHandler.postDelayed(this, 100);
                } else {
                    performRamClean();
                    status.setText("✅ RAM Cleaned!");
                    btnClean.setEnabled(true);

                    android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
                    android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
                    am.getMemoryInfo(mi);
                    ramInfo.setText("RAM: " + ((mi.totalMem - mi.availMem) / (1024 * 1024)) + " MB used");

                    handler.postDelayed(() -> {
                        if (panelView != null) {
                            status.setText("🧹 RAM Cleaner");
                            progress.setProgress(0);
                        }
                    }, 2000);
                }
            }
        };
        animHandler.post(animRunnable);
    }

    private void performRamClean() {
        try {
            Runtime.getRuntime().exec("am kill-all");
            Runtime.getRuntime().exec("pm trim-caches 999999999999");
        } catch (Exception e) {}
    }

    private void closePanel() {
        if (panelView != null) {
            windowManager.removeView(panelView);
            panelView = null;
        }
        isPanelOpen = false;
    }

    private void updateStats() {
        if (panelView != null) {
            TextView ping = panelView.findViewById(R.id.panelPing);
            TextView battery = panelView.findViewById(R.id.panelBattery);
            TextView ramInfo = panelView.findViewById(R.id.ramInfo);

            new Thread(() -> {
                try {
                    Process p = Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8");
                    BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line;
                    final String[] pingVal = {"--"};
                    while ((line = r.readLine()) != null) {
                        if (line.contains("time=")) {
                            pingVal[0] = line.substring(line.indexOf("time=") + 5).split(" ")[0];
                        }
                    }
                    final String pv = pingVal[0];
                    handler.post(() -> ping.setText("Ping: " + pv + " ms"));
                } catch (Exception e) {}
            }).start();

            BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
            int level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            Intent bi = registerReceiver(null, new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int temp = bi.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            battery.setText("Battery: " + level + "% | Temp: " + (temp / 10.0f) + "°C");

            android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
            android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
            am.getMemoryInfo(mi);
            ramInfo.setText("RAM: " + ((mi.totalMem - mi.availMem) / (1024 * 1024)) + " MB used");
        }

        handler.postDelayed(this::updateStats, 3000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handleView != null) windowManager.removeView(handleView);
        if (panelView != null) windowManager.removeView(panelView);
    }
                    }

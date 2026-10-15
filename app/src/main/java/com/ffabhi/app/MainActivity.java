package com.ffabhi.app;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends Activity {

    ListView listGames;
    Button btnAddGame, btnSidePanel;
    Switch swDnd, swBrightness;
    TextView tvPing, tvBattery, tvRam, btnSettings;
    ArrayList<String> gamesList;
    ArrayAdapter<String> adapter;
    SharedPreferences prefs, settingsPrefs;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listGames = findViewById(R.id.listGames);
        btnAddGame = findViewById(R.id.btnAddGame);
        btnSidePanel = findViewById(R.id.btnSidePanel);
        btnSettings = findViewById(R.id.btnSettings);
        swDnd = findViewById(R.id.swDnd);
        swBrightness = findViewById(R.id.swBrightness);
        tvPing = findViewById(R.id.tvPing);
        tvBattery = findViewById(R.id.tvBattery);
        tvRam = findViewById(R.id.tvRam);

        prefs = getSharedPreferences("ffabhi", MODE_PRIVATE);
        settingsPrefs = getSharedPreferences("ffabhi_settings", MODE_PRIVATE);

        gamesList = new ArrayList<>(prefs.getStringSet("games", new HashSet<>()));
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, gamesList);
        listGames.setAdapter(adapter);

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        btnSidePanel.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
                Toast.makeText(this, "Overlay permission do", Toast.LENGTH_LONG).show();
            } else {
                startService(new Intent(this, SidePanelService.class));
                Toast.makeText(this, "Side Panel ON", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddGame.setOnClickListener(v -> showAppSelector());

        listGames.setOnItemLongClickListener((parent, view, position, id) -> {
            gamesList.remove(position);
            adapter.notifyDataSetChanged();
            prefs.edit().putStringSet("games", new HashSet<>(gamesList)).apply();
            Toast.makeText(this, "Hata diya", Toast.LENGTH_SHORT).show();
            return true;
        });

        listGames.setOnItemClickListener((parent, view, position, id) -> {
            String appName = gamesList.get(position);
            launchGameWithAutoFeatures(appName);
        });

        swDnd.setOnCheckedChangeListener((b, isChecked) -> {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (isChecked) {
                if (nm.isNotificationPolicyAccessGranted()) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                } else {
                    startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                    swDnd.setChecked(false);
                }
            } else {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
            }
        });

        swBrightness.setOnCheckedChangeListener((b, isChecked) -> {
            if (isChecked) {
                if (Settings.System.canWrite(this)) {
                    Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
                } else {
                    startActivity(new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName())));
                    swBrightness.setChecked(false);
                }
            }
        });

        updateStats();
    }

    private void showAppSelector() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<ApplicationInfo> userApps = new ArrayList<>();
        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                userApps.add(app);
            }
        }
        Collections.sort(userApps, (a, b) -> a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));

        AppListAdapter appAdapter = new AppListAdapter(this, userApps);

        new android.app.AlertDialog.Builder(this)
            .setTitle("🎮 Game Select Karo")
            .setAdapter(appAdapter, (dialog, which) -> {
                ApplicationInfo selected = userApps.get(which);
                String appName = selected.loadLabel(pm).toString();
                if (!gamesList.contains(appName)) {
                    gamesList.add(appName);
                    adapter.notifyDataSetChanged();
                    prefs.edit().putStringSet("games", new HashSet<>(gamesList)).apply();
                    Toast.makeText(this, appName + " add ho gaya!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Yeh game already added hai!", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void launchGameWithAutoFeatures(String appName) {
        PackageManager pm = getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage(getPackageNameFromAppName(appName));
        if (launchIntent == null) {
            Toast.makeText(this, "App nahi mila!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (settingsPrefs.getBoolean("auto_ram", true)) {
            try { Runtime.getRuntime().exec("am kill-all"); } catch (Exception e) {}
        }

        if (settingsPrefs.getBoolean("auto_brightness", true)) {
            if (Settings.System.canWrite(this)) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
            }
        }

        if (settingsPrefs.getBoolean("auto_dnd", true)) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm.isNotificationPolicyAccessGranted()) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            }
        }

        if (Settings.canDrawOverlays(this)) {
            startService(new Intent(this, SidePanelService.class));
        }

        startActivity(launchIntent);
        Toast.makeText(this, "🔥 " + appName + " launching with Game Mode!", Toast.LENGTH_SHORT).show();
    }

    private String getPackageNameFromAppName(String appName) {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo app : apps) {
            if (app.loadLabel(pm).toString().equals(appName)) {
                return app.packageName;
            }
        }
        return "";
    }

    private void updateStats() {
        new Thread(() -> {
            try {
                Process p = Runtime.getRuntime().exec("/system/bin/ping -c 1 8.8.8.8");
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                final String[] ping = {"--"};
                while ((line = r.readLine()) != null) {
                    if (line.contains("time=")) {
                        ping[0] = line.substring(line.indexOf("time=") + 5).split(" ")[0];
                    }
                }
                runOnUiThread(() -> tvPing.setText("Ping: " + ping[0] + " ms"));
            } catch (Exception e) {}
        }).start();

        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        Intent bi = registerReceiver(null, new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int temp = bi.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        tvBattery.setText("Battery: " + level + "% | Temp: " + (temp / 10.0f) + "°C");

        android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
        android.app.ActivityManager.MemoryInfo mi = new android.app.ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        tvRam.setText("RAM: " + ((mi.totalMem - mi.availMem) / (1024 * 1024)) + " MB");

        handler.postDelayed(this::updateStats, 3000);
    }
                                              }

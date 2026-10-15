package com.ffabhi.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

public class SettingsActivity extends Activity {

    SeekBar seekPosX, seekPosY, seekWidth, seekHeight;
    TextView tvPosX, tvPosY, tvWidth, tvHeight;
    RadioGroup rgColor;
    Switch swAutoDnd, swAutoBrightness, swAutoRam, swGameFocus;
    Button btnSave;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("ffabhi_settings", MODE_PRIVATE);

        seekPosX = findViewById(R.id.seekPosX);
        seekPosY = findViewById(R.id.seekPosY);
        seekWidth = findViewById(R.id.seekWidth);
        seekHeight = findViewById(R.id.seekHeight);
        tvPosX = findViewById(R.id.tvPosX);
        tvPosY = findViewById(R.id.tvPosY);
        tvWidth = findViewById(R.id.tvWidth);
        tvHeight = findViewById(R.id.tvHeight);
        rgColor = findViewById(R.id.rgColor);
        swAutoDnd = findViewById(R.id.swAutoDnd);
        swAutoBrightness = findViewById(R.id.swAutoBrightness);
        swAutoRam = findViewById(R.id.swAutoRam);
        swGameFocus = findViewById(R.id.swGameFocus);
        btnSave = findViewById(R.id.btnSaveSettings);

        seekPosX.setProgress(prefs.getInt("dandi_x", 20));
        seekPosY.setProgress(prefs.getInt("dandi_y", 300));
        seekWidth.setProgress(prefs.getInt("dandi_width", 20));
        seekHeight.setProgress(prefs.getInt("dandi_height", 200));
        tvPosX.setText(String.valueOf(prefs.getInt("dandi_x", 20)));
        tvPosY.setText(String.valueOf(prefs.getInt("dandi_y", 300)));
        tvWidth.setText(String.valueOf(prefs.getInt("dandi_width", 20)));
        tvHeight.setText(String.valueOf(prefs.getInt("dandi_height", 200)));

        String color = prefs.getString("dandi_color", "green");
        if (color.equals("blue")) rgColor.check(R.id.rbBlue);
        else if (color.equals("fire")) rgColor.check(R.id.rbFire);
        else rgColor.check(R.id.rbGreen);

        swAutoDnd.setChecked(prefs.getBoolean("auto_dnd", true));
        swAutoBrightness.setChecked(prefs.getBoolean("auto_brightness", true));
        swAutoRam.setChecked(prefs.getBoolean("auto_ram", true));
        swGameFocus.setChecked(prefs.getBoolean("game_focus", false));

        seekPosX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) { tvPosX.setText(String.valueOf(p)); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });

        seekPosY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) { tvPosY.setText(String.valueOf(p)); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });

        seekWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) { tvWidth.setText(String.valueOf(p)); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });

        seekHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) { tvHeight.setText(String.valueOf(p)); }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        });

        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("dandi_x", seekPosX.getProgress());
            editor.putInt("dandi_y", seekPosY.getProgress());
            editor.putInt("dandi_width", seekWidth.getProgress());
            editor.putInt("dandi_height", seekHeight.getProgress());

            if (rgColor.getCheckedRadioButtonId() == R.id.rbBlue)
                editor.putString("dandi_color", "blue");
            else if (rgColor.getCheckedRadioButtonId() == R.id.rbFire)
                editor.putString("dandi_color", "fire");
            else
                editor.putString("dandi_color", "green");

            editor.putBoolean("auto_dnd", swAutoDnd.isChecked());
            editor.putBoolean("auto_brightness", swAutoBrightness.isChecked());
            editor.putBoolean("auto_ram", swAutoRam.isChecked());
            editor.putBoolean("game_focus", swGameFocus.isChecked());
            editor.apply();

            Toast.makeText(this, "Settings Save Ho Gayi!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
                }

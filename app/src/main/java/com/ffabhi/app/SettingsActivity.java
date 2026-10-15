package com.ffabhi.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;

public class SettingsActivity extends Activity {

    RadioGroup rgPosition, rgSize, rgColor;
    Switch swAutoDnd, swAutoBrightness, swAutoRam;
    Button btnSave;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("ffabhi_settings", MODE_PRIVATE);

        rgPosition = findViewById(R.id.rgPosition);
        rgSize = findViewById(R.id.rgSize);
        rgColor = findViewById(R.id.rgColor);
        swAutoDnd = findViewById(R.id.swAutoDnd);
        swAutoBrightness = findViewById(R.id.swAutoBrightness);
        swAutoRam = findViewById(R.id.swAutoRam);
        btnSave = findViewById(R.id.btnSaveSettings);

        String pos = prefs.getString("dandi_position", "left");
        if (pos.equals("right")) rgPosition.check(R.id.rbRight);
        else rgPosition.check(R.id.rbLeft);

        String size = prefs.getString("dandi_size", "medium");
        if (size.equals("small")) rgSize.check(R.id.rbSmall);
        else if (size.equals("large")) rgSize.check(R.id.rbLarge);
        else rgSize.check(R.id.rbMedium);

        String color = prefs.getString("dandi_color", "green");
        if (color.equals("blue")) rgColor.check(R.id.rbBlue);
        else if (color.equals("fire")) rgColor.check(R.id.rbFire);
        else rgColor.check(R.id.rbGreen);

        swAutoDnd.setChecked(prefs.getBoolean("auto_dnd", true));
        swAutoBrightness.setChecked(prefs.getBoolean("auto_brightness", true));
        swAutoRam.setChecked(prefs.getBoolean("auto_ram", true));

        btnSave.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();

            if (rgPosition.getCheckedRadioButtonId() == R.id.rbRight)
                editor.putString("dandi_position", "right");
            else
                editor.putString("dandi_position", "left");

            if (rgSize.getCheckedRadioButtonId() == R.id.rbSmall)
                editor.putString("dandi_size", "small");
            else if (rgSize.getCheckedRadioButtonId() == R.id.rbLarge)
                editor.putString("dandi_size", "large");
            else
                editor.putString("dandi_size", "medium");

            if (rgColor.getCheckedRadioButtonId() == R.id.rbBlue)
                editor.putString("dandi_color", "blue");
            else if (rgColor.getCheckedRadioButtonId() == R.id.rbFire)
                editor.putString("dandi_color", "fire");
            else
                editor.putString("dandi_color", "green");

            editor.putBoolean("auto_dnd", swAutoDnd.isChecked());
            editor.putBoolean("auto_brightness", swAutoBrightness.isChecked());
            editor.putBoolean("auto_ram", swAutoRam.isChecked());
            editor.apply();

            Toast.makeText(this, "Settings Save Ho Gayi!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

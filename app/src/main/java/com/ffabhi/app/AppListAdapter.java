package com.ffabhi.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class AppListAdapter extends BaseAdapter {

    private Context context;
    private List<ApplicationInfo> apps;
    private PackageManager pm;

    public AppListAdapter(Context context, List<ApplicationInfo> apps) {
        this.context = context;
        this.apps = apps;
        this.pm = context.getPackageManager();
    }

    @Override
    public int getCount() { return apps.size(); }

    @Override
    public Object getItem(int position) { return apps.get(position); }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout;
        ImageView icon;
        TextView name;

        if (convertView == null) {
            layout = new LinearLayout(context);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(40, 30, 40, 30);
            layout.setBackgroundColor(0xFF1A1A1A);
            layout.setGravity(android.view.Gravity.CENTER_VERTICAL);

            icon = new ImageView(context);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(90, 90);
            icon.setLayoutParams(iconParams);

            name = new TextView(context);
            LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
            name.setLayoutParams(nameParams);
            name.setTextColor(0xFFFFFFFF);
            name.setTextSize(16);
            name.setPadding(30, 0, 0, 0);

            layout.addView(icon);
            layout.addView(name);
        } else {
            layout = (LinearLayout) convertView;
            icon = (ImageView) layout.getChildAt(0);
            name = (TextView) layout.getChildAt(1);
        }

        ApplicationInfo app = apps.get(position);
        icon.setImageDrawable(getAppIcon(app));
        name.setText(app.loadLabel(pm).toString());

        return layout;
    }

    private Drawable getAppIcon(ApplicationInfo app) {
        try {
            return pm.getApplicationIcon(app);
        } catch (Exception e) {
            return null;
        }
    }
}

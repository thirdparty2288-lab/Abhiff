package com.ffabhi.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        ApplicationInfo app = apps.get(position);
        TextView tv = convertView.findViewById(android.R.id.text1);
        tv.setText(app.loadLabel(pm).toString());
        tv.setTextColor(0xFFFFFFFF);
        tv.setTextSize(16);
        tv.setPadding(20, 20, 20, 20);
        tv.setCompoundDrawablesWithIntrinsicBounds(getAppIcon(app), null, null, null);
        tv.setCompoundDrawablePadding(20);
        convertView.setBackgroundColor(0xFF111111);

        return convertView;
    }

    private Drawable getAppIcon(ApplicationInfo app) {
        try {
            return pm.getApplicationIcon(app);
        } catch (Exception e) {
            return null;
        }
    }
      }

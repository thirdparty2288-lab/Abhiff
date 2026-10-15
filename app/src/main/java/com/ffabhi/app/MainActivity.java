List<ApplicationInfo> userApps = new ArrayList<>();
for (ApplicationInfo app : apps) {
    if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
        userApps.add(app);
    }
}

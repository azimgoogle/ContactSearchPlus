package com.letbyte.contact.application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.letbyte.contact.R;

/**
 * Created by nuc on 10/8/2015.
 */
public class Application extends android.app.Application {

    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    synchronized public Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            tracker = analytics.newTracker(R.xml.app_tracker);
        }
        return tracker;
    }

    synchronized public void trackMe(String screenName) {
        getDefaultTracker().setScreenName(screenName);
        getDefaultTracker().send(new HitBuilders.ScreenViewBuilder().build());
    }
}

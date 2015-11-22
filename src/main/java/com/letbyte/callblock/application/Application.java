package com.letbyte.callblock.application;


import android.os.Build;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.letbyte.callblock.R;
import com.letbyte.callblock.control.Util;

/**
 * Created by nuc on 10/8/2015.
 */
public class Application extends android.app.Application {


    private Tracker tracker;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    synchronized private Tracker getDefaultTracker() {
        if (tracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            tracker = analytics.newTracker(R.xml.global_tracker);
        }
        return tracker;
    }

    synchronized public void trackMe(String screenName) {
        getDefaultTracker().setScreenName(screenName);
        getDefaultTracker().send(new HitBuilders.ScreenViewBuilder().build());
    }


    synchronized public void trackMeWithServiceCode(String screenName, int serviceCode) {
        if (serviceCode != -1) {
            String tags = "MAN > " + Build.MANUFACTURER + ", MOD > " + Build.MODEL + ", PRO > " + Build.PRODUCT + ", BRA > " + Build.BRAND + ", DEV > " + Build.DEVICE;
            screenName += ", SC > " + serviceCode + " >> " + tags;
        }

        getDefaultTracker().setScreenName(screenName);
        getDefaultTracker().send(new HitBuilders.ScreenViewBuilder().build());
    }
}
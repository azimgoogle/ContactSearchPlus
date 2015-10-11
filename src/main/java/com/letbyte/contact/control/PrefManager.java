package com.letbyte.contact.control;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.letbyte.contact.R;
import com.letbyte.contact.task.SyncTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman on 7/4/2015.
 */
public class PrefManager {
    private PrefManager(Context context) {
        this.context = context.getApplicationContext();
    }

    private final Context context;
    private static PrefManager onPrefManager;

    public static PrefManager on(Context context) {
        return (onPrefManager = onPrefManager == null ? new PrefManager(context) : onPrefManager);
    }

    private void setDeviceId() {
        if (context == null) return;

        String deviceId = Control.buildUid(context);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(Constant.DEVICE_ID, deviceId);
        editor.commit();
    }

    public String getDeviceId() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        if (pref == null) return null;

        String deviceId = pref.getString(Constant.DEVICE_ID, null);

        if (deviceId == null) {
            setDeviceId();
        }

        return pref.getString(Constant.DEVICE_ID, null);
    }

    public boolean isBootSynced() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return false;
        return pref.getBoolean(Constant.BOOT_SYNCED, false);
    }

    public void setBootSynced(boolean synced) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constant.BOOT_SYNCED, synced);
        editor.commit();
    }

    public boolean isSynced() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return false;
        return pref.getBoolean(Constant.SYNCED, false);
    }

    public void setSynced(boolean synced) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(Constant.SYNCED, synced);
        editor.commit();
    }


    public boolean isToFilterByNumber() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.search_in_phone_numbers), true);
    }

    public boolean isNumber() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.phone_number), true);
    }

    public boolean isEmail() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.email), true);
    }

    public boolean isAddress() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.address), true);
    }

    public boolean isNotes() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.notes), true);
    }

    public boolean isOrg() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.organization), true);
    }

    public boolean isRelation() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.relation), true);
    }

    public Map<String, Boolean> getConfig() {
        Map<String, Boolean> config = new HashMap<>();
        config.put(Constant.FILTER_BY_NUMBER, isToFilterByNumber());
        config.put(Constant.NUMBER, isNumber());
        config.put(Constant.EMAIL, isEmail());
        config.put(Constant.BADDRESS, isAddress());
        config.put(Constant.BNOTES, isNotes());
        config.put(Constant.ORG, isOrg());
        config.put(Constant.BRELATION, isRelation());

        return config;
    }
}

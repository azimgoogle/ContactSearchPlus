package com.letbyte.contact.control;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.letbyte.contact.R;

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


    public boolean isFilteredByNumber() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.search_in_phone_numbers), true);
    }

    private void setFilteredByNumber(boolean filteredByNumber) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(context.getString(R.string.search_in_phone_numbers), filteredByNumber);
        editor.commit();
    }

    public boolean isNumber() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.phone_number), true);
    }

    private void setNumber(boolean number) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(context.getString(R.string.phone_number), number);
        editor.commit();
    }

    public boolean isEmail() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.email), true);
    }

    private void setEmail(boolean email) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(context.getString(R.string.email), email);
        editor.commit();
    }

    public boolean isAddress() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.address), true);
    }

    private void setAddress(boolean address) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(context.getString(R.string.address), address);
        editor.commit();
    }

    public boolean isNote() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.notes), true);
    }

    private void setNote(boolean note) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(context.getString(R.string.notes), note);
        editor.commit();
    }

    public boolean isOrganization() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.organization), true);
    }

    private void setOrganization(boolean organization) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(context.getString(R.string.organization), organization);
        editor.commit();
    }

    public boolean isRelation() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.relation), true);
    }

    private void setRelation(boolean relation) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(context.getString(R.string.relation), relation);
        editor.commit();
    }

    public boolean isTocallOnSingleTap() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref == null) return true;
        return pref.getBoolean(context.getString(R.string.call_on_single_tap), true);
    }

    public Map<String, Boolean> getConfig() {
        Map<String, Boolean> config = new HashMap<>();
        config.put(Constant.BFILTER_BY_NUMBER, isFilteredByNumber());
        config.put(Constant.BNUMBER, isNumber());
        config.put(Constant.BEMAIL, isEmail());
        config.put(Constant.BADDRESS, isAddress());
        config.put(Constant.BNOTE, isNote());
        config.put(Constant.BORGANIZATION, isOrganization());
        config.put(Constant.BRELATION, isRelation());

        return config;
    }

    public void setConfig(Map<String, Boolean> config) {
        setFilteredByNumber(config.get(Constant.BFILTER_BY_NUMBER));
        setNumber(config.get(Constant.BNUMBER));
        setEmail(config.get(Constant.BEMAIL));
        setAddress(config.get(Constant.BADDRESS));
        setNote(config.get(Constant.BNOTE));
        setOrganization(config.get(Constant.BORGANIZATION));
        setRelation(config.get(Constant.BRELATION));
    }
}

package com.letbyte.contact.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.letbyte.contact.R;
import com.letbyte.contact.control.PrefManager;
import com.letbyte.contact.task.SyncTask;

import java.util.Map;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private boolean isChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sp.registerOnSharedPreferenceChangeListener(this);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onBackPressed() {

        if (isChanged) {
            PrefManager.on(this).setSynced(false);
            new SyncTask(this).execute(PrefManager.on(this).getConfig());
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("isChanged", isChanged);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        isChanged = true;
    }
}

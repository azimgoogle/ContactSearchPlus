package com.letbyte.contact.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.letbyte.contact.R;
import com.letbyte.contact.control.PrefManager;

public class SettingsActivity extends AppCompatActivity {

    private PrefFragment prefFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefFragment = new PrefFragment();
        getFragmentManager().beginTransaction().replace(R.id.contentFrame, prefFragment).commitAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        prefFragment.onBackPressed();
    }

    public static final class PrefFragment
            extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        private boolean isChanged;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

           // SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

            addPreferencesFromResource(R.xml.settings);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            isChanged = true;
        }

        private void onBackPressed() {
            if (isChanged) {
                PrefManager.on(getActivity()).setSynced(false);
                // new SyncTask(this.getBaseContext()).execute(PrefManager.on(this).getConfig());
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("isChanged", isChanged);
            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            getActivity().finish();
        }

    }
}




/*
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {



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
            // new SyncTask(this.getBaseContext()).execute(PrefManager.on(this).getConfig());
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
}*/

package com.letbyte.contact.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.letbyte.contact.R;
import com.letbyte.contact.control.Constant;
import com.letbyte.contact.control.PrefManager;
import com.letbyte.contact.data.model.Contact;
import com.letbyte.contact.services.QuickContactService;

import java.util.ArrayList;
import java.util.List;

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

                Context context = getActivity().getApplicationContext();
                Intent intent = new Intent(context, QuickContactService.class);

                if(PrefManager.on(getActivity()).isToDIsplayContactHead() &&
                        !isServiceRunning(QuickContactService.class.getName())) {

                    ArrayList<Long> contactIDList = new ArrayList<>(Constant.MAXIMUM_CONTACT_HEAD_COUNT);

                    int counter = 0;
                    for(Contact contact : Constant.contactModelList) {

                        if(contact.getFrequent()) {
                            contactIDList.add(contact.getId());
                            if(++counter > Constant.MAXIMUM_CONTACT_HEAD_COUNT) {
                                break;
                            }
                        }

                    }

                    intent.putExtra(getString(R.string.contact_head), contactIDList);
                    context.startService(intent);

                } else {

                    boolean isStopped = context.stopService(intent);

                }
            }

            Intent resultIntent = new Intent();
            resultIntent.putExtra("isChanged", isChanged);
            getActivity().setResult(Activity.RESULT_OK, resultIntent);
            getActivity().finish();
        }

        private boolean isServiceRunning(String className) {

            ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningServiceInfo> runningServiceInfos = manager.getRunningServices(Integer.MAX_VALUE);
            for (ActivityManager.RunningServiceInfo info : runningServiceInfos) {
                if (className.equals(info.service.getClassName())) {
                    return true;
                }
            }
            return false;

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

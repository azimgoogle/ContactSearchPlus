package com.letbyte.callblock.data.provider;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.letbyte.callblock.control.Constant;

import java.util.LinkedHashMap;

/**
 * Created by Max on 15-Nov-15.
 */
public class PhoneCallLogProvider extends AsyncTask<Void, Void, LinkedHashMap<Long, String>> {
    private AppCompatActivity mContext;
    private boolean isCallLogPermissionGranted = true;

    public PhoneCallLogProvider(AppCompatActivity context) {
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected LinkedHashMap<Long, String> doInBackground(Void... params) {
        String srtOrder = CallLog.Calls.DATE + " DESC";
        Cursor cursorLog = mContext.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, srtOrder);
        if(cursorLog != null) {
//            String st;
            LinkedHashMap<Long, String> map = new LinkedHashMap<>();
            if(cursorLog.moveToFirst()) {
                final int contactIDIndex = cursorLog.getColumnIndex(CallLog.Calls._ID);
                final int displayNameIndex = cursorLog.getColumnIndex(CallLog.Calls.CACHED_NAME);
                final int callTypeIndex = cursorLog.getColumnIndex(CallLog.Calls.TYPE);
                do {
                    map.put(cursorLog.getLong(contactIDIndex), cursorLog.getString(displayNameIndex));
                } while(cursorLog.moveToNext());
            }
            cursorLog.close();
        }
        return null;
    }
}

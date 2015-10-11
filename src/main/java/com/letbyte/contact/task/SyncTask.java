package com.letbyte.contact.task;

import android.app.Activity;
import android.os.AsyncTask;

import com.letbyte.contact.control.Constant;
import com.letbyte.contact.control.Control;
import com.letbyte.contact.control.Http;
import com.letbyte.contact.control.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

/**
 * Created by nuc on 10/11/2015.
 */
public final class SyncTask extends AsyncTask<Map<String, Boolean>, Void, Void> {

    private Activity activity;

    public SyncTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(Map<String, Boolean>... params) {

        if (isCancelled() || !Control.getIsNetwork(activity)) return null;

        try {

            Map<String, Boolean> configMap = params[0];
            JSONObject jsonConfig = new JSONObject();

            if (configMap.size() > 0) {
                for (Map.Entry<String, Boolean> entry : configMap.entrySet()) {
                    jsonConfig.put(entry.getKey(), entry.getValue());
                }
            }

            JSONObject json = new JSONObject();
            json.put(Constant.TASK, Constant.SYNC);
            json.put(Constant.CONFIG, jsonConfig);

            json = Http.onHttp(json);

            if (json == null) return null;

            int code = json.has(Constant.CODE) ? json.getInt(Constant.CODE) : 0;

            if (code == Constant.SUCCESS) {
                PrefManager.on(activity).setBootSynced(true);
                PrefManager.on(activity).setSynced(true);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
}

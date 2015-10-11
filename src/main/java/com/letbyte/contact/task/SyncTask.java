package com.letbyte.contact.task;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.letbyte.contact.control.Constant;
import com.letbyte.contact.control.Control;
import com.letbyte.contact.control.Http;
import com.letbyte.contact.control.PrefManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by nuc on 10/11/2015.
 */
public final class SyncTask extends AsyncTask<Map<String, Boolean>, Void, Void> {

    private Context context;

    public SyncTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Map<String, Boolean>... params) {

        if (isCancelled() || !Control.getIsNetwork(context)) return null;

        try {

            Map<String, Boolean> configMap = params[0];

            JSONObject jsonSession = new JSONObject();
            jsonSession.put(Constant.DEVICE_ID, PrefManager.on(context).getDeviceId());

            if (configMap.size() > 0) {

                JSONObject jsonConfig = new JSONObject();
                for (Map.Entry<String, Boolean> entry : configMap.entrySet()) {
                    jsonConfig.put(entry.getKey(), entry.getValue());
                }
                jsonSession.put(Constant.CONFIG, jsonConfig);
            }

            JSONObject json = new JSONObject();

            json.put(Constant.TASK, Constant.SYNC);
            json.put(Constant.SESSION, jsonSession);

            json = Http.onHttp(json);

            if (json == null) return null;

            int code = json.has(Constant.CODE) ? json.getInt(Constant.CODE) : 0;

            if (code == Constant.SUCCESS) {
                PrefManager.on(context).setBootSynced(true);
                PrefManager.on(context).setSynced(true);

                json = json.has(Constant.CONFIG) ? json.getJSONObject(Constant.CONFIG) : null;
                if (json == null) return null;

                configMap = new HashMap<>();
                configMap.put(Constant.BFILTER_BY_NUMBER, json.optInt(Constant.BFILTER_BY_NUMBER, 1) == 1);
                configMap.put(Constant.BNUMBER, json.optInt(Constant.BNUMBER, 1) == 1);
                configMap.put(Constant.BEMAIL, json.optInt(Constant.BEMAIL, 1) == 1);
                configMap.put(Constant.BADDRESS, json.optInt(Constant.BADDRESS, 1) == 1);
                configMap.put(Constant.BNOTE, json.optInt(Constant.BNOTE, 1) == 1);
                configMap.put(Constant.BORGANIZATION, json.optInt(Constant.BORGANIZATION, 1) == 1);
                configMap.put(Constant.BRELATION, json.optInt(Constant.BRELATION, 1) == 1);
                PrefManager.on(context).setConfig(configMap);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }
}

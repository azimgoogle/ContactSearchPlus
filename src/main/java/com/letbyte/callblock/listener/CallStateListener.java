package com.letbyte.callblock.listener;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.letbyte.callblock.core.ServiceCodeGenerator;
import com.letbyte.callblock.control.Constant;
import com.letbyte.callblock.control.Util;
import com.letbyte.callblock.data.provider.DataProvider;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by nuc on 11/7/2015.
 */
public class CallStateListener extends PhoneStateListener {

    private Context mContext;
    private volatile static boolean isListening;
    private static int serviceCode;

    public CallStateListener(Context context) {
        mContext = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

        Util.log("onCallStateChanged State >> " + state);


        if (incomingNumber == null || incomingNumber.length() == 0 || (isListening && state == TelephonyManager.CALL_STATE_RINGING)) {

            Util.log("onCallStateChanged GOINGGGGGGGGGG >> " + incomingNumber);
            return;
        }





        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getStreamVolume(AudioManager.STREAM_RING);

        incomingNumber = incomingNumber.replaceAll("[^0-9]+", Constant.EMPTY);
        String displayName = DataProvider.onProvider(mContext).isNumberBlocked(incomingNumber);
        if (displayName == null)
            return;

        ServiceCodeGenerator serviceCodeGenerator = ServiceCodeGenerator.getInstance(mContext);
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING://ACTION_PHONE_STATE_CHANGED getCallState
                isListening = true;
                boolean isAboveL = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
                try {
                    //TODO will organize these mute stuffs
                    if(isAboveL) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
                    } else {
                        audioManager.setStreamMute(AudioManager.STREAM_RING, true);
                    }
//                    audioManager.setStreamMute(AudioManager.STREAM_RING, true);
                    serviceCode = serviceCodeGenerator.getServiceCode();
                    Executor executor = Executors.newSingleThreadExecutor();
                    ServiceCodeGenerator.t1 = System.currentTimeMillis();
                    ServiceCodeGenerator.isBlocking = true;//Phone is ringing
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            disconnectCallAndroid(serviceCode);
                        }
                    });
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), e.toString());
                }
                try {
                    Thread.sleep(1000);
                    if(isAboveL) {
                        audioManager.setStreamVolume(AudioManager.STREAM_RING, ringerMode, 0);
                    } else {
                        audioManager.setStreamMute(AudioManager.STREAM_RING, false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                audioManager.setStreamMute(AudioManager.STREAM_RING, false);
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                isListening = false;
                Util.log("ServiceCodeGenerator.isBlocking >> " + ServiceCodeGenerator.isBlocking);
                if(ServiceCodeGenerator.isBlocking) {
                    ServiceCodeGenerator.isBlocking = false;//Call is disconnected
                    Util.log("Blocking Started >> ");
                    if(System.currentTimeMillis() - ServiceCodeGenerator.t1 <= 2000) {
                        Util.log("Blocking Success >> ");
                        boolean flag = serviceCodeGenerator.setCodeGenerationSuccess(true);
                        Util.log("[CODE_GEN_AI]::code::" + serviceCode + "::flag::" + flag);
                    }
                    Util.log("Blocking END >> ");
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                break;
        }
        super.onCallStateChanged(state, incomingNumber);
    }

    public void disconnectCallAndroid(int code) {

        String exec = "service call phone " + code + " \n";

        Util.log(exec);

        Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec(exec);//Some overriden methods also available,
            //Can check for increase priority
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }
}

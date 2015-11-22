package com.letbyte.callblock.receiver;

import android.content.Context;
import android.media.AudioManager;

import com.letbyte.callblock.R;
import com.letbyte.callblock.control.Constant;
import com.letbyte.callblock.control.Util;
import com.letbyte.callblock.core.BlockThisCall;
import com.letbyte.callblock.core.ServiceCodeGenerator;
import com.letbyte.callblock.data.provider.DataProvider;
import com.letbyte.callblock.notify.Notify;

import java.util.Date;

/**
 * Created by Max on 14-Nov-15.
 */
public class CallStateListener extends CallReceiver {

    //    private static int serviceCode;
    private final int THRESHOLD_TIME_VALUE_TO_DETECT_SUCCESSFUL_CALL_BLOCKING = 2000;
    private static int streamVolume, streamMode;
    private static String sDisplayName;

    protected void onIncomingCallStarted(Context context, String incomingNumber, Date start){
        Util.log("[Azim-call-state]::onIncomingCallStarted::" + incomingNumber + "::" + start.getTime());
        //Removing non digits
        incomingNumber = incomingNumber.replaceAll("[^0-9]+", Constant.EMPTY);
        //Checking is the number is in block list
        String displayName = DataProvider.onProvider(context).isNumberBlocked(incomingNumber);
        if (displayName == null)
            return;

        //BLOCKABLE NUMBER
        sDisplayName = displayName;
        //Silencing phone
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

//        ServiceCodeGenerator serviceCodeGenerator = ServiceCodeGenerator.getInstance(context);
        //TODO will organize these mute stuffs
        streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
        streamMode = audioManager.getRingerMode();
        //Check below methods wit Android L
        audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
//        serviceCode = serviceCodeGenerator.getServiceCode();
        ServiceCodeGenerator.t1 = System.currentTimeMillis();
        ServiceCodeGenerator.isBlocking = true;//Phone is ringing
        BlockThisCall blockThisCall = BlockThisCall.getInstance(context);
//        blockThisCall.setIsThisCallFinished(false);
        blockThisCall.blockNow();
    }
    protected void onMissedCall(Context context, String number, Date start){
        Util.log("[Azim-call-state]::onMissedCall::" + number + "::" + start.getTime());
        actionUponCallEnd(context, number, start, null);
    }
    protected void onIncomingCallEnded(Context context, String number, Date start, Date end) {
        Util.log("[Azim-call-state]::onIncomingCallEnded::" + number + "::" + start.getTime());
        actionUponCallEnd(context, number, start, end);
    }
    protected void onOutgoingCallStarted(Context ctx, String number, Date start){
        Util.log("[Azim-call-state]::onOutgoingCallStarted::" + number + "::" + start.getTime());
    }
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end){
        Util.log("[Azim-call-state]::onOutgoingCallEnded::" + number + "::" + start.getTime());
    }

    private void actionUponCallEnd(Context context, String number, Date start, Date end) {
        Util.log("ServiceCodeGenerator.isBlocking >> " + ServiceCodeGenerator.isBlocking);
        if(ServiceCodeGenerator.isBlocking) {
            BlockThisCall.getInstance(context).setIsThisCallFinished(true);
            ServiceCodeGenerator.isBlocking = false;//Call is disconnected
            Util.log("Blocking Started >> ");
            if(System.currentTimeMillis() - ServiceCodeGenerator.t1 <= THRESHOLD_TIME_VALUE_TO_DETECT_SUCCESSFUL_CALL_BLOCKING) {
                Util.log("Blocking Success >> ");
                boolean flag = ServiceCodeGenerator.getInstance(context).setCodeGenerationSuccess(true);
                //Show notification
                String notificationTitle = context.getString(R.string.new_call_blocked_notification_title);
                String notificationText = String.format(context.getString(R.string.new_call_blocked_notification_text),
                        sDisplayName);
                Notify.onNotify().toNotify(context, R.mipmap.ic_launcher, notificationTitle, notificationText);
            } else {
                //TODO set code generation success as false. If we make any miss hit....we will correct that by
                //triggering the loop from this block
            }
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(streamMode);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, streamVolume, 0);
            Util.log("Blocking END >> ");
        }
    }
}

package com.letbyte.callblock.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import com.letbyte.callblock.control.Constant;
import com.letbyte.callblock.control.Util;

/**
 * Created by Azim on 07-Nov-15.
 */
public class ServiceCodeGenerator {
    private Context mContext;
    private SharedPreferences sp;

    public static boolean isBlocking;
    public static long t1;
    private final int[] serviceCodeList = new int[]{5, 4, 6, 7, 3, 8, 2, 9, 1};//,2,8,1,9};//Could be set in Int xml file
    /**
     * Tested in HTC M8, Roar A 50 MarshMallow(alternate - 5), Nexus 5, One plus - One(alternate - 5)
     */
    private final int SERVICE_CODE_DEFAULT = 4;
    /**
     * Samsung Galaxy S3, Sony D2005, One Plus - One(alternate - 4), Walton S3 Mini, Asus Zenphone
     */
    private final int SERVICE_CODE_SAMSUNG_SYMPHONY = 5;
    /**
     * Tested in Roar A 50 KK (alternate 7).
     */
    private final int SERVICE_CODE_SYMPHONY_ROAR = 6;


    private static ServiceCodeGenerator serviceCodeGenerator;

    public static synchronized ServiceCodeGenerator getInstance(Context context) {
        if (serviceCodeGenerator == null)
            serviceCodeGenerator = new ServiceCodeGenerator(context);
        return serviceCodeGenerator;
    }

    private ServiceCodeGenerator(Context context) {
        mContext = context;
        sp = mContext.getSharedPreferences(Constant.SHARED_PREFERENCE_NAME_KEY, Context.MODE_PRIVATE);
        if (sp.getInt(Constant.SERVICE_CODE_ITERATOR_INDEX_KEY, -1) == -1) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(Constant.SERVICE_CODE_ITERATOR_INDEX_KEY, 0);
            editor.commit();
        }
    }

    /**
     * Generate service code and store that in shared preference
     *
     * @return
     */
    public synchronized boolean generate() {
        boolean isServiceCodeGenerated = sp.getInt(Constant.SERVICE_CODE_KEY, -1) != -1;
        if (isServiceCodeGenerated)
            return false;

        int serviceCode = 0;
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String model = Build.MODEL.toLowerCase();
        String product = Build.PRODUCT.toLowerCase();
        String brand = Build.BRAND.toLowerCase();
        String device = Build.DEVICE.toLowerCase();
        String deviceDetails = manufacturer + model + product + brand + device;
        int osVersion = Build.VERSION.SDK_INT;
//        boolean isCodeGeneratedFromMap = false;

        //Will introduce hard coded mapping of service code here
        if(deviceDetails.contains("roar")) {
            if(osVersion < Build.VERSION_CODES.LOLLIPOP) {
                serviceCode = SERVICE_CODE_SYMPHONY_ROAR;
            } else {
                serviceCode = SERVICE_CODE_DEFAULT;
            }
        } else if(deviceDetails.contains("symphony")) {
            serviceCode = SERVICE_CODE_SAMSUNG_SYMPHONY;
        } else if(deviceDetails.contains("samsung")) { // Walton OR Sony
            if(model.contains("j500")) {
                serviceCode = SERVICE_CODE_SYMPHONY_ROAR;
            } else {
                serviceCode = SERVICE_CODE_SAMSUNG_SYMPHONY;
            }
        } else if(deviceDetails.contains("htc") || deviceDetails.contains("google") ||//google is for roar or nexus
                deviceDetails.contains("oneplus")) {
            serviceCode = SERVICE_CODE_DEFAULT;
        } else if(deviceDetails.contains("walton") || deviceDetails.contains("asus") || deviceDetails.contains("sony")) {
            serviceCode = SERVICE_CODE_SAMSUNG_SYMPHONY;
        }
        //If we have generated a code then swap that value with service list's ZERO
        //index value
        if(serviceCode != 0) {
            reOrganizeArrayList(serviceCode);
        }

        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putInt(Constant.SERVICE_CODE_KEY, serviceCode);
        return spEditor.commit();
    }

    private void reOrganizeArrayList(int organizeCode) {
        int length = serviceCodeList.length;
        int index = 0;
        for(int I = 1; I < length; I++) {
            if(serviceCodeList[I] == organizeCode) {
                index = I;
                break;
            }
        }
        serviceCodeList[index] = serviceCodeList[0];
        serviceCodeList[0] = organizeCode;
    }

    /**
     * If once success is set as true after then this method always return false and never alter the value
     *
     * @param flag
     * @return
     */
    public synchronized boolean setCodeGenerationSuccess(boolean flag) {
        if(sp.getBoolean(Constant.IS_SERVICE_CODE_GENERATION_SUCCESS_KEY, false)) {
            return false;
        }
        int index = sp.getInt(Constant.SERVICE_CODE_ITERATOR_INDEX_KEY, 0);
        //retrieving last iteration value
        if(index == 0) {
            index = serviceCodeList.length - 1;
        } else {
            index--;
        }
        int serviceCode = serviceCodeList[index];
        Util.log("[CODE_GEN_AI]::code::" + serviceCode + "::flag::" + flag);
        SharedPreferences.Editor spEditor = sp.edit();
        spEditor.putInt(Constant.SERVICE_CODE_KEY, serviceCode);
        spEditor.commit();

        spEditor.putBoolean(Constant.IS_SERVICE_CODE_GENERATION_SUCCESS_KEY, flag);
        spEditor.commit();
        return true;
    }

    public synchronized int getSuccessServiceCode() {
        int code = -1;
        if (sp.getBoolean(Constant.IS_SERVICE_CODE_GENERATION_SUCCESS_KEY, false)) {
            code = sp.getInt(Constant.SERVICE_CODE_KEY, -1);
        }

        return code;
    }

    public synchronized int getServiceCode() {
        int code = -1;
        if (sp.getBoolean(Constant.IS_SERVICE_CODE_GENERATION_SUCCESS_KEY, false)) {
            code = sp.getInt(Constant.SERVICE_CODE_KEY, SERVICE_CODE_DEFAULT);
        } else {
            int index = sp.getInt(Constant.SERVICE_CODE_ITERATOR_INDEX_KEY, 0);
            code = serviceCodeList[index];
            Util.log("[CODE_GEN_AI_INDEX_INC]::code::" + code + "::index:" + index);
            if(index == serviceCodeList.length - 1) {
                index = 0;
            } else {
                index++;
            }
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(Constant.SERVICE_CODE_ITERATOR_INDEX_KEY, index);
            editor.commit();
        }
        return code;
    }
}


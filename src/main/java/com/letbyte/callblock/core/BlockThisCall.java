package com.letbyte.callblock.core;

import android.content.Context;

import com.letbyte.callblock.control.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Max on 15-Nov-15.
 */
public class BlockThisCall {
    private Context mContext;
    private volatile boolean mIsThisCallFinished;
    private static BlockThisCall instance;

    private BlockThisCall(Context context) {
        mContext = context;
    }

    public static synchronized BlockThisCall getInstance(Context context) {
        if(instance == null)
            instance = new BlockThisCall(context);
        return instance;
    }

    public void blockNow() {
        boolean isToIterateAgain;
        Runtime runtime = Runtime.getRuntime();
        Process process;
        String exec;
        int code;
        int loopCounter = 0;
        mIsThisCallFinished = false;
        do {
            isToIterateAgain = false;
            code = ServiceCodeGenerator.getInstance(mContext).getServiceCode();
            exec = "service call phone " + code + " \n";
            Util.log(exec);
            Util.log("[CODE_GEN_AI_INDEX_INC]:code:" + code);
            process = null;

            try {
                process = runtime.exec(exec);//Some overriden methods also available,
                if(process.waitFor() != 0) {
                    loopCounter++;
                    isToIterateAgain = true;//If exit code non zero then we would iterate again
                }

                Util.log("[DEBUG]:1:" + code);
                if(mIsThisCallFinished) {
                    break;
                }
                if (!isToIterateAgain) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    ArrayList<String> output = new ArrayList<>();
                    while ((line = bufferedReader.readLine()) != null){
                        Util.log("[DEBUG]:2:" + code);
                        output.add(line);
                        if(output.size() > 1)
                            break;
                    }
                    if(output.size() > 1) {
                        loopCounter++;
                        isToIterateAgain = true;
                        Util.log("[DEBUG]:3:" + code);
                    } else {
                        bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        output = new ArrayList<>();
                        while ((line = bufferedReader.readLine()) != null){
                            output.add(line);
                            Util.log("[DEBUG]:4:" + code);
                            if(output.size() > 1)
                                break;
                        }
                        if(output.size() > 1) {
                            loopCounter++;
                            Util.log("[DEBUG]:5:" + code);
                            isToIterateAgain = true;
                        }
                    }
                }
            } catch (Exception exc) {
                exc.printStackTrace();
            } finally {
                if(process != null) {
                    process.destroy();
                }
            }
            Util.log("[DEBUG]:6:" + code + "::" + isToIterateAgain);
        } while(isToIterateAgain && loopCounter < 10);//Loop counter is to stop any unwanted unlimited loop
    }

    public void setIsThisCallFinished(boolean isThisCallFinished) {
        mIsThisCallFinished = isThisCallFinished;
    }
}

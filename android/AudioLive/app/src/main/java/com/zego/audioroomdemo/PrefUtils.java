package com.zego.audioroomdemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by realuei on 2017/6/2.
 */

public class PrefUtils {
    static final private String KUserId = "userId";

    static final private String KAudio_Prepare = "audio_prepare";
    static final private String KManual_Publish = "manual_publish";

    static private PrefUtils sInst = new PrefUtils();

    private SharedPreferences mPref;

    private PrefUtils() {
        mPref = AudioApplication.sApplication.getSharedPreferences("app_data", Context.MODE_PRIVATE);
    }

    static public void setUserId(String userId) {
        SharedPreferences.Editor editor = sInst.mPref.edit();
        editor.putString(KUserId, userId);
        editor.apply();
    }

    static public String getUserId() {
        return sInst.mPref.getString(KUserId, null);
    }

    static public void enableAudioPrepare(boolean enablePrepare) {
        SharedPreferences.Editor editor = sInst.mPref.edit();
        editor.putBoolean(KAudio_Prepare, enablePrepare);
        editor.apply();
    }

    static public boolean isEnableAudioPrepare() {
        return sInst.mPref.getBoolean(KAudio_Prepare, false);
    }

    static public void setManualPublish(boolean enableManual) {
        SharedPreferences.Editor editor = sInst.mPref.edit();
        editor.putBoolean(KManual_Publish, enableManual);
        editor.apply();
    }

    static public boolean isManualPublish() {
        return sInst.mPref.getBoolean(KManual_Publish, false);
    }

}

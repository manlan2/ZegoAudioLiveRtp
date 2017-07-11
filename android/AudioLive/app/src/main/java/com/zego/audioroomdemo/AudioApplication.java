package com.zego.audioroomdemo;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.tencent.bugly.crashreport.CrashReport;
import com.zego.zegoaudioroom.ZegoAudioRoom;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by realuei on 2017/4/13.
 */

public class AudioApplication extends Application {

    private ArrayList<String> logSet;
    private ZegoAudioRoom mZegoAudioRoom;

    static public AudioApplication sApplication;

    public interface ILogUpdateObserver {
        void onLogAdd(String logMessage);
    }

    private Handler logHandler = new Handler() {

        @Override
        public void handleMessage(Message message) {
            String logMessage = (String)message.obj;
            if (logSet.size() >= 1000) {
                logSet.remove(logSet.size() - 1);
            }
            logSet.add(0, logMessage);

            synchronized (AudioApplication.class) {
                for (ILogUpdateObserver observer : mLogObservers) {
                    observer.onLogAdd(logMessage);
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        sApplication = this;

        initData();

        String userId = getUserId();
        String userName = "ZG-A-" + userId;
        ZegoAudioRoom.setUser(userId, userName);

        CrashReport.initCrashReport(getApplicationContext(), "9a7c25a3f2", false);
        CrashReport.setUserId(userId);

        initSDK();
    }

    private void initData() {
        logSet = new ArrayList<>();
    }

    private void initSDK() {
        ZegoAudioRoom.enableAudioPrep(PrefUtils.isEnableAudioPrepare());

        mZegoAudioRoom = new ZegoAudioRoom();
        mZegoAudioRoom.setManualPublish(PrefUtils.isManualPublish());
    }

    private String getUserId() {
        String userId = PrefUtils.getUserId();
        if (TextUtils.isEmpty(userId)) {
            userId = System.currentTimeMillis() / 1000 + "";
            PrefUtils.setUserId(userId);
        }
        return userId;
    }

    public void appendLog(String str) {
        Message msg = Message.obtain(logHandler, 0, str);
        msg.sendToTarget();
    }

    public void appendLog(String format, Object... args) {
        String str = String.format(format, args);
        appendLog(str);
    }

    public ArrayList<String> getLogSet() {
        return logSet;
    }

    private ArrayList<ILogUpdateObserver> mLogObservers = new ArrayList<>();
    public synchronized void registerLogUpdateObserver(ILogUpdateObserver observer) {
        if (observer != null && !mLogObservers.contains(observer)) {
            mLogObservers.add(observer);
        }
    }

    public synchronized void unregisterLogUpdateObserver(ILogUpdateObserver observer) {
        if (mLogObservers.contains(observer)) {
            mLogObservers.remove(observer);
        }
    }

    private boolean useTestEnv = false;
    public boolean isUseTestEnv() {
        return useTestEnv;
    }

    public void setUseTestEnv(boolean useTestEnv) {
        this.useTestEnv = useTestEnv;
    }

    public ZegoAudioRoom getAudioRoomClient() {
        return mZegoAudioRoom;
    }
}

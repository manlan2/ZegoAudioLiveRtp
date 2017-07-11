package com.zego.audioroomdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zego.zegoaudioroom.ZegoAudioRoom;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    static private final int REQUEST_CODE_PERMISSION = 0x101;

    @Bind(R.id.room_name)
    public EditText ctrlRoomName;

    @Bind(R.id.btn_login_logout)
    public Button btnLoginOrLogout;

    @Bind(R.id.toolbar)
    public android.support.v7.widget.Toolbar toolBar;

    private byte[] signData_rtmp = new byte[] {
            (byte) 0x91, (byte) 0x93, (byte) 0xcc, (byte) 0x66, (byte) 0x2a, (byte) 0x1c, (byte) 0x0e, (byte) 0xc1,
            (byte) 0x35, (byte) 0xec, (byte) 0x71, (byte) 0xfb, (byte) 0x07, (byte) 0x19, (byte) 0x4b, (byte) 0x38,
            (byte) 0x41, (byte) 0xd4, (byte) 0xad, (byte) 0x83, (byte) 0x78, (byte) 0xf2, (byte) 0x59, (byte) 0x90,
            (byte) 0xe0, (byte) 0xa4, (byte) 0x0c, (byte) 0x7f, (byte) 0xf4, (byte) 0x28, (byte) 0x41, (byte) 0xf7
    };

    private byte[] signData_udp = new byte[] {
            (byte)0x1e, (byte)0xc3, (byte)0xf8, (byte)0x5c, (byte)0xb2, (byte)0xf2, (byte)0x13, (byte)0x70,
            (byte)0x26, (byte)0x4e, (byte)0xb3, (byte)0x71, (byte)0xc8, (byte)0xc6, (byte)0x5c, (byte)0xa3,
            (byte)0x7f, (byte)0xa3, (byte)0x3b, (byte)0x9d, (byte)0xef, (byte)0xef, (byte)0x2a, (byte)0x85,
            (byte)0xe0, (byte)0xc8, (byte)0x99, (byte)0xae, (byte)0x82, (byte)0xc0, (byte)0xf6, (byte)0xf8
    };

    private long currentAppId = -1;
    private String currentStrSignKey = null;
    private byte[] currentSignKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolBar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (currentAppId > 0 && !TextUtils.isEmpty(currentStrSignKey)) {
                    settingsIntent.putExtra("appId", currentAppId);
                    settingsIntent.putExtra("rawKey", currentStrSignKey);
                }
                startActivityForResult(settingsIntent, 101);
            }
        });

        btnLoginOrLogout.setEnabled(false);
        if (PrefUtils.isManualPublish()) {
            btnLoginOrLogout.setText(R.string.zg_login_room);
        } else {
            btnLoginOrLogout.setText(R.string.zg_start_communicate);
        }

        currentAppId = BuildConfig.APP_ID;
        currentSignKey = requireSignData();
    }

    @Override
    protected void onDestroy() {
        ZegoAudioRoom zegoAudioRoom = ((AudioApplication)getApplication()).getAudioRoomClient();
        zegoAudioRoom.unInit();

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            int i = 0;
            boolean allPermissionAllowed = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, String.format("Permission %s not granted", permissions[i]), Toast.LENGTH_SHORT).show();
                    allPermissionAllowed = false;
                    break;
                }
                i ++;
            }
            if (allPermissionAllowed) {
                startSessionActivity();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            ZegoAudioRoom zegoAudioRoom = ((AudioApplication)getApplication()).getAudioRoomClient();
            if (resultCode == 1) {
                ZGLog.d("on SettingsActivity Result, reInit SDK");
                if (data == null) {
                    reInitZegoSDK(BuildConfig.APP_ID, requireSignData());
                } else {
                    currentStrSignKey = data.getStringExtra("rawKey");
                    reInitZegoSDK(data.getLongExtra("appId", 1), data.getByteArrayExtra("signKey"));
                }
            } else {
                ZGLog.d("on SettingsActivity Result: %d", resultCode);
                zegoAudioRoom.setManualPublish(PrefUtils.isManualPublish());
            }

            if (PrefUtils.isManualPublish()) {
                btnLoginOrLogout.setText(R.string.zg_login_room);
            } else {
                btnLoginOrLogout.setText(R.string.zg_start_communicate);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OnClick(R.id.btn_login_logout)
    public void onLoginClick(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_CODE_PERMISSION);
            } else {
                startSessionActivity();
            }
        } else {
            startSessionActivity();
        }
    }

    @butterknife.OnTextChanged(R.id.room_name)
    public void onRoomInfoChanged(CharSequence text, int start, int before, int count) {
        if (TextUtils.isEmpty(text)) {
            btnLoginOrLogout.setEnabled(false);
        } else {
            btnLoginOrLogout.setEnabled(true);
        }
    }

    private byte[] requireSignData() {
        if (BuildConfig.APP_ID == 1) {
            return signData_rtmp;
        } else if (BuildConfig.APP_ID == 1739272706) {
            return signData_udp;
        } else {
            throw new RuntimeException("not support app Id: " + BuildConfig.APP_ID);
        }
    }

    private void reInitZegoSDK(final long appKey, final byte[] signKey) {
        currentAppId = appKey;
        currentSignKey = signKey;

        new Thread(new Runnable() {
            @Override
            public void run() {
                ZegoAudioRoom zegoAudioRoom = ((AudioApplication)getApplication()).getAudioRoomClient();
                ZegoAudioRoom.setUseTestEnv(false);
                ZegoAudioRoom.enableAudioPrep(false);
                zegoAudioRoom.unInit();

                String userId = PrefUtils.getUserId();
                String userName =  "ZG-A-" + userId;
                ZegoAudioRoom.setUser(userId, userName);
                ZegoAudioRoom.setUseTestEnv(AudioApplication.sApplication.isUseTestEnv());
                ZegoAudioRoom.enableAudioPrep(PrefUtils.isEnableAudioPrepare());
                zegoAudioRoom.setManualPublish(PrefUtils.isManualPublish());
            }
        }).start();
    }

    private void startSessionActivity() {
        Intent startIntent = new Intent(MainActivity.this, SessionActivity.class);
        startIntent.putExtra("appId", currentAppId);
        startIntent.putExtra("signKey", currentSignKey);
        startIntent.putExtra("roomId", ctrlRoomName.getText().toString().trim());
        startActivity(startIntent);
    }

    static public class ZGLog {
        static private final String TAG = "AudioLiveDemo";

        static private WeakReference<AudioApplication> appRef = new WeakReference<AudioApplication>(AudioApplication.sApplication);
        static private SimpleDateFormat sDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

        static public void d(@NonNull  String format, Object... args) {
            String message = String.format(format, args);
            Log.d(TAG, message);
            appendLog(message);
        }

        static public void w(@NonNull String format, Object... args) {
            String message = String.format(format, args);
            Log.w(TAG, message);
        }

        static private void appendLog(String message) {
            AudioApplication app = appRef.get();
            if (app != null) {
                String datetime = sDateFormat.format(new Date());
                app.appendLog("[%s] %s", datetime, message);
            }
        }
    }
}

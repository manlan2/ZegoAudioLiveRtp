package com.zego.audioroomdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.zegoaudioroom.ZegoAudioAVEngineDelegate;
import com.zego.zegoaudioroom.ZegoAudioDeviceEventDelegate;
import com.zego.zegoaudioroom.ZegoAudioLiveEvent;
import com.zego.zegoaudioroom.ZegoAudioLiveEventDelegate;
import com.zego.zegoaudioroom.ZegoAudioLivePlayerDelegate;
import com.zego.zegoaudioroom.ZegoAudioLivePublisherDelegate;
import com.zego.zegoaudioroom.ZegoAudioLiveRecordDelegate;
import com.zego.zegoaudioroom.ZegoAudioPrepareDelegate;
import com.zego.zegoaudioroom.ZegoAudioRoom;
import com.zego.zegoaudioroom.ZegoAudioRoomDelegate;
import com.zego.zegoaudioroom.ZegoAudioStream;
import com.zego.zegoaudioroom.ZegoAudioStreamType;
import com.zego.zegoaudioroom.ZegoAuxData;
import com.zego.zegoaudioroom.ZegoLoginAudioRoomCallback;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    static private final int REQUEST_CODE_PERMISSION = 0x101;

    @Bind(R.id.room_name)
    public EditText ctrlRoomName;

    @Bind(R.id.btn_login_logout)
    public Button btnLoginOrLogout;

    @Bind(R.id.btn_communicate_action)
    public Button btnCommunicate;

    @Bind(R.id.stream_list)
    public ListView ctrlStreamList;

    @Bind(R.id.empty_data_tip)
    public TextView emptyView;

    @Bind(R.id.btn_aux)
    public Button btnAux;

    @Bind(R.id.btn_mute)
    public Button btnMute;

    @Bind(R.id.btn_recorder)
    public Button btnRecorder;

    @Bind(R.id.toolbar)
    public android.support.v7.widget.Toolbar toolBar;

    private ZegoAudioRoom zegoAudioRoom;
    private StreamAdapter streamAdapter;

    private InputStream backgroundMusicStream;

    private boolean hasLogin = false;
    private boolean enableAux = false;
    private boolean enableMute = false;
    private boolean enableRecorder = false;

    /** 是否已经推流 */
    private boolean hasPublish = false;
    private String publishStreamId = null;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (PrefUtils.getManualPublish()) {
            btnLoginOrLogout.setVisibility(View.VISIBLE);
            btnCommunicate.setEnabled(false);
        } else {
            btnLoginOrLogout.setVisibility(View.GONE);
            btnCommunicate.setEnabled(true);
        }

        setSupportActionBar(toolBar);
        toolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasLogin) {
                    Toast.makeText(MainActivity.this, R.string.zg_tip_not_allow_setting_when_login, Toast.LENGTH_LONG).show();
                } else {
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(settingsIntent, 101);
                }
            }
        });

        ZegoAudioRoom.enableAudioPrep(PrefUtils.getAudioPrepare());

        zegoAudioRoom = new ZegoAudioRoom();
        zegoAudioRoom.setManualPublish(PrefUtils.getManualPublish());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] { Manifest.permission.RECORD_AUDIO }, REQUEST_CODE_PERMISSION);
            } else {
                initZegoSDK(BuildConfig.APP_ID, requireSignData());
            }
        } else {
            initZegoSDK(BuildConfig.APP_ID, requireSignData());
        }

        streamAdapter = new StreamAdapter();
        ctrlStreamList.setEmptyView(emptyView);
        ctrlStreamList.setAdapter(streamAdapter);
    }

    @Override
    protected void onDestroy() {
        if (hasLogin) {
            logout();
        }

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
                initZegoSDK(BuildConfig.APP_ID, requireSignData());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_logs:
                startActivity(new Intent(MainActivity.this, LogsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {
            if (resultCode == 1) {
                ZGLog.d("on SettingsActivity Result, reInit SDK");
                zegoAudioRoom.unInit();

                ZegoAudioRoom.enableAudioPrep(PrefUtils.getAudioPrepare());
                zegoAudioRoom.setManualPublish(PrefUtils.getManualPublish());
                if (data == null) {
                    initZegoSDK(BuildConfig.APP_ID, requireSignData());
                } else {
                    initZegoSDK(data.getLongExtra("appId", 1), data.getByteArrayExtra("signKey"));
                }
            } else {
                ZGLog.d("on SettingsActivity Result: %d", resultCode);
                zegoAudioRoom.setManualPublish(PrefUtils.getManualPublish());
            }

            if (PrefUtils.getManualPublish()) {
                btnLoginOrLogout.setEnabled(true);
                btnLoginOrLogout.setVisibility(View.VISIBLE);
                btnCommunicate.setEnabled(false);
            } else {
                btnLoginOrLogout.setVisibility(View.GONE);
                btnCommunicate.setEnabled(true);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @OnClick({R.id.btn_login_logout, R.id.btn_communicate_action, R.id.btn_aux, R.id.btn_mute, R.id.btn_recorder})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login_logout:
                handleLoginOrLogout();
                break;

            case R.id.btn_communicate_action:
                handleCommunicate();
                break;

            case R.id.btn_aux:
                handleAuxState();
                break;

            case R.id.btn_mute:
                handleMuteState();
                break;

            case R.id.btn_recorder:
                handleRecorderState();
                break;
        }
    }


    private void handleLoginOrLogout() {
        if (hasLogin) {
            logout();
        } else {    // 在通话中，需要退出通话
            login();
        }
    }

    private void handleCommunicate() {
        if (PrefUtils.getManualPublish()) {
            if (hasPublish) {
                zegoAudioRoom.stopPublish();
                btnCommunicate.setText(R.string.zg_start_communicate);
                streamAdapter.removeItem(publishStreamId);
                publishStreamId = null;
                hasPublish = false;
            } else {
                btnCommunicate.setEnabled(false);
                zegoAudioRoom.startPublish();
            }
        } else {
            handleLoginOrLogout();
        }
    }

    private void handleAuxState() {
        enableAux = !enableAux;
        zegoAudioRoom.enableAux(enableAux);

        btnAux.setText(enableAux ? R.string.zg_btn_text_aux_off : R.string.zg_btn_text_aux);
    }

    private void handleMuteState() {
        enableMute = !enableMute;
        zegoAudioRoom.enableSpeaker(!enableMute);

        btnMute.setText(enableMute ? R.string.zg_btn_text_mute_off : R.string.zg_btn_text_mute);
    }

    private void handleRecorderState() {
        enableRecorder = !enableRecorder;
        int mask = enableRecorder ? ZegoAudioRoom.AudioRecordMask.Mix : ZegoAudioRoom.AudioRecordMask.NoRecord;
        zegoAudioRoom.enableSelectedAudioRecord(mask, 44100);

        btnRecorder.setText(enableRecorder ? R.string.zg_btn_text_record_off : R.string.zg_btn_text_record_on);
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

    private void initZegoSDK(long appKey, byte[] signKey) {
        zegoAudioRoom.initWithAppId(appKey, signKey, this);

        zegoAudioRoom.setAudioRoomDelegate(new ZegoAudioRoomDelegate() {
            @Override
            public void onKickOut(int i, String s) {
                ZGLog.d("onKickOut: %d-%s", i, s);
            }

            @Override
            public void onDisconnect(int i, String s) {
                ZGLog.d("onDisconnect: %d-%s", i, s);
            }

            @Override
            public void onStreamUpdate(ZegoAudioStreamType zegoAudioStreamType, ZegoAudioStream zegoAudioStream) {
                ZGLog.d("onStreamUpdate, type: %s, streamId: %s", zegoAudioStreamType, zegoAudioStream.getStreamId());
                switch (zegoAudioStreamType) {
                    case ZEGO_AUDIO_STREAM_ADD:
                        streamAdapter.insertItem(zegoAudioStream.getStreamId());
                        break;
                    case ZEGO_AUDIO_STREAM_DELETE:
                        streamAdapter.removeItem(zegoAudioStream.getStreamId());
                        break;
                    default: break;
                }
            }
        });
        zegoAudioRoom.setAudioPublisherDelegate(new ZegoAudioLivePublisherDelegate() {
            @Override
            public void onPublishStateUpdate(int stateCode, String streamId, HashMap<String, Object> info) {
                ZGLog.d("onPublishStateUpdate, stateCode: %d, streamId: %s, info: %s", stateCode, streamId, info);

                btnCommunicate.setEnabled(true);

                if (stateCode == 0) {
                    hasPublish = true;
                    publishStreamId = streamId;
                    streamAdapter.insertItem(streamId);

                    btnCommunicate.setText(R.string.zg_stop_communicate);
                } else {
                    btnCommunicate.setText(R.string.zg_start_communicate);
                }
            }

            @Override
            public ZegoAuxData onAuxCallback(int dataLen) {
                ZGLog.d("onAuxCallback, dataLen: %d", dataLen);
                if (dataLen <= 0) return null;

                ZegoAuxData auxData = new ZegoAuxData();
                auxData.dataBuf = new byte[dataLen];

                try {
                    if (backgroundMusicStream == null) {
                        AssetManager am = getAssets();
                        backgroundMusicStream = am.open("a.pcm");
                    }

                    int readLength = backgroundMusicStream.read(auxData.dataBuf);
                    if (readLength <= 0) {
                        backgroundMusicStream.close();
                        backgroundMusicStream = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                auxData.sampleRate = 44100;
                auxData.channelCount = 2;
                return auxData;
            }
        });
        zegoAudioRoom.setAudioPlayerDelegate(new ZegoAudioLivePlayerDelegate() {
            @Override
            public void onPlayStateUpdate(int stateCode, ZegoAudioStream zegoAudioStream) {
                ZGLog.d("onPlayStateUpdate, stateCode: %d, streamId: %s", stateCode, zegoAudioStream.getStreamId());
            }
        });
        zegoAudioRoom.setAudioLiveEventDelegate(new ZegoAudioLiveEventDelegate() {
            @Override
            public void onAudioLiveEvent(ZegoAudioLiveEvent zegoAudioLiveEvent, HashMap<String, String> info) {
                ZGLog.d("onAudioLiveEvent, event: %s, info: %s", zegoAudioLiveEvent, info);
            }
        });
        zegoAudioRoom.setAudioRecordDelegate(new ZegoAudioLiveRecordDelegate() {
            @Override
            public void onAudioRecord(byte[] audioData, int sampleRate, int numberOfChannels, int bitDepth, int type) {
                ZGLog.d("onAudioRecord, sampleRate: %d, numberOfChannels: %d, bitDepth: %d, type: %d", sampleRate, numberOfChannels, bitDepth, type);
            }
        });
        zegoAudioRoom.setAudioDeviceEventDelegate(new ZegoAudioDeviceEventDelegate() {
            @Override
            public void onAudioDevice(String deviceName, int errorCode) {
                ZGLog.d("onAudioDevice, deviceName: %s, errorCode: %d", deviceName, errorCode);
            }
        });
        zegoAudioRoom.setAudioPrepareDelegate(new ZegoAudioPrepareDelegate() {
            @Override
            public void onAudioPrepared(ByteBuffer inData, int sampleCount, int bitDepth, int sampleRate, ByteBuffer outData) {
                ZGLog.d("onAudioPrepared, inData is null? %s, sampleCount: %d, bitDepth: %d, sampleRate: %d",
                        inData == null, sampleCount, bitDepth, sampleRate);
                if (inData != null && outData != null) {
                    inData.position(0);
                    outData.position(0);
                    // outData的长度固定为sampleCount * bitDepth
                    // 不可更改
                    outData.limit(sampleCount * bitDepth);
                    // 将处理后的数据返回sdk
                    outData.put(inData);
                }
            }
        });
        zegoAudioRoom.setAudioAVEngineDelegate(new ZegoAudioAVEngineDelegate() {
            @Override
            public void onAVEngineStop() {
                ZGLog.d("onAVEngineStop");
            }
        });
    }

    private void login() {
        btnLoginOrLogout.setEnabled(false);
        ctrlRoomName.setEnabled(false);

        String roomId = ctrlRoomName.getText().toString().trim();
        if (TextUtils.isEmpty(roomId)) {
            roomId = "zego_audio_live_demo";
        }

        boolean success = zegoAudioRoom.loginRoom(roomId, new ZegoLoginAudioRoomCallback() {
            @Override
            public void onLoginCompletion(int state) {
                ZGLog.d("onLoginCompletion: 0x%1$x", state);

                if (state == 0) {
                    hasLogin = true;

                    btnAux.setEnabled(true);
                    btnMute.setEnabled(true);
                    btnRecorder.setEnabled(true);

                    if (PrefUtils.getManualPublish()) {
                        btnLoginOrLogout.setEnabled(true);
                        btnLoginOrLogout.setText(R.string.zg_logout_room);

                        btnCommunicate.setEnabled(true);
                    }
                } else {
                    ctrlRoomName.setEnabled(true);

                    btnLoginOrLogout.setText(R.string.zg_login_room);
                    btnLoginOrLogout.setEnabled(true);

                    Toast.makeText(MainActivity.this, String.format("Login Error: 0x%1$x", state), Toast.LENGTH_LONG).show();
                }
            }
        });
        ZGLog.d("login: %s", success);
    }

    private void logout() {
        boolean success = zegoAudioRoom.logoutRoom();
        streamAdapter.clear();
        hasLogin = false;
        hasPublish = false;

        btnAux.setEnabled(false);
        btnMute.setEnabled(false);
        btnRecorder.setEnabled(false);

        ctrlRoomName.setEnabled(true);

        btnLoginOrLogout.setText(R.string.zg_login_room);
        btnCommunicate.setText(R.string.zg_start_communicate);
        if (PrefUtils.getManualPublish()) {
            btnCommunicate.setEnabled(false);
        }

        ZGLog.d("logout: %s", success);
    }

    private class StreamAdapter extends BaseAdapter {
        private List<String> streamSet;
        private LayoutInflater inflater;

        public StreamAdapter() {
            inflater = getLayoutInflater();
            streamSet = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return streamSet.size();
        }

        @Override
        public Object getItem(int position) {
            return streamSet.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.widget_list_item, null, false);
            }

            String content = String.format("Stream %d: %s", position, getItem(position));
            ((TextView)convertView).setText(content);
            return convertView;
        }

        public void insertItem(String item) {
            if (streamSet.contains(item)) return;

            streamSet.add(item);
            notifyDataSetChanged();
        }

        public void removeItem(String item) {
            if (streamSet.contains(item)) {
                streamSet.remove(item);
                notifyDataSetChanged();
            }
        }

        public void clear() {
            streamSet.clear();
            notifyDataSetChanged();
        }
    }

    static private class ZGLog {
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

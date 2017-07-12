package com.zego.audioroomdemo.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.audioroomdemo.AudioApplication;
import com.zego.audioroomdemo.MainActivity;
import com.zego.audioroomdemo.utils.PrefUtils;
import com.zego.audioroomdemo.R;
import com.zego.zegoaudioroom.ZegoAudioAVEngineDelegate;
import com.zego.zegoaudioroom.ZegoAudioDeviceEventDelegate;
import com.zego.zegoaudioroom.ZegoAudioLiveEvent;
import com.zego.zegoaudioroom.ZegoAudioLiveEventDelegate;
import com.zego.zegoaudioroom.ZegoAudioLivePlayerDelegate;
import com.zego.zegoaudioroom.ZegoAudioLivePublisherDelegate;
import com.zego.zegoaudioroom.ZegoAudioLiveRecordDelegate;
import com.zego.zegoaudioroom.ZegoAudioPrepDelegate2;
import com.zego.zegoaudioroom.ZegoAudioPrepareDelegate;
import com.zego.zegoaudioroom.ZegoAudioRoom;
import com.zego.zegoaudioroom.ZegoAudioRoomDelegate;
import com.zego.zegoaudioroom.ZegoAudioStream;
import com.zego.zegoaudioroom.ZegoAudioStreamType;
import com.zego.zegoaudioroom.ZegoAuxData;
import com.zego.zegoaudioroom.ZegoLoginAudioRoomCallback;
import com.zego.zegoliveroom.entity.ZegoAudioFrame;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SessionActivity extends AppCompatActivity {

    @Bind(R.id.btn_communicate_action)
    public Button btnCommunicate;

    @Bind(R.id.btn_aux)
    public Button btnAux;

    @Bind(R.id.btn_mute)
    public Button btnMute;

    @Bind(R.id.btn_recorder)
    public Button btnRecorder;

    @Bind(R.id.tv_event_tips)
    public TextView tvEventTips;

    @Bind(R.id.stream_list)
    public ListView ctrlStreamList;

    @Bind(R.id.empty_data_tip)
    public TextView emptyView;

    private ZegoAudioRoom zegoAudioRoom;

    private StreamAdapter streamAdapter;

    private InputStream backgroundMusicStream;

    /** 是否已经推流 */
    private boolean hasPublish = false;
    private String publishStreamId = null;

    private boolean hasLogin = false;
    private boolean enableAux = false;
    private boolean enableMute = false;
    private boolean enableRecorder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        ButterKnife.bind(this);

        zegoAudioRoom = ((AudioApplication)getApplication()).getAudioRoomClient();

        if (PrefUtils.isManualPublish()) {
            btnCommunicate.setVisibility(View.VISIBLE);
            btnCommunicate.setEnabled(false);
        } else {
            btnCommunicate.setVisibility(View.GONE);
        }

        streamAdapter = new StreamAdapter();
        ctrlStreamList.setEmptyView(emptyView);
        ctrlStreamList.setAdapter(streamAdapter);

        Intent startIntent = getIntent();
        String roomId = startIntent.getStringExtra("roomId");
        if (TextUtils.isEmpty(roomId)) {
            Toast.makeText(this, "参数非法", Toast.LENGTH_LONG).show();
            finish();
        } else {
            setupCallbacks();
            login(roomId);
        }
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if (hasLogin) {
            logout();
        }
        removeCallbacks();
        super.onBackPressed();
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
                startActivity(new Intent(SessionActivity.this, LogsActivity.class));
                return true;

            case R.id.action_close:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick({R.id.btn_communicate_action, R.id.btn_aux, R.id.btn_mute, R.id.btn_recorder})
    public void onViewClicked(View view) {
        switch (view.getId()) {
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

    private void setupCallbacks() {
        zegoAudioRoom.setAudioRoomDelegate(new ZegoAudioRoomDelegate() {
            @Override
            public void onKickOut(int i, String s) {
                MainActivity.ZGLog.d("onKickOut: %d-%s", i, s);
            }

            @Override
            public void onDisconnect(int i, String s) {
                MainActivity.ZGLog.d("onDisconnect: %d-%s", i, s);
            }

            @Override
            public void onStreamUpdate(ZegoAudioStreamType zegoAudioStreamType, ZegoAudioStream zegoAudioStream) {
                MainActivity.ZGLog.d("onStreamUpdate, type: %s, streamId: %s", zegoAudioStreamType, zegoAudioStream.getStreamId());
                String streamId = zegoAudioStream.getStreamId();
                switch (zegoAudioStreamType) {
                    case ZEGO_AUDIO_STREAM_ADD:
                        streamAdapter.insertItem(streamId);
                        tvEventTips.setText("新增流：" + streamId);
                        break;
                    case ZEGO_AUDIO_STREAM_DELETE:
                        streamAdapter.removeItem(streamId);
                        tvEventTips.setText("删除流：" + streamId);
                        break;
                    default: break;
                }
            }
        });
        zegoAudioRoom.setAudioPublisherDelegate(new ZegoAudioLivePublisherDelegate() {

            @Override
            public void onPublishStateUpdate(int stateCode, String streamId, HashMap<String, Object> info) {
                MainActivity.ZGLog.d("onPublishStateUpdate, stateCode: %d, streamId: %s, info: %s", stateCode, streamId, info);

                btnCommunicate.setEnabled(true);

                if (stateCode == 0) {
                    hasPublish = true;
                    publishStreamId = streamId;
                    streamAdapter.insertItem(streamId);

                    btnCommunicate.setText(R.string.zg_stop_communicate);
                    tvEventTips.setText("推流成功");
                } else {
                    btnCommunicate.setText(R.string.zg_start_communicate);
                    tvEventTips.setText("推流失败：" + stateCode);
                }
            }

            @Override
            public ZegoAuxData onAuxCallback(int dataLen) {
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
                MainActivity.ZGLog.d("onPlayStateUpdate, stateCode: %d, streamId: %s", stateCode, zegoAudioStream.getStreamId());
                if (stateCode == 0) {
                    tvEventTips.setText("拉流成功：" + zegoAudioStream.getStreamId());
                } else {
                    tvEventTips.setText("拉流失败：" + stateCode);
                }
            }
        });
        zegoAudioRoom.setAudioLiveEventDelegate(new ZegoAudioLiveEventDelegate() {
            @Override
            public void onAudioLiveEvent(ZegoAudioLiveEvent zegoAudioLiveEvent, HashMap<String, String> info) {
                MainActivity.ZGLog.d("onAudioLiveEvent, event: %s, info: %s", zegoAudioLiveEvent, info);
            }
        });
        zegoAudioRoom.setAudioRecordDelegate(new ZegoAudioLiveRecordDelegate() {
            private long lastCallbackTime = 0;

            @Override
            public void onAudioRecord(byte[] audioData, int sampleRate, int numberOfChannels, int bitDepth, int type) {
                long nowTime = System.currentTimeMillis();
                if (nowTime - lastCallbackTime > 1000) {    // 过滤不停回调显示太多日志，只需要有一条日志表示有回调就可以了
                    MainActivity.ZGLog.d("onAudioRecord, sampleRate: %d, numberOfChannels: %d, bitDepth: %d, type: %d", sampleRate, numberOfChannels, bitDepth, type);
                }
                lastCallbackTime = nowTime;
            }
        });
        zegoAudioRoom.setAudioDeviceEventDelegate(new ZegoAudioDeviceEventDelegate() {
            @Override
            public void onAudioDevice(String deviceName, int errorCode) {
                MainActivity.ZGLog.d("onAudioDevice, deviceName: %s, errorCode: %d", deviceName, errorCode);
            }
        });
        zegoAudioRoom.setAudioPrepareDelegate(new ZegoAudioPrepareDelegate() {
            private long lastCallbackTime = 0;

            @Override
            public void onAudioPrepared(ByteBuffer inData, int sampleCount, int bitDepth, int sampleRate, ByteBuffer outData) {
                long nowTime = System.currentTimeMillis();
                if (nowTime - lastCallbackTime > 1000) {    // 过滤不停回调显示太多日志，只需要有一条日志表示有回调就可以了
                    MainActivity.ZGLog.d("onAudioPrepared, inData is null? %s, sampleCount: %d, bitDepth: %d, sampleRate: %d",
                            inData == null, sampleCount, bitDepth, sampleRate);
                }
                lastCallbackTime = nowTime;
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

        zegoAudioRoom.setAudioPrepDelegate2(new ZegoAudioPrepDelegate2() {
            @Override
            public ZegoAudioFrame onAudioPrep(ZegoAudioFrame zegoAudioFrame) {
                return zegoAudioFrame;
            }
        });
        zegoAudioRoom.setAudioAVEngineDelegate(new ZegoAudioAVEngineDelegate() {
            @Override
            public void onAVEngineStop() {
                MainActivity.ZGLog.d("onAVEngineStop");
            }
        });
    }

    private void removeCallbacks() {
        zegoAudioRoom.setAudioRoomDelegate(null);
        zegoAudioRoom.setAudioPublisherDelegate(null);
        zegoAudioRoom.setAudioPlayerDelegate(null);
        zegoAudioRoom.setAudioLiveEventDelegate(null);
        zegoAudioRoom.setAudioRecordDelegate(null);
        zegoAudioRoom.setAudioDeviceEventDelegate(null);
        zegoAudioRoom.setAudioPrepareDelegate(null);
        zegoAudioRoom.setAudioAVEngineDelegate(null);
    }

    private void handleCommunicate() {
        if (!PrefUtils.isManualPublish()) return;

        if (hasPublish) {
            zegoAudioRoom.stopPublish();
            btnCommunicate.setText(R.string.zg_start_communicate);
            streamAdapter.removeItem(publishStreamId);
            publishStreamId = null;
            hasPublish = false;
            tvEventTips.setText("停止推流");
        } else {
            btnCommunicate.setEnabled(false);
            zegoAudioRoom.startPublish();
        }
    }

    private void login(String roomId) {
        tvEventTips.setText("开始登录房间：" + roomId);
        boolean success = zegoAudioRoom.loginRoom(roomId, new ZegoLoginAudioRoomCallback() {
            @Override
            public void onLoginCompletion(int state) {
                MainActivity.ZGLog.d("onLoginCompletion: 0x%1$x", state);

                if (state == 0) {
                    hasLogin = true;

                    btnAux.setEnabled(true);
                    btnMute.setEnabled(true);
                    btnRecorder.setEnabled(true);

                    if (PrefUtils.isManualPublish()) {
                        btnCommunicate.setEnabled(true);
                    }

                    tvEventTips.setText("登录成功");
                } else {
                    Toast.makeText(SessionActivity.this, String.format("Login Error: 0x%1$x", state), Toast.LENGTH_LONG).show();
                    tvEventTips.setText("登录失败：" + state);
                }
            }
        });
        MainActivity.ZGLog.d("login: %s", success);
        if (!success) {
            tvEventTips.setText("登录失败");
        }
    }

    private void logout() {
        boolean success = zegoAudioRoom.logoutRoom();
        streamAdapter.clear();
        hasLogin = false;
        hasPublish = false;

        btnAux.setEnabled(false);
        btnMute.setEnabled(false);
        btnRecorder.setEnabled(false);

        btnCommunicate.setText(R.string.zg_start_communicate);
        if (PrefUtils.isManualPublish()) {
            btnCommunicate.setEnabled(false);
        }

        MainActivity.ZGLog.d("logout: %s", success);
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
}

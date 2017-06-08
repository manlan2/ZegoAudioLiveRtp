package com.zego.audioroomdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.zegoaudioroom.*;
import com.zego.zegoaudioroom.BuildConfig;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    @Bind(R.id.tv_version)
    public TextView tvVersion;

    @Bind(R.id.tv_version2)
    public TextView tvVersion2;

    @Bind(R.id.checkbox_audio_prepare)
    public CheckBox cbTurnOnAudioPrepare;

    @Bind(R.id.checkbox_manual_publish)
    public CheckBox cbManualPublish;

    @Bind(R.id.et_app_id)
    public EditText etAppId;

    @Bind(R.id.et_app_key)
    public EditText etAppKey;

    static final public String KAudio_Prepare = "audio_prepare";
    static final public String KManual_Publish = "manual_publish";

    private boolean oldAudioPrepareValue;
    private boolean oldManualPublishValue;

    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//            AudioApplication application = (AudioApplication)getApplication();
            switch (buttonView.getId()) {
                case R.id.checkbox_audio_prepare:
//                    application.setRuntimeValue(KAudio_Prepare, isChecked);
                    PrefUtils.setAudioPrepare(isChecked);
                    break;

                case R.id.checkbox_manual_publish:
//                    application.setRuntimeValue(KManual_Publish, isChecked);
                    PrefUtils.setManualPublish(isChecked);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ButterKnife.bind(this);

        tvVersion.setText(ZegoAudioRoom.version());
        tvVersion2.setText(ZegoAudioRoom.version2());

        etAppId.setText("" + com.zego.audioroomdemo.BuildConfig.APP_ID);

//        AudioApplication application = (AudioApplication)getApplication();
//        oldAudioPrepareValue = (boolean)application.getRuntimeValue(KAudio_Prepare, false);
        oldAudioPrepareValue = PrefUtils.getAudioPrepare();
        cbTurnOnAudioPrepare.setChecked(oldAudioPrepareValue);

//        oldManualPublishValue = (boolean)application.getRuntimeValue(KManual_Publish, false);
        oldManualPublishValue = PrefUtils.getManualPublish();
        cbManualPublish.setChecked(oldManualPublishValue);

        cbTurnOnAudioPrepare.setOnCheckedChangeListener(checkedChangeListener);
        cbManualPublish.setOnCheckedChangeListener(checkedChangeListener);
    }

    @Override
    public void onBackPressed() {
        String _appIdStr = etAppId.getEditableText().toString();
        String appKey = etAppKey.getEditableText().toString();
        long appId = 0;
        if (!TextUtils.isEmpty(_appIdStr)) {
            try {
                appId = Long.valueOf(_appIdStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "AppId 格式非法", Toast.LENGTH_LONG).show();
                return;
            }
        }

        Intent resultIntent = null;
        if (appId != com.zego.audioroomdemo.BuildConfig.APP_ID) {
            // appKey长度必须等于32位
            String[] keys = appKey.split(",");
            if (keys.length != 32) {
                Toast.makeText(this, "AppKey 必须为32位", Toast.LENGTH_LONG).show();
                return;
            }

            byte[] signKey = new byte[32];
            for (int i = 0; i < 32; i++) {
                int data = Integer.valueOf(keys[i].trim().replace("0x", ""), 16);
                signKey[i] = (byte) data;
            }
            resultIntent = new Intent();
            resultIntent.putExtra("appId", appId);
            resultIntent.putExtra("signKey", signKey);
        }
//        AudioApplication application = (AudioApplication)getApplication();
//        boolean newValue = (boolean)application.getRuntimeValue(KAudio_Prepare, false);
        boolean newValue = PrefUtils.getAudioPrepare();
        setResult(oldAudioPrepareValue == newValue ? 0 : 1, resultIntent);
        super.onBackPressed();
    }
}

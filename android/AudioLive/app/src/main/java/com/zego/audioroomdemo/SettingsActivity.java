package com.zego.audioroomdemo;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zego.zegoaudioroom.*;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    @Bind(R.id.tv_version)
    public TextView tvVersion;

    @Bind(R.id.tv_version2)
    public TextView tvVersion2;

    @Bind(R.id.tv_user_id)
    public TextView tvUserId;

    @Bind(R.id.checkbox_use_test_env)
    public CheckBox cbUseTestEnv;

    @Bind(R.id.checkbox_audio_prepare)
    public CheckBox cbTurnOnAudioPrepare;

    @Bind(R.id.checkbox_manual_publish)
    public CheckBox cbManualPublish;

    @Bind(R.id.et_app_id)
    public EditText etAppId;

    @Bind(R.id.et_app_key)
    public EditText etAppKey;

    private boolean oldUseTestEnvValue;
    private boolean oldAudioPrepareValue;
    private boolean oldManualPublishValue;

    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.checkbox_audio_prepare:
                    PrefUtils.enableAudioPrepare(isChecked);
                    break;

                case R.id.checkbox_manual_publish:
                    PrefUtils.setManualPublish(isChecked);
                    break;

                case R.id.checkbox_use_test_env:
                    AudioApplication.sApplication.setUseTestEnv(isChecked);
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

        long appId = -1;
        String signKey = null;
        Intent startIntent = getIntent();
        if (startIntent != null) {
            appId = startIntent.getLongExtra("appId", -1);
            signKey = startIntent.getStringExtra("rawKey");
        }

        if (appId > 0 && !TextUtils.isEmpty(signKey)) {
            etAppId.setText(String.valueOf(appId));
            etAppKey.setText(signKey);
        } else {
            etAppId.setText("" + com.zego.audioroomdemo.BuildConfig.APP_ID);
        }

        tvUserId.setText(PrefUtils.getUserId());

        oldUseTestEnvValue = AudioApplication.sApplication.isUseTestEnv();
        cbUseTestEnv.setChecked(oldUseTestEnvValue);

        oldAudioPrepareValue = PrefUtils.isEnableAudioPrepare();
        cbTurnOnAudioPrepare.setChecked(oldAudioPrepareValue);

        oldManualPublishValue = PrefUtils.isManualPublish();
        cbManualPublish.setChecked(oldManualPublishValue);

        cbUseTestEnv.setOnCheckedChangeListener(checkedChangeListener);
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

        boolean reInitSDK = false;
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
            resultIntent.putExtra("rawKey", appKey);
            reInitSDK = true;
        }

        reInitSDK = reInitSDK | (PrefUtils.isEnableAudioPrepare() != oldAudioPrepareValue);
        reInitSDK = reInitSDK | (AudioApplication.sApplication.isUseTestEnv() != oldUseTestEnvValue);
        setResult(reInitSDK ? 1 : 0, resultIntent);
        super.onBackPressed();
    }
}

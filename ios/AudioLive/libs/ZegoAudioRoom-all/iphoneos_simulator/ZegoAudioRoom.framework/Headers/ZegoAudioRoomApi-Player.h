//
//  ZegoAudioRoomApi-Player.h
//  ZegoAudioRoom
//
//  Created by Strong on 2017/3/15.
//  Copyright © 2017年 Zego. All rights reserved.
//

#import "ZegoAudioRoomApi.h"
#import "ZegoAudioRoomApiDefines.h"

@protocol ZegoAudioLivePlayerDelegate;
@protocol ZegoAudioLiveRecordDelegate;

@interface ZegoAudioRoomApi (Player)

- (void)setAudioPlayerDelegate:(id<ZegoAudioLivePlayerDelegate>)playerDelegate;

/// \brief （声音输出）静音开关
/// \param bEnable true打开，false关闭
/// \return true：成功；false:失败
- (bool)enableSpeaker:(bool) bEnable;

/// \brief 手机内置扬声器开关
/// \param bOn true打开，false关闭
/// \return true：成功；false:失败
- (bool)setBuiltInSpeakerOn:(bool)bOn;

/// \biref 设置播放音量
/// \param volume 音量大小 0 ~ 100
- (void)setPlayVolume:(int)volume;

/// \brief 获取当前播放视频的音量
/// \param[in] streamID 播放流名
/// \return 对应视频的音量
- (float)getSoundLevelOfStream:(NSString *)streamID;

/// \brief 音频录制回调开关
/// \param enable true 启用音频录制回调；false 关闭音频录制回调
/// \return true 成功，false 失败
- (bool)enableAudioRecord:(BOOL)enable;

/// \brief 音频录制回调开关
/// \param mask 启用音频源选择，参考 ZegoAPIAudioRecordMask
/// \param sampleRate 采样率 8000, 16000, 22050, 24000, 32000, 44100, 48000
/// \return true 成功，false 失败
- (bool)enableSelectedAudioRecord:(unsigned int)mask sampleRate:(int)sampleRate;

/// \brief 音频录制回调
/// \param audioRecordDelegate 音频录制回调协议
- (void)setAudioRecordDelegate:(id<ZegoAudioLiveRecordDelegate>)audioRecordDelegate;

/// \brief 获取 SDK 支持的最大同时播放流数
/// \return 最大支持播放流数
+ (int)getMaxPlayChannelCount;

@end

@protocol ZegoAudioLivePlayerDelegate <NSObject>

/// \brief 播放流事件
/// \param[in] stateCode 播放状态码
/// \param[in] stream 流信息
- (void)onPlayStateUpdate:(int)stateCode stream:(ZegoAudioStream *)stream;

@end

@protocol ZegoAudioLiveRecordDelegate <NSObject>

@optional
/// \brief 采集的音频数据回调
/// \param audioData 音频数据
/// \param sampleRate 音频数据采样率
/// \param numOfChannels 音频数据声道数
/// \param bitDepth  bit depth
/// \param type 音源类型 参考 ZegoAPIAudioRecordMask
- (void)onAudioRecord:(NSData *)audioData sampleRate:(int)sampleRate numOfChannels:(int)numOfChannels bitDepth:(int)bitDepth type:(unsigned int)type;

- (void)onAudioRecord:(NSData *)audioData sampleRate:(int)sampleRate numOfChannels:(int)numOfChannels bitDepth:(int)bitDepth;

@end

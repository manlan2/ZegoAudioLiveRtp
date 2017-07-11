//
//  ZegoAudioRoomApi-Publisher.h
//  ZegoAudioRoom
//
//  Created by Strong on 2017/3/15.
//  Copyright © 2017年 Zego. All rights reserved.
//

#import "ZegoAudioRoomApi.h"
#import "ZegoAudioRoomApiDefines.h"

@protocol ZegoAudioLivePublisherDelegate;

@interface ZegoAudioRoomApi (Publisher)

- (void)setAudioPublisherDelegate:(id<ZegoAudioLivePublisherDelegate>)publisherDelegate;

/// \brief 开始直播
/// \note 此函数只有在[setManualPublish:true] 时调用才有效
/// \return true:调用成功; false:调用失败，当前直播流已达上限,已经开播等原因
- (bool)startPublish;

/// \brief 停止直播
/// \note 此函数只有在[setManualPublish:true] 时调用才有效
- (void)stopPublish;

/// \brief 开启关闭麦克风
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableMic:(bool)bEnable;

/// \brief 开启采集监听
/// \param bEnable true打开，false关闭
/// \return true:调用成功；false:调用失败
- (bool)enableLoopback:(bool)bEnable;

/// \biref 设置采集监听音量
/// \param volume 音量大小 0 ~ 100
- (void)setLoopbackVolume:(int)volume;

/// \brief 混音开关
/// \param enable true 启用混音输入；false 关闭混音输入
/// \return true 成功，否则失败
- (bool)enableAux:(BOOL)enable;

/// \brief 混音输入播放静音开关
/// \param bMute true: aux 输入播放静音；false: 不静音
/// \return true 成功，否则失败
- (bool)muteAux:(bool)bMute;

/// \brief 获取当前采集的音量
/// \return 当前采集音量大小
- (float)getCaptureSoundLevel;

/// 是否开启离散音频包发送
/// \param enable true 开启，此时关闭麦克风后，不会发送静音包；false 关闭，此时关闭麦克风后会发送静音包
/// \note 默认状态下，关闭麦克风后会发送静音包
/// \note 在推流前调用，只有纯 UDP 方案才可以调用此接口
- (void)enableDTX:(bool)enable;

/**
 设置音频码率
 
 @param bitrate 码率
 @return true 成功 false 失败
 */
- (bool)setAudioBitrate:(int)bitrate;

/**
 设置延迟模式
 
 @param mode 延迟模式，默认 ZEGOAPI_LATENCY_MODE_NORMAL
 @note 在推流前调用
 */
- (void)setLatencyMode:(ZegoAPILatencyMode)mode;

/**
 音频采集自动增益开关
 
 @param enable 是否开启
 @return true 成功，false 失败
 @note 默认关闭
 */
- (bool)enableAGC:(bool)enable;

/**
 回声消除开关
 
 @param bEnable true 打开 false 关闭
 @return true 成功 false 失败
 */
- (bool)enableAEC:(bool)enable;

@end

@protocol ZegoAudioLivePublisherDelegate <NSObject>

/// \brief 推流状态更新
/// \param[in] stateCode 状态码
/// \param[in] streamID 流ID
/// \param[in] info 推流信息
- (void)onPublishStateUpdate:(int)stateCode streamID:(NSString *)streamID streamInfo:(NSDictionary *)info;

@optional

/// \brief 混音数据输入回调
/// \param pData 数据缓存起始地址
/// \param pDataLen [in] 缓冲区长度；[out]实际填充长度，必须为 0 或是缓冲区长度，代表有无混音数据
/// \param pSampleRate 混音数据采样率
/// \param pChannelCount 混音数据声道数
/// \note 混音数据 bit depth 必须为 16
- (void)onAuxCallback:(void *)pData dataLen:(int *)pDataLen sampleRate:(int *)pSampleRate channelCount:(int *)pChannelCount;

@end

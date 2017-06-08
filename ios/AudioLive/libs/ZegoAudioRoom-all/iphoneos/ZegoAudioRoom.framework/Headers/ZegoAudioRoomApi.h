//
//  ZegoAudioRoomApi.h
//  ZegoAudioRoom
//
//  Created by Strong on 2017/3/15.
//  Copyright © 2017年 Zego. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ZegoAudioRoomApiDefines.h"

@protocol ZegoAudioRoomDelegate;
@protocol ZegoAudioLiveEventDelegate;
@protocol ZegoAudioDeviceEventDelegate;
@protocol ZegoAVEngineDelegate;

typedef void(^ZegoAudioRoomBlock)(int errorCode);

@interface ZegoAudioRoomApi : NSObject

+ (NSString *)version;
+ (NSString *)version2;

/// \brief 是否启用测试环境
/// \param useTestEnv 是否使用测试环境
+ (void)setUseTestEnv:(bool)useTestEnv;

/// \brief 调试信息开关
/// \desc 建议在调试阶段打开此开关，方便调试。默认关闭
/// \param bOnVerbose 是否使用测试环境
+ (void)setVerbose:(bool)bOnVerbose;

/// \brief 设置业务类型
/// \desc 默认为2 (实时语音类型), 可以取值0 (直播类型)
/// \desc 确保在创建接口对象前调用
/// \type 业务类型
+ (void)setBusinessType:(int)type;

/// \brief 触发日志上报
+ (void)uploadLog;

/// \brief 设置用户ID及用户名
/// \param userID 用户ID
/// \param userName 用户名
+ (bool)setUserID:(NSString *)userID userName:(NSString *)userName;

/// \brief 初始化SDK
/// \param[in] appID Zego派发的数字ID, 各个开发者的唯一标识
/// \param[in] appSignature Zego派发的签名, 用来校验对应appID的合法性
/// \return api 实例，nil 表示初始化失败
- (instancetype)initWithAppID:(unsigned int)appID appSignature:(NSData*)appSignature;

- (void)setAudioRoomDelegate:(id<ZegoAudioRoomDelegate>)roomDelegate;

/// \breif 设置登录房间成功后是否需要手动发布直播
/// \note 不设置则为自动发布直播, TRUE 手动发布直播， FALSE 自动发布直播
- (void)setManualPublish:(bool)manual;

/// \brief 登陆房间
/// \param[in] roomID 房间唯一ID
- (bool)loginRoom:(NSString *)roomID completionBlock:(ZegoAudioRoomBlock)block;

/// \brief 退出房间
/// \note 会停止所有的推拉流
/// \return true 成功，false 失败
- (bool)logoutRoom;

/// \brief 直播事件通知回调
/// \param liveEventDelegate 直播事件通知回调协议
- (void)setAudioLiveEventDelegate:(id<ZegoAudioLiveEventDelegate>)liveEventDelegate;

/// \brief 音频设备错误通知回调
/// \param deviceEventDelegate 音频设备错误通知回调协议
- (void)setAudioDeviceEventDelegate:(id<ZegoAudioDeviceEventDelegate>)deviceEventDelegate;

/// \brief 暂停音频模块
- (void)pauseAudioModule;

/// \brief 恢复音频模块
- (void)resumeAudioModule;

/// \brief Engine停止回调
- (void)setAVEngineDelegate:(id<ZegoAVEngineDelegate>)engineDelegate;

@end

@protocol ZegoAudioRoomDelegate <NSObject>

@optional

/// \brief 因为登陆抢占原因等被挤出房间
/// \param[in] reason 原因
/// \param[in] roomID 房间 ID
- (void)onKickOut:(int)reason roomID:(NSString *)roomID;

/// \brief 与 server 断开
/// \param[in] errorCode 错误码，0 位无错误
/// \param[in] roomID 房间 ID
- (void)onDisconnect:(int)errorCode roomID:(NSString *)roomID;

/// \brief 流更新消息，此时sdk会开始拉流/停止拉流
/// \param[in] type 增加/删除流
/// \param[in] stream 流信息
- (void)onStreamUpdated:(ZegoAudioStreamType)type stream:(ZegoAudioStream*)stream;

@end


@protocol ZegoAudioLiveEventDelegate <NSObject>

- (void)zego_onAudioLiveEvent:(ZegoAudioLiveEvent)event info:(NSDictionary<NSString*, NSString*>*)info;

@end

@protocol ZegoAudioDeviceEventDelegate <NSObject>

- (void)zego_onAudioDevice:(NSString *)deviceName error:(int)errorCode;

@end

@protocol ZegoAVEngineDelegate <NSObject>

/**
 音视频引擎停止时回调
 */
- (void)onAVEngineStop;

@end

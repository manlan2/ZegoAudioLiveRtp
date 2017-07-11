//
//  ZegoAudioRoomApiDefines.h
//  ZegoAudioRoom
//
//  Created by Strong on 2017/3/15.
//  Copyright © 2017年 Zego. All rights reserved.
//

#ifndef ZegoAudioRoomApiDefines_h
#define ZegoAudioRoomApiDefines_h

#ifndef ZEGO_EXTERN
#ifdef __cplusplus
#define ZEGO_EXTERN     extern "C"
#else
#define ZEGO_EXTERN     extern
#endif
#endif

typedef enum : NSUInteger {
    ZEGO_AUDIO_STREAM_ADD,
    ZEGO_AUDIO_STREAM_DELETE,
} ZegoAudioStreamType;

typedef enum : NSUInteger {
    Audio_Play_BeginRetry = 1,
    Audio_Play_RetrySuccess = 2,
    
    Audio_Publish_BeginRetry = 3,
    Audio_Publish_RetrySuccess = 4,
} ZegoAudioLiveEvent;

@interface ZegoAudioStream : NSObject

@property (nonatomic, copy) NSString *userID;
@property (nonatomic, copy) NSString *userName;
@property (nonatomic, copy) NSString *streamID;

@end

ZEGO_EXTERN NSString *const kZegoAudioRoomConfigKeepAudioSesionActive;  /**< AudioSession相关配置信息的key, 值为 NSString */

#ifndef ZegoLiveRoomApiDefines_h

enum ZegoAPIAudioRecordMask
{
    ZEGOAPI_AUDIO_RECORD_NONE      = 0x0,  ///< 关闭音频录制
    ZEGOAPI_AUDIO_RECORD_CAP       = 0x01, ///< 打开采集录制
    ZEGOAPI_AUDIO_RECORD_RENDER    = 0x02, ///< 打开渲染录制
    ZEGOAPI_AUDIO_RECORD_MIX       = 0x04  ///< 打开采集和渲染混音结果录制
};

#endif

#ifndef ZegoLiveRoomApiDefines_Publisher_h

typedef enum : NSUInteger {
    ZEGOAPI_LATENCY_MODE_NORMAL = 0,    ///< 普通延迟模式
    ZEGOAPI_LATENCY_MODE_LOW,           ///< 低延迟模式，*无法用于 RTMP 流*
    ZEGOAPI_LATENCY_MODE_NORMAL2,      ///< 普通延迟模式，最高码率可达192K
} ZegoAPILatencyMode;

#endif

#ifndef ZegoLiveRoomApiDefines_IM_h

typedef enum
{
    ZEGO_UPDATE_TOTAL = 1,             ///< 全量更新
    ZEGO_UPDATE_INCREASE,              ///< 增量更新
}ZegoUserUpdateType;

typedef enum
{
    ZEGO_USER_ADD  = 1,               ///< 新增
    ZEGO_USER_DELETE,                 ///< 删除
}ZegoUserUpdateFlag;

@interface ZegoUserState : NSObject

@property (nonatomic, copy) NSString *userID;               /**< 用户 ID */
@property (nonatomic, copy) NSString *userName;             /**< 用户名 */
@property (nonatomic, assign) ZegoUserUpdateFlag updateFlag; /**< 用户更新属性 */
@property (nonatomic, assign) int role;                     /**< 角色 */

@end

#endif

#endif /* ZegoAudioRoomApiDefines_h */

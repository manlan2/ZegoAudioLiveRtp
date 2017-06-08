//
//  ZegoAudioRoomApiDefines.h
//  ZegoAudioRoom
//
//  Created by Strong on 2017/3/15.
//  Copyright © 2017年 Zego. All rights reserved.
//

#ifndef ZegoAudioRoomApiDefines_h
#define ZegoAudioRoomApiDefines_h

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

#ifndef ZegoLiveRoomApiDefines_h

enum ZegoAPIAudioRecordMask
{
    ZEGOAPI_AUDIO_RECORD_NONE      = 0x0,  ///< 关闭音频录制
    ZEGOAPI_AUDIO_RECORD_CAP       = 0x01, ///< 打开采集录制
    ZEGOAPI_AUDIO_RECORD_RENDER    = 0x02, ///< 打开渲染录制
    ZEGOAPI_AUDIO_RECORD_MIX       = 0x04  ///< 打开采集和渲染混音结果录制
};

#endif

#endif /* ZegoAudioRoomApiDefines_h */

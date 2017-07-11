//
//  ZegoAudioRoomApi-AudioIO.h
//  ZegoAudioRoom
//
//  Created by Strong on 2017/3/16.
//  Copyright © 2017年 Zego. All rights reserved.
//

#import "ZegoAudioRoomApi.h"
#import "ZegoAudioCapture.h"

@interface ZegoAudioRoomApi (AudioIO)

+ (void)enableExternalAudioDevice:(bool)enable;
- (AVE::IAudioDataInOutput* )getIAudioDataInOutput;

/**
 设置音频前处理函数
 
 @param prep 前处理函数指针
 @attention 必须在 InitSDK 前调用，不能置空
 @note 调用者调用此 API 设置音频前处理函数。SDK 会在音频编码前调用，inData 为输入的音频原始数据，outData 为函数处理后数据
 @note 单通道，位深为 16 bit
 */
+ (void)setAudioPrep:(void(*)(const short* inData, int inSamples, int sampleRate, short *outData))prep;


/**
 设置音频前处理函数
 
 @param prepSet 预处理的采样率等参数设置
 @param callback 采样数据回调
 @note 调用者调用此 API 设置音频前处理函数。SDK 会在音频编码前调用，inFrame 为采集的音频数据，outFrame 为处理后返回给SDK的数据
 */
+ (void)setAudioPrep2:(AVE::ExtPrepSet)prepSet dataCallback:(void(*)(const AVE::AudioFrame& inFrame, AVE::AudioFrame& outFrame))callback;

@end

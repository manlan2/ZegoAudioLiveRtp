//
//  ZegoAVKitManager.h
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#pragma once

#import <ZegoAudioRoom/ZegoAudioRoom.h>

@interface ZegoAudioLive : NSObject

+ (ZegoAudioRoomApi *)api;
+ (void)releaseApi;

+ (void)setCustomAppID:(uint32_t)appid sign:(NSString *)sign;
+ (uint32_t)appID;

+ (void)setUsingTestEnv:(bool)testEnv;
+ (bool)usingTestEnv;

+ (bool)usingAlphaEnv;

+ (bool)manualPublish;
+ (void)setUsingManualPublish:(bool)manual;

@end


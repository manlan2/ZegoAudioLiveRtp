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

@end

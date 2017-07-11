//
//  ZegoAVKitManager.m
//  LiveDemo
//
//  Copyright © 2015年 Zego. All rights reserved.
//

#include "ZegoAVKitManager.h"
#import "ZegoSettings.h"

static ZegoAudioRoomApi *g_ZegoApi = nil;

NSData *g_signKey = nil;
uint32_t g_appID = 0;

BOOL g_useTestEnv = NO;
BOOL g_useAlphaEnv = NO;

BOOL g_useManual = NO;

static Byte toByte(NSString* c);
static NSData* ConvertStringToSign(NSString* strSign);

@interface ZegoAudioLive () <ZegoAVEngineDelegate>

+ (NSData *)zegoAppSignFromServer;

@end

@implementation ZegoAudioLive

+ (ZegoAudioRoomApi *)api
{
    if (g_ZegoApi == nil)
    {
        [ZegoAudioRoomApi setUseTestEnv:g_useTestEnv];
        
#ifdef DEBUG
        [ZegoAudioRoomApi setVerbose:YES];
#endif
        
        [ZegoAudioRoomApi setUserID:[ZegoSettings sharedInstance].userID userName:[ZegoSettings sharedInstance].userName];
        
        NSData *appSign = [self zegoAppSignFromServer];
        g_ZegoApi = [[ZegoAudioRoomApi alloc] initWithAppID:[self appID] appSignature:appSign];
        
        [g_ZegoApi setAVEngineDelegate:self];
        
        [g_ZegoApi setManualPublish:g_useManual];
    }
    
    return g_ZegoApi;
}

+ (void)releaseApi
{
    g_ZegoApi = nil;
}

+ (void)setCustomAppID:(uint32_t)appid sign:(NSString *)sign
{
    g_appID = appid;
    NSData *d = ConvertStringToSign(sign);
    
    if (d.length == 32 && appid != 0)
    {
        g_appID = appid;
        g_signKey = [[NSData alloc] initWithData:d];
        
        g_ZegoApi = nil;
    }
}

+ (uint32_t)appID
{
    if (g_appID != 0)
    {
        return g_appID;
    }
    else
    {
        return 1739272706;  // * rtp
    }
}

+ (void)setUsingTestEnv:(bool)testEnv
{
    if (g_useTestEnv != testEnv)
    {
        [self releaseApi];
    }
    
    g_useTestEnv = testEnv;
    [ZegoAudioRoomApi setUseTestEnv:testEnv];
}

+ (bool)usingTestEnv
{
    return g_useTestEnv;
}

+ (bool)usingAlphaEnv
{
    return g_useAlphaEnv;
}

+ (void)setUsingManualPublish:(bool)manual
{
    if (g_useManual != manual)
    {
        g_useManual = manual;
        
        if (g_ZegoApi)
            [g_ZegoApi setManualPublish:manual];
    }
}

+ (bool)manualPublish
{
    return g_useManual;
}

#pragma mark - private

+ (NSData *)zegoAppSignFromServer
{
    //!! Demo 把signKey先写到代码中
    //!! 规范用法：这个signKey需要从server下发到App，避免在App中存储，防止盗用
    
    if ([self appID] == 1)
    {
        Byte signkey[] = {0x91, 0x93, 0xcc, 0x66, 0x2a, 0x1c, 0x0e, 0xc1, 0x35, 0xec, 0x71, 0xfb, 0x07, 0x19, 0x4b, 0x38, 0x41, 0xd4, 0xad, 0x83, 0x78, 0xf2, 0x59, 0x90, 0xe0, 0xa4, 0x0c, 0x7f, 0xf4, 0x28, 0x41, 0xf7};
        return [NSData dataWithBytes:signkey length:32];
    }
    else if ([self appID] == 1739272706)
    {
        Byte signkey[] = {0x1e,0xc3,0xf8,0x5c,0xb2,0xf2,0x13,0x70,0x26,0x4e,0xb3,0x71,0xc8,0xc6,0x5c,0xa3,0x7f,0xa3,0x3b,0x9d,0xef,0xef,0x2a,0x85,0xe0,0xc8,0x99,0xae,0x82,0xc0,0xf6,0xf8};
        return [NSData dataWithBytes:signkey length:32];
    }
    else if ([self appID] == 3322882036)
    {
        Byte signkey[] = {0x5d,0xe6,0x83,0xac,0xa4,0xe5,0xad,0x43,0xe5,0xea,0xe3,0x70,0x6b,0xe0,0x77,0xa4,0x18,0x79,0x38,0x31,0x2e,0xcc,0x17,0x19,0x32,0xd2,0xfe,0x22,0x5b,0x6b,0x2b,0x2f};
        return [NSData dataWithBytes:signkey length:32];
    }
    else
    {
        return g_signKey;
    }
}

#pragma mark ZegoAVEngineDelegate
+ (void)onAVEngineStop
{
    NSLog(@"onAVEngineStop");
}

@end

Byte toByte(NSString* c)
{
    NSString *str = @"0123456789abcdef";
    Byte b = [str rangeOfString:c].location;
    return b;
}

NSData* ConvertStringToSign(NSString* strSign)
{
    if(strSign == nil || strSign.length == 0)
        return nil;
    strSign = [strSign lowercaseString];
    strSign = [strSign stringByReplacingOccurrencesOfString:@" " withString:@""];
    strSign = [strSign stringByReplacingOccurrencesOfString:@"0x" withString:@""];
    NSArray* szStr = [strSign componentsSeparatedByString:@","];
    int nLen = (int)[szStr count];
    Byte szSign[32];
    for(int i = 0; i < nLen; i++)
    {
        NSString *strTmp = [szStr objectAtIndex:i];
        if(strTmp.length == 1)
            szSign[i] = toByte(strTmp);
        else
        {
            szSign[i] = toByte([strTmp substringWithRange:NSMakeRange(0, 1)]) << 4 | toByte([strTmp substringWithRange:NSMakeRange(1, 1)]);
        }
        NSLog(@"%x,", szSign[i]);
    }
    
    NSData *sign = [NSData dataWithBytes:szSign length:32];
    return sign;
}



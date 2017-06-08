//
//  ZegoVideoTalkViewController.m
//  InstantTalk
//
//  Created by Strong on 16/7/11.
//  Copyright © 2016年 ZEGO. All rights reserved.
//

#import "ZegoAudioLiveViewController.h"
#import "ZegoAVKitManager.h"
#import "ZegoLogTableViewController.h"
#import <AVFoundation/AVFoundation.h>

@interface ZegoAudioLiveViewController () <ZegoAudioLivePublisherDelegate, ZegoAudioLivePlayerDelegate, ZegoAudioRoomDelegate>
//日志记录
@property (nonatomic, strong) NSMutableArray *logArray;

@property (nonatomic, strong) NSMutableArray *streamList;

@property (nonatomic, weak) IBOutlet UITableView* tableView;

@property (nonatomic, weak) IBOutlet UIButton *mutedButton;
@property (nonatomic, weak) IBOutlet UILabel *tipsLabel;

@property (nonatomic, weak) IBOutlet UIButton *publishButton;

@property (nonatomic, strong) UIColor *defaultButtonColor;
@property (nonatomic, assign) BOOL enableSpeaker;
@property (nonatomic, assign) BOOL isPublished;

@end

@implementation ZegoAudioLiveViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Do any additional setup after loading the view.
    int maxCount = [ZegoAudioRoomApi getMaxPlayChannelCount];
    self.logArray = [NSMutableArray array];
    self.streamList = [NSMutableArray arrayWithCapacity:maxCount];
    
    BOOL audioAuthorization = [self checkAudioAuthorization];
    if (audioAuthorization == NO)
    {
        [self showAuthorizationAlert:NSLocalizedString(@"直播视频,访问麦克风", nil) title:NSLocalizedString(@"需要访问麦克风", nil)];
    }
    
    if ([ZegoAudioLive manualPublish])
        self.publishButton.hidden = NO;
    else
        self.publishButton.hidden = YES;
    
    self.publishButton.enabled = NO;
    
    [self setupLiveKit];
    
    self.mutedButton.enabled = NO;
    self.enableSpeaker = YES;
    self.defaultButtonColor = [self.mutedButton titleColorForState:UIControlStateNormal];
    
    self.tableView.tableFooterView = [[UIView alloc] init];
    
    [self addLogString:[NSString stringWithFormat:@"开始加入session: %@", self.sessionID]];
    
    self.tipsLabel.text = [NSString stringWithFormat:@"开始登录房间: %@", self.sessionID];
    
    // 监听电话事件
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(audioSessionWasInterrupted:) name:AVAudioSessionInterruptionNotification object:nil];
    
    //进入房间
    [[ZegoAudioLive api] loginRoom:self.sessionID completionBlock:^(int errorCode) {
        if (errorCode != 0)
        {
            [self addLogString:[NSString stringWithFormat:@"加入session失败: %d", errorCode]];
            self.tipsLabel.text = [NSString stringWithFormat:@"登录房间失败: %d", errorCode];
        }
        else
        {
            self.mutedButton.enabled = YES;
            self.publishButton.enabled = YES;
            [self addLogString:[NSString stringWithFormat:@"加入session成功"]];
            self.tipsLabel.text = [NSString stringWithFormat:@"登录房间成功"];
        }
    }];
}

- (void)setupLiveKit
{
    [[ZegoAudioLive api] setAudioRoomDelegate:self];
    [[ZegoAudioLive api] setAudioPlayerDelegate:self];
    [[ZegoAudioLive api] setAudioPublisherDelegate:self];
}

- (IBAction)onPublishButton:(id)sender
{
    if (self.isPublished)
    {
        //停止直播
        [[ZegoAudioLive api] stopPublish];
        [self.publishButton setTitle:@"开始直播" forState:UIControlStateNormal];
        self.isPublished = NO;
        
        //删除流
        for (ZegoAudioStream *audioStream in self.streamList)
        {
            if ([audioStream.userID isEqualToString:[ZegoSettings sharedInstance].userID])
            {
                [self.streamList removeObject:audioStream];
                break;
            }
        }
        
        [self.tableView reloadData];
    }
    else
    {
        BOOL result = [[ZegoAudioLive api] startPublish];
        if (result == NO)
        {
            self.tipsLabel.text = @"开播失败，直播流超过上限";
        }
        else
        {
            [self.publishButton setTitle:@"停止直播" forState:UIControlStateNormal];
            self.publishButton.enabled = NO;
        }
    }
}

- (void)openSetting
{
    NSURL *settingURL = [NSURL URLWithString:UIApplicationOpenSettingsURLString];
    if ([[UIApplication sharedApplication] canOpenURL:settingURL])
        [[UIApplication sharedApplication] openURL:settingURL];
}

- (void)showAuthorizationAlert:(NSString *)message title:(NSString *)title
{
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:title message:message preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"取消", nil) style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
    }];
    UIAlertAction *settingAction = [UIAlertAction actionWithTitle:NSLocalizedString(@"设置权限", nil) style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        [self openSetting];
    }];
    
    [alertController addAction:settingAction];
    [alertController addAction:cancelAction];
    
    alertController.preferredAction = settingAction;
    
    [self presentViewController:alertController animated:YES completion:nil];
    
}

#pragma mark audiosessionInterrupted notification
- (void)audioSessionWasInterrupted:(NSNotification *)notification
{
    if (AVAudioSessionInterruptionTypeBegan == [notification.userInfo[AVAudioSessionInterruptionTypeKey] intValue])
    {
        
    }
    else if (AVAudioSessionInterruptionTypeEnded == [notification.userInfo[AVAudioSessionInterruptionTypeKey] intValue])
    {
        
    }
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self setIdelTimerDisable:YES];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [self setIdelTimerDisable:NO];
}

- (BOOL)checkAudioAuthorization
{
    AVAuthorizationStatus audioAuthStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeAudio];
    if (audioAuthStatus == AVAuthorizationStatusDenied || audioAuthStatus == AVAuthorizationStatusRestricted)
        return NO;
    if (audioAuthStatus == AVAuthorizationStatusNotDetermined)
    {
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeAudio completionHandler:^(BOOL granted) {
        }];
    }
    
    return YES;
}

- (IBAction)closeView:(id)sender
{
    [[ZegoAudioLive api] logoutRoom];
    [self.streamList removeAllObjects];
    
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)onMutedButton:(id)sender
{
    if (self.enableSpeaker)
    {
        self.enableSpeaker = NO;
        [self.mutedButton setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    }
    else
    {
        self.enableSpeaker = YES;
        [self.mutedButton setTitleColor:self.defaultButtonColor forState:UIControlStateNormal];
    }
    
    [[ZegoAudioLive api] enableSpeaker:self.enableSpeaker];
}

- (void)setIdelTimerDisable:(BOOL)disable
{
    [[UIApplication sharedApplication] setIdleTimerDisabled:disable];
}

#pragma mark UITableViewDataSouce & delegate
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return self.streamList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(nonnull NSIndexPath *)indexPath
{
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"streamCell" forIndexPath:indexPath];
    
    if (indexPath.row >= self.streamList.count)
        return cell;
    
    ZegoAudioStream *audioStream = [self.streamList objectAtIndex:indexPath.row];
    if ([audioStream.userID isEqualToString:[ZegoSettings sharedInstance].userID])
        cell.textLabel.textColor = [UIColor redColor];
    else
        cell.textLabel.textColor = [UIColor blackColor];
    
    NSAttributedString *titleAttributeString = [[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"Stream %2lu: ", indexPath.row + 1] attributes:@{NSForegroundColorAttributeName: [UIColor blackColor]}];
    
    NSMutableAttributedString *contentAttributeString = [[NSMutableAttributedString alloc] initWithString:audioStream.streamID];
    if ([audioStream.userID isEqualToString:[ZegoSettings sharedInstance].userID])
    {
        [contentAttributeString setAttributes:@{NSForegroundColorAttributeName: [UIColor redColor]} range:NSMakeRange(0, contentAttributeString.length)];
    }
    else
    {
        [contentAttributeString setAttributes:@{NSForegroundColorAttributeName: [UIColor blackColor]} range:NSMakeRange(0, contentAttributeString.length)];
    }
    
    NSMutableAttributedString *totalString = [[NSMutableAttributedString alloc] initWithAttributedString:titleAttributeString];
    [totalString appendAttributedString:contentAttributeString];
    
    cell.textLabel.attributedText = totalString;
    return cell;
}

#pragma mark - ZegoAudioLivePublisherDelegate
- (void)onPublishStateUpdate:(int)stateCode streamID:(NSString *)streamID streamInfo:(NSDictionary *)info
{
    if (stateCode == 0)
    {
        [self addLogString:[NSString stringWithFormat:@"推流成功: %@", streamID]];
        self.tipsLabel.text = @"推流成功";
        
        if ([ZegoAudioLive manualPublish])
        {
            self.publishButton.enabled = YES;
            self.isPublished = YES;
        }
        
        ZegoAudioStream *audioStream = [ZegoAudioStream new];
        audioStream.streamID = streamID;
        audioStream.userID = [ZegoSettings sharedInstance].userID;
        audioStream.userName = [ZegoSettings sharedInstance].userName;
        
        [self.streamList addObject:audioStream];
        
        [self.tableView reloadData];
    }
    else
    {
        if ([ZegoAudioLive manualPublish])
        {
            self.publishButton.enabled = YES;
            self.isPublished = NO;
            [self.publishButton setTitle:@"开始直播" forState:UIControlStateNormal];
        }
        
        [self addLogString:[NSString stringWithFormat:@"推流失败: %@, error:%d", streamID, stateCode]];
        self.tipsLabel.text = [NSString stringWithFormat:@"推流失败:%d", stateCode];
    }
}

#pragma mark - ZegoAudioLivePlayerDelegate
- (void)onPlayStateUpdate:(int)stateCode stream:(ZegoAudioStream *)stream
{
    if (stateCode == 0)
    {
        [self addLogString:[NSString stringWithFormat:@"拉流成功: %@", stream.streamID]];
        self.tipsLabel.text = @"拉流成功";
    }
    else
    {
        [self addLogString:[NSString stringWithFormat:@"拉流失败: %@, error: %d", stream.streamID, stateCode]];
        self.tipsLabel.text = [NSString stringWithFormat:@"拉流失败: %d", stateCode];
    }
}

#pragma mark - ZegoAudioRoomDelegate
- (void)onDisconnect:(int)errorCode roomID:(NSString *)roomID
{
    [self addLogString:[NSString stringWithFormat:@"连接房间失败 %d", errorCode]];
}

- (void)onStreamUpdated:(ZegoAudioStreamType)type stream:(ZegoAudioStream *)stream
{
    if (type == ZEGO_AUDIO_STREAM_ADD)
    {
        BOOL alreadyHave = NO;
        for (ZegoAudioStream *playStream in self.streamList)
        {
            if ([playStream.streamID isEqualToString:stream.streamID])
            {
                alreadyHave = YES;
                break;
            }
        }
        
        [self addLogString:[NSString stringWithFormat:@"新增流:%@", stream.streamID]];
        self.tipsLabel.text = [NSString stringWithFormat: @"用户%@进入,开始拉流", stream.userID];
        if (alreadyHave == NO)
            [self.streamList addObject:stream];
    }
    else
    {
        for (ZegoAudioStream *playStream in self.streamList)
        {
            if ([playStream.streamID isEqualToString:stream.streamID])
            {
                [self addLogString:[NSString stringWithFormat:@"删除流:%@", stream.streamID]];
                self.tipsLabel.text = [NSString stringWithFormat: @"用户%@退出,停止拉流", stream.userID];;
                
                [self.streamList removeObject:playStream];
                break;
            }
        }
    }
    
    [self.tableView reloadData];
}

#pragma mark - Log
- (NSString *)getCurrentTime
{
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    formatter.dateFormat = @"HH-mm-ss:SSS";
    return [formatter stringFromDate:[NSDate date]];
}

- (void)addLogString:(NSString *)logString
{
    if (logString.length != 0)
    {
        NSString *totalString = [NSString stringWithFormat:@"%@: %@", [self getCurrentTime], logString];
        [self.logArray insertObject:totalString atIndex:0];
        
        [[NSNotificationCenter defaultCenter] postNotificationName:@"logUpdateNotification" object:self userInfo:nil];
    }
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.identifier isEqualToString:@"logSegueIdentifier"])
    {
        UINavigationController *navigationController = [segue destinationViewController];
        ZegoLogTableViewController *logViewController = (ZegoLogTableViewController *)[navigationController.viewControllers firstObject];
        logViewController.logArray = self.logArray;
    }
}

#pragma mark - request video
- (void)requestOtherVideo
{
    [self closeView:nil];
}

@end

//
//  ZegoVideoRootViewController.m
//  InstantTalk
//
//  Created by Strong on 2017/2/14.
//  Copyright © 2017年 ZEGO. All rights reserved.
//

#import "ZegoVideoRootViewController.h"
#import "ZegoAudioLiveViewController.h"

@interface ZegoVideoRootViewController ()
@property (weak, nonatomic) IBOutlet UIButton *videoTalkButton;
@property (weak, nonatomic) IBOutlet UITextField *sessionIdText;

@property (nonatomic, strong) UITapGestureRecognizer *tapGesture;

@end

@implementation ZegoVideoRootViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    self.videoTalkButton.enabled = NO;
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(textFieldTextDidChange) name:UITextFieldTextDidChangeNotification object:self.sessionIdText];
    
}

- (void)textFieldTextDidChange
{
    if(self.sessionIdText.text.length > 0 )
        self.videoTalkButton.enabled = YES;
    else
        self.videoTalkButton.enabled = NO;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)onTapTableView:(UIGestureRecognizer *)gesture
{
    [self.view endEditing:YES];
}

- (void)textFieldDidBeginEditing:(UITextField *)textField
{
    if (self.tapGesture == nil)
        self.tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapTableView:)];
    
    [self.view addGestureRecognizer:self.tapGesture];
}


- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text
{
    if (text.length == 0)
        self.videoTalkButton.enabled = NO;
    else
        self.videoTalkButton.enabled = YES;
    
    if ([text isEqualToString:@"\n"])
    {
        [textView resignFirstResponder];
        return NO;
    }
    
    return YES;
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    if ([segue.identifier isEqualToString:@"presentVideoTalk"])
    {
        ZegoAudioLiveViewController *viewController = (ZegoAudioLiveViewController *)segue.destinationViewController;
        viewController.sessionID = self.sessionIdText.text;
    }
}


@end

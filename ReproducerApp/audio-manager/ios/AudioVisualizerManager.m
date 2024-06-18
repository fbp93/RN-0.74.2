/*!
 * Copyright 2019 Evernote Corporation. All rights reserved.
 */

#import "AudioVisualizerManager.h"
#import "AudioManager-Swift.h"
#import <React/RCTUIManager.h>

@implementation AudioVisualizerManager

RCT_EXPORT_MODULE()

- (UIView *)view
{
    AudioManager *audioManager = [self.bridge moduleForName:@"AudioManager"];
    AudioVisualizerView *view = [[AudioVisualizerView alloc] init];
    view.audioManager = audioManager;
    return view;
}

RCT_EXPORT_METHOD(startUpdates:(nonnull NSNumber*) reactTag) {
}

RCT_EXPORT_METHOD(stopUpdates:(nonnull NSNumber*) reactTag) {
}
@end

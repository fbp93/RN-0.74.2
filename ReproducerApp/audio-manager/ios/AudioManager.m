#import "AudioManager.h"

@interface RCT_EXTERN_MODULE(AudioManager, RCTEventEmitter)
RCT_EXTERN_METHOD(play:(nonnull NSString *)resourceLocalPath)
RCT_EXTERN_METHOD(togglePlayPause)
RCT_EXTERN_METHOD(stop)
RCT_EXTERN_METHOD(seekToNormalizedPlaybackPoint:(double)playbackPoint)
RCT_EXTERN_METHOD(getPlaybackInfo:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(getPlaybackDuration:(RCTResponseSenderBlock)callback)
RCT_EXTERN_METHOD(releasePlayer)
RCT_EXTERN_METHOD(record:(nonnull NSString *)identifier)
RCT_EXTERN_METHOD(stopRecording)
RCT_EXTERN_METHOD(cancelRecording)
RCT_EXTERN_METHOD(releaseRecorder)
RCT_EXTERN_METHOD(setLocalizedTexts:(nonnull NSDictionary *)texts)
@end

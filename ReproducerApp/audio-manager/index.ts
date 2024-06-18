/*!
 * Copyright 2019 Evernote Corporation. All rights reserved.
 */

import { NativeModules } from 'react-native';

const { AudioManager } = NativeModules;

export default AudioManager;

export const AudioManagerPlayerEventDidStart = 'DidStart';
export const AudioManagerPlayerEventDidPause = 'DidPause';
export const AudioManagerPlayerEventDidResume = 'DidResume';
export const AudioManagerPlayerEventDidStop = 'DidStop';
export const AudioManagerPlayerEventDidFinish = 'DidFinish';
export const AudioManagerPlayerEventDidErrorOccur = 'DidErrorOccur';

export const AudioManagerRecorderEventDidStart = 'DidStartRecording';
export const AudioManagerRecorderEventDidStop = 'DidStopRecording';
export const AudioManagerRecorderEventDidErrorOccur = 'DidErrorOccurRecording';

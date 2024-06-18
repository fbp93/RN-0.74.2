/*!
 * Copyright 2019 Evernote Corporation. All rights reserved.
 */

import Foundation
import AVFoundation

enum AudioManagerPlayerEvent: String {
    case DidStart
    case DidRestart
    case DidPause
    case DidResume
    case DidStop
    case DidFinish
    case DidErrorOccur
}

enum AudioManagerRecorderEvent: String {
    case DidStartRecording
    case DidStopRecording
    case DidErrorOccurRecording
}

@objc(AudioManager) class AudioManager: RCTEventEmitter {
    var recorderInputNormalizedPower: Float {
        return 0
    }

    
    private lazy var audioPlayer: AudioPlayer = {
        let player = AudioPlayer()
        player.delegate = self
        return player
    }()
    
    private lazy var audioRecorder: AudioRecorder = {
        let recorder = AudioRecorder()
        recorder.delegate = self
        return recorder
    }()
    
    // MARK: - Life-cycle
    override init() {
        super.init()
    }
    
	deinit {
	}
    
    // MARK: - RCTBridgeModule
    
    @objc override static func requiresMainQueueSetup() -> Bool {
        return false
    }
    
    // MARK: - RCTEventEmitter
    
    override func supportedEvents() -> [String] {
        return [
        ]
    }
    
    // MARK: - Localization
    @objc(setLocalizedTexts:)
    func setLocalizedTexts(_ texts: NSDictionary) {
        // If any localized text needed, set it
    }
    
    // MARK: - Interruption
    
    @objc func handleInterruption(notification: Notification) {
	}
    
    // MARK: - Playback
    
    @objc(play:)
    func play(_ resourceLocalPath: String) -> Bool {
        return false
    }
    
    @objc(togglePlayPause)
    func togglePlayPause() {
    }
    
    @objc(stop)
    func stop() {
    }
    
    @objc(seekToNormalizedPlaybackPoint:)
    func seek(toNormalizedPlaybackPoint: Double) {
    }
    
    @objc(getPlaybackInfo:)
    func getPlaybackInfo(_ callback: RCTResponseSenderBlock) {
    }
    
    @objc(getPlaybackDuration:)
    func getPlaybackDuration(_ callback: RCTResponseSenderBlock) {
    }
    
    @objc(releasePlayer)
    func releasePlayer() {
    }
    
    // MARK: - Recording
    @objc(record:)
    func record(identifier: String) {
    }
    
    @objc(stopRecording)
    func stopRecording() {
    }
    
    @objc(cancelRecording)
    func cancelRecording() {
    }
    
    @objc(releaseRecorder)
    func releaseRecorder() {
    }
}

extension AudioManager: AudioPlayerDelegate {
    func audioPlayerDidStart() {
    }
    
    func audioPlayerDidRestart() {
    }
    
    func audioPlayerDidPause() {
    }
    
    func audioPlayerDidResume() {
    }
    
    func audioPlayerDidStop() {
    }
    
    func audioPlayerDidFinish() {
    }
    
    func audioPlayerDidErrorOccur() {
    }
}

extension AudioManager: AudioRecorderDelegate {
    func audioRecorderDidStart(info: [String: String]) {
    }
    
    func audioRecorderDidStop(info: [String: String]) {
    }
    
    func audioRecorderDidErrorOccur(info: [String: String]) {
    }
}

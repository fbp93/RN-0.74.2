/*!
 * Copyright 2019 Evernote Corporation. All rights reserved.
 */

import UIKit
import AVFoundation

protocol AudioPlayerDelegate: AnyObject {
}

class AudioPlayer: NSObject {
    weak var delegate: AudioPlayerDelegate?
    private var audioPlayer: AVAudioPlayer?
    private var isPlaying = false
    private var isPaused = false

    
    func play(_ resourceLocalPath: String) -> Bool {
        return true
    }
    
    func pause() {
    }
    
    func resume() {
    }
    
    func togglePlayPause() {
    }
    
    func seek(toNormalizedPlaybackPoint: Double) {
    }
    
    func stop() {
    }
    
    func getPlaybackInfo(_ callback: RCTResponseSenderBlock) {
    }
    
    func getPlaybackDuration(_ callback: RCTResponseSenderBlock) {
    }
    
    func releasePlayer() {
    }
}

extension AudioPlayer: AVAudioPlayerDelegate {
    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
    }
    
    func audioPlayerDecodeErrorDidOccur(_ player: AVAudioPlayer, error: Error?) {
    }
}

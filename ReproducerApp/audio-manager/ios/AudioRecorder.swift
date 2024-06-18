/*!
 * Copyright 2019 Evernote Corporation. All rights reserved.
 */

import UIKit
import AVFoundation

protocol AudioRecorderDelegate: AnyObject {
    func audioRecorderDidStart(info: [String: String])
    func audioRecorderDidStop(info: [String: String])
    func audioRecorderDidErrorOccur(info: [String: String])
}

class AudioRecorder: NSObject {
    weak var delegate: AudioRecorderDelegate?
    var recorderInputNormalizedPower: Float {
        return 0;
    }
    
    private var audioRecorder: AVAudioRecorder?
    private var isRecording = false
    private var recordingIdentifier: String?
    
    func record(identifier: String) {
    }
    
    func stopRecording() {
    }
    
    func cancelRecording() {
    }
    
    func releaseRecorder() {
    }
    
    private func checkCanRecord(completion: @escaping (Bool) -> Void) {
    }
    
    private func startRecording() {
    }
    
    private func temporaryFileURL() -> URL {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "MM.dd.yyyy_kkmma"
        let fileName = dateFormatter.string(from: Date())
        return temporaryDirectoryURL().appendingPathComponent(fileName).appendingPathExtension("m4a")
    }
    
    private func temporaryDirectoryURL() -> URL {
        return URL(fileURLWithPath: NSTemporaryDirectory(),
                   isDirectory: true)
    }
}

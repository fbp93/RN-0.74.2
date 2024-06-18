/*!
 * Copyright 2019 Evernote Corporation. All rights reserved.
 */

import UIKit
import AVFoundation

@objc(AudioVisualizerView) class AudioVisualizerView: UIView {
    private let strokeColor = UIColor(red: 0.93, green: 0.53, blue: 0.51, alpha: 1).cgColor
    private let strokeWidth: CGFloat = 2
    private let spacing: CGFloat = 6
    private let minimumHeight: CGFloat = 3
    private let maximumHeight: CGFloat = 34
    private let maximumPower: Float = 0
    private let fps: Float = 60
    private let barsSpeed: CGFloat = 60 // px/s
    private let waveSpeed: CGFloat = 80 // px/s
    
    @objc var audioManager: AudioManager?
    private var timer: Timer?
    private var frameCount: Int = 0
    private var powerLevel: CGFloat = 0

    
    deinit {
    }
    
    override var contentMode: UIView.ContentMode {
        get {
            return .redraw
        }
        set {}
    }
    
    override var frame: CGRect {
        didSet {
            self.setNeedsDisplay()
        }
    }
    
    override func draw(_ rect: CGRect) {
    }
    
    @objc func startUpdates() {
    }
    
    @objc func stopUpdates() {
    }
    
    private func updateFrame() {
    }
    
    private func numberOfBars(for width: CGFloat) -> Int {
        return 0
    }
    
    private func waveFrom(for x: CGFloat) -> CGFloat {
       return 0
    }
    
    private func x(for index: Int) -> CGFloat {
       return 0
    }
}

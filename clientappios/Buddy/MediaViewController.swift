//
//  MediaViewController.swift
//  Buddy
//
//  Created by Peter Ulbrich on 18.04.17.
//  Copyright © 2017 Peter Ulbrich. All rights reserved.
//

import UIKit
import AVFoundation
import AVKit

class MediaViewController: UIViewController {
    @IBOutlet var mediaView: MediaDetailView!
    
    enum MediaTypes {
        case video
        case picture
        case audio
        case none
    }
    
    var mediaType = MediaTypes.none
    var mediaUrl = ""
    
    var isCurrentlyShowing = false

    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        print(mediaType)
        
        switch mediaType {
        case .video:
            self.navigationController?.setNavigationBarHidden(true, animated: false)
            if !isCurrentlyShowing {
                playVideo()
                isCurrentlyShowing = true
            } else {
                self.navigationController?.popViewController(animated: false)
            }
        case .picture:
            self.navigationController?.setNavigationBarHidden(false, animated: false)
            showPicture()
        case .audio:
            self.navigationController?.setNavigationBarHidden(true, animated: false)
            if !isCurrentlyShowing {
                playAudio()
                isCurrentlyShowing = true
            } else {
                self.navigationController?.popViewController(animated: false)
            }
        default:
            break
        }
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func playVideo () {
        let url = URL(string: mediaUrl)!
        
        let player = AVPlayer(url: url)
        
        let playerViewController = AVPlayerViewController()
        
        
        playerViewController.player = player
        
        self.present(playerViewController, animated: true) {
            playerViewController.player?.play()
        }
    }
    
    func showPicture() {
        mediaView.imageView.image = nil
        mediaView.loadingIndicator.startAnimating()
        self.view.addSubview(mediaView)
        
        URLSession.shared.dataTask(with: URL(string: mediaUrl)!, completionHandler: { (data, response, error) in
            if error != nil {
                print(error!)
                return
            }
            
            let image = UIImage(data: data!)
            
            // back to main ui thread
            DispatchQueue.main.async { [weak self] in
                self?.mediaView.loadingIndicator.stopAnimating()
                self?.mediaView.imageView.image = image
            }
            
        }).resume()
        
        mediaView.translatesAutoresizingMaskIntoConstraints = false
        mediaView.topAnchor.constraint(equalTo: self.view.topAnchor).isActive = true
        mediaView.bottomAnchor.constraint(equalTo: self.view.bottomAnchor).isActive = true
        mediaView.leftAnchor.constraint(equalTo: self.view.leftAnchor).isActive = true
        mediaView.rightAnchor.constraint(equalTo: self.view.rightAnchor).isActive = true

    }
    
    func playAudio() {
        let url = URL(string: mediaUrl)!
        
        let player = AVPlayer(url: url)
        
        let playerViewController = AVPlayerViewController()
        
        playerViewController.player = player
        
        self.present(playerViewController, animated: true) {
            playerViewController.player?.play()
        }
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}

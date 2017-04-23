//
//  ViewController.swift
//  Buddy
//
//  Created by Peter Ulbrich on 12.04.17.
//  Copyright © 2017 Peter Ulbrich. All rights reserved.
//

import UIKit
import Mapbox

class MapViewController: UIViewController, MGLMapViewDelegate, UIGestureRecognizerDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate, CLLocationManagerDelegate {
    
    @IBOutlet var mapView: MGLMapView!
    
    let locationManager = CLLocationManager()
    var lastLoc: CLLocationCoordinate2D?
    
    var timer: Timer?
    
    var selectedDrop: Drop?
    
    let MB = 1 << 20
    
    override func viewDidLoad() {
        super.viewDidLoad()
  
        // For use in foreground
        self.locationManager.requestWhenInUseAuthorization()
        
        if CLLocationManager.locationServicesEnabled() {
            locationManager.delegate = self
            locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters
            locationManager.startUpdatingLocation()
        }
        
        
        // some caching for pictures
        let memoryCapacity = 500 * MB
        let diskCapacity = 500 * MB
        let urlCache = URLCache(memoryCapacity: memoryCapacity, diskCapacity: diskCapacity, diskPath: "buddyDiskPath")
        URLCache.shared = urlCache
        
        //mapView.logoView.isHidden = true
        //mapView.attributionButton.isHidden = true
        mapView.delegate = self
        
        self.timer = Timer.scheduledTimer(timeInterval: 5, target: self, selector: #selector(MapViewController.fetchAndDisplay), userInfo: nil, repeats: true)
        fetchAndDisplay()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        //self.navigationController?.setNavigationBarHidden(true, animated: animated)
        super.viewWillAppear(animated)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        //self.navigationController?.setNavigationBarHidden(false, animated: animated)
        super.viewWillDisappear(animated)
    }
    
    func fetchAndDisplay() {
        Api.fetchDrops { (drops) in
            print("GOT \(drops.count) drops")
            if let annotationsOnMap = self.mapView.annotations {
                self.mapView.removeAnnotations(annotationsOnMap)
            }
            for drop in drops {
                let location = CLLocationCoordinate2DMake(drop.latitude, drop.longitude)
                let annotation = DropPointAnnotation(coordinate: location, title: drop.comment, subtitle: nil, drop: drop)
                self.mapView.addAnnotation(annotation)
            }
        }
    }
    
    /*
     private func calloutSetup() {
     placeCalloutView(asVisible: false, withAnimation: false)
     /*
     calloutView.translatesAutoresizingMaskIntoConstraints = false
     calloutView.topAnchor.constraint(equalTo: self.view.bottomAnchor).isActive = true
     calloutView.heightAnchor.constraint(equalToConstant: 400).isActive = true
     calloutView.widthAnchor.constraint(equalTo: self.view.widthAnchor).isActive = true
     */
     }
     */
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if segue.identifier == "mediaSegue" {
            let nextScene =  segue.destination as! MediaViewController
            
            guard let drop = selectedDrop else { return }
            // Pass the selected object to the new view controller.
            switch drop.mediaType.lowercased() {
            case "image":
                nextScene.mediaType = .picture
            case "video":
                nextScene.mediaType = .video
            case "sound":
                nextScene.mediaType = .audio
            default:
                nextScene.mediaType = .none
            }
            nextScene.mediaUrl = drop.mediaUrl
        }
    }
    
    // MARK: - MapViewDelegate
    func mapView(_ mapView: MGLMapView, annotationCanShowCallout annotation: MGLAnnotation) -> Bool {
        return true
    }
    
    func mapView(_ mapView: MGLMapView, calloutViewFor annotation: MGLAnnotation) -> MGLCalloutView? {
        return CalloutView(representedObject: annotation)
    }
    
    func mapView(_ mapView: MGLMapView, tapOnCalloutFor annotation: MGLAnnotation) {
        // Optionally handle taps on the callout
        performSegue(withIdentifier: "mediaSegue", sender: self)
        
        // Hide the callout
        mapView.deselectAnnotation(annotation, animated: true)
    }
    
    func mapView(_ mapView: MGLMapView, didSelect annotation: MGLAnnotation) {
        guard let annotation = annotation as? DropPointAnnotation else { return }
        selectedDrop = annotation.drop
    }
    
    func mapView(_ mapView: MGLMapView, imageFor annotation: MGLAnnotation) -> MGLAnnotationImage? {
        guard let annotation = annotation as? DropPointAnnotation else {
            return nil
        }
        // Try to reuse the existing ‘pisa’ annotation image, if it exists.
        var annotationImage = mapView.dequeueReusableAnnotationImage(withIdentifier: annotation.drop.mediaType.lowercased())
        
        if annotationImage == nil {
            var image: UIImage
            
            switch annotation.drop.mediaType.lowercased() {
            case "image":
                image = #imageLiteral(resourceName: "marker_img")
            case "video":
                image = #imageLiteral(resourceName: "marker_video")
            case "sound":
                image = #imageLiteral(resourceName: "marker_sound")
            default:
                image = #imageLiteral(resourceName: "marker_hideable")
            }
            
            let size = CGSize(width: 39, height: 57)
            UIGraphicsBeginImageContext(size)
            image.draw(in: CGRect(x: 0, y: 0, width: size.width, height: size.height))
            let resizedImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            
            
            // Initialize the ‘pisa’ annotation image with the UIImage we just loaded.
            annotationImage = MGLAnnotationImage(image: resizedImage!, reuseIdentifier: annotation.drop.mediaType.lowercased())
        }
        
        return annotationImage
    }
    
    
    // MARK: - UIGestureRecognizerDelegate
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        return true
    }
    
    // MARK: - Media Picker
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : Any]) {
        let chosenImage = info[UIImagePickerControllerOriginalImage] as! UIImage
        Api.createDropWithImage(image: chosenImage, comment: "Hello from iOS", isHideable: false, lastLocation: lastLoc!)
        // use the image
        dismiss(animated: true, completion: nil)
    }
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        dismiss(animated: true, completion: nil)
    }
    
    // MARK: - Location Manager
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if lastLoc == nil {
            if let loc = manager.location?.coordinate {
                mapView.setCenter(loc, animated: true)

            }
        }
        lastLoc = manager.location?.coordinate ?? nil
        
    }
    
    
    
    
    // MARK: - Supporting
    
    @IBAction func addDrop(_ sender: Any) {
        let actionSheet = UIAlertController(title: "Select drop type", message: nil, preferredStyle: .actionSheet)
        
        let cancelButton = UIAlertAction(title: "Cancel", style: .cancel, handler: nil)
        actionSheet.addAction(cancelButton)
        
        let libButton = UIAlertAction(title: "Media from Library", style: .default) { (alert) in
            self.handleAddImageVideoDrop(useCamera: false)
        }
        actionSheet.addAction(libButton)
        
        let cameraButton = UIAlertAction(title: "Use Camera", style: .default) { (alert) in
            self.handleAddImageVideoDrop(useCamera: true)
        }
        actionSheet.addAction(cameraButton)
        
        let audioButton = UIAlertAction(title: "Audio", style: .default) { (alert) in
            self.handleAddAudioDrop()
        }
        actionSheet.addAction(audioButton)
        
        self.present(actionSheet, animated: true, completion: fetchAndDisplay)
    }
    
    func handleAddImageVideoDrop(useCamera: Bool) {
        let picker = UIImagePickerController()
        picker.delegate = self
        picker.allowsEditing = false
        if useCamera {
            picker.sourceType = .camera
        } else {
            picker.sourceType = .photoLibrary
        }
        self.present(picker, animated: true, completion: nil)
    }
    
    func handleAddAudioDrop() {
        
    }
    
}

extension UIImagePickerController {
    open override func viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        self.navigationBar.barTintColor = UIColor(colorLiteralRed: 50/255, green: 23/255, blue: 76/255, alpha: 1)
        self.navigationBar.topItem?.rightBarButtonItem?.tintColor = UIColor.white
        self.navigationBar.topItem?.rightBarButtonItem?.isEnabled = true
        self.navigationBar.titleTextAttributes = [
            NSForegroundColorAttributeName : UIColor.white
        ]

    }
}


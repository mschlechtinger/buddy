//
//  DropPointAnnotation.swift
//  Buddy
//
//  Created by Peter Ulbrich on 12.04.17.
//  Copyright © 2017 Peter Ulbrich. All rights reserved.
//

import Foundation
import Mapbox

// MGLAnnotation protocol reimplementation
class DropPointAnnotation: NSObject, MGLAnnotation {
    
    // As a reimplementation of the MGLAnnotation protocol, we have to add mutable coordinate and (sub)title properties ourselves.
    var coordinate: CLLocationCoordinate2D
    var title: String?
    var subtitle: String?
    
    // Custom properties that we will use to customize the annotation's image.
    var image: UIImage?
    //var reuseIdentifier: String?
    
    // Custom data
    var drop: Drop
    
    init(coordinate: CLLocationCoordinate2D, title: String?, subtitle: String?, drop: Drop) {
        self.coordinate = coordinate
        self.title = title
        self.subtitle = subtitle
        self.drop = drop
    }
}

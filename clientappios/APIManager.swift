//
//  APIManager.swift
//  Buddy
//
//  Created by Peter Ulbrich on 23.04.17.
//  Copyright © 2017 Peter Ulbrich. All rights reserved.
//

import Foundation
import UIKit
import Alamofire


let apiUrl = "http://buddy.theapi.cloud"
public struct Api {
    
    public static func fetchDrops(completion: @escaping ([Drop]) -> ()) {
        
        
        let url = URL(string: apiUrl + "/buddy/drops")!
        let urlSession = URLSession.shared
        
        let task = urlSession.dataTask(with: url) { (data, response, error) in
            let jsonData = try! JSONSerialization.jsonObject(with: data!, options: [])
            let dropsArray = jsonData as! [[String:AnyObject]]
            var drops = [Drop]()
            for drop in dropsArray {
                if let mediaUrl = drop["contentUrl"] as? String,
                    let author = drop["author"] as? String,
                    let authorPicUrl = drop["authorImgUrl"] as? String,
                    let mediaType = drop["dropType"] as? String,
                    let comment = drop["comment"] as? String,
                    let latitude = drop["latitude"] as? Double,
                    let longitude = drop["longitude"] as? Double,
                    let hideable = drop["hideable"] as? Bool,
                    let id = drop["id"] as? String,
                    let expirationDate = drop["expirationDate"] as? String
                {
                    
                    let thumbNailUrl = drop["thumbNailUrl"] as? String
                    
                    let drop = Drop(id: id, latitude: latitude, longitude: longitude, author: author, comment: comment, authorPicUrl: authorPicUrl, mediaType: mediaType, mediaUrl: mediaUrl, hideable: hideable, expirationDate: expirationDate, thumbNailUrl: thumbNailUrl)
                    drops.append(drop)
                } else {
                    print("PARSING JSON FAILED")
                }
            }
            
            completion(drops);
            
        }
        
        task.resume()
        
    }
    
    
    public static func createDropWithImage(image: UIImage, comment: String, isHideable: Bool) {
        let headers: HTTPHeaders = [String:String]()
        let URL = try! URLRequest(url: apiUrl + "/buddy/drops", method: .post, headers: headers)
        
        Alamofire.upload(multipartFormData: { multipartFormData in
            if let imageData = UIImagePNGRepresentation(image) {
                multipartFormData.append(imageData, withName: "fileData", fileName: "picture.png", mimeType: "image/png")
            }
            
            multipartFormData.append("iOS User".data(using: String.Encoding.utf8)!, withName: "author")
            multipartFormData.append("http://media.steampowered.com/steamcommunity/public/images/avatars/64/64e8917b85a0f5e20565651119c60f3d8d363898_full.jpg".data(using: String.Encoding.utf8)!, withName: "authorImgUrl")
            multipartFormData.append("Image".data(using: String.Encoding.utf8)!, withName: "dropType")
            multipartFormData.append(comment.data(using: String.Encoding.utf8)!, withName: "comment")
            multipartFormData.append("49.4755346".data(using: String.Encoding.utf8)!, withName: "latitude")
            multipartFormData.append("8.534418".data(using: String.Encoding.utf8)!, withName: "longitude")
            if isHideable {
                multipartFormData.append("true".data(using: String.Encoding.utf8)!, withName: "longitude")
            } else {
                multipartFormData.append("false".data(using: String.Encoding.utf8)!, withName: "longitude")
            }
            
            
            
        }, with: URL, encodingCompletion: {
            encodingResult in
            switch encodingResult {
            case .success(let upload, _, _):
                upload.responseJSON { response in
                    debugPrint("SUCCESS RESPONSE: \(response)")
                }
            case .failure(let encodingError):
                // hide progressbas here
                print("ERROR RESPONSE: \(encodingError)")
            }
        })
        
    }
    
}

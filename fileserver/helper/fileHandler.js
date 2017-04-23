'use strict';
const config = require('../config');
const uuidV4 = require('uuid/v4');
const path = require('path');
const thumb = require('node-thumbnail').thumb;
const thumbler = require('video-thumb');

var fileHandler = {};

fileHandler.uploadFile = function(req, res, next){
	//handle file Upload
 const fileId = uuidV4();

 let fileData = req.files.fileData;

  fileData.mv(__dirname + '/../files/' + fileId + path.extname(req.files.fileData.name), function(err) {
    if (err)
      return res.status(500).send(err.message);
    
    req.fileName = fileId;
    req.fileExt = path.extname(req.files.fileData.name);
    req.fileUrl = config.publicServiceAddress + '/files/' + fileId + path.extname(req.files.fileData.name);
    next();
	});
};

fileHandler.isBase64 = function(str) {
	var base64regex = /^([0-9a-zA-Z+/]{4})*(([0-9a-zA-Z+/]{2}==)|([0-9a-zA-Z+/]{3}=))?$/;
	return base64regex.test(str);
};

fileHandler.saveBase64 = function(req, res, next) {
	//fileHandler.isBase64(req.body.fileData)
	if(true) {

		  var binaryData = new Buffer(req.body.fileData, 'base64').toString('binary');

			let fileName = uuidV4();

			require("fs").writeFile(__dirname + '/../files/' + fileName + '.jpg', binaryData, "binary", function(err) {

			 	//Handle errors
			    if (err){
			 	  console.log(err); // writes out file without error, but it's not a valid image
			      return res.status(500).send(err.message);
			    }

				req.fileName = fileName;
			    req.fileExt = ".jpg";
			    req.fileUrl = config.publicServiceAddress + '/files/' + fileName + ".jpg";
			    next();
			});
		} else{
			next();
		}
};

fileHandler.makeThumbnail = function(req, res, next) {
	if(req.fileExt && [".jpg",".png",".jpeg"].includes(req.fileExt)){


		thumb({
		  source: __dirname + '/../files/' + req.fileName + req.fileExt, // could be a filename: dest/path/image.jpg 
		  destination: __dirname + '/../files/',
		  quiet: true,
		  width: 250,
		  concurrency: 1
		}, function(err, stdout, stderr) {
			if(err)  {
				console.log(err.message);
			}else{
				  req.thumbNailUrl = config.publicServiceAddress + '/files/' + req.fileName + '_thumb' + req.fileExt;
			}
			next();
		});
	}else{
		if(req.fileExt && [".mp4"].includes(req.fileExt)){


			thumbler.extract(__dirname + '/../files/' + req.fileName + req.fileExt, __dirname + '/../files/' + req.fileName + '_thumb.png', '00:00:01', '250x250', function(){
				
				req.thumbNailUrl = config.publicServiceAddress + '/files/' + req.fileName + '_thumb.png';
				next();
			});

		}else{
			//carry on without thumbnail Q.Q
			console.log("no thumbnail created for filetype " + req.fileExt);
			next();
		}
	}
};

module.exports = fileHandler;
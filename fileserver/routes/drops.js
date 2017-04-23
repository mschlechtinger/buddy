'use strict';

const express = require('express');
const router = express.Router();
const path = require('path');
const uuidV4 = require('uuid/v4');
const config = require('../config.js');

var drops = [];

//handle requests for path myserver/drops
router.get('/', function(req,res){

    //amazing
    res.status(200).json(drops);
});


router.post('/', function(req, res) {

  //some input validation
  var valid = true, message = "";

  if (!req.files){
      valid = false; message += 'No files were uploaded.';
  }
  if(!req.body.author){
      valid = false; message += 'No author was specified.';
  }
  if(!req.body.authorImgUrl){
      valid = false; message += 'No authorImgUrl was specified.';
  }
  if(!req.body.dropType  || (!(req.body.dropType.toString() === "Video") && !(req.body.dropType.toString() === "Image") && !(req.body.dropType.toString() === "Sound"))){
      valid = false; message += 'No valid dropType was specified.';
  }
  if(!req.body.comment){
      message += 'No comment was specified.';
  }
  if(!req.body.location){
      valid = false; message += 'No location was specified.';
  }
  if(!req.body.hideable){
      message += 'No hideable was specified.';
  }
if(!valid){
   return res.status(400).json({error: message});
}

 //handle file Upload
 const fileId = uuidV4();

 let fileData = req.files.fileData;

  fileData.mv(__dirname + '/../files/' + fileId + path.extname(req.files.fileData.name), function(err) {
    if (err)
      return res.status(500).send(err.message);
    
    var drop = {
        contentUrl : config.publicServiceAddress + '/files/' + fileId + path.extname(req.files.fileData.name),
        author: req.body.author,
        authorImgUrl: req.body.authorImgUrl,
        dropType: req.body.dropType,
        comment: req.body.comment,
        previewImg: undefined,
        location: req.body.location,
        hideable: req.body.hideable || false,
        id: uuidV4(),
        expirationDate: req.body.expirationDate || new Date()
      };
      drops.push(drop);
    res.status(200).json({status: 'Drop created!', id: drop.id, drop: drop});
  });
});
module.exports = router;
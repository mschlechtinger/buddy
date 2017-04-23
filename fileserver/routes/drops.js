'use strict';

const express = require('express');
const router = express.Router();
const uuidV4 = require('uuid/v4');
const validation = require('../helper/validation');
const fileHandler = require('../helper/fileHandler');

var drops = [];

//handle requests for path myserver/drops
router.get('/', function(req,res){

    //amazing
    res.status(200).json(drops);
});


router.post('/', validation.evalDrop, fileHandler.uploadFile, fileHandler.makeThumbnail, function(req, res) {

    var drop = {
        contentUrl : req.fileUrl,
        author: req.body.author,
        authorImgUrl: req.body.authorImgUrl,
        dropType: req.body.dropType,
        comment: req.body.comment,
        thumbNailUrl: req.thumbNailUrl,
        latitude: parseFloat(req.body.latitude),
        longitude: parseFloat(req.body.longitude),
        hideable: req.body.hideable || false,
        id: uuidV4(),
        expirationDate: req.body.expirationDate || new Date(),
        contentSize: undefined
      };

      drops.push(drop);

    res.status(200).json({status: 'Drop created!', id: drop.id, drop: drop});
  
});
module.exports = router;
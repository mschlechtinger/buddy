'use strict';

const express = require('express');
const router = express.Router();
const path = require('path');
const uuidV4 = require('uuid/v4');
const config = require('../config.js');

//distribute requests to /drops
router.use('/drops', require('./drops'));
//handle requests for path myserver/files
router.get('/files/:fileId', function(req,res,next){

	var options = {
	    root: __dirname + '/../files/',
	    dotfiles: 'deny',
	    headers: {
	        'x-timestamp': Date.now(),
	        'x-sent': true
	    }
	};

	var fileName = req.params.fileId;

  res.sendFile(fileName, options, function (err) {
    if (err) {
      next(err.message);
    } else {
      console.log('Sent:', fileName);
    }
  });

});

router.post('/files', function(req, res) {

  if (!req.files)
    return res.status(400).send('No files were uploaded.');
 
 const fileId = uuidV4();

 let fileData = req.files.fileData;

  fileData.mv(__dirname + '/../files/' + fileId + path.extname(req.files.fileData.name), function(err) {
    if (err)
      return res.status(500).send(err.message);
    
    console.log('Uploaded: ' + req.files.fileData.name + " as " + fileId + path.extname(req.files.fileData.name));
    res.status(200).json({status: 'File uploaded!', fileUrl: config.publicServiceAddress + '/files/' + fileId + path.extname(req.files.fileData.name)});
  });
});
module.exports = router;
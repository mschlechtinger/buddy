'use strict';

var validation = {};

validation.evalDrop = function(req, res , next){
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
	  if(!req.body.latitude){
	      valid = false; message += 'No latitude was specified.';
	  }
	  if(!req.body.longitude){
	      valid = false; message += 'No longitude was specified.';
	  }
	  if(!req.body.hideable){
	      message += 'No hideable was specified.';
	  }
	if(!valid){
	   return res.status(400).json({error: message});
	}

	next();
};

module.exports = validation;
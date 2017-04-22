'use strict';

const passport = require('passport');
const express = require('express');
const router = express.Router();
const authenticationCheck = require('../../authentication/authenticationCheck');

//handle request for path myserver/auth/login
router.post('/', passport.authenticate('local', { successRedirect: '/auth/login/success', failureRedirect: '/auth/login/fail'}));


//handle request for path myserver/auth/login/success
router.get('/success',authenticationCheck, function(req, res){
  res.status(200).json({"status":"OK, " + req.user});
});

//handle request for path myserver/auth/login/fail
router.get('/fail', function(req, res){
  res.status(401).json({error: 'User Login failed'});
});

//handle request for path myserver/auth/login/testAuth
router.get('/testAuth', function(req, res){
	if (req.isAuthenticated()){
		console.log('User is logged in: \n'+ req.user);
		res.status(200).json({status: 'A User is logged in'});
	}
	if (!req.user){
		console.log('No User is logged in');
		res.status(401).json({status: 'No user is logged in'});
	}
});

//exports the router as a node module
module.exports = router;
'use strict';

const express = require('express');
const router = express.Router();
const Account = require('../../models/account');


//handle request for path myserver/auth/register
router.post('/', function(req, res) {
  console.log('registering user');
  Account.register(new Account({username: req.body.username}), req.body.password, function(err, user) {
    if (err) {
      console.log(err.name, err.message);
      var jsonResponse = {};
      jsonResponse[err.name] = err.message;
      res.status(409).json(jsonResponse);
      return;
    }
    console.log('user registered!');
    
    //Login the newly registered user
    req.logIn(user, function(err) {
      if (err) {
        console.log(err);
      }
      console.log('User logged in!');
    });
    res.status(200).json({"status":"You registered the user " + req.body.username});
  });
});

//exports the router as a node module
module.exports = router;
'use strict';

const express = require('express');
const router = express.Router();
const authenticationCheck = require('../../authentication/authenticationCheck');

//handle request for path myserver/auth/logout
router.get('/', authenticationCheck, function(req, res) {
      req.logout();
      console.log("User logged out.");
      res.status(200).json({status: 'User logged out successfully'});
  });
//exports the router as a node module
module.exports = router;
'use strict';

const bodyParser = require('body-parser');
const express = require('express');
const routes =  require('./routes');
const config = require('./config');

const passport = require("passport");
const session = require('express-session');
const mongoose = require('mongoose');
const MongoStore = require('connect-mongo')(session);

const app = express();

// Use native promises
mongoose.Promise = global.Promise;
// Connect mongoose
mongoose.connect(config.mongoUri, function(err) {
  if (err) {
    console.log(`Could not connect to mongodb on ${config.mongoUri}.`);
  }
});

//Configure Express Session Handling
app.use(session({
	secret: config.cookieSecret,
	store: new MongoStore({ url: config.mongoUri }),
	name: config.cookieName,
	proxy: true,
	resave: true,
	saveUninitialized: true}
));
app.use(bodyParser.json());

// Configure passport middleware
app.use(passport.initialize());
app.use(passport.session());

// Configure passport to use account model for authentication
var Account = require('./models/account');
passport.use(Account.createStrategy());

passport.serializeUser(Account.serializeUser());
passport.deserializeUser(Account.deserializeUser());

app.use(routes);

app.listen(config.servicePort, function(){
	console.log(`Codeserver is running on ${config.localIp}:${config.servicePort}`);
});
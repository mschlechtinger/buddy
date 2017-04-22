'use strict';

const bodyParser = require('body-parser');
const express = require('express');
const routes =  require('./routes');
const config = require('./config');
const fileUpload = require('express-fileupload');

const app = express();

app.use(bodyParser.json());
app.use(fileUpload());
app.use(routes);

app.listen(config.servicePort, function(){
	console.log(`Fileserver is running on ${config.localIp}:${config.servicePort}`);
});
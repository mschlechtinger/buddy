'use strict';

const express = require('express');
const router = express.Router();
const uuidV4 = require('uuid/v4');

var people = [];

//handle requests for path myserver/people
router.get('/', function(req,res){

    //amazing
    res.status(200).json(people);
});


router.post('/', function(req, res) {

    var person = {
        
        name: req.body.name,
        imgUrl: req.body.authorImgUrl,
        latitude: parseFloat(req.body.latitude),
        longitude: parseFloat(req.body.longitude),
        id: uuidV4(),
        date: req.body.expirationDate || new Date()
      };

      people.push(person);

    res.status(200).json({status: 'Person located!', id: person.id});
});

router.put('/:personId', function(req,res){

	var person = people.filter(function(person){return req.params.personId.toString() === person.id;});

	if(!person) return res.status(404).json({error:"Person not found."});

	var personIndex = people.indexOf(person);

	people[personIndex].latitude = req.body.latitude;
	people[personIndex].longitude = req.body.longitude;
	people[personIndex].date = new Date();
	res.status(204).send();
});
module.exports = router;
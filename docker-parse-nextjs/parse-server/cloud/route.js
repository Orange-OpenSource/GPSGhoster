/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
* Author: Franck SEROT <frank.serot@orange.com> et al.
*/
Parse.Cloud.afterDelete("Route", function(request, response) {
//    console.log('resquet.object : ' + JSON.stringify(request.object))
//    console.log('request.object.objectId : ' + request.object.id)

    query1 = new Parse.Query('Position');
    query1.equalTo('route', request.object);
    query1.find()
        .then(Parse.Object.destroyAll)
        .catch((error) => {
          console.error("Error finding related position " + error.code + ": " + error.message);
        });

    query2 = new Parse.Query('FakePosition');
    query2.equalTo('route', request.object);
    query2.find()
        .then(Parse.Object.destroyAll)
        .catch((error) => {
          console.error("Error finding related fake position " + error.code + ": " + error.message);
        });
})

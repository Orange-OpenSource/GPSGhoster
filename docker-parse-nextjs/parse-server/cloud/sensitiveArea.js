/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
* Author: Franck SEROT <frank.serot@orange.com> et al.
*/
Parse.Cloud.afterDelete("SensitiveArea", function(request, response) {
//    console.log('resquet.object : ' + JSON.stringify(request.object))
//    console.log('request.object.objectId : ' + request.object.id)
//    console.log('resquet.object.bbox : ' + JSON.stringify(request.object.get('bbox')))
//    console.log('resquet.object.bbox.objectId : ' + request.object.get('bbox').id)
    query = new Parse.Query('BBox');
    query.equalTo('objectId', request.object.get('bbox').id);
    query.find()
        .then(Parse.Object.destroyAll)
        .catch((error) => {
          console.error("Error finding related comments " + error.code + ": " + error.message);
        });
})

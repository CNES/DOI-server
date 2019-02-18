/**
 * Must have library require-xml in nodejs
 */
require('require-xml');

/**
 * Put here the path to the xml
 */
var data = require("./xml_test_kernel_4.1.xml");

/**
 * Access to JS class Resource, which contains the xml as a JS object
 */
var Resource = require("./org.datacite.schema.kernel_4.1/Resource.js");

/**
 * Parse xml to json for creating the object Resource
 */
var xmlResource = new Resource(JSON.parse(data));

//console.log(data);

console.log(xmlResource.getGeoLocations());
console.log(xmlResource.getGeoLocations()[1].getGeoLocationPlace());
console.log(xmlResource.getGeoLocations()[1].getGeoLocationBox());
console.log(xmlResource.getGeoLocations()[1].getGeoLocationPolygon()[0]);
console.log(xmlResource.getGeoLocations()[1].getGeoLocationPolygon()[0].getPolygonPoint());


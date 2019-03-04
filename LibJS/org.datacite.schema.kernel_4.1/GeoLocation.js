/**
 * 
 */
import { Point } from './Point.js';
import { Box } from './Box.js';
import { GeoLocationPolygon } from './GeoLocationPolygon.js';

export class GeoLocation{
	
	constructor(json){
		if(json == undefined){
			return;
		}
		
		this.geoLocationPlace = json.geoLocationPlace;
		
		this.geoLocationPoint = new Point(json.geoLocationPoint);
		
		this.geoLocationBox = new Box(json.geoLocationBox);
		
		this.geoLocationPolygon = [];
		if(json.geoLocationPolygon instanceof Array){
			for(var i = 0; i < json.geoLocationPolygon.length; i++){
				this.geoLocationPolygon.push(new GeoLocationPolygon(json.geoLocationPolygon[i]));
			}
		} else {
			this.geoLocationPolygon.push(new GeoLocationPolygon(json.geoLocationPolygon));
		}
	}
	
	getGeoLocationPlace(){
		return this.geoLocationPlace;
	}
	getGeoLocationPoint(){
		return this.geoLocationPoint;
	}
	getGeoLocationBox(){
		return this.geoLocationBox;
	}
	getGeoLocationPolygon(){
		return this.geoLocationPolygon;
	}
	
	setGeoLocationPlace(place){
		this.geoLocationPlace = place;
	}
	setGeoLocationPoint(point){
		this.geoLocationPoint = new Point(point);
	}
	setGeoLocationBox(box){
		this.geoLocationBox = new Box(box);
	}
	setGeoLocationPolygon(geoLoc){
		this.geoLocationPolygon = [];
		if(geoLoc instanceof Array){
			for(var i = 0; i < geoLoc.length; i++){
				this.geoLocationPolygon.push(new GeoLocationPolygon(geoLoc[i]));
			}
		} else {
			this.geoLocationPolygon.push(new GeoLocationPolygon(geoLoc));
		}
	}
}
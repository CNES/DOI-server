/**
 * 
 */
import { Point } from './Point.js';

export class GeoLocationPolygon{
	
	constructor(json){
		if(json == undefined){
			return;
		}
		this.polygonPoint = [];
		if(json.polygonPoint instanceof Array){
			for(var i = 0; i < json.polygonPoint.length; i++){
				this.polygonPoint.push(new Point(json.polygonPoint[i]));
			}
		} else {
			this.polygonPoint.push(new Point(json.polygonPoint));
		}
		
		this.inPolygonPoint = new Point(json.inPolygonPoint);
	}
	
	getPolygonPoint(){
		return this.polygonPoint;
	}
	getInPolygonPoint(){
		return this.inPolygonPoint;
	}
	
	setPolygonPoint(polyPoint){
		this.polygonPoint = [];
		if(polyPoint instanceof Array){
			for(var i = 0; i < polyPoint.length; i++){
				this.polygonPoint.push(new Point(polyPoint[i]));
			}
		} else {
			this.polygonPoint.push(new Point(polyPoint));
		}
	}
	setInPolygonPoint(inPoint){
		this.inPolygonPoint = new Point(inPoint);
	}
}
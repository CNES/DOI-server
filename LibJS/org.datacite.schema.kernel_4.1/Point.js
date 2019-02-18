/**
 * 
 */
module.exports = class Point{
	
	constructor(json){
		this.pointLongitude = json.pointLongitude;
		this.pointLatitude = json.pointLatitude;
	}
	
	getPointLongitude(){
		return this.pointLongitude;
	}
	getPointLatitude(){
		return this.pointLatitude;
	}
	
	setPointLongitude(long){
		this.pointLongitude = long;
	}
	setPointLatitude(lat){
		this.pointLatitude = lat;
	}
}
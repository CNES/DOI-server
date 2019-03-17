/**
 * 
 */
export class Point{
	
	constructor(json){
		if(json == undefined){
			return;
		}
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
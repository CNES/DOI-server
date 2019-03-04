/**
 * 
 */
export class Box{
	
	constructor(json){
		if(json == undefined){
			return;
		}
		this.westBoundLongitude = json.westBoundLongitude;
		this.eastBoundLongitude = json.eastBoundLongitude;
		this.southBoundLatitude = json.southBoundLatitude;
		this.northBoundLatitude = json.northBoundLatitude;
	}
	
	getWestBoundLongitude(){
		return this.westBoundLongitude;
	}
	getEastBoundLongitude(){
		return this.eastBoundLongitude;
	}
	getSouthBoundLatitude(){
		return this.southBoundLatitude;
	}
	getNorthBoundLatitude(){
		return this.northBoundLatitude;
	}
	
	setWestBoundLongitude(west){
		this.westBoundLongitude = west;
	}
	setEastBoundLongitude(east){
		this.eastBoundLongitude = east;
	}
	setSouthBoundLatitude(south){
		this.southBoundLatitude = south;
	}
	setNorthBoundLatitude(north){
		this.northBoundLatitude = north;
	}
}
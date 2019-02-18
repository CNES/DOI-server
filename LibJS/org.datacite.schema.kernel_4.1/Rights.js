/**
 * 
 */
module.exports = class Rights{
	
	constructor(json){
		this.rights = json.$t;
		this.rightsURI = json.rightsURI;
	}
	
	getRights(){
		return this.rights;
	}
	getRightsURI(){
		return this.rightsURI;
	}
	
	setRights(right){
		this.rights = right;
	}
	setRightsURI(uri){
		this.rightsURI = uri;
	}
}
/**
 * 
 */
module.exports = class Identifier{
	
	constructor(json){
		this.identifier = json.$t;
		this.identifierType = json.identifierType;
	}
	
	getIdentifier(){
		return this.identifier;
	}
	getIdentifierType(){
		return this.identifierType;
	}
	
	setIdentifier(id){
		this.identifier = id;
	}
	setIdentifierType(idType){
//		this.identifierType = idType;
		this.identifierType = "DOI";
	}
}
/**
 * 
 */
module.exports = class AlternateIdentifier{
	
	constructor(json){
		this.alternateIdentifier = json.$t;
		this.alternateIdentifierType = json.alternateIdentifierType;
	}
	
	getAlternateIdentifier(){
		return this.alternateIdentifier;
	}
	getAlternateIdentifierType(){
		return this.alternateIdentifierType;
	}
	
	setAlternateIdentifier(altID){
		this.alternateIdentifier = altID;
	}
	setAlternateIdentifierType(altType){
		this.alternateIdentifierType = altType;
	}
}
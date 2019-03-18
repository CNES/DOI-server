/**
 * 
 */
export class AlternateIdentifier{
	
	constructor(json){
		if(json == undefined){
			return;
		}
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
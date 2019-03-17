/**
 * 
 */
export class Identifier{
	
	constructor(json){
		if(json == undefined){
			return;
		}
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
//		TODO this.identifierType = idType;
		this.identifierType = "DOI";
	}
}
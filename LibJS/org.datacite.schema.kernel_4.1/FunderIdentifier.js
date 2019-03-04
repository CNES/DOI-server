/**
 * 
 */
export class FunderIdentifier{
	
	constructor(json){
		if(json == undefined){
			return;
		}
		this.funderIdentifier = json.$t;
		this.funderIdentifierType = json.funderIdentifierType;
	}
	
	getFunderIdentifier(){
		return this.funderIdentifier;
	}
	getFunderIdentifierType(){
		return this.funderIdentifierType;
	}
	
	setFunderIdentifier(fundID){
		this.funderIdentifier = fundID;
	}
	setFunderIdentifierType(fundType){
		this.funderIdentifierType = fundType;
	}
}
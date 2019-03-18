/**
 * 
 */
export class AwardNumber{
	
	constructor(json){
		if(json == undefined){
			return;
		}
		this.awardNumber = json.$t;
		this.awardURI = json.awardURI;
	}
	
	getAwardNumber(){
		return this.awardNumber;
	}
	getAwardURI(){
		return this.awardURI;
	}
	
	setAwardNumber(number){
		this.awardNumber = number;
	}
	setAwardURI(uri){
		this.awardURI = uri;
	}
}
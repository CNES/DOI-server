/**
 * 
 */
import { AwardNumber } from './AwardNumber.js';
import { FunderIdentifier } from './FunderIdentifier.js';

export class FundingReference{
	
	constructor(json){
		if(json == undefined){
			return;
		}
		this.funderName = json.funderName;
		this.funderIdentifier = new FunderIdentifier(json.funderIdentifier);
		this.awardNumber = new AwardNumber(json.awardNumber);
		this.awardTitle = json.awardTitle;
	}
	
	getFunderName(){
		return this.funderName;
	}
	getFunderIdentifier(){
		return this.funderIdentifier;
	}
	getAwardNumber(){
		return this.awardNumber;
	}
	getAwardTitle(){
		return this.awardTitle;
	}
	
	setFunderName(name){
		this.funderName = name;
	}
	setFunderIdentifier(fundID){
		this.funderIdentifier = new FunderIdentifier(fundID);
	}
	setAwardNumber(award){
		this.awardNumber = new AwardNumber(award);
	}
	setAwardTitle(title){
		this.awardTitle = title;
	}
}
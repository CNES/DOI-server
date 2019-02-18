/**
 * 
 */
var AwardNumber = require("./AwardNumber.js");
var FunderIdentifier = require("./FunderIdentifier.js");

module.exports = class FundingReference{
	
	constructor(json){
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
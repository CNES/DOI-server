/**
 * 
 */
import { Name } from './Name.js';
import { NameIdentifier } from './NameIdentifier.js';

export class Creator{
	
	constructor(json){
		if(json == undefined){
			return;
		}
		this.creatorName = new Name(json.creatorName);
		
		this.givenName = json.givenName;
		this.familyName = json.familyName;
		
		this.nameIdentifier = [];
		if (json.nameIdentifier instanceof Array) {
			for(var i=0 ; i<json.nameIdentifier.length ; i++){
				this.nameIdentifier.push(new NameIdentifier(json.nameIdentifier[i]));
			}
		} else {
			this.nameIdentifier.push(new NameIdentifier(json.nameIdentifier));
		}
		
		this.affiliation = [];
		if (json.affiliation instanceof Array) {
			for(var i=0 ; i<json.affiliation.length ; i++){
				this.affiliation.push(json.affiliation[i]);
			}
		} else {
			this.affiliation.push(json.affiliation);
		}
	}
	
	getCreatorName(){
		return this.creatorName;
	}
	getGivenName(){
		return this.givenName;
	}
	getFamilyName(){
		return this.familyName;
	}
	getNameIdentifier(){
		return this.nameIdentifier;
	}
	getAffiliation(){
		return this.affiliation;
	}
	
	setCreatorName(name){
		this.creatorName = new Name(name);
	}
	setGivenName(name){
		this.givenName = name;
	}
	setFamilyName(name){
		this.familyName = name;
	}
	setNameIdentifier(nameID){
		this.nameIdentifier = [];
		if (nameID instanceof Array) {
			for(var i=0 ; i<nameID.length ; i++){
				this.nameIdentifier.push(new NameIdentifier(nameID[i]));
			}
		} else {
			this.nameIdentifier.push(new NameIdentifier(nameID));
		}
	}
	setAffiliation(aff){
		this.affiliation = [];
		if (aff instanceof Array) {
			for(var i=0 ; i<aff.length ; i++){
				this.affiliation.push(aff[i]);
			}
		} else {
			this.affiliation.push(aff);
		}
	}
	
}
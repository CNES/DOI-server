/**
 * 
 */
module.exports = class Name{
	
	constructor(json){
		this.name = json.$t;
		this.nameType = json.nameType;
	}
	
	getName(){
		return this.name;
	}
	getNameType(){
		return this.nameType;
	}
	
	setName(name){
		this.name = name;
	}
	setNameType(nameType){
		this.nameType = nameType;
	}
}
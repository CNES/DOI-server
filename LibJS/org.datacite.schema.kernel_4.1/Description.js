/**
 * 
 */
module.exports = class Description{
	
	constructor(json){
		this.description = json.$t;
		this.descriptionType = json.descriptionType;
	}
	
	getDescription(){
		return this.description;
	}
	getDescriptionType(){
		return this.descriptionType;
	}
	
	setDescription(desc){
		this.description = desc;
	}
	setDescriptionType(descType){
		this.descriptionType = descType;
	}
}
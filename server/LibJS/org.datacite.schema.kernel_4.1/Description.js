/**
 * 
 */
export class Description{
	
	constructor(json){
		if(json == undefined){
			return;
		}
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
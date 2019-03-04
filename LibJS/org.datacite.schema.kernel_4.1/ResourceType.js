/**
 * 
 */
export class ResourceType{
	
	constructor(json){
		if(json == undefined){
			return;
		}
		this.resourceType = json.$t;
		this.resourceTypeGeneral = json.resourceTypeGeneral;
	}
	
	getResourceType(){
		return this.resourceType;
	}
	getResourceTypeGeneral(){
		return this.resourceTypeGeneral;
	}
	
	setResourceType(type){
		this.resourceType = type;
	}
	setResourceTypeGeneral(typeGeneral){
		this.resourceTypeGeneral = typeGeneral;
	}
}
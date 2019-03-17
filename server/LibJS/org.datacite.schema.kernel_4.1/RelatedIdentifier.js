/**
 * 
 */
export class RelatedIdentifier{
	
	constructor(json){
		if(json == undefined){
			return;
		}
		this.relatedIdentifier = json.$t;
		this.relatedIdentifierType = json.relatedIdentifierType;
		this.relationType = json.relationType;
		this.resourceTypeGeneral = json.resourceTypeGeneral;
		this.relatedMetadataScheme = json.relatedMetadataScheme;
		this.schemeType = json.schemeType;
		this.schemeURI = json.schemeURI;
	}
	
	getRelatedIdentifier(){
		return this.relatedIdentifier;
	}
	getRelatedIdentifierType(){
		return this.alternateIdentifierType;
	}
	getRelationType(){
		return this.relationType;
	}
	getResourceTypeGeneral(){
		return this.resourceTypeGeneral;
	}
	getRelatedMetadataScheme(){
		return this.relatedMetadataScheme;
	}
	getSchemeType(){
		return this.schemeType;
	}
	getSchemeURI(){
		return this.schemeURI;
	}
	
	setRelatedIdentifier(relID){
		this.relatedIdentifier = relID;
	}
	setRelatedIdentifierType(relType){
		this.alternateIdentifierType = relType;
	}
	setRelationType(type){
		this.relationType = type;
	}
	setResourceTypeGeneral(typeGeneral){
		this.resourceTypeGeneral = typeGeneral;
	}
	setRelatedMetadataScheme(scheme){
		this.relatedMetadataScheme = scheme;
	}
	setSchemeType(schemeType){
		this.schemeType = schemeType;
	}
	setSchemeURI(uri){
		this.schemeURI = uri;
	}
}
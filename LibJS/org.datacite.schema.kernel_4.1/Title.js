/**
 * 
 */
module.exports = class Title{
	
	constructor(json){
		this.title = json.$t;
		this.titleType = json.titleType;
	}
	
	getTitle(){
		return this.title;
	}
	getTitleType(){
		return this.titleType;
	}
	
	setTitle(title){
		this.title = title;
	}
	setTitleType(type){
		this.titleType = type;
	}
}
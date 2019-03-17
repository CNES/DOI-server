/**
 * 
 */
export class Date{
	
	constructor(json){
		if(json == undefined){
			return;
		}
		this.date = json.$t;
		this.dateType = json.dateType;
		this.dateInformation = json.dateInformation;
	}
	
	getDate(){
		return this.date;
	}
	getDateType(){
		return this.dateType;
	}
	getDateInformation(){
		return this.dateInformation;
	}
	
	setDate(date){
		this.date = date;
	}
	setDateType(dateType){
		this.dateType = dateType;
	}
	setDateInformation(dateInf){
		this.dateInformation = dateInf;
	}
}
/**
 * 
 */
module.exports = class Subject{
	
	constructor(json){
		this.subject = json.$t;
		this.subjectScheme = json.subjectScheme;
		this.schemeURI = json.schemeURI;
		this.valueURI = json.valueURI;
	}
	
	getSubject(){
		return this.subject;
	}
	getSubjectScheme(){
		return this.subjectScheme;
	}
	getSchemeURI(){
		return this.schemeURI;
	}
	getValueURI(){
		return this.valueURI;
	}
	
	setSubject(subject){
		this.subject = subject;
	}
	setSubjectScheme(scheme){
		this.subjectScheme = scheme;
	}
	setSchemeURI(uri){
		this.schemeURI = uri;
	}
	setValueURI(uri){
		this.valueURI = uri;
	}
}
/**
 * JavaScript class Resource.
 * Contain getter and setter for every elements in XML from 
 * https://schema.datacite.org/meta/kernel-4.1/metadata.xsd
 */
	// --- required fields --- \\
import { Identifier } from './Identifier.js';
import { Creator } from './Creator.js';
import { Title } from './Title.js';
import { ResourceType } from './ResourceType.js';

	//--- optional fields --- \\
import { Subject } from './Subject.js';
import { Contributor } from './Contributor.js';
import { Date } from './Date.js';
import { AlternateIdentifier } from './AlternateIdentifier.js';
import { RelatedIdentifier } from './RelatedIdentifier.js';
import { Rights } from './Rights.js';
import { Description } from './Description.js';
import { GeoLocation } from './GeoLocation.js';
import { FundingReference } from './FundingReference.js';

export class Resource{
	
	constructor(data){
		if(data == undefined){
			return;
		}
		// --- required fields --- \\
//		console.log(data);
		
		// Identifier
		this.identifier = new Identifier(data["resource"].identifier);
		
		// Creators
		this.creators = [];
		if(data["resource"].creators != undefined){
			if (data["resource"].creators.creator instanceof Array) {
				for(var i = 0; i < data["resource"].creators.creator.length; i++){
					this.creators.push(new Creator(data["resource"].creators.creator[i]));
				}
			} else {
				this.creators.push(new Creator(data["resource"].creators.creator));
			}
		}
		
		// Titles
		this.titles = [];
		if(data["resource"].titles != undefined){
			if(data["resource"].titles.title instanceof Array){
				for(var i = 0; i < data["resource"].titles.title.length; i++){
					this.titles.push(new Title(data["resource"].titles.title[i]));
				}
			} else {
				this.titles.push(new Title(data["resource"].titles.title));
			}
		}
		
		// Publisher
		this.publisher = data["resource"].publisher;
		
		// PublicationYear
		this.publicationYear = data["resource"].publicationYear;
		
		// ResourceType
		this.resourceType = new ResourceType(data["resource"].resourceType);
		
		// --- optional fields --- \\
		// Subjects
		this.subjects = [];
		if(data["resource"].subjects != undefined){
			if(data["resource"].subjects.subject instanceof Array){
				for(var i = 0; i < data["resource"].subjects.subject.length; i++){
					this.subjects.push(new Subject(data["resource"].subjects.subject[i]));
				}
			} else {
				this.subjects.push(new Subject(data["resource"].subjects.subject));
			}
		}
		
		// Contributors
		this.contributors = [];
		if(data["resource"].contributors != undefined){
			if(data["resource"].contributors.contributor instanceof Array){
				for(var i = 0; i < data["resource"].contributors.contributor.length; i++){
					this.contributors.push(new Contributor(data["resource"].contributors.contributor[i]));
				}
			} else {
				this.contributors.push(new Contributor(data["resource"].contributors.contributor));
			}
		}
		
		// Dates
		this.dates = [];
		if(data["resource"].dates != undefined){
			if(data["resource"].dates.date instanceof Array){
				for(var i = 0; i < data["resource"].dates.date.length; i++){
					this.dates.push(new Date(data["resource"].dates.date[i]));
				}
			} else {
				this.dates.push(new Date(data["resource"].dates.date));
			}
		}
		
		// Language
		this.language = data["resource"].language;
		
		// AlternateIdentifiers
		this.alternateIdentifiers = [];
		if(data["resource"].alternateIdentifiers != undefined){
			if(data["resource"].alternateIdentifiers.alternateIdentifier instanceof Array){
				for(var i = 0; i < data["resource"].alternateIdentifiers.alternateIdentifier.length; i++){
					this.alternateIdentifiers.push(
							new AlternateIdentifier(data["resource"].alternateIdentifiers.alternateIdentifier[i]));
				}
			} else {
				this.alternateIdentifiers.push(
						new AlternateIdentifier(data["resource"].alternateIdentifiers.alternateIdentifier));
			}
		}
		
		// RelatedIdentifiers
		this.relatedIdentifiers = [];
		if(data["resource"].relatedIdentifiers != undefined){
			if(data["resource"].relatedIdentifiers.relatedIdentifier instanceof Array){
				for(var i = 0; i < data["resource"].relatedIdentifiers.relatedIdentifier.length; i++){
					this.relatedIdentifiers.push(
							new RelatedIdentifier(data["resource"].relatedIdentifiers.relatedIdentifier[i]));
				}
			} else {
				this.relatedIdentifiers.push(
						new RelatedIdentifier(data["resource"].relatedIdentifiers.relatedIdentifier));
			}
		}
		
		// Sizes
		this.sizes = [];
		if(data["resource"].sizes != undefined){
			if(data["resource"].sizes.size instanceof Array){
				for(var i = 0; i < data["resource"].sizes.size.length; i++){
					this.sizes.push(data["resource"].sizes.size[i]);
				}
			} else {
				this.sizes.push(data["resource"].sizes.size);
			}
		}
		
		// Formats
		this.formats = [];
		if(data["resource"].formats != undefined){
			if(data["resource"].formats.format instanceof Array){
				for(var i = 0; i < data["resource"].formats.format.length; i++){
					this.formats.push(data["resource"].formats.format[i]);
				}
			} else {
				this.formats.push(data["resource"].formats.format);
			}
		}
		
		// Version
		this.version = data["resource"].version;
		
		// RightsList
		this.rightsList = [];
		if(data["resource"].rightsList != undefined){
			if(data["resource"].rightsList.rights instanceof Array){
				for(var i = 0; i < data["resource"].rightsList.rights.length; i++){
					this.rightsList.push(new Rights(data["resource"].rightsList.rights[i]));
				}
			} else {
				this.rightsList.push(new Rights(data["resource"].rightsList.rights));
			}
		}
		
		// Descriptions
		this.descriptions = [];
		if(data["resource"].descriptions != undefined){
			if(data["resource"].descriptions.description instanceof Array){
				for(var i = 0; i < data["resource"].descriptions.description.length; i++){
					this.descriptions.push(new Description(data["resource"].descriptions.description[i]));
				}
			} else {
				this.descriptions.push(new Description(data["resource"].descriptions.description));
			}
		}
		
		// GeoLocations
		this.geoLocations = [];
		if(data["resource"].geoLocations != undefined){
			if(data["resource"].geoLocations.geoLocation instanceof Array){
				for(var i = 0; i < data["resource"].geoLocations.geoLocation.length; i++){
					this.geoLocations.push(new GeoLocation(data["resource"].geoLocations.geoLocation[i]));
				}
			} else {
				this.geoLocations.push(new GeoLocation(data["resource"].geoLocations.geoLocation));
			}
		}
		
		// FundingReferences
		this.fundingReferences = [];
		if(data["resource"].fundingReferences != undefined){
			if(data["resource"].fundingReferences.fundingReference instanceof Array){
				for(var i = 0; i < data["resource"].fundingReferences.fundingReference.length; i++){
					this.fundingReferences.push(new FundingReference(data["resource"].fundingReferences.fundingReference[i]));
				}
			} else {
				this.fundingReferences.push(new FundingReference(data["resource"].fundingReferences.fundingReference));
			}
		}
	}
	
	getIdentifier(){
		return this.identifier;
	}
	getCreators(){
		return this.creators;
	}
	getTitles(){
		return this.titles;
	}
	getPublisher(){
		return this.publisher;
	}
	getPublicationYear(){
		return this.publicationYear;
	}
	getResourceType(){
		return this.resourceType;
	}
	getSubjects(){
		return this.subjects;
	}
	getContributors(){
		return this.contributors;
	}
	getDates(){
		return this.dates;
	}
	getLanguage(){
		return this.language;
	}
	getAlternateIdentifiers(){
		return this.alternateIdentifiers;
	}
	getRelatedIdentifiers(){
		return this.relatedIdentifiers;
	}
	getSizes(){
		return this.sizes;
	}
	getFormats(){
		return this.formats;
	}
	getVersion(){
		return this.version;
	}
	getRightsList(){
		return this.rightsList;
	}
	getDescriptions(){
		return this.descriptions;
	}
	getGeoLocations(){
		return this.geoLocations;
	}
	getFundingReferences(){
		return this.fundingReferences;
	}
	
	// --- Setters --- \\
	
	setIdentifier(id){
		this.identifier = new Identifier(id);
	}
	setCreators(creator){
		this.creators = [];
		if (creator instanceof Array) {
			for(var i = 0; i < creator.length; i++){
				this.creators.push(new Creator(creator[i]));
			}
		} else {
			this.creators.push(new Creator(creator));
		}
	}
	setTitles(title){
		this.titles = [];
		if(title instanceof Array){
			for(var i = 0; i < title.length; i++){
				this.titles.push(new Title(title[i]));
			}
		} else {
			this.titles.push(new Title(title));
		}
	}
	setPublisher(publisher){
		this.publisher = publisher;
	}
	setPublicationYear(publicationYear){
		this.publicationYear = publicationYear;
	}
	setResourceType(resourceType){
		this.resourceType = new ResourceType(resourceType);
	}
	setSubjects(subject){
		this.subjects = [];
		if(subject instanceof Array){
			for(var i = 0; i < subject.length; i++){
				this.subjects.push(new Subject(subject[i]));
			}
		} else {
			this.subjects.push(new Subject(subject));
		}
	}
	setContributors(contrib){
		this.contributors = [];
		if(contrib instanceof Array){
			for(var i = 0; i < contrib.length; i++){
				this.contributors.push(new Contributor(contrib[i]));
			}
		} else {
			this.contributors.push(new Contributor(contrib));
		}
	}
	setDates(date){
		this.dates = [];
		if(date instanceof Array){
			for(var i = 0; i < date.length; i++){
				this.dates.push(new Date(date[i]));
			}
		} else {
			this.dates.push(new Date(date));
		}
	}
	setLanguage(lang){
		this.language = lang;
	}
	setAlternateIdentifiers(altID){
		this.alternateIdentifiers = [];
		if(altID instanceof Array){
			for(var i = 0; i < altID.length; i++){
				this.alternateIdentifiers.push(new AlternateIdentifier(altID[i]));
			}
		} else {
			this.alternateIdentifiers.push(new AlternateIdentifier(altID));
		}
	}
	setRelatedIdentifiers(relID){
		this.relatedIdentifiers = [];
		if(relID instanceof Array){
			for(var i = 0; i < relID.length; i++){
				this.relatedIdentifiers.push(new RelatedIdentifier(relID[i]));
			}
		} else {
			this.relatedIdentifiers.push(new RelatedIdentifier(relID));
		}
	}
	setSizes(size){
		this.sizes = [];
		if(size instanceof Array){
			for(var i = 0; i < size.length; i++){
				this.sizes.push(size[i]);
			}
		} else {
			this.sizes.push(size);
		}
	}
	setFormats(format){
		this.formats = [];
		if(format instanceof Array){
			for(var i = 0; i < format.length; i++){
				this.formats.push(format[i]);
			}
		} else {
			this.formats.push(format);
		}
	}
	setVersion(ver){
		this.version = ver;
	}
	setRightsList(rights){
		this.rightsList = [];
		if(rights instanceof Array){
			for(var i = 0; i < rights.length; i++){
				this.rightsList.push(new Rights(rights[i]));
			}
		} else {
			this.rightsList.push(new Rights(rights));
		}
	}
	setDescriptions(desc){
		this.descriptions = [];
		if(desc instanceof Array){
			for(var i = 0; i < desc.length; i++){
				this.descriptions.push(new Description(desc[i]));
			}
		} else {
			this.descriptions.push(new Description(desc));
		}
	}
	setGeoLocations(geoLoc){
		this.geoLocations = [];
		if(geoLoc instanceof Array){
			for(var i = 0; i < geoLoc.length; i++){
				this.geoLocations.push(new GeoLocation(geoLoc[i]));
			}
		} else {
			this.geoLocations.push(new GeoLocation(geoLoc));
		}
	}
	setFundingReferences(fundRef){
		this.fundingReferences = [];
		if(fundRef instanceof Array){
			for(var i = 0; i < fundRef.length; i++){
				this.fundingReferences.push(new FundingReference(fundRef[i]));
			}
		} else {
			this.fundingReferences.push(new FundingReference(fundRef));
		}
	}
	
}



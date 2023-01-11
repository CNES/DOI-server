import { Affiliation } from "./affiliation.model";
import { NameIdentifier } from "../xml/nameIdentifier.model";
import { XmlService } from "src/app/services/xml.service";
import { ContributorTypesEnum } from "../enum/contributorTypes";
import { NameTypesEnum } from "../enum/nameTypes";

export class Contributor {
    _attr : {
        contributorType: string;
    };
    contributorName: {
        _value : string;
        _attr : {
          nameType?: string;
          _doiLang?: string;
        }
    };
    givenName?: string;
    familyName?: string;
    nameIdentifier?: NameIdentifier[];
    affiliation?: Affiliation[];

    constructor(name: string, type: string, nameType?: string, lang?: string, givenName?: string, familyName?:string, nameIdentifiers?: NameIdentifier[], affiliations?: Affiliation[]) {
        this.contributorName = {
            _value: name,
            _attr: {
                nameType: nameType,
                _doiLang: lang
            }
        },
        this._attr = {
            contributorType: type
        };
        this.givenName = givenName;
        this.familyName = familyName;
        this.nameIdentifier = nameIdentifiers;
        this.affiliation = affiliations;
    }

  /**
   * Get the key value of enum for all types of the object
   */
    getEnumValue() {
        let xmlService: XmlService = new XmlService();
        this._attr.contributorType = xmlService.getEnumKeyByValue(ContributorTypesEnum, this._attr.contributorType);
        if(this.contributorName._attr.nameType) {
            this.contributorName._attr.nameType = xmlService.getEnumKeyByValue(NameTypesEnum, this.contributorName._attr.nameType);
        }
    }
}

export class RelatedItemContributor {
    _attr: {
        contributorType: string;
    };
    contributorName: {
        _value: string;
        _attr: {
          nameType?: string;
          _doiLang?: string;
        }
    };
    givenName?: string;
    familyName?: string;

    constructor(name: string, type: string, nameType?: string, lang?: string, givenName?: string, familyName?:string) {
        this.contributorName = {
            _value: name,
            _attr: {
                nameType: nameType,
                _doiLang: lang
            }
        },
        this._attr = {
            contributorType: type
        };
        this.givenName = givenName;
        this.familyName = familyName;
    }

    /**
    * Get the key value of enum for all types of the object
    */
    getEnumValue() {
        let xmlService: XmlService = new XmlService();
        this._attr.contributorType = xmlService.getEnumKeyByValue(ContributorTypesEnum, this._attr.contributorType);
        if(this.contributorName._attr.nameType) {
            this.contributorName._attr.nameType = xmlService.getEnumKeyByValue(NameTypesEnum, this.contributorName._attr.nameType);
        }
    }
}
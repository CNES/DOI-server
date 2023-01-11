import { Affiliation } from "./affiliation.model";
import { NameIdentifier } from "../xml/nameIdentifier.model";
import { XmlService } from "src/app/services/xml.service";
import { NameTypesEnum } from "../enum/nameTypes";

export class Creator {
  creatorName: {
    _value: string;
    _attr: {
      nameType?: string;
      _doiLang?: string;
    }
  }
  givenName?: string;
  familyName?: string;
  nameIdentifier?: NameIdentifier[];
  affiliation?: Affiliation[];

  constructor(name: string, nameType?: string, langue?: string, givenName?:string, familyName?: string, nameIdentifiers?: NameIdentifier[], affiliations?: Affiliation[]) {
    this.creatorName = {
      _value: name,
      _attr: {
        nameType: nameType,
        _doiLang: langue
      }
    },
    this.givenName = givenName;
    this.familyName = familyName;
    this.nameIdentifier = nameIdentifiers;
    this.affiliation = affiliations
  }

  /**
   * Get the key value of enum for all types of the object
   */
  getEnumValue() {
    let xmlService: XmlService = new XmlService();
    if (this.creatorName._attr.nameType) {
      this.creatorName._attr.nameType = xmlService.getEnumKeyByValue(NameTypesEnum, this.creatorName._attr.nameType);
    }
  }
}

export class RelatedItemCreator {
  creatorName: {
    _value: string;
    _attr: {
      nameType?: string; 
      _doiLang?: string;
    }
  };
  givenName?: string;
  familyName?: string;

  constructor(name: string, nameType?: string, langue?: string, givenName?:string, familyName?: string) {
    this.creatorName = {
      _value: name,
      _attr: {
        nameType: nameType,
        _doiLang: langue
      }
    },
    this.givenName = givenName;
    this.familyName = familyName;
  }

  /**
   * Get the key value of enum for all types of the object
   */
  getEnumValue() {
    let xmlService: XmlService = new XmlService();
    if (this.creatorName._attr.nameType) {
      this.creatorName._attr.nameType = xmlService.getEnumKeyByValue(NameTypesEnum, this.creatorName._attr.nameType);
    }
  }
}
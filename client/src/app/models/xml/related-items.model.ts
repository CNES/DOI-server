import { XmlService } from "src/app/services/xml.service";
import { NumberTypesEnum } from "../enum/numberTypes";
import { RelationTypesEnum } from "../enum/relationTypes";
import { RelatedItemContributor } from "./contributor.model";
import { RelatedItemCreator } from "./creator.model";
import { Title } from "./title.model";

export class RelatedItemIdentifier {
    _value: string;
    _attr : {
        relatedItemIdentifierType?: string;
        relatedMetadataScheme?: string;
        schemeURI?: string;
        schemeType?: string;
    };

    constructor(relItemidentifier: string, relItemIdentifierType?: string, relMetadataScheme?: string, relItemSchemeURI?: string, relItemSchemeType?: string) {
        this._value = relItemidentifier;
        this._attr = {
            relatedItemIdentifierType:relItemIdentifierType,
            relatedMetadataScheme: relMetadataScheme,
            schemeURI: relItemSchemeURI,
            schemeType: relItemSchemeType
        };
    }
}

export class RelatedItemNumber {
    _value: string;
    _attr:  {
        numberType: string;
    };

    constructor(num: string, type: string) {
        this._value = num;
        this._attr = {
            numberType: type
        };
    }

      /**
   * Get the key value of enum for all types of the object
   */
    getEnumValue() {
        let xmlService: XmlService = new XmlService();
        if (this._attr.numberType) {
        this._attr.numberType = xmlService.getEnumKeyByValue(NumberTypesEnum, this._attr.numberType);
        }
    }
}

export class RelatedItem {
    relatedItemIdentifier: RelatedItemIdentifier;
    creators: {
        creator?: RelatedItemCreator[];
    };
    titles: {
        title?: Title[]
    };
    publicationYear?: string;
    volume?: string;
    issue?: string;
    number?: RelatedItemNumber;
    firstPage?: string;
    lastPage?: string;
    publisher?: string;
    edition?: string;
    contributors: {
        contributor?: RelatedItemContributor[];
    };
    _attr: {
        relatedItemType: string,
        relationType: string
    };

    constructor(relItemIdentifier: RelatedItemIdentifier, relatedItemType: string, relationType: string, creators?: RelatedItemCreator[], titles?: Title[], pubYear?: string, volume?: string, issue?: string, number?: RelatedItemNumber,
        firstPage?: string, lastPage?: string, publisher?: string, edition?: string, contributors?: RelatedItemContributor[]) {
        this.relatedItemIdentifier = relItemIdentifier;
        this.creators = {
            creator: creators
        };
        this.titles = {
            title: titles
        };
        this.publicationYear = pubYear;
        this.volume = volume;
        this.issue = issue;
        this.number = number;
        this.firstPage = firstPage;
        this.lastPage = lastPage;
        this.publisher = publisher;
        this.edition = edition;
        this.contributors = {
            contributor:  contributors
        };
        this._attr = {
            relatedItemType: relatedItemType,
            relationType: relationType
        };
    }

  /**
   * Get the key value of enum for all types of the object
   */
   getEnumValue() {
    let xmlService: XmlService = new XmlService();
    if (this._attr.relationType) {
      this._attr.relationType = xmlService.getEnumKeyByValue(RelationTypesEnum, this._attr.relationType);
    }

  }
}
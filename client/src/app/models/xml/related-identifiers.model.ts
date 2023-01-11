import { XmlService } from "src/app/services/xml.service";
import { RelatedIdentifierTypesEnum } from "../enum/relatedIdentifierTypes";
import { RelationTypesEnum } from "../enum/relationTypes";
import { ResourceTypesEnum } from "../enum/resourceType";

export class RelatedIdentifier {
    _value: string;
    _attr :{
        resourceTypeGeneral: string;
        relatedIdentifierType: string;
        relationType: string;
        relatedMetadataScheme: string;
        schemeURI: string;
        schemeType: string;
    }

    constructor(relIdentifier: string, typeGeneral: string, identifierType: string, relationType: string, relatedMetadataScheme: string, schemeURI: string, schemeType: string) {
        this._value = relIdentifier;
        this._attr = {
            resourceTypeGeneral: typeGeneral,
            relatedIdentifierType: identifierType,
            relationType: relationType,
            relatedMetadataScheme: relatedMetadataScheme,
            schemeURI: schemeURI,
            schemeType: schemeType
        };
    }

    /**
    * Get the key value of enum for all types of the object
    */
     getKeyEnumValue() {
        let xmlService: XmlService = new XmlService();
        if(this._attr.resourceTypeGeneral) {
            this._attr.resourceTypeGeneral = xmlService.getEnumKeyByValue(ResourceTypesEnum, this._attr.resourceTypeGeneral);
        }
        this._attr.relatedIdentifierType = xmlService.getEnumKeyByValue(RelatedIdentifierTypesEnum, this._attr.relatedIdentifierType);
        this._attr.relationType = xmlService.getEnumKeyByValue(RelationTypesEnum, this._attr.relationType);

    }
}
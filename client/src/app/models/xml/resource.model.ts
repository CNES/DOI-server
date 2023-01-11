import { XmlService } from "src/app/services/xml.service";
import { ResourceTypesEnum } from "../enum/resourceType";

export class ResourceDoi {
  _value: string;
  _attr : {
      resourceTypeGeneral: string
  };

  constructor(resource: string, typeGeneral: string) {
    this._value = resource;
    this._attr = {
      resourceTypeGeneral: typeGeneral
    };
  }

  /**
   * Get the key value of enum for all types of the object
   */
   getEnumValue() {
    let xmlService: XmlService = new XmlService();
      this._attr.resourceTypeGeneral = xmlService.getEnumKeyByValue(ResourceTypesEnum, this._attr.resourceTypeGeneral);
  }
}
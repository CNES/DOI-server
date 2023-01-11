import { XmlService } from "src/app/services/xml.service";
import { DescriptionTypesEnum } from "../enum/descriptionTypes";

export class DescriptionDoi {
  _value: string;
  _attr : {
    descriptionType: string;
    _doiLang?: string;
  }

  constructor(description: string, type: string, lang?: string) {
    this._value = description;
    this._attr = {
      descriptionType: type,
      _doiLang: lang
    }
  }

  /**
   * Get the key value of enum for all types of the object
   */
  getEnumValue() {
    let xmlService: XmlService = new XmlService();
    this._attr.descriptionType = xmlService.getEnumKeyByValue(DescriptionTypesEnum, this._attr.descriptionType);
  }
}
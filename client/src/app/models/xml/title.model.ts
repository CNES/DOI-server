import { XmlService } from "src/app/services/xml.service";
import { TitleTypesEnum } from "../enum/titleTypes";

export class Title {
  _value: string;
  _attr: {
    titleType?: string;
    _doiLang?: string;
  };

  constructor(title: string, titleType?: string, titleLang?: string) {
    this._value = title,
    this._attr = {
      titleType: titleType,
      _doiLang: titleLang
    }
  }

  
  /**
   * Get the key value of enum for all types of the object
   */
  getEnumValue() {
    let xmlService: XmlService = new XmlService();
    if (this._attr.titleType) {
      this._attr.titleType = xmlService.getEnumKeyByValue(TitleTypesEnum, this._attr.titleType);
    }
  }
}
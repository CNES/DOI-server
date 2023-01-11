import { XmlService } from "src/app/services/xml.service";
import { DateTypesEnum } from "../enum/dateTypes";

export class DateDoi {
  _value: string;
  _attr: {
    dateType: string;
    dateInformation?: string;
  }

  constructor(date: string, dateType: string, dateInformation?: string) {
    this._value = date;
    this._attr = {
      dateType: dateType,
      dateInformation: dateInformation
    }
  }

  /**
   * Get the key value of enum for all types of the object
   */
  getEnumValue() {
    let xmlService: XmlService = new XmlService();
    this._attr.dateType = xmlService.getEnumKeyByValue(DateTypesEnum, this._attr.dateType);
  }  
}
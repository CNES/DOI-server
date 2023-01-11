export class AltIdentifier {
  _value: string;
  _attr: {
    alternateIdentifierType: string;
  }

  constructor(name: string, identifierType: string) {
    this._value = name;
    this._attr = {
      alternateIdentifierType: identifierType
    };
  }
}
export class NameIdentifier {
  _value : string;
  _attr : {
    nameIdentifierScheme: string,
    schemeURI?: string
  }

  constructor (name: string, nameIdentifierScheme: string, schemeURI?: string) {
    this._value = name;
    this._attr = {
      nameIdentifierScheme: nameIdentifierScheme,
      schemeURI: schemeURI
    }
  }
}
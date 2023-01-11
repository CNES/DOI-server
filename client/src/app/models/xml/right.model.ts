export class Right {
  _value: string;
  _attr : {
    rightsURI?: string;
    rightsIdentifier?: string;
    rightsIdentifierScheme?: string;
    schemeURI?: string;
    _doiLang?: string;
  };

  constructor(name: string, URI: string, identifier?: string, idScheme?: string, schemeURI?: string, lang?: string) {
    this._value = name;
    this._attr = {
      rightsURI: URI,
      rightsIdentifier: identifier,
      rightsIdentifierScheme: idScheme,
      schemeURI: schemeURI,
      _doiLang: lang
    };
  }
}
export class Affiliation {
  _value: string;
  _attr: {
    affiliationIdentifier?: string,
    affiliationIdentifierScheme?: string,
    schemeURI?: string
  }

  constructor(affiliation: string, identifier?: string, identifierScheme?: string, schemeURI?: string) {
    this._value = affiliation;
    this._attr = {
      affiliationIdentifier: identifier,
      affiliationIdentifierScheme: identifierScheme,
      schemeURI: schemeURI
    }
  }
}
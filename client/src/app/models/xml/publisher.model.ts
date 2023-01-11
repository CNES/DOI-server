export class Publisher {
  _value: string;
  _attr: {
    _doiLang?: string
  };

  constructor(pub: string, lang?: string) {
    this._value = pub;
    this._attr = {
      _doiLang: lang
    };
  }
}
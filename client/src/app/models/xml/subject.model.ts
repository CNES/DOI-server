export class SubjectDoi {
  _value: string;
  _attr : {
    subjectScheme: string;
    schemeURI: string;
    valueURI: string;
    classificationCode: string;
    _doiLang: string
  }

  constructor(subject : string, subjectScheme: string, subjectSchemeURI: string, subjectValueURI: string, subjectClassificationCode: string, subjectlang: string) {
      this._value = subject;
      this._attr = {
        subjectScheme: subjectScheme,
        schemeURI: subjectSchemeURI,
        valueURI: subjectValueURI,
        classificationCode: subjectClassificationCode,
        _doiLang: subjectlang
      };
  }
}

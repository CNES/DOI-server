export interface FundingRefIdentifier {
  _value? : string;
  _attr : {
    funderIdentifierType?: string;
    schemeURI?: string;
  }
}

export class FundingRef {
  funderName: string;
  awardTitle?: string;
  awardNumber: {
    _value?: string;
    _attr : {
      awardURI?: string;
    }
  };
  funderIdentifier: FundingRefIdentifier

  constructor(name: string, awardTitle?: string, awardNumber?: string, awardURI?: string, funderRefIdentifier?: string, funderRefIdentifierType?: string, funderRefSchemeURI?: string) {
    this.funderName = name;
    this.awardTitle = awardTitle;
    this.awardNumber = {
      _value: awardNumber,
      _attr: {
        awardURI: awardURI
      }
    };
    this.funderIdentifier = {
      _value: funderRefIdentifier,
      _attr: {
        funderIdentifierType: funderRefIdentifierType,
        schemeURI: funderRefSchemeURI
      }
    };
  }
}
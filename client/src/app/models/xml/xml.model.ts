import { XmlService } from "src/app/services/xml.service";
import { ContributorTypesEnum } from "../enum/contributorTypes";
import { DateTypesEnum } from "../enum/dateTypes";
import { DescriptionTypesEnum } from "../enum/descriptionTypes";
import { NameTypesEnum } from "../enum/nameTypes";
import { NumberTypesEnum } from "../enum/numberTypes";
import { RelatedIdentifierTypesEnum } from "../enum/relatedIdentifierTypes";
import { RelationTypesEnum } from "../enum/relationTypes";
import { ResourceTypesEnum } from "../enum/resourceType";
import { TitleTypesEnum } from "../enum/titleTypes";
import { AltIdentifier } from "./alt-identifier.model";
import { Contributor } from "./contributor.model";
import { Creator } from "./creator.model";
import { DateDoi } from "./date.model";
import { DescriptionDoi } from "./description.model";
import { FundingRef } from "./fundingRef.model";
import { GeoLocation } from "./geo-location.model";
import { Publisher } from "./publisher.model";
import { RelatedIdentifier } from "./related-identifiers.model";
import { RelatedItem } from "./related-items.model";
import { ResourceDoi } from "./resource.model";
import { Right } from "./right.model";
import { SubjectDoi } from "./subject.model";
import { Title } from "./title.model";

export class XmlDoi {
    resource : {
         // Mandatory
        _attr : {
            "xmlns:xsi": string
            "xmlns": string
            "xsi:schemaLocation": string
        },
        identifier: {
            _value: string | undefined,
            _attr: {
                identifierType: string 
            }
        },
        creators: {
            creator: Creator[] | undefined
        },
        titles: {
            title: Title[] | undefined
        },
        publisher: Publisher | undefined,
        publicationYear: {
            _value: number | undefined
        },
        resourceType: ResourceDoi | undefined,
        // Optionnal
        subjects: {
            subject: SubjectDoi[] | undefined
        },
        contributors: {
            contributor: Contributor[] | undefined
        },
        dates: {
            date: DateDoi[] | undefined
        },
        language: string | undefined
        alternateIdentifiers: {
            alternateIdentifier: AltIdentifier[] | undefined
        },
        relatedIdentifiers: {
            relatedIdentifier: RelatedIdentifier[] | undefined
        },
        sizes: {
            size: string[] | undefined
        },
        formats: {
            format: string[] | undefined
        },
        version: string | undefined

        rightsList: {
            rights: Right[] | undefined
        }
        descriptions: {
            description: DescriptionDoi[] | undefined
        },
        geoLocations: {
            geoLocation: GeoLocation[] | undefined
        },
        fundingReferences: {
            fundingReference: FundingRef[] | undefined
        },
        relatedItems: {
            relatedItem: RelatedItem[] | undefined
        }
    }

    constructor(identifier?: string, creators?: Creator[], titles?: Title[], pub?: Publisher, publicationYear?: number, resDoi?: ResourceDoi, subjects?: SubjectDoi[], contributors?: Contributor[], dates?: DateDoi[], sizes?: string[], formats?: string[], version?: string, rights?: Right[], fundingRefs?: FundingRef[], descriptions?: DescriptionDoi[], geoLocations?: GeoLocation[], language?: string, altIdentifiers?: AltIdentifier[], relIdentifiers?: RelatedIdentifier[], relItems?: RelatedItem[], resource?: any) {
        this.resource = {
            // Mandatory
            _attr: {
                "xmlns:xsi" : resource.xsi,
                "xmlns": resource.xmlns,
                "xsi:schemaLocation": resource.schemaLocation
            },
            identifier: {
                _attr: {
                    identifierType: "DOI"
                },
                _value: identifier
            },
            creators: {
                creator: creators
            },
            titles: {
                title: titles
            },
            publisher: pub,
            publicationYear: {
                _value : publicationYear
            },
            resourceType: resDoi,
            // Optionnal
            subjects: {
                subject: subjects
            },
            contributors: {
                contributor: contributors
            },
            dates: {
                date: dates
            },
            language: language,
            alternateIdentifiers: {
                alternateIdentifier: altIdentifiers
            },
            relatedIdentifiers: {
                relatedIdentifier: relIdentifiers
            },
            sizes: {
                size: sizes
            },
            formats: {
                format: formats
            },
            version: version,
            rightsList: {
                rights: rights
            },
            descriptions: {
                description: descriptions
            },
            geoLocations: {
                geoLocation: geoLocations
            },
            fundingReferences: {
                fundingReference: fundingRefs
            },
            relatedItems: {
                relatedItem: relItems
            }
        }

        // Change value to key enum
        let xmlService: XmlService = new XmlService();
        this.resource.titles.title?.forEach(title => {
            if (title._attr.titleType) {
                title._attr.titleType = xmlService.getEnumKeyByValue(TitleTypesEnum, title._attr.titleType);
              }
        });

        this.resource.creators.creator?.forEach(creator => {
            if (creator.creatorName._attr.nameType) {
                creator.creatorName._attr.nameType = xmlService.getEnumKeyByValue(NameTypesEnum, creator.creatorName._attr.nameType);
            }
        });

        this.resource.dates.date?.forEach(date => {
            date._attr.dateType = xmlService.getEnumKeyByValue(DateTypesEnum, date._attr.dateType);
        });

        this.resource.descriptions.description?.forEach(descr => {
            descr._attr.descriptionType = xmlService.getEnumKeyByValue(DescriptionTypesEnum, descr._attr.descriptionType);
        });

        this.resource.contributors.contributor?.forEach(contr => {
            contr._attr.contributorType = xmlService.getEnumKeyByValue(ContributorTypesEnum, contr._attr.contributorType);
            if(contr.contributorName._attr.nameType) {
                contr.contributorName._attr.nameType = xmlService.getEnumKeyByValue(NameTypesEnum, contr.contributorName._attr.nameType);
            }
        });

        this.resource.relatedIdentifiers.relatedIdentifier?.forEach(relIdent => {
            if(relIdent._attr.resourceTypeGeneral) {
                relIdent._attr.resourceTypeGeneral = xmlService.getEnumKeyByValue(ResourceTypesEnum, relIdent._attr.resourceTypeGeneral);
            }
            relIdent._attr.relatedIdentifierType = xmlService.getEnumKeyByValue(RelatedIdentifierTypesEnum, relIdent._attr.relatedIdentifierType);
            relIdent._attr.relationType = xmlService.getEnumKeyByValue(RelationTypesEnum, relIdent._attr.relationType);
        });

        this.resource.relatedItems.relatedItem?.forEach(relatedItem => {
            if (relatedItem._attr.relationType) {
                relatedItem._attr.relationType = xmlService.getEnumKeyByValue(RelationTypesEnum, relatedItem._attr.relationType);
            }
            if (relatedItem._attr.relatedItemType) {
                relatedItem._attr.relatedItemType = xmlService.getEnumKeyByValue(ResourceTypesEnum, relatedItem._attr.relatedItemType);
            }
            if (relatedItem.titles && relatedItem.titles.title) {
                relatedItem.titles.title.forEach( titl => {
                    if (titl._attr.titleType) {
                        titl._attr.titleType = xmlService.getEnumKeyByValue(TitleTypesEnum, titl._attr.titleType);
                    }
                });
            }
            if (relatedItem.creators && relatedItem.creators.creator) {
                relatedItem.creators.creator.forEach( creat => {
                    if (creat.creatorName._attr.nameType) {
                        creat.creatorName._attr.nameType = xmlService.getEnumKeyByValue(NameTypesEnum, creat.creatorName._attr.nameType);
                      }
                });
            }
            if (relatedItem.contributors && relatedItem.contributors.contributor) {
                relatedItem.contributors.contributor.forEach( contr => {
                    contr._attr.contributorType = xmlService.getEnumKeyByValue(ContributorTypesEnum, contr._attr.contributorType);
                    if(contr.contributorName._attr.nameType) {
                        contr.contributorName._attr.nameType = xmlService.getEnumKeyByValue(NameTypesEnum, contr.contributorName._attr.nameType);
                    }
                });
            }
            if (relatedItem.number && relatedItem.number._attr.numberType) {
                relatedItem.number._attr.numberType = xmlService.getEnumKeyByValue(NumberTypesEnum, relatedItem.number._attr.numberType);
            }
        });
    }
}
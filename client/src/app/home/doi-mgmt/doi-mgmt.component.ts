import { Component, OnInit, ViewChild } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import {
  TitleTypesEnum, NameTypesEnum, ResourceTypesEnum, ContributorTypesEnum, DateTypesEnum, RelatedIdentifierTypesEnum,
  RelationTypesEnum, DescriptionTypesEnum, FunderIdentifierTypesEnum, NumberTypesEnum,
  XmlDoi, Title, Creator, Affiliation, NameIdentifier, SubjectDoi, DateDoi, DescriptionDoi,
  Contributor, RelatedIdentifier, Publisher, ResourceDoi, Right, FundingRef, AltIdentifier,
  GeoLocation, GeoLocationBox, GeoLocationPoint, GeoLocationPolygon,
  RelatedItem, RelatedItemIdentifier, RelatedItemCreator, RelatedItemContributor, RelatedItemNumber
} from "../../models";
import { MatTable } from '@angular/material/table';
import { STEPPER_GLOBAL_OPTIONS } from '@angular/cdk/stepper';
import { SelectionModel } from '@angular/cdk/collections';
import { ActivatedRoute } from '@angular/router';
import * as xmlParser from 'fast-xml-parser';
import { CookieService } from 'ngx-cookie-service';
import ResourcesJson from '../../../assets/resources.json';
import { AlertService } from 'src/app/services/alert.service';
import { CitationService } from 'src/app/services/citation.service';
import { XmlService } from 'src/app/services/xml.service';
import { DoiService } from 'src/app/services/doi.service';
import { AdminService } from 'src/app/services/admin.service';
import { ProjectService } from 'src/app/services/project.service';

enum Action {
  create = "create",
  edit = "edit",
  duplicate = "duplicate",
  exportDataCiteCreate = "exportCreate",
  exportDataCiteEdit = "exportEdit",
  exportDataCiteDuplicate = "exportDuplicate"
}

@Component({
  selector: 'app-doi-mgmt',
  templateUrl: './doi-mgmt.component.html',
  styleUrls: ['./doi-mgmt.component.css'],
  providers: [{
    provide: STEPPER_GLOBAL_OPTIONS, useValue: { displayDefaultIndicatorType: false, showError: true }
  }]
})

export class DOIManagementComponent implements OnInit {

  // Regex for input fiels validation
  private uriRegex = "^http(s)?:\/\/[\-a-zA-Z0-9_&#=+*,;:?!^$@%\\\/\(\)\'\.\~]+([\-a-zA-Z0-9_&#=+*,;:?!^$@%\\\/\(\)\'\~]+)$";
  private longitudeRegex = '(\\+|-)?((\\d((\\.)|\\.\\d{1,6})?)|(0*?\\d\\d((\\.)|\\.\\d{1,6})?)|(0*?1[0-7]\\d((\\.)|\\.\\d{1,6})?)|(0*?180((\\.)|\\.0{1,6})?))';
  private latitudeRegex = '(\\+|-)?((\\d((\\.)|\\.\\d{1,6})?)|(0*?[0-8]\\d((\\.)|\\.\\d{1,6})?)|(0*?90((\\.)|\\.0{1,6})?))';
  private yearRegex = '[0-9]{4}';
  private dateRegex = '(([12][\\d]{3}-(0[1-9]|1[012])-(0[1-9]|[12]\\d|3[01])T(0[0-9]|1[0-9]|2[0-3])(:[0-5][0-9]){1,2}(.\\d{1,3}){0,1}(Z|[-+](0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]))|([12][\\d]{3}-(0[1-9]|1[012])-(0[1-9]|[12]\\d|3[01]))|([12][\\d]{3}-(0[1-9]|1[012]))|([12]\\d{3}))';

  // Boolean to expand forms
  // True for the required field
  public isOpenFormTitle: boolean = true;
  public isOpenFormCreator: boolean = true;
  // False for the optionals
  public isOpenFormSubject: boolean = false;
  public isOpenFormDate: boolean = false;
  public isOpenFormDescription: boolean = false;
  public isOpenFormContributor: boolean = false;
  public isOpenFormRelatedIdentifier: boolean = false;
  public isOpenFormGeoLocation: boolean = false;
  public isOpenFormAlternateIdentifier: boolean = false;
  public isOpenFormSize: boolean = false;
  public isOpenFormFormat: boolean = false;
  public isOpenFormRight: boolean = false;
  public isOpenFormFundingReference: boolean = false;
  public isOpenFormRelatedItem: boolean = false;

  // projects
  public projects: string[];

  // dois
  public existingDois: any[];
  public isAuthorizedDoiId: boolean;
  public alreadySubmit: boolean;

  //  Title mat card dynamic (create, edit, duplicate)
  public matCardTitle: string = "";
  public actionButton: string = "";
  public importActivate: boolean = false;

  // Form groups
  firstFormGroup!: FormGroup;
  firstFormGroupMultiTitle!: FormGroup;
  secondFormGroup!: FormGroup;
  secondFormGroupMultiCreator!: FormGroup;
  thirdFormGroup!: FormGroup;
  fourthFormGroup!: FormGroup;
  fourthFormGroupSubject!: FormGroup;
  fourthFormGroupDate!: FormGroup;
  fourthFormGroupDescription!: FormGroup;
  fifthFormGroup!: FormGroup;
  fifthFormGroupMultiContributor!: FormGroup;
  sixthFormGroup!: FormGroup;
  sixthFormGroupMultiRelIdentifier!: FormGroup;
  seventhFormGroup!: FormGroup;
  eighthFormGroup!: FormGroup;
  eighthFormGroupAltId!: FormGroup;
  ninthFormGroup!: FormGroup;
  ninthFormGroupRight!: FormGroup;
  ninthFormGroupFundRef!: FormGroup;
  tenthFormGroup!: FormGroup;
  tenthFormGroupMultiRelItems!: FormGroup;
  endFormGroup!: FormGroup;

  // The differents languages
  public languages: Array<string> = new Array();

  // enum
  public titleTypes = Object.values(TitleTypesEnum);
  public nameTypes = Object.values(NameTypesEnum);
  public resourceTypes = Object.values(ResourceTypesEnum);
  public contributorTypes = Object.values(ContributorTypesEnum);
  public dateTypes = Object.values(DateTypesEnum);
  public relatedIdentifierTypes = Object.values(RelatedIdentifierTypesEnum);
  public relationTypes = Object.values(RelationTypesEnum);
  public descriptionTypes = Object.values(DescriptionTypesEnum);
  public funderIdentifierTypes = Object.values(FunderIdentifierTypesEnum);
  public numberTypes = Object.values(NumberTypesEnum);

  // TITLES
  // List of added titles
  public titles!: Title[];
  // The selected title from html for keep it selected
  public selectionTitle = new SelectionModel<Title>(false, []);

  // CREATORS
  // List of added creators
  public creators!: Creator[];
  // The selected creator from html for keep it selected
  public selectionCreator = new SelectionModel<Creator>(false, []);
  // Number of name identifier
  public nbCreatorNameIdentifiers: number = 0;
  // Number of affilication
  public nbCreatorAffiliations: number = 0;

  // SUBJECTS
  // List of added subjects
  public subjects!: SubjectDoi[];
  // The selected subject from html for keep it selected
  public selectionSubject = new SelectionModel<SubjectDoi>(false, []);

  // DATES
  // List of added dates
  public dates!: DateDoi[];
  // The selected date from html for keep it selected
  public selectionDate = new SelectionModel<DateDoi>(false, []);

  // DESCRIPTIONS
  // List of added descriptions
  public descriptions!: DescriptionDoi[];
  // The selected desription from html for keep it selected
  public selectionDescription = new SelectionModel<DescriptionDoi>(false, []);

  // CONTRIBUTORS
  // List of added contributors
  public contributors!: Contributor[];
  // The selected contributor from html for keep it selected
  public selectionContributor = new SelectionModel<Contributor>(false, []);
  // Number of name identifier
  public nbContributorNameIdentifiers: number = 0;
  // Number of affilication
  public nbContributorAffiliations: number = 0;

  // RELATED IDENTIFIERS
  // List of added related identifiers
  public relIdentifiers!: RelatedIdentifier[];
  // The selected related identifier from html for keep it selected
  public selectionRelIdentifier = new SelectionModel<RelatedIdentifier>(false, []);

  // GEO LOCATIONS
  // List of geo locations
  public geoLocations: GeoLocation[];
  // Number of geo location polygon
  public nbGeoLocationPolygon: number = 0;
  // Number of geo location polygon
  public nbGeoLocationPolygonPoints: [number] = [4];
  // The selected geo location from html for keep it selected
  public selectionGeoLocation = new SelectionModel<GeoLocation>(false, []);

  // ALTERNATIVE IDENTIFIER
  // List of added alternative identifiers
  public altIdentifiers!: AltIdentifier[];
  // The selected alt Identifier from html for keep it selected
  public selectionAltIdentifier = new SelectionModel<AltIdentifier>(false, []);

  // SIZES
  // List of added sizes
  public sizes!: string[];

  // FORMAT
  // List of added formats
  public formats!: string[];

  // RIGHTLIST
  // List of added right list
  public rights!: Right[];
  // The selected right from html for keep it selected
  public selectionRight = new SelectionModel<Right>(false, []);

  // FUNDING REFERENCES
  // List of funding references
  public fundingRefs!: FundingRef[];
  // Number of funding reference
  public nbFundingReferences: number = 1;
  // The selected funding reference from html for keep it selected
  public selectionFundingReference = new SelectionModel<FundingRef>(false, []);

  // RELATED ITEMS
  // List of related items
  public relItems!: RelatedItem[];
  public nbRelatedItemCreators: number = 0;
  public nbRelatedItemTitles: number = 0;
  public nbRelatedItemContributors: number = 0;
  // The selected related Item from html for keep it selected
  public selectionRelItem = new SelectionModel<RelatedItem>(false, []);

  // Content of resource.json
  resourcesJson: any;

  // XML generated
  xmlDoi: string = "";

  // Boolean for dynamicly required or not a form control
  public isCreatorNameIdShemeRequired: boolean[] = new Array();
  public isContributorNameIdShemeRequired: boolean[] = new Array();
  public isFunderIdentifierTypeRequired: boolean = false;
  public isFamilyAndGivenNameRequired: boolean = false;

  // Boolean for show/hide the modify button
  // It only appear wehen user clic on the object on table
  public isModifyTitleButton: boolean = true;
  public isModifyCreatorButton: boolean = true;
  public isModifySubjectButton: boolean = true;
  public isModifyDateButton: boolean = true;
  public isModifyDescriptionButton: boolean = true;
  public isModifyContributorButton: boolean = true;
  public isModifyRelIdentifierButton: boolean = true;
  public isModifyGeoLocationButton: boolean = true;
  public isModifyAltIdentifierButton: boolean = true;
  public isModifyRightButton: boolean = true;
  public isModifyFundingReferenceButton: boolean = true;
  public isModifyRelItemButton: boolean = true;

  // View child for update all tables
  @ViewChild('tableTitles') tableTitles!: MatTable<any>;
  @ViewChild('tableCreators') tableCreators!: MatTable<any>;
  @ViewChild('tableSubjects') tableSubjects!: MatTable<any>;
  @ViewChild('tableDates') tableDates!: MatTable<any>;
  @ViewChild('tableDescriptions') tableDescriptions!: MatTable<any>;
  @ViewChild('tableContributors') tableContributors!: MatTable<any>;
  @ViewChild('tableRelatedIdentifiers') tableRelatedIdentifiers!: MatTable<any>;
  @ViewChild('tableGeoLocations') tableGeoLocations!: MatTable<any>;
  @ViewChild('tableAltIdentifiers') tableAltIdentifiers!: MatTable<any>;
  @ViewChild('tableSizes') tableSizes!: MatTable<any>;
  @ViewChild('tableFormats') tableFormats!: MatTable<any>;
  @ViewChild('tableRights') tableRights!: MatTable<any>;
  @ViewChild('tableFundingRefs') tableFundingRefs!: MatTable<any>;
  @ViewChild('tableRelItems') tableRelItems!: MatTable<any>;

  // Validator for the subElements
  public item = new FormControl('', [Validators.required]);

  /**
   * Constructor
   * @param fb
   * @param projectService
   * @param configService
   * @param _formBuilder
   * @param alertService
   * @param citationService
   */
  constructor(private adminService: AdminService,
    public fb: FormBuilder,
    private projectService: ProjectService,
    private _formBuilder: FormBuilder,
    private alertService: AlertService,
    private citationService: CitationService,
    private xmlService: XmlService,
    private doiService: DoiService,
    private route: ActivatedRoute,
    private cookieService: CookieService) {
    this.existingDois = new Array();
    this.isAuthorizedDoiId = true;
    this.alreadySubmit = false;
    this.geoLocations = new Array();
    this.projects = new Array();
  }

  /**
   * Init
   */
  ngOnInit(): void {
    // Clear alerts
    this.alertService.clear();

    // Get list of existing dois
    this.getAllAvailableDOIsFromProject();

    // Get the user projects
    this.getUserProjects();

    const type = this.route.snapshot.paramMap.get('action');
    if (type != null) {
      switch (type) {
        case Action.create:
          this.matCardTitle = "Création DOI";
          this.actionButton = "Créer le DOI";
          this.importActivate = true;
          break;
        case Action.edit:
          this.matCardTitle = "Modification DOI";
          this.actionButton = "Modifier le DOI";
          this.importActivate = false;
          const doiIdentifier = this.route.snapshot.paramMap.get('doi');
          if (doiIdentifier != null) {
            this.getAndFillMetadataIntoForm(doiIdentifier, Action.edit);
          } else {
            this.alertService.error("Une erreur est survenue : Identifiant DOI inconnu")
          }
          break;
        case Action.duplicate:
          this.matCardTitle = "Dupliquer DOI";
          this.actionButton = "Créer le DOI";
          this.importActivate = false;
          const doiIdentifierSource = this.route.snapshot.paramMap.get('doi');
          if (doiIdentifierSource != null) {
            this.getAndFillMetadataIntoForm(doiIdentifierSource, Action.duplicate);
          } else {
            this.alertService.error("Une erreur est survenue : Identifiant DOI inconnu")
          }
          break;
        default:
          this.alertService.error("Une erreur est survenue : action inconnue");
          break;
      }
    } else {
      this.alertService.error('Une erreur est survenue : action inconnue');
    }

    this.titles = new Array();
    this.creators = new Array();
    this.subjects = new Array();
    this.dates = new Array();
    this.descriptions = new Array();
    this.contributors = new Array();
    this.relIdentifiers = new Array();
    this.altIdentifiers = new Array();
    this.sizes = new Array();
    this.formats = new Array();
    this.rights = new Array();
    this.geoLocations = new Array();
    this.fundingRefs = new Array();
    this.relItems = new Array();

    // Get languages
    this.getLanguages();

    // Init all the forms
    this.initFirstForm();
    this.initFirstMultiTitleForm();

    this.secondFormGroup = this._formBuilder.group({
      // Fake control for display error if not typed
      creatorName: new FormControl('', [Validators.required]),
      // List of creator
      creators: new FormArray([new FormControl(this.creators)], [Validators.required])
    });

    this.initSecondMultiCreatorForm();

    this.thirdFormGroup = this._formBuilder.group({
      publisher: new FormControl('', [Validators.required]),
      publicationYear: new FormControl('', Validators.compose([Validators.required, Validators.pattern(this.yearRegex)])),
      publicationLang: new FormControl(''),
      resourceType: new FormControl('', [Validators.required]),
      resourceTypeGeneral: new FormControl('', [Validators.required])
    });

    this.initFourthMultiSubjectForm();
    this.initFourthMultiDateForm();
    this.initFourthMultiDescriptionForm();

    this.fifthFormGroup = this._formBuilder.group({
      contributors: new FormArray([new FormControl(this.contributors)])
    });

    this.initFifthMultiContributorForm();

    this.sixthFormGroup = this._formBuilder.group({
      relatedIdentifiers: new FormArray([new FormControl(this.relIdentifiers)])
    });

    this.initSixthMultiRelIdentifierForm();
    this.initSeventhMultiGeoLocationForm();

    this.eighthFormGroup = this._formBuilder.group({
      language: new FormControl(''),
      size: new FormControl(''),
      format: new FormControl(''),
      version: new FormControl(''),
      sizes: new FormArray([new FormControl(this.sizes)]),
      formats: new FormArray([new FormControl(this.formats)])
    });

    this.initEighthMultiAltIdForm();

    this.ninthFormGroup = this._formBuilder.group({
    });

    this.initNinthMultiRightForm();
    this.initNinthMultiFundRefForm();

    this.tenthFormGroup = this._formBuilder.group({
      relItems: new FormArray([new FormControl(this.relItems)])
    });

    this.initTenthMultiRelItemForm();

    this.endFormGroup = this.fb.group({
      identifier: new FormControl(''),
    });

    // Set the INIST code configurated in assets.json
    this.firstFormGroup.get('doi_prefix')?.setValue(this.cookieService.get('inistCode'));

    // Set the selected project with the parameter passed from routing
    let project = this.route.snapshot.paramMap.get('project');
    if (project != null) {
      this.firstFormGroup.get('doi_project')?.setValue(project.toString());
    } else {
      this.alertService.error("Une erreur est survenue :  projet inconnu");
    }

    // Update DOI identifier
    this.updateDoiIdentifier();
  }

  /**
  * Init the firstFormGroup
  */
  private initFirstForm() {
    this.firstFormGroup = this.fb.group({
      doi_prefix: new FormControl({ value: '', disabled: true }),
      doi_project: new FormControl({ value: '', disabled: true }),
      doi_identifier: new FormControl('', Validators.compose([Validators.required, this.doiIdentifierValidator()])),
      url: new FormControl('', [Validators.required, Validators.pattern(this.uriRegex)]),
      // Fake control for display error if not typed
      titleName: new FormControl('', [Validators.required]),
      titles: new FormArray([new FormControl(this.titles)], [Validators.required])
    });
  }

  /**
   * Init the firstFormGroupMultiTitle
   */
  private initFirstMultiTitleForm() {
    this.firstFormGroupMultiTitle = this.fb.group({
      titleName: new FormControl('', [Validators.required]),
      titleType: new FormControl(''),
      titleLang: new FormControl('')
    });
  }

  /**
   * Init the secondFormGroupMultiCreator
   */
  private initSecondMultiCreatorForm() {
    this.secondFormGroupMultiCreator = this._formBuilder.group({
      creatorName: new FormControl('', [Validators.required]),
      creatorNameType: new FormControl(''),
      creatorLang: new FormControl(''),
      creatorGivenName: new FormControl(''),
      creatorFamilyName: new FormControl(''),
    });
    this.isCreatorNameIdShemeRequired.push(false);
    this.nbCreatorAffiliations = 0;
    this.nbCreatorNameIdentifiers = 0;
  }

  /**
   * Init the fourthFormGroupSubject
   */
  private initFourthMultiSubjectForm() {
    this.fourthFormGroupSubject = this._formBuilder.group({
      subject: new FormControl(''),
      subjectScheme: new FormControl(''),
      subjectSchemeURI: new FormControl('', [Validators.pattern(this.uriRegex)]),
      subjectValueURI: new FormControl('', [Validators.pattern(this.uriRegex)]),
      subjectClassificationCode: new FormControl(''),
      subjectLang: new FormControl(''),
      subjects: new FormArray([new FormControl(this.subjects)])
    });
  }

  /**
   * Init the fourthFormGroupDate
   */
  private initFourthMultiDateForm() {
    this.fourthFormGroupDate = this._formBuilder.group({
      date: new FormControl('', [Validators.required, Validators.pattern(this.dateRegex)]),
      dateType: new FormControl('', [Validators.required]),
      dateInformation: new FormControl(''),
      dates: new FormArray([new FormControl(this.dates)]),
    });
  }

  /**
   * Init the fourthFormGroupDescription
   */
  private initFourthMultiDescriptionForm() {
    this.fourthFormGroupDescription = this._formBuilder.group({
      description: new FormControl(''),
      descriptionType: new FormControl(''),
      descriptionLang: new FormControl(''),
      descriptions: new FormArray([new FormControl(this.descriptions)])
    });
  }

  /**
   * Init the fifthFormGroupMultiContributor
   */
  private initFifthMultiContributorForm() {
    this.fifthFormGroupMultiContributor = this._formBuilder.group({
      contributorName: new FormControl(''),
      contributorNameType: new FormControl(''),
      contributorLang: new FormControl(''),
      contributorGivenName: new FormControl(''),
      contributorFamilyName: new FormControl(''),
      contributorType: new FormControl(''),
    });
    this.isContributorNameIdShemeRequired.push(false);
    this.nbContributorNameIdentifiers = 0;
    this.nbContributorAffiliations = 0;
  }

  /**
   * Init the sixthFormGroupMultiRelIdentifier
   */
  private initSixthMultiRelIdentifierForm() {
    this.sixthFormGroupMultiRelIdentifier = this._formBuilder.group({
      relatedIdentifier: new FormControl(''),
      relatedResourceTypeGeneral: new FormControl(''),
      relatedIdentifierType: new FormControl(''),
      relatedRelationType: new FormControl(''),
      relatedMetadataScheme: new FormControl(''),
      relatedSchemeURI: new FormControl('', [Validators.pattern(this.uriRegex)]),
      relatedSchemeType: new FormControl('')
    });
  }

  /**
   * Init the seventhFormGroup
   */
  private initSeventhMultiGeoLocationForm() {
    this.seventhFormGroup = this._formBuilder.group({
      geoLocationPlace: new FormControl(''),
      pointLongitude: new FormControl('', [Validators.pattern(this.longitudeRegex)]),
      pointLatitude: new FormControl('', [Validators.pattern(this.latitudeRegex)]),
      northBoundLatitude: new FormControl('', [Validators.pattern(this.latitudeRegex)]),
      southBoundLatitude: new FormControl('', [Validators.pattern(this.latitudeRegex)]),
      westBoundLongitude: new FormControl('', [Validators.pattern(this.longitudeRegex)]),
      eastBoundLongitude: new FormControl('', [Validators.pattern(this.longitudeRegex)]),
      geoLocations: new FormArray([new FormControl(this.geoLocations)])
    });

    this.nbGeoLocationPolygon = 0;
    this.nbGeoLocationPolygonPoints = [4];

  }

  /**
   * Init the eighthFormGroupAltId
   */
  private initEighthMultiAltIdForm() {
    this.eighthFormGroupAltId = this._formBuilder.group({
      alternateIdentifier: new FormControl(''),
      alternateIdentifierType: new FormControl(''),
      altIdentifiers: new FormArray([new FormControl(this.altIdentifiers)])
    });
  }

  /**
   * Init the ninthFormGroupRight
   */
  private initNinthMultiRightForm() {
    this.ninthFormGroupRight = this._formBuilder.group({
      right: new FormControl(''),
      rightURI: new FormControl('', [Validators.pattern(this.uriRegex)]),
      rightIdentifier: new FormControl(''),
      rightIdentifierScheme: new FormControl(''),
      rightSchemeURI: new FormControl('', [Validators.pattern(this.uriRegex)]),
      rightLang: new FormControl(''),
      rights: new FormArray([new FormControl(this.rights)])
    });
  }

  /**
   * Init the ninthFormGroupFundRef
   */
  private initNinthMultiFundRefForm() {
    this.ninthFormGroupFundRef = this._formBuilder.group({
      funderName: new FormControl(''),
      funderIdentifier: new FormControl(''),
      funderIdentifierType: new FormControl(''),
      funderSchemeURI: new FormControl('', [Validators.pattern(this.uriRegex)]),
      funderAwardNumber: new FormControl(''),
      funderAwardURI: new FormControl('', [Validators.pattern(this.uriRegex)]),
      funderAwardTitle: new FormControl(''),
      fundingRefs: new FormArray([new FormControl(this.fundingRefs)])
    });
  }

  /**
   * Init the tenthFormGroupMultiRelItems
   */
  private initTenthMultiRelItemForm() {
    this.tenthFormGroupMultiRelItems = this._formBuilder.group({
      relatedItemIdentifier: new FormControl(''),
      relatedItemIdentifierType: new FormControl(''),
      relatedItemMetadataScheme: new FormControl(''),
      relatedItemSchemeURI: new FormControl('', [Validators.pattern(this.uriRegex)]),
      relatedItemSchemeType: new FormControl(''),
      relatedItemPublicationYear: new FormControl('', [Validators.pattern(this.yearRegex)]),
      relatedItemVolume: new FormControl(''),
      relatedItemIssue: new FormControl(''),
      relatedItemNumber: new FormControl(''),
      relatedItemNumberType: new FormControl(''),
      relatedItemFirstPage: new FormControl(''),
      relatedItemLastPage: new FormControl(''),
      relatedItemPublisher: new FormControl(''),
      relatedItemEdition: new FormControl(''),
      relatedItemType: new FormControl(''),
      relatedItemRelationType: new FormControl('')
    });

    this.nbRelatedItemTitles = 0;
    this.nbRelatedItemCreators = 0;
    this.nbRelatedItemContributors = 0;
  }

  /**
   * Get ans Fill metatdata into form
   * @param doiIndentifier
   * @param action
   */
  private getAndFillMetadataIntoForm(doiIndentifier: string, action?: Action) {
    this.alertService.success("Récupération des metadatas depuis DataCite, veuillez patienter ...");

    this.doiService.getMetadataFromDoi(this.projectService.getSelectedProject().suffix.toString(), doiIndentifier).subscribe(
      data => {
      },
      error => {
        if (error.status == 200) {
          if (action == Action.edit) {
            this.doiService.getUrlFromDoi(this.projectService.getSelectedProject().suffix.toString(), doiIndentifier).subscribe(
              data => {
              },
              error => {
                if (error.status == 200) {
                  this.firstFormGroup.get("url")?.setValue(error.error.text);
                }
              }
            );
          }
          // Response OK, parse the metadatas
          this.importMetadatas(error.error.text, action);
        } else {
          this.alertService.error("Une erreur est survenue lors de la récuperation des metadatas du DOI " + doiIndentifier + " : " + error.statusText);
        }
      }
    );
  }

  /**
   * Hide all the modify buttons
   */
  private hideModifyButtons(): void {
    this.isModifyTitleButton = true;
    this.isModifyCreatorButton = true;
    this.isModifySubjectButton = true;
    this.isModifyDateButton = true;
    this.isModifyDescriptionButton = true;
    this.isModifyContributorButton = true;
    this.isModifyRelIdentifierButton = true;
    this.isModifyGeoLocationButton = true;
    this.isModifyAltIdentifierButton = true;
    this.isModifyRightButton = true;
    this.isModifyFundingReferenceButton = true;
    this.isModifyRelItemButton = true;
  }

  /**
   * Read the imported xml file and fill the form by calling ImportMetadatas
   * @param event the xml file
   */
  public readXmlFile(event: any) {
    // Reset the alert
    this.alertService.clear();

    // Reset the form before
    this.resetForm();

    // Hide modify buttons
    this.hideModifyButtons();

    // The xml file to read
    let importXmlFile: File = event.files[0];

    // The string read from xml file
    let sImportXml: string = "";

    // Read the file and put the string data into sImportXml
    let fileReader = new FileReader();
    fileReader.onload = (e) => {
      if (fileReader.result?.toString() != undefined) {
        sImportXml = fileReader.result?.toString();
        // Parse the string and fill the form
        this.importMetadatas(sImportXml, Action.create);
      }
    }
    fileReader.readAsText(importXmlFile);
  }

  /**
   * Validate the xml syntactically
   * @param sXmlToValidate the xml string to valide
   * @returns the validity status
   */
  private valideXmlSyntax(sXmlToValidate: string, action?: Action): Boolean {
    let validatorReturn: any = xmlParser.validate(sXmlToValidate);
    if (validatorReturn === true) {
      if (action && (action == Action.create || action == Action.exportDataCiteCreate || action == Action.exportDataCiteEdit || action == Action.exportDataCiteDuplicate)) {
        this.alertService.success("XML validé syntaxiquement. Suite du traitement ...");
      }
      return true;
    } else {
      this.alertService.error("Arrêt du traitement : le XML n'est syntaxiquement pas valide. Une erreur de type '" + validatorReturn.err.code + "' est présente en ligne " + validatorReturn.err.line + " : " + validatorReturn.err.msg);
      return false;
    }
  }
  /**
   * Validate the XML with the xsd on server
   * @param sXmlToValidate The XML to validate with XSD on server
   * @return boolean, true if the xml is valid, otherwise false
   */
  private validateXmlWithXsd(sXmlToValidate: string, action?: Action) {
    // Validate with XSD from server
    this.doiService.validateXmlWithXsd(sXmlToValidate).subscribe(
      data => {
        if (data == true) {
          if (action && (action == Action.create || action == Action.exportDataCiteCreate || action == Action.exportDataCiteEdit || action == Action.exportDataCiteDuplicate)) {
            this.alertService.clearSuccess();
            this.alertService.success("XML validé avec le schéma Datacite. Suite du traitement ...");
          }
          // If not export action
          if (action != Action.exportDataCiteCreate && action != Action.exportDataCiteEdit && action != Action.exportDataCiteDuplicate) {
            // Replace the attribute xml:lang by _doiLang for get it later
            sXmlToValidate = sXmlToValidate.split('xml:lang').join('_doiLang');
            // Parse the XML to fill form
            this.parseXml(sXmlToValidate, action);
          } else {
            // Create DOI on DataCite
            this.createOrModifyDoi(action);
          }
        }
      },
      error => {
        if (error.status == 406) {
          let sError: String[] = error.error.split("||");
          let sLine: String = "";
          let sMessage: String = "";
          if (sError.length == 2) {
            sLine = sError[0].slice(1);
            sMessage = sError[1].slice(0, sError[1]?.length - 2);
            this.alertService.error("Arrêt du traitement : le XML n'est pas valide avec le schéma Datacite : Ligne : " + sLine + ", Erreur : " + sMessage);
          } else {
            this.alertService.error("Arrêt du traitement : le XML n'est pas valide avec le schéma Datacite");
          }
        } else {
          this.alertService.error("Arrêt du traitement : Une erreur est survenue lors de de la validation avec le schéma Datacite : " + error.error.toString());
        }
        this.alreadySubmit = false;
      }
    );
  }

  /**
   * Parse the XML file with the fast-xml-parser library
   * and call the function fillFormsWithJson for fill the forms
   * @param sXml: The xml string to parse
   * @param action: The action (create, edit, duplicate)
   */
  private parseXml(sXml: any, action?: Action) {
    // Compute the correct options for the xml parsing
    let defaultOptions = {
      attributeNamePrefix: "",
      attrNodeName: "_attr", //default is 'false'
      textNodeName: "_value",
      ignoreAttributes: false,
      ignoreNameSpace: false,
      allowBooleanAttributes: false,
      parseNodeValue: true,
      parseAttributeValue: true,
      trimValues: true,
      parseTrueNumberOnly: false,
      arrayMode: false, //"strict"
      stopNodes: ["parse-me-as-string"]
    };

    try {
      // Parse the string to a jsonObj
      let jsonObj = xmlParser.parse(sXml, defaultOptions, true);

      this.alertService.clearSuccess();
      if (action && action == Action.create) {
        this.alertService.success("XML correctement lu... Affichage des données dans le formulaire en cours");
      } else {
        this.alertService.success("Metadatas correctement récupérées depuis DataCite... Affichage des données dans le formulaire en cours");
      }

      // Fill all the forms with data included into jsonObj
      this.fillFormsWithJson(jsonObj, action);
    } catch (error) {
      this.alertService.error('Problème durant la lecture des données du fichier XML : ' + error.message);
    }
  }

  /**
   * Validate the XML with XSD and parse the xml string and fill the forms
   * @param event The xml string to parse
   */
  private importMetadatas(sImportXml: string, action?: Action) {
    // Reset the alert
    this.alertService.clear();

    // Validate syntactically the xml
    let isvalidSyntax = this.valideXmlSyntax(sImportXml, action);

    // Validate wml with xsd if the xml is valid syntactically
    if (isvalidSyntax) {
      this.validateXmlWithXsd(sImportXml, action);
    }
  }

  /**
   * Fill the forms and array objects with the json object convert from XML
   * @param jsonObj the json object from XML
   */
  private fillFormsWithJson(jsonObj: any, action?: Action) {
    let continueTreatment: boolean = true;
    // With this jsonObj, put data into differents forms and object lists
    try {
      if (jsonObj) {
        // If action create
        if (action == Action.create) {
          // Alert user that he as no right on the project in the xml
          if (jsonObj.resource.identifier._value.split('/')[1]) {
            if (!this.projects.includes(jsonObj.resource.identifier._value.split('/')[1])) {
              this.alertService.error("Arrêt du traitement : Vous n'avez pas les droits sur le projet contenu dans le xml");
              this.initFirstForm();
              continueTreatment = false;
            } else {
              // Alert user that the project in the xml not match with the selected project on dashboard
              if (this.firstFormGroup.get('doi_project')?.value != this.route.snapshot.paramMap.get('project')) {
                this.alertService.warning("Attention, le projet selectionné précédemment n'est pas le même que celui contenu dans le xml");
              }
            }
          } else {
            this.alertService.error("Une erreur est survenue durant la récupération du code projet");
          }
        }

        // If actions are different from duplicate (create and edit), fill the first form
        if (action != Action.duplicate) {
          // First form - IDENTIFIER
          let doiIdentifier: string = jsonObj.resource.identifier._value;
          let doiIdentifierSplit: string[] = doiIdentifier.split('/');
          this.firstFormGroup.get("doi_prefix")?.setValue(doiIdentifierSplit[0]);
          this.firstFormGroup.get("doi_project")?.setValue(doiIdentifierSplit[1]);
          this.firstFormGroup.get("doi_identifier")?.setValue(doiIdentifierSplit[2]);

          // Disable the doi identifier if action = edit
          if (action == Action.edit) {
            this.firstFormGroup.get('doi_identifier')?.disable({ onlySelf: true });
          }

          if (continueTreatment) {
            // First form - TITLES
            if (jsonObj.resource.titles) {
              let titles: any = jsonObj.resource.titles;
              if (Array.isArray(titles.title)) {
                this.fillTitles(titles.title);
              } else {
                this.fillTitles([titles.title]);
              }
            }
          }
        }

        if (continueTreatment) {
          // Second form - CREATORS
          if (jsonObj.resource.creators) {
            let creators: any = jsonObj.resource.creators;
            if (Array.isArray(creators.creator)) {
              this.fillCreators(creators.creator);
            } else {
              this.fillCreators([creators.creator]);
            }
          }

          // Third form - Publication & Resource
          this.fillPublisherAndResources(jsonObj.resource);

          // Fourth form - Subjects
          if (jsonObj.resource.subjects) {
            let subjects: any = jsonObj.resource.subjects;
            if (Array.isArray(subjects.subject)) {
              this.fillSubjectsList(subjects.subject);
            } else {
              this.fillSubjectsList([subjects.subject]);
            }
          }

          // Fourth form - Dates
          if (jsonObj.resource.dates) {
            let dates: any = jsonObj.resource.dates;
            if (Array.isArray(dates.date)) {
              this.fillDatesList(dates.date);
            } else {
              this.fillDatesList([dates.date]);
            }
          }

          // Fourth form - Descriptions
          if (jsonObj.resource.descriptions) {
            let descriptions: any = jsonObj.resource.descriptions;
            if (Array.isArray(descriptions.description)) {
              this.fillDescriptionsList(descriptions.description);
            } else {
              this.fillDescriptionsList([descriptions.description]);
            }
          }

          // Fifth form - Contributors
          if (jsonObj.resource.contributors) {
            let contribs: any = jsonObj.resource.contributors;
            if (Array.isArray(contribs.contributor)) {
              this.fillContributorsList(contribs.contributor);
            } else {
              this.fillContributorsList([contribs.contributor]);
            }
          }

          // Sixth form - Related identifiers
          if (jsonObj.resource.relatedIdentifiers) {
            let relId: any = jsonObj.resource.relatedIdentifiers;
            if (Array.isArray(relId.relatedIdentifier)) {
              this.fillRelatedIdentifierList(relId.relatedIdentifier);
            } else {
              this.fillRelatedIdentifierList([relId.relatedIdentifier]);
            }
          }

          // Seventh form - Geo Location
          if (jsonObj.resource.geoLocations) {
            let geoLocs: any = jsonObj.resource.geoLocations;
            if (Array.isArray(geoLocs.geoLocation)) {
              this.fillGeoLocationsList(geoLocs.geoLocation);
            } else {
              this.fillGeoLocationsList([geoLocs.geoLocation]);
            }
          }

          // Eighth form - Language...
          if (jsonObj.resource) {
            this.fillLanguage(jsonObj.resource);
          }

          // Ninth form - Right
          if (jsonObj.resource.rightsList) {
            let rightsList: any = jsonObj.resource.rightsList;
            if (Array.isArray(rightsList.rights)) {
              this.fillRightsList(rightsList.rights);
            } else {
              this.fillRightsList([rightsList.rights]);
            }
          }

          // Ninth form - Funding Reference
          if (jsonObj.resource.fundingReferences) {
            let fundRefs: any = jsonObj.resource.fundingReferences;
            if (Array.isArray(fundRefs.fundingReference)) {
              this.fillFundingRefsList(fundRefs.fundingReference);
            } else {
              this.fillFundingRefsList([fundRefs.fundingReference]);
            }
          }

          // Tenth form - Related Items
          if (jsonObj.resource.relatedItems) {
            let relItems: any = jsonObj.resource.relatedItems;
            if (Array.isArray(relItems.relatedItem)) {
              this.fillRelatedItemsList(relItems.relatedItem);
            } else {
              this.fillRelatedItemsList([relItems.relatedItem]);
            }
          }

          // Update the xml
          this.makeXml();

          // Affichage terminé
          if (action && action == Action.create) {
            this.alertService.clearSuccess("XML correctement lu... Affichage des données dans le formulaire en cours");
            this.alertService.success('XML correctement lu... Affichage des données terminé');
          } else {
            this.alertService.clearSuccess("Metadatas correctement récupérées depuis DataCite... Affichage des données dans le formulaire en cours");
            this.alertService.success('Metadatas correctement récupérées depuis DataCite... Affichage des données terminé');
          }
        }
      }
    } catch (error) {
      this.alertService.error('Problème durant l\'affichage des données du XML : ' + error.message);
    }
  }

  /**
   * Add titles to the displayed titles array
   * @param titles The titles to add to the array
   */
  private fillTitles(titles: any[]) {
    titles.forEach((title: any) => {
      this.titles.push(
        new Title(title._value ? title._value : title,
          title._attr?.titleType ? this.xmlService.getEnumValueByKey(TitleTypesEnum, title._attr.titleType) : '',
          title._attr?._doiLang ? title._attr._doiLang : ''));

    });

    // Add value to the fake control for validate the step
    this.firstFormGroup.get('titleName')?.setValue('typed');

    // Render rows tables
    this.renderDoiTables('tableTitles');

    // Close form
    if (titles.length > 0) {
      this.isOpenFormTitle = false;
    }
  }

  /**
   * Add creators to the displayed creators array
   * @param creators The creators to add to the array
   */
  private fillCreators(creators: any[]) {
    creators.forEach((creator: any) => {
      let nameIds: NameIdentifier[] = new Array();
      let affs: Affiliation[] = new Array();
      if (creator.nameIdentifier) {
        if (Array.isArray(creator.nameIdentifier)) {
          creator.nameIdentifier.forEach((nameId: any) => {
            nameIds.push(
              new NameIdentifier(nameId._value,
                nameId._attr.nameIdentifierScheme,
                nameId._attr.schemeURI ? nameId._attr.schemeURI : ''));
          });
        } else {
          nameIds.push(
            new NameIdentifier(creator.nameIdentifier._value,
              creator.nameIdentifier._attr.nameIdentifierScheme,
              creator.nameIdentifier._attr.schemeURI ? creator.nameIdentifier._attr.schemeURI : ''));
        }
      }

      if (creator.affiliation) {
        if (Array.isArray(creator.affiliation)) {
          creator.affiliation.forEach((aff: any) => {
            affs.push(
              new Affiliation(aff._value != undefined ? aff._value : aff,
                (aff._attr && aff._attr.affiliationIdentifier != undefined) ? aff._attr.affiliationIdentifier : '',
                (aff._attr && aff._attr.affiliationIdentifierScheme != undefined) ? aff._attr.affiliationIdentifierScheme : '',
                (aff._attr && aff._attr.schemeURI) ? aff._attr.schemeURI : ''));
          });
        } else {
          affs.push(
            new Affiliation(creator.affiliation._value != undefined ? creator.affiliation._value : creator.affiliation,
              (creator.affiliation._attr && creator.affiliation._attr.affiliationIdentifier != undefined) ? creator.affiliation._attr.affiliationIdentifier : '',
              (creator.affiliation._attr && creator.affiliation._attr.affiliationIdentifierScheme != undefined) ? creator.affiliation._attr.affiliationIdentifierScheme : '',
              (creator.affiliation._attr && creator.affiliation._attr.schemeURI) ? creator.affiliation._attr.schemeURI : ''));
        }
      }

      this.creators.push(
        new Creator(creator.creatorName._value ? creator.creatorName._value : creator.creatorName,
          creator.creatorName._attr?.nameType ? creator.creatorName._attr.nameType : '',
          creator.creatorName._attr?._doiLang ? creator.creatorName._attr._doiLang : '',
          (creator.givenName && creator.givenName._value != undefined) ? creator.givenName._value : (creator.givenName ? creator.givenName : ''),
          (creator.familyName && creator.familyName._value != undefined) ? creator.familyName._value : (creator.familyName ? creator.familyName : ''),
          nameIds.length > 0 ? nameIds : undefined,
          affs.length > 0 ? affs : undefined));
    });

    // Add value to the fake control for validate the step
    this.secondFormGroup.get('creatorName')?.setValue('typed');

    // Render rows tables
    this.renderDoiTables('tableCreators');

    // Close form
    if (creators.length > 0) {
      this.isOpenFormCreator = false;
    }
  }

  /**
   * Fill the third form Publisher, publication year and resource
   */
  private fillPublisherAndResources(resource: any) {
    this.thirdFormGroup.get('publisher')?.setValue(resource.publisher._value ? resource.publisher._value : resource.publisher);
    this.thirdFormGroup.get('publicationYear')?.setValue(resource.publicationYear);
    let lang: string = (resource.publisher._attr && resource.publisher._attr._doiLang != undefined) ? resource.publisher._attr._doiLang : "";
    if (lang != "") {
      this.thirdFormGroup.get('publicationLang')?.setValue(lang);
    }
    this.thirdFormGroup.get('resourceType')?.setValue(resource.resourceType._value);
    this.thirdFormGroup.get('resourceTypeGeneral')?.setValue(this.xmlService.getEnumValueByKey(ResourceTypesEnum, resource.resourceType._attr.resourceTypeGeneral));
  }

  /**
   * Add subjects to the displayed subjects array
   * @param subjects The subjects to add to the array
   */
  private fillSubjectsList(subjects: SubjectDoi[]) {
    subjects.forEach((subject: any) => {
      this.subjects.push(
        new SubjectDoi(subject._value != undefined ? subject._value : subject,
          (subject._attr && subject._attr.subjectScheme != undefined) ? subject._attr.subjectScheme : '',
          (subject._attr && subject._attr.schemeURI != undefined) ? subject._attr.schemeURI : '',
          (subject._attr && subject._attr.valueURI != undefined) ? subject._attr.valueURI : '',
          (subject._attr && subject._attr.classificationCode != undefined) ? subject._attr.classificationCode : '',
          (subject._attr && subject._attr._doiLang != undefined) ? subject._attr._doiLang : ''));
    });
    // Render rows tables
    this.renderDoiTables('tableSubjects');

    // Close form
    if (subjects.length > 0) {
      this.isOpenFormSubject = false;
    }
  }

  /**
   * Add dates to the displayed dates array
   * @param dates The dates to add to the array
   */
  private fillDatesList(dates: DateDoi[]) {
    dates.forEach((date: any) => {
      this.dates.push(new DateDoi(date._value,
        date._attr.dateType,
        date._attr.dateInformation ? date._attr.dateInformation : ''));
    });
    // Render rows tables
    this.renderDoiTables('tableDates');

    // Close form
    if (dates.length > 0) {
      this.isOpenFormDate = false;
    }
  }

  /**
   * Add descriptions to the displayed descriptions array
   * @param descriptions The descriptions to add to the array
   */
  private fillDescriptionsList(descriptions: DescriptionDoi[]) {
    descriptions.forEach((descr: any) => {
      this.descriptions.push(new DescriptionDoi(descr._value,
        this.xmlService.getEnumValueByKey(DescriptionTypesEnum, descr._attr.descriptionType),
        descr._attr._doiLang ? descr._attr._doiLang : ''));
    });
    // Render rows tables
    this.renderDoiTables('tableDescriptions');

    // Close form
    if (descriptions.length > 0) {
      this.isOpenFormDescription = false;
    }
  }


  /**
   * Add contributors to the displayed contributors array
   * @param contributors The contributors to add to the array
   */
  private fillContributorsList(contributors: any[]) {
    contributors.forEach((contributor: any) => {
      let nameIds: NameIdentifier[] = new Array();
      let affs: Affiliation[] = new Array();
      if (contributor.nameIdentifier) {
        if (Array.isArray(contributor.nameIdentifier)) {
          contributor.nameIdentifier.forEach((nameId: any) => {
            nameIds.push(
              new NameIdentifier(nameId._value,
                nameId._attr.nameIdentifierScheme,
                nameId._attr.schemeURI ? nameId._attr.schemeURI : ''));
          });
        } else {
          nameIds.push(
            new NameIdentifier(contributor.nameIdentifier._value,
              contributor.nameIdentifier._attr.nameIdentifierScheme,
              contributor.nameIdentifier._attr.schemeURI ? contributor.nameIdentifier._attr.schemeURI : ''));
        }
      }

      if (contributor.affiliation) {
        if (Array.isArray(contributor.affiliation)) {
          contributor.affiliation.forEach((aff: any) => {
            affs.push(
              new Affiliation(aff._value != undefined ? aff._value : aff,
                (aff._attr && aff._attr.affiliationIdentifier != undefined) ? aff._attr.affiliationIdentifier : '',
                (aff._attr && aff._attr.affiliationIdentifierScheme != undefined) ? aff._attr.affiliationIdentifierScheme : '',
                (aff._attr && aff._attr.schemeURI != undefined) ? aff._attr.schemeURI : ''));
          });
        } else {
          affs.push(
            new Affiliation(contributor.affiliation._value != undefined ? contributor.affiliation._value : contributor.affiliation,
              (contributor.affiliation._attr && contributor.affiliation._attr.affiliationIdentifier != undefined) ? contributor.affiliation._attr.affiliationIdentifier : '',
              (contributor.affiliation._attr && contributor.affiliation._attr.affiliationIdentifierScheme != undefined) ? contributor.affiliation._attr.affiliationIdentifierScheme : '',
              (contributor.affiliation._attr && contributor.affiliation._attr.schemeURI != undefined) ? contributor.affiliation._attr.schemeURI : ''));
        }
      }

      this.contributors.push(
        new Contributor((contributor.contributorName && contributor.contributorName._value) ? contributor.contributorName._value : contributor.contributorName,
          (contributor._attr && contributor._attr.contributorType) ? this.xmlService.getEnumValueByKey(ContributorTypesEnum, contributor._attr.contributorType) : '',
          (contributor.contributorName && contributor.contributorName._attr && contributor.contributorName._attr.nameType) ? contributor.contributorName._attr.nameType : '',
          (contributor.contributorName && contributor.contributorName._attr && contributor.contributorName._attr._doiLang) ? contributor.contributorName._attr._doiLang : '',
          (contributor.givenName && contributor.givenName._value != undefined) ? contributor.givenName._value : (contributor.givenName ? contributor.givenName : ''),
          (contributor.familyName && contributor.familyName._value != undefined) ? contributor.familyName._value : (contributor.familyName ? contributor.familyName : ''),
          nameIds.length > 0 ? nameIds : undefined,
          affs.length > 0 ? affs : undefined));
    });

    // Render rows tables
    this.renderDoiTables('tableContributors');

    // Close form
    if (contributors.length > 0) {
      this.isOpenFormContributor = false;
    }
  }

  /**
   * Add relIdentifiers to the displayed relIdentifiers array
   * @param relIdentifiers The relIdentifiers to add to the array
   */
  private fillRelatedIdentifierList(relIdentifiers: RelatedIdentifier[]) {
    relIdentifiers.forEach((relIdent: any) => {
      this.relIdentifiers.push(new RelatedIdentifier(relIdent._value != undefined ? relIdent._value : relIdent,
        relIdent._attr.resourceTypeGeneral ? this.xmlService.getEnumValueByKey(ResourceTypesEnum, relIdent._attr.resourceTypeGeneral) : '',
        this.xmlService.getEnumValueByKey(RelatedIdentifierTypesEnum, relIdent._attr.relatedIdentifierType),
        relIdent._attr.relationType ? this.xmlService.getEnumValueByKey(RelationTypesEnum, relIdent._attr.relationType) : '',
        relIdent._attr.relatedMetadataScheme != undefined ? relIdent._attr.relatedMetadataScheme : '',
        relIdent._attr.schemeURI ? relIdent._attr.schemeURI : '',
        relIdent._attr.schemeType != undefined ? relIdent._attr.schemeType : ''));
    });
    // Render rows tables
    this.renderDoiTables('tableRelatedIdentifiers');

    // Close form
    if (relIdentifiers.length > 0) {
      this.isOpenFormRelatedIdentifier = false;
    }
  }

  /**
   * Fill the seventh form Geo Location
   */
  private fillGeoLocationsList(geoLocs: GeoLocation[]) {
    geoLocs.forEach((geoLoc: any) => {
      // Geo Location Box
      let geoLocationBox: GeoLocationBox[] = new Array();
      if (geoLoc.geoLocationBox) {
        geoLocationBox.push(new GeoLocationBox(geoLoc.geoLocationBox.northBoundLatitude != undefined ? geoLoc.geoLocationBox.northBoundLatitude : '',
          geoLoc.geoLocationBox.southBoundLatitude != undefined ? geoLoc.geoLocationBox.southBoundLatitude : '',
          geoLoc.geoLocationBox.westBoundLongitude != undefined ? geoLoc.geoLocationBox.westBoundLongitude : '',
          geoLoc.geoLocationBox.eastBoundLongitude != undefined ? geoLoc.geoLocationBox.eastBoundLongitude : ''));
      }

      // MULTIPLE - Geo Location Polygon Points
      let geoLocationPolygonPoints: GeoLocationPoint[] = new Array();
      if (geoLoc.geoLocationPolygon && geoLoc.geoLocationPolygon.polygonPoint) {
        if (Array.isArray(geoLoc.geoLocationPolygon.polygonPoint)) {
          geoLoc.geoLocationPolygon.polygonPoint.forEach((polygonPoint: any) => {
            geoLocationPolygonPoints.push(
              new GeoLocationPoint(polygonPoint.pointLongitude != undefined ? polygonPoint.pointLongitude : '',
                polygonPoint.pointLatitude != undefined ? polygonPoint.pointLatitude : ''));
          });
        } else {
          geoLocationPolygonPoints.push(
            new GeoLocationPoint(geoLoc.geoLocationPolygon.polygonPoint.pointLongitude != undefined ? geoLoc.geoLocationPolygon.polygonPoint.pointLongitude : '',
              geoLoc.geoLocationPolygon.polygonPoint.pointLatitude != undefined ? geoLoc.geoLocationPolygon.polygonPoint.pointLatitude : ''));
        }
      }

      // Geo Location Polygons
      let geoLocationPolygon: GeoLocationPolygon[] = new Array();
      if (geoLoc.geoLocationPolygon) {
        geoLocationPolygon.push(new GeoLocationPolygon(geoLocationPolygonPoints,
          new GeoLocationPoint((geoLoc.geoLocationPolygon && geoLoc.geoLocationPolygon.inPolygonPoint && geoLoc.geoLocationPolygon.inPolygonPoint.pointLongitude != undefined) ? geoLoc.geoLocationPolygon.inPolygonPoint.pointLongitude : '',
            (geoLoc.geoLocationPolygon && geoLoc.geoLocationPolygon.inPolygonPoint && geoLoc.geoLocationPolygon.inPolygonPoint.pointLatitude != undefined) ? geoLoc.geoLocationPolygon.inPolygonPoint.pointLatitude : '')))
      }
      this.geoLocations.push(
        new GeoLocation(
          (geoLoc.geoLocationPlace && geoLoc.geoLocationPlace._value != undefined) ? geoLoc.geoLocationPlace._value : (geoLoc.geoLocationPlace ? geoLoc.geoLocationPlace : ''),
          new GeoLocationPoint((geoLoc.geoLocationPoint && geoLoc.geoLocationPoint.pointLongitude != undefined) ? geoLoc.geoLocationPoint.pointLongitude : '',
            (geoLoc.geoLocationPoint && geoLoc.geoLocationPoint.pointLatitude != undefined) ? geoLoc.geoLocationPoint.pointLatitude : ''),
          geoLocationBox,
          geoLocationPolygon));

    });
    // Render rows tables
    this.renderDoiTables('tableGeoLocations');

    // Close form
    if (geoLocs.length > 0) {
      this.isOpenFormGeoLocation = false;
    }
  }

  /**
   * Eighth form
   * @param resource
   */
  private fillLanguage(resource: any) {

    // Language
    this.eighthFormGroup.get('language')?.setValue(resource.language != undefined ? resource.language : '');

    // Version
    this.eighthFormGroup.get('version')?.setValue(resource.version != undefined ? resource.version : '');

    // Alternate Identifiers
    if (resource.alternateIdentifiers && resource.alternateIdentifiers.alternateIdentifier) {
      let alternateIdentifiersList: any;
      if (Array.isArray(resource.alternateIdentifiers.alternateIdentifier)) {
        alternateIdentifiersList = resource.alternateIdentifiers.alternateIdentifier;
      } else {
        alternateIdentifiersList = [resource.alternateIdentifiers.alternateIdentifier];
      }
      alternateIdentifiersList.forEach((alternateIdentifier: AltIdentifier) => {
        this.altIdentifiers.push(new AltIdentifier(
          alternateIdentifier._value,
          (alternateIdentifier._attr && alternateIdentifier._attr.alternateIdentifierType != undefined) ? alternateIdentifier._attr.alternateIdentifierType : ''));
      })
      // Alternate Identifiers : Render rows tables
      this.renderDoiTables('tableAltIdentifiers');
      // Alternate Identifiers : Close form
      if (alternateIdentifiersList.length > 0) {
        this.isOpenFormAlternateIdentifier = false;
      }
    }

    // Sizes
    if (resource.sizes && resource.sizes.size != undefined) {
      let sizesList;
      if (Array.isArray(resource.sizes.size)) {
        sizesList = resource.sizes.size;
      } else {
        sizesList = [resource.sizes.size];
      }
      sizesList.forEach((size: string) => {
        this.sizes.push(size != undefined ? size : '');
      });

      // Sizes : Render rows tables
      this.renderDoiTables('tableSizes');

      // Sizes : Close form
      if (sizesList.length > 0) {
        this.isOpenFormSize = false;
      }
    }

    // Formats
    if (resource.formats && resource.formats.format != undefined) {
      let formatsList;
      if (Array.isArray(resource.formats.format)) {
        formatsList = resource.formats.format;
      } else {
        formatsList = [resource.formats.format];
      }
      formatsList.forEach((format: string) => {
        this.formats.push(format != undefined ? format : '');
      });

      // Formats : Render rows tables
      this.renderDoiTables('tableFormats');

      // Formats : Close form
      if (formatsList.length > 0) {
        this.isOpenFormFormat = false;
      }
    }
  }

  /**
   * Add rights to the displayed rights array
   * @param rights The rights to add to the array
   */
  private fillRightsList(rights: Right[]) {
    rights.forEach((right: Right) => {
      this.rights.push(new Right(right._value,
        (right._attr && right._attr.rightsURI) ? right._attr.rightsURI : '',
        (right._attr && right._attr.rightsIdentifier != undefined) ? right._attr.rightsIdentifier : '',
        (right._attr && right._attr.rightsIdentifierScheme != undefined) ? right._attr.rightsIdentifierScheme : '',
        (right._attr && right._attr.schemeURI) ? right._attr.schemeURI : '',
        (right._attr && right._attr._doiLang) ? right._attr._doiLang : ''));
    });
    // Render rows tables
    this.renderDoiTables('tableRights');

    // Close form
    if (rights.length > 0) {
      this.isOpenFormRight = false;
    }
  }

  /**
   * Add fundRefs to the displayed fundRefs array
   * @param fundRefs The fundRefs to add to the array
   */
  private fillFundingRefsList(fundRefs: FundingRef[]) {
    fundRefs.forEach((fundRef: any) => {
      this.fundingRefs.push(new FundingRef(fundRef.funderName,
        (fundRef.awardTitle && fundRef.awardTitle._value != undefined) ? fundRef.awardTitle._value : (fundRef.awardTitle ? fundRef.awardTitle : ''),
        (fundRef.awardNumber && fundRef.awardNumber._value != undefined) ? fundRef.awardNumber._value : fundRef.awardNumber,
        (fundRef.awardNumber && fundRef.awardNumber._attr && fundRef.awardNumber._attr.awardURI) ? fundRef.awardNumber._attr.awardURI : '',
        (fundRef.funderIdentifier && fundRef.funderIdentifier._value != undefined) ? fundRef.funderIdentifier._value : '',
        (fundRef.funderIdentifier && fundRef.funderIdentifier._attr && fundRef.funderIdentifier._attr.funderIdentifierType) ? fundRef.funderIdentifier._attr.funderIdentifierType : '',
        (fundRef.funderIdentifier && fundRef.funderIdentifier._attr && fundRef.funderIdentifier._attr.schemeURI) ? fundRef.funderIdentifier._attr.schemeURI : ''));
    });
    // Render rows tables
    this.renderDoiTables('tableFundingRefs');

    // Close form
    if (fundRefs.length > 0) {
      this.isOpenFormFundingReference = false;
    }
  }

  /**
   * Add relItems to the displayed relItems array
   * @param relItems The relItems to add to the array
   */
  private fillRelatedItemsList(relItems: RelatedItem[]) {
    relItems.forEach((relItem: any) => {
      // Related Item Creator
      let relItemCreators: RelatedItemCreator[] = new Array();
      if (relItem.creators) {
        let creators: any = relItem.creators;
        if (Array.isArray(creators.creator)) {
          creators.creator.forEach((creator: any) => {
            relItemCreators.push(
              new RelatedItemCreator(
                (creator.creatorName && creator.creatorName._value != undefined) ? creator.creatorName._value : creator.creatorName,
                (creator.creatorName && creator.creatorName._attr && creator.creatorName._attr.nameType) ? this.xmlService.getEnumValueByKey(NameTypesEnum, creator.creatorName._attr.nameType) : '',
                (creator.creatorName && creator.creatorName._attr && creator.creatorName._attr._doiLang) ? creator.creatorName._attr._doiLang : '',
                (creator.givenName && creator.givenName._value != undefined) ? creator.givenName._value : (creator.givenName ? creator.givenName : ''),
                (creator.familyName && creator.familyName._value != undefined) ? creator.familyName._value : (creator.familyName ? creator.familyName : '')));
          });
        } else {
          let creator: any = creators.creator;
          relItemCreators.push(
            new RelatedItemCreator(
              (creator.creatorName && creator.creatorName._value != undefined) ? creator.creatorName._value : creator.creatorName,
              (creator.creatorName && creator.creatorName._attr && creator.creatorName._attr.nameType) ? this.xmlService.getEnumValueByKey(NameTypesEnum, creator.creatorName._attr.nameType) : '',
              (creator.creatorName && creator.creatorName._attr && creator.creatorName._attr._doiLang) ? creator.creatorName._attr._doiLang : '',
              (creator.givenName && creator.givenName._value != undefined) ? creator.givenName._value : (creator.givenName ? creator.givenName : ''),
              (creator.familyName && creator.familyName._value != undefined) ? creator.familyName._value : (creator.familyName ? creator.familyName : '')));
        }
      }

      // Related Item Title
      let relItemTitles: Title[] = new Array();
      if (relItem.titles) {
        let titles: any = relItem.titles;
        if (Array.isArray(titles.title)) {
          titles.title.forEach((title: any) => {
            relItemTitles.push(
              new Title(
                title._value != undefined ? title._value : title,
                (title._attr && title._attr.titleType) ? this.xmlService.getEnumValueByKey(TitleTypesEnum, title._attr.titleType) : '',
                (title._attr && title._attr._doiLang) ? title._attr._doiLang : ''));
          });
        } else {
          let title: any = titles.title;
          relItemTitles.push(new Title(
            title._value != undefined ? title._value : title,
            (title._attr && title._attr.titleType) ? this.xmlService.getEnumValueByKey(TitleTypesEnum, title._attr.titleType) : '',
            (title._attr && title._attr._doiLang) ? title._attr._doiLang : ''));
        }
      }

      // Related Item Contributor
      let relItemContributors: Contributor[] = new Array();
      if (relItem.contributors) {
        let contributors: any = relItem.contributors;
        if (Array.isArray(contributors.contributor)) {
          contributors.contributor.forEach((contributor: any) => {
            relItemContributors.push(
              new Contributor(
                (contributor.contributorName && contributor.contributorName._value) ? contributor.contributorName._value : contributor.contributorName,
                (contributor._attr && contributor._attr.contributorType) ? this.xmlService.getEnumValueByKey(ContributorTypesEnum, contributor._attr.contributorType) : '',
                (contributor.contributorName && contributor.contributorName._attr && contributor.contributorName._attr.nameType) ? contributor.contributorName._attr.nameType : '',
                (contributor.contributorName && contributor.contributorName._attr && contributor.contributorName._attr._doiLang) ? contributor.contributorName._attr._doiLang : '',
                (contributor.givenName && contributor.givenName._value) ? contributor.givenName._value : (contributor.givenName ? contributor.givenName : ''),
                (contributor.familyName && contributor.familyName._value) ? contributor.familyName._value : (contributor.familyName ? contributor.familyName : '')));
          });
        } else {
          let contributor: any = contributors.contributor;
          relItemContributors.push(
            new Contributor(
              (contributor.contributorName && contributor.contributorName._value) ? contributor.contributorName._value : contributor.contributorName,
              (contributor._attr && contributor._attr.contributorType) ? this.xmlService.getEnumValueByKey(ContributorTypesEnum, contributor._attr.contributorType) : '',
              (contributor.contributorName && contributor.contributorName._attr && contributor.contributorName._attr.nameType) ? contributor.contributorName._attr.nameType : '',
              (contributor.contributorName && contributor.contributorName._attr && contributor.contributorName._attr._doiLang) ? contributor.contributorName._attr._doiLang : '',
              (contributor.givenName && contributor.givenName._value) ? contributor.givenName._value : (contributor.givenName ? contributor.givenName : ''),
              (contributor.familyName && contributor.familyName._value) ? contributor.familyName._value : (contributor.familyName ? contributor.familyName : '')));
        }
      }

      this.relItems.push(
        new RelatedItem(
          new RelatedItemIdentifier(
            (relItem.relatedItemIdentifier && relItem.relatedItemIdentifier._value != undefined) ? relItem.relatedItemIdentifier._value : relItem.relatedItemIdentifier,
            (relItem.relatedItemIdentifier && relItem.relatedItemIdentifier._attr && relItem.relatedItemIdentifier._attr.relatedItemIdentifierType) ? relItem.relatedItemIdentifier._attr.relatedItemIdentifierType : '',
            (relItem.relatedItemIdentifier && relItem.relatedItemIdentifier._attr && relItem.relatedItemIdentifier._attr.relatedMetadataScheme != undefined) ? relItem.relatedItemIdentifier._attr.relatedMetadataScheme : '',
            (relItem.relatedItemIdentifier && relItem.relatedItemIdentifier._attr && relItem.relatedItemIdentifier._attr.schemeURI) ? relItem.relatedItemIdentifier._attr.schemeURI : '',
            (relItem.relatedItemIdentifier && relItem.relatedItemIdentifier._attr && relItem.relatedItemIdentifier._attr.schemeType != undefined) ? relItem.relatedItemIdentifier._attr.schemeType : ''),
          this.xmlService.getEnumValueByKey(ResourceTypesEnum, relItem._attr.relatedItemType),
          this.xmlService.getEnumValueByKey(RelationTypesEnum, relItem._attr.relationType),
          relItemCreators,
          relItemTitles,
          relItem.publicationYear ? relItem.publicationYear : '',
          (relItem.volume && relItem.volume._value != undefined) ? relItem.volume._value : (relItem.volume ? relItem.volume : ''),
          (relItem.issue && relItem.issue._value != undefined) ? relItem.issue._value : (relItem.issue ? relItem.issue : ''),
          new RelatedItemNumber(
            (relItem.number && relItem.number._value != undefined) ? relItem.number._value : relItem.number,
            (relItem.number && relItem.number._attr && relItem.number._attr.numberType) ? this.xmlService.getEnumValueByKey(NumberTypesEnum, relItem.number._attr.numberType) : ''),
          (relItem.firstPage && relItem.firstPage._value != undefined) ? relItem.firstPage._value : (relItem.firstPage ? relItem.firstPage : ''),
          (relItem.lastPage && relItem.lastPage._value != undefined) ? relItem.lastPage._value : (relItem.lastPage ? relItem.lastPage : ''),
          (relItem.publisher && relItem.publisher._value != undefined) ? relItem.publisher._value : (relItem.publisher ? relItem.publisher : ''),
          (relItem.edition && relItem.edition._value != undefined) ? relItem.edition._value : (relItem.edition ? relItem.edition : ''),
          relItemContributors));
    });

    // Close form
    if (relItems.length > 0) {
      this.isOpenFormRelatedItem = false;
    }
  }

  /**
   * Update the field Identifier in mandatory elements
   * when the DOI Identifier is manually update
   */
  updateDoiIdentifier(): void {
    let currentDoi = this.firstFormGroup.get("doi_prefix")?.value + "/" + this.firstFormGroup.get("doi_project")?.value + "/" + this.firstFormGroup.get("doi_identifier")?.value;
    this.endFormGroup.get("identifier")?.setValue(currentDoi);
  }

  /**
   * Create an array of i cases
   * @param i
   * @returns
   */
  counter(i: number): Array<number> {
    return new Array(i);
  }

  /**
   * Counts the number of lines in a string
   * @param text to split
   * @returns number of lines
   */
  public lineCounter(text: string): number {
    return (text.split("\n")).length + 5; // Add 5 lines to permit double lines
  }

  /**
   * Display button only on the last element of the iterator
   * @param nbItem
   * @param index
   * @returns
   */
  showButton(nbItem: number, index: number) {
    if (nbItem == (index + 1)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Add element to the form when an sub-item is added
   * @param item
   * @param index
   */
  addSubItemOnly(item: string, index: number, subIndex?: number) {
    switch (item) {
      case "creatorNameIdentifier":
        this.nbCreatorNameIdentifiers++;
        this.addSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorNameIdentifier", index);
        this.addSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorNameIdentifierScheme", index);
        this.addSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorSchemeURI", index, this.uriRegex);
        this.isCreatorNameIdShemeRequired.push(false);
        break;
      case "creatorAffiliation":
        this.nbCreatorAffiliations++;
        this.addSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorAffiliation", index);
        this.addSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorAffiliationIdentifier", index);
        this.addSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorAffiliationIdentifierScheme", index);
        this.addSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorAffiliationSchemeURI", index, this.uriRegex);
        break;
      case "contributorNameIdentifier":
        this.nbContributorNameIdentifiers++;
        this.addSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorNameIdentifier", index);
        this.addSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorNameIdentifierScheme", index);
        this.addSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorSchemeURI", index, this.uriRegex);
        this.isContributorNameIdShemeRequired.push(false);
        break;
      case "contributorAffiliation":
        this.nbContributorAffiliations++;
        this.addSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorAffiliation", index);
        this.addSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorAffiliationIdentifier", index);
        this.addSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorAffiliationIdentifierScheme", index);
        this.addSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorAffiliationSchemeURI", index, this.uriRegex);
        break
      case "geoLocationPolygon":
        this.nbGeoLocationPolygon++;
        if (this.nbGeoLocationPolygonPoints[index] == undefined) {
          this.nbGeoLocationPolygonPoints.push(4);
        } else if (this.nbGeoLocationPolygonPoints[index] == 0) {
          this.nbGeoLocationPolygonPoints[index] = 4;
        }
        for (let i = 0; i < 4; i++) {
          this.addSubItemOnlyInDoiForm(this.seventhFormGroup, "polygonPointLongitude", index, this.longitudeRegex, i);
          this.addSubItemOnlyInDoiForm(this.seventhFormGroup, "polygonPointLatitude", index, this.latitudeRegex, i);
        }
        this.addSubItemOnlyInDoiForm(this.seventhFormGroup, "inPolygonPointLongitude", index, this.longitudeRegex);
        this.addSubItemOnlyInDoiForm(this.seventhFormGroup, "inPolygonPointLatitude", index, this.latitudeRegex);
        break;
      case "geoLocationPolygonPoint":
        this.nbGeoLocationPolygonPoints[index]++;
        this.addSubItemOnlyInDoiForm(this.seventhFormGroup, "polygonPointLongitude", index, this.longitudeRegex, subIndex);
        this.addSubItemOnlyInDoiForm(this.seventhFormGroup, "polygonPointLatitude", index, this.latitudeRegex, subIndex);
        break;
      case "relatedItemCreator":
        this.nbRelatedItemCreators++;
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemCreatorName", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemCreatorType", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemCreatorLang", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemCreatorGivenName", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemCreatorFamilyName", index);
        break;
      case "relatedItemTitle":
        this.nbRelatedItemTitles++;
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemTitle", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemTitleType", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemTitleLang", index);
        break;
      case "relatedItemContributor":
        this.nbRelatedItemContributors++;
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorName", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorNameType", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorLang", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorGivenName", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorFamilyName", index);
        this.addSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorType", index);
        break;
      default: ;
    }
  }

  /**
   * Add control of an sub-item in FormGroup (when sub-item is duplicate)
   * @param formGroup
   * @param item
   * @param index
   */
  addSubItemOnlyInDoiForm(formGroup: FormGroup, item: string, index: number, validator?: string, subIndex?: number) {
    let name: string;
    if (!subIndex && subIndex != 0) {
      name = item + "_" + (index).toString();
    } else {
      name = item + "_" + (index).toString() + "_" + (subIndex).toString();
    }
    if (!validator) {
      formGroup.addControl(name, new FormControl(''));
    } else {
      formGroup.addControl(name, new FormControl('', [Validators.pattern(validator)]));
    }
  }

  /**
   * Remove element to the form when an sub-item is removed
   * @param item
   * @param index
   */
  removeSubItemOnly(item: string, index: number, subIndex?: number) {
    switch (item) {
      case "creatorNameIdentifier":
        this.removeSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorNameIdentifier", index, this.nbCreatorNameIdentifiers);
        this.removeSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorNameIdentifierScheme", index, this.nbCreatorNameIdentifiers);
        this.removeSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorSchemeURI", index, this.nbCreatorNameIdentifiers, this.uriRegex);
        this.nbCreatorNameIdentifiers--;
        this.isCreatorNameIdShemeRequired.splice(index, 1);
        break;
      case "creatorAffiliation":
        this.removeSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorAffiliation", index, this.nbCreatorAffiliations);
        this.removeSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorAffiliationIdentifier", index, this.nbCreatorAffiliations);
        this.removeSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorAffiliationIdentifierScheme", index, this.nbCreatorAffiliations);
        this.removeSubItemOnlyInDoiForm(this.secondFormGroupMultiCreator, "creatorAffiliationSchemeURI", index, this.nbCreatorAffiliations, this.uriRegex);
        this.nbCreatorAffiliations--;
        break;
      case "contributorNameIdentifier":
        this.removeSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorNameIdentifier", index, this.nbContributorNameIdentifiers);
        this.removeSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorNameIdentifierScheme", index, this.nbContributorNameIdentifiers);
        this.removeSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorSchemeURI", index, this.nbContributorNameIdentifiers, this.uriRegex);
        this.nbContributorNameIdentifiers--;
        this.isContributorNameIdShemeRequired.splice(index, 1);
        break;
      case "contributorAffiliation":
        this.removeSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorAffiliation", index, this.nbContributorNameIdentifiers);
        this.removeSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorAffiliationIdentifier", index, this.nbContributorNameIdentifiers);
        this.removeSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorAffiliationIdentifierScheme", index, this.nbContributorNameIdentifiers);
        this.removeSubItemOnlyInDoiForm(this.fifthFormGroupMultiContributor, "contributorAffiliationSchemeURI", index, this.nbContributorNameIdentifiers, this.uriRegex);
        this.nbContributorAffiliations--;
        break
      case "geoLocationPolygon":
        for (let i = this.nbGeoLocationPolygonPoints[index] - 1; i >= 0; i--) {
          this.removeSubItemOnlyInDoiForm(this.seventhFormGroup, "polygonPointLongitude", index, this.nbGeoLocationPolygon, this.longitudeRegex, i);
          this.removeSubItemOnlyInDoiForm(this.seventhFormGroup, "polygonPointLatitude", index, this.nbGeoLocationPolygon, this.latitudeRegex, i);
        }
        this.nbGeoLocationPolygonPoints.splice(index, 1);
        this.removeSubItemOnlyInDoiForm(this.seventhFormGroup, "inPolygonPointLongitude", index, this.nbGeoLocationPolygon, this.longitudeRegex);
        this.removeSubItemOnlyInDoiForm(this.seventhFormGroup, "inPolygonPointLatitude", index, this.nbGeoLocationPolygon, this.latitudeRegex);
        this.nbGeoLocationPolygon--;
        break;
      case "geoLocationPolygonPoint":
        if (this.nbGeoLocationPolygonPoints[index] > 4) {
          this.removeSubItemOnlyInDoiForm(this.seventhFormGroup, "polygonPointLongitude", index, this.nbGeoLocationPolygon, this.longitudeRegex, subIndex, this.nbGeoLocationPolygonPoints[index]);
          this.removeSubItemOnlyInDoiForm(this.seventhFormGroup, "polygonPointLatitude", index, this.nbGeoLocationPolygon, this.latitudeRegex, subIndex, this.nbGeoLocationPolygonPoints[index]);
          this.nbGeoLocationPolygonPoints[index]--;
        }
        break;
      case "relatedItemCreator":
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemCreatorName", index, this.nbRelatedItemCreators);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemCreatorType", index, this.nbRelatedItemCreators);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemCreatorLang", index, this.nbRelatedItemCreators);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemCreatorGivenName", index, this.nbRelatedItemCreators);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemCreatorFamilyName", index, this.nbRelatedItemCreators);
        this.nbRelatedItemCreators--;
        break;
      case "relatedItemTitle":
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemTitle", index, this.nbRelatedItemTitles);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemTitleType", index, this.nbRelatedItemTitles);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemTitleLang", index, this.nbRelatedItemTitles);
        this.nbRelatedItemTitles--;
        break;
      case "relatedItemContributor":
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorName", index, this.nbRelatedItemContributors);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorNameType", index, this.nbRelatedItemContributors);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorLang", index, this.nbRelatedItemContributors);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorGivenName", index, this.nbRelatedItemContributors);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorFamilyName", index, this.nbRelatedItemContributors);
        this.removeSubItemOnlyInDoiForm(this.tenthFormGroupMultiRelItems, "relatedItemContributorType", index, this.nbRelatedItemContributors);
        this.nbRelatedItemContributors--;
        break;
      default: ;
    }
  }

  /**
   * Remove subItem and rename uppers subItems
   * @param formGroup : the formControl's formGroup
   * @param item : the formControl's item
   * @param index : the formControl's number
   * @param nbItem : the sum of all the formControl of the item (before removing)
   * @param validator : the formControl's validator
   */
  removeSubItemOnlyInDoiForm(formGroup: FormGroup, item: string, index: number, nbItem: number, validator?: string, subIndex?: number, nbSubItem?: number) {
    // Remove 3rd level item independently
    if (subIndex && nbSubItem) {
      // Delete current formControl
      formGroup.removeControl(item + "_" + (index).toString() + "_" + (subIndex).toString());
      // Rename uppers formControl
      for (let i = subIndex + 1; i < nbSubItem; i++) {
        let name = item + "_" + (index).toString() + "_" + (i).toString();
        let rename = item + "_" + (index).toString() + "_" + (i - 1).toString();
        if (!validator) {
          formGroup.addControl(rename, new FormControl(''));
        } else {
          formGroup.addControl(rename, new FormControl('', [Validators.pattern(validator)]));
        }
        formGroup.get(rename)?.setValue(formGroup.get(name)?.value);
        formGroup.removeControl(name);
      }
    }
    // Remove 3rd level item from parent deletion
    else if (subIndex || subIndex == 0) {
      // Delete current formControl
      formGroup.removeControl(item + "_" + (index).toString() + "_" + (subIndex).toString());
      if (subIndex == 0) {
        // Rename uppers formControl
        for (let i = index + 1; i < this.nbGeoLocationPolygon; i++) {
          for (let j = 0; j < this.nbGeoLocationPolygonPoints[i]; j++) {
            let name = item + "_" + (i).toString() + "_" + (j).toString();
            let rename = item + "_" + (i - 1).toString() + "_" + (j).toString();
            if (!validator) {
              formGroup.addControl(rename, new FormControl(''));
            } else {
              formGroup.addControl(rename, new FormControl('', [Validators.pattern(validator)]));
            }
            formGroup.get(rename)?.setValue(formGroup.get(name)?.value);
            formGroup.removeControl(name);
          }
        }
      }
    }
    // Remove 2nd level item independently
    else {
      // Delete current formControl
      formGroup.removeControl(item + "_" + (index).toString());
      // Rename uppers formControl
      for (let i = index + 1; i < nbItem; i++) {
        let name = item + "_" + (i).toString();
        let rename = item + "_" + (i - 1).toString();
        if (!validator) {
          formGroup.addControl(rename, new FormControl(''));
        } else {
          formGroup.addControl(rename, new FormControl('', [Validators.pattern(validator)]));
        }
        formGroup.get(rename)?.setValue(formGroup.get(name)?.value);
        formGroup.removeControl(name);
      }
    }
  }

  /**
   * Reset inputs fields
   */
  resetForm() {
    this.ngOnInit();
  }

  /**
   * Submit
   * Create DOI and associated URL
   */
  onSubmit() {
    // Reset alerts
    this.alertService.clear();

    this.alreadySubmit = true;

    // Make the xml
    this.makeXml();

    // Validate the XML
    //
    // Validate syntactically the xml
    let act = this.route.snapshot.paramMap.get('action');
    let isvalidSyntax: Boolean = true;
    if (act) {
      isvalidSyntax = this.valideXmlSyntax(this.xmlDoi, act == Action.create ? Action.exportDataCiteCreate : (act == Action.edit ? Action.exportDataCiteEdit : (act == Action.duplicate ? Action.exportDataCiteDuplicate : undefined)));
    }

    // Validate wml with xsd if the xml is valid syntactically
    if (isvalidSyntax) {
      this.validateXmlWithXsd(this.xmlDoi, act == Action.create ? Action.exportDataCiteCreate : (act == Action.edit ? Action.exportDataCiteEdit : (act == Action.duplicate ? Action.exportDataCiteDuplicate : undefined)));
    } else {
      this.alreadySubmit = false;
    }
  }

  /**
   * Create or modify DOI on DataCite
   */
  private createOrModifyDoi(action?: Action) {
    // Create the DOI
    this.doiService.createOrModifyDOI(this.firstFormGroup.get("doi_project")?.value, this.xmlDoi).subscribe(
      data => {
      }, error => {
        if (error.status == 201 && error.statusText == "Created") {
          this.alertService.clear();
          this.alertService.success("Les metadatas du  DOI " + this.endFormGroup.get("identifier")?.value + " ont été correctement ajoutées. Ajout de la landing page ...");

          // Create the URL on DOI
          this.doiService.createOrModifyURL(this.firstFormGroup.get("doi_project")?.value, this.endFormGroup.get("identifier")?.value, this.firstFormGroup.get("url")?.value).subscribe(
            data => {
            },
            error => {
              if (error.status == 201 && error.statusText == "Created") {
                this.alertService.success("Landing page ajouté ... Le DOI " + this.endFormGroup.get("identifier")?.value + " a correctement été publié.");

                // Disabled the Create/Modify button
                this.alreadySubmit = true;
              } else {
                this.alertService.error("Une erreur est survenue durant l'ajout de l'URL au DOI " + this.endFormGroup.get("identifier")?.value + " : " + error.statusText);
                this.alreadySubmit = false;
              }
            }
          );
        } else {
          this.alertService.error("Une erreur est survenue durant le création du DOI " + this.endFormGroup.get("identifier")?.value + " : " + error.statusText);
          this.alreadySubmit = false;
        }
      }
    );
  }

  /**
   * Load resources.json
   * Containes some usefull values to make XML file
   */
  loadResourcesJson() {
    this.resourcesJson = ResourcesJson;
  }

  /**
   * Build XML object
   * TODO : Finish to construct XML object (xsd conform)
   */
  makeXml() {
    this.loadResourcesJson();

    let defaultOptions = {
      attrNodeName: "_attr",
      textNodeName: "_value",
      format: false,
      supressEmptyNode: true
    };
    let parser = new xmlParser.j2xParser(defaultOptions);

    // Get resource xml (root)
    let resource: any = {
      "xsi": this.resourcesJson.xsi,
      "xmlns": this.resourcesJson.xmlns,
      "schemaLocation": this.resourcesJson.schemaLocation
    }

    // Get values, clone the differents lists if there are an enum type to update
    //
    // Titles
    let titles: Title[] = JSON.parse(JSON.stringify(this.titles));

    // Creators
    let creators: Creator[] = JSON.parse(JSON.stringify(this.creators));

    // Get publisher / publication datas
    let pub: Publisher = new Publisher(this.thirdFormGroup.value.publisher, this.thirdFormGroup.value.publicationLang);

    // Get resource datas
    let res: ResourceDoi = new ResourceDoi(this.thirdFormGroup.value.resourceType, this.thirdFormGroup.value.resourceTypeGeneral);
    res.getEnumValue();

    // Subjects - No clone or enum update

    // Dates
    let dates: DateDoi[] = JSON.parse(JSON.stringify(this.dates));

    // Descriptions
    let descriptions: DescriptionDoi[] = JSON.parse(JSON.stringify(this.descriptions));

    // Contributors
    let contributors: Contributor[] = JSON.parse(JSON.stringify(this.contributors));


    // Related Identifiers
    let relIdentifiers: RelatedIdentifier[] = JSON.parse(JSON.stringify(this.relIdentifiers));

    // Geo Locations - No clone or enum update
    // Alternate identifiers - No clone or enum update
    // Size - No clone or enum update
    // Formats - No clone or enum update
    // Rights - No clone or enum update
    // Funding reference - No clone or enum update

    // Related items
    let relItems: RelatedItem[] = JSON.parse(JSON.stringify(this.relItems));

    // Make the xml object
    let modelDoi: XmlDoi = new XmlDoi(this.endFormGroup.value.identifier,
      creators, titles, pub,
      this.thirdFormGroup.value.publicationYear,
      res, this.subjects, contributors, dates, this.sizes, this.formats,
      this.eighthFormGroup.value.version,
      this.rights,
      this.fundingRefs,
      descriptions,
      this.geoLocations,
      this.eighthFormGroup.value.language,
      this.altIdentifiers,
      relIdentifiers,
      relItems,
      resource);

    modelDoi = this.removeEmptyValues(modelDoi);
    this.xmlDoi = this.adaptXml(parser.parse(modelDoi));
  }



  /**
   * Create and download the xml file
   */
  public downloadXml() {
    // Create the file to download
    let xmlFile: File = new File([this.xmlDoi], this.endFormGroup.value.identifier, { type: "application/xml" });

    const anchor = window.document.createElement('a');
    anchor.href = window.URL.createObjectURL(xmlFile);

    anchor.download = this.endFormGroup.value.identifier + ".xml";
    document.body.appendChild(anchor);
    anchor.click();
    document.body.removeChild(anchor);
    window.URL.revokeObjectURL(anchor.href);
  }
  /**
   * Recurse through a JSON structure and remove array values equal to "" and object key: value pairs where the value === "".
   * Used to remove empty XML tags from the output JSON.
   *
   * @param {Object[]} val The JSON structure
   * @returns {Object[]} The new JSON structure with empty values removed
   */
  private removeEmptyValues(val: any): any {
    if (Array.isArray(val)) {
      return val.reduce((res, cur) => {
        if (cur !== "") {
          return [...res, this.removeEmptyValues(cur)];
        }
        return res;
      }, []);
    } else if (Object.prototype.toString.call(val) === "[object Object]") {
      return Object.keys(val).reduce((res, key) => {
        if (val[key] !== "") {
          return Object.assign({}, res, { [key]: this.removeEmptyValues(val[key]) });
        }
        return res;
      }, undefined);
    }
    return val;
  }

  /**
   * Supress the empty nodes
   *
   * @param xml string of the xml generated by json
   * @returns Return the xml modified
   */
  private adaptXml(xml: string): string {
    xml = '<?xml version="1.0" encoding="UTF-8"?>\n' + xml;
    xml = xml.split('_doiLang').join('xml:lang');

    // Format the XML
    let format = require('xml-formatter');
    xml = format(xml, {
      indentation: '\t',
      collapseContent: true,
      lineSeparator: '\n'
    });

    // Specific formatter
    let outXml: string = "";
    let lines: string[] = xml.split('\n');
    lines.forEach(line => {
      if (!line.endsWith('/>')) {
        if (line.includes('<hovered>false</hovered>')) {
          line = line.replace('<hovered>false</hovered>', '');
        }
        if ((line.match(new RegExp('<description>')) || line.match(new RegExp('<description.+>'))) && !line.endsWith("<descriptions>")) {
          let lineSplit: string[] = line.split('>');
          if (lineSplit.length == 3) {
            outXml += lineSplit[0] + '>\n';
            let lineSplitInt: string[] = lineSplit[1].split('<');
            if (lineSplitInt.length == 2) {
              line = '\t\t\t' + lineSplitInt[0] + '\n\t\t<' + lineSplitInt[1] + '>';
            }
          }
        }

        // Compute the xml
        outXml += line + '\n';
      }
    });
    return outXml;
  }

  //////////////////////////////
  //  -- MULTIPLE OBJECTS --  //
  //    ADD, MOD OR REMOVE    //
  //       FROM TABLE         //
  //         START            //
  //////////////////////////////

  /*
   * TITLE (FIRST FORM)
   *
   * Add a new title to the table (firstForm)
   */
  addTitle() {
    this.titles.push(
      new Title(this.firstFormGroupMultiTitle.get('titleName')?.value,
        this.firstFormGroupMultiTitle.get('titleType')?.value,
        this.firstFormGroupMultiTitle.get('titleLang')?.value));

    // Init form with empty value
    this.initFirstMultiTitleForm();

    // Render rows tables
    this.renderDoiTables('tableTitles');

    // Add value to the fake control for validate the step
    this.firstFormGroup.get('titleName')?.setValue('typed');

    // Close form
    this.isOpenFormTitle = false;
  }

  /**
   * Modify an existing title
   */
  modTitle() {
    let index: number = this.titles.indexOf(this.selectionTitle.selected[0]);
    if (index > -1) {
      this.titles[index] = new Title(this.firstFormGroupMultiTitle.get('titleName')?.value,
        this.firstFormGroupMultiTitle.get('titleType')?.value,
        this.firstFormGroupMultiTitle.get('titleLang')?.value);
    } else {
      this.alertService.error('Titre non trouvé')
    }
    // Refresh the table
    this.renderDoiTables('tableTitles');

    // Init form with empty value
    this.initFirstMultiTitleForm();

    // Hide button
    this.isModifyTitleButton = true;

    // Close form
    this.isOpenFormTitle = false;
  }

  /**
   * Remove a title from the table (firstForm)
   * @param title The title to remove from table
   */
  removeTitle(title: Title) {
    let index = this.titles.indexOf(title);
    if (index > -1) {
      this.titles.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableTitles');
    }
    if (this.selectionTitle.selected[0]) {
      // Init form with empty value
      this.initFirstMultiTitleForm();
    }

    // Open form
    if (this.titles.length == 0) {
      this.isOpenFormTitle = true;
      // Reset the value to the fake control for validate the step
      this.firstFormGroup.get('titleName')?.setValue('');
    }
  }

  /*
   * CREATOR (SECOND FORM)
   *
   * Add a new title to the table (firstForm)
   */
  addCreator() {
    let namesId: NameIdentifier[] = new Array();
    let affiliations: Affiliation[] = new Array();

    // Retrieve the differents typed fields
    for (let index = 0; index < this.nbCreatorNameIdentifiers; index++) {
      namesId.push({
        _value: this.secondFormGroupMultiCreator.get('creatorNameIdentifier_' + index)?.value,
        _attr: {
          nameIdentifierScheme: this.secondFormGroupMultiCreator.get('creatorNameIdentifierScheme_' + index)?.value,
          schemeURI: this.secondFormGroupMultiCreator.get('creatorSchemeURI_' + index)?.value
        }
      });
    }

    // Remove multiple fields
    this.removeMultipleField('creatorNameIdentifier');

    // Retrieve typed affiliations
    for (let index = 0; index < this.nbCreatorAffiliations; index++) {
      affiliations.push({
        _value: this.secondFormGroupMultiCreator.get('creatorAffiliation_' + index)?.value,
        _attr: {
          affiliationIdentifier: this.secondFormGroupMultiCreator.get('creatorAffiliationIdentifier_' + index)?.value,
          affiliationIdentifierScheme: this.secondFormGroupMultiCreator.get('creatorAffiliationIdentifierScheme_' + index)?.value,
          schemeURI: this.secondFormGroupMultiCreator.get('creatorAffiliationSchemeURI_' + index)?.value
        }
      });
    }

    // Remove multiple fields
    this.removeMultipleField('creatorAffiliation');

    // Push the creator with the typed values
    this.creators.push(
      new Creator(this.secondFormGroupMultiCreator.get('creatorName')?.value,
        this.secondFormGroupMultiCreator.get('creatorNameType')?.value ? this.secondFormGroupMultiCreator.get('creatorNameType')?.value : '',
        this.secondFormGroupMultiCreator.get('creatorLang')?.value ? this.secondFormGroupMultiCreator.get('creatorLang')?.value : '',
        this.secondFormGroupMultiCreator.get('creatorGivenName')?.value ? this.secondFormGroupMultiCreator.get('creatorGivenName')?.value : '',
        this.secondFormGroupMultiCreator.get('creatorFamilyName')?.value ? this.secondFormGroupMultiCreator.get('creatorFamilyName')?.value : '',
        namesId, affiliations));

    // Render rows tables
    this.renderDoiTables('tableCreators');

    // Add value to the fake control for validate the step
    this.secondFormGroup.get('creatorName')?.setValue('typed');

    // Init form with empty value
    this.initSecondMultiCreatorForm();

    // Close form
    this.isOpenFormCreator = false;
  }

  /**
   * Remove a creator from the table (firstForm)
   * @param creator The creator to remove from table
   */
  removeCreator(creator: Creator) {
    let index = this.creators.indexOf(creator);
    if (index > -1) {
      this.creators.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableCreators');
    }

    if (this.selectionCreator.selected[0]) {
      // Init form with empty value
      this.initSecondMultiCreatorForm();
    }

    // If there are not creators
    if (this.creators.length == 0) {
      // Reset the value to the fake control for validate the step
      this.secondFormGroup.get('creatorName')?.setValue('');
      // Open form
      this.isOpenFormCreator = true;
    }
  }

  /**
   * Modify an existing creator
   */
  modCreator() {
    let namesId: NameIdentifier[] = new Array();
    let affiliations: Affiliation[] = new Array();

    // Retrieve the differents typed fields
    for (let index = 0; index < this.nbCreatorNameIdentifiers; index++) {
      namesId.push({
        _value: this.secondFormGroupMultiCreator.get('creatorNameIdentifier_' + index)?.value,
        _attr: {
          nameIdentifierScheme: this.secondFormGroupMultiCreator.get('creatorNameIdentifierScheme_' + index)?.value,
          schemeURI: this.secondFormGroupMultiCreator.get('creatorSchemeURI_' + index)?.value
        }
      });
    }

    // Remove multiple fields (we keep the first one (_0)
    this.removeMultipleField('creatorNameIdentifier');

    // Retrieve typed affiliations
    for (let index = 0; index < this.nbCreatorAffiliations; index++) {
      affiliations.push({
        _value: this.secondFormGroupMultiCreator.get('creatorAffiliation_' + index)?.value,
        _attr: {
          affiliationIdentifier: this.secondFormGroupMultiCreator.get('creatorAffiliationIdentifier_' + index)?.value,
          affiliationIdentifierScheme: this.secondFormGroupMultiCreator.get('creatorAffiliationIdentifierScheme_' + index)?.value,
          schemeURI: this.secondFormGroupMultiCreator.get('creatorAffiliationSchemeURI_' + index)?.value
        }
      });
    }

    // Remove multiple fields (we keep the first one (_0)
    this.removeMultipleField('creatorAffiliation');

    let index: number = this.creators.indexOf(this.selectionCreator.selected[0]);
    if (index > -1) {
      this.creators[index] = new Creator(this.secondFormGroupMultiCreator.get('creatorName')?.value,
        this.secondFormGroupMultiCreator.get('creatorNameType')?.value,
        this.secondFormGroupMultiCreator.get('creatorLang')?.value,
        this.secondFormGroupMultiCreator.get('creatorGivenName')?.value,
        this.secondFormGroupMultiCreator.get('creatorFamilyName')?.value,
        namesId, affiliations);
    }
    // Refresh table
    this.renderDoiTables('tableCreators');

    // Init form with empty value
    this.initSecondMultiCreatorForm();

    // Hide button
    this.isModifyCreatorButton = true;

    // Close form
    this.isOpenFormCreator = false;
  }

  /*
   * SUBJECT (FOURTH FORM)
   *
   * Add a new subject to the table (fourth form)
   */
  addSubject() {
    this.subjects.push(new SubjectDoi(
      this.fourthFormGroupSubject.get('subject')?.value,
      this.fourthFormGroupSubject.get('subjectScheme')?.value,
      this.fourthFormGroupSubject.get('subjectSchemeURI')?.value,
      this.fourthFormGroupSubject.get('subjectValueURI')?.value,
      this.fourthFormGroupSubject.get('subjectClassificationCode')?.value,
      this.fourthFormGroupSubject.get('subjectLang')?.value));

    // Init form with empty value
    this.initFourthMultiSubjectForm();

    // Render rows tables
    this.renderDoiTables('tableSubjects');

    // Close form
    this.isOpenFormSubject = false;
  }

  /**
   * Remove a subject from the table (fourth form)
   * @param subject The subject to remove from table
   */
  removeSubject(subject: SubjectDoi) {
    let index = this.subjects.indexOf(subject);
    if (index > -1) {
      this.subjects.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableSubjects');
    }

    if (this.selectionSubject.selected[0]) {
      // Init form with empty value
      this.initFourthMultiSubjectForm();
    }

    // Open form
    if (this.subjects.length == 0) {
      this.isOpenFormSubject = true;
    }
  }

  /**
   * Modify an existing subject
   */
  modSubject() {
    let index: number = this.subjects.indexOf(this.selectionSubject.selected[0]);
    if (index > -1) {
      this.subjects[index] = new SubjectDoi(this.fourthFormGroupSubject.get('subject')?.value,
        this.fourthFormGroupSubject.get('subjectScheme')?.value,
        this.fourthFormGroupSubject.get('subjectSchemeURI')?.value,
        this.fourthFormGroupSubject.get('subjectValueURI')?.value,
        this.fourthFormGroupSubject.get('subjectClassificationCode')?.value,
        this.fourthFormGroupSubject.get('subjectLang')?.value);
    } else {
      this.alertService.error('Subject non trouvé')
    }
    // Refresh the table
    this.renderDoiTables('tableSubjects');

    // Init form with empty value
    this.initFourthMultiSubjectForm();

    // Hide button
    this.isModifySubjectButton = true;

    // Close form
    this.isOpenFormSubject = false;
  }

  /*
   * DATES (FOURTH FORM)
   *
   * Add a new date to the table (fourth form)
   */
  addDate() {
    this.dates.push(new DateDoi(this.fourthFormGroupDate.get('date')?.value,
      this.fourthFormGroupDate.get('dateType')?.value,
      this.fourthFormGroupDate.get('dateInformation')?.value));

    // Init form with empty value
    this.initFourthMultiDateForm();

    // Render rows tables
    this.renderDoiTables('tableDates');

    // Close form
    this.isOpenFormDate = false;
  }

  /**
   * Remove a date from the table (fourth form)
   * @param date The date to remove from table
   */
  removeDate(date: DateDoi) {
    let index = this.dates.indexOf(date);
    if (index > -1) {
      this.dates.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableDates');
    }
    // Reset the form if one is selected
    if (this.selectionDate.selected[0]) {
      // Init form with empty value
      this.initFourthMultiDateForm();
    }

    // Open form
    if (this.dates.length == 0) {
      this.isOpenFormDate = true;
    }
  }

  /**
   * Modify an existing date
   */
  modDate() {
    let index: number = this.dates.indexOf(this.selectionDate.selected[0]);
    if (index > -1) {
      this.dates[index] = new DateDoi(this.fourthFormGroupDate.get('date')?.value,
        this.fourthFormGroupDate.get('dateType')?.value,
        this.fourthFormGroupDate.get('dateInformation')?.value);
    } else {
      this.alertService.error('Date non trouvé')
    }
    // Refresh the table
    this.renderDoiTables('tableDates');

    // Init form with empty value
    this.initFourthMultiDateForm();

    // Hide button
    this.isModifyDateButton = true;

    // Close form
    this.isOpenFormDate = false;
  }

  /*
   * DESCRIPTION (FOURTH FORM)
   *
   * Add a new description to the table (fourth form)
   */
  addDescription() {
    this.descriptions.push(new DescriptionDoi(this.fourthFormGroupDescription.get('description')?.value,
      this.fourthFormGroupDescription.get('descriptionType')?.value,
      this.fourthFormGroupDescription.get('descriptionLang')?.value));

    // Init form with empty value
    this.initFourthMultiDescriptionForm();

    // Render rows tables
    this.renderDoiTables('tableDescriptions');

    // Close form
    this.isOpenFormDescription = false;
  }

  /**
   * Remove a description from the table (fourth form)
   * @param descr The description to remove from table
   */
  removeDescription(descr: DescriptionDoi) {
    let index = this.descriptions.indexOf(descr);
    if (index > -1) {
      this.descriptions.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableDescriptions');
    }
    // Reset the form if one is selected
    if (this.selectionDescription.selected[0]) {
      // Init form with empty value
      this.initFourthMultiDescriptionForm();
    }

    // Open form
    if (this.descriptions.length == 0) {
      this.isOpenFormDescription = true;
    }
  }

  /**
   * Modify an existing date
   */
  modDescription() {
    let index: number = this.descriptions.indexOf(this.selectionDescription.selected[0]);
    if (index > -1) {
      this.descriptions[index] = new DescriptionDoi(this.fourthFormGroupDescription.get('description')?.value,
        this.fourthFormGroupDescription.get('descriptionType')?.value,
        this.fourthFormGroupDescription.get('descriptionLang')?.value);
    } else {
      this.alertService.error('Description non trouvé')
    }
    // Refresh the table
    this.renderDoiTables('tableDescriptions');

    // Init form with empty value
    this.initFourthMultiDescriptionForm();

    // Hide button
    this.isModifyDescriptionButton = true;

    // Close form
    this.isOpenFormDescription = false;
  }

  /*
   * CONTRIBUTOR (FIFTH FORM)
   *
   * Add a new contributor to the table (fifthForm)
   */
  addContributor() {
    let namesId: NameIdentifier[] = new Array();
    let affiliations: Affiliation[] = new Array();

    // Retrieve the differents typed fields
    for (let index = 0; index < this.nbContributorNameIdentifiers; index++) {
      namesId.push(new NameIdentifier(
        this.fifthFormGroupMultiContributor.get('contributorNameIdentifier_' + index)?.value ? this.fifthFormGroupMultiContributor.get('contributorNameIdentifier_' + index)?.value : '',
        this.fifthFormGroupMultiContributor.get('contributorNameIdentifierScheme_' + index)?.value ? this.fifthFormGroupMultiContributor.get('contributorNameIdentifierScheme_' + index)?.value : '',
        this.fifthFormGroupMultiContributor.get('contributorSchemeURI_' + index)?.value ? this.fifthFormGroupMultiContributor.get('contributorSchemeURI_' + index)?.value : ''));
    }
    // Remove multiple fields (we keep the first one (_0)
    this.removeMultipleField('contributorNameIdentifier');

    // Retrieve typed affiliations
    for (let index = 0; index < this.nbContributorAffiliations; index++) {
      affiliations.push(new Affiliation(
        this.fifthFormGroupMultiContributor.get('contributorAffiliation_' + index)?.value ? this.fifthFormGroupMultiContributor.get('contributorAffiliation_' + index)?.value : '',
        this.fifthFormGroupMultiContributor.get('contributorAffiliationIdentifier_' + index)?.value ? this.fifthFormGroupMultiContributor.get('contributorAffiliationIdentifier_' + index)?.value : '',
        this.fifthFormGroupMultiContributor.get('contributorAffiliationIdentifierScheme_' + index)?.value ? this.fifthFormGroupMultiContributor.get('contributorAffiliationIdentifierScheme_' + index)?.value : '',
        this.fifthFormGroupMultiContributor.get('contributorAffiliationSchemeURI_' + index)?.value ? this.fifthFormGroupMultiContributor.get('contributorAffiliationSchemeURI_' + index)?.value : ''));
    }
    // Remove multiple fields (we keep the first one (_0)
    this.removeMultipleField('contributorAffiliation');

    // Push the contributor with the typed values
    this.contributors.push(
      new Contributor(this.fifthFormGroupMultiContributor.get('contributorName')?.value,
        this.fifthFormGroupMultiContributor.get('contributorType')?.value,
        this.fifthFormGroupMultiContributor.get('contributorNameType')?.value,
        this.fifthFormGroupMultiContributor.get('contributorLang')?.value,
        this.fifthFormGroupMultiContributor.get('contributorGivenName')?.value,
        this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.value,
        namesId, affiliations));

    // Init form with empty value
    this.initFifthMultiContributorForm();

    // Render rows tables
    this.renderDoiTables('tableContributors');

    // Close form
    this.isOpenFormContributor = false;
  }

  /**
   * Remove a contributor from the table (fifthForm)
   * @param contributor The contributor to remove from table
   */
  removeContributor(contributor: Contributor) {
    let index = this.contributors.indexOf(contributor);
    if (index > -1) {
      this.contributors.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableContributors');
    }
    // Reset the form if one is selected
    if (this.selectionContributor.selected[0]) {
      // Init form with empty value
      this.initFifthMultiContributorForm();
    }

    // Open form
    if (this.contributors.length == 0) {
      this.isOpenFormContributor = true;
    }
  }

  /**
   * Modify an existing contributor
   */
  modContributor() {
    let namesId: NameIdentifier[] = new Array();
    let affiliations: Affiliation[] = new Array();

    // Retrieve the differents typed fields
    for (let index = 0; index < this.nbContributorNameIdentifiers; index++) {
      namesId.push({
        _value: this.fifthFormGroupMultiContributor.get('contributorNameIdentifier_' + index)?.value,
        _attr: {
          nameIdentifierScheme: this.fifthFormGroupMultiContributor.get('contributorNameIdentifierScheme_' + index)?.value,
          schemeURI: this.fifthFormGroupMultiContributor.get('contributorSchemeURI_' + index)?.value
        }
      });
    }

    // Remove multiple fields (we keep the first one (_0)
    this.removeMultipleField('contributorNameIdentifier');

    // Retrieve typed affiliations
    for (let index = 0; index < this.nbContributorAffiliations; index++) {
      affiliations.push({
        _value: this.fifthFormGroupMultiContributor.get('contributorAffiliation_' + index)?.value,
        _attr: {
          affiliationIdentifier: this.fifthFormGroupMultiContributor.get('contributorAffiliationIdentifier_' + index)?.value,
          affiliationIdentifierScheme: this.fifthFormGroupMultiContributor.get('contributorAffiliationIdentifierScheme_' + index)?.value,
          schemeURI: this.fifthFormGroupMultiContributor.get('contributorAffiliationSchemeURI_' + index)?.value
        }
      });
    }

    // Remove multiple fields (we keep the first one (_0)
    this.removeMultipleField('contributorAffiliation');

    let index: number = this.contributors.indexOf(this.selectionContributor.selected[0]);
    if (index > -1) {
      this.contributors[index] = new Contributor(this.fifthFormGroupMultiContributor.get('contributorName')?.value,
        this.fifthFormGroupMultiContributor.get('contributorType')?.value,
        this.fifthFormGroupMultiContributor.get('contributorNameType')?.value,
        this.fifthFormGroupMultiContributor.get('contributorLang')?.value,
        this.fifthFormGroupMultiContributor.get('contributorGivenName')?.value,
        this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.value,
        namesId, affiliations);
    }
    // Refresh table
    this.renderDoiTables('tableContributors');

    // Init form with empty value
    this.initFifthMultiContributorForm();

    // Hide button
    this.isModifyContributorButton = true;

    // Close form
    this.isOpenFormContributor = false;
  }

  /*
   * RELATED IDENTIFIERS (SIXTH FORM)
   *
   * Add a related identifier to the table (sixth form)
   */
  addRelatedIdentifier() {
    this.relIdentifiers.push(
      new RelatedIdentifier(this.sixthFormGroupMultiRelIdentifier.get('relatedIdentifier')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedResourceTypeGeneral')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedIdentifierType')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedRelationType')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedMetadataScheme')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedSchemeURI')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedSchemeType')?.value));

    // Init form with empty value
    this.initSixthMultiRelIdentifierForm();

    // Render rows tables
    this.renderDoiTables('tableRelatedIdentifiers');

    // Close form
    this.isOpenFormRelatedIdentifier = false;
  }

  /**
   * Remove a related identifier from the table (sixth form)
   * @param relId The related identifier to remove from table
   */
  removeRelatedIdentifier(relId: RelatedIdentifier) {
    let index = this.relIdentifiers.indexOf(relId);
    if (index > -1) {
      this.relIdentifiers.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableRelatedIdentifiers');
    }
    // Reset the form if one is selected
    if (this.selectionRelIdentifier.selected[0]) {
      // Init form with empty value
      this.initSixthMultiRelIdentifierForm();
    }

    // Open form
    if (this.relIdentifiers.length == 0) {
      this.isOpenFormRelatedIdentifier = true;
    }
  }

  /**
   * Modify an existing related identifier
   */
  modRelatedIdentifier() {
    let index: number = this.relIdentifiers.indexOf(this.selectionRelIdentifier.selected[0]);
    if (index > -1) {
      this.relIdentifiers[index] = new RelatedIdentifier(this.sixthFormGroupMultiRelIdentifier.get('relatedIdentifier')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedResourceTypeGeneral')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedIdentifierType')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedRelationType')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedMetadataScheme')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedSchemeURI')?.value,
        this.sixthFormGroupMultiRelIdentifier.get('relatedSchemeType')?.value);
    } else {
      this.alertService.error('Related Identifier non trouvé')
    }
    // Refresh the table
    this.renderDoiTables('tableRelatedIdentifiers');

    // Init form with empty value
    this.initSixthMultiRelIdentifierForm();

    // Hide button
    this.isModifyRelIdentifierButton = true;

    // Close form
    this.isOpenFormRelatedIdentifier = false;
  }

  /*
   * GEO LOCATIONS (SEVENTH FORM)
   *
   * Add a geo location to the table (seventh form)
   */
  addGeoLocation() {
    let geoLocationBox: GeoLocationBox[] = new Array();
    geoLocationBox.push(new GeoLocationBox(
      this.seventhFormGroup.get('northBoundLatitude')?.value,
      this.seventhFormGroup.get('southBoundLatitude')?.value,
      this.seventhFormGroup.get('westBoundLongitude')?.value,
      this.seventhFormGroup.get('eastBoundLongitude')?.value));

    let geoLocationPolygons: GeoLocationPolygon[] = new Array();
    for (let i = 0; i < this.nbGeoLocationPolygon; i++) {
      let geoLocationPolygonPoints: GeoLocationPoint[] = new Array();
      for (let j = 0; j < this.nbGeoLocationPolygonPoints[i]; j++) {
        geoLocationPolygonPoints.push({
          pointLongitude: this.seventhFormGroup.get('polygonPointLongitude_' + i + '_' + j)?.value,
          pointLatitude: this.seventhFormGroup.get('polygonPointLatitude_' + i + '_' + j)?.value,
        });
      }
      let geoLocationInPoint: GeoLocationPoint = new GeoLocationPoint(
        this.seventhFormGroup.get('inPolygonPointLongitude_' + i)?.value,
        this.seventhFormGroup.get('inPolygonPointLatitude_' + i)?.value,
      )
      let geoLocationPolygon: GeoLocationPolygon = new GeoLocationPolygon(geoLocationPolygonPoints, geoLocationInPoint);
      geoLocationPolygons.push(geoLocationPolygon);
    }

    // Remove multiple fields (we keep the first one (_0)
    this.removeMultipleField('geoLocationPolygonPoint');

    this.geoLocations.push({
      geoLocationPlace: this.seventhFormGroup.get('geoLocationPlace')?.value,
      geoLocationPoint: {
        pointLongitude: this.seventhFormGroup.get('pointLongitude')?.value,
        pointLatitude: this.seventhFormGroup.get('pointLatitude')?.value
      },
      geoLocationBox: geoLocationBox,
      geoLocationPolygon: geoLocationPolygons
    });

    // Init form with empty value
    this.initSeventhMultiGeoLocationForm();

    // Render rows tables
    this.renderDoiTables('tableGeoLocations');

    // Close form
    this.isOpenFormGeoLocation = false;
  }

  /**
   * Remove a geo location from the table (seventh form)
   * @param geoLoc The geo location to remove from table
   */
  removeGeoLocation(geoLoc: GeoLocation) {
    let index = this.geoLocations.indexOf(geoLoc);
    if (index > -1) {
      this.geoLocations.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableGeoLocations');
    }
    // Reset the form if one is selected
    if (this.selectionGeoLocation.selected[0]) {
      // Init form with empty value
      this.initSeventhMultiGeoLocationForm();
    }

    // Open form
    if (this.geoLocations.length == 0) {
      this.isOpenFormGeoLocation = true;
    }
  }

  /**
   * Modify an existing geo location
   */
  modGeoLocation() {
    let index: number = this.geoLocations.indexOf(this.selectionGeoLocation.selected[0]);
    if (index > -1) {
      let geoLocationBox: GeoLocationBox[] = new Array();
      geoLocationBox.push(new GeoLocationBox(
        this.seventhFormGroup.get('northBoundLatitude')?.value,
        this.seventhFormGroup.get('southBoundLatitude')?.value,
        this.seventhFormGroup.get('westBoundLongitude')?.value,
        this.seventhFormGroup.get('eastBoundLongitude')?.value));

      let geoLocationPolygons: GeoLocationPolygon[] = new Array();
      for (let i = 0; i < this.nbGeoLocationPolygon; i++) {
        let geoLocationPolygonPoints: GeoLocationPoint[] = new Array();
        for (let j = 0; j < this.nbGeoLocationPolygonPoints[i]; j++) {
          geoLocationPolygonPoints.push({
            pointLongitude: this.seventhFormGroup.get('polygonPointLongitude_' + i + '_' + j)?.value,
            pointLatitude: this.seventhFormGroup.get('polygonPointLatitude_' + i + '_' + j)?.value,
          });
        }
        let geoLocationInPoint: GeoLocationPoint = new GeoLocationPoint(
          this.seventhFormGroup.get('inPolygonPointLongitude_' + i)?.value,
          this.seventhFormGroup.get('inPolygonPointLatitude_' + i)?.value,
        )
        let geoLocationPolygon: GeoLocationPolygon = new GeoLocationPolygon(geoLocationPolygonPoints, geoLocationInPoint);
        geoLocationPolygons.push(geoLocationPolygon);
      }

      // Remove multiple fields (we keep the fourth one (_0 -> _3)
      this.removeMultipleField('geoLocationPolygonPoint');

      this.geoLocations[index] =
        new GeoLocation(this.seventhFormGroup.get('geoLocationPlace')?.value,
          new GeoLocationPoint(this.seventhFormGroup.get('pointLongitude')?.value, this.seventhFormGroup.get('pointLatitude')?.value),
          geoLocationBox,
          geoLocationPolygons);
    } else {
      this.alertService.error('Geo location non trouvé')
    }
    // Refresh the table
    this.renderDoiTables('tableGeoLocations');

    // Init form with empty value
    this.initSeventhMultiGeoLocationForm();

    // Hide button
    this.isModifyGeoLocationButton = true;

    // Close form
    this.isOpenFormGeoLocation = false;
  }

  /*
   * ALTERNATIVE IDENTIFIER (EIGHT FORM)
   *
   * Add an alternative identifier to the table (eight form)
   */
  addAltIdentifier() {
    this.altIdentifiers.push({
      _value: this.eighthFormGroupAltId.get('alternateIdentifier')?.value,
      _attr: {
        alternateIdentifierType: this.eighthFormGroupAltId.get('alternateIdentifierType')?.value
      }
    });

    // Init controls with empty value
    this.eighthFormGroupAltId.get('alternateIdentifier')?.setValue('');
    this.eighthFormGroupAltId.get('alternateIdentifierType')?.setValue('');

    // Render rows tables
    this.renderDoiTables('tableAltIdentifiers');

    // Close form
    this.isOpenFormAlternateIdentifier = false;
  }

  /**
   * Remove an alternative identifier from the table (eight form)
   * @param altIdent The alternative identifier to remove from table
   */
  removeAltIdentifier(altIdent: AltIdentifier) {
    let index = this.altIdentifiers.indexOf(altIdent);
    if (index > -1) {
      this.altIdentifiers.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableAltIdentifiers');
    }
    // Reset the form if one is selected
    if (this.selectionAltIdentifier.selected[0]) {
      // Init controls with empty value
      this.eighthFormGroupAltId.get('alternateIdentifier')?.setValue('');
      this.eighthFormGroupAltId.get('alternateIdentifierType')?.setValue('');
    }

    // Open form
    if (this.altIdentifiers.length == 0) {
      this.isOpenFormAlternateIdentifier = true;
    }
  }

  /**
   * Modify an existing alternative identifier
   */
  modAltIdentifier() {
    let index: number = this.altIdentifiers.indexOf(this.selectionAltIdentifier.selected[0]);
    if (index > -1) {
      this.altIdentifiers[index] = new AltIdentifier(this.eighthFormGroupAltId.get('alternateIdentifier')?.value,
        this.eighthFormGroupAltId.get('alternateIdentifierType')?.value);
    } else {
      this.alertService.error('Alternative Identifier non trouvé')
    }
    // Refresh the table
    this.renderDoiTables('tableAltIdentifiers');

    // Init controls with empty value
    this.eighthFormGroupAltId.get('alternateIdentifier')?.setValue('');
    this.eighthFormGroupAltId.get('alternateIdentifierType')?.setValue('');

    // Hide button
    this.isModifyAltIdentifierButton = true;

    // Close form
    this.isOpenFormAlternateIdentifier = false;
  }

  /*
   * SIZES (EIGHT FORM)
   *
   * Add a size to the table (eight form)
   */
  addSize() {
    this.sizes.push(this.eighthFormGroup.get('size')?.value);

    // Init control with empty value
    this.eighthFormGroup.get('size')?.setValue('');

    // Render rows tables
    this.renderDoiTables('tableSizes');

    // Close form
    this.isOpenFormSize = false;
  }

  /**
   * Remove a size from the table (eight form)
   * @param size The size to remove from table
   */
  removeSize(size: string) {
    let index = this.sizes.indexOf(size);
    if (index > -1) {
      this.sizes.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableSizes');
    }

    // Open form
    if (this.sizes.length == 0) {
      this.isOpenFormSize = true;
    }
  }

  /*
   * FORMATS (EIGHT FORM)
   *
   * Add a format to the table (eight form)
   */
  addFormat() {
    this.formats.push(this.eighthFormGroup.get('format')?.value);

    // Init control with empty value
    this.eighthFormGroup.get('format')?.setValue('');

    // Render rows table
    this.renderDoiTables('tableFormats');

    // Close form
    this.isOpenFormFormat = false;
  }

  /**
   * Remove a format from the table (eight form)
   * @param format The format to remove from table
   */
  removeFormat(format: string) {
    let index = this.formats.indexOf(format);
    if (index > -1) {
      this.formats.splice(index, 1);
      // Render rows table
      this.renderDoiTables('tableFormats');
    }

    // Open form
    if (this.formats.length == 0) {
      this.isOpenFormFormat = true;
    }
  }

  /*
   * RIGHTS (NINTH FORM)
   *
   * Add a right to the table (ninth form)
   */
  addRight() {
    this.rights.push(new Right(
      this.ninthFormGroupRight.get('right')?.value,
      this.ninthFormGroupRight.get('rightURI')?.value ? this.ninthFormGroupRight.get('rightURI')?.value : '',
      this.ninthFormGroupRight.get('rightIdentifier')?.value ? this.ninthFormGroupRight.get('rightIdentifier')?.value : '',
      this.ninthFormGroupRight.get('rightIdentifierScheme')?.value ? this.ninthFormGroupRight.get('rightIdentifierScheme')?.value : '',
      this.ninthFormGroupRight.get('rightSchemeURI')?.value ? this.ninthFormGroupRight.get('rightSchemeURI')?.value : '',
      this.ninthFormGroupRight.get('rightLang')?.value ? this.ninthFormGroupRight.get('rightLang')?.value : ''));

    // Init form with empty value
    this.initNinthMultiRightForm();

    // Render rows tables
    this.renderDoiTables('tableRights');

    // Close form
    this.isOpenFormRight = false;
  }

  /**
   * Remove a right from the table (ninth form)
   * @param right The right to remove from table
   */
  removeRight(right: Right) {
    let index = this.rights.indexOf(right);
    if (index > -1) {
      this.rights.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableRights');
    }
    // Reset the form if one is selected
    if (this.selectionRight.selected[0]) {
      // Init form with empty value
      this.initNinthMultiRightForm();
    }

    // Open form
    if (this.rights.length == 0) {
      this.isOpenFormRight = true;
    }
  }

  /**
   * Modify an existing right
   */
  modRight() {
    let index: number = this.rights.indexOf(this.selectionRight.selected[0]);
    if (index > -1) {
      this.rights[index] = new Right(this.ninthFormGroupRight.get('right')?.value,
        this.ninthFormGroupRight.get('rightURI')?.value,
        this.ninthFormGroupRight.get('rightIdentifier')?.value,
        this.ninthFormGroupRight.get('rightIdentifierScheme')?.value,
        this.ninthFormGroupRight.get('rightSchemeURI')?.value,
        this.ninthFormGroupRight.get('rightLang')?.value);
    } else {
      this.alertService.error('Right non trouvé')
    }
    // Refresh the table
    this.renderDoiTables('tableRights');

    // Init form with empty value
    this.initNinthMultiRightForm();

    // Hide button
    this.isModifyRightButton = true;

    // Close form
    this.isOpenFormRight = false;
  }

  /*
   * FUNDING REFERENCE (NINTH FORM)
   *
   * Add a funding reference to the table (ninth form)
   */
  addFundingRef() {
    this.fundingRefs.push(
      new FundingRef(this.ninthFormGroupFundRef.get('funderName')?.value,
        this.ninthFormGroupFundRef.get('funderAwardTitle')?.value,
        this.ninthFormGroupFundRef.get('funderAwardNumber')?.value,
        this.ninthFormGroupFundRef.get('funderAwardURI')?.value,
        this.ninthFormGroupFundRef.get('funderIdentifier')?.value,
        this.ninthFormGroupFundRef.get('funderIdentifierType')?.value,
        this.ninthFormGroupFundRef.get('funderSchemeURI')?.value));

    this.isFunderIdentifierTypeRequired = false;

    // Init form with empty value
    this.initNinthMultiFundRefForm();

    // Render rows tables
    this.renderDoiTables('tableFundingRefs');

    // Close form
    this.isOpenFormFundingReference = false;
  }

  /**
   * Remove a funding reference from the table (ninth form)
   * @param fundingRef The funding reference to remove from table
   */
  removeFundingRef(fundingRef: FundingRef) {
    let index = this.fundingRefs.indexOf(fundingRef);
    if (index > -1) {
      this.fundingRefs.splice(index, 1);
      // Render rows tables
      this.renderDoiTables('tableFundingRefs');
    }
    // Reset the form if one is selected
    if (this.selectionFundingReference.selected[0]) {
      // Init form with empty value
      this.initNinthMultiFundRefForm();
    }

    // Open form
    if (this.fundingRefs.length == 0) {
      this.isOpenFormFundingReference = true;
    }
  }

  /**
   * Modify an existing funding reference
   */
  modFundingRef() {
    let index: number = this.fundingRefs.indexOf(this.selectionFundingReference.selected[0]);
    if (index > -1) {
      this.fundingRefs[index] = new FundingRef(this.ninthFormGroupFundRef.get('funderName')?.value,
        this.ninthFormGroupFundRef.get('funderIdentifier')?.value,
        this.ninthFormGroupFundRef.get('funderIdentifierType')?.value,
        this.ninthFormGroupFundRef.get('funderSchemeURI')?.value,
        this.ninthFormGroupFundRef.get('funderAwardNumber')?.value,
        this.ninthFormGroupFundRef.get('funderAwardURI')?.value,
        this.ninthFormGroupFundRef.get('funderAwardTitle')?.value);
    } else {
      this.alertService.error('Funding reference non trouvé')
    }
    // Refresh the table
    this.renderDoiTables('tableFundingRefs');

    // Init form with empty value
    this.initNinthMultiFundRefForm();

    // Hide button
    this.isModifyFundingReferenceButton = true;

    // Close form
    this.isOpenFormFundingReference = false;
  }

  /*
   * RELATED ITEMS (TENTH FORM)
   *
   * Add a related item to the table (tenth form)
   */
  addRelatedItem() {
    let creators: RelatedItemCreator[] = new Array();
    let titles: Title[] = new Array();
    let contributors: RelatedItemContributor[] = new Array();

    //
    // Retrieve the differents typed fields
    //
    // Creators
    for (let index = 0; index < this.nbRelatedItemCreators; index++) {
      creators.push(
        new RelatedItemCreator(this.tenthFormGroupMultiRelItems.get('relatedItemCreatorName_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorType_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorLang_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.value));
    }
    // Remove multiple fields (we keep the first one (_0)
    this.removeMultipleField('relatedItemCreator');

    // Titles
    for (let index = 0; index < this.nbRelatedItemTitles; index++) {
      titles.push(new Title(this.tenthFormGroupMultiRelItems.get('relatedItemTitle_' + index)?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemTitleType_' + index)?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemTitleLang_' + index)?.value));
    }
    // Remove multiple fields (we keep the first one (_0)
    this.removeMultipleField('relatedItemTitle');

    // Contributors

    for (let index = 0; index < this.nbRelatedItemContributors; index++) {
      contributors.push(
        new RelatedItemContributor(this.tenthFormGroupMultiRelItems.get('relatedItemContributorName_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorType_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorNameType_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorLang_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.value));
    }
    // Remove multiple fields (we keep the first one (_0)
    this.removeMultipleField('relatedItemContributor');


    // Push the related items with the typed values
    this.relItems.push(
      new RelatedItem(
        new RelatedItemIdentifier(this.tenthFormGroupMultiRelItems.get('relatedItemIdentifier')?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemIdentifierType')?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemMetadataScheme')?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemSchemeURI')?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemSchemeType')?.value),
        this.tenthFormGroupMultiRelItems.get('relatedItemType')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemRelationType')?.value,
        creators,
        titles,
        this.tenthFormGroupMultiRelItems.get('relatedItemPublicationYear')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemVolume')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemIssue')?.value,
        new RelatedItemNumber(this.tenthFormGroupMultiRelItems.get('relatedItemNumber')?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemNumberType')?.value),
        this.tenthFormGroupMultiRelItems.get('relatedItemFirstPage')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemLastPage')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemPublisher')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemEdition')?.value,
        contributors));


    // Init form with empty value
    this.initTenthMultiRelItemForm();

    // Render rows tables
    this.renderDoiTables('tableRelItems');

    // Close form
    this.isOpenFormRelatedItem = false;
  }

  /**
   * Remove a related item from the table (tenth form)
   * @param relItem The related item to remove from table
   */
  removeRelatedItem(relItem: any) {
    let index = this.relItems.indexOf(relItem);
    if (index > -1) {
      this.relItems.splice(index, 1);

      // Reset the form if one is selected
      if (this.selectionRelItem.selected[0]) {
        // Init form with empty value
        this.initTenthMultiRelItemForm();
      }

      // Render rows tables
      this.renderDoiTables('tableRelItems');
    }

    // Open form
    if (this.relItems.length == 0) {
      this.isOpenFormRelatedItem = true;
    }
  }


  /**
   * Modify an existing related item
   */
  modRelatedItem() {
    let index: number = this.relItems.indexOf(this.selectionRelItem.selected[0]);
    if (index > -1) {
      let creators: RelatedItemCreator[] = new Array();
      let titles: Title[] = new Array();
      let contributors: RelatedItemContributor[] = new Array();

      //
      // Retrieve the differents typed fields
      //
      // Creators
      for (let index = 0; index < this.nbRelatedItemCreators; index++) {
        creators.push(
          new RelatedItemCreator(this.tenthFormGroupMultiRelItems.get('relatedItemCreatorName_' + index)?.value,
            this.tenthFormGroupMultiRelItems.get('relatedItemCreatorType_' + index)?.value,
            this.tenthFormGroupMultiRelItems.get('relatedItemCreatorLang_' + index)?.value,
            this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.value,
            this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.value));
      }
      // Remove multiple fields (we keep the first one (_0)
      this.removeMultipleField('relatedItemCreator');

      // Titles
      for (let index = 0; index < this.nbRelatedItemTitles; index++) {
        titles.push(new Title(this.tenthFormGroupMultiRelItems.get('relatedItemTitle_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemTitleType_' + index)?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemTitleLang_' + index)?.value));
      }
      // Remove multiple fields (we keep the first one (_0)
      this.removeMultipleField('relatedItemTitle');

      // Contributors

      for (let index = 0; index < this.nbRelatedItemContributors; index++) {
        contributors.push(
          new RelatedItemContributor(this.tenthFormGroupMultiRelItems.get('relatedItemContributorName_' + index)?.value,
            this.tenthFormGroupMultiRelItems.get('relatedItemContributorType_' + index)?.value,
            this.tenthFormGroupMultiRelItems.get('relatedItemContributorNameType_' + index)?.value,
            this.tenthFormGroupMultiRelItems.get('relatedItemContributorLang_' + index)?.value,
            this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.value,
            this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.value));
      }
      // Remove multiple fields (we keep the first one (_0)
      this.removeMultipleField('relatedItemContributor');


      // Push the related items with the typed values
      this.relItems[index] = new RelatedItem(
        new RelatedItemIdentifier(this.tenthFormGroupMultiRelItems.get('relatedItemIdentifier')?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemIdentifierType')?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemMetadataScheme')?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemSchemeURI')?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemSchemeType')?.value),
        this.tenthFormGroupMultiRelItems.get('relatedItemType')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemRelationType')?.value,
        creators,
        titles,
        this.tenthFormGroupMultiRelItems.get('relatedItemPublicationYear')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemVolume')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemIssue')?.value,
        new RelatedItemNumber(this.tenthFormGroupMultiRelItems.get('relatedItemNumber')?.value,
          this.tenthFormGroupMultiRelItems.get('relatedItemNumberType')?.value),
        this.tenthFormGroupMultiRelItems.get('relatedItemFirstPage')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemLastPage')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemPublisher')?.value,
        this.tenthFormGroupMultiRelItems.get('relatedItemEdition')?.value,
        contributors);
    } else {
      this.alertService.error('Related Item non trouvé')
    }
    // Refresh the table
    this.renderDoiTables('tableRelItems');

    // Init form with empty value
    this.initTenthMultiRelItemForm();

    // Hide button
    this.isModifyRelItemButton = true;

    // Close form
    this.isOpenFormRelatedItem = false;
  }

  //////////////////////////////
  //  -- MULTIPLE OBJECTS --  //
  // ADD OR REMOVE FROM TABLE //
  //          END             //
  //////////////////////////////


  /**
   * Get languages from server
   */
  private getLanguages() {
    // Subscribe to the styles data
    this.citationService.getLanguages().subscribe(
      data => {
        // Sort the data retrieved
        this.languages = data.sort(function (a, b) {
          return a.localeCompare(b);
        });

        // Put fr-FR first
        if (this.languages.indexOf("fr-FR") > -1) {
          this.languages.splice(0, 0, this.languages.splice(this.languages.indexOf("fr-FR"), 1)[0]);
        }
      },
      error => {
        this.alertService.error(error);
      }
    );

    this.citationService.getLanguagesFromServer();
  }

  /**
   * Refresh the table
   * @param table Table to refresh
   */
  private renderDoiTables(table: string) {
    // Render rows tables
    switch (table) {
      case "tableTitles":
        if (this.tableTitles) {
          this.tableTitles.renderRows();
        }
        break;
      case "tableCreators":
        if (this.tableCreators) {
          this.tableCreators.renderRows();
        }
        break;
      case "tableSubjects":
        if (this.tableSubjects) {
          this.tableSubjects.renderRows();
        }
        break;
      case "tableDates":
        if (this.tableDates) {
          this.tableDates.renderRows();
        }
        break;
      case "tableDescriptions":
        if (this.tableDescriptions) {
          this.tableDescriptions.renderRows();
        }
        break;
      case "tableContributors":
        if (this.tableContributors) {
          this.tableContributors.renderRows();
        }
        break;
      case "tableRelatedIdentifiers":
        if (this.tableRelatedIdentifiers) {
          this.tableRelatedIdentifiers.renderRows();
        }
        break;
      case "tableGeoLocations":
        if (this.tableGeoLocations) {
          this.tableGeoLocations.renderRows();
        }
        break;
      case "tableAltIdentifiers":
        if (this.tableAltIdentifiers) {
          this.tableAltIdentifiers.renderRows();
        }
        break;
      case "tableSizes":
        if (this.tableSizes) {
          this.tableSizes.renderRows();
        }
        break;
      case "tableFormats":
        if (this.tableFormats) {
          this.tableFormats.renderRows();
        }
        break;
      case "tableRights":
        if (this.tableRights) {
          this.tableRights.renderRows();
        }
        break;
      case "tableSizes":
        if (this.tableSizes) {
          this.tableSizes.renderRows();
        }
        break;
      case "tableFundingRefs":
        if (this.tableFundingRefs) {
          this.tableFundingRefs.renderRows();
        }
        break;
      case "tableRelItems":
        if (this.tableRelItems) {
          this.tableRelItems.renderRows();
        }
        break;
      default:
        break;
    }
  }

  /**
   * Update the required field if value is types
   * @param event event
   * @param j index in array
   */
  public creatorNameIdentifierChange(event: any, j: number) {
    if (event.target.value.length > 0) {
      this.isCreatorNameIdShemeRequired[j] = true;
    } else {
      this.isCreatorNameIdShemeRequired[j] = false;
    }
  }

  /**
   * Update the required field if value is types
   * @param event event
   * @param j index in array
   */
  public contributorNameIdentifierChange(event: any, j: number) {
    if (event.target.value.length > 0) {
      this.isContributorNameIdShemeRequired[j] = true;
    } else {
      this.isContributorNameIdShemeRequired[j] = false;
    }
  }

  /**
   * Update the required field if value is types
   * @param event event
   */
  public funderIdentifierChange(event: any) {
    if (event.target.value.length > 0) {
      this.isFunderIdentifierTypeRequired = true;
    } else {
      this.isFunderIdentifierTypeRequired = false;
    }
  }

  /**
   * Update the secondFormGroupMultiCreator with the data in the table
   */
  public updateMultiTitleForm() {
    let title: Title = this.selectionTitle.selected[0];

    if (title) {
      // Reset the form
      this.initFirstMultiTitleForm();
      // Add modify button
      this.isModifyTitleButton = false;

      this.firstFormGroupMultiTitle.get('titleName')?.setValue(title._value);
      this.firstFormGroupMultiTitle.get('titleType')?.setValue(title._attr.titleType);
      this.firstFormGroupMultiTitle.get('titleLang')?.setValue(title._attr._doiLang);
      // Open form
      this.isOpenFormTitle = true;
    } else {
      // Hide the button
      this.isModifyTitleButton = true;
      // Reset the form
      this.initFirstMultiTitleForm();
      // Close form
      this.isOpenFormTitle = false;
    }
  }

  /**
   * Update the secondFormGroupMultiCreator with the data in the table
   */
  public updateMultiCreatorForm() {
    let creator: Creator = this.selectionCreator.selected[0];

    if (creator) {
      // Reset the form
      this.initSecondMultiCreatorForm();
      // Reset multiple of fields
      this.removeMultipleField('creatorNameIdentifier');
      this.removeMultipleField('creatorAffiliation');
      // Add modify button
      this.isModifyCreatorButton = false;

      this.secondFormGroupMultiCreator.get('creatorName')?.setValue(creator.creatorName._value);
      this.secondFormGroupMultiCreator.get('creatorNameType')?.setValue(creator.creatorName._attr.nameType);
      this.secondFormGroupMultiCreator.get('creatorLang')?.setValue(creator.creatorName._attr._doiLang);
      this.secondFormGroupMultiCreator.get('creatorGivenName')?.setValue(creator.givenName);
      this.secondFormGroupMultiCreator.get('creatorFamilyName')?.setValue(creator.familyName);
      // Update the form specifically
      this.updateMultiCreatorFormSpec(creator.creatorName._attr.nameType);

      // Name Identifiers
      if (creator.nameIdentifier) {
        for (let index = 0; index < creator.nameIdentifier.length; index++) {
          this.addSubItemOnly('creatorNameIdentifier', index);
          this.secondFormGroupMultiCreator.get('creatorNameIdentifier_' + index)?.setValue(creator.nameIdentifier[index]._value);
          // Update the required parameter for creatorNameIdentifierScheme
          if (creator.nameIdentifier[index]._value.length > 0) {
            this.isCreatorNameIdShemeRequired[index] = true;
          }
          this.secondFormGroupMultiCreator.get('creatorNameIdentifierScheme_' + index)?.setValue(creator.nameIdentifier[index]._attr.nameIdentifierScheme);
          this.secondFormGroupMultiCreator.get('creatorSchemeURI_' + index)?.setValue(creator.nameIdentifier[index]._attr.schemeURI);
        }
      }

      // Affiliations
      if (creator.affiliation) {
        for (let index = 0; index < creator.affiliation.length; index++) {
          this.addSubItemOnly('creatorAffiliation', index);
          this.secondFormGroupMultiCreator.get('creatorAffiliation_' + index)?.setValue(creator.affiliation[index]._value);
          this.secondFormGroupMultiCreator.get('creatorAffiliationIdentifier_' + index)?.setValue(creator.affiliation[index]._attr.affiliationIdentifier);
          this.secondFormGroupMultiCreator.get('creatorAffiliationIdentifierScheme_' + index)?.setValue(creator.affiliation[index]._attr.affiliationIdentifierScheme);
          this.secondFormGroupMultiCreator.get('creatorAffiliationSchemeURI_' + index)?.setValue(creator.affiliation[index]._attr.schemeURI);
        }
      }
      // Open form
      this.isOpenFormCreator = true;
    } else {
      // Hide the button
      this.isModifyCreatorButton = true;

      // Reset the form
      this.initSecondMultiCreatorForm();

      // Reset multiple of fields
      this.removeMultipleField('creatorNameIdentifier');
      this.removeMultipleField('creatorAffiliation');
      // Default required
      this.isCreatorNameIdShemeRequired[0] = false;
      // Close form
      this.isOpenFormCreator = false;
    }
  }

  /**
   * Update specifically the form for the name, given name and family name by the name type
   */
  public updateMultiCreatorFormSpec(value: any) {
    // Update the required status of Given and Family Name fields to true when Type = Personal
    if (this.secondFormGroupMultiCreator.get('creatorNameType')?.value == NameTypesEnum.Personal) {
      // Set required family and given name
      this.isFamilyAndGivenNameRequired = true;
    } else {
        // Set unrequired family and given name
        this.isFamilyAndGivenNameRequired = false;
    }

    if (value && value.type == "input" && value.target && (value.target.value != undefined)) {
      // If the Type is personal, concat Family Name with Given Name for build Creator Name
      if (this.secondFormGroupMultiCreator.get('creatorNameType')?.value == NameTypesEnum.Personal) {
        if (value.target.placeholder == "Given Name") {
          this.secondFormGroupMultiCreator.get('creatorName')?.setValue((this.secondFormGroupMultiCreator.get('creatorFamilyName')?.value != "" || value.target.value != "") ? this.secondFormGroupMultiCreator.get('creatorFamilyName')?.value + ", " + value.target.value : '');
        }
        if (value.target.placeholder == "Family Name") {
          this.secondFormGroupMultiCreator.get('creatorName')?.setValue((value.target.value != "" || this.secondFormGroupMultiCreator.get('creatorGivenName')?.value != "") ? value.target.value + ", " + this.secondFormGroupMultiCreator.get('creatorGivenName')?.value : '');
        }
      }
    } else {
      switch (value) {
        case (NameTypesEnum.Personal):
          this.secondFormGroupMultiCreator.get('creatorName')?.disable({ onlySelf: true });

          this.secondFormGroupMultiCreator.get('creatorGivenName')?.enable({ onlySelf: true });
          this.secondFormGroupMultiCreator.get('creatorGivenName')?.setValidators([Validators.required]);
          this.secondFormGroupMultiCreator.get('creatorGivenName')?.updateValueAndValidity();

          this.secondFormGroupMultiCreator.get('creatorFamilyName')?.enable({ onlySelf: true });
          this.secondFormGroupMultiCreator.get('creatorFamilyName')?.setValidators([Validators.required]);
          this.secondFormGroupMultiCreator.get('creatorFamilyName')?.updateValueAndValidity();
          break;
        case (NameTypesEnum.Organizational):
          this.secondFormGroupMultiCreator.get('creatorName')?.enable({ onlySelf: true });

          this.secondFormGroupMultiCreator.get('creatorGivenName')?.disable({ onlySelf: true });
          this.secondFormGroupMultiCreator.get('creatorGivenName')?.setValue('');
          this.secondFormGroupMultiCreator.get('creatorGivenName')?.setValidators(null);
          this.secondFormGroupMultiCreator.get('creatorGivenName')?.updateValueAndValidity();

          this.secondFormGroupMultiCreator.get('creatorFamilyName')?.disable({ onlySelf: true });
          this.secondFormGroupMultiCreator.get('creatorFamilyName')?.setValue('');
          this.secondFormGroupMultiCreator.get('creatorFamilyName')?.setValidators(null);
          this.secondFormGroupMultiCreator.get('creatorFamilyName')?.updateValueAndValidity();
          break;
        default:
          this.secondFormGroupMultiCreator.get('creatorName')?.enable({ onlySelf: true });

          this.secondFormGroupMultiCreator.get('creatorGivenName')?.enable({ onlySelf: true });
          this.secondFormGroupMultiCreator.get('creatorGivenName')?.setValidators(null);
          this.secondFormGroupMultiCreator.get('creatorGivenName')?.updateValueAndValidity();

          this.secondFormGroupMultiCreator.get('creatorFamilyName')?.enable({ onlySelf: true });
          this.secondFormGroupMultiCreator.get('creatorFamilyName')?.setValidators(null);
          this.secondFormGroupMultiCreator.get('creatorFamilyName')?.updateValueAndValidity();
          break;
      }
    }
  }

  /**
    * Update the fourthFormGroupSubject with the data in the table
    */
  public updateMultiSubjectForm() {
    let subject: SubjectDoi = this.selectionSubject.selected[0];

    if (subject) {
      // Reset the form
      this.initFourthMultiSubjectForm();
      // Add modify button
      this.isModifySubjectButton = false;

      this.fourthFormGroupSubject.get('subject')?.setValue(subject._value);
      this.fourthFormGroupSubject.get('subjectScheme')?.setValue(subject._attr.subjectScheme);
      this.fourthFormGroupSubject.get('subjectSchemeURI')?.setValue(subject._attr.schemeURI);
      this.fourthFormGroupSubject.get('subjectValueURI')?.setValue(subject._attr.valueURI);
      this.fourthFormGroupSubject.get('subjectClassificationCode')?.setValue(subject._attr.classificationCode);
      this.fourthFormGroupSubject.get('subjectLang')?.setValue(subject._attr._doiLang);
      // Open form
      this.isOpenFormSubject = true;
    } else {
      // Hide the button
      this.isModifySubjectButton = true;
      // Reset the form
      this.initFourthMultiSubjectForm();
      // Close form
      this.isOpenFormSubject = false;
    }
  }

  /**
    * Update the fourthFormGroupDate with the data in the table
    */
  public updateMultiDateForm() {
    let date: DateDoi = this.selectionDate.selected[0];

    if (date) {
      // Reset the form
      this.initFourthMultiDateForm();
      // Add modify button
      this.isModifyDateButton = false;

      this.fourthFormGroupDate.get('date')?.setValue(date._value);
      this.fourthFormGroupDate.get('dateType')?.setValue(date._attr.dateType);
      this.fourthFormGroupDate.get('dateInformation')?.setValue(date._attr.dateInformation);
      // Open form
      this.isOpenFormDate = true;
    } else {
      // Hide the button
      this.isModifyDateButton = true;
      // Reset the form
      this.initFourthMultiDateForm();
      // Close form
      this.isOpenFormDate = false;
    }
  }

  /**
    * Update the fourthFormGroupDescription with the data in the table
    */
  public updateMultiDescriptionForm() {
    let description: DescriptionDoi = this.selectionDescription.selected[0];

    if (description) {
      // Reset the form
      this.initFourthMultiDescriptionForm();
      // Add modify button
      this.isModifyDescriptionButton = false;

      this.fourthFormGroupDescription.get('description')?.setValue(description._value);
      this.fourthFormGroupDescription.get('descriptionType')?.setValue(description._attr.descriptionType);
      this.fourthFormGroupDescription.get('descriptionLang')?.setValue(description._attr._doiLang);
      // Open Form
      this.isOpenFormDescription = true;
    } else {
      // Hide the button
      this.isModifyDescriptionButton = true;
      // Reset the form
      this.initFourthMultiDescriptionForm();
      // Close Form
      this.isOpenFormDescription = false;
    }
  }

  /**
   * Update the secondFormGroupMultiCreator with the data in the table
   */
  public updateMultiContributorForm() {
    let contributor: Contributor = this.selectionContributor.selected[0];

    if (contributor) {
      // Reset the form
      this.initFifthMultiContributorForm();
      // Reset multiple of fields
      this.removeMultipleField('contributorNameIdentifier');
      this.removeMultipleField('contributorAffiliation');
      // Add modify button
      this.isModifyContributorButton = false;

      this.fifthFormGroupMultiContributor.get('contributorName')?.setValue(contributor.contributorName._value);
      this.fifthFormGroupMultiContributor.get('contributorNameType')?.setValue(contributor.contributorName._attr.nameType);
      this.fifthFormGroupMultiContributor.get('contributorType')?.setValue(contributor._attr.contributorType);
      this.fifthFormGroupMultiContributor.get('contributorLang')?.setValue(contributor.contributorName._attr._doiLang);
      this.fifthFormGroupMultiContributor.get('contributorGivenName')?.setValue(contributor.givenName);
      this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.setValue(contributor.familyName);

      // Update the form specifically
      this.updateMultiContributorFormSpec(contributor.contributorName._attr.nameType);

      // Name Identifiers
      if (contributor.nameIdentifier) {
        for (let index = 0; index < contributor.nameIdentifier.length; index++) {
          this.addSubItemOnly('contributorNameIdentifier', index);
          this.fifthFormGroupMultiContributor.get('contributorNameIdentifier_' + index)?.setValue(contributor.nameIdentifier[index]._value);
          // Update the required parameter for contributorNameIdentifierScheme
          if (contributor.nameIdentifier[index]._value.length > 0) {
            this.isContributorNameIdShemeRequired[index] = true;
          }
          this.fifthFormGroupMultiContributor.get('contributorNameIdentifierScheme_' + index)?.setValue(contributor.nameIdentifier[index]._attr.nameIdentifierScheme);
          this.fifthFormGroupMultiContributor.get('contributorSchemeURI_' + index)?.setValue(contributor.nameIdentifier[index]._attr.schemeURI);
        }
      }

      // Affiliations
      if (contributor.affiliation) {
        for (let index = 0; index < contributor.affiliation.length; index++) {
          this.addSubItemOnly('contributorAffiliation', index);
          this.fifthFormGroupMultiContributor.get('contributorAffiliation_' + index)?.setValue(contributor.affiliation[index]._value);
          this.fifthFormGroupMultiContributor.get('contributorAffiliationIdentifier_' + index)?.setValue(contributor.affiliation[index]._attr.affiliationIdentifier);
          this.fifthFormGroupMultiContributor.get('contributorAffiliationIdentifierScheme_' + index)?.setValue(contributor.affiliation[index]._attr.affiliationIdentifierScheme);
          this.fifthFormGroupMultiContributor.get('contributorAffiliationSchemeURI_' + index)?.setValue(contributor.affiliation[index]._attr.schemeURI);
        }
      }
      // Open form
      this.isOpenFormContributor = true;
    } else {
      // Hide the button
      this.isModifyContributorButton = true;
      // Reset the form
      this.initFifthMultiContributorForm();
      // Reset multiple of fields
      this.removeMultipleField('contributorNameIdentifier');
      this.removeMultipleField('contributorAffiliation');
      // Default required
      this.isContributorNameIdShemeRequired[0] = false;
      // Close form
      this.isOpenFormContributor = false;
    }
  }

  /**
   * Update specifically the form for the name, given name and family name by the name type
   */
  public updateMultiContributorFormSpec(value: any) {
    if (value && value.type == "input" && value.target && (value.target.value != undefined)) {
      // If the Type is personal, concat Family Name with Given Name for build Creator Name
      if (this.fifthFormGroupMultiContributor.get('contributorNameType')?.value == NameTypesEnum.Personal) {
        if (value.target.placeholder == "Given Name") {
          this.fifthFormGroupMultiContributor.get('contributorName')?.setValue((this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.value != "" || value.target.value != "") ? this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.value + ", " + value.target.value : '');
        }
        if (value.target.placeholder == "Family Name") {
          this.fifthFormGroupMultiContributor.get('contributorName')?.setValue((value.target.value != "" || this.fifthFormGroupMultiContributor.get('contributorGivenName')?.value != "") ? value.target.value + ", " + this.fifthFormGroupMultiContributor.get('contributorGivenName')?.value : '');
        }
      }
    } else {
      switch (value) {
        case (NameTypesEnum.Personal):
          this.fifthFormGroupMultiContributor.get('contributorName')?.disable({ onlySelf: true });

          this.fifthFormGroupMultiContributor.get('contributorGivenName')?.enable({ onlySelf: true });
          this.fifthFormGroupMultiContributor.get('contributorGivenName')?.setValidators([Validators.required]);
          this.fifthFormGroupMultiContributor.get('contributorGivenName')?.updateValueAndValidity();

          this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.enable({ onlySelf: true });
          this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.setValidators([Validators.required]);
          this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.updateValueAndValidity();
          break;
        case (NameTypesEnum.Organizational):
          this.fifthFormGroupMultiContributor.get('contributorName')?.enable({ onlySelf: true });

          this.fifthFormGroupMultiContributor.get('contributorGivenName')?.disable({ onlySelf: true });
          this.fifthFormGroupMultiContributor.get('contributorGivenName')?.setValue('');
          this.fifthFormGroupMultiContributor.get('contributorGivenName')?.setValidators(null);
          this.fifthFormGroupMultiContributor.get('contributorGivenName')?.updateValueAndValidity();

          this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.disable({ onlySelf: true });
          this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.setValue('');
          this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.setValidators(null);
          this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.updateValueAndValidity();
          break;
        default:
          this.fifthFormGroupMultiContributor.get('contributorName')?.enable({ onlySelf: true });

          this.fifthFormGroupMultiContributor.get('contributorGivenName')?.enable({ onlySelf: true });
          this.fifthFormGroupMultiContributor.get('contributorGivenName')?.setValidators(null);
          this.fifthFormGroupMultiContributor.get('contributorGivenName')?.updateValueAndValidity();

          this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.enable({ onlySelf: true });
          this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.setValidators(null);
          this.fifthFormGroupMultiContributor.get('contributorFamilyName')?.updateValueAndValidity();
          break;
      }
    }
  }

  /**
   * Update the sixthFormGroupMultiRelIdentifier with the data in the table
   */
  public updateMultiRelIdentifierForm() {
    let relIdentifier: RelatedIdentifier = this.selectionRelIdentifier.selected[0];
    if (relIdentifier) {
      // Reset the form
      this.initSixthMultiRelIdentifierForm();
      // Add modify button
      this.isModifyRelIdentifierButton = false;
      // Add data into form controls
      this.sixthFormGroupMultiRelIdentifier.get('relatedIdentifier')?.setValue(relIdentifier._value);
      this.sixthFormGroupMultiRelIdentifier.get('relatedResourceTypeGeneral')?.setValue(relIdentifier._attr.resourceTypeGeneral);
      this.sixthFormGroupMultiRelIdentifier.get('relatedIdentifierType')?.setValue(relIdentifier._attr.relatedIdentifierType);
      this.sixthFormGroupMultiRelIdentifier.get('relatedRelationType')?.setValue(relIdentifier._attr.relationType);
      this.sixthFormGroupMultiRelIdentifier.get('relatedMetadataScheme')?.setValue(relIdentifier._attr.relatedMetadataScheme);
      this.sixthFormGroupMultiRelIdentifier.get('relatedSchemeURI')?.setValue(relIdentifier._attr.schemeURI);
      this.sixthFormGroupMultiRelIdentifier.get('relatedSchemeType')?.setValue(relIdentifier._attr.schemeType);
      // Open form
      this.isOpenFormRelatedIdentifier = true;
    } else {
      // Hide the button
      this.isModifyRelIdentifierButton = true;
      // Reset the form
      this.initSixthMultiRelIdentifierForm();
      // Close form
      this.isOpenFormRelatedIdentifier = false;
    }
  }

  /**
   * Update the sixthFormGroupMultiRelIdentifier with the data in the table
   */
  public updateMultiGeoLocationForm() {
    let geoLoc: GeoLocation = this.selectionGeoLocation.selected[0];
    if (geoLoc) {
      // Reset the form
      this.initSeventhMultiGeoLocationForm();
      // Add modify button
      this.isModifyGeoLocationButton = false;

      // Add data into form controls
      this.seventhFormGroup.get('geoLocationPlace')?.setValue(geoLoc.geoLocationPlace);
      this.seventhFormGroup.get('pointLongitude')?.setValue(geoLoc.geoLocationPoint.pointLongitude);
      this.seventhFormGroup.get('pointLatitude')?.setValue(geoLoc.geoLocationPoint.pointLatitude);

      // Multiple Geo location Box
      if (geoLoc.geoLocationBox && geoLoc.geoLocationBox[0]) {
        this.seventhFormGroup.get('northBoundLatitude')?.setValue(geoLoc.geoLocationBox[0].northBoundLatitude);
        this.seventhFormGroup.get('southBoundLatitude')?.setValue(geoLoc.geoLocationBox[0].southBoundLatitude);
        this.seventhFormGroup.get('westBoundLongitude')?.setValue(geoLoc.geoLocationBox[0].westBoundLongitude);
        this.seventhFormGroup.get('eastBoundLongitude')?.setValue(geoLoc.geoLocationBox[0].eastBoundLongitude);
      }

      // Multiple Geo location polygon
      if (geoLoc.geoLocationPolygon && geoLoc.geoLocationPolygon[0]) {
        for (let i = 0; i < geoLoc.geoLocationPolygon.length; i++) {
          if (geoLoc.geoLocationPolygon[i].polygonPoint.length > 3) {
            this.nbGeoLocationPolygon++;
            this.nbGeoLocationPolygonPoints[i] = (geoLoc.geoLocationPolygon[i].polygonPoint.length);

            for (let j = 0; j < geoLoc.geoLocationPolygon[i].polygonPoint.length; j++) {
              // Add formControl if not exist
              if (!this.seventhFormGroup.contains('polygonPointLongitude_' + i + '_' + j) && !this.seventhFormGroup.contains('polygonPointLatitude_' + i + '_' + j)) {
                this.seventhFormGroup.addControl('polygonPointLongitude_' + i + '_' + j, new FormControl('', [Validators.pattern(this.longitudeRegex)]));
                this.seventhFormGroup.addControl('polygonPointLatitude_' + i + '_' + j, new FormControl('', [Validators.pattern(this.latitudeRegex)]));
              }
              // Set value to formControl
              this.seventhFormGroup.get('polygonPointLongitude_' + i + '_' + j)?.setValue(geoLoc.geoLocationPolygon[i].polygonPoint[j].pointLongitude);
              this.seventhFormGroup.get('polygonPointLatitude_' + i + '_' + j)?.setValue(geoLoc.geoLocationPolygon[i].polygonPoint[j].pointLatitude);
            }
            // Add formControl if not exist
            if (!this.seventhFormGroup.contains('inPolygonPointLongitude_' + i) && !this.seventhFormGroup.contains('inPolygonPointLatitude_' + i)) {
              this.seventhFormGroup.addControl('inPolygonPointLongitude_' + i, new FormControl('', [Validators.pattern(this.longitudeRegex)]));
              this.seventhFormGroup.addControl('inPolygonPointLatitude_' + i, new FormControl('', [Validators.pattern(this.latitudeRegex)]));
            }
            // Set value to formControl
            this.seventhFormGroup.get('inPolygonPointLongitude_' + i)?.setValue(geoLoc.geoLocationPolygon[i].inPolygonPoint.pointLongitude);
            this.seventhFormGroup.get('inPolygonPointLatitude_' + i)?.setValue(geoLoc.geoLocationPolygon[i].inPolygonPoint.pointLatitude);
          }
        }
      }
      // Open form
      this.isOpenFormGeoLocation = true;
    } else {
      // Hide the button
      this.isModifyGeoLocationButton = true;

      // Reset multiple of fields
      this.removeMultipleField('geoLocationPolygonPoint');
      // Reset the form
      this.initSeventhMultiGeoLocationForm();
      // Close form
      this.isOpenFormGeoLocation = false;
    }
  }

  /**
   * Update the eighthFormGroupAltId with the data in the table
   */
  public updateAltIdentifierForm() {
    let altIdentifier: AltIdentifier = this.selectionAltIdentifier.selected[0];
    if (altIdentifier) {
      // Reset the form
      this.initEighthMultiAltIdForm();
      // Add modify button
      this.isModifyAltIdentifierButton = false;
      // Add data into form controls
      this.eighthFormGroupAltId.get('alternateIdentifier')?.setValue(altIdentifier._value);
      this.eighthFormGroupAltId.get('alternateIdentifierType')?.setValue(altIdentifier._attr.alternateIdentifierType);
      // Open form
      this.isOpenFormAlternateIdentifier = true;
    } else {
      // Hide the button
      this.isModifyAltIdentifierButton = true;
      // Reset the form
      this.initEighthMultiAltIdForm();
      // Close form
      this.isOpenFormAlternateIdentifier = false;
    }
  }

  /**
   * Update the ninthFormGroupRight with the data in the table
   */
  public updateRightForm() {
    let right: Right = this.selectionRight.selected[0];
    if (right) {
      // Reset the form
      this.initNinthMultiRightForm();
      // Add modify button
      this.isModifyRightButton = false;
      // Add data into form controls
      this.ninthFormGroupRight.get('right')?.setValue(right._value);
      this.ninthFormGroupRight.get('rightURI')?.setValue(right._attr.rightsURI);
      this.ninthFormGroupRight.get('rightIdentifier')?.setValue(right._attr.rightsIdentifier);
      this.ninthFormGroupRight.get('rightIdentifierScheme')?.setValue(right._attr.rightsIdentifierScheme);
      this.ninthFormGroupRight.get('rightSchemeURI')?.setValue(right._attr.schemeURI);
      this.ninthFormGroupRight.get('rightLang')?.setValue(right._attr._doiLang);
      // Open form
      this.isOpenFormRight = true;
    } else {
      // Hide the button
      this.isModifyRightButton = true;
      // Reset the form
      this.initNinthMultiRightForm();
      // Close form
      this.isOpenFormRight = false;
    }
  }

  /**
   * Update the ninthFormGroupFundRef with the data in the table
   */
  public updateFundingRefForm() {
    let fundRef: FundingRef = this.selectionFundingReference.selected[0];
    if (fundRef) {
      // Reset the form
      this.initNinthMultiFundRefForm();
      // Add modify button
      this.isModifyFundingReferenceButton = false;
      // Add data into form controls
      this.ninthFormGroupFundRef.get('funderName')?.setValue(fundRef.funderName);
      this.ninthFormGroupFundRef.get('funderIdentifier')?.setValue(fundRef.funderIdentifier._value);
      this.ninthFormGroupFundRef.get('funderIdentifierType')?.setValue(fundRef.funderIdentifier._attr.funderIdentifierType);
      this.ninthFormGroupFundRef.get('funderSchemeURI')?.setValue(fundRef.funderIdentifier._attr.schemeURI);
      this.ninthFormGroupFundRef.get('funderAwardNumber')?.setValue(fundRef.awardNumber._value);
      this.ninthFormGroupFundRef.get('funderAwardURI')?.setValue(fundRef.awardNumber._attr.awardURI);
      this.ninthFormGroupFundRef.get('funderAwardTitle')?.setValue(fundRef.awardTitle);
      // Open form
      this.isOpenFormFundingReference = true;
    } else {
      // Hide the button
      this.isModifyFundingReferenceButton = true;
      // Reset the form
      this.initNinthMultiFundRefForm();
      // Default required field
      this.isFunderIdentifierTypeRequired = false;
      // Close form
      this.isOpenFormFundingReference = false;
    }
  }


  /**
   * Update the ninthFormGroupFundRef with the data in the table
   */
  public updateRelatedItemForm() {
    let relItem: RelatedItem = this.selectionRelItem.selected[0];
    if (relItem) {
      // Reset the form
      this.initTenthMultiRelItemForm();

      // Add modify button
      this.isModifyRelItemButton = false;

      // Add data into form controls
      this.tenthFormGroupMultiRelItems.get('relatedItemIdentifier')?.setValue(relItem.relatedItemIdentifier._value);
      this.tenthFormGroupMultiRelItems.get('relatedItemIdentifierType')?.setValue(relItem.relatedItemIdentifier._attr.relatedItemIdentifierType);
      this.tenthFormGroupMultiRelItems.get('relatedItemMetadataScheme')?.setValue(relItem.relatedItemIdentifier._attr.relatedMetadataScheme);
      this.tenthFormGroupMultiRelItems.get('relatedItemSchemeURI')?.setValue(relItem.relatedItemIdentifier._attr.schemeURI);
      this.tenthFormGroupMultiRelItems.get('relatedItemSchemeType')?.setValue(relItem.relatedItemIdentifier._attr.schemeType);
      this.tenthFormGroupMultiRelItems.get('relatedItemPublicationYear')?.setValue(relItem.publicationYear);
      this.tenthFormGroupMultiRelItems.get('relatedItemVolume')?.setValue(relItem.volume);
      this.tenthFormGroupMultiRelItems.get('relatedItemIssue')?.setValue(relItem.issue);
      this.tenthFormGroupMultiRelItems.get('relatedItemNumber')?.setValue((relItem.number && relItem.number._value != undefined) ? relItem.number._value : '');
      this.tenthFormGroupMultiRelItems.get('relatedItemNumberType')?.setValue((relItem.number && relItem.number._attr.numberType != undefined) ? relItem.number._attr.numberType : '');
      this.tenthFormGroupMultiRelItems.get('relatedItemFirstPage')?.setValue(relItem.firstPage);
      this.tenthFormGroupMultiRelItems.get('relatedItemLastPage')?.setValue(relItem.lastPage);
      this.tenthFormGroupMultiRelItems.get('relatedItemPublisher')?.setValue(relItem.publisher);
      this.tenthFormGroupMultiRelItems.get('relatedItemEdition')?.setValue(relItem.edition);
      this.tenthFormGroupMultiRelItems.get('relatedItemType')?.setValue(relItem._attr.relatedItemType);
      this.tenthFormGroupMultiRelItems.get('relatedItemRelationType')?.setValue(relItem._attr.relationType);

      // Multiple Related Item Titles
      if (relItem.titles && relItem.titles.title) {
        for (let index = 0; index < relItem.titles.title.length; index++) {
          let title = relItem.titles.title[index];
          this.addSubItemOnly('relatedItemTitle', index);
          this.tenthFormGroupMultiRelItems.get('relatedItemTitle_' + index)?.setValue(title._value);
          this.tenthFormGroupMultiRelItems.get('relatedItemTitleType_' + index)?.setValue(title._attr.titleType);
          this.tenthFormGroupMultiRelItems.get('relatedItemTitleLang_' + index)?.setValue(title._attr._doiLang);
        }
      }

      // Multiple Related Item Creators
      if (relItem.creators && relItem.creators.creator) {
        for (let index = 0; index < relItem.creators.creator.length; index++) {
          let creator = relItem.creators.creator[index];
          this.addSubItemOnly('relatedItemCreator', index);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorName_' + index)?.setValue(creator.creatorName._value);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorType_' + index)?.setValue(creator.creatorName._attr.nameType);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorLang_' + index)?.setValue(creator.creatorName._attr._doiLang);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.setValue(creator.givenName);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.setValue(creator.familyName);
          this.updateRelatedItemFormCreatorSpec(creator.creatorName._attr.nameType, index);
        }
      }
      // Multiple Related Item Contributors
      if (relItem.contributors && relItem.contributors.contributor) {
        for (let index = 0; index < relItem.contributors.contributor.length; index++) {
          let contributor = relItem.contributors.contributor[index];
          this.addSubItemOnly('relatedItemContributor', index);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorName_' + index)?.setValue(contributor.contributorName._value);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorNameType_' + index)?.setValue(contributor.contributorName._attr.nameType);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorLang_' + index)?.setValue(contributor.contributorName._attr._doiLang);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.setValue(contributor.givenName);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.setValue(contributor.familyName);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorType_' + index)?.setValue(contributor._attr.contributorType);
          this.updateRelatedItemFormContributorSpec(contributor.contributorName._attr.nameType, index);
        }
      }
      // Open form
      this.isOpenFormRelatedItem = true;
    } else {
      // Remove multiple fields
      this.removeMultipleField('relatedItemTitle');
      this.removeMultipleField('relatedItemCreator');
      this.removeMultipleField('relatedItemContributor');

      // Hide the button
      this.isModifyRelItemButton = true;
      // Reset the form
      this.initTenthMultiRelItemForm();
      // Close form
      this.isOpenFormRelatedItem = false;
    }
  }

  /**
   * Update specifically the form for the name, given name and family name by the name type
   */
  public updateRelatedItemFormCreatorSpec(value: any, index: number) {
    if (value && value.type == "input" && value.target && (value.target.value != undefined)) {
      // If the Type is personal, concat Family Name with Given Name for build Creator Name
      if (this.tenthFormGroupMultiRelItems.get('relatedItemCreatorType_' + index)?.value == NameTypesEnum.Personal) {
        if (value.target.placeholder == "Related Item Creator Given Name") {
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorName_' + index)?.setValue((this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.value != "" || value.target.value != "") ? this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.value + ", " + value.target.value : '');
        }
        if (value.target.placeholder == "Related Item Creator Family Name") {
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorName_' + index)?.setValue((value.target.value != "" || this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.value != "") ? value.target.value + ", " + this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.value : '');
        }
      }
    } else {
      switch (value) {
        case (NameTypesEnum.Personal):
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorName_' + index)?.disable({ onlySelf: true });

          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.enable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.setValidators([Validators.required]);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.updateValueAndValidity();

          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.enable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.setValidators([Validators.required]);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.updateValueAndValidity();
          break;
        case (NameTypesEnum.Organizational):
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorName_' + index)?.enable({ onlySelf: true });

          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.disable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.setValue('');
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.setValidators(null);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.updateValueAndValidity();

          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.disable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.setValue('');
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.setValidators(null);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.updateValueAndValidity();
          break;
        default:
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorName_' + index)?.enable({ onlySelf: true });

          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.enable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.setValidators(null);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorGivenName_' + index)?.updateValueAndValidity();

          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.enable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.setValidators(null);
          this.tenthFormGroupMultiRelItems.get('relatedItemCreatorFamilyName_' + index)?.updateValueAndValidity();
          break;
      }
    }
  }

  /**
   * Update specifically the form for the name, given name and family name by the name type
   */
  public updateRelatedItemFormContributorSpec(value: any, index: number) {
    if (value && value.type == "input" && value.target && (value.target.value != undefined)) {
      // If the Type is personal, concat Family Name with Given Name for build Creator Name
      if (this.tenthFormGroupMultiRelItems.get('relatedItemContributorNameType_' + index)?.value == NameTypesEnum.Personal) {
        if (value.target.placeholder == "Related Item Contributor Given Name") {
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorName_' + index)?.setValue((this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.value != "" || value.target.value != "") ? this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.value + ", " + value.target.value : '');
        }
        if (value.target.placeholder == "Related Item Contributor Family Name") {
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorName_' + index)?.setValue((value.target.value != "" || this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.value != "") ? value.target.value + ", " + this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.value : '');
        }
      }
    } else {
      switch (value) {
        case (NameTypesEnum.Personal):
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorName_' + index)?.disable({ onlySelf: true });

          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.enable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.setValidators([Validators.required]);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.updateValueAndValidity();

          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.enable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.setValidators([Validators.required]);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.updateValueAndValidity();
          break;
        case (NameTypesEnum.Organizational):
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorName_' + index)?.enable({ onlySelf: true });

          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.disable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.setValue('');
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.setValidators(null);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.updateValueAndValidity();

          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.disable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.setValue('');
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.setValidators(null);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.updateValueAndValidity();
          break;
        default:
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorName_' + index)?.enable({ onlySelf: true });

          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.enable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.setValidators(null);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorGivenName_' + index)?.updateValueAndValidity();

          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.enable({ onlySelf: true });
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.setValidators(null);
          this.tenthFormGroupMultiRelItems.get('relatedItemContributorFamilyName_' + index)?.updateValueAndValidity();
          break;
      }
    }
  }

  /**
   * GENERIC FUNCTION
   * Reset multiple field
   * @param item
   */
  private removeMultipleField(item: string) {
    switch (item) {
      case "creatorNameIdentifier":
        for (let index = 0; index < this.nbCreatorNameIdentifiers; index++) {
          this.secondFormGroupMultiCreator.removeControl('creatorNameIdentifier_' + index);
          this.secondFormGroupMultiCreator.removeControl('creatorNameIdentifierScheme_' + index);
          this.secondFormGroupMultiCreator.removeControl('creatorSchemeURI_' + index);
        }
        this.nbCreatorNameIdentifiers = 0;
        break;
      case "creatorAffiliation":
        for (let index = 0; index < this.nbCreatorAffiliations; index++) {
          this.secondFormGroupMultiCreator.removeControl('creatorAffiliation_' + index);
          this.secondFormGroupMultiCreator.removeControl('creatorAffiliationIdentifier_' + index);
          this.secondFormGroupMultiCreator.removeControl('creatorAffiliationIdentifierScheme_' + index);
          this.secondFormGroupMultiCreator.removeControl('creatorAffiliationSchemeURI_' + index);
        }
        this.nbCreatorAffiliations = 0;
        break;
      case "contributorNameIdentifier":
        for (let index = 0; index < this.nbContributorNameIdentifiers; index++) {
          this.fifthFormGroupMultiContributor.removeControl('contributorNameIdentifier_' + index);
          this.fifthFormGroupMultiContributor.removeControl('contributorNameIdentifierScheme_' + index);
          this.fifthFormGroupMultiContributor.removeControl('contributorSchemeURI_' + index);
        }
        this.nbContributorNameIdentifiers = 0;
        break;
      case "contributorAffiliation":
        for (let index = 0; index < this.nbContributorAffiliations; index++) {
          this.fifthFormGroupMultiContributor.removeControl('contributorAffiliation_' + index);
          this.fifthFormGroupMultiContributor.removeControl('contributorAffiliationIdentifier_' + index);
          this.fifthFormGroupMultiContributor.removeControl('contributorAffiliationIdentifierScheme_' + index);
          this.fifthFormGroupMultiContributor.removeControl('contributorAffiliationSchemeURI_' + index);
        }
        this.nbContributorAffiliations = 0;
        break;
      case "geoLocationPolygonPoint":
        for (let i = 0; i < this.nbGeoLocationPolygon; i++) {
          for (let j = 0; j < this.nbGeoLocationPolygonPoints[i]; j++) {
            this.seventhFormGroup.removeControl('polygonPointLongitude_' + i + '_' + j);
            this.seventhFormGroup.removeControl('polygonPointLatitude_' + i + '_' + j);
          }
          this.nbGeoLocationPolygonPoints[i] = 0;
          this.seventhFormGroup.removeControl('inPolygonPointLongitude_' + i);
          this.seventhFormGroup.removeControl('inPolygonPointLatitude_' + i);
        }
        this.nbGeoLocationPolygon = 0;
        break;
      case "relatedItemTitle":
        for (let index = 0; index < this.nbRelatedItemTitles; index++) {
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemTitle_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemTitleType_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemTitleLang_' + index);
        }
        this.nbRelatedItemTitles = 0;
        break;
      case "relatedItemCreator":
        for (let index = 0; index < this.nbRelatedItemCreators; index++) {
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemCreatorName_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemCreatorType_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemCreatorLang_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemCreatorGivenName_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemCreatorFamilyName_' + index);
        }
        this.nbRelatedItemCreators = 0;
        break;
      case "relatedItemContributor":
        for (let index = 0; index < this.nbRelatedItemContributors; index++) {
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemContributorName_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemContributorNameType_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemContributorLang_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemContributorType_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemContributorGivenName_' + index);
          this.tenthFormGroupMultiRelItems.removeControl('relatedItemContributorFamilyName_' + index);
        }
        this.nbRelatedItemContributors = 0;
        break;
      default: ;
    };
  }

  /**
   * Validators error messages
   */
  input_validation_messages = {
    'empty': [
      { type: 'required', message: 'Vous devez entrer une valeur' }
    ],
    'doi_identifier': [
      { type: 'required', message: 'Vous devez entrer une valeur' },
      { type: 'pattern', message: 'Identifiant saisie existe déjà' }
    ],
    'uri': [
      { type: 'pattern', message: 'Le format attendu est une URI' }
    ],
    'url': [
      { type: 'required', message: 'Vous devez entrer une valeur' },
      { type: 'pattern', message: 'Le format attendu est une URL' }
    ],
    'publicationYear': [
      { type: 'required', message: 'Vous devez entrer une valeur' },
      { type: 'pattern', message: 'Le format attendu est sur 4 chiffres' }
    ],
    'longitude': [
      { type: 'pattern', message: 'Entrez une valeur comprise entre -180 et 180' }
    ],
    'latitude': [
      { type: 'pattern', message: 'Entrez une valeur comprise entre -90 et 90' }
    ],
    'date': [
      { type: 'required', message: 'Vous devez entrer une valeur' },
      { type: 'pattern', message: 'Le format attendu est une date : YYYY, YYYY-MM, YYYY-MM-DD, YYYY-MM-DDThh:mmTZD, YYYY-MM-DDThh:mm:ssTZD, YYYY-MM-DDThh:mm:ss.sTZD' }
    ],
  }

  /**
   * Displays error message for URI's subElements
   * @returns
   */
  public getErrorMessage(item: string): string {
    let text: string = '';
    switch (item) {
      case 'uri': text = 'Le format attendu est une URI';
        break;
      case 'longitude': text = 'Entrez une valeur comprise entre -180 et 180';
        break;
      case 'latitude': text = 'Entrez une valeur comprise entre -90 et 90';
        break;
      case 'required': text = 'Vous devez entrer une valeur';
        break;
      default: text = 'Valeur nulle ou erronée';
    }
    return text;
  }

  /**
   * Get all DOIs identifier from project
   */
  public getAllAvailableDOIsFromProject() {
    // Subscribe to the dois data
    this.adminService.getProjectDois().subscribe(
      data => {
        // Sort the data retrieved
        this.existingDois = data.sort(function (a, b) {
          return a.localeCompare(b);
        });
      },
      error => {
        this.alertService.error(error);
      }
    );
    this.adminService.getProjectDoisFromServer(this.projectService.getSelectedProject().suffix);
  }

  public doiIdentifierValidator(): ValidatorFn {

    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;

      if (!value) {
        return null;
      }

      // Update the identifier before the test
      this.updateDoiIdentifier();

      let res: boolean = false;
      if ((this.route.snapshot.paramMap.get('action') != Action.edit) && this.existingDois.includes(this.endFormGroup.get("identifier")?.value.toUpperCase())) {
        res = true;
      } else {
        res = false;
      }
      return res ? { pattern: true } : null;
    }
  }

  /**
   * Get all projects from server for user
   */
  getUserProjects() {
    // Get the user and call the service
    const user = this.cookieService.get('currentUser');
    if (user) {
      this.adminService.getUserProjectsFromServer(user).subscribe(
        data => {
          this.projects = new Array();
          data.forEach(x => {
            this.projects.push(x.suffix.toString());
          });
        },
        error => {
          if (error.statusText == "Unknown Error") {
            this.alertService.error("Une erreur est survenue durant la récupération des projets du compte " + user + ", impossible de contacter le serveur");
          } else {
            this.alertService.error("Une erreur est survenue durant la récupération des projets du compte " + user + ' : ' + error.statusText);
          }
        }
      );
    }
  }
}

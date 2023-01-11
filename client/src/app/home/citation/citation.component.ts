import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { CookieService } from 'ngx-cookie-service';
import { AdminService } from 'src/app/services/admin.service';
import { AlertService } from 'src/app/services/alert.service';
import { CitationService } from 'src/app/services/citation.service';
import { ProjectService } from 'src/app/services/project.service';

@Component({
  selector: 'app-citation',
  templateUrl: './citation.component.html',
  styleUrls: ['./citation.component.css']
})
export class CitationComponent implements OnInit {

  citationForm!: FormGroup;

  public selectedDoi: string;
  public styles: Array<string>;
  public selectedStyle: string;
  public languages: Array<string>;
  public selectedLanguage: string;

  // The selected project from html for keep it selected
  selection = new SelectionModel<any>(false, []);

  constructor(public fb: FormBuilder,
    private adminService: AdminService,
    private projectService: ProjectService,
    private citationService: CitationService,
    private cookieService: CookieService,
    private alertService: AlertService) {
    this.selectedDoi = "";
    this.styles = new Array();
    this.selectedStyle = "";
    this.languages = new Array();
    this.selectedLanguage = "";
  }

  ngOnInit(): void {
    this.alertService.clear();

    // Init form
    this.citationForm = this.fb.group({
      selectedDoi: new FormControl('', Validators.required),
      styles: new FormControl('', Validators.required),
      languages: new FormControl('', Validators.required),
      strCitation: new FormControl('')
    });

    // Init doi value from cookie
    if (this.cookieService.get('selectedDoi') != null) {
      this.citationForm.get('selectedDoi')?.setValue(this.cookieService.get('selectedDoi'));
      this.selectedDoi = this.cookieService.get('selectedDoi')!;
    }else{
      this.citationForm.get('selectedDoi')?.setValue('');
      this.selectedDoi = '';
    };

    // Get the INIST code configurated
    this.citationForm.get('doi_prefix')?.setValue(this.cookieService.get('inistCode') + "/");

    // Get the selected project at init
    this.citationForm.get('doi_project')?.setValue(this.projectService.getSelectedProject().suffix.toString() + "/");

    this.getProjectDois(this.projectService.getSelectedProject().suffix);

    this.getCitationStyles();
    this.getCitationLanguages();
  }

  /**
   * Get Project Dois
   * @param projectSuffix 
   */
  private getProjectDois(projectSuffix: number) {
    // and call the web service
    this.adminService.getProjectDoisFromServer(projectSuffix);
  }

  /**
   * Get citation styles from server
   */
  private getCitationStyles() {
    // Subscribe to the styles data
    let sub = this.citationService.getStyles().subscribe(
      data => {
        // Sort the data retrieved
        this.styles = data.sort(function (a, b) {
          return a.localeCompare(b);
        });

        let exist: any = this.styles.find(x => x == 'bibtex');
        if (exist != undefined) {
          this.selectedStyle = exist;
        } else {
          this.selectedStyle = this.styles[0];
        }

        sub.unsubscribe();
      },
      error => {
        this.alertService.error(error);
      }
    );

    this.citationService.getStylesFromServer();
  }

  /**
   * Get citation languages from server
   */
  private getCitationLanguages() {
    // Subscribe to the styles data
    let sub = this.citationService.getLanguages().subscribe(
      data => {
        // Sort the data retrieved
        this.languages = data.sort(function (a, b) {
          return a.localeCompare(b);
        });

        let exist: any = this.languages.find(x => x == 'fr-FR');
        if (exist != undefined) {
          this.selectedLanguage = exist;
        } else {
          this.selectedLanguage = this.languages[0];
        }

        sub.unsubscribe();
      },
      error => {
        this.alertService.error(error);
      }
    );

    this.citationService.getLanguagesFromServer();
  }

  /**
   * Update the selected style
   * @param event 
   */
  public setSelectedStyle(event: any) {
    this.selectedStyle = event.value;
    this.alertService.clear();
  }

  /**
   * Update the selected language
   * @param event 
   */
  public setSelectedLanguage(event: any) {
    this.selectedLanguage = event.value;
    this.alertService.clear();
  }

  /**
   * Generate the citation
   */
  public generateCitation() {
    this.alertService.clear();
    // Subscribe to the citation data
    this.citationService.getCitationFromServer(this.selectedDoi, this.selectedLanguage, this.selectedStyle).subscribe(
      data => {},
      error => {
        if (error.status == "200") {
          this.alertService.success("Citation générée");
          this.citationForm.get("strCitation")?.setValue(error.error.text);
        } else if (error.statusText == "Not Found") {
          this.alertService.error("Une erreur est survenue, le DOI n'a pas été trouvé");
        } else if (error.statusText == "Unknown Error") {
          this.alertService.error("La génération de la citation a échouée, impossible de contacter le serveur");
        } else {
          this.alertService.error("Une erreur est survenue durant la demande de génération de la citation");
        }
      }
    );
  }
}

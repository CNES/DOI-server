import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { SelectionModel } from '@angular/cdk/collections';
import { MatDialog } from '@angular/material/dialog';
import { CookieService } from 'ngx-cookie-service';
import { Project } from 'src/app/models/projects.model';
import { AdminService } from 'src/app/services/admin.service';
import { AlertService } from 'src/app/services/alert.service';
import { ProjectService } from 'src/app/services/project.service';
import { DoiService } from 'src/app/services/doi.service';
import { DeactivateDboxComponent } from './dialog-box/deactivate-dbox/deactivate-dbox.component'
import { AuthService } from 'src/app/services/auth.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  // form groups
  dashboardFormGroup!: FormGroup;
  // projects
  public projects: Project[];
  public seletectedProject: Project;
  // dois
  public dois: any[];
  public doisFiltered: any[];
  public selectedDoi: string;

  // The selected project from html for keep it selected
  selection = new SelectionModel<any>(false, []);

  // Doi Filter
  public isDoiFilter: boolean;
  public doiFilter: string;

  panelOpenState = false;

  // Constructor
  constructor(
    public fb: FormBuilder,
    private adminService: AdminService,
    private projectService: ProjectService,
    private alertService: AlertService,
    private doiService: DoiService,
    private authService: AuthService,
    private cookieService: CookieService,
    private dialog: MatDialog) {
    this.projects = new Array();
    this.seletectedProject = { suffix: 0, projectname: '' };
    this.dois = new Array();
    this.doisFiltered = new Array();
    this.selectedDoi = "";
    this.isDoiFilter = false;
    this.doiFilter = "";
  }

  // Init
  ngOnInit(): void {
    this.alertService.clear();

    this.dashboardFormGroup = this.fb.group({
      dois: new FormControl('', Validators.required),
      doiFilter: new FormControl('', Validators.required)
    });
    this.getUserProjects();
  }

  /**
   * Get all projects from server
   */
  getUserProjects() {
    // Get the user and call the service
    const user = this.cookieService.get('currentUser');
    if (user) {
      this.adminService.getUserProjectsFromServer(user).subscribe(
        data => {
          // No project, add info message
          if (data.length == 0) {
            this.alertService.info("Aucun projet associé à votre compte");
          } else {
            // Sort the data retrieved
            this.projects = data.sort(function (a, b) {
              return a.projectname.localeCompare(b.projectname);
            });

            // Manage selected project
            const selProject = this.projectService.getSelectedProject();
            if (selProject.suffix != 0) {
              this.seletectedProject = this.projects.filter(x => x.suffix == selProject.suffix && x.projectname == selProject.projectname)[0];
              if (this.seletectedProject == undefined) {
                this.seletectedProject = this.projects[0];
                this.projectService.setSelectedProject(this.projects[0]);
              }
            } else {
              if (this.projects.length > 0) {
                this.seletectedProject = this.projects[0];
                this.projectService.setSelectedProject(this.projects[0]);
              }
            }
            // Update DOIs list of selected project
            this.setSelectedProject(this.seletectedProject);
          }
        },
        error => {
          if (error.statusText == "Unknown Error") {
            this.alertService.error("Une erreur est survenue durant la récupération des projets du compte " + user + ", impossible de contacter le serveur");
          } else {
            this.alertService.error("Une erreur est survenue durant la récupération des projets du compte " + user + ' : ' + error.statusText);
          }
        }
      );
    } else {
      this.authService.logout();
    }
  }

  /**
   * Update the selected project
   * @param value : current project
   */
  public setSelectedProject(value: any) {

    this.seletectedProject = value;
    this.projectService.setSelectedProject(value);

    // Subscribe to the dois data
    this.adminService.getProjectDois().subscribe(
      data => {
        // Sort the data retrieved
        this.dois = data.sort(function (a, b) {
          return a.localeCompare(b);
        });
        this.selectedDoi = this.dois[0];
        this.selection.select(this.selectedDoi);
        this.doisFiltered = this.dois;
        // Update DOI list
        this.updateDoiList();
      },
      error => {
        this.alertService.error(error);
      }

    );

    this.getProjectDois(this.projectService.getSelectedProject().suffix);
  }

  /**
   * Update the selected DOI
   * @param doi 
   */
  public setSelectedDoi(doi: any) {
    this.selectedDoi = doi;
    this.cookieService.set('selectedDoi', this.selectedDoi);
    this.alertService.clear();
  }

  /**
   * Get DOIs from project
   * @param projectSuffix 
   */
  private getProjectDois(projectSuffix: number) {
    // and call the web service
    this.adminService.getProjectDoisFromServer(projectSuffix);
  }

  /**
   * Set isDoiFilter alternatively true or false
   */
  public setDoiFilter() {
    if (this.isDoiFilter) {
      this.isDoiFilter = false;
    } else {
      this.isDoiFilter = true;
    }
  }

  /**
   * Update DOIs list to match with doiFilter
   */
  public updateDoiList() {
    this.doiFilter = this.dashboardFormGroup.get('doiFilter')?.value;
    this.doisFiltered = this.dois;
    this.doisFiltered = this.doisFiltered.filter(doi => (doi.toLowerCase()).match(this.doiFilter.toLowerCase()));
  }

  /**
   * Action on click of deactivate button
   * @param event 
   */
  public deactivateDoi(event: any) {

    let confirmation: boolean = false;

    const dialogRef = this.dialog.open(DeactivateDboxComponent, {
      width: '600px',
      data: { doi: event, confirmation: confirmation }
    });

    dialogRef.afterClosed().subscribe(result => {
      confirmation = result;

      // Call doiService for launch the deasctivate request
      this.doiService.deactivateDoi(this.seletectedProject.suffix.toString(), event).subscribe(
        data => {

        },
        error => {
          if (error.status == "200") {
            //this.alerts.push({type: 'success', message: 'Le doi ' + event + ' a été desactivé'});
            this.alertService.success("Le doi " + event + " a été desactivé");
            // Refresh the project list
            this.getProjectDois(this.seletectedProject.suffix);
          } else if (error.statusText == "Unknown Error") {
            this.alertService.error("Une erreur est survenue durant la désactivation du DOI " + event + ", impossible de contacter le serveur");
          }
        });
    });
  }
}

import { SelectionModel } from '@angular/cdk/collections';
import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Project, Person, ActionsEnum } from '../../models';
import { AdminService } from 'src/app/services/admin.service';
import { AlertService } from 'src/app/services/alert.service';
import { AddAdminDBoxComponent } from './dialog-box/add-admin-dbox.component';
import { RemoveProjectDBoxComponent } from './dialog-box/remove-project-dbox.component';
import { RemoveAdminDBoxComponent } from './dialog-box/remove-admin-dbox.component';
import { RenameProjectDBoxComponent } from './dialog-box/rename-project-dbox.component';
import { AddProjectDBoxComponent } from './dialog-box/add-project-dbox.component';
import { RemoveProjectUserDBoxComponent } from './dialog-box/remove-project-user-dbox.component';
import { AddProjectUserDBoxComponent } from './dialog-box/add-project-user-dbox.component';
import { CookieService } from 'ngx-cookie-service';

@Component({
  selector: 'app-administration',
  templateUrl: './administration.component.html',
  styleUrls: ['./administration.component.css']
})
export class AdministrationComponent implements OnInit {

  // Header of the first table : project table
  displayedProjectsColumns: string[] = ['Nom', 'Suffixe'];
  // Header of the second and third table : users table
  displayedPersonColumns: string[] = ['Nom'];

  // The array of all users of DOI
  public users: Person[];

  // PROJECT
  //
  // The array of all project
  public projects: Project[]

  // The selected project from html for keep it selected
  selectionProject = new SelectionModel<Project>(false, []);

  // PROJECT USERS
  //
  // The user of the selected project
  public usersFromSelectedProject: Person[];

  // The selected project user from html for keep it selected
  selectionProjectUser = new SelectionModel<any>(false, []);

  // The selected project in the mat-select for user configuration
  public seletectedProject: Project;

  // The boolean that mean all users from DOI are in the group (deactivate the add button)
  public noUserNotInSelectedproject: boolean

  // ADMIN USER
  //
  // The array of admin users
  public admins: Person[];

  // The selected admin user from html for keep it selected
  selectionAdminUser = new SelectionModel<any>(false, []);

  // The boolean that mean the are no any users not admin (deactivate the add button)
  public noUserNotAdmin: boolean
  public adminSelectedHimself: boolean;

  // The action enumerate
  private actionsEnum = ActionsEnum;

  constructor(private adminService: AdminService,
    private alertService: AlertService,
    private cookieService: CookieService,
    public dialog: MatDialog) {
    // Init variables
    this.projects = new Array();
    this.usersFromSelectedProject = new Array();
    this.seletectedProject = { suffix: 0, projectname: "" };
    this.admins = new Array();
    this.users = new Array();
    this.noUserNotAdmin = false;
    this.noUserNotInSelectedproject = false;
    this.adminSelectedHimself = false;
  }

  ngOnInit(): void {
    this.alertService.clear();

    // Get all users from server
    this.getUsers();

    // Get all projets from server
    this.getProjects();

    // Get all administrators from server
    this.getAdministrators();
  }

  /**
   * Open the dialog box for add a new project
   */
  openAddProjectDialog() {
    const dialogRef = this.dialog.open(AddProjectDBoxComponent, {
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.event == this.actionsEnum[this.actionsEnum.addProject]) {
        // On submit call the function that make the request on server
        this.createNewProject(result.data);
      }
    });
  }

  /**
   * Open the dialog box for rename an existing project
   * @param proj : the project to rename
   */
  openRenameProjectDialog() {
    // Save the current project name before the value is overwritten
    let oldProjectName = this.selectionProject.selected[0].projectname;

    const dialogRef = this.dialog.open(RenameProjectDBoxComponent, {
      width: '600px',
      data: this.selectionProject.selected[0]
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.event == this.actionsEnum[this.actionsEnum.renameProject]) {
        // On submit call the function that make the request on server
        this.renameProject(oldProjectName, result.data);
      }
    });
  }

  /**
   * Open the dialog box for confirmation for remove an existing project
   * @param proj : the project to rename
   */
  openRemoveProjectDialog() {
    const dialogRef = this.dialog.open(RemoveProjectDBoxComponent, {
      width: '600px',
      data: this.selectionProject.selected[0]
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.event == this.actionsEnum[this.actionsEnum.removeProject]) {
        // On submit call the function that make the request on server
        this.removeProject(result.data);
      }
    });
  }

  /**
   * Open the dialog box for add a new user on an existing project
   */
  openAddUserToProjectDialog() {
    // Filter on users there are not admin
    let personsNotInProj: Person[] = new Array();
    this.users.forEach(user => {
      let found: boolean = false;
      this.usersFromSelectedProject.forEach(userAlreadyInProj => {
        if (user.name == userAlreadyInProj.name) {
          found = true;
        }
      });
      if (!found) {
        personsNotInProj.push(user);
      }
    });
    personsNotInProj.sort();

    const dialogRef = this.dialog.open(AddProjectUserDBoxComponent, {
      width: '600px',
      data: {
        project: this.seletectedProject,
        users: personsNotInProj
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.event == this.actionsEnum[this.actionsEnum.addProjectUser]) {
        // On submit call the function that make the request on server
        this.addProjectUser(result.data.name);
      }
    });
  }

  /**
   * Open the dialog box for remove an user on an existing project
   * @param user : the user to remove from project
   */
  openRemoveProjectUserDialog() {
    const dialogRef = this.dialog.open(RemoveProjectUserDBoxComponent, {
      width: '500px',
      data: {
        user: this.selectionProjectUser.selected[0],
        project: this.seletectedProject
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.event == this.actionsEnum[this.actionsEnum.removeProjectUser]) {
        // On submit call the function that make the request on server
        this.removeProjectUser(result.data.name);
      }
    });
  }

  /**
   * Open the dialog box for add a new user on administrator group
   */
  openAddAdminDialog() {
    // Filter on users there are not admin
    let personsNotAdmin: Person[] = new Array();
    this.users.forEach(user => {
      let found: boolean = false;
      this.admins.forEach(admin => {
        if (user.name == admin.name) {
          found = true;
        }
      });
      if (!found) {
        personsNotAdmin.push(user);
      }
    });
    personsNotAdmin.sort();

    const dialogRef = this.dialog.open(AddAdminDBoxComponent, {
      width: '600px',
      data: personsNotAdmin
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.event == this.actionsEnum[this.actionsEnum.addAdmin]) {
        // On submit call the function that make the request on server
        this.addAdministrator(result.data.name);
      }
    });
  }

  /**
   * Open the dialog box for remove an user on administrator group
   * @param user : the user to remove from administrator group
   */
  openRemoveAdminDialog() {
    const dialogRef = this.dialog.open(RemoveAdminDBoxComponent, {
      width: '550px',
      data: this.selectionAdminUser.selected[0]
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.event == this.actionsEnum[this.actionsEnum.removeAdmin]) {
        // On submit call the function that make the request on server
        this.removeAdministrator(result.data.name);
      }
    });
  }

  /**
   * Function for html tha update the selected project if it's change and get the users from it
   * @param proj : the new selected project
   */
  public selectProject(event: any) {
    this.seletectedProject = event.value;
    this.getProjectUsers();
  }

  /**
   * Function for retrieve all the administrators from server
   */
  private getAdministrators() {
    // Subscribe to the data
    this.adminService.getAdminUsersFromServer().subscribe(
      data => {
        // Clear administrators
        this.selectionAdminUser.clear();
        let sAdmins = data.sort(function (a, b) {
          return a.localeCompare(b);
        });;
        this.admins = new Array();
        sAdmins.forEach(admin => {
          this.admins.push({
            name: admin
          });
        });

        // Compute if any users are not admin for disable the add button
        if (this.admins.length == this.users.length) {
          this.noUserNotAdmin = true;
        } else {
          this.noUserNotAdmin = false;
        }

      },
      error => {
        if (error.statusText == "Unknown Error") {
          this.alertService.clear();
          this.alertService.error("Une erreur est survenue durant la récupération des administrateurs, impossible de contacter le serveur");
        } else {
          this.alertService.clear();
          this.alertService.error("Une erreur est survenue durant la récupération des administrateurs : " + error.statusText);
        }
      }
    );
  }

  /**
   * Add one user to administator group
   * @param name The person username
   */
  private addAdministrator(name: string) {
    this.adminService.addAdminUser(name).subscribe(
      data => {
        this.alertService.clear();
        this.alertService.success("L'utilisateur " + name + " a été ajouté aux administrateur de DOI");

        // Get administrators for update the list
        this.getAdministrators();
      }, error => {
        this.alertService.clear();
        this.alertService.error("Une erreur est survenue durant l'ajout aux administrateur de  " + name);
      }
    );
  }

  /**
   * Delete one user from the administrator group
   * @param name The person username
   */
  private removeAdministrator(name: string) {
    this.adminService.removeAdminUser(name).subscribe(
      data => {
        this.alertService.clear();
        this.alertService.success("L'utilisateur " + name + " a été supprimé des administrateurs de DOI");

        // Get administrators for update the list
        this.getAdministrators();
      }, error => {
        this.alertService.clear();
        this.alertService.error("Une erreur est survenue durant la suppression des administrateurs de  " + name);
      }
    );
  }

  /**
   * Update the boolean, that the admin selected is the current logged in user
   * @param admin the admin selected
   */
  public updateAdminBooleans(admin: any) {
    if (this.selectionAdminUser.selected[0]) {
      if (this.selectionAdminUser.selected[0].name == this.cookieService.get('currentUser')) {
        this.adminSelectedHimself = true;
      } else {
        this.adminSelectedHimself = false;
      }
    } else {
      this.adminSelectedHimself = false;
    }
  }

  /**
   * Function for retrieve all the users from server
   */
  private getUsers() {
    // Subscribe to the data
    this.adminService.getUsersFromServer().subscribe(
      data => {
        
        let sUsers = data.sort(function (a, b) {
          return a.localeCompare(b);
        });
        sUsers.forEach(user => {
          this.users.push({
            name: user
          });
        });
      },
      error => {
        if (error.statusText == "Unknown Error") {
          this.alertService.clear();
          this.alertService.error("Une erreur est survenue durant la récupération des utilisateurs, impossible de contacter le serveur");
        } else {
          this.alertService.clear();
          this.alertService.error("Une erreur est survenue durant la récupération des utilisateurs : " + error.statusText);
        }
      }
    );
  }

  /**
   * Function for retrieve all the users on a project from server
   */
  private getProjectUsers() {
    // Subscribe to the data
    this.adminService.getProjectUsersFromServer(this.seletectedProject.suffix).subscribe(
      data => {
        // Clear project users
        this.selectionProjectUser.clear();
        let sUsersFromSelectedProject = data.sort(function (a, b) {
          return a.localeCompare(b);
        });
        this.usersFromSelectedProject = new Array();
        sUsersFromSelectedProject.forEach(user => {
          this.usersFromSelectedProject.push({
            name: user
          });
        });

        // Compute if any users are not admin for disable the add button
        if (this.users.length == this.usersFromSelectedProject.length) {
          this.noUserNotInSelectedproject = true;
        } else {
          this.noUserNotInSelectedproject = false;
        }
      },
      error => {
        if (error.statusText == "Unknown Error") {
          this.alertService.clear();
          this.alertService.error("Une erreur est survenue durant la récupération des utilisateurs du projet " + this.seletectedProject.projectname + ", impossible de contacter le serveur");
        } else {
          this.alertService.clear();
          this.alertService.error("Une erreur est survenue durant la récupération des utilisateurs du projet " + this.seletectedProject.projectname + " : " + error.statusText);
        }
      }
    );
  }

  /**
   * Function for add a new user to the selected project on server
   * @param user : the user to add on project
   */
  private addProjectUser(user: string) {
    // Get the selected project
    let project = this.seletectedProject;

    this.adminService.addprojectUser(project.suffix, user).subscribe(
      data => {
        this.alertService.clear();
        this.alertService.success("Utilisateur " + user + " ajouté au project " + project.projectname);
        // Refresh the admin projects list
        this.getProjectUsers();

        // Update the project list
        this.adminService.getUserProjectsFromServer(user);
      }, error => {
        this.alertService.clear();
        this.alertService.error(error);
        this.alertService.error("Une erreur est survenue durant l'ajout de l'utilisateur " + user + "au project " + project.projectname);
      }
    );
  }

  /**
   * Function for remove an user to project to server
   * @param user : user to remove
   */
  private removeProjectUser(user: string) {
    // Get the selected project
    let project = this.seletectedProject;

    this.adminService.removeProjectUser(project.suffix, user).subscribe(
      data => {
        this.alertService.clear();
        this.alertService.success("Utilisateur " + user + " supprimé du project " + project.projectname);
        // Refresh the admin projects list
        this.getProjectUsers();

        // Update the user projects list
        this.adminService.getUserProjectsFromServer(user);
      }, error => {
        this.alertService.clear();
        this.alertService.error(error);
        this.alertService.error("Une erreur est survenue durant la suppression de l'utilisateur " + user + "au project " + project.projectname);
      }
    );
  }

  /**
   * Function for retrieve all the project from server
   */
  private getProjects() {

    // Subscribe to the data
    this.adminService.getProjectsFromServer().subscribe(
      data => {
        // Clear projects
        this.selectionProject.clear();
        // Sort the data retrieved
        this.projects = data.sort(function (a, b) {
          return a.projectname.localeCompare(b.projectname);
        });
        if (this.projects.length > 0) {
          this.seletectedProject = this.projects[0];
          this.getProjectUsers();
        }
      },
      error => {
        if (error.statusText == "Unknown Error") {
          this.alertService.clear();
          this.alertService.error("Une erreur est survenue durant la récupération des projets, impossible de contacter le serveur");
        } else {
          this.alertService.clear();
          this.alertService.error("Une erreur est survenue durant la récupération des projets : " + error.statusText);
        }
      }
    );
  }

  /**
   * Function for create a new project
   * @param name The name of the new project to create
   */
  private createNewProject(name: string) {
    this.adminService.addProject(name).subscribe(
      data => {
        this.alertService.clear();
        this.alertService.success("Projet " + name + " créé");
        // Refresh the project list
        this.getProjects();
      }, error => {
        this.alertService.clear();
        this.alertService.error(error);
        this.alertService.error("Une erreur est survenue durant la création du projet " + name);
      }
    );
  }

  /**
   * Function for modify the name of a project
   * @param oldProjectName the old project name
   * @param proj The project to rename
   */
  private renameProject(oldProjectName: string, project: Project) {

    this.adminService.renameProject(project.suffix, project.projectname).subscribe(
      data => {
        this.alertService.clear();
        this.alertService.success("Le projet " + oldProjectName + " a été renommé en " + project.projectname);

        // Refresh the project list
        this.getProjects();
      }, error => {
        this.alertService.clear();
        this.alertService.error("Une erreur est survenue durant le renommage du projet " + oldProjectName);
      }
    );
  }

  /**
   * Function for delete a project
   * @param proj project to delete
   */
  private removeProject(proj: Project) {
    this.adminService.deleteProject(proj.suffix).subscribe(
      data => {
        this.alertService.clear();
        this.alertService.success("Le projet " + proj.projectname + " a été supprimé");

        // Refresh the project list
        this.getProjects();
      }, error => {
        this.alertService.clear();
        if (error.status == 404) {
          this.alertService.warning("Au moins un DOI est associé au projet " + proj.projectname + ", le projet ne peut être supprimé.");
        } else {
          this.alertService.error("Une erreur est survenue durant la suppression du projet " + proj.projectname);
        }
      });
  }
}

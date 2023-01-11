import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { AdminService } from '../services/admin.service';
import { AlertService } from '../services/alert.service';
import { AuthService } from '../services/auth.service';
import { DoiService } from '../services/doi.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  public formGroup!: FormGroup;
  private isAdmin: boolean;

  constructor(private authService: AuthService,
    private router: Router,
    private alertService: AlertService,
    private adminService: AdminService,
    private doiService: DoiService,
    private cookieService: CookieService) {
    this.isAdmin = false;
  }

  ngOnInit(): void {
    this.initForm();

    let errorLogout: string = this.cookieService.get('errorLogout');
    if (errorLogout) {
      this.alertService.error(errorLogout);
    }
    this.cookieService.deleteAll();
  }

  /**
   * Initialize the form
   */
  initForm() {
    this.formGroup = new FormGroup({
      login: new FormControl('', [Validators.required]),
      password: new FormControl('', [Validators.required])
    });
  }

  /**
   * Login method for authenticate users from server
   */
  loginProcess() {
    // Clear the alert component
    this.alertService.clear();

    if (this.formGroup.valid) {
      this.authService.login(this.formGroup.value['login'], this.formGroup.value['password'])
        .subscribe(
          (data: string) => {
            this.cookieService.set('currentUserToken', data);
            this.cookieService.set('currentUser', this.formGroup.value['login']);

            // Retrieve INIST CODE, it will call isAdminUser, and isUserHaveProjects
            this.getInistCode();
          },
          error => {
            if (error == undefined) {
              this.alertService.error("Utilisateur et/ou mot de passe erroné");
            } else {
              if (error.statusText == "Unknown Error") {
                this.alertService.error("Authentification échouée, impossible de contacter le serveur");
              } else if (error.statusText == "Unauthorized") {
                this.alertService.error("Utilisateur et/ou mot de passe erroné");
              }
            }
          }
        );
    }
  }

  /**
   * Check if the connected user have project or
   * is administrator for route do correct page
   * If not, display message on login page
   */
  private isUserHaveProjects() {
    const user = this.cookieService.get('currentUser');
    if (user) {
      this.adminService.getUserProjectsFromServer(user).subscribe(
        data => {
          // If almost one project, go to dashboard
          if (data.length > 0) {
            this.router.navigate(['/dashboard']);
            // If the are any project and user is admin, go administration
          } else if (data.length == 0 && this.isAdmin) {
            this.router.navigate(['/administration']);
            // Else, cannot be connected
          } else {
            this.alertService.error("Utilisateur sans aucun projet associé");
            this.cookieService.deleteAll();
          }
        },
        error => {
          this.alertService.error("Erreur lors de la récupération des projets de l'utilisateur.");
        }
      );
    }
  }

  /**
   * Check if the connected user is administrator
   */
  private isAdminUser() {
    // Subscribe to the data
    this.adminService.isAdminFromServer().subscribe(
      data => {
        this.isAdmin = true;
        this.cookieService.set('isAdmin', 'true');
        this.isUserHaveProjects();
      },
      error => {
        if (error.statusText == "Forbidden") {
          this.isAdmin = false;
          this.cookieService.set('isAdmin', 'false');
          this.isUserHaveProjects();
        } else {
          this.alertService.error("Erreur lors de la récupération des rôles de l'utilisateur")
        }
      }
    );
  }

  private getInistCode() {
    // Subscribe to the data
    this.doiService.getInistCode().subscribe(
      data => {
        this.cookieService.set('inistCode', data);
        this.isAdminUser();
      },
      error => {
        this.alertService.error("Erreur lors de la récupération du code INIST depuis le serveur")
        this.cookieService.delete('inistCode');
      }
    );
  }
}

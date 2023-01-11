import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { ConfigService } from '../services/config.service';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';

@Injectable({ providedIn: 'root' })
export class AuthService {
    constructor(
        private router: Router,
        private http: HttpClient,
        private configService: ConfigService,
        private cookieService: CookieService) { }

    /**
     * Login to DOI client
     * @param username
     * @param password
     * @returns the token
     */
    public login(username: string, password: string): Observable<string> {
        // Remove user from local storage
        this.cookieService.deleteAll();

        // Build the header with the user credential
        const httpOptions = {
            headers: new HttpHeaders({
                'Authorization': 'Basic ' + btoa(username + ':' + password)
            })
        };

        // Call API RESTLET
        return this.http.post<string>(this.configService.apiBaseUrl + '/admin/token', null, httpOptions);
    }

    /**
     * Logout from DOI client
     * Erase the token and username informations
     * Route do login page
     */
    public logout() {
        // If token in cookie, call logout API
        if (this.cookieService.get('currentUserToken') != "") {
            // Build header
            let option = {
                headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded'),
                params: new HttpParams().set('token', this.cookieService.get('currentUserToken'))
            };

            this.http.delete<any>(this.configService.apiBaseUrl + '/admin/token', option).subscribe(
                data => {
                    this.cookieService.delete('errorLogout');
                    this.router.navigate(['/login']);
                },
                error => {
                    this.cookieService.set("errorLogout", "Une erreur s'est produite durant la déconnexion");
                    this.router.navigate(['/login']);
                }
            );
        } else {
            this.cookieService.deleteAll();
            this.router.navigate(['/login']);
        }
    }

    /** Get the token from API */
    public getToken() {
        return this.http.post<string>(this.configService.apiBaseUrl + '/admin/token', null);
    }
}
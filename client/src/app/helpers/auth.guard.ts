import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

    /**
     * Create an instace of AuthGuard
     */
    constructor(private router: Router,
        private cookieService: CookieService) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        if (this.cookieService.get('currentUserToken') != null) {
            // logged in so return true
            return true;
        }
        else {
            // not logged in so redirect to login page
            this.router.navigate(['/login']);
            return false;
        }
    }
}
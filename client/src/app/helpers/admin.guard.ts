import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';

@Injectable({ providedIn: 'root' })
export class AdminGuard implements CanActivate {

    /**
     * Create an instace of AdminGuard
     */
    constructor(private router: Router,
        private cookieService: CookieService) { }

    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        let isAdmin: any = this.cookieService.get('isAdmin');
        if (isAdmin && isAdmin == 'true') {
            // ok so return true
            return true;
        } else {
          // not logged in so redirect to login page
          this.router.navigate(['/dashboard']);
          return false;
        }
    }
}
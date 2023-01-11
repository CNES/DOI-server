import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';

@Injectable()
export class BasicAuthInterceptor implements HttpInterceptor {
    constructor(private cookieService: CookieService) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        // Add header with basic auth credentials if user is logged in and request is to the api url
        const token = this.cookieService.get('currentUserToken');
        if (token) {
            request = request.clone({
                setHeaders: {
                    'Authorization': `Bearer ` + token
                }
            });
        }
        return next.handle(request);
    }
}
import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
    constructor(private router: Router) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(catchError(err => {
            let error: any;
            switch (err.status) {
                case 401:
                    // auto logout if 401 response returned from api
                    this.router.navigate(['/login']);
                    break;
                case 500:
                    if (err.error.includes('Sorry, an error has occured, The request requires user authentication!')) {
                        this.router.navigate(['/login']);
                    }
                    break;
                default:
                    error = err;
                    break;
            }
            return throwError(error);
        }));
    }
}
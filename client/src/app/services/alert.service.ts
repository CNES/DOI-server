import { Injectable } from '@angular/core';
import { Router, NavigationStart } from '@angular/router';
import { Observable, Subject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class AlertService {
    private subject = new Subject<any>();
    private keepAfterRouteChange = false;

    constructor(private router: Router) {
        // clear alert messages on route change unless 'keepAfterRouteChange' flag is true
        this.router.events.subscribe(event => {
            if (event instanceof NavigationStart) {
                if (this.keepAfterRouteChange) {
                    // only keep for a single route change
                    this.keepAfterRouteChange = false;
                } else {
                    // clear alert message
                    this.clear();
                }
            }
        });
    }

    /**
     * Subscription to the alerts
     * @param message The alert message
     * @param keepAfterRouteChange Keep the alert after un route change, default no)
     */
    getAlert(): Observable<any> {
        return this.subject.asObservable();
    }

    /**
     * Add a success
     * @param message The alert message success
     * @param keepAfterRouteChange Keep the alert after un route change, default no)
     */
    success(message: string, keepAfterRouteChange = false) {
        this.keepAfterRouteChange = keepAfterRouteChange;
        this.subject.next({ type: 'success', message: message });
    }

    /**
     * Add an error
     * @param message The alert message
     * @param keepAfterRouteChange Keep the alert after un route change, default no)
     */
    error(message: string, keepAfterRouteChange = false) {
        this.keepAfterRouteChange = keepAfterRouteChange;
        this.subject.next({ type: 'danger', message: message });
    }


    /**
     * Add an info
     * @param message The alert message
     * @param keepAfterRouteChange Keep the alert after un route change, default no)
     */
     info(message: string, keepAfterRouteChange = false) {
        this.keepAfterRouteChange = keepAfterRouteChange;
        this.subject.next({ type: 'info', message: message });
    }


    /**
     * Add an info
     * @param message The alert message
     * @param keepAfterRouteChange Keep the alert after un route change, default no)
     */
     warning(message: string, keepAfterRouteChange = false) {
        this.keepAfterRouteChange = keepAfterRouteChange;
        this.subject.next({ type: 'warning', message: message });
    }

    /**
     * Clear all displayed alerts
     */
    clear() {
        // clear by calling subject.next() without parameters
        this.subject.next();
    }

    /**
     * Clear all errors with an optional  name
     * @param message Optional message to filter on
     */
    clearError(message?: String) {
        if (message) {
            this.subject.next({type: 'clear-error', message: message});
        } else {
            this.subject.next({type: 'clear-error', message: ''});
        }
    }

    /**
     * Clear all succcess with an optional  name
     * @param message Optional message to filter on
     */
     clearSuccess(message?: String) {
        if (message) {
            this.subject.next({type: 'clear-success', message: message});
        } else {
            this.subject.next({type: 'clear-success', message: ''});
        }
    }
}
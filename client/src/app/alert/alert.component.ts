import { Component, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';
import { AlertService } from '../services/alert.service';

interface Alert {
    type: string;
    message: string;
  }

@Component({
    selector: 'alert',
    templateUrl: './alert.component.html',
    styleUrls: ['./alert.component.css']
  })
export class AlertComponent implements OnDestroy {
    private subscription: Subscription;
    alerts: Alert[];

    constructor(private alertService: AlertService) {
        this.alerts = new Array();
        this.subscription = this.alertService.getAlert()
        .subscribe(alert => {
            if (alert != undefined) {
                switch (alert.type) {
                    case 'success':
                        this.alerts.push({type: 'success', message: alert.message});
                        break;
                    case 'info':
                        this.alerts.push({type: 'info', message: alert.message});
                        break;
                    case 'warning':
                        this.alerts.push({type: 'warning', message: alert.message});
                        break;
                    case 'danger':
                        this.alerts.push({type: 'danger', message: alert.message});
                        break;
                    case 'clear-error':
                        this.clearError(alert.message);
                        break;
                    case 'clear-success':
                        this.clearSuccess(alert.message);
                        break;
                    default:
                }
            } else {
                this.alerts = new Array();
            }
        });
    }

    ngOnDestroy() {
        this.subscription.unsubscribe();
    }

    /**
     * Close a specific alert (user form html to close one)
     * @param alert The alert to close
     */
    closeAlert(alert: Alert) {
        this.alerts.splice(this.alerts.indexOf(alert), 1);
    }

    /**
     * Clear all error with an optional message
     * @param message the message to filter on
     */
    clearError(message?: string){
        if (message) {
            this.alerts = this.alerts.filter(alert => alert.type != 'danger' || alert.message != message);
        } else {
            this.alerts = this.alerts.filter(alert => alert.type != 'danger');
        }
    }

    /**
     * Clear all success with an optional message
     * @param message the message to filter on
     */
     clearSuccess(message?: string){
        if (message) {
            this.alerts = this.alerts.filter(alert => alert.type != 'success' || alert.message != message);
        } else {
            this.alerts = this.alerts.filter(alert => alert.type != 'success');
        }
    }
}
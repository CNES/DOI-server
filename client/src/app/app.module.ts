import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './routing/app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './login/login.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NgbPaginationModule, NgbAlertModule} from '@ng-bootstrap/ng-bootstrap';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MatFormFieldModule} from '@angular/material/form-field';
import { MatInputModule} from '@angular/material/input';
import { MatButtonModule} from '@angular/material/button';
import { MatCardModule} from '@angular/material/card';
import { MatToolbarModule} from '@angular/material/toolbar';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { BasicAuthInterceptor } from './helpers/basic-auth.interceptor';
import { ErrorInterceptor } from './helpers/error.interceptor';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatListModule } from '@angular/material/list';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from './services/auth.service';
import { ConfigService } from './services/config.service';
import { AuthGuard } from './helpers/auth.guard';

import { MatTabsModule } from '@angular/material/tabs';
import { MatTreeModule } from '@angular/material/tree';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table' 
import { MatDialogModule } from '@angular/material/dialog';
import { DOIManagementComponent } from './home/doi-mgmt/doi-mgmt.component';
import { AdministrationComponent } from './home/administration/administration.component';
import { AddProjectDBoxComponent } from './home/administration/dialog-box/add-project-dbox.component';
import { RenameProjectDBoxComponent } from './home/administration/dialog-box/rename-project-dbox.component';
import { RemoveProjectDBoxComponent } from './home/administration/dialog-box/remove-project-dbox.component';
import { AddProjectUserDBoxComponent } from './home/administration/dialog-box/add-project-user-dbox.component';
import { RemoveProjectUserDBoxComponent } from './home/administration/dialog-box/remove-project-user-dbox.component';
import { AddAdminDBoxComponent } from './home/administration/dialog-box/add-admin-dbox.component';
import { RemoveAdminDBoxComponent } from './home/administration/dialog-box/remove-admin-dbox.component';
import { AlertComponent } from './alert/alert.component';
import { NavbarComponent } from './navbar/navbar.component';
import { FooterComponent } from './navbar/footer/footer.component';
import { CreationTokenComponent } from './home/creation-token/creation-token.component';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { CitationComponent } from './home/citation/citation.component';
import { DashboardComponent } from './home/dashboard/dashboard.component';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DeactivateDboxComponent } from './home/dashboard/dialog-box/deactivate-dbox/deactivate-dbox.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { CookieService } from 'ngx-cookie-service'

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    DOIManagementComponent,
    AdministrationComponent,
    AddProjectDBoxComponent,
    RenameProjectDBoxComponent,
    RemoveProjectDBoxComponent,
    AddProjectUserDBoxComponent,
    RemoveProjectUserDBoxComponent,
    AddAdminDBoxComponent,
    RemoveAdminDBoxComponent,
    AlertComponent,
    NavbarComponent,
    FooterComponent,
    CreationTokenComponent,
    CitationComponent,
    DashboardComponent,
    DeactivateDboxComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    FlexLayoutModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule,
    MatToolbarModule,
    MatGridListModule,
    MatListModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    MatTabsModule,
    MatTreeModule,
    MatIconModule,
    MatSelectModule,
    MatStepperModule,
    MatSlideToggleModule,
    MatTableModule,
    MatDialogModule,
    ClipboardModule,
    MatTooltipModule,
    MatExpansionModule,
    NgbAlertModule
  ],
  providers: [
    AuthService,
    AuthGuard,
    { provide: HTTP_INTERCEPTORS, useClass: BasicAuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true },
    { provide: APP_INITIALIZER,
      multi: true,
      deps: [ConfigService],
      useFactory: (appConfigService: ConfigService) => {
        return () => { return appConfigService.loadAppConfig(); };
      }
    },
    CookieService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

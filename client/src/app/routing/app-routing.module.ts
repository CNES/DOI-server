import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../helpers/auth.guard';
import { AdminGuard } from '../helpers/admin.guard';
import { AdministrationComponent } from '../home/administration/administration.component';
import { CitationComponent } from '../home/citation/citation.component';
import { DOIManagementComponent } from '../home/doi-mgmt/doi-mgmt.component';
import { CreationTokenComponent } from '../home/creation-token/creation-token.component';
import { LoginComponent } from '../login/login.component';
import { DashboardComponent } from '../home/dashboard/dashboard.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', canActivate: [AuthGuard], component: DashboardComponent },
  { path: 'doi-mgmt', canActivate: [AuthGuard], component: DOIManagementComponent },
  { path: 'citation', canActivate: [AuthGuard], component: CitationComponent },
  { path: 'creation-token', canActivate: [AuthGuard], component: CreationTokenComponent },
  { path: 'administration',  canActivate: [AuthGuard, AdminGuard], component: AdministrationComponent },

    // otherwise redirect to home
    {path: '', redirectTo: 'dashboard', pathMatch: 'full'},
    {path: '**', redirectTo: 'dashboard'}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
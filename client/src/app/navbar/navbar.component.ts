import { Component, OnInit } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent implements OnInit {

  public isAdminHidden: boolean = true;
  public user: string = "";

  constructor(
    private authService: AuthService,
    private cookieService: CookieService) {
      let isAdmin: any = this.cookieService.get('isAdmin');
      if (isAdmin && isAdmin == 'true') {
        this.isAdminHidden = true;
      } else {
        this.isAdminHidden = false;
      }
  }

  ngOnInit(): void {
    // Update user
    const user = this.cookieService.get('currentUser');
    if (user != null) {
      this.user = user;
    }
  }

  /**
   * Logout from DOI Client
   */
  logout() {
    this.authService.logout();
  }
}

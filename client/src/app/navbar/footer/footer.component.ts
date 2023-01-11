import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css']
})
export class FooterComponent implements OnInit {

  public capLogo: boolean = false;
  private inc: number = 0;

  constructor() { }

  ngOnInit(): void {
  }

  public changeLogo() {
    this.inc++;
    if (this.inc > 4) {
      this.capLogo = !this.capLogo;
      this.inc = 0;
    }
  }
}

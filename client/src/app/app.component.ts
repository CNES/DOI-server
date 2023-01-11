import { AfterViewInit, Component, ElementRef } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements AfterViewInit {
  title = 'DOI-client';
  constructor(private elementRef: ElementRef) {
  }
  ngAfterViewInit() {
  }
}

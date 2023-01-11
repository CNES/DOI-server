import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DOIManagementComponent } from './doi-mgmt.component';

describe('CreationDOIComponent', () => {
  let component: DOIManagementComponent;
  let fixture: ComponentFixture<DOIManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DOIManagementComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DOIManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeactivateDboxComponent } from './deactivate-dbox.component';

describe('DeactivateDboxComponent', () => {
  let component: DeactivateDboxComponent;
  let fixture: ComponentFixture<DeactivateDboxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DeactivateDboxComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeactivateDboxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioProfileInfoComponent } from './studio-profile-info.component';

describe('StudioProfileInfoComponent', () => {
  let component: StudioProfileInfoComponent;
  let fixture: ComponentFixture<StudioProfileInfoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StudioProfileInfoComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudioProfileInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

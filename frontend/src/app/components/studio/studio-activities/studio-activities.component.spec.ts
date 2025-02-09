import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioActivitiesComponent } from './studio-activities.component';

describe('StudioActivitiesComponent', () => {
  let component: StudioActivitiesComponent;
  let fixture: ComponentFixture<StudioActivitiesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StudioActivitiesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudioActivitiesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

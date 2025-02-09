import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivityPreferencesComponent } from './activity-preferences.component';

describe('ActivityPreferencesComponent', () => {
  let component: ActivityPreferencesComponent;
  let fixture: ComponentFixture<ActivityPreferencesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActivityPreferencesComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ActivityPreferencesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

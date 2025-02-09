import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioInstructorComponent } from './studio-instructor.component';

describe('StudioInstructorComponent', () => {
  let component: StudioInstructorComponent;
  let fixture: ComponentFixture<StudioInstructorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StudioInstructorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StudioInstructorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

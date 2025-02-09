import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExploreTypeComponent } from './explore-type.component';

describe('ExploreTypeComponent', () => {
  let component: ExploreTypeComponent;
  let fixture: ComponentFixture<ExploreTypeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ExploreTypeComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ExploreTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

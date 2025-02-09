import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioReviewsComponent } from './studio-reviews.component';

describe('StudioReviewsComponent', () => {
  let component: StudioReviewsComponent;
  let fixture: ComponentFixture<StudioReviewsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StudioReviewsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StudioReviewsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

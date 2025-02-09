import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioFaqComponent } from './studio-faq.component';

describe('StudioFaqComponent', () => {
  let component: StudioFaqComponent;
  let fixture: ComponentFixture<StudioFaqComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StudioFaqComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudioFaqComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

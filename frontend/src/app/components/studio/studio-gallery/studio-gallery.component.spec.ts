import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioGalleryComponent } from './studio-gallery.component';

describe('StudioGalleryComponent', () => {
  let component: StudioGalleryComponent;
  let fixture: ComponentFixture<StudioGalleryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StudioGalleryComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StudioGalleryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

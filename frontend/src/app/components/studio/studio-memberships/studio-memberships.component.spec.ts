import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudioMembershipsComponent } from './studio-memberships.component';

describe('StudioMembershipsComponent', () => {
  let component: StudioMembershipsComponent;
  let fixture: ComponentFixture<StudioMembershipsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StudioMembershipsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StudioMembershipsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

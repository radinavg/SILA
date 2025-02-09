import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyFriendsPageComponent } from './my-friends-page.component';

describe('MyFriendsPageComponent', () => {
  let component: MyFriendsPageComponent;
  let fixture: ComponentFixture<MyFriendsPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyFriendsPageComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(MyFriendsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

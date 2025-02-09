import { ComponentFixture, TestBed } from '@angular/core/testing';
import {DeleteProfileConfirmationComponent} from "./delete-profile-confirmation.component";


describe('DeleteProfileConfirmationComponent', () => {
  let component: DeleteProfileConfirmationComponent;
  let fixture: ComponentFixture<DeleteProfileConfirmationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DeleteProfileConfirmationComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(DeleteProfileConfirmationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

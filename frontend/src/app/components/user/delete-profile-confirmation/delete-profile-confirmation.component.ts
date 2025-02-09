import {Component, Input, OnInit} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {User} from "../../../dtos/user";
import {UserService} from "../../../services/user.service";
import {Router} from "@angular/router";
import {AuthService} from "../../../services/auth.service";

@Component({
  selector: 'delete-profile-confirmation',
  templateUrl: './delete-profile-confirmation.component.html',
  styleUrl: './delete-profile-confirmation.component.scss'
})
export class DeleteProfileConfirmationComponent implements OnInit{
  @Input() user: User;


  constructor(
    public activeModal: NgbActiveModal,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private userService: UserService,
    private router: Router,
    private authService: AuthService) {}

  ngOnInit() {
  }

  closeDialog() {
    this.activeModal.dismiss();
  }

  submitForm() {
    this.userService.deleteUser(this.user.email)
      .subscribe({
        next: data => {
          this.closeDialog();
          this.notification.success("The user " + data.email + " has been deleted successfully!");
          this.authService.logoutUser();
          this.router.navigate(['/login']);
        },
        error: err => {
          this.notification.error(this.errorFormatter.format(err), "Could delete user", {
              enableHtml: true,
              timeOut: 10000,
            }
          )
        }
      })

  }


}

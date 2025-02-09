import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {UserService} from "../../../services/user.service";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {ResetUserPasswordDto} from "../../../dtos/user";

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss'
})
export class ForgotPasswordComponent {

  constructor(
    private notification: ToastrService,
    private userService: UserService,
    private errorFormatter: ErrorFormatterService,
    private router: Router,
  ) { }

  passwordReset: ResetUserPasswordDto = {email: ''};

  resetPassword() {

    this.notification.info("Resetting password...");
    this.userService.resetPassword(this.passwordReset)
      .subscribe({
        next: data => {
          this.notification.success("Reset mail has been sent out to " + data.email);
          this.router.navigate(['/login']);
        },
        error: err => {
          console.log("Reset password for: ", this.passwordReset.email)
          this.notification.error(this.errorFormatter.format(err), "Could not reset password", {
              enableHtml: true,
              timeOut: 10000,
            }
          )
        }
      })
  }
}

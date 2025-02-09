import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, NgForm, NgModel, ReactiveFormsModule, Validators} from "@angular/forms";
import {UserUpdatePasswordDto} from "../../../dtos/user";
import {ToastrService} from "ngx-toastr";
import {UserService} from "../../../services/user.service";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {AuthService} from "../../../services/auth.service";
import {Router} from "@angular/router";
import {NgIf} from "@angular/common";

@Component({
  selector: 'app-user-change-password',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    NgIf
  ],
  templateUrl: './user-change-password.component.html',
  styleUrl: './user-change-password.component.scss'
})
export class UserChangePasswordComponent {

  passwordForm: FormGroup;
  submitted = false;

  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private router: Router
  ) {
    this.passwordForm = this.formBuilder.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8), this.passwordValidator]],
      confirmPassword: ['', [Validators.required]]
    }, { validator: this.passwordsMatchValidator });
  }


  onSubmit(): void {
    this.submitted = true;

    if (this.passwordForm.invalid) return;

    const { currentPassword, newPassword } = this.passwordForm.value;
    const passwordDto: UserUpdatePasswordDto = {
      currentPassword,
      newPassword,
      confirmationPassword: this.passwordForm.get('confirmPassword')?.value
    };

    this.userService.updateUserPassword(passwordDto)
      .subscribe({
        next: () => {
          this.notification.success('Password updated successfully!');
          this.router.navigate(['/user/profile']);
        },
        error: (err) => {
          this.notification.error(
            this.errorFormatter.format(err),
            'Could not update password',
            { enableHtml: true, timeOut: 10000 }
          );
        }
      });
  }

  private passwordValidator(control: any): { [key: string]: boolean } | null {
    const password = control.value;
    const hasUpperCase = /[A-Z]/.test(password);
    const hasNumber = /\d/.test(password);
    const hasSpecialChar = /[.,\-!\"§$%&\/()=?`*+\\]/.test(password);

    if (!hasUpperCase || !hasNumber || !hasSpecialChar) {
      return { invalidPassword: true };
    }
    return null;
  }

  private passwordsMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;

    return newPassword === confirmPassword ? null : { passwordsMismatch: true };
  }
}

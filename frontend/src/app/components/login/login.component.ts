import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {AuthRequest} from '../../dtos/auth-request';
import {ToastrService} from "ngx-toastr";
import {UserService} from "../../services/user.service";
import {firstValueFrom} from "rxjs";


@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

    loginForm: UntypedFormGroup;
    submitted = false;
    error = false;
    errorMessage = '';

    constructor(private formBuilder: UntypedFormBuilder,
                private authService: AuthService,
                private router: Router,
                private notification: ToastrService,
                private userService: UserService
    ) {
        this.loginForm = this.formBuilder.group({
            username: ['', [Validators.required, Validators.maxLength(100), Validators.email]],
            password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(16)]]
        });
    }


    loginUser() {
        this.submitted = true;
        if (this.loginForm.valid) {
            const authRequest: AuthRequest = new AuthRequest(this.loginForm.controls['username'].value, this.loginForm.controls['password'].value);
            this.authenticateUser(authRequest);
        } else {
            console.log('Invalid input');
        }
    }


    authenticateUser(authRequest: AuthRequest) {
        this.authService.loginUser(authRequest).subscribe({
            next: () => {
                console.log('Successfully logged in user: ' + authRequest.email);
              if (this.authService.getUserRole() == 'ADMIN') {
                this.router.navigate(['/admin/studios']);
              } else if (this.authService.getUserRole() == 'USER') {
                this.checkAndNavigate();
              } else if (this.authService.getUserRole() == 'STUDIO_ADMIN') {
                this.router.navigate(['/explore'])
              }
            },
            error: error => {
                console.log('Could not log in due to:', error);

                const errorObj = JSON.parse(error.error);
                const formattedError = `${errorObj.detail}`;
                this.notification.error(formattedError, "Could not log in due to:", {
                    enableHtml: true,
                    timeOut: 10000,
                });
            }
        });
    }

    async checkAndNavigate() {
        const preferencesSet = await firstValueFrom(this.userService.checkPreferencesSet());
        console.log("preferencesSet: ", preferencesSet);
        if (preferencesSet) {
            await this.router.navigate(['/']);
        } else {
            await this.router.navigate(['/preferences']);
        }
    }

    vanishError() {
        this.error = false;
    }

    ngOnInit() {
        if (this.authService.isLoggedIn()) {
            if (this.authService.getUserRole() == 'ADMIN') {
                this.router.navigate(['/admin/studios']);
            } else if (this.authService.getUserRole() == 'USER') {
                this.checkAndNavigate();
            } else if (this.authService.getUserRole() == 'STUDIO_ADMIN') {
              this.router.navigate(['/explore'])
            }
        }
    }
}

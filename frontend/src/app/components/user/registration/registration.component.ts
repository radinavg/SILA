import {Component, NgZone, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {UserService} from "../../../services/user.service";
import {UserCreateDto} from "../../../dtos/user";
import {Gender} from "../../../enums/gender";
import {Util} from "../../../utils/util";
import {debounceTime, distinctUntilChanged, switchMap} from "rxjs";


@Component({
    selector: 'app-registration',
    templateUrl: './registration.component.html',
    styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnInit {
    registrationForm: UntypedFormGroup;
    locationSuggestions: { display_name: string; lon: number; lat: number }[] = [];

  isLoading = false;

    registrationDetail: UserCreateDto = {
      firstName: '',
      lastName: '',
      gender: null,
      location: '',
      longitude: 0,
      latitude: 0,
      email: '',
      password: '',
      passwordConfirmation: ''
    }

    constructor(
        private formBuilder: UntypedFormBuilder,
        private userService: UserService,
        private router: Router,
        private notification: ToastrService,
        private errorFormatter: ErrorFormatterService,
        private ngZone: NgZone,
        private util: Util) {
        this.registrationForm = this.formBuilder.group({
            email: ['', [Validators.required, Validators.email]],
            name: ['', Validators.required],
            lastname: ['', Validators.required],
            gender: [null, Validators.required],
            location: ['', Validators.required],
            password: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword:['', Validators.required],
        }, {
            validator: this.util.PasswordMatching('password', 'confirmPassword'),
        });
    }

  ngOnInit() {
    this.registrationForm.get('location')?.valueChanges
      .pipe(
        debounceTime(300),  // wait for user to stop typing
        distinctUntilChanged(),
        switchMap(value => this.util.fetchLocationSuggestions(value))
      )
      .subscribe({
        next: (suggestions: { display_name: string; lon: number; lat: number}[] ) => {
          this.locationSuggestions = suggestions;
          console.log('Location suggestions:', this.locationSuggestions);
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Error fetching location suggestions:', err);
          this.isLoading = false;
        },
      });
  }

  clearLocationSuggestions(): void {
    setTimeout(() => {
      this.ngZone.run(() => {
        this.locationSuggestions = [];
      });
    }, 200);
  }

  selectLocation(suggestion: { display_name: string; lon: number; lat: number }): void {
    this.registrationForm.patchValue({ location: suggestion.display_name });
    this.registrationDetail.longitude = suggestion.lon;
    this.registrationDetail.latitude = suggestion.lat;
    this.locationSuggestions = [];
  }

    registration(){
        this.util.PasswordMatching('password', 'confirmPassword')(this.registrationForm);
        if(this.registrationForm.invalid){
            console.log('Invalid input');
            if(this.registrationForm.value['confirmPassword'].errors?.['mustMatch']){
                this.notification.error('Passwords do not match!', 'Invalid input');
                return;
            }
        }

        console.log(this.registrationForm)
        this.registrationDetail.firstName = this.registrationForm.value['name'];
        this.registrationDetail.lastName = this.registrationForm.value['lastname'];
        this.registrationDetail.gender = this.registrationForm.value['gender'] as Gender;
        this.registrationDetail.location = this.registrationForm.value['location'];
        this.registrationDetail.email = this.registrationForm.value['email'];
        this.registrationDetail.password = this.registrationForm.value['password'];
        this.registrationDetail.passwordConfirmation = this.registrationForm.value['confirmPassword'];

        console.log(this.registrationDetail.gender)

        this.userService.registerUser(this.registrationDetail).subscribe({
            next: () => {
                this.notification.success("You have successfully created new account");
                console.log('Successfully registered user');
                this.router.navigate(['/login']);
            },
            error: (response) => {
                console.log(this.registrationDetail);
                this.notification.error(this.errorFormatter.format(response),"Registration failed", {
                    enableHtml:true
                })
                console.error(response);
            }
        })
    }
}

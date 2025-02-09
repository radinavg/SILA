import {DeleteProfileConfirmationComponent} from "../delete-profile-confirmation/delete-profile-confirmation.component";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {ToastrService} from "ngx-toastr";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthService} from "../../../services/auth.service";
import {UserService} from "../../../services/user.service";
import {Component, NgZone, OnInit} from "@angular/core";
import {UserInfoDto, UserUpdateDto} from "../../../dtos/user";
import {RouterLink} from "@angular/router";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import { FontAwesomeModule, FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { faChevronRight, faChevronDown, faCamera } from '@fortawesome/free-solid-svg-icons';
import {Globals} from "../../../global/globals";
import {HttpHeaders} from "@angular/common/http";
import {ProfileImageService} from "../../../services/reload-services/profile-image.service";
import {Util} from "../../../utils/util";
import {debounceTime, distinctUntilChanged, of, switchMap} from "rxjs";
import {Membership} from "../../../dtos/membership";
import {MembershipService} from "../../../services/membership.service";


@Component({
  selector: 'app-user-view',
  standalone: true,
  imports: [
    RouterLink,
    FaIconComponent,
    FormsModule,
    NgIf,
    NgForOf,
    FontAwesomeModule,
    ReactiveFormsModule
  ],
  templateUrl: './user-view.component.html',
  styleUrl: './user-view.component.scss'
})
export class UserViewComponent implements OnInit{

  user: UserUpdateDto = {
    firstName: '',
    lastName: '',
    email: '',
    location: '',
    longitude: 0,
    latitude: 0,
    gender: null,
    profileImagePath: ''
  };

  profileImageUrl: string | null = null;
  locationSuggestions: { display_name: string; lon: number; lat: number }[] = [];
  currentUserMemberships: Membership[] = [];


  userFields = [
    { label: 'First Name', key: 'firstName', expanded: false, editing: false },
    { label: 'Last Name', key: 'lastName', expanded: false, editing: false },
    { label: 'Email', key: 'email', expanded: false, editing: false },
    { label: 'Gender', key: 'gender', expanded: false, editing: false },
    { label: 'Location', key: 'location', expanded: false, editing: false }
  ];

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private modalService: NgbModal,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private profileImageService: ProfileImageService,
    private membershipService: MembershipService,
    library: FaIconLibrary,
    private globals: Globals,
    private util: Util,
    private ngZone: NgZone
  ) {
    library.addIcons(faChevronRight, faChevronDown, faCamera);
  }

  ngOnInit(): void {
    this.loadUser();
    this.loadUserMemberships();
  }

  loadUser() : void {
    this.userService.getUserInfo(this.authService.getUserEmail()).subscribe({
      next: (data) => {
        this.user = data;
        this.profileImageUrl = this.user.profileImagePath
          ? `${this.globals.backendImageUri}${this.user.profileImagePath}`
          : 'assets/default-profile.png'
        console.log("User: ", this.user)

      },
      error: (err) =>
        this.notification.error(this.errorFormatter.format(err), 'Could not fetch user info', {
          enableHtml: true,
          timeOut: 10000
        })
    });
  }

  loadUserMemberships(): void {
    this.membershipService.getMembershipsForUser().subscribe({
      next: (memberships) => {
        this.currentUserMemberships = memberships;
        console.log("User's Memberships: ", this.currentUserMemberships);
      },
      error: (err) =>
        this.notification.error('Error loading memberships', 'Error', { timeOut: 5000 })
    });
  }

  deleteMembership(membershipId: number): void {
    this.membershipService.deleteMembership(membershipId).subscribe({
      next: () => {
        // Remove the deleted membership from the local array
        this.currentUserMemberships = this.currentUserMemberships.filter(membership => membership.membershipId !== membershipId);
        this.notification.success('Membership unsubscribed successfully', 'Success');
      },
      error: (err) => {
        this.notification.error('Failed to unsubscribe from membership', 'Error', { timeOut: 5000 });
      }
    });
  }


  heading() {
    return `Hello, ${this.user.firstName}!`;
  }

  toggleField(index: number): void {
    this.userFields[index].expanded = !this.userFields[index].expanded;
  }

  editField(index: number): void {
    this.userFields[index].editing = true;
  }

  saveField(index: number): void {
    this.userFields[index].editing = false;
    this.userService.updateUserInfo(this.user).subscribe({
      next: () => {
        this.notification.success(`${this.userFields[index].label} updated successfully`)
        this.loadUser();
      },
      error: (err) =>
        this.notification.error(this.errorFormatter.format(err), `Error updating ${this.userFields[index].label}`, {
          enableHtml: true,
          timeOut: 10000
        })
    });
  }

  cancelEdit(index: number): void {
    this.userFields[index].editing = false;
  }

  openDeleteConfirmation(): void {
    const modalRef = this.modalService.open(DeleteProfileConfirmationComponent);
    modalRef.componentInstance.user = this.user;
    modalRef.result.then(
      (result) => {
        if (result) {
          console.log('Profile deleted');
        }
      },
      (reason) => {
        console.log('Modal dismissed:', reason);
      }
    );
  }

  selectLocation(suggestion: { display_name: string; lon: number; lat: number }): void {
    this.user.location = suggestion.display_name;
    this.user.latitude = suggestion.lat;
    this.user.longitude = suggestion.lon;
    this.locationSuggestions = [];
  }

  fetchLocationSuggestions(query: string): void {
    of(query)
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((value) => this.util.fetchLocationSuggestions(value))
      )
      .subscribe({
        next: (suggestions) => {
          this.locationSuggestions = suggestions;
          console.log('Location suggestions:', this.locationSuggestions);
        },
        error: (err) => {
          console.error('Error fetching location suggestions:', err);
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

  onImageUpload(event: Event): void {
    const fileInput = event.target as HTMLInputElement;
    if (fileInput?.files?.length) {
      const file = fileInput.files[0];
      const formData = new FormData();
      formData.append('file', file);
      const headers = new HttpHeaders();
      headers.append('Content-Type', 'multipart/form-data');

      this.userService.uploadProfileImage(formData, headers).subscribe({
        next: () => {
          this.profileImageService.triggerReload();
          this.loadUser();
          this.notification.success('Profile image updated successfully');
        },
        error: (err) => {
          console.log(err)
          this.notification.error(this.errorFormatter.format(err), `Error Uploading Profile Photo`, {
          enableHtml: true,
            timeOut: 10000
        });
        }
      });
    }
  }

}

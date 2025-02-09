import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {AuthService} from "../../services/auth.service";
import {UserService} from "../../services/user.service";
import {ToastrService} from "ngx-toastr";
import {Globals} from "../../global/globals"
import {ProfileImageService} from "../../services/reload-services/profile-image.service";
import {StudioService} from "../../services/studio.service";
import {NavigationEnd, Router} from "@angular/router";
import {filter} from "rxjs";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  profileImageUrl: string | null = null;
  studioId: number | null = null;

  constructor(
    public authService: AuthService,
    private userService: UserService,
    private notification: ToastrService,
    private globals: Globals,
    private profileImageService: ProfileImageService,
    private studioService: StudioService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}


  ngOnInit(): void {
    this.profileImageService.reload$.subscribe(() => {
      this.loadUserProfileImage(); // trigger reload of user profile image if it has changed in user view component
    });
    this.loadUserProfileImage();
    this.loadStudioAdminStudioPage();
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => {
        if (this.authService.getUserRole() === 'STUDIO_ADMIN') {
          this.loadStudioAdminStudioPage(); // Refresh studio ID on route changes
        }
      });
  }

  loadUserProfileImage(): void {
    const userEmail = this.authService.getUserEmail();
    this.userService.getUserInfo(userEmail).subscribe({
      next: (user) => {
        this.profileImageUrl = user.profileImagePath
          ? `${this.globals.backendImageUri}${user.profileImagePath}`
          : 'assets/default-profile.png';
      },
      error: (err) => {
        this.notification.error('Failed to load user profile image');
        console.error(err);
      }
    });
  }

  loadStudioAdminStudioPage(): void {
    if (this.authService.getUserRole() === "STUDIO_ADMIN") {
      this.studioService.getStudioIdByAdmin(this.authService.getUserEmail()).subscribe(
        response => {
          this.studioId = response;
          this.cdr.detectChanges();
          console.log("studioId:", this.studioId);
        },
        error => {
          console.error('Error loading studio id:', error);
        }
      );
    }

  }
  navigateToYourStudio(): void {
    if (this.studioId) {
      this.router.navigate(['/studio', this.studioId]).then(success => {
        if (!success) {
          this.notification.error('Navigation to your studio failed');
        }
      });
    } else {
      this.notification.error('Studio ID is not available');
    }
  }

}

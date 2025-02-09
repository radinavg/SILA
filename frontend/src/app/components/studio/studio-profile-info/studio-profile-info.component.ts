import {Component, EventEmitter, Input, NgZone, OnInit, Output} from '@angular/core';
import {faHeart} from "@fortawesome/free-solid-svg-icons/faHeart";
import {faLocationPin} from "@fortawesome/free-solid-svg-icons";
import {StudioDto} from "../../../dtos/studio";
import {Globals} from "../../../global/globals";
import {StudioService} from "../../../services/studio.service";
import {ToastrService} from "ngx-toastr";
import {faPen} from "@fortawesome/free-solid-svg-icons/faPen";
import {FormControl, FormGroup} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthService} from "../../../services/auth.service";
import {StudioUpdateDto} from "../../../dtos/studio-update-dto";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {ProfileImageService} from "../../../services/reload-services/profile-image.service";
import {Util} from "../../../utils/util";
import {debounceTime, distinctUntilChanged, of, switchMap} from "rxjs";

@Component({
  selector: 'app-studio-profile-info',
  templateUrl: './studio-profile-info.component.html',
  styleUrls: ['./studio-profile-info.component.scss']
})
export class StudioProfileInfoComponent implements OnInit {

  protected readonly faHeart = faHeart;
  protected readonly faLocationPin = faLocationPin;
  protected readonly faPen = faPen;

  editStudioNameForm: FormGroup;
  editStudioDescriptionForm: FormGroup;
  editStudioLocationForm: FormGroup;
  editStudioProfileImageForm: FormGroup;

  @Input() studio!: StudioDto;
  @Output() studioChange = new EventEmitter<StudioDto>();
  imagePreview: string;
  selectedImage: File | null = null;
  isCurrentUserAdminOfThisStudio: boolean = false;
  locationSuggestions: { display_name: string; lon: number; lat: number }[] = [];

  studioUpdateDto: StudioUpdateDto = {
    name: '',
    description: '',
    location:'',
    longitude: 0,
    latitude: 0,
  };

  constructor(protected globals: Globals,
              private studioService: StudioService,
              private notification: ToastrService,
              private errorFormatter: ErrorFormatterService,
              private modalService: NgbModal,
              protected authService: AuthService,
              private profileImageService: ProfileImageService,
              private util: Util,
              private ngZone: NgZone
  ) {

    this.editStudioNameForm = new FormGroup({
      name: new FormControl('')
    });

    this.editStudioDescriptionForm = new FormGroup({
      description: new FormControl('')
    });

    this.editStudioLocationForm = new FormGroup({
      location: new FormControl('')
    });

    this.editStudioProfileImageForm = new FormGroup({
      profileImageFile: new FormControl('')
    });
  }

  getStars(): string[] {
    const filledStars = Math.round(this.averageRating); // Round to nearest whole number
    return Array(5)
      .fill('☆')
      .map((star, index) => (index < filledStars ? '★' : '☆'));
  }

  averageRating: number = 0;
  reviewCount: number = 0;

  calculateAverageRating(): void {
    console.log(this.studio)
    if (this.studio.reviewsLength && this.studio.reviewsLength > 0) {
      this.averageRating = this.studio.averageReview;
      this.reviewCount = this.studio.reviewsLength;
    } else {
      this.averageRating = 0;
      this.reviewCount = 0;
    }
  }

  ngOnInit(): void {
    this.studioService.isCurrentUserAdmin(this.studio.studioId).subscribe(
      isAdmin => {
        this.isCurrentUserAdminOfThisStudio = isAdmin;
      },
      error => {
        console.error('Error checking admin status:', error);
      }
    );
    this.calculateAverageRating();
    this.studioUpdateDto = {
      name: this.studio.name,
      description: this.studio.description,
      location: this.studio.location
    };
  }


  onClick() {
    if (this.studio.isFavouriteForUser) {
      this.studioService.removeFavouriteStudio(this.studio.studioId).subscribe({
        next: (studioDto: StudioDto) => {
          console.log("Successfully removed from favourites:" +  studioDto);
          this.notification.success("Studio removed from favourites.");
          this.studio = studioDto;
          this.studioChange.emit(this.studio);

        },
        error: err => {
          console.error(err);
        }
      })
    } else {
      this.studioService.addFavouriteStudio(this.studio.studioId).subscribe({
        next: (studioDto: StudioDto) => {
          console.log("Successfully added to favourites:" +  studioDto);
          this.notification.success("Studio added to favourites.");
          this.studio = studioDto;
          this.studioChange.emit(this.studio);

        },
        error: err => {
          console.error(err);
        }
      })
    }
  }


  openModal(modal) {
    this.modalService.open(modal);
  }

  selectLocation(suggestion: { display_name: string; lon: number; lat: number }): void {
    this.studio.location = suggestion.display_name;
    this.studioUpdateDto.location = suggestion.display_name;
    this.studioUpdateDto.longitude = suggestion.lon;
    this.studioUpdateDto.latitude = suggestion.lat;
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

  onStudioNameSubmit(modal: any) {
    this.studioUpdateDto.name = this.editStudioNameForm.get('name').value;
    this.updateStudio(modal);
  }

  onStudioDescriptionSubmit(modal: any) {
    this.studioUpdateDto.description =  this.editStudioDescriptionForm.get('description').value;
    this.updateStudio(modal);
  }

  onStudioLocationSubmit(modal: any) {
    this.studioUpdateDto.location = this.editStudioLocationForm.get('location').value;
    this.updateStudio(modal);
  }

  updateStudio(modal?: any) {
    this.studioService.updateStudioByStudioId(this.studioUpdateDto, this.studio.studioId).subscribe({
      next: value => {
        this.studioChange.emit(value);
        this.studio = value;
        this.notification.success("Studio Information Updated!")
        if (modal) {
          modal.close(); // Close modal only on success
        }
      },
      error: err => {
        this.notification.error(this.errorFormatter.format(err), 'Could not update studio info', {
          enableHtml: true,
          timeOut: 10000
        });
        console.log(err);
      }
    });
  }

  onStudioProfileImageSubmit(modal: any) {
    if (!this.selectedImage) {
      return;
    }

    this.studioService.updateProfileImage(this.selectedImage, this.studio.studioId).subscribe({
      next: value => {
        this.studioChange.emit(value);
        this.studio = value;
        this.editStudioProfileImageForm.reset();
        this.notification.success("Studio Profile Image Updated!")
        this.profileImageService.triggerReload();
        if (modal) {
          modal.close(); // Close modal only on success
        }
      },
      error: err => {
        this.notification.error(this.errorFormatter.format(err), 'Could not update studio profile image', {
          enableHtml: true,
          timeOut: 10000
        });
        console.log(err);
      }
    });
  }

  onFileSelected(event: Event) {
    const fileInput = event.target as HTMLInputElement;
    const selectedFile: File = fileInput.files[0];

    if (!selectedFile) {
      return;
    }

    const reader = new FileReader();

    reader.onload = () => {
      this.imagePreview = reader.result as string;

      this.selectedImage = selectedFile;
    };
    reader.readAsDataURL(selectedFile);
  }

  onCancel(modal: any) {
    this.imagePreview = null
    this.editStudioDescriptionForm.reset()
    this.editStudioNameForm.reset()
    this.editStudioLocationForm.reset()
    this.editStudioProfileImageForm.reset()
    modal.dismiss('Close click')
  }
}

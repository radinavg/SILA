import {Component, NgZone, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {StudioCreateDto} from "../../dtos/studio";
import {StudioService} from "../../services/studio.service";
import {HttpErrorResponse} from "@angular/common/http";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {ToastrService} from "ngx-toastr";
import {Util} from "../../utils/util";
import {debounceTime, distinctUntilChanged, switchMap} from "rxjs";

@Component({
  selector: 'app-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.scss']
})
export class FormComponent implements OnInit{

  studioForm: UntypedFormGroup;
  imagePreview: string | null = null;
  locationSuggestions: { display_name: string; lon: number; lat: number }[] = [];

  isLoading = false;

  studioCoordinates = {
    longitude: 0,
    latitude: 0,
  };

  constructor(private fb: UntypedFormBuilder,
              private studioService: StudioService,
              private errorFormatter: ErrorFormatterService,
              private notification: ToastrService,
              private util: Util,
              private ngZone: NgZone
  ) {
    this.studioForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      description: [''],
      location: ['', Validators.required],
      profileImageFile: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(16)]],
      confirmPassword:['', [Validators.required, Validators.minLength(8), Validators.maxLength(16)]],
    }, {
      validator: this.util.PasswordMatching('password', 'confirmPassword'),

    });
  }

  ngOnInit() {
    this.studioForm.get('location')?.valueChanges
      .pipe(
        debounceTime(300),  // Wait for the user to stop typing
        distinctUntilChanged(),
        switchMap(value => this.util.fetchLocationSuggestions(value)) // Call the fetch method
      )
      .subscribe({
        next: (suggestions: { display_name: string; lon: number; lat: number }[]) => {
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
    this.studioForm.patchValue({ location: suggestion.display_name });
    this.studioCoordinates.latitude = suggestion.lat;
    this.studioCoordinates.longitude = suggestion.lon;
    this.locationSuggestions = [];
  }

  onSubmit() {
      const studioData: StudioCreateDto = this.studioForm.value;
      studioData.latitude = this.studioCoordinates.latitude;
      studioData.longitude = this.studioCoordinates.longitude;
      this.studioService.createStudio(studioData).subscribe({
        next: (response) => {
          this.notification.success("Request to promote your studio on Sila has been sent to our admins. Once admins have approved your appearance in the Sila you will be notified via email.");
          console.log('Studio created successfully:', response);
          this.studioForm.reset();
          this.imagePreview = null;
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error creating studio:', error.message);
          this.notification.error(this.errorFormatter.format(error), "Request to promote studio failed", {
              enableHtml: true,
              timeOut: 10000,
            }
          )
        },
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

      this.studioForm.patchValue({
        profileImageFile: selectedFile
      });
    };
    reader.readAsDataURL(selectedFile);
  }
}

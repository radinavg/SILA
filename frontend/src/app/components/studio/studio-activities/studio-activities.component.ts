import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {StudioActivity, StudioActivityCreateDto, StudioActivityListDto} from "../../../dtos/studioActivity";
import {faPlus} from "@fortawesome/free-solid-svg-icons";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {HttpErrorResponse} from "@angular/common/http";
import {ActivatedRoute} from "@angular/router";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {ToastrService} from "ngx-toastr";
import {StudioActivityService} from "../../../services/studio-activity.service";
import {Globals} from "../../../global/globals";
import {AuthService} from "../../../services/auth.service";
import {StudioService} from "../../../services/studio.service";
import {StudioDto} from "../../../dtos/studio";
import {ProfileImageService} from "../../../services/reload-services/profile-image.service";
import {StudioActivityReloadService} from "../../../services/reload-services/studio-activity-reload.service";
import {SkillLevel} from "../../../enums/skillLevel";

@Component({
  selector: 'app-studio-activities',
  templateUrl: './studio-activities.component.html',
  styleUrls: ['./studio-activities.component.scss']
})
export class StudioActivitiesComponent implements OnInit {


  @Input() studioId!: number;
  @Input() studioActivities!: StudioActivityListDto[];
  @Output() studioActivitiesChange = new EventEmitter<StudioActivityListDto[]>();

  private currentSlide: number = 0;
  newActivityForm: UntypedFormGroup;
  isCurrentUserAdminOfThisStudio: boolean = false;

  protected readonly faPlus = faPlus;
  imagePreview: string | null = null;
  profileImageFile: File | null = null;
  activityTypes: string[] = [];

  constructor(private modalService: NgbModal,
              protected globals: Globals,
              private fb: UntypedFormBuilder,
              private studioActivityService: StudioActivityService,
              private route: ActivatedRoute,
              private errorFormatter: ErrorFormatterService,
              private notification: ToastrService,
              public authService: AuthService,
              private studioService: StudioService,
              private studioActivityReloadService: StudioActivityReloadService
  ) {

    this.newActivityForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      price: ['', Validators.required, Validators.min(0)],
      type: ['', Validators.required],
      date: ['', Validators.required],
      time: ['', Validators.required],
      duration: ['', Validators.required],
      capacity: ['', Validators.required],
      skillLevel: [null, Validators.required],
      equipment: [false]
    });
  }

  ngOnInit(): void {
    this.studioService.isCurrentUserAdmin(this.studioId).subscribe(
      isAdmin => {
        this.isCurrentUserAdminOfThisStudio = isAdmin;
      },
      error => {
        console.error('Error checking admin status:', error);
      }
    );

    this.getAllActivityTypes()
  }

  getAllActivityTypes(): void{

    this.studioActivityService.getAllActivityTypes().subscribe({
      next: value => {
        this.activityTypes = value;
      },
      error: err => {
        console.error(err);
      }
    })
  }

  formatActivityType(type: string): string {
    return type.replace('_', ' ').toLowerCase();
  }

  formatDateTime(dateTime: Date | undefined): string {
    if (!dateTime) {
      return 'Date not scheduled';
    }

    const date = new Date(dateTime); // Ensure it's a Date object
    const hours = date.getHours().toString().padStart(2, '0'); // Add leading zero if needed
    const minutes = date.getMinutes().toString().padStart(2, '0'); // Add leading zero if needed
    const day = date.getDate().toString().padStart(2, '0'); // Day of the month
    const month = (date.getMonth() + 1).toString().padStart(2, '0'); // Month (0-based)
    const year = date.getFullYear();

    return `${hours}:${minutes} on ${day}.${month}.${year}`;
  }

  onSubmit() {

    if (this.newActivityForm.invalid) {
      return;
    }

    const formValues = this.newActivityForm.value;
    const studioId = Number(this.route.snapshot.paramMap.get('id'));

    const type = formValues.type.replace(' ', '_').toUpperCase();

    const localDateTime = new Date(`${formValues.date}T${formValues.time}:00`);
    const timezoneOffsetInMS = localDateTime.getTimezoneOffset() * 60000;
    const adjustedDateTime = new Date(localDateTime.getTime() - timezoneOffsetInMS);

    const newActivity: StudioActivityCreateDto = {
      name: formValues.name,
      profileImageFile: this.profileImageFile,
      studioId: studioId.toString(),
      description: formValues.description,
      price: formValues.price,
      type: type,
      duration: formValues.duration.toString(),
      capacity: formValues.capacity.toString(),
      dateTime: adjustedDateTime.toISOString(),
      skillLevel: formValues.skillLevel as SkillLevel,
      equipment: formValues.equipment,
    };


    console.log("create activity in backend: ", newActivity)
    console.log("profile image: ", this.profileImageFile)

    this.studioActivityService.createStudioActivity(newActivity).subscribe({
      next: (response) => {
        console.log('Activity created successfully:', response);
        this.notification.success("Activity created successfully", "Success");


        this.studioActivityReloadService.triggerReload();
        this.studioActivitiesChange.emit(this.studioActivities);
        this.newActivityForm.reset();
        this.imagePreview = null;
      },
      error: (error: HttpErrorResponse) => {
        console.error('Error creating activity:', error.message);
        this.notification.error(this.errorFormatter.format(error), "Error Creating Activity", {
            enableHtml: true,
            timeOut: 10000,
          }
        );
      },
    });
  }


  get slideTransform(): string {
    return `translateX(-${this.currentSlide * 300}px)`;
  }

  slideLeft() {
    if (this.currentSlide > 0) {
      this.currentSlide--;
    }
  }

  slideRight() {
    if ((this.currentSlide + 1) * 300 < this.studioActivities.length * 300) {
      this.currentSlide++;
    }
  }

  openModal(addActivityModal: any) {
    this.modalService.open(addActivityModal)
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

      this.profileImageFile = selectedFile;
    };
    reader.readAsDataURL(selectedFile);
  }

  onCancel(modal: any) {
    this.imagePreview = null
    modal.dismiss('Close click')
  }
}

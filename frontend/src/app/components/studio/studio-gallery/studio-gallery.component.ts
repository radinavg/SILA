import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';

import {AuthService} from "../../../services/auth.service";
import {FormControl, FormGroup, UntypedFormBuilder} from "@angular/forms";
import {StudioDto} from "../../../dtos/studio";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StudioService} from "../../../services/studio.service";
import {Globals} from "../../../global/globals";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";

@Component({
  selector: 'app-studio-gallery',
  templateUrl: './studio-gallery.component.html',
  styleUrls: ['./studio-gallery.component.scss']
})
export class StudioGalleryComponent implements OnInit {

  @Input() studio!: StudioDto;
  @Output() studioChange = new EventEmitter<StudioDto>();
  currentImageIndex: number = 0;
  imageVisible: boolean = true;

  imagePreviews: string[] = [];
  selectedImages: File[] = [];
  newImagesGroup: FormGroup;
  isCurrentUserAdminOfThisStudio: boolean = false;



  constructor(protected authService: AuthService,
              protected fb: UntypedFormBuilder,
              private modalService: NgbModal,
              private studioService: StudioService,
              protected globals: Globals,
              private notification: ToastrService,
              private errorFormatter: ErrorFormatterService) {
    this.newImagesGroup = new FormGroup({
      images: new FormControl()
    })
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
  }

  prevImage() {
    this.triggerFade(() => {
      this.currentImageIndex = (this.currentImageIndex > 0)
        ? this.currentImageIndex - 1
        : this.studio.galleryImages.length - 1;
    });
  }


  nextImage() {
    this.triggerFade(() => {
      this.currentImageIndex = (this.currentImageIndex < this.studio.galleryImages.length - 1)
        ? this.currentImageIndex + 1
        : 0;
    });
  }

  private triggerFade(callback: () => void) {
    this.imageVisible = false;
    setTimeout(() => {
      callback();
      this.imageVisible = true;
    }, 500);
  }


  onFileSelected(event) {

    const images: File[] = Array.from(event.target.files);

    for (let image of images) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imagePreviews.push(e.target.result);
        this.selectedImages.push(image)
      };
      reader.readAsDataURL(image);
    }

    console.log(this.selectedImages)
  }

  onSubmit(modal: any) {
    if (this.selectedImages.length === 0) {
      return;
    }

    const formData = new FormData();
    this.selectedImages.forEach((file) => formData.append('files', file));
    console.log(formData)

    this.studioService.uploadGalleryImages(formData, this.studio.studioId).subscribe({
      next: value => {
        this.studioChange.emit(value);
        this.notification.success("Uploaded images to the gallery", "Success");
        if (modal) {
          modal.close();
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

    this.selectedImages = [];
  }



  openModal(addGalleryImagesModal: any) {
    this.modalService.open(addGalleryImagesModal)
  }

  onCancel(modal) {
    this.imagePreviews = []
    modal.dismiss('Close click')
  }
}

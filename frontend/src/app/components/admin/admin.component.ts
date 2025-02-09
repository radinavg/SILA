import {Component, TemplateRef, OnInit} from '@angular/core';
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {StudioService} from "../../services/studio.service";
import {ApproveStudioDto, StudioInfoDto} from "../../dtos/studio";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {Globals} from "../../global/globals";


@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {

  studios: StudioInfoDto[] = []

  studioApproval: ApproveStudioDto = {
    studioId: 0,
    approved: false
  }

  selectedStudio: StudioInfoDto;

  constructor(private modalService: NgbModal,
              private studioService: StudioService,
              private notification: ToastrService,
              private errorFormatter: ErrorFormatterService,
              private globals: Globals
  ) { }

  ngOnInit(): void {
    this.studioService.getNotApprovedStudios().subscribe({
      next: res => {
        this.studios = res;
        console.log("Studios awaiting approval", this.studios)
      },
      error: err => {
        console.error("Error loading studios:", err);
      }
    })
  }

  openModal(studio: any, content: TemplateRef<any>) {
    this.selectedStudio = studio;
    this.modalService.open(content);
  }

  approveStudio(studio: StudioInfoDto, modal: any) {
    console.log('Approved:', studio);
    this.studioApproval.studioId = studio.studioId;
    this.studioApproval.approved = true;
    this.studioService.approveStudio(this.studioApproval).subscribe({
      next: res => {
        this.notification.success("You approved this studio to go live on Sila!", "Success");
        console.log('Studio approved and updated:', res);
        modal.close();
        this.ngOnInit()
      },
      error: err => {
        console.error("Error approving studio:", err);
        this.notification.error(this.errorFormatter.format(err), "Error approving studio", {
            enableHtml: true,
            timeOut: 10000,
          }
        )
      }
    });
  }

  deleteStudio(studio: any, modal: any) {
    console.log('Deleted:', studio);
    this.studioService.deleteStudio(studio.studioId).subscribe({
      next: res => {
        console.log('Studio deleted:', res);
        this.notification.success("Studio availing approval successfully deleted", "Deleted");
        modal.close();
        this.ngOnInit()
      },
      error: err => {
        console.error("Error deleting studio:", err);
        this.notification.error(this.errorFormatter.format(err), "Error deleting studio", {
            enableHtml: true,
            timeOut: 10000,
          }
        );
      }
    });
  }

  getStudioImagePath(studio: StudioInfoDto): string {
    const imageUrl = studio.profileImagePath;
    if (imageUrl && imageUrl !== '') {
      // Check if the URL starts with assets, then it's an image from our system,
      // this is now important because data generator has web images and previous implementation wouldn't work with our own images
      if (imageUrl.startsWith('assets/')) {
        return `${this.globals.backendImageUri}${imageUrl}`;
      }
      return 'assets/default-profile.png';
    }
  }
}

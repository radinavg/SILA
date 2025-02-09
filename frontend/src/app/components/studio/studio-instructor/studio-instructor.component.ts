import {Component, EventEmitter, Input, OnInit, Output, TemplateRef} from '@angular/core';
import {Instructor} from "../../../dtos/instructor";
import {NgForOf, NgIf} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {faPlus} from "@fortawesome/free-solid-svg-icons";
import {FaIconComponent} from "@fortawesome/angular-fontawesome";

import {StudioService} from "../../../services/studio.service";
import {Globals} from "../../../global/globals";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {StudioDto} from "../../../dtos/studio";

@Component({
  selector: 'app-studio-instructor',
  standalone: true,
  imports: [
    NgForOf,
    FormsModule,
    FaIconComponent,
    NgIf
  ],
  templateUrl: './studio-instructor.component.html',
  styleUrl: './studio-instructor.component.scss'
})
export class StudioInstructorComponent implements OnInit{
  @Input() studioId: number
  @Input() instructors:Instructor[];
  isCurrentUserAdminOfThisStudio: boolean = false;
  @Output() instructorsChange=new EventEmitter<Instructor[]>();
  studio: StudioDto

  instructorToCreate = {
    firstName: '',
    lastName: '',
    profileImage: null as unknown as File,
  };

  constructor(private modalService: NgbModal,
              private studioService: StudioService,
              protected globals: Globals,
              private notification: ToastrService,
              private errorFormatter: ErrorFormatterService) {
  }


  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.instructorToCreate.profileImage = file;
    }
  }

  openInstructorModal(content: TemplateRef<any>): void {
    this.modalService.open(content);
  }

  addInstructor(modal: any): void {
    // Call the service to add the instructor
    this.studioService.addInstructor(this.instructorToCreate, this.studioId)
      .subscribe({
        next: (response) => {
          console.log('Instructor added successfully', response);
          this.instructorsChange.emit(this.instructors)
          this.instructorToCreate.firstName = ''
          this.instructorToCreate.lastName = ''
          this.instructors.push(response);
          this.notification.success("Instructor created successfully", "Success");
          modal.close(); // Close the modal
        },
        error: (error) => {
          // Handle error: Display an error message
          this.notification.error(this.errorFormatter.format(error), 'Could not add instructor', {
            enableHtml: true,
            timeOut: 10000
          });
          console.error('Error adding instructor', error);

        }
      });
  }


  protected readonly faPlus = faPlus;

  ngOnInit(): void {
    this.studioService.isCurrentUserAdmin(this.studioId).subscribe(
      isAdmin => {
        this.isCurrentUserAdminOfThisStudio = isAdmin;
      },
      error => {
        console.error('Error checking admin status:', error);
      }
    );
    this.studioService.getStudioById(this.studioId).subscribe(
      data => {
        this.studio = data;
        console.log(this.studio);
      },
      error => {
        console.error('Error getting studio');
      }
    )
  }
}

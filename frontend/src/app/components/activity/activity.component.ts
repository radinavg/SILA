import {AfterViewInit, Component, OnInit} from '@angular/core';
import {StudioActivity} from "../../dtos/studioActivity";
import {ActivatedRoute, RouterLink} from "@angular/router";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {ToastrService} from "ngx-toastr";
import {StudioActivityService} from "../../services/studio-activity.service";
import {DatePipe, NgClass, NgForOf, NgIf} from "@angular/common";
import {StudioForm} from "../../dtos/studio";
import {Globals} from "../../global/globals";
import {FormsModule} from "@angular/forms";
import {Instructor} from "../../dtos/instructor";
import {UserInfoDto, UserSearchDto} from "../../dtos/user";
import {UserService} from "../../services/user.service";
import {InvitationService} from "../../services/invitation.service";
import {ActivityInvitation, FriendshipRequest} from "../../dtos/invitations";
import {AuthService} from "../../services/auth.service";
import {StudioService} from "../../services/studio.service";
import * as L from 'leaflet';
import { ChangeDetectorRef } from '@angular/core';
import {MembershipService} from "../../services/membership.service";

@Component({
  selector: 'app-activity',
  standalone: true,
  imports: [
    DatePipe,
    NgForOf,
    RouterLink,
    NgIf,
    NgClass,
    FormsModule
  ],
  templateUrl: './activity.component.html',
  styleUrl: './activity.component.scss'
})
export class ActivityComponent implements OnInit, AfterViewInit {
  studioActivity: StudioActivity = {
    studioActivityId: 0,
    profileImage: null,
    name: '',
    description: '',
    dateTime: new Date(),
    duration: 0, // in minutes
    price: 0,
    type: '',
  };
  studio: StudioForm = {
    description: "",
    studioId: 0,
    location: "",
    longitude: 0,
    latitude: 0,
    name: "",
    profileImage: null,
    instructors: []
  };
  freeSpaces: number;
  isCollapsed = true;
  studioId = 0;

  alreadyBooked:boolean = false;
  selectedInstructor: Instructor;
  updatedTitle:string;
  updatedDescription:string;
  updatedDate:Date
  updatedDuration:number
  isCurrentUserAdminOfThisStudio: boolean = false;
  private map: L.Map;
  participatingFriends: UserInfoDto[]

  //Friend Invitation
  friendSearchQuery: UserSearchDto = {
    firstName: '',
    lastName: '',
    email: '',
    pageIndex: 0,
    pageSize: 10
  };
  friendUsers: UserInfoDto[] = []; //friends
  alreadySent: ActivityInvitation[];

  hasMembershipForStudio: boolean = false;

  constructor(private route: ActivatedRoute,
              private activityService: StudioActivityService,
              private errorFormatter: ErrorFormatterService,
              private notification: ToastrService,
              private modalService: NgbModal,
              private userService: UserService,
              private invitationService: InvitationService,
              protected authService:AuthService,
              protected globals: Globals,
              private studioService: StudioService,
              private cdr: ChangeDetectorRef,
              private membershipService: MembershipService
  ) {
  }

  toggleDescription() {
    this.isCollapsed = !this.isCollapsed;
  }

  ngOnInit(): void {
    const activityId = Number(this.route.snapshot.paramMap.get('id'));
    if (activityId) {
      this.activityService.findStudioActivityById(activityId).subscribe({
        next: (data) => {
          this.studioActivity = data;
          console.log('Studio activity data:', data);
          this.getParticipatingFriends();
          this.updateAlreadySent();
          this.checkIsAlreadyBooked();
          this.updatedDescription = this.studioActivity.description;
          this.updatedTitle = this.studioActivity.name;
          this.activityService.findStudioForActivity(activityId).subscribe({
            next: (studio) => {
              this.studio = studio;
              console.log('Studio data:', studio);
              this.freeSpaces = this.getFreeSpaces();
              this.studioService.isCurrentUserAdmin(this.studio.studioId).subscribe(
                isAdmin => {
                  this.isCurrentUserAdminOfThisStudio = isAdmin;
                },
                error => {
                  console.error('Error checking admin status:', error);
                }
              );
              this.membershipService.hasMembershipForStudio(this.studio.studioId).subscribe({
                next: (data) => {
                  this.hasMembershipForStudio = data
                  console.log(this.hasMembershipForStudio)
                }
              })
            },
            error: (e) => {
              console.error('Error fetching studio for activity:', e);
            },
          });

        },
        error: (err) => {
          console.error('Error fetching studio activity:', err);
        }
      });


    }

  }

  calculateEndTime(startTime: Date, duration: number): Date {
    const endTime = new Date(startTime);
    endTime.setMinutes(endTime.getMinutes() + duration);
    return endTime;
  }

  bookClass(): void {
    this.activityService.bookAnActivity(this.studioActivity).subscribe({
      next: (data) => {
        this.notification.success(`Successfully booked the activity: ${data.name}`);
        console.log('Booked activity:', data);
        this.alreadyBooked = true
        this.ngOnInit()

      },
      error: (err) => {
        console.error('Error booking the activity:', err);
        this.notification.error(err.error.detail);
      }
    });
  }

  getFreeSpaces(): number {
    const capacity = this.studioActivity?.capacity || 0;
    const currentUsers = this.studioActivity?.applicationUsers?.length || 0;
    return Math.max(capacity - currentUsers, 0);
  }

  openInstructorModal(instructorModal: any) {
    this.modalService.open(instructorModal);
  }

  assignInstructor(modal: any): void {
    const instructor = this.selectedInstructor;
    if (instructor) {
      this.studioActivity.instructor = instructor;
      this.activityService.updateStudioActivity(this.studioActivity).subscribe({
        next: () => {
          this.notification.success('Instructor assigned successfully!');
          modal.close();
        },
        error: (err) => {
          this.notification.error(this.errorFormatter.format(err), "Error assigning instructor", {
              enableHtml: true,
              timeOut: 10000,
            }
          );
        }
      });
    }
  }

  openStudioActivityTitleModal(studioTitleModal: any): void {
    this.modalService.open(studioTitleModal);
  }

  saveStudioActivityTitle(modal: any): void {
    this.studioActivity.name = this.updatedTitle
    this.activityService.updateStudioActivity(this.studioActivity).subscribe({
      next: () => {
        this.notification.success('Studio title updated successfully!');
        modal.close();
      },
      error: (err) => {
        this.notification.error(this.errorFormatter.format(err), "Error updating studio title", {
            enableHtml: true,
            timeOut: 10000,
          }
        );
      }
    });
  }

  openStudioActivityDescriptionModal(studioDescriptionModal: any): void {
    this.modalService.open(studioDescriptionModal);
  }

  saveStudioActivityDescription(modal: any): void {
    this.studioActivity.description = this.updatedDescription;
    this.activityService.updateStudioActivity(this.studioActivity).subscribe({
      next: () => {
        this.notification.success('Studio description updated successfully!');
        modal.close();
      },
      error: (err) => {
        this.notification.error(this.errorFormatter.format(err), "Error updating studio description", {
            enableHtml: true,
            timeOut: 10000,
          }
        );
      }
    });
  }

  openStudioActivityDateModal(studioDateModal: any): void {
    this.updatedDate = this.studioActivity.dateTime;
    this.modalService.open(studioDateModal);
  }

  saveStudioActivityDate(modal: any): void {
    this.studioActivity.dateTime = this.updatedDate;
    this.activityService.updateStudioActivity(this.studioActivity).subscribe({
      next: () => {
        this.notification.success('Studio activity date updated successfully!');
        modal.close();
      },
      error: (err) => {
        this.notification.error(this.errorFormatter.format(err), "Error updating studio activity date", {
            enableHtml: true,
            timeOut: 10000,
          }
        );
      }
    });
  }

  openStudioActivityDurationModal(studioDurationModal: any): void {
    this.updatedDuration = this.studioActivity.duration;
    this.modalService.open(studioDurationModal);
  }

  saveStudioActivityDuration(modal: any): void {
    this.studioActivity.duration = this.updatedDuration;
    this.activityService.updateStudioActivity(this.studioActivity).subscribe({
      next: () => {
        this.notification.success('Studio activity duration updated successfully!');
        modal.close();
      },
      error: (err) => {
        this.notification.error(this.errorFormatter.format(err), "Error updating studio activity duration", {
            enableHtml: true,
            timeOut: 10000,
          }
        );
      }
    });
  }

  ngAfterViewInit(): void {
    this.initMap();
    this.geocodeLocation();
  }
  private initMap(): void {
    this.map = L.map('map', {
      center: [48.20849, 16.37208], // Coordinates for Vienna
      zoom: 13
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
      attribution: '&copy; OpenStreetMap contributors'
    }).addTo(this.map);
  }

  private addMarkerAndCenterMap(lat: number, lon: number): void {
    if (this.map) {
      this.map.setView([lat, lon], 13);
      L.marker([lat, lon], {
        icon: L.icon({
          iconUrl: 'assets/map-marker-icon.png',
          iconSize: [30, 40],
          iconAnchor: [15, 40]
        })
      })
        .addTo(this.map)
        .bindPopup(`<strong>${this.studio.name}</strong><br>${this.studio.location}`)
        .openPopup();
    }
  }

  private geocodeLocation(): void {
    if (this.studio && this.studio.latitude && this.studio.longitude) {
      this.addMarkerAndCenterMap(this.studio.latitude, this.studio.longitude);
    } else {
      console.error('Geocoding: Latitude and Longitude are not available.');
    }
  }


  getParticipatingFriends() {
    this.activityService.getParticipatingFriends(this.studioActivity.studioActivityId).subscribe({
      next: (data: UserInfoDto[]) => {
        this.participatingFriends = data;
        console.log('Participating friends fetched successfully:', this.participatingFriends);
      },
      error: (error) => {
        console.error('Error fetching participating friends:', error);
      }
    });
  }

  searchFriends(): void {
    this.userService.searchMyFriends(this.friendSearchQuery).subscribe({
      next: (data) => {

        this.friendUsers = data;
        console.log('Search results:', this.friendUsers); // Log for debugging
      },
      error: (err) => {
        console.error('Error fetching users:', err);
      },
      complete: () => {
        console.log('Search completed');
      },
    });
  }
  updateAlreadySent() {
    const email = this.authService.getUserEmail();

    this.invitationService.getUserSentActivityInvitations(email).subscribe({
      next: (invitations) => {
        console.log('Received sent invitations:', invitations);
        this.alreadySent = invitations; // Assuming you have a state variable to store these invitations

      },
      error: (error) => {
        console.error('Error fetching sent activity invitations:', error);

      }
    });
  }


  isAlreadySent(user: any): boolean {
    console.log(this.alreadySent.some((sentRequest: any) => sentRequest.to.email === user.email))

    return this.alreadySent.some((sentRequest: any) => sentRequest.to.email === user.email);
  }

  inviteFriend(friend: UserInfoDto) {
    const request: ActivityInvitation = {
      activityInvitationId:0,
      from: null,
      to:friend,
      seen: false,
      studioActivity:this.studioActivity,
      requestDateTime: null
    };

    this.invitationService.sendActivityInvitation(request).subscribe({
      next: (response) => {
        console.log('Activity invitation sent successfully:', response);

        this.alreadySent.push(request)
      },
      error: (error) => {
        console.error('Error while sending activity invitation:', error);

      }
    });
  }

  openInviteFriendsModal(modal: any): void {
    const modalRef = this.modalService.open(modal);

    modalRef.result.finally(() => {
      this.resetModal();
    });
  }

  resetModal(): void {
    this.friendSearchQuery = {
      firstName: '',
      lastName: '',
      email: '',
      pageIndex: 0,
      pageSize: 10
    }; // Reset search query
    this.friendUsers = []; // Clear the search results array
    this.updateAlreadySent()
  }

  checkIsAlreadyBooked() {
    this.activityService.checkIsAlreadyBooked(this.studioActivity.studioActivityId).subscribe({
      next: (isBooked) => {
       this.alreadyBooked = isBooked;
      },
      error: (error) => {
        console.error('Error checking booking status:', error);
      }
    });
  }

  removeBooking(): void {
    this.activityService.unbookAnActivity(this.studioActivity).subscribe({
      next: (data) => {
        this.notification.success(`Successfully removed the booking`);
        console.log('Unbooked activity:', data);
        this.alreadyBooked = false;
        this.freeSpaces++;
      },

      error: (err) => {
        console.error('Error removing the booking:', err);
        const message = err.error.detail.split('. Conflicts')[0]; // Get the part before 'conflicts'
        this.notification.error(message);
      }

    });
  }




}

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Membership } from '../../../dtos/membership';
import { faPlus } from '@fortawesome/free-solid-svg-icons';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';

import { ToastrService } from 'ngx-toastr';
import {StudioService} from "../../../services/studio.service";
import {StudioDto} from "../../../dtos/studio";
import {MembershipService} from "../../../services/membership.service";

@Component({
  selector: 'app-studio-memberships',
  templateUrl: './studio-memberships.component.html',
  styleUrls: ['./studio-memberships.component.scss']
})
export class StudioMembershipsComponent implements OnInit {

  @Input() studioId!: number;
  @Input() memberships!: Membership[];
  @Output() membershipsChange = new EventEmitter<Membership[]>();
  studio: StudioDto


  newMembershipForm: UntypedFormGroup;
  protected readonly faPlus = faPlus;
  private currentSlide = 0;
  isCurrentUserAdminOfThisStudio: boolean = false;
  currentUserMemberships: Membership[] = [];

  constructor(
    private fb: UntypedFormBuilder,
    private studioService: StudioService,
    private modalService: NgbModal,
    private notification: ToastrService,
    private membershipService: MembershipService
  ) {
    this.newMembershipForm = this.fb.group({
      name: ['', Validators.required],
      duration: ['', [Validators.required, Validators.min(0)]],
      minDuration: ['', [Validators.required, Validators.min(0)]],
      price: ['', [Validators.required, Validators.min(0)]]
    });
  }

  ngOnInit(): void {
    this.membershipService.getMembershipsForUser().subscribe(
      (userMemberships) => {
        this.currentUserMemberships = userMemberships;
      },
      error => {
        console.error('Error fetching user memberships', error);
      }
    );
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

  get slideTransform(): string {
    return `translateX(-${this.currentSlide * 300}px)`;
  }

  slideLeft(): void {
    if (this.currentSlide > 0) {
      this.currentSlide--;
    }
  }

  slideRight(): void {
    if ((this.currentSlide + 1) * 300 < this.memberships.length * 300) {
      this.currentSlide++;
    }
  }

  openModal(modal: any): void {
    this.modalService.open(modal);
  }

  onSubmit(): void {
    if (this.newMembershipForm.invalid) {
      return;
    }

    const formValues = this.newMembershipForm.value;
    const newMembership: Membership = {
      name: formValues.name,
      duration: formValues.duration,
      minDuration: formValues.minDuration,
      price: formValues.price,
      applicationUserId: 1, // Replace with actual user ID logic
      studioActivityId: this.studioId
    };

    this.studioService.addMembership(this.studioId,newMembership).subscribe({
      next: (response) => {
        this.notification.success('Membership created successfully', 'Success');
        console.log(response)
        this.memberships.push(response);
        this.membershipsChange.emit(this.memberships);
        this.newMembershipForm.reset();

        this.modalService.dismissAll();
      },
      error: (err) => {
        this.notification.error('Failed to create membership', 'Error');
      }
    });
  }

  isUserSubscribedToMembership(membership: Membership): boolean {
    return this.currentUserMemberships.some(userMembership => userMembership.membershipId === membership.membershipId);
  }

  subscribeToMembership(membership: Membership): void {
    if (this.isUserSubscribedToMembership(membership)) {
      this.notification.info('You are already subscribed to this membership!', 'Info');
      return;
    }

    this.membershipService.addMembership(membership).subscribe({
      next: (response) => {
        this.notification.success('Successfully subscribed to membership!', 'Success');
        console.log("membership successfully", response);
        this.ngOnInit()
      },
      error: (err) => {
        this.notification.error('Failed to subscribe', 'Error');
        console.error('Subscription error:', err);
      }
    });
  }
}

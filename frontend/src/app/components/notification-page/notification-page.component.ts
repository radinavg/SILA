import {Component, OnInit} from '@angular/core';
import {ActivityInvitation, FriendshipRequest, Notifications} from "../../dtos/invitations";
import {ToastrService} from "ngx-toastr";
import {NotificationsService} from "../../services/notifications.service";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {RequestStatus} from "../../enums/request-status";
import {InvitationService} from "../../services/invitation.service";
import {Globals} from "../../global/globals";
import {Router, RouterLink} from "@angular/router";

@Component({
  selector: 'app-notification-page',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    RouterLink,
    DatePipe
  ],
  templateUrl: './notification-page.component.html',
  styleUrl: './notification-page.component.scss'
})
export class NotificationPageComponent implements OnInit {

  protected notifications: Notifications;
  protected mergedNotifications: (ActivityInvitation | FriendshipRequest)[]

  constructor(private notificationsService: NotificationsService,
              protected toastrService: ToastrService,
              private invitationService: InvitationService,
              protected globals: Globals,
              private router: Router) {
  }


  ngOnInit(): void {

    this.notificationsService.getAllUnprocessedNotifications().subscribe({
      next: notifications => {
        this.notifications = notifications;
        this.mergedNotifications = this.mergeNotifications(notifications);

        console.log(this.mergedNotifications)
      },
      error: err => {
        this.toastrService.error("Unable to fetch notifications for this user");
        console.error(err);
      }
    });

  }


  isNotificationFriendshipRequest(notification: any): boolean {
    return !('studioActivity' in notification);
  }

  mergeNotifications(notifications: Notifications) {

    const merged = [];
    const friendshipRequests = notifications.friendshipRequests;
    const activityInvitations = notifications.activityInvitations;

    let i = 0;
    let j = 0;

    // Merge the arrays while comparing their datetime fields
    while (i < friendshipRequests.length && j < activityInvitations.length) {
      if (new Date(friendshipRequests[i].requestDateTime) <= new Date(activityInvitations[j].requestDateTime)) {
        merged.push(friendshipRequests[i]);
        i++;
      } else {
        merged.push(activityInvitations[j]);
        j++;
      }
    }

    while (i < friendshipRequests.length) {
      merged.push(friendshipRequests[i]);
      i++;
    }

    while (j < activityInvitations.length) {
      merged.push(activityInvitations[j]);
      j++;
    }

    return merged;
  }


  onAcceptFriendshipRequest(friendRequest: FriendshipRequest) {
    friendRequest.status = RequestStatus.ACCEPTED
    this.invitationService.updateFriendshipRequest(friendRequest).subscribe({
      next: friendRequest => {
        this.toastrService.success("Friendship request accepted!")
        this.mergedNotifications.filter(n => {
          if (this.isNotificationFriendshipRequest(n)) {
            this.router.navigate(['/user/my-friends']);
            return (n as FriendshipRequest).friendRequestId !== friendRequest.friendRequestId;
          }
          return true;
        });
      },
      error: err => {
        this.toastrService.error(err);
        console.log(err);
      }
    });
  }

  onDeclineFriendshipRequest(friendRequest: FriendshipRequest) {
    friendRequest.status = RequestStatus.REJECTED
    this.invitationService.updateFriendshipRequest(friendRequest).subscribe({
      next: friendRequest => {
        this.toastrService.success("Friendship request declined!")
        this.mergedNotifications.filter(n => {
          if (this.isNotificationFriendshipRequest(n)) {
           return (n as FriendshipRequest).friendRequestId !== friendRequest.friendRequestId;
          }
          return true;
        });
      },
      error: err => {
        this.toastrService.error(err);
        console.log(err);
      }
    });
  }
}

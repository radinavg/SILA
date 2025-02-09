import {Component, OnInit} from '@angular/core';
import {StudioActivity, StudioActivityTypeSearchResponseDto} from "../../dtos/studioActivity";
import {UserService} from "../../services/user.service";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {MatPaginator} from "@angular/material/paginator";
import {NgForOf, NgIf} from "@angular/common";
import {RouterLink} from "@angular/router";
import {Globals} from "../../global/globals";

@Component({
  selector: 'app-bookings',
  standalone: true,
  imports: [
    MatPaginator,
    NgForOf,
    NgIf,
    RouterLink
  ],
  templateUrl: './bookings.component.html',
  styleUrl: './bookings.component.scss'
})
export class BookingsComponent implements OnInit {

  activities: StudioActivity[] = []
  currentBookings: StudioActivity[] = [];
  pastBookings: StudioActivity[] = [];
  defaultImage = 'assets/image-icon-600nw-211642900.webp';
  today = new Date();

  constructor(
    private userService: UserService,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService,
    private globals: Globals,
  ) {}

  ngOnInit(): void {
    this.userService.getActivities().subscribe({
      next: (data) => {
        this.activities = data;
        console.log("Activities: ", this.activities)
        this.currentBookings = this.activities.filter(activity => {
          const activityDate = new Date(activity.dateTime); // Assuming `dateTime` exists in activity
          return activityDate >= this.today; // Current and future bookings
        });
        console.log("Current Activities: ", this.currentBookings)
        this.pastBookings = this.activities.filter(activity => {
          const activityDate = new Date(activity.dateTime);
          return activityDate < this.today; // Past bookings
        });
        console.log("Past Activities: ", this.pastBookings)
      },
      error: (err) =>
        this.notification.error(this.errorFormatter.format(err), 'Could not fetch activities', {
          enableHtml: true,
          timeOut: 10000
        })
    });
  }

  getImage(activity: StudioActivity): string {
    const img = activity.profileImage;
    if (img && img.path !== '') {
      // Check if the URL starts with assets, then it's an image from our system,
      // this is now important because data generator has web images and previous implementation wouldn't work with our own images
      if (img.path.startsWith('assets/')) {
        return `${this.globals.backendImageUri}${img.path}`;
      }
      return img.path;
    }
    return this.defaultImage;
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

}

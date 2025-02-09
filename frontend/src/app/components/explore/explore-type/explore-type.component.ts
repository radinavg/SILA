import {AfterViewInit, Component} from '@angular/core';
import {ActivatedRoute, RouterLink} from "@angular/router";
import {NgForOf, NgIf} from "@angular/common";
import {StudioActivityService} from "../../../services/studio-activity.service";
import {SearchActivitiesDto, StudioActivityTypeSearchResponseDto} from "../../../dtos/studioActivity";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {Globals} from "../../../global/globals";

@Component({
  selector: 'app-explore-type',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    RouterLink,
    MatPaginator
  ],
  templateUrl: './explore-type.component.html',
  styleUrl: './explore-type.component.scss'
})
export class ExploreTypeComponent implements AfterViewInit {

  dataSource = new MatTableDataSource<StudioActivityTypeSearchResponseDto>();
  total: number = 0;
  paginator: MatPaginator;

  activityType: SearchActivitiesDto = { activityType: '', pageIndex: 0, pageSize: 12 };
  defaultImage = 'assets/image-icon-600nw-211642900.webp';

  constructor(private route: ActivatedRoute,
              private activityService: StudioActivityService,
              private  globals: Globals) {}

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.route.paramMap.subscribe(params => {
      this.activityType.activityType = params.get('activity');
      console.log('Exploring activity type:', this.activityType);
    });
    //this.activities = this.mockActivities();
    this.activityService.getActivitiesByType(this.activityType).subscribe(
      (data) => {
        this.dataSource.data = data.content;
        this.total = data.totalElements;
        console.log(this.dataSource.data)
      },
      (error) => {
        console.error('Error fetching activity types:', error);
      }
    );
  }


  formatActivityName(activity: string): string {
    return activity
      .toLowerCase()
      .split('_')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  getImage(activity: StudioActivityTypeSearchResponseDto): string {
    const imageUrl = activity.profileImagePath;
    if (imageUrl && imageUrl !== '') {
      // Check if the URL starts with assets, then it's an image from our system,
      // this is now important because data generator has web images and previous implementation wouldn't work with our own images
      if (imageUrl.startsWith('assets/')) {
        return `${this.globals.backendImageUri}${imageUrl}`;
      }
      return imageUrl;
    }
    return this.defaultImage;
  }

  // mockActivities(): StudioActivity[] {
  //   return [
  //     {
  //       studioActivityId: 1,
  //       image: 'assets/yoga-studio.jpg',
  //       name: 'Morning Yoga',
  //       description: 'A calming yoga session to start your day.',
  //       dateTime: new Date('2024-11-25T08:00:00'),
  //       duration: 60,
  //       price: 15,
  //       type: 'yoga',
  //       membership: null, // Can be a mock membership object
  //       applicationUsers: [], // Simulating empty users
  //     },
  //     {
  //       studioActivityId: 2,
  //       image: 'assets/yoga-studio-bg.jpg',
  //       name: 'Pilates for Beginners',
  //       description: 'An introductory Pilates session for new practitioners.',
  //       dateTime: new Date('2024-11-25T10:00:00'),
  //       duration: 75,
  //       price: 20,
  //       type: 'pilates',
  //       membership: null, // Can be a mock membership object
  //       applicationUsers: [], // Simulating empty users
  //     },
  //   ];
  // }

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

  pageChanged(event: any) {
    this.activityType.pageIndex = event.pageIndex;
    this.activityType.pageSize = event.pageSize;
    this.ngAfterViewInit();
  }



}

import {AfterViewInit, Component} from '@angular/core';
import {StudioActivityService} from "../../services/studio-activity.service";
import {NgForOf, NgIf} from "@angular/common";
import {RouterLink} from "@angular/router";
import {StudioSearchDto, StudioInfoDto} from "../../dtos/studio";
import {StudioService} from "../../services/studio.service";
import {FormsModule} from "@angular/forms";
import {ProfileImageDto} from "../../dtos/image";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";
import {debounceTime, Subject} from "rxjs";
import {Globals} from "../../global/globals";

@Component({
  selector: 'app-explore',
  standalone: true,
  imports: [
    NgForOf,
    RouterLink,
    FormsModule,
    NgIf,
    MatPaginator
  ],
  templateUrl: './explore.component.html',
  styleUrl: './explore.component.scss'
})
export class ExploreComponent implements AfterViewInit {

  activityTypes: string[] = [];

  dataSource = new MatTableDataSource<StudioInfoDto>();
  total: number = 0;
  paginator: MatPaginator;
  searchChangedObservable = new Subject<void>();

  studioSearch: StudioSearchDto = {
    name: '',
    location: '',
    pageIndex: 0,
    pageSize: 12
  };

  defaultImage = 'assets/image-icon-600nw-211642900.webp';

  constructor(private activityService: StudioActivityService,
              private studioService: StudioService,
              protected globals: Globals) {}

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.searchChangedObservable
      .pipe(debounceTime(300))
      .subscribe({next: () => this.searchStudios()});
    this.activityService.getActivityTypes().subscribe(
      (data) => {
        this.activityTypes = data;
      },
      (error) => {
        console.error('Error fetching activity types:', error);
      }
    );
    this.searchStudios()
  }

  searchChanged(): void {
    this.searchChangedObservable.next();
  }

  formatActivityName(activity: string): string {
    return activity
      .toLowerCase()
      .split('_')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  searchStudios(): void {
    this.studioService.searchStudios(this.studioSearch).subscribe(
      (data) => {
        this.dataSource.data = data.content;
        this.total = data.totalElements;
      },
      (error) => {
        console.error('Error fetching studios:', error);
      }
    );
  }

  getProfileImage(studio: StudioInfoDto): string {
    const imageUrl = studio.profileImagePath;
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

  pageChanged(event: any) {
    this.studioSearch.pageIndex = event.pageIndex;
    this.studioSearch.pageSize = event.pageSize;
    this.searchStudios();
  }

  // mockStudios(): StudioForm[] {
  //   return [
  //     {
  //       studioId: 1,
  //       name: 'Zen Yoga Studio',
  //       description: 'A peaceful space to connect with your inner self.',
  //       location: 'New York, NY',
  //       profileImage: 'assets/yoga-studio.jpg',
  //       images: [],
  //       studioActivities: [],
  //       faqs: [],
  //     },
  //     {
  //       studioId: 2,
  //       name: 'Flex Pilates Studio',
  //       description: 'Strengthen your body with Pilates exercises.',
  //       location: 'Los Angeles, CA',
  //       profileImage: 'assets/yoga-studio-2.jpg',
  //       images: [],
  //       studioActivities: [],
  //       faqs: [],
  //     }
  //   ];
  // }



}

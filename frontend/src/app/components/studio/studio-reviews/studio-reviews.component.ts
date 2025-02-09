import {Component, OnInit} from '@angular/core';
import {StudioDto} from "../../../dtos/studio";
import {ActivatedRoute} from "@angular/router";
import {StudioService} from "../../../services/studio.service";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {ToastrService} from "ngx-toastr";
import {DatePipe, NgForOf, NgIf} from "@angular/common";
import {Globals} from "../../../global/globals";
import {UserDetailDto} from "../../../dtos/user";
import {Review, ReviewCreateDto, ReviewSortDto} from "../../../dtos/review";
import {AuthService} from "../../../services/auth.service";
import {ReviewService} from "../../../services/review.service";
import {FormsModule} from "@angular/forms";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";

@Component({
  selector: 'app-studio-reviews',
  standalone: true,
  imports: [
    NgForOf,
    NgIf,
    FormsModule,
    DatePipe,
    MatPaginator
  ],
  templateUrl: './studio-reviews.component.html',
  styleUrl: './studio-reviews.component.scss'
})
export class StudioReviewsComponent implements OnInit{

  protected studio: StudioDto | null = null;

  rating: number = 0;

  hoveredRating: number = 0;

  ratingStars = [1, 2, 3, 4, 5];

  reviewsDataSource = new MatTableDataSource<Review>();
  sortParams: ReviewSortDto = {
    pageIndex: 0,
    pageSize: 10
  };

  total: number = 0;
  paginator: MatPaginator;

  constructor(private route: ActivatedRoute,
              private studioService: StudioService,
              private errorFormatter: ErrorFormatterService,
              private notification: ToastrService,
              protected globals: Globals,
              public authService: AuthService,
              private reviewService: ReviewService
  ) {
  }

  ngOnInit(): void {
    this.reviewsDataSource.paginator = this.paginator;
    this.loadStudio();
  }

  loadReviews(): void {
    const studioId = Number(this.route.snapshot.paramMap.get('id'));
    this.reviewService.sortReviews(studioId, this.sortParams).subscribe({
      next: res => {
        this.reviewsDataSource.data = res.content;
        this.total = res.totalElements;
      }
    })
  }

  loadStudio(): void {
    const studioId = Number(this.route.snapshot.paramMap.get('id'));
    if (studioId) {
      this.studioService.getStudioById(studioId).subscribe(
        (studio: StudioDto) => {
          this.studio = studio;
          this.loadReviews();
          this.calculateAverageRating()
          console.log("Studio:", this.studio);
        },
        error => {
          console.error('Error loading studio:', error);
        }
      );
    }
  }
  getUserProfileImagePath(user: UserDetailDto): string {
    return user.profileImagePath
      ? `${this.globals.backendImageUri}${user.profileImagePath}`
      : 'assets/default-profile.png';
  }

  getStars(rating: number): string[] {
    const roundedRating = Math.round(rating); // Round to the nearest whole number
    return Array(5)
      .fill('')
      .map((_, i) => (i < roundedRating ? 'full' : 'empty'));
  }

  hasUserReviewed(): boolean {
    return this.reviewsDataSource.data.some(review => review.user.email === this.authService.getUserEmail());
  }


  setRating(rating: number): void {
    this.rating = rating;
  }

  setHoveredRating(rating: number): void {
    this.hoveredRating = rating;
  }

  clearHoveredRating(): void {
    this.hoveredRating = 0;
  }

  submitReview(rating: number, text: string) {
    const newReview: ReviewCreateDto = {
      text: text,
      rating: rating
    };

    this.reviewService.addReview(this.studio.studioId, newReview).subscribe({
      next: (data) => {
        console.log("Review: ", data)
        this.notification.success('Review submitted successfully!');
        this.ngOnInit();
      },
      error: (err) =>
        this.notification.error(this.errorFormatter.format(err), 'Could not add review', {
          enableHtml: true,
          timeOut: 10000
        })
    });

  }

  deleteReview(id: number) {
    this.reviewService.deleteReview(id).subscribe({
      next: (data) => {
        console.log("Review: ", data)
        this.notification.success('Review deleted successfully!');
        this.ngOnInit();
      },
      error: (err) =>
        this.notification.error(this.errorFormatter.format(err), 'Could not delete review', {
          enableHtml: true,
          timeOut: 10000
        })
    })
  }

  getAverageStars(): string[] {
    const filledStars = Math.round(this.averageRating); // Round to nearest whole number
    return Array(5)
      .fill('☆')
      .map((star, index) => (index < filledStars ? '★' : '☆'));
  }

  averageRating: number = 0;
  reviewCount: number = 0;

  calculateAverageRating(): void {
    if (this.studio.reviewsLength && this.studio.reviewsLength > 0) {
      this.averageRating = this.studio.averageReview;
      this.reviewCount = this.studio.reviewsLength;
    } else {
      this.averageRating = 0;
      this.reviewCount = 0;
    }
  }

  editingReview: Review | null = null;

  startEdit(review: Review): void {
    this.editingReview = { ...review }; // Create a copy to prevent live updates
  }

  cancelEdit(): void {
    this.editingReview = null; // Cancel editing mode
  }

  saveEdit(): void {
    if (this.editingReview) {
      this.reviewService.updateReview(this.editingReview.reviewId, this.editingReview).subscribe({
        next: (updatedReview: Review) => {
          this.notification.success('Review updated successfully!');
          this.ngOnInit();
          this.editingReview = null;
        },
        error: (err) => {
          this.notification.error(this.errorFormatter.format(err), 'Could not update review', {
            enableHtml: true,
            timeOut: 10000
          });
        }
      });
    }
  }

  setEditingRating(rating: number): void {
    if (this.editingReview) {
      this.editingReview.rating = rating; // Update the editing review's rating
    }
  }

  pageChanged(event: any) {
    this.sortParams.pageIndex = event.pageIndex;
    this.sortParams.pageSize = event.pageSize;
    this.loadStudio();
  }


}

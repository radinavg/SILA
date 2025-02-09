import {UserDetailDto} from "./user";
import {StudioDto} from "./studio";

export class Review {
  reviewId: number;
  text: string;
  rating: number;
  user: UserDetailDto;
  createdAt: Date;
}

export class ReviewCreateDto {
  text: string;
  rating: number;
}


export class ReviewSortDto {
  pageIndex: number;
  pageSize: number;
}

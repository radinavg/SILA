import {StudioActivity} from "./studioActivity";
import {Faq} from "./faq";
import {Membership} from "./membership";
import {GalleryImageDto, ProfileImageDto} from "./image";
import {Review} from "./review";
import {Instructor} from "./instructor";


export class StudioForm {
  studioId: number;
  name: string;
  description: string;
  location: string;
  longitude: number;
  latitude: number;
  profileImage: ProfileImageDto;
  instructors: Instructor[]

}

export class StudioCreateDto {
  name: string;
  description: string;
  location: string;
  longitude: number;
  latitude: number;
  email: string;
  password: string;
  confirmPassword: string;
  profileImageFile: File;
}

export interface CreatedStudioDto {
  name: string;
  description: string;
  location: string;
}

export class StudioDto {
  studioId: number;
  name: string;
  description: string;
  location: string;
  profileImage: ProfileImageDto;
  galleryImages: GalleryImageDto[];
  memberships?: Membership[];
  faqs: Faq[];
  isFavouriteForUser: boolean;
  reviewsLength: number;
  averageReview: number;
  instructors: Instructor[]
}


export interface StudioSearchDto {
  name?: string;
  location?: string;
  pageIndex: number;
  pageSize: number;
}

export interface StudioInfoDto {
  studioId: number;
  name: string;
  description: string;
  location: string;
  email: string;
  profileImagePath: string;
}

export interface ApproveStudioDto {
  studioId: number;
  approved: boolean;
}

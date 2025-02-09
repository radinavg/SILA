import {Membership} from "./membership";
import {UserCreateDto} from "./user";
import {ProfileImageDto} from "./image";
import {Instructor} from "./instructor";
import {SkillLevel} from "../enums/skillLevel";

export class StudioActivity {
  studioActivityId: number;
  profileImage: ProfileImageDto;
  name: string;
  description: string;
  dateTime: Date;
  duration: number;
  price: number;
  type: string;
  membership?: Membership;
  capacity?: number;
  applicationUsers?: UserCreateDto[];
  instructor?:Instructor;
}

export interface StudioActivityListDto {
  studioActivityId: number;
  name: string;
  profileImage: ProfileImageDto;
  description: string;
  dateTime: Date;
  duration: number;
  price: number;
}

export class StudioActivityCreateDto {
  name: string;
  profileImageFile: File;
  description: string;
  dateTime: string;
  duration: number;
  price: number;
  type: string;
  studioId: string;
  capacity: string;
  skillLevel: SkillLevel;
  equipment: boolean;
}


export interface SearchActivitiesDto {
  activityType: string;
  pageIndex: number;
  pageSize: number;

}

export interface StudioActivityTypeSearchResponseDto {
  studioActivityId: number;
  name: string;
  description: string;
  dateTime: Date;
  duration: number;
  price: number;
  profileImagePath: string;
}

import {Gender} from "../enums/gender";
import {ActivityInvitation, FriendshipRequest} from "./invitations";

export interface UserCreateDto {
  firstName: string;
  lastName: string;
  gender: Gender;
  email: string;
  password: string;
  passwordConfirmation: string;
  location: string;
  longitude: number;
  latitude: number;
}

export interface ResetUserPasswordDto {
  email: string
}

export interface UserPreferencesDto {
  prefersIndividual: boolean;
  prefersTeam: boolean;
  prefersWaterBased: boolean;
  prefersIndoor: boolean;
  prefersOutdoor: boolean;
  prefersBothIndoorAndOutdoor: boolean;
  prefersWarmClimate: boolean;
  prefersColdClimate: boolean;
  rainCompatibility: boolean;
  windSuitability: boolean;
  focusUpperBody: boolean;
  focusLowerBody: boolean;
  focusCore: boolean;
  focusFullBody: boolean;
  isBeginner: boolean;
  isIntermediate: boolean;
  isAdvanced: boolean;
  physicalDemandLevel: number; // 1-10
  prefersLowIntensity: boolean;
  prefersModerateIntensity: boolean;
  prefersHighIntensity: boolean;
  goalStrength: boolean;
  goalEndurance: boolean;
  goalFlexibility: boolean;
  goalBalanceCoordination: boolean;
  goalMentalFocus: boolean;
}

export interface UserInfoDto {
  firstName: string;
  lastName: string;
  email: string;
  location: string;
  gender: Gender;
  profileImagePath: string;
}

export interface UserUpdateDto {
  firstName: string;
  lastName: string;
  email: string;
  location: string;
  longitude?: number;
  latitude?: number;
  gender: Gender;
  profileImagePath: string;
}

export interface UserDetailDto {
  firstName: string;
  lastName: string;
  email: string;
  location: string;
  gender: Gender;
  isLocked: boolean;
  isAdmin: boolean;
  isStudioAdmin: boolean;
  profileImagePath: string;
  friendRequests:FriendshipRequest[];
  activityInvitations:ActivityInvitation[]
}

export interface User {
  id: number;
  firstName: string;
  email: string;
}

export interface UserUpdatePasswordDto {
  currentPassword: string,
  newPassword: string,
  confirmationPassword: string
}

export interface UserSearchDto {
  firstName?: string;
  lastName?: string;
  email?: string;
  isAdmin?: boolean;
  isLocked?: boolean;
  pageIndex: number;
  pageSize: number;
}

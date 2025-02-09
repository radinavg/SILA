import {User, UserInfoDto} from "./user";
import {StudioActivity} from "./studioActivity";
import {RequestStatus} from "../enums/request-status";


export class FriendshipRequest {
  friendRequestId: number;
  from:UserInfoDto;
  to:UserInfoDto;
  status:RequestStatus;
  requestDateTime:Date;
}

export class ActivityInvitation {
  activityInvitationId: number;
  from:UserInfoDto;
  to:UserInfoDto;
  studioActivity:StudioActivity;
  seen:boolean;
  requestDateTime:Date;
}

export class Notifications {

  friendshipRequests: FriendshipRequest[];
  activityInvitations: ActivityInvitation[];
}

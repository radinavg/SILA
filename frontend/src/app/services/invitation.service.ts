import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient, HttpParams} from "@angular/common/http";
import {ActivityInvitation, FriendshipRequest} from "../dtos/invitations";
import {Observable} from "rxjs";
import {UserInfoDto} from "../dtos/user";


const baseUri = environment.backendUrl + '/api/v1/invitation';
@Injectable({
  providedIn: 'root'
})
export class InvitationService {

  constructor(
    private http : HttpClient,
  ) {
  }

  sendFriendRequest(friendRequestDto: FriendshipRequest): Observable<FriendshipRequest> {

    return this.http.post<FriendshipRequest>(baseUri, friendRequestDto);
  }

  sendActivityInvitation(activityInvitation:ActivityInvitation):Observable<ActivityInvitation>{
   return this.http.post<ActivityInvitation>(baseUri + "/activityInvitation", activityInvitation);

  }

  getUserSentFriendRequests(email:String): Observable<FriendshipRequest[]> {
    return this.http.get<FriendshipRequest[]>(baseUri + "/my_friend_invitations/" + email)
  }

  getUserSentActivityInvitations(email:String): Observable<ActivityInvitation[]> {
    return this.http.get<ActivityInvitation[]>(baseUri + "/my_activity_invitations/" + email)
  }

  getAllUnprocessedNotifications(): Observable<FriendshipRequest[]> {
    return this.http.get<FriendshipRequest[]>(baseUri);
  }

  updateFriendshipRequest(friendRequest: FriendshipRequest): Observable<FriendshipRequest> {

    console.log(friendRequest)

    return this.http.put<FriendshipRequest>(baseUri, friendRequest);
  }
}

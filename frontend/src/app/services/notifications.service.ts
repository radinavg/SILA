import { Injectable } from '@angular/core';
import {Observable} from "rxjs";
import {FriendshipRequest, Notifications} from "../dtos/invitations";
import {HttpClient} from "@angular/common/http";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class NotificationsService {

  baseUri = environment.backendUrl + '/api/v1/notifications';

  constructor(private http : HttpClient,) { }


  getAllUnprocessedNotifications(): Observable<Notifications> {
    return this.http.get<Notifications>(this.baseUri);
  }


}

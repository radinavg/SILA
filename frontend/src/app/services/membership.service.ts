import { Injectable } from '@angular/core';
import {environment} from "../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {Membership} from "../dtos/membership";

const baseUri = environment.backendUrl + '/api/v1/memberships';

@Injectable({
  providedIn: 'root'
})
export class MembershipService {

  constructor(private httpClient: HttpClient,) { }


  getMembershipsForUser(): Observable<Membership[]> {
    return this.httpClient.get<Membership[]>(`${baseUri}`);
  }


  addMembership(membershipDto: Membership): Observable<Membership> {
    return this.httpClient.post<Membership>(`${baseUri}`, membershipDto);
  }


  deleteMembership(membershipId: number): Observable<void> {
    return this.httpClient.delete<void>(`${baseUri}/${membershipId}`);
  }

  hasMembershipForStudio(studioId: number): Observable<boolean> {
    return this.httpClient.get<boolean>(`${baseUri}/hasMembershipForStudio/` + studioId);
  }
}

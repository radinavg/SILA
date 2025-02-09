import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {catchError, Observable} from "rxjs";
import {
  SearchActivitiesDto,
  StudioActivity,
  StudioActivityCreateDto, StudioActivityListDto,
  StudioActivityTypeSearchResponseDto
} from "../dtos/studioActivity";
import {StudioForm} from "../dtos/studio";
import {PaginatedResponse} from "../dtos/paginator";
import {environment} from "../../environments/environment";
import {UserInfoDto} from "../dtos/user";

const baseUri = environment.backendUrl + '/api/v1/studio-activities';

@Injectable({
  providedIn: 'root'
})
export class StudioActivityService {

  constructor(private httpClient: HttpClient) {
  }

  createStudioActivity(studioActivityCreateDto: StudioActivityCreateDto): Observable<StudioActivityListDto> {

    const formData = new FormData();

    for (const key in studioActivityCreateDto) {
      formData.append(key, studioActivityCreateDto[key]);
    }

    console.log(formData);

    return this.httpClient.post<StudioActivityListDto>(baseUri, formData);
  }

  updateStudioActivity(studioActivity: StudioActivity): Observable<StudioActivity> {
    return this.httpClient.put<StudioActivity>(baseUri, studioActivity);
  }

  findStudioActivityById(id: number): Observable<StudioActivity> {
    return this.httpClient.get<StudioActivity>(baseUri + '/' + id);
  }

  deleteStudioActivityById(id: number): Observable<StudioActivity> {
    return this.httpClient.delete<StudioActivity>(baseUri + '/' + id);
  }

  getActivityTypes(): Observable<string[]> {
    return this.httpClient.get<string[]>(baseUri + '/types');
  }

  getActivitiesByType(type: SearchActivitiesDto): Observable<PaginatedResponse<StudioActivityTypeSearchResponseDto>> {
    let params = new HttpParams();
    params = params.append('activityType', type.activityType)
    params = params.append('pageIndex', type.pageIndex.toString());
    params = params.append('pageSize', type.pageSize.toString());

    return this.httpClient.get<PaginatedResponse<StudioActivityTypeSearchResponseDto>>(baseUri + '/exploreTypes', {params});
  }

  findStudioForActivity(activityId: number): Observable<StudioForm> {
    return this.httpClient.get<StudioForm>(baseUri + '/activityId/' + activityId)
  }

  bookAnActivity(activity: StudioActivity): Observable<StudioActivity> {
    return this.httpClient.post<StudioActivity>(baseUri + '/book', activity)
  }

  unbookAnActivity(activity: StudioActivity): Observable<StudioActivity> {
    return this.httpClient.post<StudioActivity>(baseUri + '/unbook', activity)
  }

  getAllActivityTypes(): Observable<string[]> {
    return this.httpClient.get<string[]>(baseUri + '/types');
  }

  getParticipatingFriends(activityId: number): Observable<UserInfoDto[]> {
    return this.httpClient.get<UserInfoDto[]>(baseUri + "/get_participated_friends/" + activityId)
  }

  checkIsAlreadyBooked(activityId: number): Observable<boolean> {
    console.log(`Checking if activity ${activityId} is already booked`);

    return this.httpClient.get<boolean>(`${baseUri}/is_already_booked/${activityId}`)

  }

  getStudioActivities(studioId: number): Observable<StudioActivityListDto[]> {
    return this.httpClient.get<StudioActivityListDto[]>(baseUri + "/studio/" + studioId + "/activities");
  }

}

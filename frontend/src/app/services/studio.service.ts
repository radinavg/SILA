import { Injectable } from '@angular/core';
import {StudioCreateDto, StudioDto, StudioForm} from "../dtos/studio";
import {Observable} from "rxjs";
import {
  ApproveStudioDto,
  CreatedStudioDto,
  StudioSearchDto,
  StudioInfoDto
} from "../dtos/studio";
import {HttpClient, HttpParams} from "@angular/common/http";
import {Faq} from "../dtos/faq";
import {Membership} from "../dtos/membership";
import {environment} from "../../environments/environment";
import {StudioUpdateDto} from "../dtos/studio-update-dto";
import {PaginatedResponse} from "../dtos/paginator";
import {Instructor, InstructorCreateDto} from "../dtos/instructor";

const baseUri = environment.backendUrl + '/api/v1/studios';

@Injectable({
  providedIn: 'root'
})
export class StudioService {

  constructor(private httpClient: HttpClient,) { }

  createStudio(studio: StudioCreateDto): Observable<CreatedStudioDto> {

    const formData = new FormData();

    for (const key in studio) {
      formData.append(key, studio[key]);
    }

    console.log(formData)
    return this.httpClient.post<CreatedStudioDto>(baseUri, formData);
  }

  getNotApprovedStudios(): Observable<StudioInfoDto[]> {
    return this.httpClient.get<StudioInfoDto[]>(baseUri + '/notApproved');
  }

  approveStudio(studio: ApproveStudioDto): Observable<StudioInfoDto> {
    return this.httpClient.put<StudioInfoDto>(baseUri + "/approve", studio);
  }

  updateStudioByStudioId(studio: StudioUpdateDto, studioId: number): Observable<StudioDto> {
    console.log(studio);
    console.log(baseUri + `/${studioId}`)
    return this.httpClient.put<StudioDto>(baseUri + `/${studioId}`, studio);
  }

  updateProfileImage(file: File, studioId: number): Observable<StudioDto> {
    const formData = new FormData()
    formData.append('file', file);

    return this.httpClient.put<StudioDto>(baseUri + `/${studioId}/update-profile-image`, formData);
  }


  deleteStudio(id: number): Observable<StudioForm> {
    return this.httpClient.delete<StudioForm>(baseUri + '/' + id);
  }

  getStudioById(id: number): Observable<StudioDto> {
    return this.httpClient.get<StudioDto>(baseUri + '/' + id);
  }

  addFAQ(id: number, faq: Faq): Observable<Faq> {
    return this.httpClient.post<Faq>(baseUri + '/' + id + '/faqs', faq);
  }

  addMembership(id: number, membership: Membership): Observable<Membership> {
    return this.httpClient.post<Membership>(baseUri + '/' + id + '/memberships', membership);
  }

  addFavouriteStudio(studioId: number): Observable<StudioDto> {
    return this.httpClient.post<StudioDto>(`${baseUri}/${studioId}/add-favourite`, null);
  }

  removeFavouriteStudio(studioId: number): Observable<StudioDto> {
    return this.httpClient.delete<StudioDto>(`${baseUri}/${studioId}/remove-favourite`);
  }

  getFavouriteStudios(): Observable<StudioDto[]> {
    console.log(baseUri + '/favourite')
    return this.httpClient.get<StudioDto[]>(baseUri + '/favourite');
  }

  searchStudios(studioSearch: StudioSearchDto): Observable<PaginatedResponse<StudioInfoDto>> {
    let params = new HttpParams();

    if (studioSearch.name) {
      params = params.append('name', studioSearch.name);
    }
    if (studioSearch.location) {
      params = params.append('location', studioSearch.location);
    }

    params = params.append('pageIndex', studioSearch.pageIndex.toString());
    params = params.append('pageSize', studioSearch.pageSize.toString());

    return this.httpClient.get<PaginatedResponse<StudioInfoDto>>(baseUri + '/search', { params });
  }

  getStudioIdByAdmin(email: string): Observable<number> {
    return this.httpClient.get<number>(baseUri + "/getStudioByAdmin/" + email);
  }

  isCurrentUserAdmin(studioId: number): Observable<boolean> {
    return this.httpClient.get<boolean>(baseUri + "/isAdmin/" + studioId);
  }

  addInstructor(instructor: InstructorCreateDto, studioId: number): Observable<Instructor> {
    const formData = new FormData();
    for (const key in instructor) {
      formData.append(key, instructor[key]);
    }

    return this.httpClient.post<Instructor>(baseUri + "/add-instructor/" + studioId, formData);
  }

  uploadGalleryImages(formData: FormData, studioId: number): Observable<StudioDto> {
    return this.httpClient.put<StudioDto>(`${baseUri}/${studioId}/add-gallery-images`, formData);
  }

}

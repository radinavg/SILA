import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {environment} from "../../environments/environment";
import {Review, ReviewCreateDto, ReviewSortDto} from "../dtos/review";
import {Observable} from "rxjs";
import {PaginatedResponse} from "../dtos/paginator";
const baseUri = environment.backendUrl + '/api/v1/reviews';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  constructor(private httpClient: HttpClient) { }

  addReview(id: number, review: ReviewCreateDto): Observable<Review> {
    return this.httpClient.post<Review>(baseUri + '/' + id, review);
  }

  deleteReview(id: number): Observable<Review> {
    return this.httpClient.delete<Review>(baseUri + '/' + id);
  }

  updateReview(id: number, review: ReviewCreateDto): Observable<Review> {
    return this.httpClient.put<Review>(baseUri + '/' + id, review);
  }

  sortReviews(studioId: number, sortDto: ReviewSortDto): Observable<PaginatedResponse<Review>> {
    let params = new HttpParams();

    params = params.append('pageIndex', sortDto.pageIndex.toString());
    params = params.append('pageSize', sortDto.pageSize.toString());

    return this.httpClient.get<PaginatedResponse<Review>>(`${baseUri}/${studioId}`, { params });
  }

}

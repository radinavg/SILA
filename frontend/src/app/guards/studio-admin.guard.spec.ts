import {inject, TestBed} from '@angular/core/testing';

import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {StudioAdminAuthGuard} from "./studio-admin-auth.guard";


describe('StudioAdminAuthGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule, ReactiveFormsModule],
      providers: [StudioAdminAuthGuard]
    });
  });

  it('should ...', inject([StudioAdminAuthGuard], (guard: StudioAdminAuthGuard) => {
    expect(guard).toBeTruthy();
  }));
});

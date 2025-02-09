import {inject, TestBed} from '@angular/core/testing';

import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {AnonymousUserGuard} from "./anonymous-user.guard";

describe('AdminAuthGuard', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule, ReactiveFormsModule],
      providers: [AnonymousUserGuard]
    });
  });

  it('should ...', inject([AnonymousUserGuard], (guard: AnonymousUserGuard) => {
    expect(guard).toBeTruthy();
  }));
});

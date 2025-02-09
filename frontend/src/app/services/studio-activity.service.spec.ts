import { TestBed } from '@angular/core/testing';

import { StudioActivityService } from './studio-activity.service';

describe('StudioActivityService', () => {
  let service: StudioActivityService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StudioActivityService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

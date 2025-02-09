import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProfileImageService {
  private reloadSubject = new BehaviorSubject<void>(null);

  reload$ = this.reloadSubject.asObservable();

  triggerReload(): void {
    this.reloadSubject.next();
  }
}

import {Injectable} from "@angular/core";
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class StudioActivityReloadService {
  private reloadSubject = new BehaviorSubject<void>(null);

  reload$ = this.reloadSubject.asObservable();

  triggerReload(): void {
    this.reloadSubject.next();
  }
}

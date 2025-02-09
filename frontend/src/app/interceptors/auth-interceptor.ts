import {Injectable} from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import {AuthService} from '../services/auth.service';
import {catchError, Observable, throwError} from 'rxjs';
import {Globals} from '../global/globals';
import {Router} from "@angular/router";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private globals: Globals,
    private router: Router
  ) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const authUri = this.globals.backendUri + '/authentication';
    const resetPasswordUri = this.globals.backendUri + '/users/reset/password'
    const addFormStudioUri = this.globals.backendUri + '/studios'
    const registerUser = this.globals.backendUri + '/users/create'


    if (req.url === authUri || req.url === resetPasswordUri || req.url === addFormStudioUri || req.url === registerUser) {
      return next.handle(req);
    }

    const authReq = req.clone({
      headers: req.headers.set('Authorization', 'Bearer ' + this.authService.getToken())
    });

    return next.handle(authReq).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 403) {
          this.authService.logoutUser();
          this.router.navigate(['/']);
        }
        return throwError(error);
      })
    );
  }
}

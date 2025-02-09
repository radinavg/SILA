import {Injectable, SecurityContext} from '@angular/core';
import {DomSanitizer} from '@angular/platform-browser';
import {Router} from "@angular/router";

@Injectable({
  providedIn: 'root'
})
export class ErrorFormatterService {

  constructor(
    private domSanitizer: DomSanitizer,
    private router: Router
  ) { }

  /**
   * Function that formats error in a manner of a list.
   *
   * @return Formated String error message
   */
  format(error: any): string {
    let message = this.domSanitizer.sanitize(SecurityContext.HTML, error.error.message) ?? '';

    if (error.status === 0) {
      this.router.navigate(['/']);
      return "";
    }

    if (!!error.error.errors) {
      message += ':<ul>';
      for (const e of error.error.errors) {
        /* Use Angular's DomSanitizer to strip dangerous parts out of the HTML
         * before putting it into the error message.
         * Toastr already does this, but it can't hurt to do here too,
         * in case the library every fails to do it.
         */
        const sanE = this.domSanitizer.sanitize(SecurityContext.HTML, e);
        message += `<li>${sanE}</li>`;
      }
      message += '</ul>';
    } else {
      if (error.error.detail) {
        const sanE = this.domSanitizer.sanitize(SecurityContext.HTML, error.error.detail);
        message += `<li>${sanE}</li>`;
      } else {
        message += '.';
      }
    }
    return message;
  }
}

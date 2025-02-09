import {Injectable} from '@angular/core';
import {FormGroup} from "@angular/forms";
import {from, Observable, of} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class Util {
  public PasswordMatching (password: string, passwordToMatch: string){
    return (formGroup: FormGroup) => {
      const pass = formGroup.controls[password];
      const passToMatch = formGroup.controls[passwordToMatch];

      if (!passToMatch.value) {
        passToMatch.setErrors({ required: true });
        return;
      }

      if(passToMatch.errors && !passToMatch.errors['mustMatch']) {
        return;
      }

      if(passToMatch.value === '' || passToMatch.value !== pass.value){
        passToMatch.setErrors({mustMatch: true})
      } else {
        passToMatch.setErrors(null);
      }
    };
  }

  fetchLocationSuggestions(query: string): Observable<{ display_name: string; lon: number; lat: number }[]> {
    if (!query || query.length < 3) {
      return of([]); // Return an empty array if the query is too short
    }

    const baseUrl = 'https://nominatim.openstreetmap.org/search';
    const params = new URLSearchParams({
      q: `${query}, Vienna Austria`,
      format: 'json',
      limit: '10',
    }).toString();

    const url = `${baseUrl}?${params}`;
    console.log('url', url);

    return from(
      fetch(url)
        .then(response => {
          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }
          return response.json();
        })
        .then(data => {
          console.log('Raw API data:', data);
          return data.map((item: any) => ({
            display_name: item.display_name,
            lon: item.lon,
            lat: item.lat,
          }));
        })
        .catch(err => {
          console.error('Error fetching location data:', err);
          return [];
        })
    );
  }

}



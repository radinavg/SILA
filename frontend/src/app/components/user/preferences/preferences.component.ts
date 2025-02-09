import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {UserPreferencesDto} from "../../../dtos/user";
import {UserService} from "../../../services/user.service";
import {Router} from "@angular/router";
import {ToastrService} from "ngx-toastr";
import {firstValueFrom} from "rxjs";

@Component({
    selector: 'app-preferences',
    templateUrl: './preferences.component.html',
    styleUrls: ['./preferences.component.scss']
})
export class PreferencesComponent implements OnInit {

    preferencesForm: UntypedFormGroup;

    constructor(
        private formBuilder: UntypedFormBuilder,
        private userService: UserService,
        private router: Router,
        private notification: ToastrService
    ) {
        this.preferencesForm = this.formBuilder.group({
            prefersIndividual: [false],
            prefersTeam: [false],
            prefersWaterBased: [false],
            prefersIndoor: [false],
            prefersOutdoor: [false],
            prefersBothIndoorAndOutdoor: [false],
            prefersWarmClimate: [false],
            prefersColdClimate: [false],
            rainCompatibility: [false],
            windSuitability: [false],
            focusUpperBody: [false],
            focusLowerBody: [false],
            focusCore: [false],
            focusFullBody: [false],
            isBeginner: [false],
            isIntermediate: [false],
            isAdvanced: [false],
            physicalDemandLevel: [5], // Default to 5
            prefersLowIntensity: [false],
            prefersModerateIntensity: [false],
            prefersHighIntensity: [false],
            goalStrength: [false],
            goalEndurance: [false],
            goalFlexibility: [false],
            goalBalanceCoordination: [false],
            goalMentalFocus: [false],
        });
    }

    ngOnInit(): void {
    }

    async submitPreferences() {
        const preferencesSet = await firstValueFrom(this.userService.checkPreferencesSet());

        if (preferencesSet) {
            this.updatePreferences();
        } else {
            this.createPreferences();
        }
    }

    createPreferences(): void {
        const preferences: UserPreferencesDto = this.preferencesForm.value;

        this.userService.createUserPreferences(preferences).subscribe({
            next: () => {
                this.notification.success("Preferences created successfully!");
                this.router.navigate(['/home']);
            },
            error: (err) => {
                this.notification.error("Error creating preferences");
                console.error(err);
            }
        });
    }

    updatePreferences(): void {
        const preferences: UserPreferencesDto = this.preferencesForm.value;

        this.userService.updateUserPreferences(preferences).subscribe({
            next: () => {
                this.notification.success("Preferences updated successfully!");
                this.router.navigate(['/home']);
            },
            error: (err) => {
                this.notification.error("Error updating preferences");
                console.error(err);
            }
        });
    }
}

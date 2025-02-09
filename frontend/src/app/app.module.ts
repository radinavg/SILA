import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { StudioComponent } from './components/studio/studio.component';
import { FormComponent } from './components/form/form.component';
import { HeaderComponent } from './components/header/header.component';
import {StartPageComponent} from "./components/start-page/start-page.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {AdminComponent} from "./components/admin/admin.component";
import {NgbModule} from "@ng-bootstrap/ng-bootstrap";
import { HttpClientModule } from '@angular/common/http';
import {httpInterceptorProviders} from './interceptors';
import { ToastrModule } from 'ngx-toastr';
import {LoginComponent} from "./components/login/login.component";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {RegistrationComponent} from "./components/user/registration/registration.component";
import {ForgotPasswordComponent} from "./components/user/forgot-password/forgot-password.component";
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {CommonModule, NgOptimizedImage} from "@angular/common";
import { StudioProfileInfoComponent } from './components/studio/studio-profile-info/studio-profile-info.component';
import { StudioGalleryComponent } from './components/studio/studio-gallery/studio-gallery.component';
import { StudioActivitiesComponent } from './components/studio/studio-activities/studio-activities.component';
import { StudioFaqComponent } from './components/studio/studio-faq/studio-faq.component';
import {
  DeleteProfileConfirmationComponent
} from "./components/user/delete-profile-confirmation/delete-profile-confirmation.component";
import {StudioInstructorComponent} from "./components/studio/studio-instructor/studio-instructor.component";
import {HomePageComponent} from "./components/home-page/home-page.component";
import {PreferencesComponent} from "./components/user/preferences/preferences.component";
import { StudioMembershipsComponent } from './components/studio/studio-memberships/studio-memberships.component';
import { FriendsPageComponent } from './components/friends-page/friends-page.component';
import { ActivityPreferencesComponent } from "./components/studio/activity-preferences/activity-preferences.component";

@NgModule({
  declarations: [
    AppComponent,
    StudioComponent,
    PreferencesComponent,
    ActivityPreferencesComponent,
    HomePageComponent,
    FormComponent,
    HeaderComponent,
    StartPageComponent,
    AdminComponent,
    LoginComponent,
    RegistrationComponent,
    ForgotPasswordComponent,
    StudioProfileInfoComponent,
    StudioGalleryComponent,
    StudioActivitiesComponent,
    StudioFaqComponent,
    DeleteProfileConfirmationComponent,
    StudioMembershipsComponent,
    FriendsPageComponent,
  ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        ReactiveFormsModule,
        HttpClientModule,
        NgbModule,
        FormsModule,
        ToastrModule.forRoot(),
        BrowserAnimationsModule,
        MatCardModule,
        MatInputModule,
        MatButtonModule,
        FontAwesomeModule,
        NgOptimizedImage,
        StudioInstructorComponent,
        CommonModule
    ],
  providers: [httpInterceptorProviders],
  bootstrap: [AppComponent]
})
export class AppModule { }

import {NgModule} from '@angular/core';
import {mapToCanActivate, RouterModule, Routes} from '@angular/router';
import {StartPageComponent} from "./components/start-page/start-page.component";
import {AnonymousUserGuard} from "./guards/anonymous-user.guard";
import {AdminComponent} from "./components/admin/admin.component";
import {FormComponent} from "./components/form/form.component";
import {AuthGuard} from "./guards/auth.guard";
import {LoginComponent} from "./components/login/login.component";
import {RegistrationComponent} from "./components/user/registration/registration.component";
import {ForgotPasswordComponent} from "./components/user/forgot-password/forgot-password.component";
import {StudioComponent} from "./components/studio/studio.component";
import {UserViewComponent} from "./components/user/user-view/user-view.component";
import {UserChangePasswordComponent} from "./components/user/user-change-password/user-change-password.component";
import {ExploreComponent} from "./components/explore/explore.component";
import {ExploreTypeComponent} from "./components/explore/explore-type/explore-type.component";
import {AdminUsersComponent} from "./components/admin/admin-users/admin-users.component";
import {ActivityComponent} from "./components/activity/activity.component";
import {StudioReviewsComponent} from "./components/studio/studio-reviews/studio-reviews.component";
import {AdminAuthGuard} from "./guards/admin-auth.guard";
import {HomePageComponent} from "./components/home-page/home-page.component";
import {BookingsComponent} from "./components/bookings/bookings.component";
import {PreferencesComponent} from "./components/user/preferences/preferences.component";
import {FriendsPageComponent} from "./components/friends-page/friends-page.component";
import {NotificationPageComponent} from "./components/notification-page/notification-page.component";
import {MyFriendsPageComponent} from "./components/my-friends-page/my-friends-page.component";
import {ActivityPreferencesComponent} from "./components/studio/activity-preferences/activity-preferences.component";

const routes: Routes = [
  {path: '', canActivate: mapToCanActivate([AnonymousUserGuard]), component: StartPageComponent},
  {path: 'home', component: HomePageComponent},
  {path: 'login', canActivate: mapToCanActivate([AnonymousUserGuard]), component: LoginComponent},
  {path: 'preferences', component: PreferencesComponent},
  {path: 'studio-preferences', component: ActivityPreferencesComponent},
  {path: 'form', canActivate: mapToCanActivate([AnonymousUserGuard]), component: FormComponent},
  {path: 'admin/studios', canActivate: mapToCanActivate([AdminAuthGuard]), component: AdminComponent},
  {path: 'admin/users', canActivate: mapToCanActivate([AdminAuthGuard]), component: AdminUsersComponent},
  {path: 'register', canActivate: mapToCanActivate([AnonymousUserGuard]), component: RegistrationComponent},
  {path: 'studio/:id', canActivate: mapToCanActivate([AuthGuard]), component: StudioComponent},
  {path: 'studio/:id/reviews', canActivate: mapToCanActivate([AuthGuard]), component: StudioReviewsComponent},
  {path: 'activity/:id', canActivate: mapToCanActivate([AuthGuard]), component:ActivityComponent},
  {path: 'friends',canActivate:mapToCanActivate([AuthGuard]),component:FriendsPageComponent},

  {
    path: 'user', children: [
      {path: 'profile', canActivate: mapToCanActivate([AuthGuard]), component: UserViewComponent},
      {path: 'notifications', canActivate: mapToCanActivate([AuthGuard]), component: NotificationPageComponent},
      {path: 'my-friends', canActivate: mapToCanActivate([AuthGuard]), component: MyFriendsPageComponent},
      {path: 'password/reset', canActivate: mapToCanActivate([AnonymousUserGuard]), component: ForgotPasswordComponent},
      {path: 'password/update', canActivate: mapToCanActivate([AuthGuard]), component: UserChangePasswordComponent},
    ]
  },
  {path: 'explore', canActivate: mapToCanActivate([AuthGuard]), component: ExploreComponent},
  {path: 'explore/:activity', canActivate: mapToCanActivate([AuthGuard]), component: ExploreTypeComponent },
  {path: 'bookings', canActivate: mapToCanActivate([AuthGuard]), component: BookingsComponent}
];

@NgModule({
  imports: [ RouterModule.forRoot(routes, {
    onSameUrlNavigation: 'reload',
    useHash: true
  }),],
  exports: [RouterModule]
})
export class AppRoutingModule { }

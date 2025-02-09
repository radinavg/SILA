import {Component, OnInit} from '@angular/core';
import {StudioDto} from "../../dtos/studio";
import {StudioService} from "../../services/studio.service";
import {Router} from "@angular/router";
import {AuthService} from "../../services/auth.service";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../services/error-formatter.service";
import {UserInfoDto} from "../../dtos/user";
import {UserService} from "../../services/user.service";
import {Globals} from "../../global/globals";
import {StudioActivity} from "../../dtos/studioActivity";

@Component({
    selector: 'app-home-page',
    templateUrl: './home-page.component.html',
    styleUrl: './home-page.component.scss'
})
export class HomePageComponent implements OnInit {

    likedStudios: StudioDto[];
    user: UserInfoDto;
    defaultImage = 'assets/image-icon-600nw-211642900.webp';
    activityRecommendations: StudioActivity[];
    studioRecommendations: StudioDto[];

    constructor(private studioService: StudioService,
                private router: Router,
                public authService: AuthService,
                private notification: ToastrService,
                private errorFormatter: ErrorFormatterService,
                private userService: UserService,
                public globals: Globals
    ) {
    }

    ngOnInit() {
        this.loadLikedStudios();
        this.userService.getRecommendations().subscribe({
            next: (data) => {
                this.activityRecommendations = data;
                console.log("Activity Recommendations: ", this.activityRecommendations)
            },
            error: (err) =>
                this.notification.error(this.errorFormatter.format(err), 'Could not fetch activity recommendations', {
                    enableHtml: true,
                    timeOut: 10000
                })
        });
        this.userService.getStudioRecommendations().subscribe({
          next: (data) => {
            this.studioRecommendations = data;
            console.log("Studio recommendations: ", this.studioRecommendations)
          },
          error: (err) =>
            this.notification.error(this.errorFormatter.format(err), 'Could not fetch studio recommendations', {
              enableHtml: true,
              timeOut: 10000
            })
        });
        this.userService.getUserInfo(this.authService.getUserEmail()).subscribe((data) => {
                this.user = data;
            },
            (error) => {
                console.error('Error fetching user:', error);
            })
    }

    getImage(activity: StudioActivity): string {
        const img = activity.profileImage;
        if (img && img.path !== '') {
            // Check if the URL starts with assets, then it's an image from our system,
            // this is now important because data generator has web images and previous implementation wouldn't work with our own images
            if (img.path.startsWith('assets/')) {
                return `${this.globals.backendImageUri}${img.path}`;
            }
            return img.path;
        }
        return this.defaultImage;
    }

    loadLikedStudios(): StudioDto[] {
        this.studioService.getFavouriteStudios().subscribe({
                next: (data: StudioDto[]) => {
                    this.likedStudios = data;
                    console.log(this.likedStudios);
                },
                error: (error) => {
                    this.notification.error(this.errorFormatter.format(error), "Error fetching favourite studios");
                }
            }
        )
        return this.likedStudios;
    }

    getProfileImage(studio: StudioDto): string {
        if (studio.profileImage == null) {
            return this.defaultImage;
        }
        const imageUrl = studio.profileImage.path;
        if (imageUrl && imageUrl !== '') {
            // Check if the URL starts with assets, then it's an image from our system,
            // this is now important because data generator has web images and previous implementation wouldn't work with our own images
            if (imageUrl.startsWith('assets/')) {
                return `${this.globals.backendImageUri}${imageUrl}`;
            }
            return imageUrl;
        }
        return this.defaultImage;
    }

}

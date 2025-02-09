import {Component, OnInit, TemplateRef} from '@angular/core';
import {StudioDto} from "../../dtos/studio";
import {ActivatedRoute} from "@angular/router";
import {StudioService} from "../../services/studio.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthService} from "../../services/auth.service";
import {ProfileImageService} from "../../services/reload-services/profile-image.service";
import {StudioActivityListDto} from "../../dtos/studioActivity";
import {StudioActivityService} from "../../services/studio-activity.service";
import {StudioActivityReloadService} from "../../services/reload-services/studio-activity-reload.service";


@Component({
  selector: 'app-studio',
  templateUrl: './studio.component.html',
  styleUrls: ['./studio.component.scss']
})
export class StudioComponent implements OnInit{

  protected studio: StudioDto | null = null;
  protected studioActivities: StudioActivityListDto[] | null = null;

  constructor(private route: ActivatedRoute,
              private studioService: StudioService,
              private modalService: NgbModal,
              public authService: AuthService,
              private profileImageService: ProfileImageService,
              private studioActivityService: StudioActivityService,
              private reloadStudioActivitiesService: StudioActivityReloadService
  ) {
  }

  loadStudio(): void {
    const studioId = Number(this.route.snapshot.paramMap.get('id'));
    if (studioId) {
      this.studioService.getStudioById(studioId).subscribe(
        (studio: StudioDto) => {
          this.studio = {
            ...studio,
            faqs: (studio.faqs || []).map(faq => ({ ...faq, open: false }))
          };
          this.loadActivities();
          console.log(this.studio);
        },
        error => {
          console.error('Error loading studio:', error);
        }
      );
    }
  }

  loadActivities(): void {
    const studioId = Number(this.route.snapshot.paramMap.get('id'));
    this.studioActivityService.getStudioActivities(studioId).subscribe({
      next : activities => {
        this.studioActivities = activities;
      },
      error: err => {
        console.error("Couldn't fetch activities", err);
      }
    })
  }
  ngOnInit(): void {
    this.profileImageService.reload$.subscribe(() => {
      this.loadStudio(); // trigger reload of studio if profile image was updated
    });
    this.reloadStudioActivitiesService.reload$.subscribe(() => {
      this.loadActivities(); // reload studio activities if new activity is added in child component
    })
    this.loadStudio();
  }

  openMembershipModal(membership: any, content: TemplateRef<any>):void{
    this.modalService.open(content);
  }

}

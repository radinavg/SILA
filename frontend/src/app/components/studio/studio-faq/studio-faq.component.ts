import {Component, Input, OnInit} from '@angular/core';
import {faChevronDown, faChevronRight, faPlus} from "@fortawesome/free-solid-svg-icons";
import {Faq} from "../../../dtos/faq";
import {ActivatedRoute} from "@angular/router";
import {StudioService} from "../../../services/studio.service";
import {StudioDto} from "../../../dtos/studio";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {AuthService} from "../../../services/auth.service";

@Component({
  selector: 'app-studio-faq',
  templateUrl: './studio-faq.component.html',
  styleUrls: ['./studio-faq.component.scss']
})
export class StudioFaqComponent implements OnInit {


  @Input() faqs!: Faq[]
  protected isAddFaqVisible: boolean = false;
  isCurrentUserAdminOfThisStudio: boolean = false;
  studio: StudioDto

  protected readonly faPlus = faPlus;
  protected readonly faChevronRight = faChevronRight;
  protected readonly faChevronDown = faChevronDown;


  constructor(private route: ActivatedRoute,
              private studioService: StudioService,
              private notification: ToastrService,
              private errorFormatter: ErrorFormatterService,
              public authService: AuthService
  ) {
  }

  ngOnInit(): void {
    const studioId = Number(this.route.snapshot.paramMap.get('id'));
    if (studioId) {
      this.studioService.getStudioById(studioId).subscribe(
        (studio: StudioDto) => {
          this.faqs = (studio.faqs || []).map(faq => ({ ...faq, open: false }));
          this.studioService.isCurrentUserAdmin(studioId).subscribe(
            isAdmin => {
              this.isCurrentUserAdminOfThisStudio = isAdmin;
            },
            error => {
              console.error('Error checking admin status:', error);
            }
          );
        },
        error => {
          console.error('Error loading studio:', error);
        }
      );
    }
    this.studioService.getStudioById(studioId).subscribe(
      data => {
        this.studio = data;
        console.log(this.studio);
      },
      error => {
        console.error('Error getting studio');
      }
    )
  }

  toggleFaq(selectedFaq: any) {
    this.faqs.forEach(faq => {
      faq.open = (faq === selectedFaq) ? !faq.open : false;
    });
  }

  toggleAddFaq(): void {
    this.isAddFaqVisible = !this.isAddFaqVisible;
  }

  addFaq(question: string, answer: string): void {
    const newFaq: Faq = {question, answer};
    const studioId = Number(this.route.snapshot.paramMap.get('id'));

    this.studioService.addFAQ(studioId, newFaq).subscribe(
      (savedFaq: Faq) => {
        console.log('FAQ added successfully:', savedFaq);
        this.notification.success("FAQ successfully added.");
        this.ngOnInit()
      },
      error => {
        console.error('Error adding FAQ:', error);
        this.notification.error(this.errorFormatter.format(error), "Could not add FAQ", {
            enableHtml: true,
            timeOut: 10000,
          }
        )
      }
    );
  }

}

import {AfterViewInit, ChangeDetectorRef, Component, TemplateRef} from '@angular/core';
import {UserDetailDto, UserSearchDto} from "../../../dtos/user";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {UserService} from "../../../services/user.service";
import {ToastrService} from "ngx-toastr";
import {ErrorFormatterService} from "../../../services/error-formatter.service";
import {NgForOf, NgIf} from "@angular/common";
import {Globals} from "../../../global/globals";
import {MatTableDataSource} from "@angular/material/table";
import {debounceTime, Subject} from "rxjs";
import {MatPaginator} from "@angular/material/paginator";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [
    NgIf,
    NgForOf,
    FormsModule,
    MatPaginator
  ],
  templateUrl: './admin-users.component.html',
  styleUrl: './admin-users.component.scss'
})
export class AdminUsersComponent implements AfterViewInit {

  dataSource = new MatTableDataSource<UserDetailDto>();
  searchParams: UserSearchDto = { pageIndex: 0, pageSize: 10 };
  searchChangedObservable = new Subject<void>();
  total: number = 0;
  paginator: MatPaginator;
  isLocked: String;
  isAdmin: String;

  searchChanged(): void {
    this.searchChangedObservable.next();
  }

  selectedUser: UserDetailDto | null = null;

  constructor(private modalService: NgbModal,
              private userService: UserService,
              private notification: ToastrService,
              private errorFormatter: ErrorFormatterService,
              private globals: Globals,
              private cdr: ChangeDetectorRef,
  ) { }

  getUserProfileImagePath(user: UserDetailDto): string {
    return user.profileImagePath
      ? `${this.globals.backendImageUri}${user.profileImagePath}`
      : 'assets/default-profile.png';
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
    this.searchChangedObservable
      .pipe(debounceTime(300))
      .subscribe({next: () => this.reloadUsers()});
    this.reloadUsers();
  }

  reloadUsers(): void {
    if (this.isLocked === "true") {
      this.searchParams.isLocked = true;
    } else {
      if (this.isLocked === "false") {
        this.searchParams.isLocked = false;
      } else {
        delete this.searchParams.isLocked;
      }
    }

    if (this.isAdmin === "true") {
      this.searchParams.isAdmin = true;
    } else {
      if (this.isAdmin === "false") {
        this.searchParams.isAdmin = false;
      } else {
        delete this.searchParams.isAdmin;
      }
    }
    this.userService.getAllUsers(this.searchParams).subscribe({
      next: res => {
        this.dataSource.data = res.content;
        this.total = res.totalElements;
        this.cdr.detectChanges();
        console.log("Users", this.dataSource.data)

      },
      error: err => {
        console.error("Error loading users:", err);
      }
    });
  }


  openModal(user: UserDetailDto, content: TemplateRef<any>) {
    this.selectedUser = user;
    this.modalService.open(content);
  }

  deleteUser(user: UserDetailDto, modal: any) {
    console.log('Deleted:', user);
    this.userService.deleteUser(user.email).subscribe({
      next: res => {
        console.log('User deleted:', res);
        this.notification.success("User deleted successfully", "Deleted");
        modal.close();
        this.reloadUsers();
      },
      error: err => {
        console.error("Error deleting user:", err);
        this.notification.error(this.errorFormatter.format(err), "Error deleting user", {
          enableHtml: true,
          timeOut: 10000,
        });
      }
    });
  }

  unblockUser(user: UserDetailDto, modal: any): void {
    if (!user) {
      console.error('No user selected for unblocking.');
      return;
    }

    this.userService.unblockUser(user.email).subscribe({
      next: () => {
        this.notification.success(`User ${user.firstName} ${user.lastName} has been unblocked successfully.`);
        modal.close();
        this.reloadUsers();
      },
      error: (err) => {
        console.error('Error unblocking user:', err);
        this.notification.error(this.errorFormatter.format(err), 'Error unblocking user', {
          enableHtml: true,
          timeOut: 10000
        });
      }
    });
  }

  pageChanged(event: any) {
    this.searchParams.pageIndex = event.pageIndex;
    this.searchParams.pageSize = event.pageSize;
    this.reloadUsers();
  }


}

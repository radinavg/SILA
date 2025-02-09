import {Component, OnInit} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {NgForOf, NgIf} from "@angular/common";
import {UserInfoDto, UserSearchDto} from "../../dtos/user";
import {Globals} from "../../global/globals";
import {UserService} from "../../services/user.service";
import {ToastrService} from "ngx-toastr";


@Component({
  selector: 'app-my-friends-page',
  standalone: true,
    imports: [
        FormsModule,
        NgForOf,
        NgIf
    ],
  templateUrl: './my-friends-page.component.html',
  styleUrl: './my-friends-page.component.scss'
})
export class MyFriendsPageComponent implements OnInit{
  users: UserInfoDto[] = [];
  isLoading: boolean;
  searchQuery: UserSearchDto = {
    firstName: '',
    lastName: '',
    email: '',
    pageIndex: 0,
    pageSize: 10
  };


  constructor(protected globals: Globals,
              private userService: UserService,
              private toastrService: ToastrService) {

  }

  ngOnInit(): void {
    this.searchUsers();
  }

  searchUsers() {
    this.userService.getFriendsForUser(this.searchQuery).subscribe({
      next: (data) => {
        // Assuming there's a property `users` in your component to hold the search results
        this.users = data;

        console.log('Search results:', this.users); // Log for debugging
      },
      error: (err) => {
        // Handle the error, e.g., display a message or log it
        console.error('Error fetching users:', err);
      },
      complete: () => {
        console.log('Search completed'); // Optional, if you want to do something after completion
      },
    });
  }

  deleteFriend(user: UserInfoDto) {
    this.userService.deleteFriend(user.email).subscribe({
      next: () => {
        this.toastrService.success("Friend removed successfully")
        this.searchUsers()
      },
      error: err => {
        this.toastrService.error("Error while removing friend")
        console.log(err);
      }
    });
  }
}

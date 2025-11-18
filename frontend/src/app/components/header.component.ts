import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <header class="bg-white shadow-md sticky top-0 z-50">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          <!-- Logo and Location -->
          <div class="flex items-center space-x-6">
            <div class="flex items-center">
              <h1 class="text-2xl font-bold text-red-600 cursor-pointer">CineBook Pro</h1>
            </div>
            <div class="hidden md:flex items-center space-x-2 border-l border-gray-300 pl-6">
              <svg class="w-5 h-5 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path>
              </svg>
              <select 
                [(ngModel)]="selectedLocation" 
                (change)="onLocationChange()"
                class="border-none outline-none text-gray-700 font-medium cursor-pointer bg-transparent">
                <option value="mumbai">Mumbai</option>
                <option value="delhi">Delhi</option>
                <option value="bangalore">Bangalore</option>
                <option value="hyderabad">Hyderabad</option>
                <option value="chennai">Chennai</option>
              </select>
            </div>
          </div>

          <!-- Navigation Links -->
          <nav class="hidden lg:flex items-center space-x-8">
            <a href="#" class="text-gray-700 hover:text-red-600 font-medium transition">Movies</a>
            <a href="#" class="text-gray-700 hover:text-red-600 font-medium transition">Events</a>
            <a href="#" class="text-gray-700 hover:text-red-600 font-medium transition">Sports</a>
            <a href="#" class="text-gray-700 hover:text-red-600 font-medium transition">Activities</a>
          </nav>

          <!-- Sign In / User Menu -->
          <div class="flex items-center space-x-4">
            <button 
              *ngIf="!isLoggedIn"
              (click)="openSignInModal()"
              class="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 font-medium transition shadow-sm">
              Sign In
            </button>
            <div *ngIf="isLoggedIn" class="relative">
              <button 
                (click)="toggleUserMenu()"
                class="flex items-center space-x-2 px-4 py-2 rounded-md hover:bg-gray-100 transition">
                <div class="w-8 h-8 bg-red-600 rounded-full flex items-center justify-center text-white font-semibold">
                  {{ userInitials }}
                </div>
                <span class="text-gray-700 font-medium">{{ userName }}</span>
                <svg class="w-4 h-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                </svg>
              </button>
              <div 
                *ngIf="showUserMenu"
                class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-1 z-50 border border-gray-200">
                <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">My Bookings</a>
                <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Profile</a>
                <a href="#" class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">Settings</a>
                <hr class="my-1">
                <a href="#" (click)="signOut()" class="block px-4 py-2 text-sm text-red-600 hover:bg-gray-100">Sign Out</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
  `,
  styles: [`
    select {
      appearance: none;
      background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%23666'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'/%3E%3C/svg%3E");
      background-repeat: no-repeat;
      background-position: right 0 top 50%;
      padding-right: 1.5rem;
    }
  `]
})
export class HeaderComponent implements OnInit {
  selectedLocation = 'mumbai';
  isLoggedIn = false;
  userName = '';
  userInitials = '';
  showUserMenu = false;

  ngOnInit() {
    // Check if user is logged in
    const token = localStorage.getItem('authToken');
    const user = localStorage.getItem('user');
    if (token && user) {
      this.isLoggedIn = true;
      const userData = JSON.parse(user);
      this.userName = userData.name || userData.email || 'User';
      this.userInitials = this.userName.charAt(0).toUpperCase();
    }
  }

  onLocationChange() {
    // Emit location change event or update service
    console.log('Location changed to:', this.selectedLocation);
  }

  openSignInModal() {
    // Emit event to parent component to open sign-in modal
    const event = new CustomEvent('openSignIn');
    window.dispatchEvent(event);
  }

  toggleUserMenu() {
    this.showUserMenu = !this.showUserMenu;
  }

  signOut() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    this.isLoggedIn = false;
    this.showUserMenu = false;
    window.location.reload();
  }
}


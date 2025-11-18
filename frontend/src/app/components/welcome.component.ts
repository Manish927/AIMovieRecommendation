import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { MovieListComponent } from './movie-list.component';

interface Movie {
  movieId: number;
  title: string;
  description: string;
  genre: string;
  director: string;
  cast: string;
  releaseDate: string;
  duration: number;
  posterUrl: string;
  rating: number;
  totalRatings: number;
  language: string;
  certification: string;
}

interface Recommendation {
  movieId: number;
  title: string;
  score: number;
  reason: string;
  algorithm: string;
}

@Component({
  selector: 'app-welcome',
  standalone: true,
  imports: [CommonModule, FormsModule, MovieListComponent],
  template: `
    <div class="min-h-screen bg-gray-50">



      <!-- Quick Filters -->
      <section class="bg-white border-b border-gray-200 sticky top-16 z-40">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div class="flex items-center space-x-4 overflow-x-auto">
            <button 
              (click)="filterByType('all')"
              [class.bg-red-600]="activeFilter === 'all'"
              [class.text-white]="activeFilter === 'all'"
              [class.text-gray-700]="activeFilter !== 'all'"
              class="px-6 py-2 rounded-full font-medium whitespace-nowrap transition">
              All
            </button>
            <button 
              (click)="filterByType('now-showing')"
              [class.bg-red-600]="activeFilter === 'now-showing'"
              [class.text-white]="activeFilter === 'now-showing'"
              [class.text-gray-700]="activeFilter !== 'now-showing'"
              class="px-6 py-2 rounded-full font-medium whitespace-nowrap transition">
              Now Showing
            </button>
            <button 
              (click)="filterByType('coming-soon')"
              [class.bg-red-600]="activeFilter === 'coming-soon'"
              [class.text-white]="activeFilter === 'coming-soon'"
              [class.text-gray-700]="activeFilter !== 'coming-soon'"
              class="px-6 py-2 rounded-full font-medium whitespace-nowrap transition">
              Coming Soon
            </button>
            <button 
              *ngFor="let genre of genres"
              (click)="filterByGenre(genre)"
              [class.bg-red-600]="selectedGenre === genre"
              [class.text-white]="selectedGenre === genre"
              [class.text-gray-700]="selectedGenre !== genre"
              class="px-6 py-2 rounded-full font-medium whitespace-nowrap transition">
              {{ genre }}
            </button>
          </div>
        </div>
      </section>

      <!-- Personalized Recommendations Section (for logged-in users) -->
      <section *ngIf="isLoggedIn" class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 bg-gradient-to-b from-gray-50 to-white">
        <div class="mb-6">
          <div class="flex items-center justify-between mb-4">
            <div>
              <h2 class="text-3xl font-bold text-gray-900 mb-2 flex items-center">
                <svg class="w-8 h-8 text-red-600 mr-2" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path>
                </svg>
                Recommended For You
              </h2>
              <p class="text-gray-600">Movies tailored to your preferences using AI-powered recommendations</p>
            </div>
            <span class="bg-red-100 text-red-800 text-xs font-semibold px-3 py-1 rounded-full">
              Powered by MovieLens
            </span>
          </div>
        </div>

        <!-- Loading State -->
        <div *ngIf="loadingRecommendations" class="text-center py-12">
          <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-red-600"></div>
          <p class="mt-4 text-gray-600">Loading personalized recommendations...</p>
        </div>

        <!-- Recommendations Grid -->
        <div *ngIf="!loadingRecommendations && recommendations.length > 0" class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4 mb-8">
          <div 
            *ngFor="let rec of recommendations; let i = index" 
            class="group cursor-pointer transform transition-all duration-300 hover:scale-105">
            <div class="bg-white rounded-lg overflow-hidden shadow-md hover:shadow-2xl transition-shadow relative">
              <!-- Recommendation Badge (Rank Order) -->
              <div class="absolute top-2 left-2 z-10 bg-gradient-to-r from-red-600 to-pink-600 text-white px-2 py-1 rounded-full text-xs font-bold shadow-lg">
                #{{ i + 1 }}
              </div>
              
              <!-- Match Score Badge -->
              <div class="absolute top-2 right-2 z-10 bg-black bg-opacity-75 text-white px-2 py-1 rounded text-xs font-semibold">
                {{ (rec.score * 100).toFixed(0) }}% match
              </div>

              <!-- Movie Poster Placeholder -->
              <div class="relative overflow-hidden bg-gradient-to-br from-gray-200 to-gray-300" style="padding-top: 145%;">
                <div class="absolute inset-0 flex items-center justify-center">
                  <svg class="w-16 h-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
                  </svg>
                </div>
                
                <!-- Overlay on Hover -->
                <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-40 transition-all duration-300 flex items-center justify-center">
                  <button 
                    (click)="bookRecommendedMovie(rec.movieId); $event.stopPropagation()"
                    class="opacity-0 group-hover:opacity-100 px-6 py-3 bg-red-600 text-white rounded-lg font-medium transform translate-y-4 group-hover:translate-y-0 transition-all duration-300">
                    Book Now
                  </button>
                </div>
              </div>

              <!-- Movie Info -->
              <div class="p-3">
                <h3 class="text-sm font-bold text-gray-900 mb-1 line-clamp-1">{{ rec.title }}</h3>
                <p class="text-xs text-gray-500 mb-2 line-clamp-2">{{ rec.reason }}</p>
                <div class="flex items-center justify-between">
                  <span class="text-xs bg-indigo-100 text-indigo-800 px-2 py-1 rounded">{{ rec.algorithm }}</span>
                  <button 
                    (click)="viewRecommendedMovie(rec.movieId); $event.stopPropagation()"
                    class="text-xs text-red-600 hover:text-red-700 font-medium">
                    Details →
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- No Recommendations Message -->
        <div *ngIf="!loadingRecommendations && recommendations.length === 0" class="text-center py-12 bg-white rounded-lg shadow-md mb-6">
          <svg class="mx-auto h-16 w-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
          </svg>
          <p class="mt-4 text-lg text-gray-600">No personalized recommendations available yet.</p>
          <p class="mt-2 text-sm text-gray-500">Rate 3-5 movies to get started!</p>
        </div>

        <!-- Algorithm Info -->
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
          <div class="flex items-start">
            <svg class="w-5 h-5 text-blue-600 mr-2 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path>
            </svg>
            <div>
              <p class="text-sm text-blue-800 font-semibold mb-1">How these recommendations work:</p>
              <p class="text-xs text-blue-700">
                Our AI analyzes your movie ratings and viewing history using collaborative filtering and content-based algorithms 
                trained on the MovieLens dataset (27M+ ratings). Recommendations are personalized based on similar users' preferences 
                and movies with similar genres, directors, and cast.
              </p>
            </div>
          </div>
        </div>
      </section>

      <!-- Movies Section -->
      <section class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="mb-6">
          <h2 class="text-3xl font-bold text-gray-900 mb-2">
            {{ activeFilter === 'now-showing' ? 'Now Showing' : activeFilter === 'coming-soon' ? 'Coming Soon' : 'All Movies' }}
          </h2>
          <p class="text-gray-600">Book tickets for your favorite movies</p>
        </div>

        <!-- Movie Grid -->
        <app-movie-list 
          [externalSearchQuery]="searchQuery"
          [externalSelectedGenre]="selectedGenre"
          [externalActiveFilter]="activeFilter">
        </app-movie-list>
      </section>
    </div>
  `
})
export class WelcomeComponent implements OnInit {
  searchQuery = '';
  activeFilter = 'all';
  selectedGenre = '';
  featuredMovie: Movie | null = null;
  genres = ['Action', 'Comedy', 'Drama', 'Sci-Fi', 'Thriller', 'Horror'];
  recommendations: Recommendation[] = [];
  isLoggedIn = false;
  currentUserId: number | null = null;
  loadingRecommendations = false;
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadFeaturedMovie();
    this.checkLoginStatus();
    if (this.isLoggedIn && this.currentUserId) {
      this.loadRecommendations();
    }
    
    // Listen for rating submissions to reload recommendations
    window.addEventListener('ratingSubmitted', () => {
      console.log('Rating submitted, reloading recommendations...');
      if (this.isLoggedIn && this.currentUserId) {
        setTimeout(() => {
          this.loadRecommendations();
        }, 500);
      }
    });
  }

  loadFeaturedMovie() {
    this.http.get<Movie[]>(`${this.apiUrl}/movies`).subscribe({
      next: (movies) => {
        if (movies && movies.length > 0) {
          // Get the highest rated movie as featured
          this.featuredMovie = movies.sort((a, b) => (b.rating || 0) - (a.rating || 0))[0];
        }
      },
      error: (err) => {
        console.error('Failed to load featured movie:', err);
      }
    });
  }



  onSearch() {
    // Search functionality is handled by movie-list component via @Input binding
    // The search query is automatically passed to movie-list component
    // which triggers ngOnChanges and calls searchMovies()
  }

  filterByType(type: string) {
    // If clicking the same filter, toggle it off (set to 'all')
    if (this.activeFilter === type) {
      this.activeFilter = 'all';
      // Also clear genre filter when toggling off
      this.selectedGenre = '';
    } else {
      this.activeFilter = type;
      // Clear genre filter when selecting a type filter
      this.selectedGenre = '';
    }
  }

  filterByGenre(genre: string) {
    // Toggle genre filter
    if (this.selectedGenre === genre) {
      // If clicking the same genre, deselect it
      this.selectedGenre = '';
    } else {
      // Select the new genre and clear type filter
      this.selectedGenre = genre;
      this.activeFilter = 'all';
    }
  }

  bookMovie(movie: Movie) {
    // Navigate to booking page
    this.router.navigate(['/book'], { queryParams: { movieId: movie.movieId } });
  }

  checkLoginStatus() {
    const token = localStorage.getItem('authToken');
    const user = localStorage.getItem('user');
    console.log('Checking login status - token:', !!token, 'user:', user);
    if (token && user) {
      this.isLoggedIn = true;
      try {
        const userData = JSON.parse(user);
        console.log('Parsed user data:', userData);
        this.currentUserId = userData.userID || userData.userId || null;
        console.log('Extracted user ID:', this.currentUserId);
      } catch (e) {
        console.error('Error parsing user data:', e);
      }
    } else {
      console.log('User not logged in or user data missing');
    }
  }

  loadRecommendations() {
    if (!this.currentUserId) {
      console.log('Cannot load recommendations: user ID is null');
      return;
    }

    console.log('Loading recommendations for user ID:', this.currentUserId);
    this.loadingRecommendations = true;
    // Route through Movie Service Gateway
    const url = `${this.apiUrl}/api/recommendations/user/${this.currentUserId}/hybrid?limit=10`;
    console.log('Recommendation API URL:', url);
    
    // Use hybrid recommendations (combines collaborative and content-based)
    this.http.get<Recommendation[]>(url).subscribe({
      next: (data) => {
        console.log('Recommendations received:', data);
        console.log('Number of recommendations:', data?.length || 0);
        
        // Sort recommendations by score (descending) to ensure rank order
        if (data && data.length > 0) {
          this.recommendations = data
            .sort((a, b) => (b.score || 0) - (a.score || 0))
            .slice(0, 10); // Ensure we only show top 10
          console.log('Sorted recommendations:', this.recommendations);
        } else {
          this.recommendations = [];
        }
        
        this.loadingRecommendations = false;
        
        if (this.recommendations.length === 0) {
          console.warn('No recommendations returned. User may need to rate more movies.');
        } else {
          console.log(`Successfully loaded ${this.recommendations.length} recommendations`);
        }
      },
      error: (err) => {
        console.error('Failed to load recommendations:', err);
        console.error('Error details:', err.error || err.message);
        console.error('Full error:', err);
        this.recommendations = [];
        this.loadingRecommendations = false;
        // Don't show error to user, just silently fail
      }
    });
  }

  bookRecommendedMovie(movieId: number) {
    this.router.navigate(['/book'], { queryParams: { movieId: movieId } });
  }

  viewRecommendedMovie(movieId: number) {
    // Navigate to movie details or open in new tab
    window.open(`${this.apiUrl}/movies/${movieId}`, '_blank');
  }
}



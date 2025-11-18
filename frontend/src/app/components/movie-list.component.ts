import { Component, OnInit, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

interface Movie {
  movieId: number;
  title: string;
  description: string;
  genre: string;
  director: string;
  cast: string; // JSON string
  releaseDate: string;
  duration: number;
  posterUrl: string;
  rating: number;
  totalRatings: number;
  language: string;
  certification: string;
}

interface UserRating {
  movieId: number;
  rating: number;
}

@Component({
  selector: 'app-movie-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div>
      <div *ngIf="loading" class="text-center py-12">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-red-600"></div>
        <p class="mt-4 text-gray-600">Loading movies...</p>
      </div>

      <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ error }}
      </div>

      <!-- Movies Info -->
      <div *ngIf="movies.length > 0" class="mb-4 flex items-center justify-between">
        <p class="text-sm text-gray-600">
          Showing top {{ totalMovies }} newest movies
        </p>
      </div>

      <!-- Movie Grid - CineBook Pro Style -->
      <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
        <div 
          *ngFor="let movie of paginatedMovies" 
          class="group cursor-pointer transform transition-all duration-300 hover:scale-105">
          <!-- Movie Poster Card -->
          <div class="bg-white rounded-lg overflow-hidden shadow-md hover:shadow-2xl transition-shadow">
            <!-- Poster Image -->
            <div class="relative overflow-hidden bg-gray-200" style="padding-top: 145%;">
              <img
                [src]="movie.posterUrl || 'assets/placeholder-300x450.svg'"
                (error)="onImgError($event)"
                [alt]="movie.title"
                class="absolute inset-0 w-full h-full object-cover group-hover:scale-110 transition-transform duration-300">
              
              <!-- Overlay on Hover -->
              <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-40 transition-all duration-300 flex flex-col items-center justify-center gap-2">
                <button 
                  (click)="bookMovie(movie); $event.stopPropagation()"
                  class="opacity-0 group-hover:opacity-100 px-6 py-3 bg-red-600 text-white rounded-lg font-medium transform translate-y-4 group-hover:translate-y-0 transition-all duration-300">
                  Book Now
                </button>
                
                <!-- Rate Movie Button (only if logged in) -->
                <button 
                  *ngIf="isLoggedIn"
                  (click)="openRatingModal(movie); $event.stopPropagation()"
                  class="opacity-0 group-hover:opacity-100 px-4 py-2 bg-yellow-500 text-white rounded-lg text-sm font-medium transform translate-y-4 group-hover:translate-y-0 transition-all duration-300 flex items-center space-x-1">
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path>
                  </svg>
                  <span>Rate</span>
                </button>
              </div>
              
              <!-- User's Rating Badge (if rated) -->
              <div *ngIf="isLoggedIn && getUserRating(movie.movieId)" class="absolute top-2 left-2 bg-green-600 text-white px-2 py-1 rounded text-xs font-semibold flex items-center space-x-1">
                <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path>
                </svg>
                <span>You: {{ getUserRating(movie.movieId) }}/5</span>
              </div>

              <!-- Rating Badge -->
              <div class="absolute top-2 right-2 bg-black bg-opacity-75 text-white px-2 py-1 rounded flex items-center space-x-1">
                <svg class="w-4 h-4 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path>
                </svg>
                <span class="text-sm font-semibold">{{ movie.rating?.toFixed(1) || 'N/A' }}</span>
              </div>
            </div>

            <!-- Movie Info -->
            <div class="p-3">
              <h3 class="text-sm font-bold text-gray-900 mb-1 line-clamp-1">{{ movie.title }}</h3>
              <p class="text-xs text-gray-600 mb-2">{{ movie.genre }}</p>
              <div class="flex items-center justify-between text-xs text-gray-500 mb-2">
                <span>{{ movie.language }}</span>
                <span>{{ movie.duration }} min</span>
              </div>
              
              <!-- Rate Button (Always Visible for Logged-In Users) -->
              <button 
                *ngIf="isLoggedIn"
                (click)="openRatingModal(movie); $event.stopPropagation()"
                class="w-full mt-2 px-3 py-1.5 bg-yellow-500 hover:bg-yellow-600 text-white rounded text-xs font-medium transition-colors flex items-center justify-center space-x-1">
                <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path>
                </svg>
                <span>{{ getUserRating(movie.movieId) ? 'Update Rating' : 'Rate Movie' }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Rating Modal -->
      <div *ngIf="showRatingModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50" (click)="closeRatingModal()">
        <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4" (click)="$event.stopPropagation()">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-xl font-bold text-gray-900">Rate Movie</h3>
            <button (click)="closeRatingModal()" class="text-gray-400 hover:text-gray-600">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </button>
          </div>
          
          <div class="mb-4">
            <h4 class="text-lg font-semibold text-gray-800 mb-2">{{ selectedMovie?.title }}</h4>
            <p class="text-sm text-gray-600">{{ selectedMovie?.genre }}</p>
          </div>

          <!-- Star Rating -->
          <div class="mb-6">
            <p class="text-sm font-medium text-gray-700 mb-3">Your Rating:</p>
            <div class="flex items-center space-x-2">
              <div class="flex space-x-1">
                <button 
                  *ngFor="let star of [1,2,3,4,5]; let i = index"
                  (click)="setRating(i + 1)"
                  class="focus:outline-none transition-transform hover:scale-110">
                  <svg 
                    [class.text-yellow-400]="currentRating >= i + 1"
                    [class.text-gray-300]="currentRating < i + 1"
                    class="w-8 h-8" 
                    fill="currentColor" 
                    viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"></path>
                  </svg>
                </button>
              </div>
              <span class="text-sm font-semibold text-gray-700 ml-2">
                {{ currentRating > 0 ? currentRating + '/5' : 'Select rating' }}
              </span>
            </div>
          </div>

          <!-- Submit Button -->
          <div class="flex space-x-3">
            <button 
              (click)="submitRating()"
              [disabled]="currentRating === 0 || submittingRating"
              class="flex-1 bg-red-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-red-700 transition disabled:opacity-50 disabled:cursor-not-allowed">
              {{ submittingRating ? 'Submitting...' : 'Submit Rating' }}
            </button>
            <button 
              (click)="closeRatingModal()"
              class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50 transition">
              Cancel
            </button>
          </div>

          <!-- Success/Error Messages -->
          <div *ngIf="ratingMessage" class="mt-4 p-3 rounded" 
               [class.bg-green-100]="ratingSuccess"
               [class.text-green-800]="ratingSuccess"
               [class.bg-red-100]="!ratingSuccess"
               [class.text-red-800]="!ratingSuccess">
            {{ ratingMessage }}
          </div>
        </div>
      </div>

      <div *ngIf="paginatedMovies.length === 0 && !loading" class="text-center py-12">
        <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
        </svg>
        <p class="mt-4 text-gray-600">No movies found.</p>
      </div>
    </div>
  `
})
export class MovieListComponent implements OnInit, OnChanges {
  @Input() externalSearchQuery: string = '';
  @Input() externalSelectedGenre: string = '';
  @Input() externalActiveFilter: string = 'all';
  movies: Movie[] = [];
  paginatedMovies: Movie[] = [];
  loading = false;
  error: string | null = null;
  searchQuery = '';
  selectedGenre = '';
  activeFilter = 'all';
  isLoggedIn = false;
  currentUserId: number | null = null;
  userRatings: Map<number, number> = new Map(); // movieId -> rating
  showRatingModal = false;
  selectedMovie: Movie | null = null;
  currentRating = 0;
  submittingRating = false;
  ratingMessage = '';
  ratingSuccess = false;
  
  // Pagination properties (simplified for top 30 movies)
  currentPage = 1;
  pageSize = 50; // Keep for compatibility, but we'll show all 30
  totalMovies = 0;
  totalPages = 1;
  Math = Math; // Expose Math to template
  
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadMovies();
    this.checkLoginStatus();
    if (this.isLoggedIn && this.currentUserId) {
      this.loadUserRatings();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    let shouldReload = false;

    if (changes['externalSearchQuery']) {
      this.searchQuery = this.externalSearchQuery || '';
      // Only trigger search if not the first change (to avoid searching on initial load)
      if (!changes['externalSearchQuery'].firstChange) {
        this.searchMovies();
        return; // Search takes precedence
      }
    }

    if (changes['externalSelectedGenre']) {
      this.selectedGenre = this.externalSelectedGenre || '';
      if (!changes['externalSelectedGenre'].firstChange) {
        shouldReload = true;
      }
    }

    if (changes['externalActiveFilter']) {
      this.activeFilter = this.externalActiveFilter || 'all';
      if (!changes['externalActiveFilter'].firstChange) {
        shouldReload = true;
      }
    }

    // Apply filters if genre or type filter changed
    if (shouldReload) {
      this.applyFilters();
    }
  }

  applyFilters() {
    // If there's a search query, search takes precedence
    if (this.searchQuery.trim()) {
      this.searchMovies();
      return;
    }

    // Apply genre filter if selected
    if (this.selectedGenre) {
      this.filterByGenre();
      return;
    }

    // Apply type filter
    if (this.activeFilter === 'now-showing' || this.activeFilter === 'coming-soon') {
      // For now-showing and coming-soon, we can filter by release date
      // For simplicity, just load all movies for now
      // TODO: Implement date-based filtering if needed
      this.loadMovies();
    } else {
      // 'all' filter - load all movies
      this.loadMovies();
    }
  }

  loadMovies() {
    this.loading = true;
    this.error = null;
    this.http.get<Movie[]>(`${this.apiUrl}/movies`).subscribe({
      next: (data) => {
        // Sort by movieId descending (newest first) and limit to top 30
        const sortedMovies = data
          .sort((a, b) => (b.movieId || 0) - (a.movieId || 0))
          .slice(0, 30); // Top 30 newest movies
        
        this.movies = sortedMovies;
        this.totalMovies = sortedMovies.length;
        this.totalPages = Math.ceil(this.totalMovies / this.pageSize);
        this.updatePaginatedMovies();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load movies. Make sure the backend is running.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  updatePaginatedMovies() {
    // Since we're only showing top 30 movies, display all of them
    // No pagination needed for 30 movies
    this.paginatedMovies = this.movies;
  }

  goToPage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePaginatedMovies();
      // Scroll to top of movie grid
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }

  searchMovies() {
    if (this.searchQuery.trim()) {
      this.loading = true;
      this.currentPage = 1; // Reset to first page on search
      this.http.get<Movie[]>(`${this.apiUrl}/movies/search?query=${encodeURIComponent(this.searchQuery)}`).subscribe({
        next: (data) => {
          this.movies = data;
          this.totalMovies = data.length;
          this.totalPages = Math.ceil(this.totalMovies / this.pageSize);
          this.updatePaginatedMovies();
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to search movies.';
          this.loading = false;
        }
      });
    } else {
      this.currentPage = 1;
      this.loadMovies();
    }
  }

  filterByGenre() {
    if (this.selectedGenre) {
      this.loading = true;
      this.currentPage = 1; // Reset to first page on filter
      this.http.get<Movie[]>(`${this.apiUrl}/movies/genre/${encodeURIComponent(this.selectedGenre)}`).subscribe({
        next: (data) => {
          this.movies = data;
          this.totalMovies = data.length;
          this.totalPages = Math.ceil(this.totalMovies / this.pageSize);
          this.updatePaginatedMovies();
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to filter movies.';
          this.loading = false;
        }
      });
    } else {
      this.currentPage = 1;
      this.loadMovies();
    }
  }

  loadTopRated() {
    this.loading = true;
    this.currentPage = 1; // Reset to first page
    this.http.get<Movie[]>(`${this.apiUrl}/movies/top-rated`).subscribe({
      next: (data) => {
        this.movies = data;
        this.totalMovies = data.length;
        this.totalPages = Math.ceil(this.totalMovies / this.pageSize);
        this.updatePaginatedMovies();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load top rated movies.';
        this.loading = false;
      }
    });
  }

  checkLoginStatus() {
    const token = localStorage.getItem('authToken');
    const user = localStorage.getItem('user');
    if (token && user) {
      this.isLoggedIn = true;
      try {
        const userData = JSON.parse(user);
        this.currentUserId = userData.userID || userData.userId || null;
      } catch (e) {
        console.error('Error parsing user data:', e);
      }
    }
  }

  loadUserRatings() {
    if (!this.currentUserId) return;
    
    this.http.get<any[]>(`${this.apiUrl}/ratings/user/${this.currentUserId}`).subscribe({
      next: (ratings) => {
        ratings.forEach(rating => {
          this.userRatings.set(rating.movieId, rating.rating);
        });
      },
      error: (err) => {
        console.error('Failed to load user ratings:', err);
      }
    });
  }

  getUserRating(movieId: number): number | null {
    return this.userRatings.get(movieId) || null;
  }

  openRatingModal(movie: Movie) {
    if (!this.isLoggedIn) {
      alert('Please sign in to rate movies');
      return;
    }
    this.selectedMovie = movie;
    this.currentRating = this.getUserRating(movie.movieId) || 0;
    this.showRatingModal = true;
    this.ratingMessage = '';
  }

  closeRatingModal() {
    this.showRatingModal = false;
    this.selectedMovie = null;
    this.currentRating = 0;
    this.ratingMessage = '';
  }

  setRating(rating: number) {
    this.currentRating = rating;
  }

  submitRating() {
    if (!this.selectedMovie || !this.currentUserId || this.currentRating === 0) {
      return;
    }

    this.submittingRating = true;
    this.ratingMessage = '';

    const ratingData = {
      userId: this.currentUserId,
      movieId: this.selectedMovie.movieId,
      rating: this.currentRating
    };

    this.http.post(`${this.apiUrl}/ratings`, ratingData).subscribe({
      next: (response) => {
        this.ratingSuccess = true;
        this.ratingMessage = 'Rating submitted successfully!';
        this.userRatings.set(this.selectedMovie!.movieId, this.currentRating);
        
        // Reload movies to update average ratings
        setTimeout(() => {
          this.loadMovies();
          this.closeRatingModal();
          
          // Trigger recommendation reload if parent component has that method
          // This will be handled by the welcome component watching for changes
          window.dispatchEvent(new CustomEvent('ratingSubmitted'));
        }, 1500);
      },
      error: (err) => {
        this.ratingSuccess = false;
        this.ratingMessage = err.error?.message || 'Failed to submit rating. Please try again.';
        console.error('Rating submission error:', err);
      },
      complete: () => {
        this.submittingRating = false;
      }
    });
  }

  viewDetails(movie: Movie) {
    alert(`Title: ${movie.title}\nDirector: ${movie.director}\nDuration: ${movie.duration} min\nRelease: ${movie.releaseDate}`);
  }

  bookMovie(movie: Movie) {
    // Navigate to booking page
    this.router.navigate(['/book'], { queryParams: { movieId: movie.movieId } });
  }

  onImgError(event: Event) {
    const img = event.target as HTMLImageElement;
    img.src = 'assets/placeholder-300x450.svg';
  }
}


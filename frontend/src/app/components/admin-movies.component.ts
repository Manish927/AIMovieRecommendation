import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';

interface Movie {
  movieId?: number;
  title: string;
  description?: string;
  genre?: string;
  director?: string;
  cast?: string;
  releaseDate?: string;
  duration?: number;
  posterUrl?: string;
  rating?: number;
  totalRatings?: number;
  language?: string;
  certification?: string;
}

@Component({
  selector: 'app-admin-movies',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="px-4 py-6">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-2xl font-bold text-gray-900">Movie Management</h2>
        <button
          (click)="showAddForm = !showAddForm"
          class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">
          {{ showAddForm ? 'Cancel' : 'Add New Movie' }}
        </button>
      </div>

      <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ error }}
      </div>

      <!-- Add/Edit Form -->
      <div *ngIf="showAddForm || editingMovie" class="bg-white rounded-lg shadow p-6 mb-6">
        <h3 class="text-lg font-semibold mb-4">{{ editingMovie ? 'Edit Movie' : 'Add New Movie' }}</h3>
        <form (ngSubmit)="saveMovie()">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700">Title *</label>
              <input type="text" [(ngModel)]="currentMovie.title" name="title" required
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Genre</label>
              <input type="text" [(ngModel)]="currentMovie.genre" name="genre"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Director</label>
              <input type="text" [(ngModel)]="currentMovie.director" name="director"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Duration (minutes)</label>
              <input type="number" [(ngModel)]="currentMovie.duration" name="duration"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Release Date</label>
              <input type="date" [(ngModel)]="currentMovie.releaseDate" name="releaseDate"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Language</label>
              <input type="text" [(ngModel)]="currentMovie.language" name="language"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2">
            </div>
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-gray-700">Description</label>
              <textarea [(ngModel)]="currentMovie.description" name="description" rows="3"
                class="mt-1 block w-full border border-gray-300 rounded-md px-3 py-2"></textarea>
            </div>
          </div>
          <div class="mt-4 flex justify-end space-x-2">
            <button type="button" (click)="cancelEdit()" class="px-4 py-2 border border-gray-300 rounded hover:bg-gray-50">
              Cancel
            </button>
            <button type="submit" class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">
              {{ editingMovie ? 'Update' : 'Add' }} Movie
            </button>
          </div>
        </form>
      </div>

      <!-- Movies List -->
      <div class="bg-white rounded-lg shadow overflow-hidden">
        <table class="min-w-full divide-y divide-gray-200">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Title</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Genre</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Rating</th>
              <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
            </tr>
          </thead>
          <tbody class="bg-white divide-y divide-gray-200">
            <tr *ngFor="let movie of movies">
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{{ movie.movieId }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{{ movie.title }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ movie.genre }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{{ movie.rating || 'N/A' }}</td>
              <td class="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                <button (click)="editMovie(movie)" class="text-indigo-600 hover:text-indigo-900">Edit</button>
                <button (click)="deleteMovie(movie.movieId!)" class="text-red-600 hover:text-red-900">Delete</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class AdminMoviesComponent implements OnInit {
  movies: Movie[] = [];
  currentMovie: Movie = { title: '' };
  showAddForm = false;
  editingMovie: Movie | null = null;
  loading = false;
  error: string | null = null;
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadMovies();
  }

  getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('adminToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  }

  loadMovies() {
    this.loading = true;
    this.http.get<Movie[]>(`${this.apiUrl}/movies`).subscribe({
      next: (data) => {
        this.movies = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load movies';
        this.loading = false;
      }
    });
  }

  saveMovie() {
    const headers = this.getAuthHeaders();
    const movieData = { ...this.currentMovie };

    if (this.editingMovie) {
      this.http.put<Movie>(`${this.apiUrl}/admin/movies/${this.editingMovie.movieId}`, movieData, { headers }).subscribe({
        next: () => {
          this.loadMovies();
          this.cancelEdit();
        },
        error: (err) => {
          this.error = 'Failed to update movie';
        }
      });
    } else {
      this.http.post<Movie>(`${this.apiUrl}/admin/movies`, movieData, { headers }).subscribe({
        next: () => {
          this.loadMovies();
          this.cancelEdit();
        },
        error: (err) => {
          this.error = 'Failed to create movie';
        }
      });
    }
  }

  editMovie(movie: Movie) {
    this.editingMovie = movie;
    this.currentMovie = { ...movie };
    this.showAddForm = true;
  }

  deleteMovie(movieId: number) {
    if (!confirm('Are you sure you want to delete this movie?')) return;

    const headers = this.getAuthHeaders();
    this.http.delete(`${this.apiUrl}/admin/movies/${movieId}`, { headers }).subscribe({
      next: () => {
        this.loadMovies();
      },
      error: (err) => {
        this.error = 'Failed to delete movie';
      }
    });
  }

  cancelEdit() {
    this.showAddForm = false;
    this.editingMovie = null;
    this.currentMovie = { title: '' };
  }
}


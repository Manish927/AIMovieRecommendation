import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

interface Recommendation {
  movieId: number;
  title: string;
  score: number;
  reason: string;
  algorithm: string;
}

@Component({
  selector: 'app-recommendations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="px-4 py-6">
      <h2 class="text-3xl font-bold text-gray-900 mb-6">Movie Recommendations</h2>
      
      <div class="bg-white shadow-md rounded-lg p-6 mb-6">
        <div class="mb-4">
          <label class="block text-gray-700 text-sm font-bold mb-2" for="userId">
            User ID
          </label>
          <div class="flex gap-4">
            <input 
              [(ngModel)]="userId"
              name="userId"
              id="userId"
              type="number" 
              placeholder="Enter your User ID"
              class="flex-1 shadow appearance-none border rounded py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline">
            <button 
              (click)="loadRecommendations('hybrid')"
              [disabled]="!userId || loading"
              class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline disabled:opacity-50">
              Get Recommendations
            </button>
          </div>
        </div>
        
        <div class="flex gap-2">
          <button 
            (click)="loadRecommendations('collaborative')"
            [disabled]="!userId || loading"
            class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50">
            Collaborative Filtering
          </button>
          <button 
            (click)="loadRecommendations('content-based')"
            [disabled]="!userId || loading"
            class="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50">
            Content-Based
          </button>
          <button 
            (click)="loadRecommendations('hybrid')"
            [disabled]="!userId || loading"
            class="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 disabled:opacity-50">
            Hybrid (AI)
          </button>
        </div>
      </div>

      <div *ngIf="loading" class="text-center py-8">
        <p class="text-gray-600">Loading recommendations...</p>
      </div>

      <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ error }}
      </div>

      <div *ngIf="recommendations.length > 0" class="mb-4">
        <p class="text-gray-600 mb-4">
          Showing {{ recommendations.length }} recommendations using <strong>{{ currentAlgorithm }}</strong> algorithm
        </p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div 
          *ngFor="let rec of recommendations; let i = index" 
          class="bg-white rounded-lg shadow-md p-6 hover:shadow-xl transition-shadow">
          <div class="flex items-start justify-between mb-4">
            <span class="bg-indigo-100 text-indigo-800 text-xs font-semibold px-2 py-1 rounded">
              #{{ i + 1 }}
            </span>
            <span class="bg-gray-100 text-gray-800 text-xs font-semibold px-2 py-1 rounded">
              {{ (rec.score * 100).toFixed(0) }}% match
            </span>
          </div>
          <h3 class="text-xl font-bold text-gray-900 mb-2">{{ rec.title }}</h3>
          <p class="text-sm text-gray-600 mb-3">{{ rec.reason }}</p>
          <div class="flex items-center justify-between">
            <span class="text-xs text-gray-500">{{ rec.algorithm }}</span>
            <button 
              (click)="viewMovie(rec.movieId)"
              class="px-3 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700 text-sm">
              View Details
            </button>
          </div>
        </div>
      </div>

      <div *ngIf="recommendations.length === 0 && !loading && userId" class="text-center py-8">
        <p class="text-gray-600">No recommendations available. Try rating some movies first!</p>
      </div>
    </div>
  `
})
export class RecommendationsComponent implements OnInit {
  recommendations: Recommendation[] = [];
  loading = false;
  error: string | null = null;
  userId: number | null = null;
  currentAlgorithm = 'hybrid';
  private apiUrl = 'http://localhost:8082';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    // Try to get userId from localStorage if available
    const savedUserId = localStorage.getItem('userId');
    if (savedUserId) {
      this.userId = parseInt(savedUserId);
    }
  }

  loadRecommendations(algorithm: string) {
    if (!this.userId) {
      this.error = 'Please enter a User ID';
      return;
    }

    this.loading = true;
    this.error = null;
    this.currentAlgorithm = algorithm;

    let endpoint = '';
    switch (algorithm) {
      case 'collaborative':
        endpoint = `${this.apiUrl}/recommendations/user/${this.userId}/collaborative?limit=10`;
        break;
      case 'content-based':
        endpoint = `${this.apiUrl}/recommendations/user/${this.userId}/content-based?limit=10`;
        break;
      case 'hybrid':
      default:
        endpoint = `${this.apiUrl}/recommendations/user/${this.userId}/hybrid?limit=10`;
        break;
    }

    this.http.get<Recommendation[]>(endpoint).subscribe({
      next: (data) => {
        this.recommendations = data;
        this.loading = false;
        localStorage.setItem('userId', this.userId!.toString());
      },
      error: (err) => {
        this.error = 'Failed to load recommendations. Make sure you have rated some movies and the recommendation service is running.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  viewMovie(movieId: number) {
    window.open(`http://localhost:8081/movies/${movieId}`, '_blank');
  }
}



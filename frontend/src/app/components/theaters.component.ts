import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';

interface Theater {
  theaterId: number;
  name: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  phone: string;
  totalScreens: number;
}

interface TheaterMovie {
  id: number;
  theaterId: number;
  movieId: number;
  screenNumber: number;
  showTime: string;
  ticketPrice: number; // Base price
  dynamicPrice?: number; // Current dynamic price
  basePrice?: number; // Original base price
  predictedDemand?: number; // Predicted demand (0.0 to 1.0)
  availableSeats: number;
  totalSeats: number;
  lastPriceUpdate?: string;
}

interface DynamicPricing {
  theaterMovieId: number;
  basePrice: number;
  currentPrice: number;
  priceMultiplier: number;
  predictedDemand: number;
  pricingStrategy: string;
  lastUpdated: string;
  revenueImpact: number;
}

@Component({
  selector: 'app-theaters',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="px-4 py-6">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-3xl font-bold text-gray-900">Theaters & Showtimes</h2>
        <button 
          (click)="updateAllPricing()"
          [disabled]="updatingPricing"
          class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50">
          {{ updatingPricing ? 'Updating...' : 'Update All Pricing' }}
        </button>
      </div>
      
      <div class="mb-6 flex gap-4">
        <input 
          type="text" 
          [(ngModel)]="searchCity"
          (input)="searchTheaters()"
          placeholder="Search by city..." 
          class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent">
      </div>

      <div *ngIf="loading" class="text-center py-8">
        <p class="text-gray-600">Loading theaters...</p>
      </div>

      <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ error }}
      </div>

      <div class="space-y-6">
        <div 
          *ngFor="let theater of theaters" 
          class="bg-white rounded-lg shadow-md p-6">
          <div class="flex justify-between items-start mb-4">
            <div>
              <h3 class="text-2xl font-bold text-gray-900">{{ theater.name }}</h3>
              <p class="text-gray-600 mt-1">{{ theater.address }}, {{ theater.city }}, {{ theater.state }} {{ theater.zipCode }}</p>
              <p class="text-gray-500 text-sm mt-1">Phone: {{ theater.phone }} | Screens: {{ theater.totalScreens }}</p>
            </div>
            <button 
              (click)="loadShowtimes(theater.theaterId)"
              class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">
              View Showtimes
            </button>
          </div>

          <div *ngIf="(showtimes[theater.theaterId] || []).length > 0" class="mt-4 border-t pt-4">
            <h4 class="font-bold text-gray-900 mb-3">Current Showtimes:</h4>
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div 
                *ngFor="let showtime of showtimes[theater.theaterId]" 
                class="border border-gray-200 rounded p-3 hover:border-indigo-500 transition">
                <p class="font-semibold text-gray-900">Screen {{ showtime.screenNumber }}</p>
                <p class="text-sm text-gray-600">{{ formatDate(showtime.showTime) }}</p>
                
                <!-- Dynamic Pricing Display -->
                <div class="my-2 p-2 bg-gray-50 rounded">
                  <div class="flex items-center justify-between mb-1">
                    <span class="text-xs text-gray-500">Current Price:</span>
                    <span class="font-bold text-lg text-indigo-600">
                      &#36;{{ (showtime.dynamicPrice || showtime.ticketPrice)?.toFixed(2) }}
                    </span>
                  </div>
                  <div *ngIf="showtime.dynamicPrice && showtime.basePrice" class="flex items-center justify-between text-xs">
                    <span class="text-gray-500">Base Price:</span>
                    <span class="text-gray-600 line-through">&#36;{{ showtime.basePrice.toFixed(2) }}</span>
                    <span [class]="getPriceChangeClass(showtime.dynamicPrice, showtime.basePrice)">
                      {{ getPriceChangePercent(showtime.dynamicPrice, showtime.basePrice) }}
                    </span>
                  </div>
                  <div *ngIf="showtime.predictedDemand !== undefined" class="mt-2">
                    <div class="flex items-center justify-between text-xs mb-1">
                      <span class="text-gray-500">Demand:</span>
                      <span class="font-semibold" [class]="getDemandClass(showtime.predictedDemand)">
                        {{ (showtime.predictedDemand * 100).toFixed(0) }}%
                      </span>
                    </div>
                    <div class="w-full bg-gray-200 rounded-full h-1.5">
                      <div 
                        class="h-1.5 rounded-full transition-all"
                        [class]="getDemandBarClass(showtime.predictedDemand)"
                        [style.width.%]="showtime.predictedDemand * 100">
                      </div>
                    </div>
                  </div>
                </div>
                
                <p class="text-sm text-gray-600">Seats: {{ showtime.availableSeats }}/{{ showtime.totalSeats }}</p>
                <div class="flex gap-2 mt-2">
                  <button 
                    (click)="updatePricing(showtime.id, theater.theaterId)"
                    class="flex-1 px-2 py-1 bg-yellow-500 text-white rounded hover:bg-yellow-600 text-xs">
                    Update Price
                  </button>
                  <button 
                    (click)="bookTicket(showtime)"
                    [disabled]="showtime.availableSeats === 0"
                    class="flex-1 px-3 py-1 bg-indigo-600 text-white rounded hover:bg-indigo-700 disabled:bg-gray-400 disabled:cursor-not-allowed text-sm">
                    Book Now
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div *ngIf="theaters.length === 0 && !loading" class="text-center py-8">
        <p class="text-gray-600">No theaters found.</p>
      </div>
    </div>
  `
})
export class TheatersComponent implements OnInit {
  theaters: Theater[] = [];
  showtimes: { [theaterId: number]: TheaterMovie[] } = {};
  loading = false;
  updatingPricing = false;
  error: string | null = null;
  searchCity = '';
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient, private router: Router) {}

  ngOnInit() {
    this.loadTheaters();
  }

  loadTheaters() {
    this.loading = true;
    this.error = null;
    this.http.get<Theater[]>(`${this.apiUrl}/theaters`).subscribe({
      next: (data) => {
        this.theaters = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load theaters. Make sure the backend is running.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  searchTheaters() {
    if (this.searchCity.trim()) {
      this.loading = true;
      this.http.get<Theater[]>(`${this.apiUrl}/theaters/city/${encodeURIComponent(this.searchCity)}`).subscribe({
        next: (data) => {
          this.theaters = data;
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Failed to search theaters.';
          this.loading = false;
        }
      });
    } else {
      this.loadTheaters();
    }
  }

  loadShowtimes(theaterId: number) {
    this.http.get<TheaterMovie[]>(`${this.apiUrl}/theater-movies/theater/${theaterId}`).subscribe({
      next: (data) => {
        this.showtimes[theaterId] = data;
      },
      error: (err) => {
        console.error('Failed to load showtimes', err);
      }
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString();
  }

  bookTicket(showtime: TheaterMovie) {
    // Navigate to booking page with showtime pre-selected
    this.router.navigate(['/book'], { 
      queryParams: { 
        movieId: showtime.movieId,
        theaterMovieId: showtime.id
      } 
    });
  }

  updatePricing(showtimeId: number, theaterId: number) {
    this.http.post<DynamicPricing>(`${this.apiUrl}/dynamic-pricing/update/${showtimeId}`, {}).subscribe({
      next: (pricing) => {
        // Reload showtimes for the specific theater to reflect updated pricing
        this.loadShowtimes(theaterId);
        alert(`Price updated! New price: $${pricing.currentPrice.toFixed(2)} (${(pricing.priceMultiplier * 100 - 100).toFixed(0)}%)`);
      },
      error: (err) => {
        alert('Failed to update pricing');
        console.error(err);
      }
    });
  }

  updateAllPricing() {
    this.updatingPricing = true;
    this.http.post<string>(`${this.apiUrl}/dynamic-pricing/update-all`, {}).subscribe({
      next: (result) => {
        this.updatingPricing = false;
        alert(result);
        // Reload all theaters
        this.loadTheaters();
      },
      error: (err) => {
        this.updatingPricing = false;
        alert('Failed to update pricing');
        console.error(err);
      }
    });
  }

  getPriceChangePercent(current: number, base: number): string {
    const change = ((current - base) / base) * 100;
    const sign = change > 0 ? '+' : '';
    return `${sign}${change.toFixed(0)}%`;
  }

  getPriceChangeClass(current: number, base: number): string {
    const change = current - base;
    if (change > 0) {
      return 'text-red-600 font-semibold';
    } else if (change < 0) {
      return 'text-green-600 font-semibold';
    }
    return 'text-gray-600';
  }

  getDemandClass(demand: number): string {
    if (demand >= 0.8) return 'text-red-600';
    if (demand >= 0.6) return 'text-orange-600';
    if (demand >= 0.4) return 'text-yellow-600';
    return 'text-green-600';
  }

  getDemandBarClass(demand: number): string {
    if (demand >= 0.8) return 'bg-red-500';
    if (demand >= 0.6) return 'bg-orange-500';
    if (demand >= 0.4) return 'bg-yellow-500';
    return 'bg-green-500';
  }
}


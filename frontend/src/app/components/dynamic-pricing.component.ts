import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

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

interface DemandPrediction {
  theaterMovieId: number;
  predictedDemand: number;
  confidence: number;
  predictionMethod: string;
  predictedFor: string;
  factors: string;
  recommendedPriceMultiplier: number;
}

@Component({
  selector: 'app-dynamic-pricing',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="px-4 py-6">
      <h2 class="text-3xl font-bold text-gray-900 mb-6">Dynamic Pricing & Demand Prediction</h2>
      
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
        <!-- Revenue Impact Card -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-xl font-bold text-gray-900 mb-4">Total Revenue Impact</h3>
          <div class="text-4xl font-bold text-green-600 mb-2">
            &#36;{{ totalRevenueImpact?.toFixed(2) || '0.00' }}
          </div>
          <p class="text-gray-600 text-sm">Estimated additional revenue from dynamic pricing</p>
        </div>

        <!-- Statistics Card -->
        <div class="bg-white rounded-lg shadow-md p-6">
          <h3 class="text-xl font-bold text-gray-900 mb-4">Pricing Statistics</h3>
          <div class="space-y-2">
            <div class="flex justify-between">
              <span class="text-gray-600">Active Showtimes:</span>
              <span class="font-semibold">{{ pricingData.length }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Avg Price Multiplier:</span>
              <span class="font-semibold">{{ averageMultiplier.toFixed(2) }}x</span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600">Avg Demand:</span>
              <span class="font-semibold">{{ averageDemand.toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>

      <div *ngIf="loading" class="text-center py-8">
        <p class="text-gray-600">Loading pricing data...</p>
      </div>

      <div *ngIf="error" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
        {{ error }}
      </div>

      <!-- Pricing Table -->
      <div class="bg-white rounded-lg shadow-md overflow-hidden">
        <div class="px-6 py-4 bg-gray-50 border-b">
          <h3 class="text-xl font-bold text-gray-900">Current Pricing</h3>
        </div>
        <div class="overflow-x-auto">
          <table class="min-w-full divide-y divide-gray-200">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Showtime ID</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Base Price</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Current Price</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Multiplier</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Demand</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Revenue Impact</th>
                <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-gray-200">
              <tr *ngFor="let pricing of pricingData">
                <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  {{ pricing.theaterMovieId }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                  &#36;{{ pricing.basePrice.toFixed(2) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm font-semibold text-indigo-600">
                  &#36;{{ pricing.currentPrice.toFixed(2) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                  <span [class]="getMultiplierClass(pricing.priceMultiplier)">
                    {{ (pricing.priceMultiplier * 100).toFixed(0) }}%
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                  <div class="flex items-center">
                    <span class="mr-2">{{ (pricing.predictedDemand * 100).toFixed(0) }}%</span>
                    <div class="w-16 bg-gray-200 rounded-full h-2">
                      <div 
                        class="h-2 rounded-full"
                        [class]="getDemandBarClass(pricing.predictedDemand)"
                        [style.width.%]="pricing.predictedDemand * 100">
                      </div>
                    </div>
                  </div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm" [class]="getRevenueClass(pricing.revenueImpact)">
                  &#36;{{ pricing.revenueImpact.toFixed(2) }}
                </td>
                <td class="px-6 py-4 whitespace-nowrap text-sm">
                  <button 
                    (click)="updateSinglePricing(pricing.theaterMovieId)"
                    class="px-3 py-1 bg-yellow-500 text-white rounded hover:bg-yellow-600 text-xs">
                    Update
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class DynamicPricingComponent implements OnInit {
  pricingData: DynamicPricing[] = [];
  totalRevenueImpact: number = 0;
  loading = false;
  error: string | null = null;
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadPricingData();
    this.loadRevenueImpact();
  }

  loadPricingData() {
    // This would load from a specific theater or all showtimes
    // For now, we'll create a placeholder
    this.loading = true;
    // In a real implementation, you'd fetch from an endpoint
    this.loading = false;
  }

  loadRevenueImpact() {
    this.http.get<number>(`${this.apiUrl}/dynamic-pricing/revenue-impact`).subscribe({
      next: (impact) => {
        this.totalRevenueImpact = impact;
      },
      error: (err) => {
        console.error('Failed to load revenue impact', err);
      }
    });
  }

  updateSinglePricing(showtimeId: number) {
    this.http.post<DynamicPricing>(`${this.apiUrl}/dynamic-pricing/update/${showtimeId}`, {}).subscribe({
      next: () => {
        this.loadPricingData();
        this.loadRevenueImpact();
      },
      error: (err) => {
        alert('Failed to update pricing');
        console.error(err);
      }
    });
  }

  get averageMultiplier(): number {
    if (this.pricingData.length === 0) return 1.0;
    return this.pricingData.reduce((sum, p) => sum + p.priceMultiplier, 0) / this.pricingData.length;
  }

  get averageDemand(): number {
    if (this.pricingData.length === 0) return 0.5;
    return this.pricingData.reduce((sum, p) => sum + p.predictedDemand, 0) / this.pricingData.length;
  }

  getMultiplierClass(multiplier: number): string {
    if (multiplier >= 1.2) return 'text-red-600 font-semibold';
    if (multiplier >= 1.0) return 'text-orange-600';
    return 'text-green-600';
  }

  getDemandBarClass(demand: number): string {
    if (demand >= 0.8) return 'bg-red-500';
    if (demand >= 0.6) return 'bg-orange-500';
    if (demand >= 0.4) return 'bg-yellow-500';
    return 'bg-green-500';
  }

  getRevenueClass(revenue: number): string {
    return revenue > 0 ? 'text-green-600 font-semibold' : 'text-gray-600';
  }
}



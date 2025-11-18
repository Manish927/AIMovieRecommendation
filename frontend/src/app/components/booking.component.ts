import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { BookingService, BookingRequest, PriceBreakdown } from '../services/booking.service';
import { HttpClient } from '@angular/common/http';

interface Movie {
  movieId: number;
  title: string;
  description: string;
  genre: string;
  director: string;
  cast: string;
  releaseDate: string;
  duration: number;
  rating: number;
  language: string;
  posterUrl?: string;
}

interface Theater {
  theaterId: number;
  name: string;
  address: string;
  city: string;
  state: string;
  zipCode: string;
  phone: string;
}

interface TheaterMovie {
  id: number;
  theaterId: number;
  movieId: number;
  screenNumber: number;
  showTime: string;
  ticketPrice: number;
  dynamicPrice?: number;
  availableSeats: number;
  totalSeats: number;
}

interface Seat {
  id: string;
  row: string;
  column: number;
  status: 'available' | 'selected' | 'booked' | 'reserved';
  price?: number;
}

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="min-h-screen bg-gray-50 py-8">
      <div class="max-w-6xl mx-auto px-4">
        <!-- Progress Steps -->
        <div class="mb-8">
          <div class="flex items-center justify-between">
            <div class="flex items-center" [class.text-red-600]="currentStep >= 1" [class.text-gray-400]="currentStep < 1">
              <div class="w-8 h-8 rounded-full flex items-center justify-center border-2" 
                   [class.bg-red-600]="currentStep >= 1" [class.border-red-600]="currentStep >= 1" 
                   [class.border-gray-300]="currentStep < 1" [class.text-white]="currentStep >= 1">
                <span *ngIf="currentStep > 1">✓</span>
                <span *ngIf="currentStep <= 1">1</span>
              </div>
              <span class="ml-2 font-medium">Movie</span>
            </div>
            <div class="flex-1 h-0.5 mx-4" [class.bg-red-600]="currentStep >= 2" [class.bg-gray-300]="currentStep < 2"></div>
            <div class="flex items-center" [class.text-red-600]="currentStep >= 2" [class.text-gray-400]="currentStep < 2">
              <div class="w-8 h-8 rounded-full flex items-center justify-center border-2"
                   [class.bg-red-600]="currentStep >= 2" [class.border-red-600]="currentStep >= 2"
                   [class.border-gray-300]="currentStep < 2" [class.text-white]="currentStep >= 2">
                <span *ngIf="currentStep > 2">✓</span>
                <span *ngIf="currentStep <= 2">2</span>
              </div>
              <span class="ml-2 font-medium">Showtime</span>
            </div>
            <div class="flex-1 h-0.5 mx-4" [class.bg-red-600]="currentStep >= 3" [class.bg-gray-300]="currentStep < 3"></div>
            <div class="flex items-center" [class.text-red-600]="currentStep >= 3" [class.text-gray-400]="currentStep < 3">
              <div class="w-8 h-8 rounded-full flex items-center justify-center border-2"
                   [class.bg-red-600]="currentStep >= 3" [class.border-red-600]="currentStep >= 3"
                   [class.border-gray-300]="currentStep < 3" [class.text-white]="currentStep >= 3">
                <span *ngIf="currentStep > 3">✓</span>
                <span *ngIf="currentStep <= 3">3</span>
              </div>
              <span class="ml-2 font-medium">Seats</span>
            </div>
            <div class="flex-1 h-0.5 mx-4" [class.bg-red-600]="currentStep >= 4" [class.bg-gray-300]="currentStep < 4"></div>
            <div class="flex items-center" [class.text-red-600]="currentStep >= 4" [class.text-gray-400]="currentStep < 4">
              <div class="w-8 h-8 rounded-full flex items-center justify-center border-2"
                   [class.bg-red-600]="currentStep >= 4" [class.border-red-600]="currentStep >= 4"
                   [class.border-gray-300]="currentStep < 4" [class.text-white]="currentStep >= 4">
                <span *ngIf="currentStep > 4">✓</span>
                <span *ngIf="currentStep <= 4">4</span>
              </div>
              <span class="ml-2 font-medium">Review</span>
            </div>
          </div>
        </div>

        <!-- Error Message -->
        <div *ngIf="errorMessage" class="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {{ errorMessage }}
        </div>

        <!-- Step 1: Movie Selection -->
        <div *ngIf="currentStep === 1" class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-2xl font-bold mb-4">Select Movie</h2>
          <div *ngIf="selectedMovie" class="mb-4 p-4 bg-gray-50 rounded">
            <h3 class="font-bold text-lg">{{ selectedMovie.title }}</h3>
            <p class="text-sm text-gray-600">{{ selectedMovie.genre }} • {{ selectedMovie.duration }} min</p>
          </div>
          <div *ngIf="!selectedMovie" class="text-center py-8">
            <p class="text-gray-600 mb-4">Please select a movie from the home page to start booking.</p>
            <button (click)="goToHome()" class="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700">
              Go to Home
            </button>
          </div>
          <button *ngIf="selectedMovie" (click)="goToStep(2)" 
                  class="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700">
            Continue to Showtime Selection
          </button>
        </div>

        <!-- Step 2: Showtime Selection -->
        <div *ngIf="currentStep === 2" class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-2xl font-bold mb-4">Select Showtime</h2>
          <div *ngIf="theaters.length === 0" class="text-center py-8">
            <p class="text-gray-600">Loading theaters...</p>
          </div>
          <div class="space-y-4">
            <div *ngFor="let theater of theaters" class="border border-gray-200 rounded p-4">
              <h3 class="font-bold text-lg mb-2">{{ theater.name }}</h3>
              <p class="text-sm text-gray-600 mb-3">{{ theater.address }}, {{ theater.city }}</p>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                <button *ngFor="let showtime of getShowtimesForTheater(theater.theaterId)"
                        (click)="selectShowtime(showtime)"
                        [class.bg-red-600]="selectedShowtime?.id === showtime.id"
                        [class.text-white]="selectedShowtime?.id === showtime.id"
                        [class.bg-gray-200]="selectedShowtime?.id !== showtime.id"
                        [disabled]="showtime.availableSeats === 0"
                        class="px-4 py-2 rounded hover:bg-red-700 disabled:bg-gray-400 disabled:cursor-not-allowed">
                  <div class="text-sm font-medium">{{ formatTime(showtime.showTime) }}</div>
                  <div class="text-xs">Screen {{ showtime.screenNumber }}</div>
                  <div class="text-xs mt-1">₹{{ (showtime.dynamicPrice || showtime.ticketPrice).toFixed(0) }}</div>
                  <div class="text-xs">{{ showtime.availableSeats }} seats</div>
                </button>
              </div>
            </div>
          </div>
          <div class="mt-6 flex justify-between">
            <button (click)="goToStep(1)" class="px-6 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400">
              Back
            </button>
            <button (click)="goToStep(3)" [disabled]="!selectedShowtime" 
                    class="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-400">
              Continue to Seat Selection
            </button>
          </div>
        </div>

        <!-- Step 3: Seat Selection -->
        <div *ngIf="currentStep === 3" class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-2xl font-bold mb-4">Select Seats</h2>
          
          <!-- Screen -->
          <div class="mb-6 text-center">
            <div class="h-2 w-full bg-gray-700 rounded-b-lg mx-auto max-w-2xl"></div>
            <p class="text-sm font-semibold mt-2 text-gray-600">SCREEN THIS WAY</p>
          </div>

          <!-- Seat Map -->
          <div class="overflow-x-auto mb-6">
            <div class="flex flex-col items-center space-y-2 min-w-max">
              <div *ngFor="let row of seatRows" class="flex items-center space-x-2">
                <span class="w-8 text-center font-bold text-sm">{{ row }}</span>
                <button *ngFor="let seat of getSeatsForRow(row)"
                        (click)="toggleSeat(seat)"
                        [disabled]="seat.status === 'booked' || seat.status === 'reserved'"
                        class="w-8 h-8 rounded text-xs font-medium transition-all"
                        [ngClass]="{
                          'bg-gray-300 hover:bg-red-400': seat.status === 'available',
                          'bg-red-600 text-white ring-2 ring-red-300': seat.status === 'selected',
                          'bg-red-500': seat.status === 'booked',
                          'bg-yellow-400': seat.status === 'reserved',
                          'opacity-50 cursor-not-allowed': seat.status === 'booked' || seat.status === 'reserved'
                        }">
                  {{ seat.column }}
                </button>
                <span class="w-8 text-center font-bold text-sm">{{ row }}</span>
              </div>
            </div>
          </div>

          <!-- Legend -->
          <div class="flex justify-center gap-6 mb-6 text-sm">
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 bg-gray-300 rounded"></div>
              <span>Available</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 bg-red-600 rounded"></div>
              <span>Selected</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 bg-red-500 rounded"></div>
              <span>Booked</span>
            </div>
            <div class="flex items-center gap-2">
              <div class="w-4 h-4 bg-yellow-400 rounded"></div>
              <span>Reserved</span>
            </div>
          </div>

          <!-- Selected Seats Summary -->
          <div class="bg-gray-50 rounded p-4 mb-6">
            <h3 class="font-bold mb-2">Selected Seats ({{ selectedSeats.length }})</h3>
            <div *ngIf="selectedSeats.length > 0" class="flex flex-wrap gap-2">
              <span *ngFor="let seat of selectedSeats" 
                    class="px-3 py-1 bg-red-100 text-red-800 rounded-full text-sm font-medium">
                {{ seat.row }}{{ seat.column }}
              </span>
            </div>
            <p *ngIf="selectedSeats.length === 0" class="text-gray-500 text-sm">No seats selected</p>
          </div>

          <!-- Reservation Timer -->
          <div *ngIf="reservationTimer > 0 && selectedSeats.length > 0" 
               class="bg-yellow-50 border border-yellow-200 rounded p-3 mb-4">
            <p class="text-sm text-yellow-800">
              ⏱️ Seats reserved for: {{ formatTimer(reservationTimer) }}
            </p>
          </div>

          <div class="flex justify-between">
            <button (click)="goToStep(2)" class="px-6 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400">
              Back
            </button>
            <button (click)="goToStep(4)" [disabled]="selectedSeats.length === 0" 
                    class="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-400">
              Continue to Review ({{ selectedSeats.length }} seat{{ selectedSeats.length !== 1 ? 's' : '' }})
            </button>
          </div>
        </div>

        <!-- Step 4: Review & Payment -->
        <div *ngIf="currentStep === 4" class="bg-white rounded-lg shadow-md p-6">
          <h2 class="text-2xl font-bold mb-4">Review Booking</h2>
          
          <!-- Booking Summary -->
          <div class="bg-gray-50 rounded p-6 mb-6">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
              <div>
                <h3 class="font-bold text-lg mb-2">{{ selectedMovie?.title }}</h3>
                <p class="text-sm text-gray-600">{{ selectedMovie?.genre }} • {{ selectedMovie?.duration }} min</p>
              </div>
              <div>
                <p class="text-sm"><strong>Theater:</strong> {{ selectedTheater?.name }}</p>
                <p class="text-sm"><strong>Screen:</strong> {{ selectedShowtime?.screenNumber }}</p>
                <p class="text-sm"><strong>Showtime:</strong> {{ formatDateTime(selectedShowtime?.showTime) }}</p>
              </div>
            </div>
            <div class="border-t pt-4">
              <p class="text-sm mb-2"><strong>Selected Seats:</strong></p>
              <div class="flex flex-wrap gap-2">
                <span *ngFor="let seat of selectedSeats" 
                      class="px-2 py-1 bg-red-100 text-red-800 rounded text-xs">
                  {{ seat.row }}{{ seat.column }}
                </span>
              </div>
            </div>
          </div>

          <!-- Price Breakdown -->
          <div class="bg-gray-50 rounded p-6 mb-6">
            <h3 class="font-bold mb-4">Price Breakdown</h3>
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span>Base Price ({{ selectedSeats.length }} × ₹{{ (selectedShowtime?.dynamicPrice || selectedShowtime?.ticketPrice || 0).toFixed(0) }})</span>
                <span>₹{{ (priceBreakdown?.basePrice || 0).toFixed(2) }}</span>
              </div>
              <div *ngIf="priceBreakdown && (priceBreakdown.discountAmount || 0) > 0" class="flex justify-between text-green-600">
                <span>Discount ({{ discountCode }})</span>
                <span>-₹{{ (priceBreakdown.discountAmount || 0).toFixed(2) }}</span>
              </div>
              <div class="flex justify-between">
                <span>Subtotal</span>
                <span>₹{{ (priceBreakdown?.subtotal || 0).toFixed(2) }}</span>
              </div>
              <div class="flex justify-between">
                <span>Tax (18% GST)</span>
                <span>₹{{ (priceBreakdown?.taxAmount || 0).toFixed(2) }}</span>
              </div>
              <div class="flex justify-between">
                <span>Service Charge (5%)</span>
                <span>₹{{ (priceBreakdown?.serviceCharge || 0).toFixed(2) }}</span>
              </div>
              <div class="border-t pt-2 mt-2 flex justify-between font-bold text-lg">
                <span>Total</span>
                <span class="text-red-600">₹{{ (priceBreakdown?.totalPrice || 0).toFixed(2) }}</span>
              </div>
            </div>
          </div>

          <!-- Discount Code -->
          <div class="mb-6">
            <label class="block text-sm font-medium mb-2">Promo Code (Optional)</label>
            <div class="flex gap-2">
              <input type="text" [(ngModel)]="discountCode" placeholder="Enter promo code"
                     class="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500">
              <button (click)="applyDiscount()" 
                      class="px-6 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700">
                Apply
              </button>
            </div>
            <p *ngIf="discountMessage" class="mt-2 text-sm" 
               [class.text-green-600]="discountMessage.includes('applied')"
               [class.text-red-600]="discountMessage.includes('Invalid')">
              {{ discountMessage }}
            </p>
          </div>

          <!-- Loading State -->
          <div *ngIf="creatingBooking" class="text-center py-4">
            <p class="text-gray-600">Creating booking...</p>
          </div>

          <div class="flex justify-between">
            <button (click)="goToStep(3)" [disabled]="creatingBooking"
                    class="px-6 py-2 bg-gray-300 text-gray-700 rounded-lg hover:bg-gray-400">
              Back
            </button>
            <button (click)="createBooking()" [disabled]="creatingBooking || !priceBreakdown" 
                    class="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:bg-gray-400">
              {{ creatingBooking ? 'Processing...' : 'Confirm Booking' }}
            </button>
          </div>
        </div>

        <!-- Booking Success -->
        <div *ngIf="bookingSuccess" class="bg-white rounded-lg shadow-md p-6 text-center">
          <div class="text-green-600 text-6xl mb-4">✓</div>
          <h2 class="text-2xl font-bold mb-2">Booking Confirmed!</h2>
          <p class="text-gray-600 mb-4">Booking ID: {{ createdBookingId }}</p>
          <p class="text-sm text-gray-500 mb-6">You will receive a confirmation email shortly.</p>
          <button (click)="goToHome()" class="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700">
            Back to Home
          </button>
        </div>
      </div>
    </div>
  `
})
export class BookingComponent implements OnInit, OnDestroy {
  currentStep = 1;
  selectedMovie: Movie | null = null;
  selectedTheater: Theater | null = null;
  selectedShowtime: TheaterMovie | null = null;
  theaters: Theater[] = [];
  showtimes: TheaterMovie[] = [];
  seats: Seat[] = [];
  selectedSeats: Seat[] = [];
  discountCode = '';
  discountMessage = '';
  priceBreakdown: PriceBreakdown | null = null;
  reservationTimer = 0;
  private reservationInterval: any;
  creatingBooking = false;
  bookingSuccess = false;
  createdBookingId: number | null = null;
  errorMessage = '';
  private apiUrl = 'http://localhost:8081';

  seatRows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    // Get movie ID and theaterMovieId from route params
    this.route.queryParams.subscribe(params => {
      const movieId = params['movieId'];
      const theaterMovieId = params['theaterMovieId'];
      
      if (movieId) {
        this.loadMovie(Number(movieId), theaterMovieId ? Number(theaterMovieId) : undefined);
      }
    });
  }

  ngOnDestroy() {
    if (this.reservationInterval) {
      clearInterval(this.reservationInterval);
    }
  }

  loadMovie(movieId: number, preSelectedTheaterMovieId?: number) {
    this.http.get<Movie>(`${this.apiUrl}/movies/${movieId}`).subscribe({
      next: (movie) => {
        this.selectedMovie = movie;
        this.currentStep = 2; // Auto-advance to showtime selection when movie is pre-selected
        this.loadTheaters(preSelectedTheaterMovieId);
      },
      error: (err) => {
        this.errorMessage = 'Failed to load movie details';
        console.error(err);
      }
    });
  }

  loadTheaters(preSelectedTheaterMovieId?: number) {
    this.http.get<Theater[]>(`${this.apiUrl}/theaters`).subscribe({
      next: (theaters) => {
        this.theaters = theaters;
        // Load showtimes for all theaters
        theaters.forEach(theater => {
          this.loadShowtimes(theater.theaterId, preSelectedTheaterMovieId);
        });
      },
      error: (err) => {
        this.errorMessage = 'Failed to load theaters';
        console.error(err);
      }
    });
  }

  loadShowtimes(theaterId: number, preSelectedTheaterMovieId?: number) {
    this.http.get<TheaterMovie[]>(`${this.apiUrl}/theater-movies/theater/${theaterId}`).subscribe({
      next: (showtimes) => {
        // Filter showtimes for selected movie
        if (this.selectedMovie) {
          const movieShowtimes = showtimes.filter(st => st.movieId === this.selectedMovie!.movieId);
          this.showtimes.push(...movieShowtimes);
          
          // Auto-select showtime if pre-selected
          if (preSelectedTheaterMovieId) {
            const preselected = movieShowtimes.find(st => st.id === preSelectedTheaterMovieId);
            if (preselected) {
              this.selectShowtime(preselected);
              this.currentStep = 3; // Skip to seat selection
            }
          }
        }
      },
      error: (err) => {
        console.error('Failed to load showtimes', err);
      }
    });
  }

  getShowtimesForTheater(theaterId: number): TheaterMovie[] {
    return this.showtimes.filter(st => st.theaterId === theaterId);
  }

  selectShowtime(showtime: TheaterMovie) {
    this.selectedShowtime = showtime;
    this.selectedTheater = this.theaters.find(t => t.theaterId === showtime.theaterId) || null;
    this.initializeSeats();
  }

  initializeSeats() {
    // Initialize seat map (simplified - in production, fetch from backend)
    this.seats = [];
    const totalSeats = this.selectedShowtime?.totalSeats || 100;
    const bookedSeats = (this.selectedShowtime?.totalSeats || 0) - (this.selectedShowtime?.availableSeats || 0);
    
    let seatIndex = 0;
    this.seatRows.forEach(row => {
      for (let col = 1; col <= 10; col++) {
        const seatId = `${row}${col}`;
        let status: 'available' | 'selected' | 'booked' | 'reserved' = 'available';
        
        // Randomly mark some seats as booked (simplified)
        if (seatIndex < bookedSeats) {
          status = 'booked';
        }
        
        this.seats.push({
          id: seatId,
          row: row,
          column: col,
          status: status,
          price: (this.selectedShowtime?.dynamicPrice || this.selectedShowtime?.ticketPrice) || 0
        });
        seatIndex++;
      }
    });
  }

  getSeatsForRow(row: string): Seat[] {
    return this.seats.filter(s => s.row === row);
  }

  toggleSeat(seat: Seat) {
    if (seat.status === 'booked' || seat.status === 'reserved') {
      return;
    }

    const index = this.selectedSeats.findIndex(s => s.id === seat.id);
    if (index >= 0) {
      // Deselect
      this.selectedSeats.splice(index, 1);
      seat.status = 'available';
    } else {
      // Select
      this.selectedSeats.push(seat);
      seat.status = 'selected';
    }

    // Start reservation timer if seats selected
    if (this.selectedSeats.length > 0 && this.reservationTimer === 0) {
      this.startReservationTimer();
    } else if (this.selectedSeats.length === 0) {
      this.stopReservationTimer();
    }

    // Recalculate price
    this.calculatePrice();
  }

  startReservationTimer() {
    this.reservationTimer = 600; // 10 minutes in seconds
    this.reservationInterval = setInterval(() => {
      this.reservationTimer--;
      if (this.reservationTimer <= 0) {
        this.stopReservationTimer();
        // Release seats (in production, call API)
        this.selectedSeats.forEach(seat => {
          const s = this.seats.find(se => se.id === seat.id);
          if (s) {
            s.status = 'available';
          }
        });
        this.selectedSeats = [];
        this.errorMessage = 'Seat reservation expired. Please select seats again.';
      }
    }, 1000);
  }

  stopReservationTimer() {
    if (this.reservationInterval) {
      clearInterval(this.reservationInterval);
      this.reservationInterval = null;
    }
    this.reservationTimer = 0;
  }

  formatTimer(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }

  applyDiscount() {
    if (!this.discountCode) {
      return;
    }
    
    // Validate discount code (simplified - should call backend)
    if (this.discountCode.toUpperCase() === 'SAVE10') {
      this.discountMessage = 'Discount code applied!';
      this.calculatePrice();
    } else {
      this.discountMessage = 'Invalid discount code';
    }
  }

  calculatePrice() {
    if (!this.selectedShowtime || this.selectedSeats.length === 0) {
      this.priceBreakdown = null;
      return;
    }

    const pricePerTicket = this.selectedShowtime.dynamicPrice || this.selectedShowtime.ticketPrice || 0;
    this.bookingService.calculatePrice(
      pricePerTicket,
      this.selectedSeats.length,
      this.discountCode || undefined
    ).subscribe({
      next: (breakdown) => {
        this.priceBreakdown = breakdown;
      },
      error: (err) => {
        console.error('Failed to calculate price', err);
      }
    });
  }

  goToStep(step: number) {
    this.currentStep = step;
    if (step === 4) {
      this.calculatePrice();
    }
  }

  createBooking() {
    if (!this.selectedShowtime || !this.selectedMovie || this.selectedSeats.length === 0) {
      this.errorMessage = 'Please complete all booking details';
      return;
    }

    const userId = this.getUserId();
    if (!userId) {
      this.errorMessage = 'Please sign in to continue';
      return;
    }

    this.creatingBooking = true;
    this.errorMessage = '';

    const pricePerTicket = this.selectedShowtime.dynamicPrice || this.selectedShowtime.ticketPrice || 0;
    const request: BookingRequest = {
      userId: userId,
      theaterMovieId: this.selectedShowtime.id,
      numberOfSeats: this.selectedSeats.length,
      pricePerTicket: pricePerTicket,
      discountCode: this.discountCode || undefined
    };

    this.bookingService.createBooking(request).subscribe({
      next: (booking) => {
        this.creatingBooking = false;
        this.createdBookingId = booking.bookingId;
        this.bookingSuccess = true;
        this.stopReservationTimer();
      },
      error: (err) => {
        this.creatingBooking = false;
        this.errorMessage = err.error?.message || 'Failed to create booking. Please try again.';
        console.error(err);
      }
    });
  }

  getUserId(): number | null {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        return user.userID || null;
      } catch (e) {
        return null;
      }
    }
    return null;
  }

  formatTime(dateString: string): string {
    return new Date(dateString).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }

  formatDateTime(dateString: string | undefined): string {
    if (!dateString) return '';
    try {
      return new Date(dateString).toLocaleString();
    } catch (e) {
      return '';
    }
  }

  goToHome() {
    this.router.navigate(['/']);
  }
}


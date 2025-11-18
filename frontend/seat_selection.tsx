import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common'; // <-- IMPORTS: Added CommonModule for pipes

// --- Type Definitions ---
type SeatStatus = 'available' | 'booked' | 'selected';

interface Seat {
  id: string; // e.g., 'R1C5'
  row: number;
  col: number;
  status: SeatStatus;
}

// --- Constants ---
const ROWS = 8;
const COLS = 14;
const SEAT_PRICE = 150; // Price in currency units

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule], // <-- FIX: Imported CommonModule to enable the 'currency' pipe
  template: `
    <div class="min-h-screen bg-gray-100 dark:bg-gray-900 text-gray-800 dark:text-gray-100 p-4 sm:p-8">
      <div class="max-w-4xl mx-auto">

        <!-- Header -->
        <header class="text-center mb-8">
          <h1 class="text-3xl sm:text-4xl font-extrabold text-indigo-600 dark:text-indigo-400">
            Movie Seat Selection
          </h1>
          <p class="text-lg text-gray-500 dark:text-gray-400 mt-1">
            Book your seats for "Angular: The Reactive Journey"
          </p>
        </header>

        <!-- Screen Visual -->
        <div class="mb-8">
          <div class="h-2 w-full bg-gray-700 dark:bg-gray-600 rounded-b-lg shadow-xl shadow-gray-700/50 dark:shadow-indigo-900/50"></div>
          <p class="text-center text-sm font-semibold mt-2 text-gray-600 dark:text-gray-300">
            SCREEN THIS WAY
          </p>
        </div>

        <!-- Seat Map Container -->
        <div class="overflow-x-auto pb-4">
          <div class="flex flex-col items-center space-y-2">
            
            <!-- Seat Rows -->
            @for (row of seatMap(); track row.row) {
              <div class="flex space-x-2" [attr.aria-label]="'Row ' + row.row">
                <!-- Row Label -->
                <div class="w-8 flex items-center justify-center font-bold text-sm text-gray-500 dark:text-gray-400">
                  {{ row.rowLabel }}
                </div>
                
                <!-- Seats in Row -->
                @for (seat of row.seats; track seat.id) {
                  <button 
                    [disabled]="seat.status === 'booked'"
                    (click)="toggleSeatSelection(seat.id)"
                    [attr.aria-label]="'Seat ' + seat.id + ' status: ' + seat.status"
                    class="
                      w-7 h-7 sm:w-8 sm:h-8 rounded-md transition-all duration-150 ease-in-out shadow-sm
                      disabled:cursor-not-allowed disabled:shadow-inner disabled:opacity-70
                      focus:ring-4 focus:ring-indigo-500/50 dark:focus:ring-indigo-300/50
                    "
                    [ngClass]="{
                      'bg-gray-300 hover:bg-indigo-400 dark:bg-gray-700 dark:hover:bg-indigo-600 cursor-pointer': seat.status === 'available',
                      'bg-indigo-600 hover:bg-indigo-700 dark:bg-indigo-500 dark:hover:bg-indigo-700 text-white cursor-pointer ring-2 ring-indigo-300 dark:ring-indigo-800': seat.status === 'selected',
                      'bg-red-500 dark:bg-red-700': seat.status === 'booked'
                    }"
                  >
                  </button>
                }
                <!-- Row Label (Right) -->
                <div class="w-8 flex items-center justify-center font-bold text-sm text-gray-500 dark:text-gray-400">
                  {{ row.rowLabel }}
                </div>
              </div>
            }
          </div>
        </div>

        <!-- Legend and Summary -->
        <div class="mt-10 bg-white dark:bg-gray-800 p-6 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700">
          
          <!-- Legend -->
          <div class="flex justify-center flex-wrap gap-4 mb-6 text-sm font-medium">
            <div class="flex items-center space-x-2">
              <div class="w-4 h-4 rounded-sm bg-gray-300 dark:bg-gray-700"></div>
              <span>Available</span>
            </div>
            <div class="flex items-center space-x-2">
              <div class="w-4 h-4 rounded-sm bg-indigo-600 dark:bg-indigo-500"></div>
              <span>Selected</span>
            </div>
            <div class="flex items-center space-x-2">
              <div class="w-4 h-4 rounded-sm bg-red-500 dark:bg-red-700"></div>
              <span>Booked</span>
            </div>
          </div>

          <hr class="border-gray-200 dark:border-gray-700 mb-6">
          
          <!-- Summary -->
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- Selected Seats List -->
            <div class="p-3 bg-gray-50 dark:bg-gray-900 rounded-lg">
              <h3 class="font-semibold mb-2 text-lg text-indigo-600 dark:text-indigo-400">Your Selection ({{ selectedSeats().length }})</h3>
              @if (selectedSeats().length > 0) {
                <div class="flex flex-wrap gap-2 text-sm">
                  @for (seatId of selectedSeats(); track seatId) {
                    <span class="px-2 py-1 bg-indigo-100 dark:bg-indigo-900 text-indigo-800 dark:text-indigo-200 rounded-full font-medium">
                      {{ seatId }}
                    </span>
                  }
                </div>
              } @else {
                <p class="text-gray-500 dark:text-gray-400">Please click on an available seat to select it.</p>
              }
            </div>

            <!-- Price Summary -->
            <div class="p-3 bg-gray-50 dark:bg-gray-900 rounded-lg">
              <p class="text-base font-medium">Seat Price: <span class="font-bold text-green-600 dark:text-green-400">{{ SEAT_PRICE | currency:'$' }}</span></p>
              <p class="text-base font-medium">Quantity: <span class="font-bold">{{ selectedSeats().length }}</span></p>
              <div class="mt-3 pt-3 border-t border-gray-300 dark:border-gray-600">
                <p class="text-xl font-extrabold flex justify-between items-center">
                  Total Cost: 
                  <span class="text-indigo-600 dark:text-indigo-400">{{ totalCost() | currency:'$' }}</span>
                </p>
              </div>
              
              <button 
                [disabled]="selectedSeats().length === 0"
                class="mt-4 w-full py-3 rounded-lg font-bold text-lg transition-colors duration-200 shadow-md 
                      disabled:bg-gray-400 disabled:cursor-not-allowed
                      bg-indigo-600 hover:bg-indigo-700 text-white dark:bg-indigo-500 dark:hover:bg-indigo-600"
                (click)="bookSeats()"
              >
                Book {{ selectedSeats().length }} Seat{{ selectedSeats().length === 1 ? '' : 's' }}
              </button>
            </div>
          </div>

        </div>
      </div>
    </div>
  `,
  styles: [`
    /* Ensure the body font is used */
    :host {
      font-family: 'Inter', sans-serif;
    }
  `],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  // Use a string array signal to hold the IDs of the currently selected seats.
  selectedSeats = signal<string[]>([]);

  // 2D Array signal representing the whole theater map.
  // Using an array of objects for easier template iteration and row labeling.
  seatMap = signal<{ rowLabel: string, seats: Seat[] }[]>([]);

  readonly SEAT_PRICE = SEAT_PRICE;

  constructor() {
    this.initializeSeats();
  }

  /**
   * Initializes the seat map with available and randomly booked seats.
   */
  initializeSeats(): void {
    const map: { rowLabel: string, seats: Seat[] }[] = [];
    
    for (let r = 1; r <= ROWS; r++) {
      const rowSeats: Seat[] = [];
      const rowLabel = String.fromCharCode(64 + r); // A, B, C, ...

      for (let c = 1; c <= COLS; c++) {
        const id = `${rowLabel}${c}`;
        
        // Randomly set some seats as 'booked' for demonstration
        const isBooked = Math.random() < 0.15; // ~15% are booked
        
        rowSeats.push({
          id,
          row: r,
          col: c,
          status: isBooked ? 'booked' : 'available',
        });
      }
      map.push({ rowLabel, seats: rowSeats });
    }
    this.seatMap.set(map);
  }

  /**
   * Computed signal to calculate the total booking cost.
   * This updates automatically whenever selectedSeats changes.
   */
  totalCost = computed(() => {
    return this.selectedSeats().length * SEAT_PRICE;
  });

  /**
   * Finds a seat in the 2D map by its ID.
   * @param id The seat ID (e.g., 'A1', 'B10').
   * @returns The found Seat object or undefined.
   */
  private findSeat(id: string): { row: { rowLabel: string, seats: Seat[] }, seat: Seat, mapIndex: number, seatIndex: number } | undefined {
    const map = this.seatMap();
    for (let i = 0; i < map.length; i++) {
        const seatIndex = map[i].seats.findIndex(s => s.id === id);
        if (seatIndex !== -1) {
            return {
                row: map[i],
                seat: map[i].seats[seatIndex],
                mapIndex: i,
                seatIndex: seatIndex
            };
        }
    }
    return undefined;
  }

  /**
   * Toggles the selection status of an available seat.
   * @param id The seat ID to toggle.
   */
  toggleSeatSelection(id: string): void {
    const seatInfo = this.findSeat(id);
    if (!seatInfo || seatInfo.seat.status === 'booked') {
        return;
    }

    // Use signal's update mechanism to modify the nested state safely.
    this.seatMap.update(map => {
      const updatedMap = [...map];
      const row = updatedMap[seatInfo.mapIndex];
      const seat = row.seats[seatInfo.seatIndex];

      if (seat.status === 'selected') {
        // Deselect
        seat.status = 'available';
        this.selectedSeats.update(seats => seats.filter(sId => sId !== id));
      } else if (seat.status === 'available') {
        // Select
        seat.status = 'selected';
        this.selectedSeats.update(seats => [...seats, id]);
      }
      
      // Since objects within the array were mutated, the change is visible.
      // However, to ensure full reactivity with ChangeDetectionStrategy.OnPush, 
      // we must return a new array reference at the top level.
      return updatedMap; 
    });
  }

  /**
   * Simulates the booking process.
   */
  bookSeats(): void {
    const seatsToBook = this.selectedSeats();
    
    if (seatsToBook.length === 0) {
      console.log('No seats selected.');
      return;
    }

    // 1. Update seat status to 'booked' on the map
    this.seatMap.update(map => {
      const updatedMap = [...map];

      for (const seatId of seatsToBook) {
        const seatInfo = this.findSeat(seatId);
        if (seatInfo) {
          // Mutate the specific seat status
          updatedMap[seatInfo.mapIndex].seats[seatInfo.seatIndex].status = 'booked';
        }
      }
      return updatedMap;
    });

    // 2. Clear the selected seats
    this.selectedSeats.set([]);

    // 3. Provide feedback (since we can't use alert/confirm)
    // NOTE: The calculation inside the console.log was slightly off, showing double price. 
    // Fixed it to just use the total cost before it was cleared.
    const bookingMessage = `Successfully booked ${seatsToBook.length} seat(s): ${seatsToBook.join(', ')} for $${seatsToBook.length * SEAT_PRICE}.`;
    
    console.log(bookingMessage);
    
    // In a real app, you would show a toast or a modal here.
    const feedbackElement = document.getElementById('booking-feedback');
    if (feedbackElement) {
        feedbackElement.textContent = `Booking Confirmed! You booked: ${seatsToBook.join(', ')}`;
        setTimeout(() => feedbackElement.textContent = '', 5000); // Clear message after 5 seconds
    }
  }
}
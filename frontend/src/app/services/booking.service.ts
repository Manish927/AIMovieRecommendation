import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BookingRequest {
  userId: number;
  theaterMovieId: number;
  numberOfSeats: number;
  pricePerTicket: number;
  discountCode?: string;
}

export interface Booking {
  bookingId: number;
  userId: number;
  theaterMovieId: number;
  numberOfSeats: number;
  basePrice: number;
  taxAmount: number;
  serviceCharge: number;
  discountAmount: number;
  totalPrice: number;
  pricePerTicket: number;
  bookingTime: string;
  status: string;
  reservationExpiresAt?: string;
}

export interface PriceBreakdown {
  basePrice: number;
  discountAmount: number;
  subtotal: number;
  taxAmount: number;
  serviceCharge: number;
  totalPrice: number;
}

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private apiUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  createBooking(request: BookingRequest): Observable<Booking> {
    const headers = this.getAuthHeaders();
    return this.http.post<Booking>(`${this.apiUrl}/bookings`, request, { headers });
  }

  getBooking(bookingId: number): Observable<Booking> {
    const headers = this.getAuthHeaders();
    return this.http.get<Booking>(`${this.apiUrl}/bookings/${bookingId}`, { headers });
  }

  getUserBookings(userId: number): Observable<Booking[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<Booking[]>(`${this.apiUrl}/bookings/user/${userId}`, { headers });
  }

  cancelBooking(bookingId: number): Observable<Booking> {
    const headers = this.getAuthHeaders();
    return this.http.put<Booking>(`${this.apiUrl}/bookings/${bookingId}/cancel`, {}, { headers });
  }

  calculatePrice(
    pricePerTicket: number,
    numberOfSeats: number,
    discountCode?: string
  ): Observable<PriceBreakdown> {
    // Calculate price breakdown (this would ideally come from backend, but we'll calculate client-side for now)
    const basePrice = pricePerTicket * numberOfSeats;
    let discountAmount = 0;
    
    // Apply discount if code provided (simplified - should validate with backend)
    if (discountCode && discountCode.toUpperCase() === 'SAVE10') {
      discountAmount = basePrice * 0.10;
    }
    
    const subtotal = basePrice - discountAmount;
    const taxAmount = subtotal * 0.18; // 18% GST
    const serviceCharge = subtotal * 0.05; // 5% service charge
    const totalPrice = subtotal + taxAmount + serviceCharge;

    return new Observable(observer => {
      observer.next({
        basePrice,
        discountAmount,
        subtotal,
        taxAmount,
        serviceCharge,
        totalPrice
      });
      observer.complete();
    });
  }

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    if (token) {
      return new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });
    }
    return new HttpHeaders({
      'Content-Type': 'application/json'
    });
  }
}


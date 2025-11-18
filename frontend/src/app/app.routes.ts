import { Routes } from '@angular/router';
import { WelcomeComponent } from './components/welcome.component';
import { BookingComponent } from './components/booking.component';

export const routes: Routes = [
  {
    path: '',
    component: WelcomeComponent
  },
  {
    path: 'movies',
    component: WelcomeComponent
  },
  {
    path: 'book',
    component: BookingComponent
  },
  {
    path: '**',
    redirectTo: ''
  }
];



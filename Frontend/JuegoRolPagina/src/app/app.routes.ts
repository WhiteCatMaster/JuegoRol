import { Routes } from '@angular/router';
import { Landing } from './landing/landing';
import { OpcionesComponent } from './opciones.component';

export const routes: Routes = [
  { path: '', component: Landing },
  { path: 'opciones', component: OpcionesComponent }
];

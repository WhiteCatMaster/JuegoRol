import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class MusicaService {
  urlYoutube = signal<string | null>(null);

  constructor() {}
}
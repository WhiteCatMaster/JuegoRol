import { TestBed } from '@angular/core/testing';

import { ServicioAPI } from './servicio-api';

describe('ServicioAPI', () => {
  let service: ServicioAPI;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ServicioAPI);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

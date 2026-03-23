import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreadorHechizos } from './creador-hechizos';

describe('CreadorHechizos', () => {
  let component: CreadorHechizos;
  let fixture: ComponentFixture<CreadorHechizos>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreadorHechizos],
    }).compileComponents();

    fixture = TestBed.createComponent(CreadorHechizos);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

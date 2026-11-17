import { TestBed } from '@angular/core/testing';
import { provideTaiga } from '@taiga-ui/core';
import { of } from 'rxjs';
import { GeolocalizacionService } from '../../../core/services/geolocalizacion-service';
import { MapaSede } from './mapa-sede';

describe('Mapa sede · distribución ESM real de Leaflet', () => {
  const geo = { detectar: vi.fn(() => of({ latitud: -15.49, longitud: -70.13, precisionM: 10 })) };
  beforeEach(() => {
    Object.defineProperty(window, 'matchMedia', {
      configurable: true,
      value: () => ({ matches: false, addEventListener() {}, removeEventListener() {} }),
    });
    geo.detectar.mockClear();
  });
  async function create() {
    await TestBed.configureTestingModule({
      imports: [MapaSede], providers: [provideTaiga(), { provide: GeolocalizacionService, useValue: geo }],
    }).compileComponents();
    const f = TestBed.createComponent(MapaSede);
    f.componentRef.setInput('inicial', { latitud: -12.0463, longitud: -77.0428 });
    f.detectChanges();
    await f.whenStable();
    return f;
  }
  it('crea el mapa y el marcador desde la sede inicial y actualiza el radio', async () => {
    const f = await create();
    expect(f.nativeElement.querySelector('.leaflet-container')).toBeTruthy();
    expect(f.nativeElement.querySelector('.sede-pin')).toBeTruthy();
    const link = f.nativeElement.querySelector('a[href*="maps"]') as HTMLAnchorElement;
    expect(link.href).toContain('-12.0463');
    f.componentRef.setInput('tolerancia', 250);
    f.detectChanges();
    await f.whenStable();
    expect(f.nativeElement.querySelector('.sede-pin')).toBeTruthy();
    f.destroy();
  });
  it('usa la ubicación y emite las coordenadas conservando la API del mapa', async () => {
    const f = await create();
    const changed = vi.fn();
    f.componentInstance.sedeChange.subscribe(changed);
    f.componentInstance.usarMiUbicacion();
    f.detectChanges();
    expect(geo.detectar).toHaveBeenCalledTimes(1);
    expect(changed).toHaveBeenCalledWith({ latitud: -15.49, longitud: -70.13 });
    f.destroy();
  });
});

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { GameService } from './game.service';
import { API_BASE } from '../core/config/api.config';

// Verifica REQ-016/REQ-017: a API é consumida por caminho RELATIVO (mesma origem),
// sem URL absoluta (sem http://host:porta). O proxy de dev encaminha /api → backend.
describe('GameService (API relativa)', () => {
  let service: GameService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GameService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GameService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('A base relativa da API é /api/v1', () => {
    expect(API_BASE).toBe('/api/v1');
  });

  it('getPerfil usa caminho relativo /api/v1/game/perfil (sem URL absoluta)', () => {
    service.getPerfil().subscribe();
    const req = httpMock.expectOne('/api/v1/game/perfil');
    expect(req.request.url.startsWith('/api/v1/')).toBe(true);
    expect(req.request.url).not.toMatch(/^https?:\/\//);
    req.flush({});
  });

  it('getProtocolos usa caminho relativo /api/v1/game/protocolos', () => {
    service.getProtocolos().subscribe();
    const req = httpMock.expectOne('/api/v1/game/protocolos');
    expect(req.request.url).not.toMatch(/^https?:\/\//);
    req.flush([]);
  });
});

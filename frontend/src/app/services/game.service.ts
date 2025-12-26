import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

//INTERFACES

export interface Protocolo {
  id: number;
  nome: string;
  icone: string;
  cor?: string;
  descricao?: string;
  atributo: 'FORCA' | 'DESTREZA' | 'INTELECTO' | 'CARISMA' | 'CONSTITUICAO';
  dificuldade: 'EASY' | 'MEDIUM' | 'HARD' | 'EPIC';
  duracaoMinutos: number;
}

export interface Usuario {
  id: number;
  nome: string;
  classe: string;
  nivel: number;
  xpAtual: number;
  xpParaProximoNivel: number;
  moedas: number;
  
  // Atributos
  forca: number;
  destreza: number;
  intelecto: number;
  carisma: number;
  constituicao: number;

  // Recursos
  hpAtual: number;
  maxHp: number;
  mpAtual: number;
  maxMp: number;
}

// SERVIÇO

@Injectable({
  providedIn: 'root'
})
export class GameService {

  // URL da API
  private apiUrl = 'http://localhost:8080/game';

  constructor(private http: HttpClient) { }

  // 1. Pegar Perfil
  getPerfil(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/perfil`);
  }

  // 2. Listar Missões
  getProtocolos(): Observable<Protocolo[]> {
    return this.http.get<Protocolo[]>(`${this.apiUrl}/protocolos`);
  }

  // 3. Criar Missão
  criarProtocolo(protocolo: Protocolo): Observable<Protocolo> {
    return this.http.post<Protocolo>(`${this.apiUrl}/protocolos`, protocolo);
  }

  // 4. Fazer Check-in (Ganhar XP)
  fazerCheckin(protocoloId: number): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/checkin/${protocoloId}`, {});
  }

  // 5. Configurar Personagem (Setup)
  setup(dados: { nome: string; classe: string }): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/setup`, dados);
  }

  // 6. Ações de Jogo
  usarCura(): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/habilidade/curar`, {});
  }

  sofrerDano(qtd: number): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/dano/${qtd}`, {});
  }

  // 7. Histórico
  getHistorico(data: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/historico?data=${data}`);
  }
}
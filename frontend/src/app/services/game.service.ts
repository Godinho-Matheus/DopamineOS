import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';

// 1. ENUMS
export enum Atributo {
  FORCA = 'FORCA',
  DESTREZA = 'DESTREZA',
  INTELECTO = 'INTELECTO',
  CARISMA = 'CARISMA',
  CONSTITUICAO = 'CONSTITUICAO'
}

export enum Dificuldade {
  EASY = 'EASY',
  MEDIUM = 'MEDIUM',
  HARD = 'HARD',
  EPIC = 'EPIC'
}

export enum ClasseRPG {
  GUERREIRO = 'GUERREIRO',
  MAGO = 'MAGO',
  LADINO = 'LADINO'
}

// 2. INTERFACES

export interface LogAtividade {
  id: number;
  nomeMissao: string;
  icone: string;
  xpGanho: number;
  duracaoMinutos: number;
  dataHora: string;
}

export interface Protocolo {
  id?: number;
  nome: string;
  icone: string;
  cor?: string;
  descricao?: string;
  atributo: Atributo;
  dificuldade: Dificuldade;
  duracaoMinutos: number;
}


export interface Usuario {
  id: number;
  nome: string;
  classe: ClasseRPG;
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

export interface StandardError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

// 3. O SERVIÇO

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private apiUrl = 'http://localhost:8080/api/v1/game';
  
  constructor(private http: HttpClient) { }

  // 1. Pegar Perfil
  getPerfil(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/perfil`);
  }

  // 2. Listar Missões
  getProtocolos(): Observable<Protocolo[]> {
    return this.http.get<Protocolo[]>(`${this.apiUrl}/protocolos`);
  }

  // 3. Criar Missão Única
  criarProtocolo(protocolo: Protocolo): Observable<Protocolo> {
    return this.http.post<Protocolo>(`${this.apiUrl}/protocolo`, protocolo);
  }

  // 4. Salvar Várias Missões
  salvarMissoes(protocolos: Protocolo[]): Observable<Protocolo[]> {
    return this.http.post<Protocolo[]>(`${this.apiUrl}/protocolos`, protocolos);
  }

  // 5. Fazer Check-in
  fazerCheckin(protocoloId: number): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/checkin/${protocoloId}`, {});
  }

  // 6. Setup Inicial
  setup(nome: string, classe: ClasseRPG): Observable<Usuario> {
    const payload = { nome, classe };
    return this.http.post<Usuario>(`${this.apiUrl}/setup`, payload);
  }
  
  // 7. Histórico Geral
  getHistoricoGeral(): Observable<LogAtividade[]> {
    return this.http.get<LogAtividade[]>(`${this.apiUrl}/historico`);
  }
}
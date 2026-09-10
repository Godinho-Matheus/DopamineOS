import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

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
  dataHora: string; // ISO string (LocalDateTime serializado pelo Jackson)
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
  forca: number;
  destreza: number;
  intelecto: number;
  carisma: number;
  constituicao: number;
  hpAtual: number;
  maxHp: number;
  mpAtual: number;
  maxMp: number;
}

export interface SetupPayload {
  nome: string;
  classe: ClasseRPG;
  missoes?: Protocolo[];
}

// 3. O SERVIÇO

@Injectable({
  providedIn: 'root'
})
export class GameService {
  private readonly apiUrl = 'http://localhost:8080/api/v1/game';

  constructor(private http: HttpClient) {}

  getPerfil(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.apiUrl}/perfil`);
  }

  getProtocolos(): Observable<Protocolo[]> {
    return this.http.get<Protocolo[]>(`${this.apiUrl}/protocolos`);
  }

  criarProtocolo(protocolo: Protocolo): Observable<Protocolo> {
    return this.http.post<Protocolo>(`${this.apiUrl}/protocolo`, protocolo);
  }

  salvarMissoes(protocolos: Protocolo[]): Observable<Protocolo[]> {
    return this.http.post<Protocolo[]>(`${this.apiUrl}/protocolos`, protocolos);
  }

  deletarProtocolo(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/protocolo/${id}`);
  }

  fazerCheckin(protocoloId: number): Observable<Usuario> {
    return this.http.post<Usuario>(`${this.apiUrl}/checkin/${protocoloId}`, {});
  }

  /**
   * Envia nome, classe e, opcionalmente, as missões escolhidas pelo usuário.
   * O backend cria o deck padrão se missoes estiver vazio.
   */
  setup(nome: string, classe: ClasseRPG, missoes: Protocolo[] = []): Observable<Usuario> {
    const payload: SetupPayload = { nome, classe, missoes };
    return this.http.post<Usuario>(`${this.apiUrl}/setup`, payload);
  }

  getHistoricoGeral(): Observable<LogAtividade[]> {
    return this.http.get<LogAtividade[]>(`${this.apiUrl}/historico`);
  }
}
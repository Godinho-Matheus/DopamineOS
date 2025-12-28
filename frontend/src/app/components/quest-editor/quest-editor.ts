import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { GameService, Protocolo, Atributo, Dificuldade } from '../../services/game.service';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-quest-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, SidebarComponent],
  templateUrl: './quest-editor.html'
})
export class QuestEditorComponent implements OnInit {
  protocolos: Protocolo[] = [];
  public readonly EAtributo = Atributo;
  public readonly EDificuldade = Dificuldade;

  novaMissao: Protocolo = {
    nome: '',
    icone: '⚔️',
    descricao: 'Nova tarefa',
    atributo: Atributo.FORCA,
    dificuldade: Dificuldade.EASY,
    duracaoMinutos: 15
  };

  // PRESETS
  presets: Partial<Protocolo>[] = [
    { nome: 'Beber Água', icone: '💧', atributo: Atributo.CONSTITUICAO, dificuldade: Dificuldade.EASY, duracaoMinutos: 1 },
    { nome: 'Treino Pesado', icone: '🏋️', atributo: Atributo.FORCA, dificuldade: Dificuldade.HARD, duracaoMinutos: 60 },
    { nome: 'Ler Livro', icone: '📚', atributo: Atributo.INTELECTO, dificuldade: Dificuldade.MEDIUM, duracaoMinutos: 30 },
    { nome: 'Meditar', icone: '🧘', atributo: Atributo.INTELECTO, dificuldade: Dificuldade.EASY, duracaoMinutos: 15 },
    { nome: 'Correr', icone: '🏃', atributo: Atributo.DESTREZA, dificuldade: Dificuldade.MEDIUM, duracaoMinutos: 45 },
    { nome: 'Networking', icone: '🤝', atributo: Atributo.CARISMA, dificuldade: Dificuldade.HARD, duracaoMinutos: 60 },
    { nome: 'Dormir Cedo', icone: '💤', atributo: Atributo.CONSTITUICAO, dificuldade: Dificuldade.MEDIUM, duracaoMinutos: 480 },
    { nome: 'Cozinhar', icone: '🍳', atributo: Atributo.DESTREZA, dificuldade: Dificuldade.EASY, duracaoMinutos: 40 }
  ];

  constructor(private gameService: GameService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar() {
    this.gameService.getProtocolos().subscribe({
      next: (dados) => this.protocolos = dados,
      error: (erro) => console.error('Erro ao carregar', erro)
    });
  }

  usarPreset(preset: any) {
    this.novaMissao = {
      ...this.novaMissao,
      ...preset,
      descricao: 'Hábito predefinido'
    };
  }

  salvar() {
    if (!this.novaMissao.nome) return;

    this.gameService.criarProtocolo(this.novaMissao).subscribe({
      next: () => {
        this.carregar();
        this.resetarFormulario();
      },
      error: (err) => alert('Erro ao salvar: ' + err.message)
    });
  }


  deletar(id: number | undefined) {
    if (!id) return;
    if(confirm('Rasgar este contrato de missão?')) {
       // this.gameService.deletarProtocolo(id).subscribe(() => this.carregar());
       alert('Implemente o DELETE no backend primeiro!'); 
    }
  }


  private resetarFormulario() {
    this.novaMissao = {
      nome: '',
      icone: '✨',
      descricao: '',
      atributo: Atributo.FORCA,
      dificuldade: Dificuldade.EASY,
      duracaoMinutos: 15
    };
  }

  getCorAtributo(attr: Atributo): string {
    switch(attr) {
      case Atributo.FORCA: return 'text-orange-500 border-orange-500/50 bg-orange-500/10';
      case Atributo.DESTREZA: return 'text-green-500 border-green-500/50 bg-green-500/10';
      case Atributo.INTELECTO: return 'text-blue-500 border-blue-500/50 bg-blue-500/10';
      case Atributo.CARISMA: return 'text-purple-500 border-purple-500/50 bg-purple-500/10';
      case Atributo.CONSTITUICAO: return 'text-red-500 border-red-500/50 bg-red-500/10';
      default: return 'text-gray-500';
    }
  }
  
  get chavesAtributo(): string[] {
    return Object.keys(Atributo);
  }
  
  get chavesDificuldade(): string[] {
    return Object.keys(Dificuldade);
  }
}
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { GameService, Protocolo } from '../../services/game.service';
import { SidebarComponent } from '../sidebar/sidebar';

@Component({
  selector: 'app-quest-editor',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, SidebarComponent],
  templateUrl: './quest-editor.html'
})
export class QuestEditorComponent implements OnInit {

  protocolos: Protocolo[] = [];

  // Objeto da nova missão
  novaMissao = {
    nome: '',
    icone: '⚔️',
    descricao: 'Nova tarefa',
    atributo: 'FORCA',
    dificuldade: 'EASY'
  };

  // --- NOVO: PRESETS (Modelos Prontos) ---
  presets = [
    { nome: 'Beber Água', icone: '💧', atributo: 'CONSTITUICAO', dificuldade: 'EASY' },
    { nome: 'Treino Pesado', icone: '🏋️', atributo: 'FORCA', dificuldade: 'HARD' },
    { nome: 'Ler Livro', icone: '📚', atributo: 'INTELECTO', dificuldade: 'MEDIUM' },
    { nome: 'Meditar', icone: '🧘', atributo: 'INTELECTO', dificuldade: 'EASY' },
    { nome: 'Correr', icone: '🏃', atributo: 'DESTREZA', dificuldade: 'MEDIUM' },
    { nome: 'Networking', icone: '🤝', atributo: 'CARISMA', dificuldade: 'HARD' },
    { nome: 'Dormir Cedo', icone: '💤', atributo: 'CONSTITUICAO', dificuldade: 'MEDIUM' },
    { nome: 'Cozinhar', icone: '🍳', atributo: 'DESTREZA', dificuldade: 'EASY' }
  ];

  constructor(private gameService: GameService) {}

  ngOnInit(): void {
    this.carregar();
  }

  carregar() {
    this.gameService.getProtocolos().subscribe(dados => this.protocolos = dados);
  }

  // Função para carregar um modelo pronto
  usarPreset(preset: any) {
    this.novaMissao = { ...preset, descricao: 'Hábito predefinido' };
  }

  salvar() {
    if (!this.novaMissao.nome) return;
    this.gameService.criarProtocolo(this.novaMissao).subscribe(() => {
      this.carregar();
      // Resetar para um padrão limpo
      this.novaMissao = { nome: '', icone: '✨', descricao: '', atributo: 'FORCA', dificuldade: 'EASY' };
    });
  }

  deletar(id: number) {
    if(confirm('Rasgar este contrato de missão?')) {
      this.gameService.deletarProtocolo(id).subscribe(() => this.carregar());
    }
  }

  // Auxiliar para cores (Visual apenas)
  getCorAtributo(attr: string): string {
    switch(attr) {
      case 'FORCA': return 'text-orange-500 border-orange-500/50 bg-orange-500/10';
      case 'DESTREZA': return 'text-green-500 border-green-500/50 bg-green-500/10';
      case 'INTELECTO': return 'text-blue-500 border-blue-500/50 bg-blue-500/10';
      case 'CARISMA': return 'text-purple-500 border-purple-500/50 bg-purple-500/10';
      case 'CONSTITUICAO': return 'text-red-500 border-red-500/50 bg-red-500/10';
      default: return 'text-gray-500';
    }
  }
}
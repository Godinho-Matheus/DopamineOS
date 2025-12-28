import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html'
})
export class SidebarComponent {

  constructor(private router: Router) {}

  menuItems = [
    {
      label: 'Visão Geral',
      path: '/dashboard',
      icon: '🏰',
      exact: true
    },
    {
      label: 'Grimório de Missões',
      path: '/game/quest-editor',
      icon: '📜',
      exact: false
    },
    { label: 'Histórico',
      path: '/history',
      icon: '⏳',
      exact: true
    }
    // Futuras expansões:
    // { label: 'Loja & Inventário', path: '/shop', icon: '💰' },
    // { label: 'Conquistas', path: '/achievements', icon: '🏆' },
  ];

  logout() {
    if(confirm('Deseja realmente deslogar do sistema?')) {
      // Aqui limparíamos o token JWT / localStorage
      // localStorage.clear();
      this.router.navigate(['/setup']);
    }
  }
}
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router'; // Importar isso!

@Component({
  selector: 'app-sidebar',
  standalone: true,
  // Adicione RouterLink e RouterLinkActive aqui
  imports: [CommonModule, RouterLink, RouterLinkActive], 
  templateUrl: './sidebar.html'
})
export class SidebarComponent {}
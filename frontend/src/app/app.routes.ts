import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'setup',
        pathMatch: 'full'
    },
    {
        path: 'setup',
        loadComponent: () => import('./components/setup/setup').then(m => m.SetupComponent),
        title: 'LumeOS - Setup'
    },
    {
        path: 'dashboard',
        loadComponent: () => import('./components/dashboard/dashboard').then(m => m.DashboardComponent),
        title: 'LumeOS - Visão Geral'
    },
    {
        path: 'game/quest-editor',
        loadComponent: () => import('./components/quest-editor/quest-editor').then(m => m.QuestEditorComponent),
        title: 'LumeOS - Grimório'
    },
    {
        path: 'history',
        loadComponent: () => import('./components/history/history').then(m => m.HistoryComponent),
        title: 'LumeOS - Histórico'
    },
    {
        // Wildcard deve ser sempre a ÚLTIMA rota
        path: '**',
        redirectTo: 'dashboard'
    }
];

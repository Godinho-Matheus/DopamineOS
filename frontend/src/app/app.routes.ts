import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard';
import { SetupComponent } from './components/setup/setup'; // Importe o novo
import { QuestEditorComponent } from './components/quest-editor/quest-editor';

export const routes: Routes = [
    { path: 'setup', component: SetupComponent },
    { path: 'dashboard', component: DashboardComponent },
    { path: 'editor', component: QuestEditorComponent },
    { path: '', redirectTo: 'setup', pathMatch: 'full' }
];
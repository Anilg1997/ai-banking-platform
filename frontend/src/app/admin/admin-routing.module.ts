import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { AdminUsersComponent } from './admin-users.component';
import { AdminCardsComponent } from './admin-cards.component';
import { AdminAgentComponent } from './admin-agent.component';
import { AdminSettingsComponent } from './admin-settings.component';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: AdminDashboardComponent },
  { path: 'users', component: AdminUsersComponent },
  { path: 'cards', component: AdminCardsComponent },
  { path: 'agent', component: AdminAgentComponent },
  { path: 'settings', component: AdminSettingsComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class AdminRoutingModule {}

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AccountsComponent } from './accounts/accounts.component';
import { AccountDetailComponent } from './accounts/account-detail.component';
import { TransactionsComponent } from './transactions/transactions.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { CardsComponent } from './cards/cards.component';
import { CardDetailComponent } from './cards/card-detail.component';
import { CardApplyComponent } from './cards/card-apply.component';
import { LoansComponent } from './loans/loans.component';
import { ProfileComponent } from './profile/profile.component';
import { AuthGuard } from './core/auth/auth.guard';

const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
  { path: 'accounts', component: AccountsComponent, canActivate: [AuthGuard] },
  { path: 'accounts/:id', component: AccountDetailComponent, canActivate: [AuthGuard] },
  { path: 'transactions', component: TransactionsComponent, canActivate: [AuthGuard] },
  { path: 'notifications', component: NotificationsComponent, canActivate: [AuthGuard] },
  { path: 'cards', component: CardsComponent, canActivate: [AuthGuard] },
  { path: 'cards/apply', component: CardApplyComponent, canActivate: [AuthGuard] },
  { path: 'cards/:id', component: CardDetailComponent, canActivate: [AuthGuard] },
  { path: 'loans', component: LoansComponent, canActivate: [AuthGuard] },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'admin', loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule) },
  { path: '**', redirectTo: '/login' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}

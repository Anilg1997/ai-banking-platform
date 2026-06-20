import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AdminService, AdminStats, AgentStats } from '../core/services/admin.service';

@Component({
  standalone: false,
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  stats: AdminStats = { totalUsers: 0, activeUsers: 0, totalCards: 0, creditLimitUsage: 0, agentConversations: 0 };
  agentStats: AgentStats = { totalConversations: 0, activeConversations: 0, actionsToday: 0 };
  recentActivity: { type: string; message: string; time: string }[] = [];
  recentApplications: { applicant: string; cardType: string; amount: number; status: string }[] = [];
  recentActions: { action: string; admin: string; target: string; timestamp: string }[] = [];
  isLoading = false;

  private destroy$ = new Subject<void>();

  constructor(private adminService: AdminService, private router: Router) {}

  ngOnInit(): void {
    this.loadData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadData(): void {
    this.isLoading = true;
    forkJoin({
      userStats: this.adminService.getUserStats(),
      cardStats: this.adminService.getCardStats(),
      agentStats: this.adminService.getAgentStats(),
      activity: this.adminService.getRecentActivity(),
      actions: this.adminService.getAgentActions(),
    })
      .pipe(takeUntil(this.destroy$), finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (result) => {
          this.stats = {
            totalUsers: result.userStats.totalUsers,
            activeUsers: result.userStats.activeUsers,
            totalCards: result.cardStats.totalCards,
            creditLimitUsage: result.cardStats.creditLimitUsage,
            agentConversations: result.agentStats.totalConversations,
          };
          this.agentStats = result.agentStats;
          this.recentActivity = result.activity;
          this.recentActions = result.actions;
          this.recentApplications = [
            { applicant: 'Alex Turner', cardType: 'VISA Platinum', amount: 85000, status: 'Pending' },
            { applicant: 'Maria Garcia', cardType: 'Mastercard Gold', amount: 62000, status: 'Pending' },
            { applicant: 'James Lee', cardType: 'AMEX Platinum', amount: 120000, status: 'Pending' },
            { applicant: 'Amanda White', cardType: 'VISA Signature', amount: 95000, status: 'Pending' },
            { applicant: 'Kevin Park', cardType: 'Mastercard Standard', amount: 45000, status: 'Pending' },
          ];
        },
      });
  }

  formatCurrency(amount: number, currency: string = 'USD'): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);
  }

  formatRelativeTime(dateStr: string): string {
    return dateStr;
  }

  getActivityIcon(type: string): string {
    switch (type) {
      case 'user': return 'person_add';
      case 'card': return 'credit_card';
      case 'transaction': return 'receipt_long';
      case 'agent': return 'smart_toy';
      default: return 'circle';
    }
  }

  getActivityColor(type: string): string {
    switch (type) {
      case 'user': return 'user';
      case 'card': return 'card';
      case 'transaction': return 'transaction';
      case 'agent': return 'agent';
      default: return '';
    }
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }
}

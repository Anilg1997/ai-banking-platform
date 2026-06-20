import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AdminService, AgentConversation, AgentStats, KnowledgeDoc } from '../core/services/admin.service';

@Component({
  standalone: false,
  selector: 'app-admin-agent',
  templateUrl: './admin-agent.component.html',
  styleUrls: ['./admin-agent.component.scss'],
})
export class AdminAgentComponent implements OnInit, OnDestroy {
  conversations: AgentConversation[] = [];
  knowledgeDocs: KnowledgeDoc[] = [];
  agentStats: AgentStats = { totalConversations: 0, activeConversations: 0, actionsToday: 0 };
  isLoading = false;
  expandedConvId: string | null = null;

  private destroy$ = new Subject<void>();

  agentTypeDistribution = [
    { type: 'Customer Support', count: 1420, color: '#60a5fa' },
    { type: 'Fraud Detection', count: 890, color: '#f87171' },
    { type: 'Financial Advisor', count: 650, color: '#4ade80' },
    { type: 'Loan Advisor', count: 490, color: '#a78bfa' },
  ];

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadData(): void {
    this.isLoading = true;
    this.adminService.getAgentStats()
      .pipe(takeUntil(this.destroy$), finalize(() => (this.isLoading = false)))
      .subscribe({ next: (stats) => { this.agentStats = stats; } });

    this.adminService.getConversations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (convs) => { this.conversations = convs; } });

    this.adminService.getKnowledgeDocs()
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (docs) => { this.knowledgeDocs = docs; } });
  }

  toggleExpand(convId: string): void {
    this.expandedConvId = this.expandedConvId === convId ? null : convId;
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Active': return 'status-active';
      case 'Resolved': return 'status-resolved';
      case 'Pending': return 'status-pending';
      default: return '';
    }
  }

  getAgentTypeIcon(type: string): string {
    switch (type) {
      case 'Customer Support': return 'support_agent';
      case 'Fraud Detection': return 'security';
      case 'Financial Advisor': return 'account_balance';
      case 'Loan Advisor': return 'payments';
      default: return 'smart_toy';
    }
  }

  getMaxAgentCount(): number {
    return Math.max(...this.agentTypeDistribution.map(a => a.count), 1);
  }

  refreshData(): void {
    this.loadData();
  }
}

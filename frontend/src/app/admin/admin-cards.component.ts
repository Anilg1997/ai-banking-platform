import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AdminService, CardSummary, CardApplication } from '../core/services/admin.service';

@Component({
  standalone: false,
  selector: 'app-admin-cards',
  templateUrl: './admin-cards.component.html',
  styleUrls: ['./admin-cards.component.scss'],
})
export class AdminCardsComponent implements OnInit, OnDestroy {
  cards: CardSummary[] = [];
  applications: CardApplication[] = [];
  isLoading = false;
  selectedTab = 0;

  totalCards = 0;
  activeCards = 0;
  frozenCards = 0;
  pendingApps = 0;

  selectedApplication: CardApplication | null = null;
  showAppDetail = false;

  private destroy$ = new Subject<void>();

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
    this.adminService.getCardStats()
      .pipe(takeUntil(this.destroy$), finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (stats) => {
          this.totalCards = stats.totalCards;
          this.activeCards = stats.activeCards;
          this.frozenCards = stats.frozenCards;
          this.pendingApps = stats.pendingApplications;
        },
      });

    this.adminService.getCards()
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (cards) => { this.cards = cards; } });

    this.adminService.getCardApplications()
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: (apps) => { this.applications = apps; } });
  }

  approveApplication(id: string): void {
    this.adminService.approveApplication(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const app = this.applications.find(a => a.id === id);
          if (app) app.status = 'APPROVED';
          if (this.selectedApplication?.id === id) {
            this.selectedApplication.status = 'APPROVED';
          }
        },
      });
  }

  rejectApplication(id: string): void {
    const reason = prompt('Enter rejection reason:');
    if (!reason || reason.trim() === '') return;
    this.adminService.rejectApplication(id, reason)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const app = this.applications.find(a => a.id === id);
          if (app) app.status = 'REJECTED';
          if (this.selectedApplication?.id === id) {
            this.selectedApplication.status = 'REJECTED';
          }
        },
      });
  }

  updateCardStatus(id: string, status: string): void {
    this.adminService.updateCardStatus(id, status)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const card = this.cards.find(c => c.id === id);
          if (card) card.status = status as CardSummary['status'];
        },
      });
  }

  viewCardDetails(id: string): void {
    const card = this.cards.find(c => c.id === id);
    if (card) alert(`Card Details:\nHolder: ${card.cardHolderName}\nNumber: ${card.cardNumber}\nType: ${card.cardType}\nLimit: $${card.creditLimit.toLocaleString()}\nAvailable: $${card.availableCredit.toLocaleString()}\nExpiry: ${card.expiryDate}`);
  }

  viewApplicationDetail(app: CardApplication): void {
    this.selectedApplication = app;
    this.showAppDetail = true;
  }

  closeAppDetail(): void {
    this.showAppDetail = false;
    this.selectedApplication = null;
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'status-active';
      case 'FROZEN': return 'status-frozen';
      case 'CANCELLED': return 'status-cancelled';
      case 'PENDING': return 'status-pending';
      case 'APPROVED': return 'status-approved';
      case 'REJECTED': return 'status-rejected';
      default: return '';
    }
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
  }
}

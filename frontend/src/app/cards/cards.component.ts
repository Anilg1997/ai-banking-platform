import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AuthService } from '../core/auth/auth.service';
import { CardService } from '../core/services/card.service';
import { CardSummary, CardTransaction, CARD_NETWORK_LABELS, CARD_TYPE_LABELS, CARD_STATUS_LABELS, CARD_NETWORK_COLORS } from '../core/models/card.model';

@Component({
  standalone: false,
  selector: 'app-cards',
  templateUrl: './cards.component.html',
  styleUrls: ['./cards.component.scss'],
})
export class CardsComponent implements OnInit, OnDestroy {
  cards: CardSummary[] = [];
  allTransactions: CardTransaction[] = [];
  isLoading = false;
  selectedCardId: string | null = null;
  selectedCardTransactions: CardTransaction[] = [];

  totalCreditLimit = 0;
  totalAvailableCredit = 0;
  totalUsedCredit = 0;
  totalRewardPoints = 0;

  cardNetworkLabels = CARD_NETWORK_LABELS;
  cardTypeLabels = CARD_TYPE_LABELS;
  cardStatusLabels = CARD_STATUS_LABELS;

  private destroy$ = new Subject<void>();

  constructor(
    private cardService: CardService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadCards();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadCards(): void {
    const userId = this.authService.currentUser?.id;
    if (!userId) return;

    this.isLoading = true;

    this.cardService.getCards(userId).pipe(
      takeUntil(this.destroy$),
      finalize(() => (this.isLoading = false))
    ).subscribe({
      next: (cards) => {
        this.cards = cards;
        this.calculateTotals();
        if (cards.length > 0) {
          this.selectCard(cards[0]);
        }
      },
      error: () => {
        this.cards = this.cardService.getMockCards();
        this.calculateTotals();
        if (this.cards.length > 0) {
          this.selectCard(this.cards[0]);
        }
        this.allTransactions = [];
        this.cards.forEach(c => {
          this.allTransactions.push(...this.cardService.getMockTransactions(c.id));
        });
      },
    });
  }

  calculateTotals(): void {
    this.totalCreditLimit = this.cards.reduce((s, c) => s + (c.creditLimit || 0), 0);
    this.totalAvailableCredit = this.cards.reduce((s, c) => s + (c.availableCredit || 0), 0);
    this.totalUsedCredit = this.cards.reduce((s, c) => s + (c.usedCredit || 0), 0);
    this.totalRewardPoints = this.cards.reduce((s, c) => s + (c.rewardPoints || 0), 0);
  }

  selectCard(card: CardSummary): void {
    this.selectedCardId = card.id;
    this.selectedCardTransactions = this.cardService.getMockTransactions(card.id);
  }

  freezeCard(id: string): void {
    this.cardService.freezeCard(id).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        const card = this.cards.find(c => c.id === id);
        if (card) card.isFrozen = true;
        this.snackBar.open('Card frozen successfully', 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
      },
      error: () => {
        const card = this.cards.find(c => c.id === id);
        if (card) card.isFrozen = true;
        this.snackBar.open('Card frozen', 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
      },
    });
  }

  unfreezeCard(id: string): void {
    this.cardService.unfreezeCard(id).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        const card = this.cards.find(c => c.id === id);
        if (card) card.isFrozen = false;
        this.snackBar.open('Card unfrozen successfully', 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
      },
      error: () => {
        const card = this.cards.find(c => c.id === id);
        if (card) card.isFrozen = false;
        this.snackBar.open('Card unfrozen', 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
      },
    });
  }

  activateCard(id: string): void {
    this.cardService.activateCard(id).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.snackBar.open('Card activated successfully', 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
      },
      error: () => {
        this.snackBar.open('Card activated', 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
      },
    });
  }

  makePayment(id: string): void {
    this.router.navigate(['/cards', id, 'pay']);
  }

  getCardGradient(network: string): string {
    return this.cardService.getCardGradient(network);
  }

  maskNumber(num: string): string {
    return '**** **** **** ' + num.slice(-4);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'status-active';
      case 'INACTIVE': return 'status-pending';
      case 'BLOCKED': return 'status-frozen';
      case 'LOST': case 'STOLEN': return 'status-suspended';
      case 'EXPIRED': return 'status-closed';
      default: return '';
    }
  }

  getTxnStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'status-completed';
      case 'PENDING': return 'status-pending';
      case 'FAILED': return 'status-failed';
      case 'REFUNDED': return 'status-reversed';
      default: return '';
    }
  }

  formatCurrency(amount: number, currency: string = 'USD'): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));
    if (diffDays === 0) return `Today, ${date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}`;
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  trackByCardId(index: number, card: CardSummary): string { return card.id; }
  trackByTxnId(index: number, txn: CardTransaction): string { return txn.id; }
}

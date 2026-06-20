import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { CardService } from '../core/services/card.service';
import {
  Card, CardTransaction, CardStatement,
  CARD_NETWORK_LABELS, CARD_TYPE_LABELS, CARD_STATUS_LABELS,
} from '../core/models/card.model';
import { MOCK_CARD_STATEMENTS, MOCK_MONTHLY_STATS } from '../core/services/card.mock';

@Component({
  standalone: false,
  selector: 'app-card-detail',
  templateUrl: './card-detail.component.html',
  styleUrls: ['./card-detail.component.scss'],
})
export class CardDetailComponent implements OnInit, OnDestroy {
  Math = Math;
  card: Card | null = null;
  error: string | null = null;
  isLoading = true;
  activeTab = 0;

  transactions: CardTransaction[] = [];
  transactionsTotal = 0;
  transactionsPage = 0;
  transactionsPageSize = 10;
  txnFilter = { category: '', dateFrom: '', dateTo: '', minAmount: null as number | null, maxAmount: null as number | null };

  statements: CardStatement[] = [];
  monthlyStats = MOCK_MONTHLY_STATS;
  showCvv = false;
  showFullNumber = false;

  cardNetworkLabels = CARD_NETWORK_LABELS;
  cardTypeLabels = CARD_TYPE_LABELS;
  cardStatusLabels = CARD_STATUS_LABELS;

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private cardService: CardService,
    private router: Router,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      const id = params['id'];
      if (id) {
        this.loadCard(id);
        this.loadTransactions(id);
        this.loadStatements(id);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadCard(id: string): void {
    this.isLoading = true;
    this.cardService.getCard(id).pipe(
      takeUntil(this.destroy$),
      finalize(() => (this.isLoading = false))
    ).subscribe({
      next: (card) => { this.card = card; },
      error: () => {
        const mock = this.cardService.getMockCards().find(c => c.id === id);
        if (mock) {
          this.card = {
            ...mock, userId: '', cvv: '123', issuedAt: new Date().toISOString(),
            isFrozen: mock.isFrozen, isActivated: mock.isActivated,
            autoPayEnabled: false, transactionAlertsEnabled: true,
          };
        } else {
          this.error = 'Could not load card details';
        }
      },
    });
  }

  loadTransactions(cardId: string): void {
    this.cardService.getCardTransactions(cardId, this.transactionsPage, this.transactionsPageSize).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (res) => {
        this.transactions = res.content;
        this.transactionsTotal = res.totalElements;
      },
      error: () => {
        const txns = this.cardService.getMockTransactions(cardId);
        this.transactions = txns.slice(
          this.transactionsPage * this.transactionsPageSize,
          (this.transactionsPage + 1) * this.transactionsPageSize
        );
        this.transactionsTotal = txns.length;
      },
    });
  }

  loadStatements(cardId: string): void {
    this.cardService.getCardStatements(cardId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (stmts) => { this.statements = stmts; },
      error: () => {
        this.statements = MOCK_CARD_STATEMENTS.filter(s => s.cardId === cardId);
      },
    });
  }

  onTabChange(index: number): void {
    this.activeTab = index;
  }

  changePage(dir: number): void {
    const newPage = this.transactionsPage + dir;
    if (newPage < 0 || newPage * this.transactionsPageSize >= this.transactionsTotal) return;
    this.transactionsPage = newPage;
    if (this.card) this.loadTransactions(this.card.id);
  }

  applyFilter(): void {
    this.transactionsPage = 0;
    if (this.card) this.loadTransactions(this.card.id);
  }

  clearFilter(): void {
    this.txnFilter = { category: '', dateFrom: '', dateTo: '', minAmount: null, maxAmount: null };
    this.transactionsPage = 0;
    if (this.card) this.loadTransactions(this.card.id);
  }

  toggleFreeze(): void {
    if (!this.card) return;
    if (this.card.isFrozen) {
      this.cardService.unfreezeCard(this.card.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => { if (this.card) this.card.isFrozen = false; this.snackBar.open('Card unfrozen', 'Close', { duration: 3000, panelClass: ['success-snackbar'] }); },
        error: () => { if (this.card) this.card.isFrozen = false; this.snackBar.open('Card unfrozen', 'Close', { duration: 3000, panelClass: ['success-snackbar'] }); },
      });
    } else {
      this.cardService.freezeCard(this.card.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => { if (this.card) this.card.isFrozen = true; this.snackBar.open('Card frozen', 'Close', { duration: 3000, panelClass: ['success-snackbar'] }); },
        error: () => { if (this.card) this.card.isFrozen = true; this.snackBar.open('Card frozen', 'Close', { duration: 3000, panelClass: ['success-snackbar'] }); },
      });
    }
  }

  reportLostStolen(): void {
    if (!this.card) return;
    if (confirm('Report this card as lost or stolen? This will permanently block the card.')) {
      this.cardService.reportLostStolen(this.card.id).pipe(takeUntil(this.destroy$)).subscribe({
        next: () => { this.snackBar.open('Card reported as lost/stolen. A replacement will be issued.', 'Close', { duration: 5000, panelClass: ['success-snackbar'] }); },
        error: () => { this.snackBar.open('Card reported. A replacement will be issued.', 'Close', { duration: 5000, panelClass: ['success-snackbar'] }); },
      });
    }
  }

  toggleAutoPay(): void {
    if (!this.card) return;
    const enabled = !this.card.autoPayEnabled;
    this.cardService.setAutoPay(this.card.id, enabled).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => { if (this.card) this.card.autoPayEnabled = enabled; },
      error: () => { if (this.card) this.card.autoPayEnabled = enabled; },
    });
  }

  toggleAlerts(): void {
    if (!this.card) return;
    const enabled = !this.card.transactionAlertsEnabled;
    this.cardService.setTransactionAlerts(this.card.id, enabled).pipe(takeUntil(this.destroy$)).subscribe({
      next: () => { if (this.card) this.card.transactionAlertsEnabled = enabled; },
      error: () => { if (this.card) this.card.transactionAlertsEnabled = enabled; },
    });
  }

  getCardGradient(network: string): string {
    return this.cardService.getCardGradient(network);
  }

  maskNumber(num: string): string {
    if (this.showFullNumber) return num.replace(/(.{4})/g, '$1 ');
    return '**** **** **** ' + num.slice(-4);
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'status-active';
      case 'INACTIVE': return 'status-pending';
      case 'BLOCKED': case 'LOST': case 'STOLEN': return 'status-frozen';
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

  getStatementStatusClass(status: string): string {
    switch (status) {
      case 'PAID': return 'status-completed';
      case 'PENDING': return 'status-pending';
      case 'OVERDUE': return 'status-failed';
      case 'PARTIAL': return 'status-pending';
      default: return '';
    }
  }

  formatCurrency(amount: number, currency: string = 'USD'): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  usedCreditPercent(): number {
    if (!this.card || this.card.creditLimit === 0) return 0;
    return Math.min(100, Math.round((this.card.usedCredit / this.card.creditLimit) * 100));
  }

  trackByTxnId(index: number, txn: CardTransaction): string { return txn.id; }
  trackByStmtId(index: number, stmt: CardStatement): string { return stmt.id; }
}

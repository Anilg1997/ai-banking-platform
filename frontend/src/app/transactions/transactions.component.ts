import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AuthService } from '../core/auth/auth.service';
import { AccountService } from '../core/services/account.service';
import { TransactionService } from '../core/services/transaction.service';
import {
  TransactionSummary,
  TRANSACTION_TYPE_LABELS,
  TRANSACTION_TYPE_ICONS,
  TRANSACTION_STATUS_LABELS,
} from '../core/models/transaction.model';
import { AccountSummary } from '../core/models/account.model';

@Component({
  standalone: false,
  selector: 'app-transactions',
  templateUrl: './transactions.component.html',
  styleUrls: ['./transactions.component.scss'],
})
export class TransactionsComponent implements OnInit, OnDestroy {
  transactions: TransactionSummary[] = [];
  accounts: AccountSummary[] = [];
  isLoading = false;
  totalCount = 0;
  isSending = false;
  showSendForm = false;
  selectedFilter: string = 'all';

  sendForm = {
    fromAccountId: '',
    toAccountId: '',
    amount: 0,
    currency: 'USD',
    description: '',
    category: 'OTHER',
  };

  transactionTypeLabels = TRANSACTION_TYPE_LABELS;
  transactionTypeIcons = TRANSACTION_TYPE_ICONS;
  transactionStatusLabels = TRANSACTION_STATUS_LABELS;

  categories = ['FOOD', 'TRANSPORT', 'SHOPPING', 'BILLS', 'ENTERTAINMENT', 'SALARY', 'SAVINGS', 'INVESTMENT', 'RENT', 'OTHER'];

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadData();
    this.connectSSE();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.transactionService.disconnectFromRealtimeUpdates();
  }

  loadData(): void {
    const userId = this.authService.currentUser?.id;
    if (!userId) return;

    this.isLoading = true;

    forkJoin({
      transactions: this.transactionService.getUserTransactions(userId),
      accounts: this.accountService.getActiveAccounts(userId),
      count: this.transactionService.getTransactionCount(userId),
    })
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false))
      )
      .subscribe({
        next: (result) => {
          this.transactions = result.transactions;
          this.accounts = result.accounts;
          this.totalCount = result.count.count;
        },
      });
  }

  connectSSE(): void {
    const userId = this.authService.currentUser?.id;
    if (userId) {
      this.transactionService.connectToRealtimeUpdates(userId);
      this.transactionService.realTimeUpdates$
        .pipe(takeUntil(this.destroy$))
        .subscribe((txn) => {
          this.transactions.unshift(txn);
          if (this.transactions.length > 100) {
            this.transactions = this.transactions.slice(0, 100);
          }
        });
    }
  }

  toggleSendForm(): void {
    this.showSendForm = !this.showSendForm;
    if (!this.showSendForm) {
      this.resetSendForm();
    } else if (this.accounts.length > 0) {
      this.sendForm.fromAccountId = this.accounts[0].id;
    }
  }

  sendMoney(): void {
    if (!this.sendForm.fromAccountId || !this.sendForm.toAccountId || this.sendForm.amount <= 0) {
      return;
    }

    this.isSending = true;
    this.transactionService
      .transfer({
        fromAccountId: this.sendForm.fromAccountId,
        toAccountId: this.sendForm.toAccountId,
        amount: this.sendForm.amount,
        currency: this.sendForm.currency,
        description: this.sendForm.description,
        category: this.sendForm.category,
      })
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isSending = false))
      )
      .subscribe({
        next: () => {
          this.showSendForm = false;
          this.resetSendForm();
          this.loadData();
        },
        error: (err) => {
          console.error('Transfer failed:', err);
        },
      });
  }

  get filteredTransactions(): TransactionSummary[] {
    if (this.selectedFilter === 'all') return this.transactions;
    return this.transactions.filter(
      (t) => t.type === this.selectedFilter.toUpperCase()
    );
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'COMPLETED': return 'status-completed';
      case 'PENDING': return 'status-pending';
      case 'PROCESSING': return 'status-processing';
      case 'FAILED': return 'status-failed';
      case 'REVERSED': return 'status-reversed';
      case 'CANCELLED': return 'status-cancelled';
      default: return '';
    }
  }

  getDirection(txn: TransactionSummary): 'sent' | 'received' {
    const userId = this.authService.currentUser?.id;
    return txn.fromUserId === userId ? 'sent' : 'received';
  }

  formatCurrency(amount: number, currency: string = 'USD'): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) {
      return `Today, ${date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}`;
    } else if (diffDays === 1) {
      return 'Yesterday';
    } else if (diffDays < 7) {
      return `${diffDays} days ago`;
    }
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  }

  private resetSendForm(): void {
    this.sendForm = {
      fromAccountId: this.accounts[0]?.id || '',
      toAccountId: '',
      amount: 0,
      currency: 'USD',
      description: '',
      category: 'OTHER',
    };
  }
}

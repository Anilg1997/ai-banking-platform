import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AuthService } from '../core/auth/auth.service';
import { AccountService } from '../core/services/account.service';
import { AccountSummary, ACCOUNT_TYPE_LABELS, ACCOUNT_TYPE_ICONS, ACCOUNT_STATUS_LABELS } from '../core/models/account.model';

@Component({
  selector: 'app-accounts',
  templateUrl: './accounts.component.html',
  styleUrls: ['./accounts.component.scss'],
})
export class AccountsComponent implements OnInit, OnDestroy {
  accounts: AccountSummary[] = [];
  isLoading = false;
  totalBalance = 0;
  totalCount = 0;
  showCreateForm = false;
  newAccount = {
    accountName: '',
    accountType: 'CHECKING' as any,
    currency: 'USD',
    initialDeposit: 0,
  };
  isCreating = false;

  accountTypeLabels = ACCOUNT_TYPE_LABELS;
  accountTypeIcons = ACCOUNT_TYPE_ICONS;
  accountStatusLabels = ACCOUNT_STATUS_LABELS;
  accountTypes = Object.keys(ACCOUNT_TYPE_LABELS) as any[];

  private destroy$ = new Subject<void>();

  constructor(
    private accountService: AccountService,
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
    this.loadAccounts();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAccounts(): void {
    const userId = this.authService.currentUser?.id;
    if (!userId) return;

    this.isLoading = true;

    forkJoin({
      accounts: this.accountService.getAccounts(userId),
      balance: this.accountService.getTotalBalance(userId),
    })
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false))
      )
      .subscribe({
        next: (result) => {
          this.accounts = result.accounts;
          this.totalBalance = result.balance.totalBalance;
          this.totalCount = result.balance.accountCount;
        },
        error: () => {
          this.snackBar.open('Failed to load accounts', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar'],
          });
        },
      });
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    if (!this.showCreateForm) {
      this.resetForm();
    }
  }

  createAccount(): void {
    const userId = this.authService.currentUser?.id;
    if (!userId || !this.newAccount.accountName) return;

    this.isCreating = true;
    this.accountService
      .createAccount({
        userId,
        accountName: this.newAccount.accountName,
        accountType: this.newAccount.accountType,
        currency: this.newAccount.currency,
        initialDeposit: this.newAccount.initialDeposit,
      })
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isCreating = false))
      )
      .subscribe({
        next: () => {
          this.snackBar.open('Account created successfully!', 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar'],
          });
          this.showCreateForm = false;
          this.resetForm();
          this.loadAccounts();
        },
        error: (err) => {
          this.snackBar.open(err.error?.message || 'Failed to create account', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar'],
          });
        },
      });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'status-active';
      case 'PENDING': return 'status-pending';
      case 'FROZEN': return 'status-frozen';
      case 'CLOSED': return 'status-closed';
      case 'SUSPENDED': return 'status-suspended';
      default: return '';
    }
  }

  formatCurrency(amount: number, currency: string = 'USD'): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  }

  private resetForm(): void {
    this.newAccount = {
      accountName: '',
      accountType: 'CHECKING',
      currency: 'USD',
      initialDeposit: 0,
    };
  }
}

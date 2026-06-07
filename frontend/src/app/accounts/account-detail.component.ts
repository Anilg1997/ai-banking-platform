import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AccountService } from '../core/services/account.service';
import { Account, ACCOUNT_TYPE_LABELS, ACCOUNT_STATUS_LABELS } from '../core/models/account.model';

@Component({
  standalone: false,
  selector: 'app-account-detail',
  templateUrl: './account-detail.component.html',
  styleUrls: ['./account-detail.component.scss'],
})
export class AccountDetailComponent implements OnInit, OnDestroy {
  account: Account | null = null;
  error: string | null = null;
  isLoading = true;

  accountTypeLabels = ACCOUNT_TYPE_LABELS;
  accountStatusLabels = ACCOUNT_STATUS_LABELS;

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private accountService: AccountService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe((params) => {
      const id = params['id'];
      if (id) {
        this.loadAccount(id);
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAccount(id: string): void {
    this.isLoading = true;
    this.error = null;

    this.accountService
      .getAccount(id)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false))
      )
      .subscribe({
        next: (account) => {
          this.account = account;
        },
        error: (err) => {
          this.error = err.error?.message || 'Could not load account details';
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
}

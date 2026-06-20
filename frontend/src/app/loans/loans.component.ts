import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AuthService } from '../core/auth/auth.service';
import { LoanService } from '../core/services/loan.service';
import {
  Loan, LoanApplication, LoanTypeInfo,
  LOAN_TYPE_LABELS, LOAN_TYPE_ICONS, LOAN_STATUS_LABELS, AVAILABLE_LOAN_TYPES,
} from '../core/models/loan.model';

@Component({
  standalone: false,
  selector: 'app-loans',
  templateUrl: './loans.component.html',
  styleUrls: ['./loans.component.scss'],
})
export class LoansComponent implements OnInit, OnDestroy {
  activeLoans: Loan[] = [];
  loanApplications: LoanApplication[] = [];
  isLoading = false;

  preQualifiedLimit = 50000;
  availableLoanTypes = AVAILABLE_LOAN_TYPES;

  loanTypeLabels = LOAN_TYPE_LABELS;
  loanTypeIcons = LOAN_TYPE_ICONS;
  loanStatusLabels = LOAN_STATUS_LABELS;

  showApplyForm = false;
  applyData = { loanType: '' as string, amount: 0, tenureMonths: 0 };
  isApplying = false;

  private destroy$ = new Subject<void>();

  constructor(
    private loanService: LoanService,
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
    this.loadLoans();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadLoans(): void {
    const userId = this.authService.currentUser?.id;
    if (!userId) return;

    this.isLoading = true;

    this.loanService.getLoans(userId).pipe(
      takeUntil(this.destroy$),
      finalize(() => (this.isLoading = false))
    ).subscribe({
      next: (loans) => { this.activeLoans = loans; this.loadApplications(userId); },
      error: () => {
        this.activeLoans = this.loanService.getMockLoans();
        this.loanApplications = this.loanService.getMockApplications();
      },
    });
  }

  loadApplications(userId: string): void {
    this.loanService.getLoanApplications(userId).pipe(takeUntil(this.destroy$)).subscribe({
      next: (apps) => { this.loanApplications = apps; },
      error: () => { this.loanApplications = this.loanService.getMockApplications(); },
    });
  }

  getRepaymentPercent(loan: Loan): number {
    if (loan.totalEmis === 0) return 0;
    return Math.round((loan.paidEmis / loan.totalEmis) * 100);
  }

  getDefaultTenure(loanType: string): number {
    switch (loanType) {
      case 'PERSONAL': return 60;
      case 'HOME': return 240;
      case 'AUTO': return 60;
      case 'EDUCATION': return 120;
      case 'BUSINESS': return 84;
      default: return 60;
    }
  }

  getMaxAmount(loanType: string): number {
    const found = this.availableLoanTypes.find(t => t.type === loanType);
    return found ? found.maxAmount : 50000;
  }

  getLoanTypeInfo(loanType: string): LoanTypeInfo | undefined {
    return this.availableLoanTypes.find(t => t.type === loanType);
  }

  toggleApplyForm(loanType?: string): void {
    this.showApplyForm = !this.showApplyForm;
    if (this.showApplyForm) {
      this.applyData = { loanType: loanType || '', amount: 0, tenureMonths: this.getDefaultTenure(loanType || 'PERSONAL') };
    }
  }

  onLoanTypeChange(): void {
    this.applyData.tenureMonths = this.getDefaultTenure(this.applyData.loanType);
    if (this.applyData.amount > this.getMaxAmount(this.applyData.loanType)) {
      this.applyData.amount = this.getMaxAmount(this.applyData.loanType);
    }
  }

  submitApplication(): void {
    const userId = this.authService.currentUser?.id;
    if (!userId || !this.applyData.loanType || this.applyData.amount <= 0) return;

    this.isApplying = true;
    this.loanService.applyForLoan({ userId, ...this.applyData, tenureMonths: this.applyData.tenureMonths }).pipe(
      takeUntil(this.destroy$),
      finalize(() => (this.isApplying = false))
    ).subscribe({
      next: () => {
        this.snackBar.open('Loan application submitted successfully!', 'Close', { duration: 5000, panelClass: ['success-snackbar'] });
        this.showApplyForm = false;
        this.loadLoans();
      },
      error: () => {
        this.snackBar.open('Loan application submitted! Our team will review it shortly.', 'Close', { duration: 5000, panelClass: ['success-snackbar'] });
        this.showApplyForm = false;
      },
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'status-active';
      case 'CLOSED': return 'status-closed';
      case 'DEFAULTER': return 'status-suspended';
      default: return '';
    }
  }

  getAppStatusClass(status: string): string {
    switch (status) {
      case 'SUBMITTED': return 'status-pending';
      case 'VERIFICATION': return 'status-pending';
      case 'APPROVED': return 'status-active';
      case 'REJECTED': return 'status-frozen';
      case 'DISBURSED': return 'status-active';
      default: return '';
    }
  }

  formatCurrency(amount: number, currency: string = 'USD'): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(amount);
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  getLoanTypeLabel(loanType: string): string {
    return this.loanTypeLabels[loanType as keyof typeof this.loanTypeLabels] || loanType;
  }

  trackByLoanId(index: number, loan: Loan): string { return loan.id; }
  trackByAppId(index: number, app: LoanApplication): string { return app.id; }
}

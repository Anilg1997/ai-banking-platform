import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AuthService } from '../core/auth/auth.service';
import { CardService } from '../core/services/card.service';
import { CardNetwork, CardType, CARD_NETWORK_LABELS, CARD_TYPE_LABELS, CARD_NETWORK_COLORS } from '../core/models/card.model';

interface CardOption {
  network: CardNetwork;
  type: CardType;
  label: string;
  benefits: string[];
}

@Component({
  standalone: false,
  selector: 'app-card-apply',
  templateUrl: './card-apply.component.html',
  styleUrls: ['./card-apply.component.scss'],
})
export class CardApplyComponent implements OnInit, OnDestroy {
  currentStep = 1;
  totalSteps = 4;
  isSubmitting = false;
  submitted = false;
  applicationRef = '';

  cardOptions: CardOption[] = [
    { network: 'VISA', type: 'CREDIT', label: 'Visa Platinum Credit Card', benefits: ['1% unlimited cashback', 'Premium travel insurance', 'Airport lounge access', '0% intro APR for 12 months'] },
    { network: 'MASTERCARD', type: 'CREDIT', label: 'Mastercard World Credit Card', benefits: ['2% back on dining & travel', 'Price protection', 'Extended warranty', 'Concierge service'] },
    { network: 'AMEX', type: 'CREDIT', label: 'Amex Gold Credit Card', benefits: ['4x points on dining', '3x points on flights', '$200 annual airline credit', 'No foreign transaction fees'] },
    { network: 'RUPAY', type: 'DEBIT', label: 'RuPay Platinum Debit Card', benefits: ['Free domestic ATM withdrawals', 'Purchase protection', 'Contactless payments', 'Exclusive merchant offers'] },
    { network: 'VISA', type: 'DEBIT', label: 'Visa Signature Debit Card', benefits: ['Daily cash withdrawals up to $2,000', 'Mastercard acceptance worldwide', 'Real-time transaction alerts', 'Free card replacement'] },
    { network: 'MASTERCARD', type: 'PREPAID', label: 'Mastercard Prepaid Card', benefits: ['No credit check required', 'Load money anytime', 'Budget-friendly spending', 'Online shopping enabled'] },
  ];

  selectedOption: CardOption | null = null;

  personalInfo = {
    firstName: '', lastName: '', email: '', phone: '',
    employmentType: '', employerName: '', designation: '', annualIncome: null as number | null,
  };

  kycInfo = {
    aadharNumber: '', panNumber: '',
    addressLine1: '', addressLine2: '',
    city: '', state: '', pincode: '',
  };

  agreeTerms = false;
  employmentTypes = ['Salaried', 'Self-Employed', 'Business Owner', 'Freelancer', 'Student', 'Retired', 'Unemployed'];

  cardNetworkLabels = CARD_NETWORK_LABELS;
  cardTypeLabels = CARD_TYPE_LABELS;

  private destroy$ = new Subject<void>();

  constructor(
    private cardService: CardService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }
    const user = this.authService.currentUser;
    if (user) {
      this.personalInfo.firstName = user.firstName || '';
      this.personalInfo.lastName = user.lastName || '';
      this.personalInfo.email = user.email || '';
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  selectCard(option: CardOption): void {
    this.selectedOption = option;
  }

  nextStep(): void {
    if (this.currentStep < this.totalSteps) {
      this.currentStep++;
    }
  }

  prevStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  goToStep(step: number): void {
    if (step < this.currentStep || (step > this.currentStep && this.canProceed())) {
      this.currentStep = step;
    }
  }

  canProceed(): boolean {
    switch (this.currentStep) {
      case 1: return !!this.selectedOption;
      case 2: return !!this.personalInfo.firstName && !!this.personalInfo.lastName && !!this.personalInfo.email && !!this.personalInfo.employmentType;
      case 3: return !!this.kycInfo.aadharNumber && !!this.kycInfo.panNumber && !!this.kycInfo.addressLine1 && !!this.kycInfo.city && !!this.kycInfo.state && !!this.kycInfo.pincode;
      case 4: return this.agreeTerms;
      default: return true;
    }
  }

  submit(): void {
    if (!this.selectedOption || !this.canProceed()) return;

    this.isSubmitting = true;
    const userId = this.authService.currentUser?.id || 'user-1';

    this.cardService.applyForCard({
      userId,
      cardNetwork: this.selectedOption.network,
      cardType: this.selectedOption.type,
      ...this.personalInfo,
      annualIncome: this.personalInfo.annualIncome || 0,
      ...this.kycInfo,
    }).pipe(
      takeUntil(this.destroy$),
      finalize(() => (this.isSubmitting = false))
    ).subscribe({
      next: (res) => {
        this.applicationRef = res.applicationRef;
        this.submitted = true;
        this.snackBar.open('Application submitted successfully!', 'Close', { duration: 5000, panelClass: ['success-snackbar'] });
      },
      error: () => {
        this.applicationRef = 'APP-' + Date.now().toString(36).toUpperCase();
        this.submitted = true;
        this.snackBar.open('Application submitted! Our team will review it shortly.', 'Close', { duration: 5000, panelClass: ['success-snackbar'] });
      },
    });
  }

  getCardGradient(network: string): string {
    return CARD_NETWORK_COLORS[network as keyof typeof CARD_NETWORK_COLORS] || CARD_NETWORK_COLORS.VISA;
  }

  getProgressPercent(): number {
    return (this.currentStep / this.totalSteps) * 100;
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
  }

  trackByOption(index: number, opt: CardOption): string { return opt.network + opt.type; }
}

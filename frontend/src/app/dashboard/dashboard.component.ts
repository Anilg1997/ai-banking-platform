import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { AccountService } from '../core/services/account.service';
import { AiService, Insight } from '../core/services/ai.service';
import { User } from '../core/models/user.model';
import { AccountSummary, ACCOUNT_TYPE_LABELS } from '../core/models/account.model';

interface Transaction {
  name: string;
  date: string;
  amount: number;
  type: 'credit' | 'debit';
  icon: string;
}

interface Investment {
  name: string;
  shares: number;
  value: number;
  change: number;
}

interface UpcomingPayment {
  name: string;
  dueDate: string;
  amount: number;
  status: 'paid' | 'pending' | 'auto';
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  user: User | null = null;

  recentTransactions: Transaction[] = [
    { name: 'Salary Deposit', date: 'Today, 9:30 AM', amount: 8450, type: 'credit', icon: 'account_balance' },
    { name: 'Amazon Shopping', date: 'Yesterday', amount: 234.99, type: 'debit', icon: 'shopping_cart' },
    { name: 'Electric Bill', date: '2 days ago', amount: 145.50, type: 'debit', icon: 'bolt' },
    { name: 'Freelance Payment', date: '3 days ago', amount: 2200, type: 'credit', icon: 'work' },
    { name: 'Netflix Subscription', date: '5 days ago', amount: 19.99, type: 'debit', icon: 'tv' },
    { name: 'Transfer to Savings', date: '1 week ago', amount: 2000, type: 'debit', icon: 'savings' },
  ];

  investments: Investment[] = [
    { name: 'S&P 500 ETF', shares: 45, value: 28450, change: 3.2 },
    { name: 'Tech Growth Fund', shares: 120, value: 18240, change: -1.5 },
    { name: 'Government Bonds', shares: 50, value: 12500, change: 0.8 },
    { name: 'Gold ETF', shares: 30, value: 9850, change: 2.1 },
    { name: 'AI Innovation Fund', shares: 75, value: 15600, change: 5.8 },
  ];

  upcomingPayments: UpcomingPayment[] = [
    { name: 'Mortgage Payment', dueDate: 'Jun 15, 2025', amount: 3200, status: 'pending' },
    { name: 'Credit Card Bill', dueDate: 'Jun 18, 2025', amount: 1450, status: 'pending' },
    { name: 'Internet Service', dueDate: 'Jun 20, 2025', amount: 89.99, status: 'auto' },
    { name: 'Car Insurance', dueDate: 'Jun 25, 2025', amount: 245, status: 'auto' },
  ];

  userAccounts: AccountSummary[] = [];
  totalBankBalance: number = 0;
  accountTypeLabels = ACCOUNT_TYPE_LABELS;
  aiInsights: Insight[] = [];
  aiInsightsLoading = false;

  constructor(
    private authService: AuthService,
    private accountService: AccountService,
    private aiService: AiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
      if (user) {
        this.loadAccounts(user.id);
      }
    });

    if (!this.authService.isLoggedIn) {
      this.router.navigate(['/login']);
    }
  }

  loadAccounts(userId: string): void {
    this.accountService.getAccounts(userId).subscribe({
      next: (accounts) => {
        this.userAccounts = accounts;
        this.totalBankBalance = accounts.reduce((sum, a) => sum + a.balance, 0);
      }
    });
    this.loadAiInsights();
  }

  loadAiInsights(): void {
    this.aiInsightsLoading = true;
    this.aiService.getInsights().subscribe({
      next: (response) => {
        this.aiInsights = response.insights || [];
        this.aiInsightsLoading = false;
      },
      error: () => {
        this.aiInsightsLoading = false;
        console.warn('AI Insights not available - ensure the AI service is running on port 8090');
      }
    });
  }

  formatCurrency(amount: number, currency: string = 'USD'): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
    }).format(amount);
  }
}

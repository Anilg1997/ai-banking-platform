import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Loan, LoanApplication, AVAILABLE_LOAN_TYPES, LoanTypeInfo } from '../models/loan.model';

@Injectable({
  providedIn: 'root',
})
export class LoanService {
  private apiUrl = `${environment.apiUrl}/api/loans`;

  constructor(private http: HttpClient) {}

  getLoans(userId: string): Observable<Loan[]> {
    return this.http.get<Loan[]>(`${this.apiUrl}/user/${userId}`);
  }

  getLoanApplications(userId: string): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(`${this.apiUrl}/user/${userId}/applications`);
  }

  applyForLoan(data: { userId: string; loanType: string; amount: number; tenureMonths: number }): Observable<LoanApplication> {
    return this.http.post<LoanApplication>(`${this.apiUrl}/apply`, data);
  }

  getLoanTypes(): LoanTypeInfo[] {
    return AVAILABLE_LOAN_TYPES;
  }

  getMockLoans(): Loan[] {
    return [
      {
        id: 'loan-001', userId: 'user-1', loanType: 'HOME', loanAmount: 250000,
        approvedAmount: 250000, emiAmount: 1895.50, tenureMonths: 240, interestRate: 6.75,
        status: 'ACTIVE', nextPaymentDate: '2026-07-15T00:00:00Z', paidEmis: 24, totalEmis: 240,
        outstandingAmount: 235400.00, sanctionDate: '2024-07-15T00:00:00Z', currency: 'USD',
      },
      {
        id: 'loan-002', userId: 'user-1', loanType: 'AUTO', loanAmount: 45000,
        approvedAmount: 45000, emiAmount: 785.40, tenureMonths: 60, interestRate: 8.25,
        status: 'ACTIVE', nextPaymentDate: '2026-07-10T00:00:00Z', paidEmis: 14, totalEmis: 60,
        outstandingAmount: 36500.00, sanctionDate: '2025-05-10T00:00:00Z', currency: 'USD',
      },
    ];
  }

  getMockApplications(): LoanApplication[] {
    return [
      {
        id: 'app-001', userId: 'user-1', loanType: 'EDUCATION', amount: 75000,
        tenureMonths: 120, status: 'SUBMITTED', applicationRef: 'LN-2026-0042', createdAt: '2026-06-10T00:00:00Z',
      },
      {
        id: 'app-002', userId: 'user-1', loanType: 'BUSINESS', amount: 150000,
        tenureMonths: 84, status: 'VERIFICATION', applicationRef: 'LN-2026-0038', createdAt: '2026-06-05T00:00:00Z',
      },
    ];
  }
}

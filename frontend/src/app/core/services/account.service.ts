import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Account, AccountSummary, AccountRequest } from '../models/account.model';

@Injectable({
  providedIn: 'root',
})
export class AccountService {
  private apiUrl = `${environment.apiUrl}/api/accounts`;

  constructor(private http: HttpClient) {}

  getAccounts(userId: string): Observable<AccountSummary[]> {
    return this.http.get<AccountSummary[]>(`${this.apiUrl}/user/${userId}`);
  }

  getActiveAccounts(userId: string): Observable<AccountSummary[]> {
    return this.http.get<AccountSummary[]>(`${this.apiUrl}/user/${userId}/active`);
  }

  getAccount(id: string): Observable<Account> {
    return this.http.get<Account>(`${this.apiUrl}/${id}`);
  }

  getAccountByNumber(accountNumber: string): Observable<Account> {
    return this.http.get<Account>(`${this.apiUrl}/number/${accountNumber}`);
  }

  createAccount(request: AccountRequest): Observable<Account> {
    return this.http.post<Account>(this.apiUrl, request);
  }

  updateAccount(id: string, request: AccountRequest): Observable<Account> {
    return this.http.put<Account>(`${this.apiUrl}/${id}`, request);
  }

  updateAccountStatus(id: string, status: string): Observable<Account> {
    return this.http.patch<Account>(`${this.apiUrl}/${id}/status`, { status });
  }

  deleteAccount(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getTotalBalance(userId: string): Observable<{ totalBalance: number; accountCount: number; userId: string }> {
    return this.http.get<{ totalBalance: number; accountCount: number; userId: string }>(
      `${this.apiUrl}/user/${userId}/balance`
    );
  }
}

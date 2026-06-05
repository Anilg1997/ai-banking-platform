import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  TransactionSummary,
  TransactionResponse,
  TransferRequest,
} from '../models/transaction.model';

@Injectable({
  providedIn: 'root',
})
export class TransactionService {
  private apiUrl = `${environment.apiUrl}/api/transactions`;
  private sseSubject = new Subject<TransactionSummary>();
  public realTimeUpdates$ = this.sseSubject.asObservable();
  private eventSource: EventSource | null = null;

  constructor(private http: HttpClient, private zone: NgZone) {}

  /** Initiate a transfer */
  transfer(request: TransferRequest): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.apiUrl}/transfer`, request);
  }

  /** Get a transaction by ID */
  getTransaction(id: string): Observable<TransactionResponse> {
    return this.http.get<TransactionResponse>(`${this.apiUrl}/${id}`);
  }

  /** Get all transactions for a user (paginated) */
  getUserTransactions(userId: string, page = 0, size = 50): Observable<TransactionSummary[]> {
    return this.http.get<TransactionSummary[]>(
      `${this.apiUrl}/user/${userId}?page=${page}&size=${size}`
    );
  }

  /** Get recent transactions for a user */
  getRecentTransactions(userId: string, limit = 10): Observable<TransactionSummary[]> {
    return this.http.get<TransactionSummary[]>(
      `${this.apiUrl}/user/${userId}/recent?limit=${limit}`
    );
  }

  /** Get transactions for a specific account */
  getAccountTransactions(accountId: string): Observable<TransactionSummary[]> {
    return this.http.get<TransactionSummary[]>(`${this.apiUrl}/account/${accountId}`);
  }

  /** Get transaction count for a user */
  getTransactionCount(userId: string): Observable<{ count: number; userId: string }> {
    return this.http.get<{ count: number; userId: string }>(
      `${this.apiUrl}/user/${userId}/count`
    );
  }

  /** Reverse a transaction */
  reverseTransaction(id: string): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.apiUrl}/${id}/reverse`, {});
  }

  /** Connect to SSE stream for real-time transaction updates */
  connectToRealtimeUpdates(userId: string): void {
    if (this.eventSource) {
      this.disconnectFromRealtimeUpdates();
    }

    this.zone.runOutsideAngular(() => {
      this.eventSource = new EventSource(`${this.apiUrl}/stream/${userId}`);

      this.eventSource.onopen = () => {
        console.log('SSE: Connected to transaction stream');
      };

      this.eventSource.addEventListener('transaction', (event: MessageEvent) => {
        const data: TransactionSummary = JSON.parse(event.data);
        this.zone.run(() => {
          this.sseSubject.next(data);
        });
      });

      this.eventSource.addEventListener('connected', (event: MessageEvent) => {
        console.log('SSE: Connection established', event.data);
      });

      this.eventSource.onerror = (error) => {
        console.error('SSE: Error', error);
        // Reconnect after 5 seconds
        setTimeout(() => {
          if (this.eventSource) {
            this.connectToRealtimeUpdates(userId);
          }
        }, 5000);
      };
    });
  }

  /** Disconnect from SSE stream */
  disconnectFromRealtimeUpdates(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
      console.log('SSE: Disconnected');
    }
  }
}

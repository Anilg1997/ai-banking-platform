import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Card, CardSummary, CardTransaction, CardStatement,
  CardApplyRequest, CARD_NETWORK_COLORS,
} from '../models/card.model';
import { MOCK_CARDS, MOCK_CARD_TRANSACTIONS } from './card.mock';

@Injectable({
  providedIn: 'root',
})
export class CardService {
  private apiUrl = `${environment.apiUrl}/api/cards`;

  constructor(private http: HttpClient) {}

  getCards(userId: string): Observable<CardSummary[]> {
    return this.http.get<CardSummary[]>(`${this.apiUrl}/user/${userId}`);
  }

  getCard(id: string): Observable<Card> {
    return this.http.get<Card>(`${this.apiUrl}/${id}`);
  }

  getCardTransactions(cardId: string, page: number = 0, size: number = 20): Observable<{ content: CardTransaction[]; totalElements: number }> {
    return this.http.get<{ content: CardTransaction[]; totalElements: number }>(
      `${this.apiUrl}/${cardId}/transactions?page=${page}&size=${size}`
    );
  }

  getCardStatements(cardId: string): Observable<CardStatement[]> {
    return this.http.get<CardStatement[]>(`${this.apiUrl}/${cardId}/statements`);
  }

  freezeCard(id: string): Observable<Card> {
    return this.http.patch<Card>(`${this.apiUrl}/${id}/freeze`, {});
  }

  unfreezeCard(id: string): Observable<Card> {
    return this.http.patch<Card>(`${this.apiUrl}/${id}/unfreeze`, {});
  }

  activateCard(id: string): Observable<Card> {
    return this.http.patch<Card>(`${this.apiUrl}/${id}/activate`, {});
  }

  reportLostStolen(id: string): Observable<Card> {
    return this.http.patch<Card>(`${this.apiUrl}/${id}/report-lost-stolen`, {});
  }

  setAutoPay(id: string, enabled: boolean): Observable<Card> {
    return this.http.patch<Card>(`${this.apiUrl}/${id}/auto-pay`, { enabled });
  }

  setTransactionAlerts(id: string, enabled: boolean): Observable<Card> {
    return this.http.patch<Card>(`${this.apiUrl}/${id}/alerts`, { enabled });
  }

  makePayment(id: string, amount: number, accountId: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${id}/payments`, { amount, accountId });
  }

  applyForCard(request: CardApplyRequest): Observable<{ applicationRef: string; id: string }> {
    return this.http.post<{ applicationRef: string; id: string }>(`${this.apiUrl}/apply`, request);
  }

  getMockCards(): CardSummary[] {
    return MOCK_CARDS;
  }

  getMockTransactions(cardId: string): CardTransaction[] {
    return MOCK_CARD_TRANSACTIONS.filter(t => t.cardId === cardId);
  }

  getCardGradient(network: string): string {
    return CARD_NETWORK_COLORS[network as keyof typeof CARD_NETWORK_COLORS] || CARD_NETWORK_COLORS.VISA;
  }
}

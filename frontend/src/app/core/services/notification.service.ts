import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Notification, NOTIFICATION_ICONS, NOTIFICATION_COLORS } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private apiUrl = `${environment.apiUrl}/api/notifications`;
  private unreadCount = new BehaviorSubject<number>(0);
  private notifications = new BehaviorSubject<Notification[]>([]);
  private sseSubject = new Subject<Notification>();
  private eventSource: EventSource | null = null;

  public unreadCount$ = this.unreadCount.asObservable();
  public notifications$ = this.notifications.asObservable();
  public realTime$ = this.sseSubject.asObservable();

  constructor(private http: HttpClient, private zone: NgZone) {}

  getNotifications(userId: string): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}`);
  }

  getRecentNotifications(userId: string, limit = 10): Observable<Notification[]> {
    return this.http.get<Notification[]>(`${this.apiUrl}/user/${userId}/recent?limit=${limit}`);
  }

  getUnreadCount(userId: string): Observable<{ count: number; userId: string }> {
    return this.http.get<{ count: number; userId: string }>(`${this.apiUrl}/user/${userId}/count`);
  }

  markAsRead(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}/read`, {});
  }

  markAllAsRead(userId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/user/${userId}/read-all`, {});
  }

  getIcon(type: string): string {
    return NOTIFICATION_ICONS[type as keyof typeof NOTIFICATION_ICONS] || 'notifications';
  }

  getColor(type: string): string {
    return NOTIFICATION_COLORS[type as keyof typeof NOTIFICATION_COLORS] || '#8ba3c7';
  }

  timeAgo(dateStr: string): string {
    const date = new Date(dateStr);
    const now = new Date();
    const secs = Math.floor((now.getTime() - date.getTime()) / 1000);
    if (secs < 60) return 'just now';
    const mins = Math.floor(secs / 60);
    if (mins < 60) return `${mins}m ago`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 7) return `${days}d ago`;
    return date.toLocaleDateString();
  }

  connectToSSE(userId: string): void {
    if (this.eventSource) this.disconnect();
    this.zone.runOutsideAngular(() => {
      this.eventSource = new EventSource(`${this.apiUrl}/stream/${userId}`);
      this.eventSource.addEventListener('notification', (event: MessageEvent) => {
        const notif: Notification = JSON.parse(event.data);
        this.zone.run(() => {
          this.sseSubject.next(notif);
          const current = this.notifications.value;
          this.notifications.next([notif, ...current].slice(0, 100));
          this.unreadCount.next(this.unreadCount.value + 1);
        });
      });
      this.eventSource.onerror = () => {
        setTimeout(() => this.connectToSSE(userId), 5000);
      };
    });
  }

  disconnect(): void {
    this.eventSource?.close();
    this.eventSource = null;
  }

  loadInitialData(userId: string): void {
    this.getRecentNotifications(userId).subscribe({
      next: (n) => {
        this.notifications.next(n);
        this.unreadCount.next(n.filter((x) => !x.read).length);
      },
    });
    this.getUnreadCount(userId).subscribe({
      next: (r) => this.unreadCount.next(r.count),
    });
  }
}

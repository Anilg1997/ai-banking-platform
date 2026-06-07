import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AuthService } from '../core/auth/auth.service';
import { NotificationService } from '../core/services/notification.service';
import {
  Notification,
  NOTIFICATION_ICONS,
  NOTIFICATION_COLORS,
} from '../core/models/notification.model';

@Component({
  standalone: false,
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss'],
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notifications: Notification[] = [];
  isLoading = false;
  selectedFilter: 'all' | 'unread' = 'all';
  unreadCount = 0;

  notificationIcons = NOTIFICATION_ICONS;
  notificationColors = NOTIFICATION_COLORS;

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadNotifications();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadNotifications(): void {
    const userId = this.authService.currentUser?.id;
    if (!userId) return;

    this.isLoading = true;
    this.notificationService
      .getNotifications(userId)
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => (this.isLoading = false))
      )
      .subscribe({
        next: (notifications) => {
          this.notifications = notifications;
          this.unreadCount = notifications.filter((n) => !n.read).length;
        },
      });
  }

  get filteredNotifications(): Notification[] {
    if (this.selectedFilter === 'unread') {
      return this.notifications.filter((n) => !n.read);
    }
    return this.notifications;
  }

  markAsRead(notification: Notification): void {
    if (!notification.read) {
      this.notificationService.markAsRead(notification.id).subscribe(() => {
        notification.read = true;
        notification.readAt = new Date().toISOString();
        this.unreadCount = Math.max(0, this.unreadCount - 1);
      });
    }
  }

  markAllAsRead(): void {
    const userId = this.authService.currentUser?.id;
    if (userId) {
      this.notificationService.markAllAsRead(userId).subscribe(() => {
        this.notifications.forEach((n) => {
          n.read = true;
          n.readAt = new Date().toISOString();
        });
        this.unreadCount = 0;
      });
    }
  }

  deleteNotification(notification: Notification, event: Event): void {
    event.stopPropagation();
    // Remove from local list immediately
    this.notifications = this.notifications.filter((n) => n.id !== notification.id);
    if (!notification.read) {
      this.unreadCount = Math.max(0, this.unreadCount - 1);
    }
  }

  getIcon(type: string): string {
    return NOTIFICATION_ICONS[type as keyof typeof NOTIFICATION_ICONS] || 'notifications';
  }

  getColor(type: string): string {
    return NOTIFICATION_COLORS[type as keyof typeof NOTIFICATION_COLORS] || '#8ba3c7';
  }

  formatTime(dateStr: string): string {
    return this.notificationService.timeAgo(dateStr);
  }

  formatFullDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}

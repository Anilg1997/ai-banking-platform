import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { filter, takeUntil } from 'rxjs/operators';
import { AuthService } from './core/auth/auth.service';
import { NotificationService } from './core/services/notification.service';

@Component({
  standalone: false,
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'NovaBank';
  isAuthPage = true;
  showNotifications = false;
  unreadCount = 0;
  notifications: any[] = [];
  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    public authService: AuthService,
    public notificationService: NotificationService
  ) {}

  ngOnInit() {
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.isAuthPage = event.url === '/login' || event.url === '/register';
      });

    // Subscribe to notification data
    this.notificationService.unreadCount$
      .pipe(takeUntil(this.destroy$))
      .subscribe(count => this.unreadCount = count);

    this.notificationService.notifications$
      .pipe(takeUntil(this.destroy$))
      .subscribe(n => this.notifications = n);

    // Connect SSE when user logs in
    this.authService.currentUser$
      .pipe(takeUntil(this.destroy$))
      .subscribe(user => {
        if (user) {
          this.notificationService.connectToSSE(user.id);
          this.notificationService.loadInitialData(user.id);
        }
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.notificationService.disconnect();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (this.showNotifications && !target.closest('.notification-wrapper')) {
      this.closeNotifications();
    }
  }

  toggleNotifications() {
    if (this.showNotifications) {
      this.closeNotifications();
    } else {
      this.showNotifications = true;
    }
  }

  closeNotifications(): void {
    if (!this.showNotifications) return;
    this.showNotifications = false;
    // Mark all as read when closing
    const userId = this.authService.currentUser?.id;
    if (userId && this.unreadCount > 0) {
      this.notificationService.markAllAsRead(userId).subscribe();
    }
  }

  getInitials(): string {
    const user = this.authService.currentUser;
    if (user) {
      return (user.firstName?.[0] || '') + (user.lastName?.[0] || '');
    }
    return 'NB';
  }
}

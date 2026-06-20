import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from '../core/auth/auth.service';
import { User } from '../core/models/user.model';

@Component({
  standalone: false,
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss'],
})
export class ProfileComponent implements OnInit, OnDestroy {
  user: User | null = null;
  isLoggedIn = false;

  personalInfo = { firstName: '', lastName: '', email: '', phone: '' };
  passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
  preferences = { emailNotifications: true, smsNotifications: false, pushNotifications: true, currency: 'USD', language: 'English' };
  twoFactorEnabled = false;

  isSaving = false;
  isChangingPassword = false;

  recentLogins = [
    { device: 'Chrome on Windows', location: 'New York, USA', time: '2 hours ago', current: true },
    { device: 'Safari on iPhone', location: 'New York, USA', time: '2 days ago', current: false },
    { device: 'Firefox on macOS', location: 'Boston, USA', time: '1 week ago', current: false },
  ];

  savedDevices = [
    { name: 'John\'s iPhone 15', type: 'iPhone', lastUsed: '2 days ago', trusted: true },
    { name: 'Home Desktop', type: 'Windows PC', lastUsed: '1 hour ago', trusted: true },
    { name: 'Work Laptop', type: 'MacBook Pro', lastUsed: '1 week ago', trusted: false },
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn) {
      this.router.navigate(['/login']);
      return;
    }
    this.authService.currentUser$.pipe(takeUntil(this.destroy$)).subscribe(user => {
      this.user = user;
      this.isLoggedIn = !!user;
      if (user) {
        this.personalInfo = {
          firstName: user.firstName || '',
          lastName: user.lastName || '',
          email: user.email || '',
          phone: '',
        };
        this.twoFactorEnabled = user.twoFactorEnabled || false;
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  savePersonalInfo(): void {
    this.isSaving = true;
    setTimeout(() => {
      this.isSaving = false;
      this.snackBar.open('Profile updated successfully!', 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
    }, 800);
  }

  changePassword(): void {
    if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
      this.snackBar.open('Passwords do not match', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      return;
    }
    if (this.passwordForm.newPassword.length < 8) {
      this.snackBar.open('Password must be at least 8 characters', 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      return;
    }

    this.isChangingPassword = true;
    setTimeout(() => {
      this.isChangingPassword = false;
      this.passwordForm = { currentPassword: '', newPassword: '', confirmPassword: '' };
      this.snackBar.open('Password changed successfully!', 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
    }, 1000);
  }

  toggleTwoFactor(): void {
    this.twoFactorEnabled = !this.twoFactorEnabled;
    this.snackBar.open(
      this.twoFactorEnabled ? 'Two-factor authentication enabled' : 'Two-factor authentication disabled',
      'Close', { duration: 3000, panelClass: ['success-snackbar'] }
    );
  }

  getInitials(): string {
    if (!this.user) return '?';
    return (this.user.firstName?.charAt(0) || '') + (this.user.lastName?.charAt(0) || '');
  }

  getMemberSince(): string {
    return 'Member since June 2025';
  }
}

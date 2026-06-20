import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { AdminService, AdminUser } from '../core/services/admin.service';

@Component({
  standalone: false,
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.scss'],
})
export class AdminUsersComponent implements OnInit, OnDestroy {
  users: AdminUser[] = [];
  filteredUsers: AdminUser[] = [];
  isLoading = false;
  searchQuery = '';
  selectedRole = 'All';
  activeCount = 0;
  lockedCount = 0;
  verifiedCount = 0;

  roles = ['All', 'User', 'Premium', 'Admin'];
  editableRoles = ['User', 'Premium', 'Admin'];

  private destroy$ = new Subject<void>();

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadUsers(): void {
    this.isLoading = true;
    this.adminService.getUsers()
      .pipe(takeUntil(this.destroy$), finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (users) => {
          this.users = users;
          this.applyFilters();
        },
      });
  }

  applyFilters(): void {
    let result = [...this.users];
    if (this.searchQuery) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(u =>
        u.firstName.toLowerCase().includes(q) ||
        u.lastName.toLowerCase().includes(q) ||
        u.email.toLowerCase().includes(q)
      );
    }
    if (this.selectedRole !== 'All') {
      result = result.filter(u => u.role === this.selectedRole);
    }
    this.filteredUsers = result;
    this.activeCount = this.users.filter(u => u.status === 'Active').length;
    this.lockedCount = this.users.filter(u => u.status === 'Locked').length;
    this.verifiedCount = this.users.filter(u => u.emailVerified).length;
  }

  updateRole(userId: string, role: string): void {
    this.adminService.updateRole(userId, role)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const user = this.users.find(u => u.id === userId);
          if (user) user.role = role;
          this.applyFilters();
        },
      });
  }

  toggleStatus(userId: string, currentStatus: string): void {
    const newStatus = currentStatus === 'Active' ? 'Locked' : 'Active';
    this.adminService.toggleUserStatus(userId, currentStatus)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const user = this.users.find(u => u.id === userId);
          if (user) user.status = newStatus as 'Active' | 'Locked';
          this.applyFilters();
        },
      });
  }

  getRoleBadgeClass(role: string): string {
    switch (role) {
      case 'Admin': return 'role-admin';
      case 'Premium': return 'role-premium';
      case 'User': return 'role-user';
      default: return '';
    }
  }

  getStatusBadgeClass(status: string): string {
    return status === 'Active' ? 'status-active' : 'status-locked';
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }
}

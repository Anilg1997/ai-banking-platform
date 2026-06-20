import { Component } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  standalone: false,
  selector: 'app-admin-settings',
  templateUrl: './admin-settings.component.html',
  styleUrls: ['./admin-settings.component.scss'],
})
export class AdminSettingsComponent {
  general = {
    appName: 'NovaBank',
    supportEmail: 'support@novabank.com',
    maintenanceMode: false,
  };

  security = {
    maxLoginAttempts: 5,
    lockDurationMinutes: 30,
    sessionTimeoutMinutes: 60,
    twoFactorRequired: true,
  };

  notifications = {
    emailEnabled: true,
    smsEnabled: false,
    pushEnabled: true,
    emailProvider: 'smtp.novabank.com',
    dailyReportTime: '08:00',
  };

  ai = {
    ollamaModel: 'llama3.1',
    temperature: 0.7,
    ragEnabled: true,
    toolsEnabled: true,
    maxTokens: 2048,
  };

  models = ['llama3.1', 'llama3', 'mistral', 'mixtral', 'gemma2', 'phi3'];

  constructor(private snackBar: MatSnackBar) {}

  saveGeneral(): void {
    this.showToast('General settings saved successfully');
  }

  saveSecurity(): void {
    this.showToast('Security settings saved successfully');
  }

  saveNotifications(): void {
    this.showToast('Notification settings saved successfully');
  }

  saveAi(): void {
    this.showToast('AI settings saved successfully');
  }

  private showToast(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['success-snackbar'],
    });
  }
}

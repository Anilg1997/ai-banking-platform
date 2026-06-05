export interface Notification {
  id: string;
  userId: string;
  type: NotificationType;
  title: string;
  message: string;
  referenceId: string;
  referenceType: string;
  read: boolean;
  createdAt: string;
  readAt: string;
}

export type NotificationType =
  | 'TRANSACTION_COMPLETED'
  | 'TRANSACTION_FAILED'
  | 'TRANSACTION_REVERSED'
  | 'ACCOUNT_CREATED'
  | 'ACCOUNT_STATUS_CHANGE'
  | 'DEPOSIT_RECEIVED'
  | 'PAYMENT_DUE'
  | 'SECURITY_ALERT'
  | 'SYSTEM_UPDATE';

export const NOTIFICATION_ICONS: Record<NotificationType, string> = {
  TRANSACTION_COMPLETED: 'check_circle',
  TRANSACTION_FAILED: 'error',
  TRANSACTION_REVERSED: 'undo',
  ACCOUNT_CREATED: 'account_balance',
  ACCOUNT_STATUS_CHANGE: 'info',
  DEPOSIT_RECEIVED: 'arrow_downward',
  PAYMENT_DUE: 'calendar_today',
  SECURITY_ALERT: 'warning',
  SYSTEM_UPDATE: 'system_update',
};

export const NOTIFICATION_COLORS: Record<NotificationType, string> = {
  TRANSACTION_COMPLETED: '#22c55e',
  TRANSACTION_FAILED: '#ef4444',
  TRANSACTION_REVERSED: '#a78bfa',
  ACCOUNT_CREATED: '#60a5fa',
  ACCOUNT_STATUS_CHANGE: '#f59e0b',
  DEPOSIT_RECEIVED: '#22c55e',
  PAYMENT_DUE: '#f59e0b',
  SECURITY_ALERT: '#ef4444',
  SYSTEM_UPDATE: '#60a5fa',
};

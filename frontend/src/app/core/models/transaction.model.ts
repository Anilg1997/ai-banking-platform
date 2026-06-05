export interface TransactionSummary {
  id: string;
  transactionRef: string;
  type: TransactionType;
  status: TransactionStatus;
  amount: number;
  currency: string;
  fromAccountId: string;
  fromUserId: string;
  fromAccountNumber: string;
  fromAccountName: string;
  toAccountId: string;
  toUserId: string;
  toAccountNumber: string;
  toAccountName: string;
  description: string;
  category: string;
  createdAt: string;
}

export interface TransactionResponse {
  id: string;
  transactionRef: string;
  type: TransactionType;
  status: TransactionStatus;
  amount: number;
  currency: string;
  fromAccountId: string;
  fromUserId: string;
  fromAccountNumber: string;
  fromAccountName: string;
  toAccountId: string;
  toUserId: string;
  toAccountNumber: string;
  toAccountName: string;
  toEmail: string;
  description: string;
  category: string;
  feeAmount: number;
  failureReason: string;
  createdAt: string;
  completedAt: string;
}

export interface TransferRequest {
  fromAccountId: string;
  toAccountId: string;
  toEmail?: string;
  amount: number;
  currency: string;
  description?: string;
  category?: string;
}

export type TransactionType = 'TRANSFER' | 'DEPOSIT' | 'WITHDRAWAL' | 'PAYMENT' | 'REFUND' | 'FEE' | 'INTEREST' | 'EXCHANGE';

export type TransactionStatus = 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'REVERSED' | 'CANCELLED';

export const TRANSACTION_TYPE_LABELS: Record<TransactionType, string> = {
  TRANSFER: 'Transfer',
  DEPOSIT: 'Deposit',
  WITHDRAWAL: 'Withdrawal',
  PAYMENT: 'Payment',
  REFUND: 'Refund',
  FEE: 'Fee',
  INTEREST: 'Interest',
  EXCHANGE: 'Exchange',
};

export const TRANSACTION_TYPE_ICONS: Record<TransactionType, string> = {
  TRANSFER: 'swap_horiz',
  DEPOSIT: 'download',
  WITHDRAWAL: 'upload',
  PAYMENT: 'receipt',
  REFUND: 'reply',
  FEE: 'money_off',
  INTEREST: 'trending_up',
  EXCHANGE: 'currency_exchange',
};

export const TRANSACTION_STATUS_LABELS: Record<TransactionStatus, string> = {
  PENDING: 'Pending',
  PROCESSING: 'Processing',
  COMPLETED: 'Completed',
  FAILED: 'Failed',
  REVERSED: 'Reversed',
  CANCELLED: 'Cancelled',
};

export const TRANSACTION_CATEGORIES = [
  'FOOD', 'TRANSPORT', 'SHOPPING', 'BILLS', 'ENTERTAINMENT',
  'HEALTH', 'EDUCATION', 'SALARY', 'SAVINGS', 'INVESTMENT',
  'RENT', 'INSURANCE', 'OTHER'
];

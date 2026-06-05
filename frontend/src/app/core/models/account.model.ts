export interface Account {
  id: string;
  accountNumber: string;
  userId: string;
  accountName: string;
  accountType: AccountType;
  status: AccountStatus;
  balance: number;
  availableBalance: number;
  holdsBalance: number;
  currency: string;
  interestRate: number;
  branchCode: string;
  branchName: string;
  allowOverdraft: boolean;
  overdraftLimit: number;
  createdAt: string;
  updatedAt: string;
}

export interface AccountSummary {
  id: string;
  accountNumber: string;
  accountName: string;
  accountType: AccountType;
  status: AccountStatus;
  balance: number;
  availableBalance: number;
  currency: string;
  allowOverdraft: boolean;
}

export interface AccountRequest {
  userId: string;
  accountName: string;
  accountType: AccountType;
  currency: string;
  initialDeposit?: number;
  allowOverdraft?: boolean;
  overdraftLimit?: number;
  branchCode?: string;
  branchName?: string;
}

export type AccountType = 'CHECKING' | 'SAVINGS' | 'BUSINESS' | 'CREDIT' | 'INVESTMENT' | 'FIXED_DEPOSIT';

export type AccountStatus = 'PENDING' | 'ACTIVE' | 'FROZEN' | 'CLOSED' | 'SUSPENDED';

export const ACCOUNT_TYPE_LABELS: Record<AccountType, string> = {
  CHECKING: 'Checking',
  SAVINGS: 'Savings',
  BUSINESS: 'Business',
  CREDIT: 'Credit',
  INVESTMENT: 'Investment',
  FIXED_DEPOSIT: 'Fixed Deposit',
};

export const ACCOUNT_STATUS_LABELS: Record<AccountStatus, string> = {
  PENDING: 'Pending',
  ACTIVE: 'Active',
  FROZEN: 'Frozen',
  CLOSED: 'Closed',
  SUSPENDED: 'Suspended',
};

export const ACCOUNT_TYPE_ICONS: Record<AccountType, string> = {
  CHECKING: 'account_balance',
  SAVINGS: 'savings',
  BUSINESS: 'business',
  CREDIT: 'credit_card',
  INVESTMENT: 'trending_up',
  FIXED_DEPOSIT: 'lock',
};

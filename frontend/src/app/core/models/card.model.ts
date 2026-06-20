export interface Card {
  id: string;
  userId: string;
  cardNumber: string;
  cardholderName: string;
  expiryDate: string;
  cvv: string;
  cardNetwork: CardNetwork;
  cardType: CardType;
  status: CardStatus;
  creditLimit: number;
  availableCredit: number;
  usedCredit: number;
  rewardPoints: number;
  currency: string;
  issuedAt: string;
  isFrozen: boolean;
  isActivated: boolean;
  autoPayEnabled: boolean;
  transactionAlertsEnabled: boolean;
}

export interface CardSummary {
  id: string;
  cardNumber: string;
  cardholderName: string;
  expiryDate: string;
  cardNetwork: CardNetwork;
  cardType: CardType;
  status: CardStatus;
  creditLimit: number;
  availableCredit: number;
  usedCredit: number;
  rewardPoints: number;
  currency: string;
  isFrozen: boolean;
  isActivated: boolean;
}

export interface CardTransaction {
  id: string;
  cardId: string;
  merchantName: string;
  merchantCategory: string;
  description: string;
  amount: number;
  currency: string;
  type: 'credit' | 'debit';
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
  createdAt: string;
}

export interface CardStatement {
  id: string;
  cardId: string;
  statementMonth: string;
  statementYear: number;
  totalDue: number;
  minimumDue: number;
  dueDate: string;
  paidAmount: number;
  status: 'PENDING' | 'PAID' | 'OVERDUE' | 'PARTIAL';
  pdfUrl?: string;
  generatedAt: string;
}

export interface CardApplication {
  id: string;
  userId: string;
  cardNetwork: CardNetwork;
  cardType: CardType;
  status: ApplicationStatus;
  applicationRef: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  employmentType: string;
  employerName: string;
  designation: string;
  annualIncome: number;
  aadharNumber: string;
  panNumber: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  pincode: string;
  createdAt: string;
}

export interface CardApplyRequest {
  userId: string;
  cardNetwork: CardNetwork;
  cardType: CardType;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  employmentType: string;
  employerName: string;
  designation: string;
  annualIncome: number;
  aadharNumber: string;
  panNumber: string;
  addressLine1: string;
  addressLine2: string;
  city: string;
  state: string;
  pincode: string;
}

export type CardNetwork = 'VISA' | 'MASTERCARD' | 'AMEX' | 'RUPAY';
export type CardType = 'CREDIT' | 'DEBIT' | 'PREPAID';
export type CardStatus = 'ACTIVE' | 'INACTIVE' | 'BLOCKED' | 'LOST' | 'STOLEN' | 'EXPIRED';
export type ApplicationStatus = 'DRAFT' | 'SUBMITTED' | 'VERIFICATION' | 'APPROVED' | 'REJECTED';

export const CARD_NETWORK_LABELS: Record<CardNetwork, string> = {
  VISA: 'Visa',
  MASTERCARD: 'Mastercard',
  AMEX: 'American Express',
  RUPAY: 'RuPay',
};

export const CARD_TYPE_LABELS: Record<CardType, string> = {
  CREDIT: 'Credit',
  DEBIT: 'Debit',
  PREPAID: 'Prepaid',
};

export const CARD_STATUS_LABELS: Record<CardStatus, string> = {
  ACTIVE: 'Active',
  INACTIVE: 'Inactive',
  BLOCKED: 'Blocked',
  LOST: 'Lost',
  STOLEN: 'Stolen',
  EXPIRED: 'Expired',
};

export const APPLICATION_STATUS_LABELS: Record<ApplicationStatus, string> = {
  DRAFT: 'Draft',
  SUBMITTED: 'Submitted',
  VERIFICATION: 'Under Verification',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
};

export const CARD_NETWORK_COLORS: Record<CardNetwork, string> = {
  VISA: 'linear-gradient(135deg, #b8860b, #d4a843, #f0d68a)',
  MASTERCARD: 'linear-gradient(135deg, #1a3a8a, #2563eb, #60a5fa)',
  AMEX: 'linear-gradient(135deg, #065f46, #059669, #34d399)',
  RUPAY: 'linear-gradient(135deg, #9a3412, #ea580c, #fb923c)',
};

export interface Loan {
  id: string;
  userId: string;
  loanType: LoanType;
  loanAmount: number;
  approvedAmount: number;
  emiAmount: number;
  tenureMonths: number;
  interestRate: number;
  status: LoanStatus;
  nextPaymentDate: string;
  paidEmis: number;
  totalEmis: number;
  outstandingAmount: number;
  sanctionDate: string;
  closureDate?: string;
  currency: string;
}

export interface LoanApplication {
  id: string;
  userId: string;
  loanType: LoanType;
  amount: number;
  tenureMonths: number;
  status: LoanApplicationStatus;
  applicationRef: string;
  createdAt: string;
}

export interface LoanTypeInfo {
  type: LoanType;
  label: string;
  icon: string;
  description: string;
  interestRate: string;
  maxAmount: number;
  maxTenure: string;
}

export type LoanType = 'PERSONAL' | 'HOME' | 'AUTO' | 'EDUCATION' | 'BUSINESS';
export type LoanStatus = 'ACTIVE' | 'CLOSED' | 'DEFAULTER';
export type LoanApplicationStatus = 'DRAFT' | 'SUBMITTED' | 'VERIFICATION' | 'APPROVED' | 'REJECTED' | 'DISBURSED';

export const LOAN_TYPE_LABELS: Record<LoanType, string> = {
  PERSONAL: 'Personal Loan',
  HOME: 'Home Loan',
  AUTO: 'Auto Loan',
  EDUCATION: 'Education Loan',
  BUSINESS: 'Business Loan',
};

export const LOAN_TYPE_ICONS: Record<LoanType, string> = {
  PERSONAL: 'person',
  HOME: 'home',
  AUTO: 'directions_car',
  EDUCATION: 'school',
  BUSINESS: 'business_center',
};

export const LOAN_STATUS_LABELS: Record<LoanStatus, string> = {
  ACTIVE: 'Active',
  CLOSED: 'Closed',
  DEFAULTER: 'Defaulted',
};

export const AVAILABLE_LOAN_TYPES: LoanTypeInfo[] = [
  {
    type: 'PERSONAL',
    label: 'Personal Loan',
    icon: 'person',
    description: 'Fund your dreams with flexible personal loans at competitive rates.',
    interestRate: '10.5% - 15% p.a.',
    maxAmount: 50000,
    maxTenure: '5 years',
  },
  {
    type: 'HOME',
    label: 'Home Loan',
    icon: 'home',
    description: 'Make your dream home a reality with our affordable home loans.',
    interestRate: '6.5% - 9% p.a.',
    maxAmount: 500000,
    maxTenure: '30 years',
  },
  {
    type: 'AUTO',
    label: 'Auto Loan',
    icon: 'directions_car',
    description: 'Drive your dream car with easy EMI options and quick approval.',
    interestRate: '7.5% - 11% p.a.',
    maxAmount: 100000,
    maxTenure: '7 years',
  },
  {
    type: 'EDUCATION',
    label: 'Education Loan',
    icon: 'school',
    description: 'Invest in your future with education loans for premier institutions.',
    interestRate: '8% - 12% p.a.',
    maxAmount: 150000,
    maxTenure: '15 years',
  },
  {
    type: 'BUSINESS',
    label: 'Business Loan',
    icon: 'business_center',
    description: 'Grow your enterprise with customized business financing solutions.',
    interestRate: '11% - 18% p.a.',
    maxAmount: 500000,
    maxTenure: '10 years',
  },
];

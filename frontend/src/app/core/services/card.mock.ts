import { CardSummary, CardTransaction, CardStatement } from '../models/card.model';

export const MOCK_CARDS: CardSummary[] = [
  {
    id: 'card-001',
    cardNumber: '4532015112890367',
    cardholderName: 'John Doe',
    expiryDate: '08/28',
    cardNetwork: 'VISA',
    cardType: 'CREDIT',
    status: 'ACTIVE',
    creditLimit: 15000,
    availableCredit: 10500,
    usedCredit: 4500,
    rewardPoints: 2850,
    currency: 'USD',
    isFrozen: false,
    isActivated: true,
  },
  {
    id: 'card-002',
    cardNumber: '5425233430109903',
    cardholderName: 'John Doe',
    expiryDate: '11/27',
    cardNetwork: 'MASTERCARD',
    cardType: 'CREDIT',
    status: 'ACTIVE',
    creditLimit: 25000,
    availableCredit: 18300,
    usedCredit: 6700,
    rewardPoints: 5200,
    currency: 'USD',
    isFrozen: false,
    isActivated: true,
  },
  {
    id: 'card-003',
    cardNumber: '371449635398431',
    cardholderName: 'John Doe',
    expiryDate: '03/29',
    cardNetwork: 'AMEX',
    cardType: 'DEBIT',
    status: 'ACTIVE',
    creditLimit: 0,
    availableCredit: 0,
    usedCredit: 0,
    rewardPoints: 150,
    currency: 'USD',
    isFrozen: true,
    isActivated: true,
  },
];

export const MOCK_CARD_TRANSACTIONS: CardTransaction[] = [
  {
    id: 'ct-001', cardId: 'card-001', merchantName: 'Amazon',
    merchantCategory: 'SHOPPING', description: 'Online purchase', amount: 234.99,
    currency: 'USD', type: 'debit', status: 'COMPLETED', createdAt: '2026-06-18T14:30:00Z',
  },
  {
    id: 'ct-002', cardId: 'card-001', merchantName: 'Starbucks',
    merchantCategory: 'FOOD', description: 'Coffee & snacks', amount: 12.50,
    currency: 'USD', type: 'debit', status: 'COMPLETED', createdAt: '2026-06-18T09:15:00Z',
  },
  {
    id: 'ct-003', cardId: 'card-001', merchantName: 'Netflix',
    merchantCategory: 'ENTERTAINMENT', description: 'Monthly subscription', amount: 19.99,
    currency: 'USD', type: 'debit', status: 'COMPLETED', createdAt: '2026-06-17T00:00:00Z',
  },
  {
    id: 'ct-004', cardId: 'card-001', merchantName: 'Shell Gas Station',
    merchantCategory: 'TRANSPORT', description: 'Fuel', amount: 65.30,
    currency: 'USD', type: 'debit', status: 'COMPLETED', createdAt: '2026-06-16T18:45:00Z',
  },
  {
    id: 'ct-005', cardId: 'card-001', merchantName: 'Payment Received',
    merchantCategory: 'OTHER', description: 'Credit card payment', amount: 1000.00,
    currency: 'USD', type: 'credit', status: 'COMPLETED', createdAt: '2026-06-15T10:00:00Z',
  },
  {
    id: 'ct-006', cardId: 'card-002', merchantName: 'Best Buy',
    merchantCategory: 'SHOPPING', description: 'Electronics purchase', amount: 1299.99,
    currency: 'USD', type: 'debit', status: 'COMPLETED', createdAt: '2026-06-17T16:20:00Z',
  },
  {
    id: 'ct-007', cardId: 'card-002', merchantName: 'Uber',
    merchantCategory: 'TRANSPORT', description: 'Ride to airport', amount: 45.80,
    currency: 'USD', type: 'debit', status: 'PENDING', createdAt: '2026-06-19T07:30:00Z',
  },
  {
    id: 'ct-008', cardId: 'card-002', merchantName: 'Costco',
    merchantCategory: 'SHOPPING', description: 'Weekly groceries', amount: 287.43,
    currency: 'USD', type: 'debit', status: 'COMPLETED', createdAt: '2026-06-16T12:00:00Z',
  },
  {
    id: 'ct-009', cardId: 'card-002', merchantName: 'Payment Received',
    merchantCategory: 'OTHER', description: 'Credit card payment', amount: 2000.00,
    currency: 'USD', type: 'credit', status: 'COMPLETED', createdAt: '2026-06-14T09:00:00Z',
  },
  {
    id: 'ct-010', cardId: 'card-003', merchantName: 'Walmart',
    merchantCategory: 'SHOPPING', description: 'Household items', amount: 156.78,
    currency: 'USD', type: 'debit', status: 'COMPLETED', createdAt: '2026-06-15T15:45:00Z',
  },
];

export const MOCK_CARD_STATEMENTS: CardStatement[] = [
  {
    id: 'stmt-001', cardId: 'card-001', statementMonth: 'May', statementYear: 2026,
    totalDue: 1250.75, minimumDue: 45.00, dueDate: '2026-06-20T00:00:00Z',
    paidAmount: 1000.00, status: 'PARTIAL', generatedAt: '2026-06-01T00:00:00Z',
  },
  {
    id: 'stmt-002', cardId: 'card-001', statementMonth: 'April', statementYear: 2026,
    totalDue: 980.50, minimumDue: 35.00, dueDate: '2026-05-20T00:00:00Z',
    paidAmount: 980.50, status: 'PAID', generatedAt: '2026-05-01T00:00:00Z',
  },
  {
    id: 'stmt-003', cardId: 'card-001', statementMonth: 'March', statementYear: 2026,
    totalDue: 2100.00, minimumDue: 65.00, dueDate: '2026-04-20T00:00:00Z',
    paidAmount: 2100.00, status: 'PAID', generatedAt: '2026-04-01T00:00:00Z',
  },
  {
    id: 'stmt-004', cardId: 'card-002', statementMonth: 'May', statementYear: 2026,
    totalDue: 2340.22, minimumDue: 75.00, dueDate: '2026-06-22T00:00:00Z',
    paidAmount: 0, status: 'PENDING', generatedAt: '2026-06-01T00:00:00Z',
  },
  {
    id: 'stmt-005', cardId: 'card-002', statementMonth: 'April', statementYear: 2026,
    totalDue: 1875.90, minimumDue: 55.00, dueDate: '2026-05-22T00:00:00Z',
    paidAmount: 1875.90, status: 'PAID', generatedAt: '2026-05-01T00:00:00Z',
  },
];

export const MOCK_MONTHLY_STATS = {
  totalSpent: 3240.50,
  lastPayment: 1000.00,
  interestYTD: 145.30,
  rewardPoints: 2850,
};

import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

export interface AdminUser {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  status: 'Active' | 'Locked';
  emailVerified: boolean;
  createdAt: string;
}

export interface CardSummary {
  id: string;
  cardNumber: string;
  cardHolderName: string;
  cardType: 'VISA' | 'MASTERCARD' | 'AMEX';
  status: 'ACTIVE' | 'FROZEN' | 'CANCELLED';
  creditLimit: number;
  availableCredit: number;
  expiryDate: string;
}

export interface CardApplication {
  id: string;
  applicantName: string;
  applicantEmail: string;
  cardType: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  annualIncome: number;
  creditScore: number;
  appliedAt: string;
}

export interface AgentConversation {
  id: string;
  title: string;
  agentType: string;
  status: string;
  messageCount: number;
  lastActivity: string;
  messages: { role: string; content: string; timestamp: string }[];
}

export interface AgentStats {
  totalConversations: number;
  activeConversations: number;
  actionsToday: number;
}

export interface AdminStats {
  totalUsers: number;
  activeUsers: number;
  totalCards: number;
  creditLimitUsage: number;
  agentConversations: number;
}

export interface KnowledgeDoc {
  id: string;
  title: string;
  type: string;
  lastUpdated: string;
  size: string;
}

export interface AdminActivity {
  id: string;
  action: string;
  admin: string;
  target: string;
  timestamp: string;
}

export interface CardApplicationDetail {
  id: string;
  applicantName: string;
  applicantEmail: string;
  phoneNumber: string;
  dateOfBirth: string;
  address: string;
  employmentStatus: string;
  annualIncome: number;
  creditScore: number;
  cardType: string;
  requestedLimit: number;
  status: string;
  appliedAt: string;
  documents: string[];
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  getUserStats(): Observable<{ totalUsers: number; activeUsers: number }> {
    return of({ totalUsers: 12847, activeUsers: 10234 });
  }

  getCardStats(): Observable<{ totalCards: number; activeCards: number; frozenCards: number; pendingApplications: number; creditLimitUsage: number }> {
    return of({ totalCards: 15623, activeCards: 12480, frozenCards: 340, pendingApplications: 89, creditLimitUsage: 67 });
  }

  getAgentStats(): Observable<AgentStats> {
    return of({ totalConversations: 3450, activeConversations: 128, actionsToday: 892 });
  }

  getUsers(): Observable<AdminUser[]> {
    return of([
      { id: '1', firstName: 'John', lastName: 'Smith', email: 'john.smith@email.com', role: 'User', status: 'Active', emailVerified: true, createdAt: '2025-01-15' },
      { id: '2', firstName: 'Emily', lastName: 'Johnson', email: 'emily.j@email.com', role: 'User', status: 'Active', emailVerified: true, createdAt: '2025-02-20' },
      { id: '3', firstName: 'Michael', lastName: 'Brown', email: 'm.brown@email.com', role: 'Premium', status: 'Locked', emailVerified: true, createdAt: '2024-11-03' },
      { id: '4', firstName: 'Sarah', lastName: 'Davis', email: 'sarah.davis@email.com', role: 'User', status: 'Active', emailVerified: false, createdAt: '2025-03-10' },
      { id: '5', firstName: 'Robert', lastName: 'Wilson', email: 'r.wilson@email.com', role: 'Admin', status: 'Active', emailVerified: true, createdAt: '2024-08-22' },
      { id: '6', firstName: 'Jessica', lastName: 'Taylor', email: 'j.taylor@email.com', role: 'Premium', status: 'Active', emailVerified: true, createdAt: '2024-12-01' },
      { id: '7', firstName: 'David', lastName: 'Anderson', email: 'd.anderson@email.com', role: 'User', status: 'Locked', emailVerified: false, createdAt: '2025-04-18' },
      { id: '8', firstName: 'Lisa', lastName: 'Martinez', email: 'l.martinez@email.com', role: 'User', status: 'Active', emailVerified: true, createdAt: '2025-05-05' },
    ]);
  }

  getCards(): Observable<CardSummary[]> {
    return of([
      { id: 'c1', cardNumber: '****4532', cardHolderName: 'John Smith', cardType: 'VISA', status: 'ACTIVE', creditLimit: 15000, availableCredit: 8200, expiryDate: '08/27' },
      { id: 'c2', cardNumber: '****7821', cardHolderName: 'Emily Johnson', cardType: 'MASTERCARD', status: 'ACTIVE', creditLimit: 25000, availableCredit: 18500, expiryDate: '11/26' },
      { id: 'c3', cardNumber: '****3390', cardHolderName: 'Michael Brown', cardType: 'AMEX', status: 'FROZEN', creditLimit: 50000, availableCredit: 32000, expiryDate: '03/28' },
      { id: 'c4', cardNumber: '****5612', cardHolderName: 'Sarah Davis', cardType: 'VISA', status: 'ACTIVE', creditLimit: 10000, availableCredit: 7200, expiryDate: '06/27' },
      { id: 'c5', cardNumber: '****8945', cardHolderName: 'Robert Wilson', cardType: 'MASTERCARD', status: 'CANCELLED', creditLimit: 30000, availableCredit: 0, expiryDate: '09/25' },
      { id: 'c6', cardNumber: '****2103', cardHolderName: 'Jessica Taylor', cardType: 'VISA', status: 'ACTIVE', creditLimit: 20000, availableCredit: 14500, expiryDate: '01/28' },
    ]);
  }

  getCardApplications(): Observable<CardApplication[]> {
    return of([
      { id: 'a1', applicantName: 'Alex Turner', applicantEmail: 'alex.t@email.com', cardType: 'VISA Platinum', status: 'PENDING', annualIncome: 85000, creditScore: 720, appliedAt: '2 hours ago' },
      { id: 'a2', applicantName: 'Maria Garcia', applicantEmail: 'm.garcia@email.com', cardType: 'MASTERCARD Gold', status: 'PENDING', annualIncome: 62000, creditScore: 680, appliedAt: '5 hours ago' },
      { id: 'a3', applicantName: 'James Lee', applicantEmail: 'j.lee@email.com', cardType: 'AMEX Platinum', status: 'PENDING', annualIncome: 120000, creditScore: 780, appliedAt: '1 day ago' },
      { id: 'a4', applicantName: 'Amanda White', applicantEmail: 'a.white@email.com', cardType: 'VISA Signature', status: 'PENDING', annualIncome: 95000, creditScore: 740, appliedAt: '2 days ago' },
      { id: 'a5', applicantName: 'Kevin Park', applicantEmail: 'k.park@email.com', cardType: 'MASTERCARD Standard', status: 'PENDING', annualIncome: 45000, creditScore: 640, appliedAt: '3 days ago' },
    ]);
  }

  getConversations(): Observable<AgentConversation[]> {
    return of([
      { id: 'conv1', title: 'Balance Inquiry - John Smith', agentType: 'Customer Support', status: 'Active', messageCount: 12, lastActivity: '2 min ago', messages: [
        { role: 'user', content: 'What is my current balance?', timestamp: '10:30 AM' },
        { role: 'agent', content: 'Your current balance is $12,450.32', timestamp: '10:30 AM' },
        { role: 'user', content: 'Thank you!', timestamp: '10:31 AM' },
      ]},
      { id: 'conv2', title: 'Fraud Alert - Card Transaction', agentType: 'Fraud Detection', status: 'Active', messageCount: 8, lastActivity: '15 min ago', messages: [
        { role: 'user', content: 'I did not authorize this transaction', timestamp: '9:45 AM' },
        { role: 'agent', content: 'I have flagged the transaction for review', timestamp: '9:46 AM' },
      ]},
      { id: 'conv3', title: 'Loan Application Assistance', agentType: 'Loan Advisor', status: 'Resolved', messageCount: 24, lastActivity: '1 hour ago', messages: [
        { role: 'user', content: 'I need help with my loan application', timestamp: '8:00 AM' },
        { role: 'agent', content: 'I can help you with that. What type of loan?', timestamp: '8:01 AM' },
      ]},
      { id: 'conv4', title: 'Credit Limit Increase Request', agentType: 'Customer Support', status: 'Active', messageCount: 6, lastActivity: '30 min ago', messages: [
        { role: 'user', content: 'I would like to request a credit limit increase', timestamp: '11:00 AM' },
      ]},
      { id: 'conv5', title: 'Investment Portfolio Review', agentType: 'Financial Advisor', status: 'Pending', messageCount: 15, lastActivity: '3 hours ago', messages: [
        { role: 'user', content: 'Can you review my portfolio performance?', timestamp: '7:30 AM' },
      ]},
    ]);
  }

  getKnowledgeDocs(): Observable<KnowledgeDoc[]> {
    return of([
      { id: 'k1', title: 'Account Opening Procedures', type: 'PDF', lastUpdated: '2025-05-10', size: '2.4 MB' },
      { id: 'k2', title: 'Fraud Detection Guidelines', type: 'PDF', lastUpdated: '2025-04-28', size: '1.8 MB' },
      { id: 'k3', title: 'KYC Compliance Handbook', type: 'PDF', lastUpdated: '2025-06-01', size: '3.2 MB' },
      { id: 'k4', title: 'Loan Processing Workflow', type: 'DOCX', lastUpdated: '2025-05-15', size: '1.1 MB' },
      { id: 'k5', title: 'Customer Support Scripts', type: 'PDF', lastUpdated: '2025-06-10', size: '0.9 MB' },
      { id: 'k6', title: 'Anti-Money Laundering Policy', type: 'PDF', lastUpdated: '2025-05-22', size: '4.5 MB' },
    ]);
  }

  getRecentActivity(): Observable<{ type: string; message: string; time: string }[]> {
    return of([
      { type: 'user', message: 'New user registered: Amanda White', time: '5 min ago' },
      { type: 'card', message: 'Card application submitted by Kevin Park', time: '12 min ago' },
      { type: 'transaction', message: 'Large transaction flagged: $25,000 transfer', time: '18 min ago' },
      { type: 'user', message: 'User account locked: Michael Brown', time: '25 min ago' },
      { type: 'card', message: 'Card status changed: Jessica Taylor - Active', time: '35 min ago' },
      { type: 'agent', message: 'Fraud alert conversation resolved', time: '45 min ago' },
      { type: 'user', message: 'Email verified: Sarah Davis', time: '1 hour ago' },
      { type: 'transaction', message: 'International transfer completed: $3,200', time: '1 hour ago' },
    ]);
  }

  getAgentActions(): Observable<AdminActivity[]> {
    return of([
      { id: 'act1', action: 'Approved card application', admin: 'Admin', target: 'VISA Platinum - A. Turner', timestamp: '10 min ago' },
      { id: 'act2', action: 'Froze card', admin: 'Admin', target: 'Mastercard ****7821', timestamp: '25 min ago' },
      { id: 'act3', action: 'Updated user role', admin: 'Admin', target: 'M. Brown → Premium', timestamp: '40 min ago' },
      { id: 'act4', action: 'Unlocked account', admin: 'Admin', target: 'User: Lisa Martinez', timestamp: '1 hour ago' },
      { id: 'act5', action: 'Rejected application', admin: 'Admin', target: 'AMEX - K. Park (low credit)', timestamp: '2 hours ago' },
    ]);
  }

  updateRole(userId: string, role: string): Observable<any> {
    return of({ success: true });
  }

  toggleUserStatus(userId: string, currentStatus: string): Observable<any> {
    return of({ success: true });
  }

  approveApplication(id: string): Observable<any> {
    return of({ success: true });
  }

  rejectApplication(id: string, reason: string): Observable<any> {
    return of({ success: true });
  }

  updateCardStatus(id: string, status: string): Observable<any> {
    return of({ success: true });
  }
}

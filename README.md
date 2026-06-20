# NovaBank - Enterprise AI-Powered Banking Platform

> **Next-Generation Banking Platform** with Microservices, LangChain4j AI Agents, RAG, MCP, Vector Database, Credit Cards, and Real-Time Processing

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                    Angular 19 Banking UI (:4200)                     │
│         User Dashboard  |  Admin Dashboard  |  AI Chat              │
├──────────────────────────────────────────────────────────────────────┤
│                  API Gateway - Spring Cloud Gateway (:8080)           │
│              Rate Limiting (Redis) + JWT Validation                   │
├──────┬──────┬──────┬──────┬──────┬──────┬──────┬──────┬─────────────┤
│ Auth │ Acct │ Trans│ Card │ Agent│ Notif│ Loan │ Admin│             │
│ :8081│ :8083│ :8085│ :8089│ :8095│ :8087│  MS  │  MS  │             │
├──────┴──────┴──────┴──────┴──────┴──────┴──────┴──────┴─────────────┤
│ PostgreSQL 16    │ MongoDB 7    │ Redis 7    │ ChromaDB (Vector)    │
├──────────────────────────────────────────────────────────────────────┤
│ Kafka (Event Stream) │ Ollama (Llama 3.2 + nomic-embed-text)        │
│ LangChain4j Agents  │ RAG Pipeline │ MCP Tools                     │
├──────────────────────────────────────────────────────────────────────┤
│ Docker Compose │ GitHub Actions CI/CD                                │
└──────────────────────────────────────────────────────────────────────┘
```

## Features

### User Features
- **Multi-Factor Authentication** - JWT-based auth with refresh tokens, account lockout
- **Account Management** - Checking, Savings, Business, Investment, Fixed Deposit accounts
- **Real-Time Transactions** - Instant transfers with Kafka event streaming + SSE updates
- **Credit Card Services** - Apply, activate, freeze/unfreeze, payments, statements
- **Virtual Credit Cards** - Visa, Mastercard, AMEX, RuPay with reward points
- **Loan Management** - Personal, Home, Auto, Education, Business loans with EMI calculator
- **AI-Powered Assistant** - Conversational AI with RAG knowledge base + Agent mode
- **Financial Insights** - AI-generated personalized financial recommendations
- **Real-Time Notifications** - SSE-based instant notifications for all banking events
- **Profile Management** - Personal info, security settings, notification preferences

### Admin Features
- **Admin Dashboard** - System overview, stats, activity feed, quick actions
- **User Management** - View all users, manage roles, toggle account status
- **Card Administration** - View all cards, approve/reject applications, manage limits
- **Transaction Monitoring** - View all transactions, flag suspicious activity
- **AI Agent Monitor** - View conversations, track agent actions, manage knowledge base
- **System Settings** - Configure security, notifications, AI parameters

### AI & Intelligence Features
- **LangChain4j Agentic AI** - Java-based AI agents with tool-calling capabilities
- **Retrieval-Augmented Generation (RAG)** - Documents stored in ChromaDB vector store
- **Ollama Integration** - Local LLM (Llama 3.2) for complete data privacy
- **MCP (Model Context Protocol)** - Standardized tool interface for AI models
- **Agent Personas** - General, Financial Advisor, Fraud Analyst, Loan Officer, Card Specialist
- **Automated Banking Tools** - Account balance, transactions, card info, spending analysis
- **ChromaDB Vector Store** - Persistent, scalable vector database for RAG

## Tech Stack

| Category | Technology |
|----------|-----------|
| **Backend** | Java 21, Spring Boot 3.4.5, Spring Cloud Gateway |
| **Frontend** | Angular 19, Angular Material, RxJS |
| **Database** | PostgreSQL 16, MongoDB 7, Redis 7 |
| **Vector DB** | ChromaDB (Open Source) |
| **AI/ML** | Ollama (Llama 3.2), LangChain4j, RAG Pipeline |
| **Protocol** | MCP (Model Context Protocol) |
| **Streaming** | Apache Kafka, Server-Sent Events (SSE) |
| **Auth** | JWT (HMAC-SHA256), BCrypt, Redis Sessions |
| **DevOps** | Docker, Docker Compose, GitHub Actions |

## Project Structure

```
ai-banking-platform/
├── backend/                          # Java Spring Boot Microservices
│   ├── pom.xml                       # Parent Maven POM (multi-module)
│   ├── auth-service/                 # Authentication (port 8081)
│   ├── account-service/              # Account management (port 8083, MongoDB)
│   ├── transaction-service/          # Transaction processing (port 8085)
│   ├── notification-service/         # Notifications + SSE (port 8087)
│   ├── card-service/                 # Credit cards (port 8089) [NEW]
│   ├── agent-service/                # LangChain4j AI agents (port 8095) [NEW]
│   └── api-gateway/                  # Spring Cloud Gateway (port 8080)
│
├── frontend/                         # Angular 19 Web Application
│   └── src/app/
│       ├── core/                     # Auth, guards, interceptors, services, models
│       ├── auth/                     # Login & Register components
│       ├── dashboard/                # User dashboard
│       ├── accounts/                 # Account management
│       ├── transactions/             # Transaction history
│       ├── cards/                    # Credit card management [NEW]
│       ├── loans/                    # Loan management [NEW]
│       ├── profile/                  # User profile [NEW]
│       ├── admin/                    # Admin dashboard, users, cards, agent, settings [NEW]
│       ├── ai/                       # Enhanced AI chat with agent mode [UPDATED]
│       └── notifications/           # Notification center
│
├── ai/                               # Python AI Service (legacy, replaced by agent-service)
│   ├── ai-service/                   # FastAPI AI service
│   └── mcp-server/                   # MCP tool definitions
│
├── docker/
│   └── postgres/init/               # Database initialization scripts
│
├── docker-compose.yml                # All services + ChromaDB
├── .env.example                      # Environment variables
└── README.md
```

## Quick Start

### Prerequisites

```bash
# Required installations
- Java 21+         # java -version
- Maven 3.9+       # mvn -version
- Node.js 22+      # node --version
- Angular CLI 19+  # ng version
- Docker Desktop   # docker --version
- Ollama           # ollama list
- Git              # git --version
```

### 1. Clone & Install

```bash
git clone <your-repo-url>
cd ai-banking-platform

# Install Ollama models
ollama pull llama3.2
ollama pull nomic-embed-text

# Backend: Build all services
cd backend
mvn clean install -DskipTests
cd ..

# Frontend: Install dependencies
cd frontend
npm install
cd ..
```

### 2. Start Infrastructure

```bash
# Start databases, message broker, vector DB, and monitoring
docker-compose up -d postgres redis mongodb chromadb kafka redis-commander mongo-express kafka-ui
```

This starts:
- **PostgreSQL** (:5432) - All transaction databases
- **Redis** (:6379) - Caching, rate limiting, sessions
- **MongoDB** (:27017) - Account documents
- **ChromaDB** (:8000) - Vector embeddings for RAG
- **Kafka** (:9092) - Event streaming
- **Redis Commander** (:8082) - Redis UI
- **Mongo Express** (:8084) - MongoDB UI
- **Kafka UI** (:8086) - Kafka management

### 3. Run Microservices

```bash
# Terminal 1: Auth Service
cd backend && mvn spring-boot:run -pl auth-service

# Terminal 2: Account Service
cd backend && mvn spring-boot:run -pl account-service

# Terminal 3: Transaction Service
cd backend && mvn spring-boot:run -pl transaction-service

# Terminal 4: Notification Service
cd backend && mvn spring-boot:run -pl notification-service

# Terminal 5: Card Service [NEW]
cd backend && mvn spring-boot:run -pl card-service

# Terminal 6: Agent Service [NEW]
cd backend && mvn spring-boot:run -pl agent-service

# Terminal 7: API Gateway
cd backend && mvn spring-boot:run -pl api-gateway
```

### 4. Run Frontend

```bash
cd frontend && ng serve -o
```

Access the app at **http://localhost:4200**

### 5. Docker (All Services)

```bash
docker-compose up -d --build
```

## API Endpoints

### Auth Service (:8081)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/refresh` | Refresh token |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Current user info |
| GET | `/api/admin/users` | List users (admin) |
| GET | `/api/admin/users/{id}` | Get user (admin) |
| PATCH | `/api/admin/users/{id}/role` | Update role (admin) |

### Card Service (:8089) [NEW]
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/cards/apply` | Apply for credit card |
| GET | `/api/cards/my-cards` | Get user's cards |
| GET | `/api/cards/{id}` | Get card details |
| GET | `/api/cards/{id}/transactions` | Card transactions |
| GET | `/api/cards/{id}/statement` | Latest statement |
| POST | `/api/cards/{id}/activate` | Activate card |
| POST | `/api/cards/{id}/freeze` | Freeze card |
| POST | `/api/cards/{id}/make-payment` | Make payment |
| POST | `/api/cards/transactions/{id}/dispute` | Dispute transaction |

### Agent Service (:8095) [NEW]
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/agent/chat` | AI agent chat |
| POST | `/api/agent/analyze` | Analyze financial data |
| GET | `/api/agent/tools` | List MCP tools |
| GET | `/api/agent/conversations` | User conversations |
| GET | `/api/admin/agent/stats` | Agent usage stats (admin) |
| POST | `/api/admin/agent/knowledge/upload` | Upload KB document (admin) |

## AI & Agent System

### How It Works

1. **LangChain4j + Ollama**: The agent-service uses LangChain4j (Java AI framework) with Ollama (Llama 3.2) for LLM inference. All data stays local - no external API calls.

2. **RAG with ChromaDB**: Banking knowledge base (FAQs, policies, procedures) is stored as vector embeddings in ChromaDB. When a user asks a question, relevant documents are retrieved and provided as context to the LLM.

3. **MCP Tools**: The agent has access to 11 banking tools via Model Context Protocol:
   - Account information (balance, list accounts)
   - Transaction history (recent, by account, stats)
   - Card details (info, credit, rewards)
   - Financial analysis (spending patterns, loan eligibility)
   - Fraud detection (anomaly detection)

4. **Agent Personas**: 5 specialized agent types:
   - **GENERAL** - Helpful banking assistant
   - **FINANCIAL_ADVISOR** - Investment and savings advice
   - **FRAUD_ANALYST** - Transaction monitoring and anomaly detection
   - **LOAN_OFFICER** - Loan processing and eligibility
   - **CARD_SPECIALIST** - Credit card services

5. **Frontend Toggle**: Users can toggle between standard AI chat (with RAG) and Agent Mode (with tool execution) directly in the chat interface.

## Credit Card System

The card-service implements a complete credit card management system:

- **Virtual Card Design**: Visa (gold), Mastercard (blue), AMEX (green), RuPay (orange)
- **Card Lifecycle**: Apply -> Submit -> Under Review -> Approved/Rejected -> Activate -> Active -> Freeze/Lost/Cancelled
- **Billing**: Monthly statement generation, minimum payment calculation, due dates
- **Rewards**: 1 point per $100 (2x on travel/dining), automatic tracking
- **Security**: CVV validation, PIN management, fraud dispute, freeze/unfreeze
- **Payments**: One-time and auto-pay from linked accounts

## Roadmap

### Phase 1 - Foundation
- Multi-module Maven project with Spring Boot 3.4
- Auth service with JWT + Redis
- API Gateway with rate limiting
- Angular 19 frontend with dark theme

### Phase 2 - Core Banking
- Account management with MongoDB
- Transaction processing with Kafka
- Real-time SSE notifications
- Responsive dashboard with charts

### Phase 3 - Cards & AI [YOU ARE HERE]
- **Credit Card Service** - Full lifecycle management
- **LangChain4j Agent Service** - AI agents with tool calling
- **ChromaDB Vector Store** - Persistent RAG knowledge base
- **Admin Dashboard** - Complete system management
- **Agent Mode** - AI-powered banking automation
- **Enhanced Frontend** - Virtual cards, loans, profile pages

### Phase 4 - Production
- AWS deployment (ECS/RDS/ElastiCache)
- Monitoring with Prometheus/Grafana
- Load testing and optimization
- Security audit

## Screenshots

| Feature | Description |
|---------|-------------|
| **Login** | Dark theme login with SSO options |
| **Dashboard** | Stats cards, accounts, transactions, AI insights |
| **Accounts** | Account cards with balances and management |
| **Transactions** | Transfer funds with real-time updates |
| **Credit Cards** | Virtual card designs, management, payments |
| **Card Detail** | Tabs for overview, transactions, statements, settings |
| **Card Apply** | Multi-step application with card selection |
| **Loans** | Active loans, eligibility, apply |
| **Admin Dashboard** | System stats, activity feed, user charts |
| **Admin Users** | User management with role controls |
| **Admin Cards** | Card oversight and application processing |
| **AI Chat** | Intelligent assistant with RAG + Agent mode |
| **Profile** | Personal info, security, preferences |

## Testing

```bash
# Backend unit tests
cd backend && mvn test

# Frontend tests
cd frontend && ng test

# Full integration test
docker-compose up -d --build
```

## License

MIT License - see LICENSE file for details.

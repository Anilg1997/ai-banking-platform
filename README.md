# 🏦 NovaBank - Enterprise Banking Platform

> **Next-Generation Banking Platform** with Microservices, AI Agents, RAG, and Modern Architecture

## 🌟 Tech Stack

### Backend
| Technology | Purpose |
|------------|---------|
| **Java 21 + Spring Boot 3.4** | Microservices framework |
| **Spring Cloud Gateway** | API Gateway & routing |
| **Spring Security + JWT** | Authentication & authorization |
| **PostgreSQL 16** | Primary relational database |
| **MongoDB 7** | Document store (Phase 2) |
| **Redis 7** | Caching, session management, rate limiting |
| **Apache Kafka** | Event streaming (Phase 2) |
| **Docker + Docker Compose** | Containerization & local dev |

### Frontend
| Technology | Purpose |
|------------|---------|
| **Angular 19** | Frontend framework |
| **Angular Material** | UI component library |
| **RxJS + NgRx** | State management |
| **Chart.js / ngx-charts** | Data visualization |

### AI & Intelligence
| Technology | Purpose |
|------------|---------|
| **Ollama (Llama 3.2)** | Local LLM for AI features |
| **RAG Pipeline** | Retrieval-Augmented Generation |
| **AI Agents** | Autonomous banking assistants |
| **MCP (Model Context Protocol)** | AI tool integration |

### DevOps
| Technology | Purpose |
|------------|---------|
| **GitHub Actions** | CI/CD pipeline |
| **Docker** | Containerization |
| **AWS (future)** | Cloud deployment |

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                     Angular Banking UI                       │
│                (http://localhost:4200)                       │
├──────────────────────────────────────────────────────────────┤
│              API Gateway (Port 8080)                         │
│          Spring Cloud Gateway + Rate Limiting                │
├──────┬──────┬──────┬──────┬──────┬──────┬──────┬────────────┤
│ Auth │Account│Trans │Loan  │ Fraud│Notif │Report│   Admin   │
│ MS   │ MS   │ MS   │ MS   │Detect│ MS   │ MS   │   MS      │
├──────┴──────┴──────┴──────┴──────┴──────┴──────┴────────────┤
│  PostgreSQL 16 (Core)  │  MongoDB 7 (Docs/Logs)              │
│  Redis 7 (Cache/Session/Sessions)│  Kafka (Event Stream)     │
│  Ollama (Llama 3.2 + Embeddings) │  RAG Pipeline             │
├──────────────────────────────────────────────────────────────┤
│  Docker Compose (Local Dev)  │  AWS / K8s (Production)       │
└──────────────────────────────────────────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

```bash
# Required installations
- Java 21+         # java -version
- Maven 3.9+       # mvn -version
- Node.js 22+      # node --version
- Angular CLI 19+  # ng version
- Docker Desktop   # docker --version
- Ollama           # ollama list (should show models)
- Git              # git --version
```

### 1️⃣ Clone & Setup

```bash
cd banking-platform

# Backend: Build all services
cd backend
mvn clean install -DskipTests
cd ..

# Frontend: Install dependencies
cd frontend
npm install
cd ..
```

### 2️⃣ Infrastructure (Docker)

```bash
# Start databases and services
docker-compose up -d postgres redis
```

This starts:
- **PostgreSQL** on port `5432`
- **Redis** on port `6379`
- **Redis Commander** (UI) on port `8082`

### 3️⃣ Run Microservices

```bash
# Terminal 1: Auth Service
cd backend
mvn spring-boot:run -pl auth-service

# Terminal 2: API Gateway
cd backend
mvn spring-boot:run -pl api-gateway
```

### 4️⃣ Run Frontend

```bash
cd frontend
ng serve -o
```

Access the app at **http://localhost:4200**

---

## 📁 Project Structure

```
banking-platform/
├── backend/
│   ├── pom.xml                    # Parent Maven POM
│   ├── auth-service/              # Authentication microservice
│   │   ├── src/main/java/.../
│   │   │   ├── config/            # Security, Redis config
│   │   │   ├── controller/        # Auth REST endpoints
│   │   │   ├── dto/               # Request/Response DTOs
│   │   │   ├── model/             # User entity
│   │   │   ├── repository/        # JPA repository
│   │   │   ├── security/          # JWT provider & filter
│   │   │   └── service/           # Business logic
│   │   └── src/main/resources/    # application.yml
│   └── api-gateway/               # Spring Cloud Gateway
├── frontend/
│   ├── src/app/
│   │   ├── core/                  # Auth service, guards, interceptors
│   │   ├── auth/                  # Login & Register components
│   │   └── dashboard/             # Dashboard component
│   ├── src/styles.scss            # Global banking theme
│   └── package.json
├── ai/                            # AI Agent & RAG pipeline (Phase 3)
├── docker-compose.yml             # All services
├── .github/workflows/             # CI/CD pipeline
└── README.md
```

---

## 🔌 API Endpoints

### Auth Service (Port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/refresh` | Refresh access token |
| POST | `/api/auth/logout` | User logout |
| GET | `/api/auth/validate` | Validate token |
| GET | `/api/auth/me` | Get current user |
| GET | `/actuator/health` | Health check |

### API Gateway (Port 8080)

Routes all `/api/auth/**` requests to Auth Service with rate limiting.

---

## 🤖 AI Features (Coming in Phase 3)

### RAG Chatbot
- Retrieves banking knowledge from document store
- Powered by Ollama (Llama 3.2) + embeddings
- Answers customer queries intelligently

### AI Agents
- **Fraud Detection Agent** - Real-time transaction monitoring
- **Loan Approval Agent** - Automated underwriting with document analysis
- **Financial Advisor Agent** - Personalized investment advice

### MCP Servers
- Expose banking tools/APIs as MCP resources
- Enable AI models to query accounts, transactions, balances
- Securely controlled through the API Gateway

---

## 🧪 Testing

```bash
# Backend tests
cd backend
mvn test

# Frontend tests
cd frontend
ng test
```

---

## 🚢 Deployment

### Local (Docker)
```bash
docker-compose up -d --build
```

### AWS (Future)
See `/docs/deployment-aws.md` for production deployment guide.

---

## 📋 Phase Roadmap

### ✅ Phase 1: Foundation
- [x] Project structure & monorepo setup
- [x] Auth Service (Register/Login with JWT)
- [x] API Gateway with rate limiting
- [x] Docker Compose (PostgreSQL + Redis)
- [x] Angular frontend with banking UI
- [x] Register, Login, Dashboard pages
- [x] GitHub Actions CI/CD
- [x] Local Ollama integration ready

### ✅ Phase 2: Core Banking
- [x] Account Service (CRUD, balances)
- [x] Transaction Service (send, receive, history)
- [x] Kafka event streaming setup
- [x] MongoDB for documents
- [x] Redis caching layer
- [x] More UI screens (accounts, transactions, account detail, notifications)
- [x] Real-time SSE updates for transactions & notifications
- [x] Notification Service with Kafka consumer

### ✅ Phase 3: AI & Intelligence
- [x] RAG pipeline with Ollama
- [x] AI-powered chat assistant with knowledge base (30 FAQ entries)
- [x] AI financial insights & analysis
- [x] MCP server implementation (banking tools)
- [x] AI-powered UI components (chat widget, insights card)

### 🔄 Phase 4: Production Ready
- [x] Full CI/CD pipeline (GitHub Actions)
- [x] Docker images for all services
- [ ] AWS infrastructure (ECS/RDS/ElastiCache/MSK)
- [ ] Monitoring & observability
- [ ] Load testing & optimization
- [ ] Security audit

---

## 👨‍💻 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

## 🙏 Acknowledgments

- Built with Spring Boot, Angular, and modern AI technologies
- Ollama for local LLM capabilities
- Open source community

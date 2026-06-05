"""AI Service Configuration."""

import os

# Ollama Configuration
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://host.docker.internal:11434")
OLLAMA_CHAT_MODEL = os.getenv("OLLAMA_CHAT_MODEL", "llama3.2")
OLLAMA_EMBED_MODEL = os.getenv("OLLAMA_EMBED_MODEL", "nomic-embed-text")
OLLAMA_REQUEST_TIMEOUT = int(os.getenv("OLLAMA_REQUEST_TIMEOUT", "60"))

# API Configuration
AI_SERVICE_PORT = int(os.getenv("AI_SERVICE_PORT", "8090"))
AI_SERVICE_HOST = os.getenv("AI_SERVICE_HOST", "0.0.0.0")

# CORS
CORS_ORIGINS = os.getenv("CORS_ORIGINS", "http://localhost:4200,http://localhost:8080")

# Banking Service URLs (for MCP tools)
ACCOUNT_SERVICE_URL = os.getenv("ACCOUNT_SERVICE_URL", "http://localhost:8083")
TRANSACTION_SERVICE_URL = os.getenv("TRANSACTION_SERVICE_URL", "http://localhost:8085")
AUTH_SERVICE_URL = os.getenv("AUTH_SERVICE_URL", "http://localhost:8081")

# Vector DB config
VECTOR_DIMENSION = 768  # nomic-embed-text outputs 768-dim vectors
SIMILARITY_TOP_K = 3

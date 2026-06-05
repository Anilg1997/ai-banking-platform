# 🤖 NovaBank AI - RAG, Agents & MCP

> AI Infrastructure for the Enterprise Banking Platform

## Overview

This directory contains the AI components for NovaBank:
- **RAG Pipeline** - Context-aware banking chatbot
- **AI Agents** - Autonomous banking assistants
- **MCP Server** - Model Context Protocol for AI tool integration

## Prerequisites

```bash
# Ollama is already installed with these models:
ollama list
# Should show:
# - llama3.2:latest (2.0 GB)
# - nomic-embed-text:latest (274 MB)
```

## Quick Start

### 1. Test Ollama

```bash
# Test basic LLM
ollama run llama3.2 "What is your role?"

# Test embeddings
curl http://localhost:11434/api/embeddings -d '{
  "model": "nomic-embed-text",
  "prompt": "Banking query here"
}'
```

### 2. RAG Pipeline Setup (Coming in Phase 3)

```
ai/
├── rag/
│   ├── embeddings/       # Document chunking & embedding
│   ├── vector-store/     # PGVector or Chroma integration
│   ├── retrieval/        # Semantic search
│   └── generation/       # LLM response generation
├── agents/
│   ├── fraud-detection/  # Real-time fraud monitoring
│   ├── loan-underwriter/ # Automated loan processing
│   └── financial-advisor/# Investment advice
└── mcp-server/           # MCP protocol implementation
```

### 3. API Integration

The AI services integrate with the backend through:
- REST APIs via API Gateway
- Kafka event streams for real-time processing
- WebSocket for chat interactions

## MCP (Model Context Protocol)

MCP enables AI models to securely interact with banking tools:

```python
# Example MCP Tool Definition
@mcp.tool
def get_account_balance(account_id: str) -> dict:
    """Get the current balance for a bank account"""
    return {"balance": 10000, "currency": "USD"}

@mcp.tool
def analyze_spending(user_id: str, period: str) -> dict:
    """Analyze spending patterns for a user"""
    return {"categories": [...], "total": ...}
```

## Resources

- [Ollama Documentation](https://ollama.ai/docs)
- [MCP Specification](https://modelcontextprotocol.io)
- [LangChain Python](https://python.langchain.com)
- [Spring AI](https://docs.spring.io/spring-ai/reference/)

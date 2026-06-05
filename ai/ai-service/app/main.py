"""NovaBank AI Service - Main Application.

Provides AI-powered banking assistant with RAG, chat, insights, and MCP tools.
Integrates with Ollama for local LLM capabilities.
"""

import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.config import AI_SERVICE_HOST, AI_SERVICE_PORT, CORS_ORIGINS
from app.routers import chat, insights, tools
from app.services.rag_service import initialize_knowledge_base

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(name)-30s | %(levelname)-6s | %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Application lifespan - initialize resources on startup."""
    logger.info("🚀 NovaBank AI Service starting...")

    # Initialize RAG knowledge base
    try:
        await initialize_knowledge_base()
        logger.info("✅ Knowledge base initialized")
    except Exception as e:
        logger.warning(f"⚠️ Knowledge base initialization failed: {e}")

    yield

    logger.info("👋 NovaBank AI Service shutting down...")


app = FastAPI(
    title="NovaBank AI Service",
    description="AI-powered banking assistant with RAG, chat, insights, and MCP tools",
    version="3.0.0",
    lifespan=lifespan,
)

# CORS
origins = [o.strip() for o in CORS_ORIGINS.split(",")]
app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(chat.router)
app.include_router(insights.router)
app.include_router(tools.router)


@app.get("/api/ai/health")
async def root_health():
    """Root health check endpoint."""
    return {
        "service": "NovaBank AI Service",
        "version": "3.0.0",
        "status": "running",
    }


@app.get("/")
async def root():
    return {
        "service": "NovaBank AI Service",
        "docs": "/docs",
        "endpoints": {
            "chat": "/api/ai/chat",
            "insights": "/api/ai/insights",
            "tools": "/api/ai/tools/list",
        }
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host=AI_SERVICE_HOST, port=AI_SERVICE_PORT)

"""Chat router for the banking AI assistant."""

import json
import logging
from typing import Optional

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.services.ollama_service import chat_completion, check_ollama_health
from app.services.rag_service import get_rag_context, initialize_knowledge_base

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/ai/chat", tags=["chat"])


class ChatMessage(BaseModel):
    role: str = Field(..., pattern="^(user|assistant|system)$")
    content: str = Field(..., min_length=1, max_length=4000)


class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=2000)
    history: list[ChatMessage] = Field(default_factory=list)
    use_rag: bool = Field(default=True, description="Whether to use RAG for context")


class ChatResponse(BaseModel):
    reply: str
    sources: list[str] = Field(default_factory=list)
    used_rag: bool = False


SYSTEM_PROMPT = """You are NovaBank AI Assistant, a helpful and professional banking assistant for NovaBank Enterprise Banking Platform.

Your capabilities:
- Answer questions about NovaBank products, services, and policies
- Provide financial advice and guidance
- Help users understand banking concepts
- Explain account types, fees, and interest rates
- Guide users through common banking tasks

Guidelines:
- Be professional, friendly, and concise
- Use the provided context when available to give accurate answers
- If you don't know something, say so honestly
- Never ask for sensitive information like passwords or PINs
- Encourage users to use official channels for sensitive operations
- Format responses with clear structure for readability

When you use information from provided context, reference it naturally in your response."""



@router.post("", response_model=ChatResponse)
async def chat(request: ChatRequest):
    """Send a message to the banking AI assistant with optional RAG context."""
    # Check if Ollama is available
    health = await check_ollama_health()
    if not health.get("ollama"):
        raise HTTPException(
            status_code=503,
            detail="Ollama service is not available. Please ensure Ollama is running.",
        )

    # Get RAG context if enabled
    context = ""
    sources = []
    used_rag = False

    if request.use_rag:
        context = await get_rag_context(request.message)
        if context:
            used_rag = True
            logger.info(f"RAG context retrieved for: {request.message[:50]}...")

    # Build messages
    messages = [m.model_dump() for m in request.history]
    messages.append({"role": "user", "content": request.message})

    # Build system prompt with context
    system = SYSTEM_PROMPT
    if context:
        system += f"\n\nRelevant Banking Information:\n{context}"

    try:
        response = await chat_completion(
            messages=messages,
            system_prompt=system,
            temperature=0.7,
            max_tokens=512,
        )

        reply = response.get("message", {}).get("content", "")
        if not reply:
            reply = "I apologize, but I wasn't able to generate a response. Please try again."

        return ChatResponse(
            reply=reply,
            sources=sources,
            used_rag=used_rag,
        )

    except Exception as e:
        logger.error(f"Chat completion failed: {e}")
        raise HTTPException(
            status_code=500,
            detail="Failed to generate response. Please ensure Ollama is running with llama3.2.",
        )


@router.get("/health")
async def health_check():
    """Check AI service health including Ollama connection."""
    health = await check_ollama_health()

    # Count knowledge base
    from app.services.rag_service import vector_store

    return {
        "status": "healthy" if health.get("ollama") else "degraded",
        "ollama": health,
        "knowledge_base": {
            "documents": vector_store.count,
            "initialized": vector_store.initialized,
        },
    }

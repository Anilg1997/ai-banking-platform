"""Service for interacting with Ollama API."""

import json
import logging
from typing import Optional

import httpx
from app.config import (
    OLLAMA_BASE_URL,
    OLLAMA_CHAT_MODEL,
    OLLAMA_EMBED_MODEL,
    OLLAMA_REQUEST_TIMEOUT,
)

logger = logging.getLogger(__name__)


async def chat_completion(
    messages: list[dict],
    model: str = OLLAMA_CHAT_MODEL,
    temperature: float = 0.7,
    max_tokens: int = 1024,
    stream: bool = False,
    system_prompt: Optional[str] = None,
) -> dict:
    """Send a chat completion request to Ollama."""
    if system_prompt:
        messages = [{"role": "system", "content": system_prompt}] + messages

    payload = {
        "model": model,
        "messages": messages,
        "temperature": temperature,
        "max_tokens": max_tokens,
        "stream": stream,
    }

    logger.debug(f"Sending chat request to Ollama: model={model}")

    async with httpx.AsyncClient(timeout=OLLAMA_REQUEST_TIMEOUT) as client:
        response = await client.post(
            f"{OLLAMA_BASE_URL}/api/chat",
            json=payload,
        )
        response.raise_for_status()
        result = response.json()

    logger.debug(f"Ollama response received: tokens={result.get('eval_count', 'N/A')}")
    return result


async def generate_embeddings(text: str) -> list[float]:
    """Generate embeddings for a text using Ollama."""
    payload = {
        "model": OLLAMA_EMBED_MODEL,
        "prompt": text,
    }

    async with httpx.AsyncClient(timeout=OLLAMA_REQUEST_TIMEOUT) as client:
        response = await client.post(
            f"{OLLAMA_BASE_URL}/api/embeddings",
            json=payload,
        )
        response.raise_for_status()
        result = response.json()

    return result.get("embedding", [])


async def check_ollama_health() -> dict:
    """Check if Ollama is running and models are available."""
    results = {"ollama": False, "chat_model": False, "embed_model": False}

    try:
        async with httpx.AsyncClient(timeout=5) as client:
            resp = await client.get(f"{OLLAMA_BASE_URL}/api/tags")
            resp.raise_for_status()
            models = resp.json().get("models", [])
            results["ollama"] = True

            for m in models:
                name = m.get("name", "")
                if name.startswith(OLLAMA_CHAT_MODEL):
                    results["chat_model"] = True
                if name.startswith(OLLAMA_EMBED_MODEL):
                    results["embed_model"] = True

    except Exception as e:
        logger.warning(f"Ollama health check failed: {e}")

    return results


def format_context(documents: list[dict]) -> str:
    """Format retrieved documents into a context string for the LLM."""
    if not documents:
        return ""

    context_parts = []
    for i, doc in enumerate(documents, 1):
        content = doc.get("content", doc.get("text", ""))
        source = doc.get("source", doc.get("category", "unknown"))
        context_parts.append(f"[{i}] From {source}: {content}")

    return "\n\n".join(context_parts)

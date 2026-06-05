"""Insights router for AI-powered financial analysis."""

import logging
import random
from typing import Optional

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.services.ollama_service import chat_completion, check_ollama_health
from app.services.rag_service import BANKING_INSIGHTS

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/ai/insights", tags=["insights"])


class InsightResponse(BaseModel):
    insights: list[dict]


@router.get("", response_model=InsightResponse)
async def get_financial_insights(user_id: Optional[str] = None):
    """Get AI-powered financial insights and recommendations."""
    # In production, these would be personalized based on user data
    # For now, return curated insights from our knowledge base
    insights = [
        {
            "id": "insight-savings",
            "type": "opportunity",
            "title": "Boost Your Savings",
            "message": BANKING_INSIGHTS["savings"],
            "icon": "savings",
            "color": "green",
            "action": {"label": "Set Up Auto-Save", "link": "/accounts"},
        },
        {
            "id": "insight-investing",
            "type": "tip",
            "title": "Portfolio Insight",
            "message": BANKING_INSIGHTS["investing"],
            "icon": "trending_up",
            "color": "gold",
            "action": {"label": "View Portfolio", "link": "/accounts"},
        },
        {
            "id": "insight-emergency",
            "type": "alert",
            "title": "Emergency Fund Check",
            "message": BANKING_INSIGHTS["emergency"],
            "icon": "shield",
            "color": "blue",
            "action": {"label": "Start Saving", "link": "/accounts"},
        },
        {
            "id": "insight-budgeting",
            "type": "tip",
            "title": "Budgeting Tip",
            "message": BANKING_INSIGHTS["budgeting"],
            "icon": "account_balance_wallet",
            "color": "purple",
            "action": {"label": "Create Budget", "link": "/accounts"},
        },
        {
            "id": "insight-retirement",
            "type": "opportunity",
            "title": "Retirement Planning",
            "message": BANKING_INSIGHTS["retirement"],
            "icon": "elderly",
            "color": "green",
            "action": {"label": "Calculate", "link": "/accounts"},
        },
    ]

    return InsightResponse(insights=insights)


class AnalyzeRequest(BaseModel):
    data: str = Field(..., min_length=1, max_length=5000)
    analysis_type: str = Field(default="spending", pattern="^(spending|savings|investing|general)$")


@router.post("/analyze")
async def analyze_finances(request: AnalyzeRequest):
    """Analyze financial data using AI."""
    health = await check_ollama_health()
    if not health.get("ollama"):
        raise HTTPException(status_code=503, detail="Ollama service is unavailable")

    prompt_map = {
        "spending": "Analyze the following spending data and provide insights on spending patterns, areas for potential savings, and recommendations:",
        "savings": "Analyze the following savings data and provide recommendations for optimizing savings growth:",
        "investing": "Analyze the following investment data and provide portfolio recommendations:",
        "general": "Provide a financial analysis of the following data:",
    }

    system = prompt_map.get(request.analysis_type, prompt_map["general"])
    system += "\nFormat your response with clear sections and actionable recommendations."

    try:
        response = await chat_completion(
            messages=[{"role": "user", "content": request.data}],
            system_prompt=system,
            temperature=0.5,
            max_tokens=1024,
        )

        return {
            "analysis": response.get("message", {}).get("content", ""),
            "type": request.analysis_type,
        }

    except Exception as e:
        logger.error(f"Analysis failed: {e}")
        raise HTTPException(status_code=500, detail="Analysis failed")

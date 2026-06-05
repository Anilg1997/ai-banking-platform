"""MCP-compatible banking tools router.

Provides tools that AI agents can use to query banking data.
Follows Model Context Protocol conventions.
"""

import logging
from typing import Any, Optional

import httpx
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field

from app.config import (
    ACCOUNT_SERVICE_URL,
    TRANSACTION_SERVICE_URL,
    AUTH_SERVICE_URL,
)

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/ai/tools", tags=["tools"])


class ToolDefinition(BaseModel):
    """MCP tool definition."""
    name: str
    description: str
    parameters: dict


class ToolCallRequest(BaseModel):
    tool: str = Field(..., min_length=1)
    arguments: dict = Field(default_factory=dict)


class ToolCallResponse(BaseModel):
    result: Any = None
    error: Optional[str] = None


# Define available banking tools
TOOLS = [
    ToolDefinition(
        name="get_account_balance",
        description="Get the current balance and available balance for a specific bank account",
        parameters={
            "type": "object",
            "properties": {
                "account_id": {
                    "type": "string",
                    "description": "The unique ID of the bank account"
                }
            },
            "required": ["account_id"]
        }
    ),
    ToolDefinition(
        name="get_user_accounts",
        description="Get all bank accounts for a user with their balances",
        parameters={
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string",
                    "description": "The unique ID of the user"
                }
            },
            "required": ["user_id"]
        }
    ),
    ToolDefinition(
        name="get_recent_transactions",
        description="Get recent transaction history for a user",
        parameters={
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string",
                    "description": "The unique ID of the user"
                },
                "limit": {
                    "type": "integer",
                    "description": "Number of recent transactions to return (max 20)",
                    "default": 10
                }
            },
            "required": ["user_id"]
        }
    ),
    ToolDefinition(
        name="get_account_transactions",
        description="Get transaction history for a specific bank account",
        parameters={
            "type": "object",
            "properties": {
                "account_id": {
                    "type": "string",
                    "description": "The unique ID of the bank account"
                }
            },
            "required": ["account_id"]
        }
    ),
    ToolDefinition(
        name="get_total_balance",
        description="Get the total balance across all user accounts",
        parameters={
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string",
                    "description": "The unique ID of the user"
                }
            },
            "required": ["user_id"]
        }
    ),
    ToolDefinition(
        name="get_transaction_stats",
        description="Get transaction statistics (count, total sent, total received) for a user",
        parameters={
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string",
                    "description": "The unique ID of the user"
                }
            },
            "required": ["user_id"]
        }
    ),
]


@router.get("/list")
async def list_tools():
    """List all available MCP banking tools."""
    return {"tools": [t.model_dump() for t in TOOLS]}


@router.post("/call", response_model=ToolCallResponse)
async def call_tool(request: ToolCallRequest):
    """Execute a banking tool call."""
    tool_name = request.tool
    args = request.arguments

    logger.info(f"Tool call: {tool_name} with args: {args}")

    try:
        if tool_name == "get_account_balance":
            result = await _get_account_balance(args.get("account_id", ""))
        elif tool_name == "get_user_accounts":
            result = await _get_user_accounts(args.get("user_id", ""))
        elif tool_name == "get_recent_transactions":
            result = await _get_recent_transactions(
                args.get("user_id", ""),
                args.get("limit", 10)
            )
        elif tool_name == "get_account_transactions":
            result = await _get_account_transactions(args.get("account_id", ""))
        elif tool_name == "get_total_balance":
            result = await _get_total_balance(args.get("user_id", ""))
        elif tool_name == "get_transaction_stats":
            result = await _get_transaction_stats(args.get("user_id", ""))
        else:
            return ToolCallResponse(error=f"Unknown tool: {tool_name}")

        return ToolCallResponse(result=result)

    except Exception as e:
        logger.error(f"Tool call {tool_name} failed: {e}")
        return ToolCallResponse(error=str(e))


async def _make_request(method: str, url: str) -> Any:
    """Make an HTTP request to a microservice."""
    async with httpx.AsyncClient(timeout=10) as client:
        response = await client.request(method, url)
        response.raise_for_status()
        if response.status_code == 204:
            return {}
        return response.json()


async def _get_account_balance(account_id: str) -> dict:
    if not account_id:
        raise ValueError("account_id is required")
    data = await _make_request("GET", f"{ACCOUNT_SERVICE_URL}/api/accounts/{account_id}")
    return {
        "accountNumber": data.get("accountNumber"),
        "accountName": data.get("accountName"),
        "balance": float(data.get("balance", 0)),
        "availableBalance": float(data.get("availableBalance", 0)),
        "currency": data.get("currency", "USD"),
        "status": data.get("status"),
    }


async def _get_user_accounts(user_id: str) -> list:
    if not user_id:
        raise ValueError("user_id is required")
    return await _make_request("GET", f"{ACCOUNT_SERVICE_URL}/api/accounts/user/{user_id}")


async def _get_recent_transactions(user_id: str, limit: int = 10) -> list:
    if not user_id:
        raise ValueError("user_id is required")
    limit = min(max(1, limit), 20)
    return await _make_request(
        "GET",
        f"{TRANSACTION_SERVICE_URL}/api/transactions/user/{user_id}/recent?limit={limit}"
    )


async def _get_account_transactions(account_id: str) -> list:
    if not account_id:
        raise ValueError("account_id is required")
    return await _make_request(
        "GET",
        f"{TRANSACTION_SERVICE_URL}/api/transactions/account/{account_id}"
    )


async def _get_total_balance(user_id: str) -> dict:
    if not user_id:
        raise ValueError("user_id is required")
    return await _make_request(
        "GET",
        f"{ACCOUNT_SERVICE_URL}/api/accounts/user/{user_id}/balance"
    )


async def _get_transaction_stats(user_id: str) -> dict:
    if not user_id:
        raise ValueError("user_id is required")
    count = await _make_request(
        "GET",
        f"{TRANSACTION_SERVICE_URL}/api/transactions/user/{user_id}/count"
    )
    return {
        "transactionCount": count.get("count", 0),
        "userId": user_id,
    }

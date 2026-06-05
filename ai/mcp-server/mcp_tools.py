"""NovaBank MCP (Model Context Protocol) Tool Server.

This server exposes banking tools in the MCP format that AI agents
can use to query accounts, transactions, and perform banking operations.

MCP enables AI models to securely interact with banking tools via a
standardized protocol.
"""

import json
import logging
import os
from typing import Any, Optional

import httpx

logging.basicConfig(level=logging.INFO, format="%(asctime)s | %(levelname)-6s | %(message)s")
logger = logging.getLogger(__name__)

# Service URLs
ACCOUNT_SERVICE_URL = os.getenv("ACCOUNT_SERVICE_URL", "http://localhost:8083")
TRANSACTION_SERVICE_URL = os.getenv("TRANSACTION_SERVICE_URL", "http://localhost:8085")


# ============== MCP Tool Definitions ==============

MCP_TOOLS = {
    "get_account_balance": {
        "name": "get_account_balance",
        "description": "Get the current balance and details for a specific bank account",
        "input_schema": {
            "type": "object",
            "properties": {
                "account_id": {
                    "type": "string",
                    "description": "The unique ID of the bank account"
                }
            },
            "required": ["account_id"]
        }
    },
    "get_user_accounts": {
        "name": "get_user_accounts",
        "description": "List all bank accounts for a user with their balances and statuses",
        "input_schema": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string",
                    "description": "The unique ID of the user"
                }
            },
            "required": ["user_id"]
        }
    },
    "get_recent_transactions": {
        "name": "get_recent_transactions",
        "description": "Get the most recent transactions for a user",
        "input_schema": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string",
                    "description": "The unique ID of the user"
                },
                "limit": {
                    "type": "integer",
                    "description": "Number of transactions to return (max 20)",
                    "default": 10
                }
            },
            "required": ["user_id"]
        }
    },
    "get_total_balance": {
        "name": "get_total_balance",
        "description": "Get the total aggregated balance across all user accounts",
        "input_schema": {
            "type": "object",
            "properties": {
                "user_id": {
                    "type": "string",
                    "description": "The unique ID of the user"
                }
            },
            "required": ["user_id"]
        }
    },
}


# ============== Tool Implementations ==============

async def call_mcp_tool(tool_name: str, arguments: dict) -> dict:
    """Execute an MCP tool and return the result."""
    logger.info(f"MCP tool call: {tool_name} with {arguments}")

    if tool_name == "get_account_balance":
        return await _get_account_balance(arguments.get("account_id", ""))
    elif tool_name == "get_user_accounts":
        return await _get_user_accounts(arguments.get("user_id", ""))
    elif tool_name == "get_recent_transactions":
        return await _get_recent_transactions(
            arguments.get("user_id", ""),
            arguments.get("limit", 10)
        )
    elif tool_name == "get_total_balance":
        return await _get_total_balance(arguments.get("user_id", ""))
    else:
        raise ValueError(f"Unknown MCP tool: {tool_name}")


async def _make_request(method: str, url: str) -> Any:
    async with httpx.AsyncClient(timeout=10) as client:
        resp = await client.request(method, url)
        resp.raise_for_status()
        return resp.json() if resp.status_code != 204 else {}


async def _get_account_balance(account_id: str) -> dict:
    if not account_id:
        return {"error": "account_id is required"}
    data = await _make_request("GET", f"{ACCOUNT_SERVICE_URL}/api/accounts/{account_id}")
    return {
        "accountNumber": data.get("accountNumber"),
        "accountName": data.get("accountName"),
        "balance": float(data.get("balance", 0)),
        "availableBalance": float(data.get("availableBalance", 0)),
        "currency": data.get("currency", "USD"),
        "status": data.get("status"),
    }


async def _get_user_accounts(user_id: str) -> dict:
    if not user_id:
        return {"error": "user_id is required"}
    accounts = await _make_request("GET", f"{ACCOUNT_SERVICE_URL}/api/accounts/user/{user_id}")
    return {"accounts": accounts, "count": len(accounts)}


async def _get_recent_transactions(user_id: str, limit: int = 10) -> dict:
    if not user_id:
        return {"error": "user_id is required"}
    limit = min(max(1, limit), 20)
    txns = await _make_request(
        "GET",
        f"{TRANSACTION_SERVICE_URL}/api/transactions/user/{user_id}/recent?limit={limit}"
    )
    return {"transactions": txns, "count": len(txns)}


async def _get_total_balance(user_id: str) -> dict:
    if not user_id:
        return {"error": "user_id is required"}
    return await _make_request(
        "GET",
        f"{ACCOUNT_SERVICE_URL}/api/accounts/user/{user_id}/balance"
    )


# ============== MCP Server (CLI entry point) ==============

def print_mcp_tools():
    """Print MCP tools definition (for AI agent configuration)."""
    print(json.dumps({
        "protocol": "model-context-protocol",
        "version": "2025-03-26",
        "server_info": {
            "name": "novabank-banking-tools",
            "description": "NovaBank banking tools for AI agents"
        },
        "tools": list(MCP_TOOLS.values())
    }, indent=2))


if __name__ == "__main__":
    import asyncio

    # When run directly, print the MCP tool definitions
    print_mcp_tools()
    print("\n📋 To use with an MCP-compatible AI agent, configure the above as your MCP server.")
    print("🔧 Example: Use Ollama with these tools to enable AI-powered banking operations.")

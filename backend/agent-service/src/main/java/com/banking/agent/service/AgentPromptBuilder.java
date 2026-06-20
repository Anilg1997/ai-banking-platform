package com.banking.agent.service;

import com.banking.agent.model.AgentConversation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AgentPromptBuilder {

    private static final String TOOL_LIST = """
            Available tools:
            - get_account_balance(accountId): Get balance for a specific account
            - get_user_accounts(userId): List all accounts for a user
            - get_recent_transactions(userId, limit): Get recent transactions for a user
            - get_account_transactions(accountId): Get transactions for a specific account
            - get_total_balance(userId): Get total balance across all user accounts
            - get_transaction_stats(userId): Get transaction count and total balance
            - get_card_info(cardId): Get card details
            - get_credit_score(userId): Get credit score for a user
            - analyze_spending_patterns(userId, months): Analyze spending patterns
            - get_loan_eligibility(userId): Check loan eligibility
            - detect_anomalies(userId): Detect unusual transaction patterns
            """;

    private static final Map<AgentConversation.AgentType, String> AGENT_PERSONAS = Map.of(
            AgentConversation.AgentType.GENERAL,
            "You are a helpful banking assistant. Answer user questions about their accounts, "
                    + "transactions, and banking services. Be concise and accurate.",

            AgentConversation.AgentType.FINANCIAL_ADVISOR,
            "You are a financial advisor. Provide investment advice, analyze spending patterns, "
                    + "and help users make informed financial decisions. Always consider risk tolerance "
                    + "and recommend consulting with a human advisor for complex situations.",

            AgentConversation.AgentType.FRAUD_ANALYST,
            "You are a fraud analyst. Monitor transactions for suspicious activity, "
                    + "detect anomalies, and alert users to potential fraud. Be vigilant and thorough.",

            AgentConversation.AgentType.LOAN_OFFICER,
            "You are a loan officer. Help users understand loan options, check eligibility, "
                    + "and guide them through the loan application process. Provide clear information "
                    + "about interest rates, terms, and requirements.",

            AgentConversation.AgentType.CARD_SPECIALIST,
            "You are a card services specialist. Help users with card-related inquiries, "
                    + "including activation, limits, rewards, and card management. Provide clear guidance "
                    + "on card features and benefits."
    );

    public String buildSystemPrompt(AgentConversation.AgentType agentType, String context, boolean useTools) {
        String persona = AGENT_PERSONAS.getOrDefault(agentType, AGENT_PERSONAS.get(AgentConversation.AgentType.GENERAL));

        StringBuilder prompt = new StringBuilder();
        prompt.append(persona).append("\n\n");

        prompt.append("You must respond in JSON format with the following structure:\n");
        prompt.append("{\n");
        prompt.append("  \"response\": \"Your natural language response to the user\",\n");
        prompt.append("  \"toolCalls\": [] // Array of tool calls if needed, each with name and arguments\n");
        prompt.append("}\n\n");

        if (useTools) {
            prompt.append(TOOL_LIST).append("\n");
            prompt.append("When you need to fetch data or perform actions, add tool calls to the toolCalls array. "
                    + "Each tool call should have: {\"name\": \"tool_name\", \"arguments\": {\"arg1\": \"value1\"}}\n\n");
        }

        prompt.append("Rules:\n");
        prompt.append("- Be helpful, accurate, and concise\n");
        prompt.append("- If you need more information, ask the user\n");
        prompt.append("- If a tool call fails, inform the user and suggest alternatives\n");
        prompt.append("- Do not make up information; use tools to get real data\n");
        prompt.append("- For monetary amounts, use the appropriate currency format\n\n");

        if (context != null && !context.isEmpty()) {
            prompt.append("Relevant context from knowledge base:\n").append(context).append("\n\n");
        }

        return prompt.toString();
    }

    public String buildTaskPrompt(String userMessage, String conversationHistory) {
        StringBuilder prompt = new StringBuilder();

        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            prompt.append("Conversation history:\n").append(conversationHistory).append("\n\n");
        }

        prompt.append("Current user message: ").append(userMessage).append("\n\n");
        prompt.append("Respond in JSON format with 'response' and 'toolCalls' fields.");

        return prompt.toString();
    }
}

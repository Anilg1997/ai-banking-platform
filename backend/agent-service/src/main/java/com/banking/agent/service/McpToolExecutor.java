package com.banking.agent.service;

import com.banking.agent.dto.ToolDefinition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class McpToolExecutor {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${app.services.account-service.url}")
    private String accountServiceUrl;

    @Value("${app.services.transaction-service.url}")
    private String transactionServiceUrl;

    @Value("${app.services.card-service.url}")
    private String cardServiceUrl;

    @Value("${app.services.auth-service.url}")
    private String authServiceUrl;

    public Map<String, Object> executeTool(String toolName, Map<String, Object> arguments, String jwtToken) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> result = new HashMap<>();
        try {
            Object data = switch (toolName) {
                case "get_account_balance" -> getAccountBalance(arguments, jwtToken);
                case "get_user_accounts" -> getUserAccounts(arguments, jwtToken);
                case "get_recent_transactions" -> getRecentTransactions(arguments, jwtToken);
                case "get_account_transactions" -> getAccountTransactions(arguments, jwtToken);
                case "get_total_balance" -> getTotalBalance(arguments, jwtToken);
                case "get_transaction_stats" -> getTransactionStats(arguments, jwtToken);
                case "get_card_info" -> getCardInfo(arguments, jwtToken);
                case "get_credit_score" -> getCreditScore(arguments, jwtToken);
                case "analyze_spending_patterns" -> analyzeSpendingPatterns(arguments, jwtToken);
                case "get_loan_eligibility" -> getLoanEligibility(arguments, jwtToken);
                case "detect_anomalies" -> detectAnomalies(arguments, jwtToken);
                default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
            };
            result.put("success", true);
            result.put("data", data);
            result.put("duration", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("Tool execution failed: {} - {}", toolName, e.getMessage());
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("duration", System.currentTimeMillis() - startTime);
        }
        return result;
    }

    public List<ToolDefinition> listTools() {
        List<ToolDefinition> tools = new ArrayList<>();

        tools.add(ToolDefinition.builder().name("get_account_balance")
                .description("Get balance for a specific account")
                .parameters(Map.of("accountId", Map.of("type", "string", "description", "Account ID")))
                .build());

        tools.add(ToolDefinition.builder().name("get_user_accounts")
                .description("List all accounts for a user")
                .parameters(Map.of("userId", Map.of("type", "string", "description", "User ID")))
                .build());

        tools.add(ToolDefinition.builder().name("get_recent_transactions")
                .description("Get recent transactions for a user")
                .parameters(Map.of("userId", Map.of("type", "string", "description", "User ID"),
                        "limit", Map.of("type", "number", "description", "Number of transactions")))
                .build());

        tools.add(ToolDefinition.builder().name("get_account_transactions")
                .description("Get transactions for a specific account")
                .parameters(Map.of("accountId", Map.of("type", "string", "description", "Account ID")))
                .build());

        tools.add(ToolDefinition.builder().name("get_total_balance")
                .description("Get total balance across all user accounts")
                .parameters(Map.of("userId", Map.of("type", "string", "description", "User ID")))
                .build());

        tools.add(ToolDefinition.builder().name("get_transaction_stats")
                .description("Get transaction count and total balance summary")
                .parameters(Map.of("userId", Map.of("type", "string", "description", "User ID")))
                .build());

        tools.add(ToolDefinition.builder().name("get_card_info")
                .description("Get card details")
                .parameters(Map.of("cardId", Map.of("type", "string", "description", "Card ID")))
                .build());

        tools.add(ToolDefinition.builder().name("get_credit_score")
                .description("Get credit score for a user")
                .parameters(Map.of("userId", Map.of("type", "string", "description", "User ID")))
                .build());

        tools.add(ToolDefinition.builder().name("analyze_spending_patterns")
                .description("Analyze spending patterns over a number of months")
                .parameters(Map.of("userId", Map.of("type", "string", "description", "User ID"),
                        "months", Map.of("type", "number", "description", "Number of months to analyze")))
                .build());

        tools.add(ToolDefinition.builder().name("get_loan_eligibility")
                .description("Check loan eligibility for a user")
                .parameters(Map.of("userId", Map.of("type", "string", "description", "User ID")))
                .build());

        tools.add(ToolDefinition.builder().name("detect_anomalies")
                .description("Detect unusual transaction patterns for a user")
                .parameters(Map.of("userId", Map.of("type", "string", "description", "User ID")))
                .build());

        return tools;
    }

    private String getAuthHeader(String jwtToken) {
        return "Bearer " + jwtToken;
    }

    private Object getAccountBalance(Map<String, Object> args, String token) {
        String accountId = (String) args.get("accountId");
        JsonNode response = webClientBuilder.build().get()
                .uri(accountServiceUrl + "/api/accounts/" + accountId)
                .header("Authorization", getAuthHeader(token))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(10));
        return response != null ? response.toPrettyString() : "No data";
    }

    private Object getUserAccounts(Map<String, Object> args, String token) {
        String userId = (String) args.get("userId");
        JsonNode[] response = webClientBuilder.build().get()
                .uri(accountServiceUrl + "/api/accounts/user/" + userId)
                .header("Authorization", getAuthHeader(token))
                .retrieve()
                .bodyToMono(JsonNode[].class)
                .block(Duration.ofSeconds(10));
        return response != null ? Arrays.stream(response).map(JsonNode::toPrettyString).collect(Collectors.toList()) : List.of();
    }

    private Object getRecentTransactions(Map<String, Object> args, String token) {
        String userId = (String) args.get("userId");
        int limit = args.containsKey("limit") ? Integer.parseInt(args.get("limit").toString()) : 10;
        JsonNode[] response = webClientBuilder.build().get()
                .uri(transactionServiceUrl + "/api/transactions/user/" + userId + "/recent?limit=" + limit)
                .header("Authorization", getAuthHeader(token))
                .retrieve()
                .bodyToMono(JsonNode[].class)
                .block(Duration.ofSeconds(10));
        return response != null ? Arrays.stream(response).map(JsonNode::toPrettyString).collect(Collectors.toList()) : List.of();
    }

    private Object getAccountTransactions(Map<String, Object> args, String token) {
        String accountId = (String) args.get("accountId");
        JsonNode[] response = webClientBuilder.build().get()
                .uri(transactionServiceUrl + "/api/transactions/account/" + accountId)
                .header("Authorization", getAuthHeader(token))
                .retrieve()
                .bodyToMono(JsonNode[].class)
                .block(Duration.ofSeconds(10));
        return response != null ? Arrays.stream(response).map(JsonNode::toPrettyString).collect(Collectors.toList()) : List.of();
    }

    private Object getTotalBalance(Map<String, Object> args, String token) {
        String userId = (String) args.get("userId");
        JsonNode response = webClientBuilder.build().get()
                .uri(accountServiceUrl + "/api/accounts/user/" + userId + "/balance")
                .header("Authorization", getAuthHeader(token))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(10));
        return response != null ? response.toPrettyString() : "No data";
    }

    private Object getTransactionStats(Map<String, Object> args, String token) {
        String userId = (String) args.get("userId");
        Map<String, Object> stats = new HashMap<>();

        try {
            JsonNode balanceResponse = webClientBuilder.build().get()
                    .uri(accountServiceUrl + "/api/accounts/user/" + userId + "/balance")
                    .header("Authorization", getAuthHeader(token))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(10));
            if (balanceResponse != null) {
                stats.put("totalBalance", balanceResponse.get("totalBalance").asText());
                stats.put("accountCount", balanceResponse.get("accountCount").asText());
            }
        } catch (Exception e) {
            log.warn("Failed to get balance stats: {}", e.getMessage());
        }

        try {
            JsonNode[] txns = webClientBuilder.build().get()
                    .uri(transactionServiceUrl + "/api/transactions/user/" + userId + "/recent?limit=1000")
                    .header("Authorization", getAuthHeader(token))
                    .retrieve()
                    .bodyToMono(JsonNode[].class)
                    .block(Duration.ofSeconds(10));
            if (txns != null) {
                stats.put("transactionCount", txns.length);
            }
        } catch (Exception e) {
            log.warn("Failed to get transaction count: {}", e.getMessage());
        }

        return stats;
    }

    private Object getCardInfo(Map<String, Object> args, String token) {
        String cardId = (String) args.get("cardId");
        JsonNode response = webClientBuilder.build().get()
                .uri(cardServiceUrl + "/api/cards/" + cardId)
                .header("Authorization", getAuthHeader(token))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(10));
        return response != null ? response.toPrettyString() : "No data";
    }

    private Object getCreditScore(Map<String, Object> args, String token) {
        String userId = (String) args.get("userId");
        JsonNode response = webClientBuilder.build().get()
                .uri(cardServiceUrl + "/api/admin/cards/application/score?userId=" + userId)
                .header("Authorization", getAuthHeader(token))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(10));
        return response != null ? response.toPrettyString() : "No data";
    }

    private Object analyzeSpendingPatterns(Map<String, Object> args, String token) {
        String userId = (String) args.get("userId");
        int months = args.containsKey("months") ? Integer.parseInt(args.get("months").toString()) : 3;
        Map<String, Object> analysis = new HashMap<>();

        try {
            JsonNode[] txns = webClientBuilder.build().get()
                    .uri(transactionServiceUrl + "/api/transactions/user/" + userId + "/recent?limit=500")
                    .header("Authorization", getAuthHeader(token))
                    .retrieve()
                    .bodyToMono(JsonNode[].class)
                    .block(Duration.ofSeconds(10));

            if (txns != null && txns.length > 0) {
                Map<String, BigDecimal> categoryTotals = new HashMap<>();
                BigDecimal totalSpent = BigDecimal.ZERO;
                int txnCount = 0;

                for (JsonNode txn : txns) {
                    if (txn.has("amount")) {
                        BigDecimal amount = new BigDecimal(txn.get("amount").asText());
                        if (amount.compareTo(BigDecimal.ZERO) < 0) {
                            String category = txn.has("category") ? txn.get("category").asText() : "Other";
                            BigDecimal negAmount = amount.abs();
                            categoryTotals.merge(category, negAmount, BigDecimal::add);
                            totalSpent = totalSpent.add(negAmount);
                            txnCount++;
                        }
                    }
                }

                analysis.put("totalSpending", totalSpent.toString());
                analysis.put("transactionCount", txnCount);
                analysis.put("spendingByCategory", categoryTotals.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString())));
                analysis.put("averagePerTransaction", txnCount > 0
                        ? totalSpent.divide(BigDecimal.valueOf(txnCount), BigDecimal.ROUND_HALF_UP).toString()
                        : "0");
            }
        } catch (Exception e) {
            log.warn("Failed to analyze spending: {}", e.getMessage());
            analysis.put("error", e.getMessage());
        }

        return analysis;
    }

    private Object getLoanEligibility(Map<String, Object> args, String token) {
        String userId = (String) args.get("userId");
        Map<String, Object> eligibility = new HashMap<>();

        try {
            JsonNode balanceResponse = webClientBuilder.build().get()
                    .uri(accountServiceUrl + "/api/accounts/user/" + userId + "/balance")
                    .header("Authorization", getAuthHeader(token))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(Duration.ofSeconds(10));
            if (balanceResponse != null) {
                BigDecimal totalBalance = new BigDecimal(balanceResponse.get("totalBalance").asText());
                eligibility.put("totalBalance", totalBalance.toString());
                eligibility.put("hasSufficientFunds", totalBalance.compareTo(new BigDecimal("1000")) >= 0);
            }

            JsonNode[] accounts = webClientBuilder.build().get()
                    .uri(accountServiceUrl + "/api/accounts/user/" + userId)
                    .header("Authorization", getAuthHeader(token))
                    .retrieve()
                    .bodyToMono(JsonNode[].class)
                    .block(Duration.ofSeconds(10));
            if (accounts != null) {
                long activeAccounts = Arrays.stream(accounts)
                        .filter(a -> "ACTIVE".equals(a.get("status").asText()))
                        .count();
                eligibility.put("activeAccounts", activeAccounts);
                eligibility.put("accountHistoryLength", activeAccounts > 0 ? "Established" : "New");
            }

            eligibility.put("preliminaryStatus", "Eligible for review");
        } catch (Exception e) {
            log.warn("Failed to check loan eligibility: {}", e.getMessage());
            eligibility.put("error", e.getMessage());
        }

        return eligibility;
    }

    private Object detectAnomalies(Map<String, Object> args, String token) {
        String userId = (String) args.get("userId");
        Map<String, Object> anomalies = new HashMap<>();
        List<Map<String, Object>> suspiciousTxns = new ArrayList<>();

        try {
            JsonNode[] txns = webClientBuilder.build().get()
                    .uri(transactionServiceUrl + "/api/transactions/user/" + userId + "/recent?limit=100")
                    .header("Authorization", getAuthHeader(token))
                    .retrieve()
                    .bodyToMono(JsonNode[].class)
                    .block(Duration.ofSeconds(10));

            if (txns != null && txns.length > 0) {
                Map<String, BigDecimal> dailyTotals = new HashMap<>();
                Map<String, Integer> dailyCounts = new HashMap<>();

                for (JsonNode txn : txns) {
                    String date = txn.has("createdAt") ? txn.get("createdAt").asText().substring(0, 10) : "unknown";
                    BigDecimal amount = txn.has("amount") ? new BigDecimal(txn.get("amount").asText()) : BigDecimal.ZERO;
                    dailyTotals.merge(date, amount.abs(), BigDecimal::add);
                    dailyCounts.merge(date, 1, Integer::sum);
                }

                BigDecimal avgDailyTotal = dailyTotals.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(Math.max(1, dailyTotals.size())), BigDecimal.ROUND_HALF_UP);
                double avgDailyCount = dailyCounts.values().stream()
                        .mapToInt(Integer::intValue)
                        .average().orElse(0);

                for (Map.Entry<String, BigDecimal> entry : dailyTotals.entrySet()) {
                    if (entry.getValue().compareTo(avgDailyTotal.multiply(new BigDecimal("3"))) > 0) {
                        Map<String, Object> anomaly = new HashMap<>();
                        anomaly.put("date", entry.getKey());
                        anomaly.put("totalAmount", entry.getValue().toString());
                        anomaly.put("reason", "Unusually high daily total");
                        suspiciousTxns.add(anomaly);
                    }
                }

                anomalies.put("anomaliesDetected", !suspiciousTxns.isEmpty());
                anomalies.put("suspiciousDays", suspiciousTxns);
                anomalies.put("averageDailySpending", avgDailyTotal.toString());
                anomalies.put("averageDailyTransactions", avgDailyCount);
            }
        } catch (Exception e) {
            log.warn("Failed to detect anomalies: {}", e.getMessage());
            anomalies.put("error", e.getMessage());
        }

        return anomalies;
    }
}

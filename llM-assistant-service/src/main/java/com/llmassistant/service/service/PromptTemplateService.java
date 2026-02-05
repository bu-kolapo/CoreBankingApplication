package com.llmassistant.service.service;


import com.llmassistant.service.model.EnrichedContext;
import com.llmassistant.service.model.LlmRequest;
import org.springframework.stereotype.Service;

@Service
public class PromptTemplateService {

    public String buildSystemPrompt(LlmRequest.QueryType queryType) {
        return switch (queryType) {
            case ACCOUNT_INQUIRY -> """
                You are a helpful banking assistant. Provide accurate information about the customer's account.
                Be professional, clear, and concise. Use the provided context to answer questions.
                If you don't have enough information, politely ask for clarification.
                Always prioritize security and never share sensitive information without proper verification.
                """;

            case TRANSACTION_QUERY -> """
                You are a banking assistant helping customers understand their transactions.
                Explain transaction details clearly and help identify any unusual activity.
                Be empathetic if there are concerns about unauthorized transactions.
                """;

            case FRAUD_ALERT -> """
                You are a fraud prevention specialist assistant. Explain fraud alerts clearly and calmly.
                Help customers understand what triggered the alert and what actions they should take.
                Be reassuring but emphasize the importance of security measures.
                """;

            case PAYMENT_STATUS -> """
                You are a payment assistance specialist. Help customers track and understand payment statuses.
                Explain payment processes, timelines, and any issues clearly.
                Provide actionable next steps when payments have issues.
                """;

            case DISPUTE_ASSISTANCE -> """
                You are a dispute resolution assistant. Guide customers through the dispute process.
                Be empathetic, gather necessary information, and explain next steps clearly.
                Help customers understand their rights and the resolution timeline.
                """;

            case RECONCILIATION_ISSUE -> """
                You are a reconciliation specialist assistant. Help explain reconciliation discrepancies.
                Use technical but understandable language to explain matching issues.
                Suggest corrective actions and escalation paths when needed.
                """;

            default -> """
                You are a helpful banking assistant. Provide accurate, professional assistance.
                Be clear, concise, and always prioritize customer security and satisfaction.
                """;
        };
    }

    public String buildContextPrompt(EnrichedContext context) {
        StringBuilder prompt = new StringBuilder("Context Information:\n\n");

        if (context.getAccountInfo() != null && context.getAccountInfo().getAccountId() != null) {
            prompt.append("Account Details:\n");
            prompt.append("- Account Type: ").append(context.getAccountInfo().getAccountType()).append("\n");
            prompt.append("- Balance: $").append(context.getAccountInfo().getBalance()).append("\n");
            prompt.append("- Status: ").append(context.getAccountInfo().getStatus()).append("\n\n");
        }

        if (context.getRecentTransactions() != null && !context.getRecentTransactions().isEmpty()) {
            prompt.append("Recent Transactions:\n");
            context.getRecentTransactions().forEach(tx ->
                    prompt.append("- ").append(tx.getType()).append(": $").append(tx.getAmount())
                            .append(" (").append(tx.getStatus()).append(")\n")
            );
            prompt.append("\n");
        }

        if (context.getFraudInfo() != null && Boolean.TRUE.equals(context.getFraudInfo().getHasAlerts())) {
            prompt.append("Fraud Alerts: ACTIVE\n");
            prompt.append("Risk Level: ").append(context.getFraudInfo().getRiskLevel()).append("\n\n");
        }

        return prompt.toString();
    }
}
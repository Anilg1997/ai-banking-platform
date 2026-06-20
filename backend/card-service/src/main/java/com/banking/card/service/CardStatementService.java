package com.banking.card.service;

import com.banking.card.model.Card;
import com.banking.card.model.CardStatement;
import com.banking.card.model.CardTransaction;
import com.banking.card.repository.CardRepository;
import com.banking.card.repository.CardStatementRepository;
import com.banking.card.repository.CardTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardStatementService {

    private final CardStatementRepository statementRepository;
    private final CardTransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    @Transactional
    public CardStatement generateMonthlyStatement(Card card) {
        LocalDate now = LocalDate.now();
        LocalDate periodStart = now.withDayOfMonth(1).minusMonths(1);
        LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

        CardStatement latestStmt = statementRepository.findTopByCardIdOrderByStatementDateDesc(card.getId())
                .orElse(null);

        BigDecimal openingBalance = latestStmt != null ? latestStmt.getClosingBalance() : BigDecimal.ZERO;

        List<CardTransaction> periodTxns = transactionRepository
                .findByCardIdAndCreatedAtBetween(card.getId(),
                        periodStart.atStartOfDay(),
                        periodEnd.atTime(23, 59, 59));

        BigDecimal totalCharges = BigDecimal.ZERO;
        BigDecimal totalPayments = BigDecimal.ZERO;
        BigDecimal totalCashAdvances = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        long rewardPointsEarned = 0;

        for (CardTransaction txn : periodTxns) {
            if (txn.getStatus() == CardTransaction.TransactionStatus.COMPLETED) {
                switch (txn.getType()) {
                    case PURCHASE:
                    case FOREIGN_TRANSACTION_FEE:
                        totalCharges = totalCharges.add(txn.getAmount());
                        break;
                    case PAYMENT:
                        totalPayments = totalPayments.add(txn.getAmount());
                        break;
                    case CASH_ADVANCE:
                        totalCashAdvances = totalCashAdvances.add(txn.getAmount());
                        break;
                    case FEE:
                    case ANNUAL_FEE:
                    case LATE_FEE:
                        totalFees = totalFees.add(txn.getAmount());
                        break;
                }
                rewardPointsEarned += txn.getRewardPointsEarned();
            }
        }

        BigDecimal closingBalance = openingBalance.add(totalCharges).add(totalCashAdvances)
                .add(totalFees).subtract(totalPayments);

        BigDecimal interest = calculateInterest(card, openingBalance, closingBalance);
        BigDecimal minimumPayment = calculateMinimumPayment(closingBalance, card.getMinimumPaymentPercent());

        if (interest.compareTo(BigDecimal.ZERO) > 0) {
            closingBalance = closingBalance.add(interest);
        }

        LocalDate dueDate = periodEnd.plusDays(21);
        if (dueDate.getDayOfWeek().name().equals("SATURDAY") || dueDate.getDayOfWeek().name().equals("SUNDAY")) {
            dueDate = dueDate.plusDays(2);
        }

        CardStatement statement = CardStatement.builder()
                .cardId(card.getId())
                .userId(card.getUserId())
                .statementDate(now)
                .dueDate(dueDate)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .openingBalance(openingBalance)
                .closingBalance(closingBalance)
                .minimumPayment(minimumPayment)
                .totalCharges(totalCharges)
                .totalPayments(totalPayments)
                .totalCashAdvances(totalCashAdvances)
                .totalInterest(interest)
                .totalFees(totalFees)
                .rewardPointsEarned(rewardPointsEarned)
                .rewardPointsUsed(0L)
                .paymentStatus(CardStatement.PaymentStatus.UNPAID)
                .paidAmount(BigDecimal.ZERO)
                .generatedAt(LocalDateTime.now())
                .build();

        CardStatement saved = statementRepository.save(statement);

        card.setOutstandingAmount(closingBalance);
        cardRepository.save(card);

        log.info("Generated statement for card {}: balance={}, minPayment={}, due={}",
                card.getId(), closingBalance, minimumPayment, dueDate);

        return saved;
    }

    @Transactional
    public CardStatement payStatement(CardStatement statement, BigDecimal amount) {
        BigDecimal newPaid = statement.getPaidAmount().add(amount);
        statement.setPaidAmount(newPaid);
        statement.setPaymentDate(LocalDateTime.now());

        if (newPaid.compareTo(statement.getClosingBalance()) >= 0) {
            statement.setPaymentStatus(CardStatement.PaymentStatus.PAID);
        } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
            statement.setPaymentStatus(CardStatement.PaymentStatus.PARTIALLY_PAID);
        } else if (statement.getDueDate().isBefore(LocalDate.now())) {
            statement.setPaymentStatus(CardStatement.PaymentStatus.OVERDUE);
        }

        return statementRepository.save(statement);
    }

    public List<CardStatement> getAllStatements(String cardId) {
        return statementRepository.findByCardIdOrderByStatementDateDesc(cardId);
    }

    public CardStatement getLatestStatement(String cardId) {
        return statementRepository.findTopByCardIdOrderByStatementDateDesc(cardId)
                .orElse(null);
    }

    public BigDecimal calculateMinimumPayment(BigDecimal balance, BigDecimal percent) {
        BigDecimal minPayment = balance.multiply(percent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal fixedMin = new BigDecimal("50");
        return minPayment.compareTo(fixedMin) > 0 ? minPayment : fixedMin.min(balance);
    }

    private BigDecimal calculateInterest(Card card, BigDecimal openingBalance, BigDecimal closingBalance) {
        if (openingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal dailyRate = card.getInterestRate()
                .divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)
                .divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        BigDecimal avgBalance = openingBalance.add(closingBalance)
                .divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        return avgBalance.multiply(dailyRate).multiply(new BigDecimal("30"))
                .setScale(2, RoundingMode.HALF_UP);
    }
}

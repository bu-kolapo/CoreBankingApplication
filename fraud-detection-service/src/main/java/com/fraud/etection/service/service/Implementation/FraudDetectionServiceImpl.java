package com.fraud.etection.service.service.Implementation;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud.etection.service.dto.CustomerRiskProfileDto;
import com.fraud.etection.service.dto.FraudCheckRequest;
import com.fraud.etection.service.dto.FraudCheckResult;
import com.fraud.etection.service.model.CustomerRiskProfile;
import com.fraud.etection.service.model.FraudCheck;
import com.fraud.etection.service.model.FraudEvaluationContext;
import com.fraud.etection.service.repository.CustomerRiskProfileRepository;
import com.fraud.etection.service.repository.FraudCheckRepository;
import com.fraud.etection.service.service.FraudDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final FraudCheckRepository fraudCheckRepository;
    private final CustomerRiskProfileRepository riskProfileRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // Fraud detection rules (configurable thresholds)
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal CRITICAL_AMOUNT_THRESHOLD = new BigDecimal("50000");
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;
    private static final int MAX_TRANSACTIONS_PER_DAY = 50;

    public FraudDetectionServiceImpl(
            FraudCheckRepository fraudCheckRepository,
            CustomerRiskProfileRepository riskProfileRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.fraudCheckRepository = fraudCheckRepository;
        this.riskProfileRepository = riskProfileRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // ─── CORE FRAUD CHECK ────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<FraudCheckResult> performFraudCheck(FraudCheckRequest request) {

        log.info("🔍 Performing fraud check for {} {}",
                request.getEntityType(), request.getEntityId());

        String checkId = UUID.randomUUID().toString();

        return riskProfileRepository.findByCustomerId(request.getCustomerId())
                .switchIfEmpty(createDefaultRiskProfile(request.getCustomerId()))
                .flatMap(profile -> {

                    List<String> flaggedReasons = new ArrayList<>();
                    BigDecimal[] riskScore = {BigDecimal.ZERO};   // wrapper to allow mutation

                    // ── Rule 1: Blacklist check ──
                    if (Boolean.TRUE.equals(profile.getBlacklisted())) {

                        flaggedReasons.add("Customer is blacklisted");
                        riskScore[0] = riskScore[0].add(new BigDecimal("100"));

                        return saveFraudCheck(
                                checkId, request,
                                "CRITICAL",
                                riskScore[0],
                                "BLOCKED",
                                flaggedReasons,
                                true, true, false, false, false
                        );
                    }

                    // ── Rule 2: Whitelist bypass ──
                    if (Boolean.TRUE.equals(profile.getWhitelisted())) {

                        log.info("✅ Customer is whitelisted - auto-approve");

                        return saveFraudCheck(
                                checkId, request,
                                "LOW",
                                BigDecimal.ZERO,
                                "APPROVED",
                                List.of(),
                                false, false, false, false, false
                        );
                    }

                    // ── Rule 3: Amount threshold ──
                    boolean amountExceeded = false;

                    if (request.getAmount().compareTo(CRITICAL_AMOUNT_THRESHOLD) >= 0) {

                        flaggedReasons.add("Amount exceeds critical threshold: " + CRITICAL_AMOUNT_THRESHOLD);
                        riskScore[0] = riskScore[0].add(new BigDecimal("50"));
                        amountExceeded = true;

                    } else if (request.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {

                        flaggedReasons.add("Amount exceeds high threshold: " + HIGH_AMOUNT_THRESHOLD);
                        riskScore[0] = riskScore[0].add(new BigDecimal("30"));
                        amountExceeded = true;
                    }

                    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
                    LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);

                    boolean finalAmountExceeded = amountExceeded;

                    // ── Rule 4: Velocity ──
                    return fraudCheckRepository
                            .countByCustomerAndDecisionSince(request.getCustomerId(), "APPROVED", oneHourAgo)
                            .zipWith(
                                    fraudCheckRepository.countByCustomerAndDecisionSince(
                                            request.getCustomerId(), "APPROVED", oneDayAgo)
                            )
                            .flatMap(counts -> {

                                boolean velocityFailed = false;

                                Long hourlyCount = counts.getT1();
                                Long dailyCount = counts.getT2();

                                if (hourlyCount >= MAX_TRANSACTIONS_PER_HOUR) {

                                    flaggedReasons.add("Velocity check failed: " + hourlyCount + " transactions in 1 hour");
                                    riskScore[0] = riskScore[0].add(new BigDecimal("40"));
                                    velocityFailed = true;
                                }

                                if (dailyCount >= MAX_TRANSACTIONS_PER_DAY) {

                                    flaggedReasons.add("Velocity check failed: " + dailyCount + " transactions in 24 hours");
                                    riskScore[0] = riskScore[0].add(new BigDecimal("30"));
                                    velocityFailed = true;
                                }

                                // ── Rule 5: Unusual pattern ──
                                boolean unusualPattern = detectUnusualPattern(request, profile);

                                if (unusualPattern) {

                                    flaggedReasons.add("Unusual transaction pattern detected");
                                    riskScore[0] = riskScore[0].add(new BigDecimal("20"));
                                }

                                // ── Final Decision ──
                                String riskLevel;
                                String decision;

                                if (riskScore[0].compareTo(new BigDecimal("70")) >= 0) {
                                    riskLevel = "CRITICAL";
                                    decision = "BLOCKED";
                                } else if (riskScore[0].compareTo(new BigDecimal("50")) >= 0) {
                                    riskLevel = "HIGH";
                                    decision = "FLAGGED";
                                } else if (riskScore[0].compareTo(new BigDecimal("30")) >= 0) {
                                    riskLevel = "MEDIUM";
                                    decision = "FLAGGED";
                                } else {
                                    riskLevel = "LOW";
                                    decision = "APPROVED";
                                }

                                return saveFraudCheck(
                                        checkId,
                                        request,
                                        riskLevel,
                                        riskScore[0],
                                        decision,
                                        flaggedReasons,
                                        velocityFailed,
                                        finalAmountExceeded,
                                        false,
                                        false,
                                        unusualPattern
                                );
                            });
                })
                .doOnSuccess(result -> {

                    log.info("🔍 Fraud check completed: checkId={}, decision={}, riskScore={}",
                            result.getCheckId(),
                            result.getDecision(),
                            result.getRiskScore());

                    if ("BLOCKED".equals(result.getDecision()) ||
                            "FLAGGED".equals(result.getDecision())) {

                        publishFraudAlert(result);
                    }
                });
    }



    private Mono<FraudCheckResult> saveFraudCheck(
            String checkId,
            FraudCheckRequest request,
            String riskLevel,
            BigDecimal riskScore,
            String decision,
            List<String> flaggedReasons,
            boolean velocityFailed,
            boolean amountExceeded,
            boolean suspiciousLocation,
            boolean blacklisted,
            boolean unusualPattern) {

        FraudCheck fraudCheck = FraudCheck.builder()
                .checkId(checkId)
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .customerId(request.getCustomerId())
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .riskLevel(riskLevel)
                .riskScore(riskScore)
                .decision(decision)
                .flaggedReasons(serializeToJson(flaggedReasons))
                .velocityCheckFailed(velocityFailed)
                .amountThresholdExceeded(amountExceeded)
                .suspiciousLocationDetected(suspiciousLocation)
                .blacklistedUser(blacklisted)
                .unusualPatternDetected(unusualPattern)
                .ipAddress(request.getIpAddress())
                .deviceId(request.getDeviceId())
                .location(request.getLocation())
                .userAgent(request.getUserAgent())
                .reviewStatus("FLAGGED".equals(decision) || "BLOCKED".equals(decision) ? "PENDING" : "REVIEWED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return fraudCheckRepository.save(fraudCheck)
                .map(saved -> FraudCheckResult.builder()
                        .checkId(saved.getCheckId())
                        .riskLevel(saved.getRiskLevel())
                        .riskScore(saved.getRiskScore())
                        .decision(saved.getDecision())
                        .flaggedReasons(flaggedReasons)
                        .requiresReview("PENDING".equals(saved.getReviewStatus()))
                        .build());
    }

    // Mock pattern detection - replace with ML model integration
    private boolean detectUnusualPattern(FraudCheckRequest request, CustomerRiskProfile profile) {
        // Example: if this transaction is 10x the customer's average, flag it
        if (profile.getAvgTransactionAmount() != null && profile.getAvgTransactionAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = request.getAmount().divide(profile.getAvgTransactionAmount(), 2, BigDecimal.ROUND_HALF_UP);
            return ratio.compareTo(new BigDecimal("10")) >= 0;
        }
        return false;
    }

    // ─── QUERY ───────────────────────────────────────────────────────

    @Override
    public Mono<FraudCheckResult> getFraudCheck(String checkId) {
        return fraudCheckRepository.findByCheckId(checkId)
                .map(this::toResult)
                .switchIfEmpty(Mono.error(new RuntimeException("Fraud check not found")));
    }

    @Override
    public Flux<FraudCheckResult> getFraudChecksForEntity(String entityType, String entityId) {
        return fraudCheckRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .map(this::toResult);
    }

    @Override
    public Flux<FraudCheckResult> getFlaggedChecks() {
        return fraudCheckRepository.findByDecisionOrderByCreatedAtDesc("FLAGGED")
                .map(this::toResult);
    }

    @Override
    public Flux<FraudCheckResult> getBlockedChecks() {
        return fraudCheckRepository.findByDecisionOrderByCreatedAtDesc("BLOCKED")
                .map(this::toResult);
    }

    // ─── RISK PROFILE ────────────────────────────────────────────────

    @Override
    public Mono<CustomerRiskProfileDto> getCustomerRiskProfile(String customerId) {
        return riskProfileRepository.findByCustomerId(customerId)
                .map(this::toProfileDto)
                .switchIfEmpty(createDefaultRiskProfile(customerId).map(this::toProfileDto));
    }

    @Override
    @Transactional
    public Mono<CustomerRiskProfileDto> updateCustomerRiskProfile(String customerId) {
        log.info("🔄 Updating risk profile for customer: {}", customerId);

        return riskProfileRepository.findByCustomerId(customerId)
                .switchIfEmpty(createDefaultRiskProfile(customerId))
                .flatMap(profile -> {
                    // Recalculate stats based on recent fraud checks
                    LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

                    return fraudCheckRepository.findRecentByCustomer(customerId, oneMonthAgo)
                            .collectList()
                            .flatMap(checks -> {
                                int totalChecks = checks.size();
                                int flaggedCount = (int) checks.stream().filter(c -> "FLAGGED".equals(c.getDecision())).count();
                                int blockedCount = (int) checks.stream().filter(c -> "BLOCKED".equals(c.getDecision())).count();

                                BigDecimal avgScore = checks.stream()
                                        .map(FraudCheck::getRiskScore)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .divide(BigDecimal.valueOf(Math.max(totalChecks, 1)), 2, BigDecimal.ROUND_HALF_UP);

                                profile.setTotalTransactions(totalChecks);
                                profile.setFraudIncidents(flaggedCount + blockedCount);
                                profile.setRiskScore(avgScore);

                                if (avgScore.compareTo(new BigDecimal("70")) >= 0) {
                                    profile.setRiskRating("CRITICAL");
                                } else if (avgScore.compareTo(new BigDecimal("50")) >= 0) {
                                    profile.setRiskRating("HIGH");
                                } else if (avgScore.compareTo(new BigDecimal("30")) >= 0) {
                                    profile.setRiskRating("MEDIUM");
                                } else {
                                    profile.setRiskRating("LOW");
                                }

                                profile.setLastFraudCheckAt(LocalDateTime.now());
                                profile.setUpdatedAt(LocalDateTime.now());

                                return riskProfileRepository.save(profile);
                            });
                })
                .map(this::toProfileDto)
                .doOnSuccess(dto -> log.info("✅ Risk profile updated: {}", customerId));
    }

    private Mono<CustomerRiskProfile> createDefaultRiskProfile(String customerId) {
        log.info("🆕 Creating default risk profile for customer: {}", customerId);

        CustomerRiskProfile profile = CustomerRiskProfile.builder()
                .profileId(UUID.randomUUID().toString())
                .customerId(customerId)
                .riskRating("LOW")
                .riskScore(BigDecimal.ZERO)
                .totalTransactions(0)
                .totalTransactionVolume(BigDecimal.ZERO)
                .avgTransactionAmount(BigDecimal.ZERO)
                .maxTransactionAmount(BigDecimal.ZERO)
                .fraudIncidents(0)
                .chargebacks(0)
                .failedTransactions(0)
                .blacklisted(false)
                .whitelisted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return riskProfileRepository.save(profile);
    }

    // ─── BLACKLIST / WHITELIST ───────────────────────────────────────

    @Override
    @Transactional
    public Mono<Void> blacklistCustomer(String customerId, String reason) {
        log.warn("🚫 Blacklisting customer: {} reason: {}", customerId, reason);

        return riskProfileRepository.findByCustomerId(customerId)
                .switchIfEmpty(createDefaultRiskProfile(customerId))
                .flatMap(profile -> {
                    profile.setBlacklisted(true);
                    profile.setBlacklistReason(reason);
                    profile.setBlacklistedAt(LocalDateTime.now());
                    profile.setRiskRating("CRITICAL");
                    profile.setRiskScore(new BigDecimal("100"));
                    profile.setUpdatedAt(LocalDateTime.now());

                    return riskProfileRepository.save(profile);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Customer blacklisted: {}", customerId));
    }

    @Override
    @Transactional
    public Mono<Void> whitelistCustomer(String customerId) {
        log.info("✅ Whitelisting customer: {}", customerId);

        return riskProfileRepository.findByCustomerId(customerId)
                .switchIfEmpty(createDefaultRiskProfile(customerId))
                .flatMap(profile -> {
                    profile.setWhitelisted(true);
                    profile.setWhitelistedAt(LocalDateTime.now());
                    profile.setBlacklisted(false);
                    profile.setRiskRating("LOW");
                    profile.setRiskScore(BigDecimal.ZERO);
                    profile.setUpdatedAt(LocalDateTime.now());

                    return riskProfileRepository.save(profile);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Customer whitelisted: {}", customerId));
    }

    @Override
    @Transactional
    public Mono<Void> removeFromBlacklist(String customerId) {
        log.info("🔓 Removing customer from blacklist: {}", customerId);

        return riskProfileRepository.findByCustomerId(customerId)
                .flatMap(profile -> {
                    profile.setBlacklisted(false);
                    profile.setBlacklistReason(null);
                    profile.setBlacklistedAt(null);
                    profile.setUpdatedAt(LocalDateTime.now());

                    return riskProfileRepository.save(profile);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Customer removed from blacklist: {}", customerId));
    }

    // ─── MANUAL REVIEW ───────────────────────────────────────────────

    @Override
    @Transactional
    public Mono<Void> approveCheck(String checkId, String reviewedBy, String notes) {
        log.info("✅ Approving fraud check: {}", checkId);

        return fraudCheckRepository.findByCheckId(checkId)
                .switchIfEmpty(Mono.error(new RuntimeException("Fraud check not found")))
                .flatMap(check -> {
                    check.setDecision("APPROVED");
                    check.setReviewStatus("REVIEWED");
                    check.setReviewedBy(reviewedBy);
                    check.setReviewNotes(notes);
                    check.setReviewedAt(LocalDateTime.now());
                    check.setUpdatedAt(LocalDateTime.now());

                    return fraudCheckRepository.save(check);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Fraud check approved: {}", checkId));
    }

    @Override
    @Transactional
    public Mono<Void> rejectCheck(String checkId, String reviewedBy, String notes) {
        log.info("❌ Rejecting fraud check: {}", checkId);

        return fraudCheckRepository.findByCheckId(checkId)
                .switchIfEmpty(Mono.error(new RuntimeException("Fraud check not found")))
                .flatMap(check -> {
                    check.setDecision("BLOCKED");
                    check.setReviewStatus("REVIEWED");
                    check.setReviewedBy(reviewedBy);
                    check.setReviewNotes(notes);
                    check.setReviewedAt(LocalDateTime.now());
                    check.setUpdatedAt(LocalDateTime.now());

                    return fraudCheckRepository.save(check);
                })
                .then()
                .doOnSuccess(v -> log.info("✅ Fraud check rejected: {}", checkId));
    }

    // ─── KAFKA ───────────────────────────────────────────────────────

    private void publishFraudAlert(FraudCheckResult result) {
        try {
            kafkaTemplate.send("fraud-alert", result.getCheckId(), result);
            log.info("📤 Published fraud alert: {}", result.getCheckId());
        } catch (Exception e) {
            log.error("❌ Failed to publish fraud alert", e);
        }
    }

    // ─── HELPERS ─────────────────────────────────────────────────────

    private String serializeToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> deserializeFromJson(String json) {
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    // ─── MAPPERS ─────────────────────────────────────────────────────

    private FraudCheckResult toResult(FraudCheck check) {
        return FraudCheckResult.builder()
                .checkId(check.getCheckId())
                .riskLevel(check.getRiskLevel())
                .riskScore(check.getRiskScore())
                .decision(check.getDecision())
                .flaggedReasons(deserializeFromJson(check.getFlaggedReasons()))
                .requiresReview("PENDING".equals(check.getReviewStatus()))
                .build();
    }

    private CustomerRiskProfileDto toProfileDto(CustomerRiskProfile profile) {
        return CustomerRiskProfileDto.builder()
                .profileId(profile.getProfileId())
                .customerId(profile.getCustomerId())
                .riskRating(profile.getRiskRating())
                .riskScore(profile.getRiskScore())
                .totalTransactions(profile.getTotalTransactions())
                .fraudIncidents(profile.getFraudIncidents())
                .blacklisted(profile.getBlacklisted())
                .whitelisted(profile.getWhitelisted())
                .build();
    }
}
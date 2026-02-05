package com.fraud.etection.service.controller;

import com.fraud.etection.service.dto.CustomerRiskProfileDto;
import com.fraud.etection.service.dto.FraudCheckRequest;
import com.fraud.etection.service.dto.FraudCheckResult;
import com.fraud.etection.service.service.FraudDetectionService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@RestController
@RequestMapping("/api/fraud")
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;

    public FraudDetectionController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    // ─── FRAUD CHECKS ────────────────────────────────────────────────

    @PostMapping("/check")
    public Mono<ResponseEntity<FraudCheckResult>> performFraudCheck(
            @Valid @RequestBody FraudCheckRequest request) {

        log.info("🔍 Manual fraud check request: {}", request.getEntityId());

        return fraudDetectionService.performFraudCheck(request)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result))
                .onErrorResume(error -> {
                    log.error("❌ Fraud check failed", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @GetMapping("/check/{checkId}")
    public Mono<ResponseEntity<FraudCheckResult>> getFraudCheck(@PathVariable String checkId) {
        return fraudDetectionService.getFraudCheck(checkId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/flagged")
    public Flux<FraudCheckResult> getFlaggedChecks() {
        log.info("🔍 Getting flagged fraud checks");
        return fraudDetectionService.getFlaggedChecks();
    }

    @GetMapping("/blocked")
    public Flux<FraudCheckResult> getBlockedChecks() {
        log.info("🔍 Getting blocked fraud checks");
        return fraudDetectionService.getBlockedChecks();
    }

    // ─── RISK PROFILES ───────────────────────────────────────────────

    @GetMapping("/profile/{customerId}")
    public Mono<ResponseEntity<CustomerRiskProfileDto>> getCustomerRiskProfile(
            @PathVariable String customerId) {

        return fraudDetectionService.getCustomerRiskProfile(customerId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/profile/{customerId}/update")
    public Mono<ResponseEntity<CustomerRiskProfileDto>> updateCustomerRiskProfile(
            @PathVariable String customerId) {

        return fraudDetectionService.updateCustomerRiskProfile(customerId)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    log.error("❌ Failed to update risk profile", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    // ─── BLACKLIST / WHITELIST ───────────────────────────────────────

    @PostMapping("/blacklist/{customerId}")
    public Mono<ResponseEntity<Void>> blacklistCustomer(
            @PathVariable String customerId,
            @RequestParam String reason) {

        return fraudDetectionService.blacklistCustomer(customerId, reason)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to blacklist customer", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @PostMapping("/whitelist/{customerId}")
    public Mono<ResponseEntity<Void>> whitelistCustomer(@PathVariable String customerId) {
        return fraudDetectionService.whitelistCustomer(customerId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    @DeleteMapping("/blacklist/{customerId}")
    public Mono<ResponseEntity<Void>> removeFromBlacklist(@PathVariable String customerId) {
        return fraudDetectionService.removeFromBlacklist(customerId)
                .then(Mono.just(ResponseEntity.ok().<Void>build()));
    }

    // ─── MANUAL REVIEW ───────────────────────────────────────────────

    @PostMapping("/check/{checkId}/approve")
    public Mono<ResponseEntity<Void>> approveCheck(
            @PathVariable String checkId,
            @RequestParam String reviewedBy,
            @RequestParam(required = false) String notes) {

        return fraudDetectionService.approveCheck(checkId, reviewedBy, notes)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to approve check", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    @PostMapping("/check/{checkId}/reject")
    public Mono<ResponseEntity<Void>> rejectCheck(
            @PathVariable String checkId,
            @RequestParam String reviewedBy,
            @RequestParam(required = false) String notes) {

        return fraudDetectionService.rejectCheck(checkId, reviewedBy, notes)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(error -> {
                    log.error("❌ Failed to reject check", error);
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}
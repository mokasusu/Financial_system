package com.financial.modules.financialstatement.scheduler;

import com.financial.modules.financialstatement.entity.FinancialStatement;
import com.financial.modules.financialstatement.repository.FinancialStatementRepository;
import com.financial.modules.financialstatement.service.FinancialStatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DraftCleanupScheduler {

    private final FinancialStatementRepository statementRepository;
    private final FinancialStatementService statementService;

    /**
     * Periodically cleans up abandoned draft statements created more than 24 hours ago.
     * Runs daily at 02:00 AM server time.
     */
    @Scheduled(cron = "${app.draft.cleanup-cron:0 0 2 * * ?}")
    public void cleanupStaleDrafts() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        log.info("Starting scheduled cleanup for abandoned draft statements created before {}", threshold);

        List<FinancialStatement> staleDrafts = statementRepository.findByIsDraftTrueAndCreatedAtBefore(threshold);
        if (staleDrafts.isEmpty()) {
            log.info("No abandoned draft statements found for cleanup.");
            return;
        }

        log.info("Found {} abandoned draft statement(s) to clean up.", staleDrafts.size());
        int cleanedCount = 0;
        for (FinancialStatement draft : staleDrafts) {
            try {
                statementService.hardDeleteDraft(draft.getId());
                cleanedCount++;
            } catch (Exception ex) {
                log.error("Failed to clean up stale draft statement ID {}: {}", draft.getId(), ex.getMessage(), ex);
            }
        }
        log.info("Completed scheduled cleanup. Successfully purged {}/{} abandoned drafts.", cleanedCount, staleDrafts.size());
    }
}


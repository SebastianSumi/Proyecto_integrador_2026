package pe.edu.upeu.saludablemente.observability;

import java.util.Objects;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Schedules operational logs only after a successful transaction commit.
 */
public final class TransactionLog {

    private TransactionLog() {
    }

    public static void afterCommit(Runnable logAction) {
        Objects.requireNonNull(logAction, "logAction must not be null");

        if (!TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isActualTransactionActive()) {
            logAction.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                logAction.run();
            }
        });
    }
}

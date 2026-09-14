package pe.edu.upeu.saludablemente.observability;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class TransactionLogTest {

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void runsImmediatelyWhenNoTransactionSynchronizationIsActive() {
        AtomicInteger invocations = new AtomicInteger();

        TransactionLog.afterCommit(invocations::incrementAndGet);

        assertThat(invocations).hasValue(1);
    }

    @Test
    void runsOnlyAfterCommitWhenTransactionSynchronizationIsActive() {
        AtomicInteger invocations = new AtomicInteger();
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        TransactionLog.afterCommit(invocations::incrementAndGet);

        assertThat(invocations).hasValue(0);
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        assertThat(invocations).hasValue(1);
    }

    @Test
    void doesNotRunForRollbackCompletion() {
        AtomicInteger invocations = new AtomicInteger();
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);

        TransactionLog.afterCommit(invocations::incrementAndGet);

        TransactionSynchronizationManager.getSynchronizations().forEach(
                synchronization -> synchronization.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK)
        );
        assertThat(invocations).hasValue(0);
    }
}

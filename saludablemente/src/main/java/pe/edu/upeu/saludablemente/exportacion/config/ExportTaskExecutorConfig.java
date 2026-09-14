package pe.edu.upeu.saludablemente.exportacion.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pe.edu.upeu.saludablemente.exception.LimiteExportacionExcedidoException;

@Slf4j
@Configuration
public class ExportTaskExecutorConfig {

    @Value("${exportacion.async.core-pool-size:2}")
    private int corePoolSize;

    @Value("${exportacion.async.max-pool-size:4}")
    private int maxPoolSize;

    @Value("${exportacion.async.queue-capacity:50}")
    private int queueCapacity;

    @Bean(name = "exportTaskExecutor")
    public ThreadPoolTaskExecutor exportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("export-task-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // Rechazo controlado cuando la cola está colmada
        executor.setRejectedExecutionHandler((runnable, pool) -> {
            log.error("Cola de exportacion colmada. Tarea rechazada.");
            throw new LimiteExportacionExcedidoException(
                    "El motor de exportacion esta saturado. Intente mas tarde.");
        });

        executor.initialize();
        log.info("ExportTaskExecutor inicializado: core={}, max={}, queue={}",
                corePoolSize, maxPoolSize, queueCapacity);
        return executor;
    }
}

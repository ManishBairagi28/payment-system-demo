package com.example.batch;

import com.example.batch.service.PaymentMaintenanceService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Command line runner that executes the batch job and exits
 */
@Component
public class PaymentMaintenanceRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMaintenanceRunner.class);
    
    private final PaymentMaintenanceService maintenanceService;
    private final MeterRegistry meterRegistry;

    @Autowired
    public PaymentMaintenanceRunner(PaymentMaintenanceService maintenanceService, 
                                   MeterRegistry meterRegistry) {
        this.maintenanceService = maintenanceService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting payment maintenance batch job");
        
        Instant startTime = Instant.now();
        boolean success = false;
        int recordsProcessed = 0;
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Execute the maintenance job
            recordsProcessed = maintenanceService.expireStalePayments();
            success = true;
            
            logger.info("Payment maintenance job completed successfully. Processed {} records", recordsProcessed);
            
            // Record success metric
            meterRegistry.counter("batch.job.status",
                    "job", "payment-maintenance",
                    "status", "success",
                    "environment", "local")
                    .increment();
                    
        } catch (Exception e) {
            logger.error("Payment maintenance job failed", e);
            
            // Record failure metric
            meterRegistry.counter("batch.job.status",
                    "job", "payment-maintenance",
                    "status", "failure",
                    "environment", "local")
                    .increment();
                    
            throw e;
        } finally {
            // Record duration
            sample.stop(meterRegistry.timer("batch.job.duration",
                    "job", "payment-maintenance",
                    "environment", "local"));
                    
            // Record records processed
            meterRegistry.gauge("batch.job.records.processed",
                    recordsProcessed);
                    
            Duration duration = Duration.between(startTime, Instant.now());
            logger.info("Batch job execution time: {} seconds", duration.getSeconds());
            
            // Push metrics to Pushgateway before exit
            logger.info("Pushing metrics to Pushgateway");
            // The Micrometer Pushgateway registry will handle this automatically
        }
    }
}

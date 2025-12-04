package com.example.batch.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.PushGateway;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for Prometheus Pushgateway
 */
@Configuration
public class PushgatewayConfig {

    private static final Logger logger = LoggerFactory.getLogger(PushgatewayConfig.class);

    @Value("${management.metrics.export.prometheus.pushgateway.base-url:http://pushgateway:9091}")
    private String pushgatewayUrl;

    @Value("${management.metrics.export.prometheus.pushgateway.job:payment-batch-job}")
    private String jobName;

    @Value("${management.metrics.export.prometheus.pushgateway.enabled:true}")
    private boolean pushgatewayEnabled;

    private PushGateway pushGateway;
    private PrometheusRegistry collectorRegistry;

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            registry.config().commonTags(
                    "application", "payment-batch-job",
                    "environment", "local"
            );
        };
    }

    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        if (pushgatewayEnabled) {
            try {
                collectorRegistry = registry.getPrometheusRegistry();
                pushGateway = new PushGateway(pushgatewayUrl);

                logger.info("Pushgateway configured at: {}", pushgatewayUrl);
            } catch (Exception e) {
                logger.error("Failed to configure Pushgateway", e);
            }
        }

        return registry;
    }

    /**
     * Push metrics to Pushgateway before application shutdown
     */
    @PreDestroy
    public void pushMetrics() {
        if (pushgatewayEnabled && pushGateway != null && collectorRegistry != null) {
            try {
                Map<String, String> groupingKey = new HashMap<>();
                groupingKey.put("instance", getHostname());

                logger.info("Pushing metrics to Pushgateway before shutdown");
               //Error at this line please check and fic then run it ----  pushGateway.pushAdd(collectorRegistry, jobName, groupingKey);
                logger.info("Successfully pushed metrics to Pushgateway");
            } catch (Exception e) {
                logger.error("Failed to push metrics to Pushgateway", e);
            }
        }
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}

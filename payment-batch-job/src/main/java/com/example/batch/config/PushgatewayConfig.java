package com.example.batch.config;

import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Configuration for Prometheus Pushgateway
 */
@Configuration
@EnableScheduling
public class PushgatewayConfig {

    private static final Logger logger = LoggerFactory.getLogger(PushgatewayConfig.class);

    @Value("${management.metrics.export.prometheus.pushgateway.base-url:http://pushgateway:9091}")
    private String pushgatewayUrl;

    @Value("${management.metrics.export.prometheus.pushgateway.job:payment-batch-job}")
    private String jobName;

    @Value("${management.metrics.export.prometheus.pushgateway.enabled:true}")
    private boolean pushgatewayEnabled;

    private PrometheusMeterRegistry prometheusMeterRegistry;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        this.prometheusMeterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

        // Apply common tags directly to the registry
        this.prometheusMeterRegistry.config().commonTags(
                "application", "payment-batch-job",
                "environment", "local"
        );

        if (pushgatewayEnabled) {
            logger.info("Pushgateway configured at: {}", pushgatewayUrl);
        }

        return this.prometheusMeterRegistry;
    }

    /**
     * Push metrics to Pushgateway before application shutdown
     */
    @PreDestroy
    public void pushMetrics() {
        if (pushgatewayEnabled && prometheusMeterRegistry != null) {
            try {
                pushMetricsToPushgateway();
                logger.info("Successfully pushed metrics to Pushgateway on shutdown");
            } catch (Exception e) {
                logger.error("Failed to push metrics to Pushgateway on shutdown", e);
            }
        }
    }

    /**
     * Periodically push metrics to Pushgateway (optional - for long-running jobs)
     */
    @Scheduled(fixedRate = 60000) // Push every minute
    public void scheduledPushMetrics() {
        if (pushgatewayEnabled && prometheusMeterRegistry != null) {
            try {
                pushMetricsToPushgateway();
                logger.debug("Successfully pushed metrics to Pushgateway (scheduled)");
            } catch (Exception e) {
                logger.warn("Failed to push metrics to Pushgateway (scheduled)", e);
            }
        }
    }

    private void pushMetricsToPushgateway() throws IOException, InterruptedException {
        String metricsData = prometheusMeterRegistry.scrape();
        String hostname = getHostname();

        String url = String.format("%s/metrics/job/%s/instance/%s",
                pushgatewayUrl, jobName, hostname);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(metricsData))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Pushgateway returned status: " + response.statusCode()
                    + ", body: " + response.body());
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

# Payment System with Full Observability Stack

A complete Spring Boot microservices-based payment system with comprehensive open-source observability stack including logs, metrics, and dashboards.

## Architecture Overview

### Applications
1. **Payment Service** - REST API for payment operations
2. **Payment Batch Job** - Short-lived batch process for payment maintenance

### Infrastructure
- **MySQL** - Primary database
- **RabbitMQ** - Message broker for async events
- **Prometheus** - Metrics time-series database
- **Loki** - Log aggregation system
- **Grafana** - Visualization and dashboards
- **OpenTelemetry Collector** - Host metrics collection

## Project Structure

```
payment-system/
├── payment-service/          # Payment REST API microservice
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── payment-batch-job/        # Batch job for maintenance tasks
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── docker/                   # Docker and observability configs
│   ├── docker-compose.yml
│   ├── prometheus/
│   ├── loki/
│   ├── promtail/
│   └── otel-collector/
├── grafana/                  # Grafana provisioning
│   ├── datasources/
│   └── dashboards/
├── mysql/                    # Database initialization
│   └── init/
└── README.md
```

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- Java 21+ (for local development)
- Maven 3.6+ (for local development)
- At least 8GB RAM available

### Running the Complete Stack

1. Clone the repository:
```bash
git clone &lt;repository-url&gt;
cd payment-system
```

2. Start all services:
```bash
cd docker
docker-compose up -d
```

3. Wait for all services to be healthy (approximately 2-3 minutes):
```bash
docker-compose ps
```

4. Access the services:
- **Payment API**: http://localhost:8080/api/v1/payments
- **Grafana**: http://localhost:3000 (admin/admin)
- **Prometheus**: http://localhost:9090
- **RabbitMQ Management**: http://localhost:15672 (guest/guest)
- **Pushgateway**: http://localhost:9091
- **PHP MyAdmin GUI MySql**: http://localhost:8081 (phpMyAdmin login page)

## API Endpoints

### Payment Service

#### Create Payment
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.50,
    "currency": "USD",
    "description": "Test payment",
    "customerId": "CUST123",
    "merchantId": "MERCH456"
  }'
```

#### Get Payment by ID
```bash
curl http://localhost:8080/api/v1/payments/{paymentId}
```

#### Search Payments
```bash
curl "http://localhost:8080/api/v1/payments?status=PENDING&amp;page=0&amp;size=10"
```

#### Health Check
```bash
curl http://localhost:8080/api/v1/actuator/health
```

#### Metrics
```bash
curl http://localhost:8080/api/v1/actuator/prometheus
```

## Batch Job Execution

The batch job can be run manually:

```bash
docker-compose run --rm payment-batch-job
```

Or scheduled via cron:
```bash
# Add to crontab for hourly execution
0 * * * * cd /path/to/payment-system/docker &amp;&amp; docker-compose run --rm payment-batch-job
```

## Observability Stack

### Metrics Flow
1. **Payment Service** → Actuator/Prometheus endpoint → Scraped by **Prometheus** → **Grafana**
2. **Batch Job** → **Pushgateway** → Scraped by **Prometheus** → **Grafana**
3. **MySQL** → **MySQL Exporter** → Scraped by **Prometheus** → **Grafana**
4. **RabbitMQ** → **RabbitMQ Exporter** → Scraped by **Prometheus** → **Grafana**
5. **Host/Containers** → **OpenTelemetry Collector** → **Prometheus** → **Grafana**

### Logs Flow
1. **Applications** → stdout/file → **Promtail** → **Loki** → **Grafana**

### Available Dashboards

1. **Application Metrics Dashboard**
   - Request rates and latencies
   - Business metrics (payments created/failed)
   - Batch job status and metrics
   - Error rates

2. **Database &amp; RabbitMQ Health Dashboard**
   - MySQL queries per second
   - Connection pools
   - Slow queries
   - RabbitMQ queue depths
   - Message rates
   - Dead letter queue monitoring

3. **Host Metrics Dashboard**
   - CPU, Memory, Disk, Network usage
   - Container-specific metrics
   - System load average

4. **Logs Dashboard**
   - Log levels over time
   - Error and warning analysis
   - Search by payment ID or trace ID
   - Service-specific log streams

## Key Features

### Payment Service
- RESTful API with validation
- Async event publishing to RabbitMQ
- Structured JSON logging with trace/span IDs
- Custom business metrics
- Dead letter queue for failed messages
- JPA/Hibernate with MySQL

### Batch Job
- Expires stale payments (&gt;24 hours)
- Pushes metrics to Pushgateway
- Short-lived process suitable for cron/K8s CronJob
- Same database as payment service

### Observability
- **Structured Logging**: JSON format with trace context
- **Distributed Tracing**: TraceID and SpanID propagation
- **Business Metrics**: Payment counts, statuses, currencies
- **Technical Metrics**: Latencies, error rates, throughput
- **Infrastructure Metrics**: Database, message broker, host resources

## Configuration

### Environment Variables

Key environment variables that can be customized:

```bash
# MySQL
MYSQL_ROOT_PASSWORD=root_password
MYSQL_DATABASE=payment_db
MYSQL_USER=payment_user
MYSQL_PASSWORD=payment_pass

# RabbitMQ
RABBITMQ_DEFAULT_USER=guest
RABBITMQ_DEFAULT_PASS=guest

# Grafana
GF_SECURITY_ADMIN_USER=admin
GF_SECURITY_ADMIN_PASSWORD=admin
```

### Application Properties

Modify `application.yml` in each service for:
- Database connections
- RabbitMQ configuration
- Logging levels
- Metrics tags

## Development

### Building Services Locally

```bash
# Payment Service
cd payment-service
mvn clean package
java -jar target/payment-service.jar

# Batch Job
cd payment-batch-job
mvn clean package
java -jar target/payment-batch-job.jar
```

### Running Tests

```bash
mvn test
```

## Monitoring Examples

### PromQL Queries

1. Payment creation rate:
```promql
sum(rate(payments_created_total[5m])) by (status)
```

2. P95 latency:
```promql
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{uri="/api/v1/payments"}[5m])) by (le))
```

3. Dead letter queue depth:
```promql
rabbitmq_queue_messages{queue="payment-events-dlq"}
```

### LogQL Queries

1. Errors in last hour:
```logql
{service="payment-service"} | json | level="ERROR" | line_format "{{.timestamp}} {{.message}}"
```

2. Logs for specific payment:
```logql
{service="payment-service"} | json | paymentId="550e8400-e29b-41d4-a716-446655440001"
```

3. Trace all services for a request:
```logql
{service=~"payment-service|payment-batch-job"} | json | traceId="abc123"
```

## Troubleshooting

### Common Issues

1. **Services not starting**: Check logs with `docker-compose logs &lt;service-name&gt;`
2. **Database connection issues**: Ensure MySQL is healthy before starting services
3. **Missing metrics**: Verify Prometheus targets at http://localhost:9090/targets
4. **No logs in Grafana**: Check Promtail status and Loki datasource configuration

### Useful Commands

```bash
# View all logs
docker-compose logs -f

# Restart a specific service
docker-compose restart payment-service

# Clean up everything
docker-compose down -v

# Check resource usage
docker stats
```

## Production Considerations

1. **Security**:
   - Change default passwords
   - Enable TLS for all services
   - Implement proper authentication
   - Use secrets management

2. **Scaling**:
   - Use external MySQL cluster
   - Deploy RabbitMQ in HA mode
   - Scale Prometheus with federation
   - Use Loki with object storage

3. **Persistence**:
   - Mount volumes for data persistence
   - Regular backups for MySQL
   - Prometheus remote storage

4. **Monitoring**:
   - Set up alerting rules
   - Configure notification channels
   - Implement SLOs/SLIs
   - Add custom business dashboards

## License

This is a demonstration project for educational purposes.

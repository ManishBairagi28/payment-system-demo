#!/bin/bash
# Script to run the payment batch job
# This can be scheduled via cron or other schedulers

echo "Starting Payment Batch Job at $(date)"

# Set environment variables if needed
export JAVA_OPTS="-Xmx512m -Xms256m"

# Run the batch job
java $JAVA_OPTS -jar /app/payment-batch-job.jar

# Check exit status
if [ $? -eq 0 ]; then
    echo "Payment Batch Job completed successfully at $(date)"
else
    echo "Payment Batch Job failed at $(date)"
    exit 1
fi

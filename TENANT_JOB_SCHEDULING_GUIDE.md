# Tenant-Aware Job Scheduling Guide

## Overview

The application uses **Quartz Scheduler** with a multi-tenant architecture where each tenant can have their own scheduled jobs that run independently against their specific database.

## Architecture

### Key Components

1. **TenantAwareJob** - Base class for all tenant-specific jobs
   - Automatically sets tenant context before execution
   - Ensures proper cleanup after job completion
   - All tenant jobs must extend this class
2. **TenantJobSchedulerService** - Manages job scheduling per tenant
   - Schedule/unschedule jobs for specific tenants
   - Pause/resume tenant jobs
   - Group jobs by tenant for easy management
3. **Quartz Configuration**
   - Uses **catalog database** for Quartz metadata (not tenant databases)
   - JDBC job store for persistence across restarts
   - Isolated job execution per tenant

## How It Works

```
┌─────────────────────────────────────────────────────────────┐
│  Quartz Scheduler (Catalog DB for job metadata)            │
└─────────────────────────────────────────────────────────────┘
                           │
                           ├─→ Job Group: tenant-company1
                           │   ├─→ daily-report (Cron: 0 0 9 * * ?)
                           │   └─→ weekly-summary (Cron: 0 0 9 * * MON)
                           │
                           ├─→ Job Group: tenant-company2
                           │   ├─→ daily-report (Cron: 0 0 8 * * ?)
                           │   └─→ monthly-invoice (Cron: 0 0 10 1 * ?)
                           │
                           └─→ When job executes:
                               1. TenantAwareJob sets TenantContext
                               2. Job logic accesses tenant's database
                               3. TenantContext cleared after execution
```

## Creating Tenant-Aware Jobs

### Step 1: Create a Job Class

```java
package com.optahaul.mas_java_poc.job;

import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TenantEmailNotificationJob extends TenantAwareJob {

    @Override
    protected void executeInternal(JobExecutionContext context, String tenantId) throws Exception {
        log.info("Sending notifications for tenant: {}", tenantId);

        // Tenant context is already set - repositories will use correct database
        // Example: emailService.sendPendingNotifications();

        log.info("Notifications sent for tenant: {}", tenantId);
    }
}
```

### Step 2: Schedule the Job

```java
// Via REST API
POST /api/tenant/jobs/daily-report
Content-Type: application/json
Host: company1.optahaul.com

{
  "cronExpression": "0 0 9 * * ?"  // 9 AM daily
}

// Or programmatically
@Autowired
private TenantJobSchedulerService jobScheduler;

public void setupTenantJobs(String tenantId) {
    jobScheduler.scheduleJobForTenant(
        tenantId,
        TenantEmailNotificationJob.class,
        "email-notifications",
        "0 */30 * * * ?"  // Every 30 minutes
    );
}
```

## REST API Endpoints

### Schedule Daily Report Job

```bash
curl -X POST http://company1.optahaul.com:8080/api/tenant/jobs/daily-report \
  -H "Content-Type: application/json" \
  -d '{"cronExpression": "0 0 9 * * ?"}'
```

### Unschedule Daily Report Job

```bash
curl -X DELETE http://company1.optahaul.com:8080/api/tenant/jobs/daily-report
```

### Pause All Jobs for Current Tenant

```bash
curl -X POST http://company1.optahaul.com:8080/api/tenant/jobs/pause
```

### Resume All Jobs for Current Tenant

```bash
curl -X POST http://company1.optahaul.com:8080/api/tenant/jobs/resume
```

### Delete All Jobs for Current Tenant

```bash
curl -X DELETE http://company1.optahaul.com:8080/api/tenant/jobs
```

## Automatic Job Management

Jobs are automatically managed during tenant lifecycle:

### Tenant Creation

```java
tenantService.createTenant("company3", "ACME Corp", "acme");
// Tenant is ready for job scheduling
```

### Tenant Suspension

```java
tenantService.suspendTenant("company1");
// All jobs for company1 are automatically PAUSED
```

### Tenant Activation

```java
tenantService.activateTenant("company1");
// All jobs for company1 are automatically RESUMED
```

### Tenant Deletion

```java
tenantService.deleteTenant("company1");
// All jobs for company1 are automatically UNSCHEDULED
```

## Example Use Cases

### 1. Daily Report Generation

```java
@Component
public class TenantDailyReportJob extends TenantAwareJob {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReportService reportService;

    @Override
    protected void executeInternal(JobExecutionContext context, String tenantId) {
        // Queries tenant's database automatically
        List<Book> books = bookRepository.findAll();
        reportService.generateDailyReport(tenantId, books);
    }
}
```

### 2. Email Notifications

```java
@Component
public class TenantNotificationJob extends TenantAwareJob {

    @Override
    protected void executeInternal(JobExecutionContext context, String tenantId) {
        // Send pending notifications for this tenant
        // Access tenant's database to fetch notification queue
    }
}
```

### 3. Data Cleanup

```java
@Component
public class TenantCleanupJob extends TenantAwareJob {

    @Override
    protected void executeInternal(JobExecutionContext context, String tenantId) {
        // Clean up old records in tenant's database
        // Delete expired sessions, logs, etc.
    }
}
```

## Cron Expression Examples

|     Schedule     |   Cron Expression   |         Description          |
|------------------|---------------------|------------------------------|
| Every 15 minutes | `0 */15 * * * ?`    | Run every 15 minutes         |
| Every hour       | `0 0 * * * ?`       | Run at the top of every hour |
| Daily at 9 AM    | `0 0 9 * * ?`       | Run every day at 9:00 AM     |
| Weekdays at 8 AM | `0 0 8 ? * MON-FRI` | Run Monday-Friday at 8:00 AM |
| Weekly on Monday | `0 0 9 ? * MON`     | Run every Monday at 9:00 AM  |
| Monthly on 1st   | `0 0 10 1 * ?`      | Run 1st of month at 10:00 AM |

## Configuration

### Application Properties

```properties
# Quartz uses catalog database for job metadata
spring.quartz.job-store-type=jdbc
spring.quartz.jdbc.initialize-schema=always
spring.quartz.properties.org.quartz.threadPool.threadCount=10
spring.quartz.properties.org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
```

## Best Practices

1. **Always extend TenantAwareJob** - Never implement `Job` directly
2. **Let the framework handle context** - Don't manually set/clear TenantContext
3. **Use @Transactional carefully** - Specify `transactionManager = "tenantTransactionManager"`
4. **Handle exceptions** - Log errors and use JobExecutionException for failures
5. **Make jobs idempotent** - Jobs should handle re-execution safely
6. **Monitor job execution** - Check Quartz logs for job status

## Troubleshooting

### Job Not Executing

- Check if tenant status is "ACTIVE"
- Verify cron expression is valid
- Check Quartz logs for errors
- Ensure job is not paused

### Wrong Database Accessed

- Verify TenantAwareJob is being used (not plain Job)
- Check TenantContext is properly set
- Confirm tenantId is correct in job data map

### Jobs Persist After Tenant Deletion

- Ensure `deleteTenant()` is called (not manual deletion)
- Check Quartz catalog database for orphaned jobs
- Manually clean up: `jobScheduler.unscheduleAllJobsForTenant(tenantId)`

## Database Schema

Quartz tables are created in the **catalog database**:
- `QRTZ_JOB_DETAILS` - Job definitions with tenant context
- `QRTZ_TRIGGERS` - Trigger schedules per tenant
- `QRTZ_CRON_TRIGGERS` - Cron expressions
- `QRTZ_FIRED_TRIGGERS` - Currently executing jobs
- Additional Quartz metadata tables

## Testing

### Test Tenant Job Scheduling

```bash
# Schedule a job for company1
curl -X POST http://company1.optahaul.com:8080/api/tenant/jobs/daily-report \
  -H "Content-Type: application/json" \
  -d '{"cronExpression": "0 */1 * * * ?"}'  # Every minute for testing

# Watch logs - job should execute every minute with company1 context

# Schedule same job for company2 with different schedule
curl -X POST http://company2.optahaul.com:8080/api/tenant/jobs/daily-report \
  -H "Content-Type: application/json" \
  -d '{"cronExpression": "0 */2 * * * ?"}'  # Every 2 minutes

# Each tenant's job runs independently with their own database context
```

## Summary

This tenant-aware job scheduling system provides:
- ✅ **Complete isolation** - Each tenant has independent job schedules
- ✅ **Automatic context management** - Tenant context set/cleared automatically
- ✅ **Centralized job metadata** - Quartz data in catalog DB
- ✅ **Lifecycle integration** - Jobs managed during tenant lifecycle
- ✅ **REST API control** - Tenants can manage their own jobs
- ✅ **Production ready** - Persistent, fault-tolerant scheduling

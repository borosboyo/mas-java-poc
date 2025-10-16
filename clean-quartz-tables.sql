-- Clean all Quartz tables to remove corrupted data
TRUNCATE TABLE qrtz_fired_triggers CASCADE;
TRUNCATE TABLE qrtz_paused_trigger_grps CASCADE;
TRUNCATE TABLE qrtz_scheduler_state CASCADE;
TRUNCATE TABLE qrtz_locks CASCADE;
TRUNCATE TABLE qrtz_blob_triggers CASCADE;
TRUNCATE TABLE qrtz_simprop_triggers CASCADE;
TRUNCATE TABLE qrtz_cron_triggers CASCADE;
TRUNCATE TABLE qrtz_simple_triggers CASCADE;
TRUNCATE TABLE qrtz_triggers CASCADE;
TRUNCATE TABLE qrtz_calendars CASCADE;
TRUNCATE TABLE qrtz_job_details CASCADE;

-- Verify tables are empty
SELECT 'qrtz_job_details' as table_name, COUNT(*) as row_count FROM qrtz_job_details
UNION ALL
SELECT 'qrtz_triggers', COUNT(*) FROM qrtz_triggers
UNION ALL
SELECT 'qrtz_cron_triggers', COUNT(*) FROM qrtz_cron_triggers;


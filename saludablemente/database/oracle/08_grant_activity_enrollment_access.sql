-- Execute as SAL_ACTIVITIES after 07_create_activity_enrollments.sql.
-- No DELETE grant: cancellation is a logical state transition.

GRANT SELECT, INSERT, UPDATE ON SAL_ACTIVITIES.ACTIVITY_ENROLLMENTS TO SALUDABLEMENTE_APP;

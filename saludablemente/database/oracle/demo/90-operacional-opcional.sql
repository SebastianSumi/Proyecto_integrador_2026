-- OPTIONAL operational boundary.
-- This script deliberately performs no DML. Audit, security, download, webhook and
-- interoperability records represent operational activity and must be created only by
-- their owning services or an explicitly approved scenario. No production-like events,
-- credentials, webhook secrets, or access records are simulated by the demo seed.
--
-- The required demo data is contained in scripts 10 through 60.
SELECT 'No optional operational fixtures were inserted.' AS result FROM dual;
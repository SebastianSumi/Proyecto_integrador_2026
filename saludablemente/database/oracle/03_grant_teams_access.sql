-- Execute as SAL_TEAMS after 02_create_teams.sql.
-- The application user receives DML only; schema ownership stays in SAL_TEAMS.

GRANT SELECT, INSERT, UPDATE, DELETE ON SAL_TEAMS.TEAMS TO SALUDABLEMENTE_APP;

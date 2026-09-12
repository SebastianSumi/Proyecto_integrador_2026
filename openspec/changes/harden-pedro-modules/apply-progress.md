# Apply Progress: Harden Pedro Modules

## Current state

Work Unit 1 is complete. Tasks 1.1 through 1.4 have focused or connected Oracle evidence; WU2–4 remain unstarted.

## Completed tasks

- [x] 1.1 RED: Missing `ORACLE_JDBC_URL`, `ORACLE_APP_USERNAME`, `ORACLE_APP_PASSWORD`, and Compose `ORACLE_PASSWORD` are covered without values in assertions or output.
- [x] 1.3 GREEN: Development datasource values are required placeholders, Compose requires an external password, and provisioning scripts use hidden input, pre-DDL validation, and `WHENEVER SQLERROR EXIT SQL.SQLCODE ROLLBACK`.

## Pending tasks

- [x] 1.2 RED: Executed both SQL*Plus missing-input cases against a connected SYSDBA target; each aborted before DDL with only setting-name prompts and no non-empty input echo.
- [x] 1.4 REFACTOR: Verified the existing catalog-backed 01–08 state after an authorized SAL_ACTIVITIES credential reconciliation; completed owner/table/grant postflight, hidden-input credential suppression, and safe operations documentation.
- [ ] 2.1 through 4.5: Unchanged and unstarted.

## Retained command evidence

All paths below are relative to the working directory unless stated otherwise. No secret value is reproduced in this artifact.

### RED chronology and limitation

The test file was written before production files were changed. The first retained focused invocation was:

```powershell
mvn -Dtest=SecureOracleRuntimeTest test
```

- Working directory: `C:\Users\pfloa\PedroProjects\MyRepositories\Proyecto_integrador_2026\saludablemente`
- Environment shape: no `MAVEN_OPTS` override was set by the command.
- Result: Maven reported `Could not create local repository at C:\.m2\repository`; test compilation/execution did not start.
- Exit-code limitation: the retained command record did not capture `$LASTEXITCODE`; the non-zero test-process exit is therefore not asserted here.

A later pre-production retry used this retained command shape:

```powershell
$env:MAVEN_OPTS='-Dmaven.repo.local=C:\Users\pfloa\.m2\repository'; mvn -Dtest=SecureOracleRuntimeTest test
```

- Same working directory; `MAVEN_OPTS` supplied only the Maven repository location.
- Result: Maven reached test compilation but reported `AccessDeniedException` for `spring-boot-jdbc-4.0.7.jar`; no test assertion ran.
- Exit-code limitation: the retained record reports `BUILD FAILURE` but does not retain `$LASTEXITCODE`.

Therefore, retained evidence proves that the RED test source preceded production edits, but it **does not prove a functional failing RED assertion** before those edits. Cache-access failures blocked that proof. No stronger chronology claim is made.

### GREEN focused Maven command

```powershell
$env:MAVEN_OPTS='-Dmaven.repo.local=C:\Users\pfloa\.m2\repository'; mvn -Dtest=SecureOracleRuntimeTest test
```

- Working directory: `C:\Users\pfloa\PedroProjects\MyRepositories\Proyecto_integrador_2026\saludablemente`
- Environment shape: `MAVEN_OPTS` set to `-Dmaven.repo.local=C:\Users\pfloa\.m2\repository`; no Oracle setting value was passed on the command line.
- Result: Maven reported `BUILD SUCCESS`; `SecureOracleRuntimeTest` ran 12 tests with 0 failures, 0 errors, and 0 skips.
- Exit result: success (Maven reported `BUILD SUCCESS`; no separate `$LASTEXITCODE` line was retained).
- Bounded non-secret evidence: Spring reported one `Missing required configuration: <setting>` failure per omitted Oracle setting during environment preparation. The retained output showed no datasource/Hikari initialization before those failures.

### Compose missing-`ORACLE_PASSWORD` scenario

The retained PowerShell wrapper created an explicit empty env file in `%TEMP%`, then launched this exact child command:

```text
docker compose --env-file %TEMP%\saludablemente-empty-compose.env -f database/docker/compose-dev.yml config
```

- Working directory: `C:\Users\pfloa\PedroProjects\MyRepositories\Proyecto_integrador_2026\saludablemente`
- Environment shape: the child process removed `ORACLE_PASSWORD`; it used the explicit empty env file and did not invoke `up` or any container-start command.
- Exit result: `EXIT=1`.
- Bounded non-secret output evidence: `MISSING_SETTING_IDENTIFIED=True; CONTAINERS_STARTED=False`.
- Secret-suppression check: the wrapper required output to contain `ORACLE_PASSWORD` and rejected a rendered `ORACLE_PASSWORD: <value>` line; it completed without that rejection. No password value is retained.

### SQL*Plus `/nolog` attempts

Both attempts used `sqlplus -S /nolog` from `C:\Users\pfloa\PedroProjects\MyRepositories\Proyecto_integrador_2026\saludablemente`, with no connection target or credentials supplied by the command.

| Script | Exact stdin input shape | Retained exit/result | Bounded evidence and limitation |
|---|---|---|---|
| `database/oracle/01_provision_users.sql` | `@database/oracle/01_provision_users.sql`, then two blank lines, then `exit` | The first retained wrapper reported `EXIT=0`; a later retained inspection did not retain an exit code. | Later inspection recorded `CONTAINS_SP2_0640=True`, `CONTAINS_CREATE_USER=False`, and `CONTAINS_Connected_to=False`. This proves that `/nolog` was disconnected and emitted no DDL statement; it cannot prove the script's connected pre-DDL missing-input branch. |
| `database/oracle/04_provision_activities_user.sql` | `@database/oracle/04_provision_activities_user.sql`, then one blank line, then `exit` | The retained wrapper reported `EXIT=0`. | The retained summary confirms `CREDENTIAL_VALUE_ECHOED=False`. It did **not** retain an `SP2-0640` match for this script, so none is claimed. With `/nolog` and no target, this attempt is not connected-Oracle proof. |

### Connected SQL*Plus missing-input evidence

A live Docker target was verified before this run: container `saludablemente-oracle` was running and healthy, image `gvenzl/oracle-free:23-slim`, host mapping `1522:1521`, and ID prefix `2083c48daf08`. A password-free `docker exec` SQL*Plus OS-authentication probe returned `CONNECTION_OK` and `CREATE_USER_PRIVILEGE=TRUE`; no environment values were inspected or printed.

The provisioning users were absent before the cases. Each repository script was copied only to an ephemeral container `/tmp` path, executed using `sqlplus -L -S / as sysdba`, supplied blank interactive inputs, and then removed. Bounded results:

| Script | Exit | Required setting names | Non-empty prompt values echoed | DDL echoed | Post-case users |
|---|---:|---|---:|---:|---|
| `01_provision_users.sql` | non-zero | present | 0 | false | absent |
| `04_provision_activities_user.sql` | non-zero | present | 0 | false | absent |

This is connected pre-DDL evidence for task 1.2 only. **Historical limitation:** at the time of this task 1.2 run, full provisioning and postflight had not yet been attempted because the interactive inputs were unavailable. That limitation was resolved later by the task 1.4 controlled resume recorded below; no value is recorded here.
## RED → GREEN → REFACTOR evidence

| Stage | Evidence | Result |
|---|---|---|
| RED | Source chronology plus the two Maven attempts above. | Test source preceded production changes, but functional RED execution is unproven because Maven cache access blocked compilation. |
| GREEN | Focused Maven command above. | PASS — 12 tests, 0 failures/errors/skips. |
| GREEN | Compose missing-password configuration scenario above. | PASS — exit 1, missing setting identified, no container-start command, no rendered password-value line. |
| REFACTOR | Static script hardening was implemented: hidden `ACCEPT`, pre-DDL `RAISE_APPLICATION_ERROR`, `SET ECHO OFF`, `SET VERIFY OFF`, and SQL error exits. | Historical status after task 1.3: connected Oracle execution, postflight, and operations documentation were pending. Task 1.4 later resolved that status with credential reconciliation, catalog-backed 01–08 verification, postflight, and operations documentation. |

## Work Unit Evidence

| Evidence | Result |
|---|---|
| Focused test command and exact result | See the GREEN focused Maven command above: `MAVEN_OPTS=-Dmaven.repo.local=C:\\Users\\pfloa\\.m2\\repository`, working directory `saludablemente`, Maven `BUILD SUCCESS`, 12 tests, 0 failures/errors/skips. |
| Runtime harness command/scenario and exact result | Compose `config` scenario: exit 1, missing `ORACLE_PASSWORD` identified, no container-start command, no rendered value line. Connected Docker SQL*Plus OS-authentication evidence proves both blank-input scripts abort before DDL with no non-empty prompt echo. Historical task 1.2 evidence then lacked interactive inputs; task 1.4 later reconciled the credential, completed hidden authentication and full postflight, and verified the already-applied 01–08 catalog state. |
| Rollback boundary | Revert `saludablemente/src/main/resources/application-dev.yaml`, `saludablemente/database/docker/compose-dev.yml`, `saludablemente/database/oracle/01_provision_users.sql`, `saludablemente/database/oracle/04_provision_activities_user.sql`, `saludablemente/src/main/java/pe/edu/upeu/saludablemente/configuration/OracleRuntimePreflight.java`, `saludablemente/src/main/java/pe/edu/upeu/saludablemente/configuration/OracleRuntimeEnvironmentPostProcessor.java`, `saludablemente/src/main/resources/META-INF/spring.factories`, and `saludablemente/src/test/java/pe/edu/upeu/saludablemente/configuration/SecureOracleRuntimeTest.java` together. |

## Historical scope boundary: task 1.2 run

This subsection describes the earlier connected task 1.2 run: only its evidence and OpenSpec tracking changed then; no WU2–4 source, tests, Maven configuration, migrations, API error handling, Team identity, Oracle harness, documentation, provisioning DDL, commit, branch, PR, push, or RDD review were performed at that point. The later task 1.4 controlled resume is separately recorded below and updated only WU1 operations documentation and OpenSpec progress.
## WU1 task 1.4 connected resume evidence

An authorized PDB SYSDBA rotation changed only `SAL_ACTIVITIES` to the already-generated ignored local `SAL_ACTIVITIES_PASSWORD`. Objects, grants, and data were retained. The password was supplied only through non-echoing SQL*Plus standard input.

| Check | Result |
|---|---|
| Rotation | SQL*Plus exit `0`; `SAL_ACTIVITIES_ROTATION_OK`; no `ORA-*`/`SP2-*`; no credential value in output. |
| Hidden authentication | SQL*Plus exit `0`; `SAL_ACTIVITIES_HIDDEN_AUTH_OK`; no `ORA-*`/`SP2-*`; no credential value in output. |
| Remaining scripts 05–08 | Not rerun: read-only catalog evidence already found every object and grant they create, so rerunning non-idempotent DDL was unsafe. |
| Full postflight | SQL*Plus exit `0`; all three accounts `OPEN`; 4 required tables and all 12 expected application grants present; no `ORA-*`/`SP2-*`. |
| Documentation | `docs/next-iteration.md` now records configuration names, safe local injection, verified state, rotation, verification, and rollback without values. |

### WU1 credential-injection and rollback boundary

- Local values are intentionally omitted from all versioned artifacts. The Git-ignored `.env` supplies `ORACLE_PASSWORD`, `ORACLE_JDBC_URL`, `ORACLE_APP_USERNAME`, `ORACLE_APP_PASSWORD`, `SAL_TEAMS_PASSWORD`, and `SAL_ACTIVITIES_PASSWORD` to their respective container, Spring, and SQL*Plus consumers.
- Roll back runtime code/configuration as one unit: Compose, `application-dev.yaml`, scripts 01 and 04, secure-runtime configuration/tests, and `docs/next-iteration.md`.
- A database rollback requires DBA authorization and provenance/backup review. Do not drop schemas, users, or application data that may predate this work unit.

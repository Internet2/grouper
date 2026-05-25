# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture Overview

Grouper is Internet2's enterprise access management system with a multi-module Maven project structure.
Most modules are Java source code.

- **grouper-parent/**: Parent POM defining dependencies and build configuration (Java 17, Maven build)
- **grouper/**: Core API module containing the main Grouper logic and domain objects
- **grouper-ui/**: Web UI module (JSP/Servlet-based with modern features)
- **grouper-ws/**: Web Services module providing REST/SCIM APIs
- **grouper-misc/**: Various utility modules:
  - **grouperClient/**: Client library for accessing Grouper APIs
  - **grouper-installer/**: Installation and upgrade utilities
  - **grouperActivemq/**: ActiveMQ integration


See `ARCHITECTURE.md` for the per-area breakdown — core APIs, UI classes, UI templates, web services, authentication and security, provisioning system.

## Configuration

Grouper ships with base property files named `*.base.properties`. User-managed files with the same name minus `.base` override them. Database-backed configuration takes precedence over both and is edited via the UI (history is kept in a separate table — do not edit the database directly).

When editing configuration that shows up in the UI as a wizard, add externalized text for the label and description.

See `CONFIGURATION.md` for the full list of base and user-managed property files with descriptions, plus details on the database-backed config.

## Git Repository Information

- Current branch: GROUPER_5_BRANCH
- Main development branch: GROUPER_5_BRANCH (for PRs)
- Other actively maintained branches: GROUPER_4_BRANCH

Released versions are tagged in the format GROUPER_RELEASE_a.b.c, where a.b.c is the version number.

## Issue Tracking
- JIRA issues: https://todos.internet2.edu/browse/<JIRA-ID>
- GitHub repository: https://github.com/Internet2/grouper
- Each distinct piece of work should go in its own Jira.  If there is a followup to a previous recent item, it can have (commit 2) after the Jira ticket number in the commit message

## Development Guidelines

### Java language constructs
- Avoid Java streams (`.stream().map().filter().collect(...)`) and lambda expressions used as functional arguments. Use explicit `for` loops and `if` statements instead.
- Avoid `Optional`, `Collectors`, method references (`Foo::bar`), and other Java 8+ functional-style constructs in new code.
- Anonymous inner classes are fine — long-established IDE and debugger support, straightforward to read and step through.
- Rationale: streams and lambdas produce weak stack traces, awkward breakpoint behavior, and are harder to read for long-time Grouper developers maintaining the codebase. Explicit loops and conditionals are the house style.
- When editing an existing block of stream-based code as part of an unrelated change, you may convert it to explicit form opportunistically. Don't make sweeping stream removal a goal in itself.

### Commit Messages
- Must exactly match the JIRA issue title
- Format: `GRP-XXXX: [exact JIRA title]`
- Example: `GRP-5599: Script daemon output remove info about script type and source code`

### SQL Batching
- When writing code that performs SQL inserts/updates/deletes across multiple rows, use batched execution.
- In Grouper, use `GcDbAccess.batchBindVars(List<List<Object>>).executeBatchSql()` instead of calling `executeSql()` in a loop.
- If batching is genuinely not possible (e.g., each row needs a different SQL string, or results from one row drive the next), stop and discuss with the user before falling back to row-by-row.

### DDL Comments
- Any DDL change — new table, new column, or new view — must ship with `COMMENT ON TABLE`/`COMMENT ON VIEW`/`COMMENT ON COLUMN` statements.
- Add the comments to both `GrouperDdl_Grouper_install_postgres.sql` and `GrouperDdl_Grouper_install_oracle.sql`. MySQL does not support `COMMENT ON`, so skip the mysql install file.
- For views: postgres uses `COMMENT ON VIEW <name>`, oracle uses `COMMENT ON TABLE <name>` (oracle treats views as tables for comments).
- Mirror the same comments in the Java upgrade task, wrapped in `if (GrouperDdlUtils.isPostgres() || GrouperDdlUtils.isOracle()) { ... }`, using `GrouperDdlUtils.isPostgres() ? "VIEW" : "TABLE"` when commenting a view.

## Primary Build Commands

**Maven (Recommended)**:
```bash
# Build entire project from grouper-parent/
cd grouper-parent
mvn clean install dependency:copy-dependencies -Dcheckstyle.skip

# Build specific module
mvn -f grouper-misc/grouperClient clean install dependency:copy-dependencies
mvn -f grouper clean install dependency:copy-dependencies
mvn -f grouper-ui clean install dependency:copy-dependencies
```

## "Quickstart" Demo application

See the folder `grouper/misc/quickstart-docker-compose` for a docker-compose setup that can run a reference Grouper system. Details for running the
application can be found in the README.md file in that folder

## Unit testing

See `TESTING.md` for build prerequisites, the test PostgreSQL Docker container setup, the full-suite command, and the list of named suites that can be run individually.

## AI/GSH development resources

The `grouper/misc/aiGsh/` directory contains reference material for AI-assisted Grouper work: GSH writing rules (`aiGshInstructions.txt`), GSH and SQL examples (`aiGsh.txt`, `aiGshSql.txt`), the full PostgreSQL DDL (`aiGshDdl.txt`), Grouper wiki text (`grouperWikiAi.txt`), and the Grouper WS Swagger spec (`grouperWs_swagger.json`). Read `aiGshInstructions.txt` before writing GSH scripts.

## Deployment architecture

Grouper runs in Docker containers. Generally the UI, WS, and daemon run in different containers and each layer has one to many nodes, so do not assume that you can set things in static variables and all traffic will see that data. State must be stored in the database or elsewhere.

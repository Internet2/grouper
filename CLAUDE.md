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


### Key Development Areas

**Core APIs** (`grouper/src/grouper/edu/internet2/middleware/grouper/`):
- Groups, Stems (folders), Members, Privileges
- Rules engine and change log
- Provisioning and external system integration

**UI classes** (`grouper-ui/java/src/edu/internet2/middleware/grouper/`):
- Custom MVC design
- Java beans for UI objects
- HTTP, servlet, and authentication handling

**UI Templates** (`grouper-ui/webapp/WEB-INF/grouperUi2/`):
- JSP-based templates with modern JavaScript
- Template system for group/folder management

**Web Services** (`grouper-ws/grouper-ws/src/`):
- REST and SCIM APIs
- SCIM 2.0 implementation

### Authentication & Security
- Built-in authentication with GrouperSystem user
- External authentication via filters/valves
- Subject API for user/group resolution
- Rules-based authorization

### Provisioning System
Grouper includes extensive provisioning capabilities:
- SQL provisioner for database targets
- LDAP provisioner
- SCIM provisioner
- Custom provisioners via plugin architecture


### Configuration

Grouper ships will base configuration properties, which can be overridden by the user. Base property files and with `*.base.properties`:

- `grouper-misc/grouperClient/conf/grouper.client.base.properties`
- `grouper/conf/grouper.hibernate.base.properties` - Database connectivity
- `grouper/conf/grouperText/grouper.textNg.en.us.base.properties` -internationalization (i18n) strings.  User facing text should be in this file.
- `grouper/conf/grouperText/grouper.textNg.fr.fr.base.properties` -internationalization (i18n) strings
- `grouper/conf/grouper-loader.base.properties` - External system and daemon job configuration
- `grouper/conf/grouper-ws-ng.base.properties` - Web services settings
- `grouper/conf/grouper-ui-ng.base.properties` - Web UI settings
- `grouper/conf/subject.base.properties` - Subject source definitions
- `grouper/conf/grouper.base.properties` - Core Grouper configuration
- `grouper/conf/grouper.cache.base.properties`


User-managed configuration are in corresponding files without the `.base` in the filename. Note that grouperText, grouper-ui and grouper-ws have slightly different names than this standard. File morphString.properties has no base properties file.

- `grouper-misc/grouperClient/conf/grouper.client.properties`
- `grouper/conf/grouper.hibernate.properties`
- `grouper/conf/grouperText/grouper.text.en.us.properties`
- `grouper/conf/grouperText/grouper.text.fr.fr.properties`
- `grouper/conf/grouper-loader.properties`
- `grouper/conf/grouper-ws.properties`
- `grouper/conf/grouper-ui.properties`
- `grouper/conf/subject.properties`
- `grouper/conf/grouper.properties`
- `grouper/conf/grouper.cache.properties`
- `morphString.properties` - String encryption configuration

There is also configuration in the database which takes precedence over the non base config files.  This is edited in the UI since history in kept in another table, so this should not be edited in the database but it could be viewed in the database or there is an MCP tool to look at configuration (preferred).

When editing configuration that shows up in the UI as a wizard, add externalized text for the label and description.

## Git Repository Information

- Current branch: GROUPER_7_BRANCH
- Main development branch: GROUPER_7_BRANCH (for PRs)
- Other actively maintained branches: GROUPER_4_BRANCH, GROUPER_6_BRANCH

Released versions are tagged in the format GROUPER_RELEASE_a.b.c, where a.b.c is the version number.

## Issue Tracking
- JIRA issues: https://todos.internet2.edu/browse/<JIRA-ID>
- GitHub repository: https://github.com/Internet2/grouper
- Each distinct piece of work should go in its own Jira.  If there is a followup to a previous recent item, it can have (commit 2) after the Jira ticket number in the commit message

## Development Guidelines

### Commit Messages
- Must exactly match the JIRA issue title
- Format: `GRP-XXXX: [exact JIRA title]`
- Example: `GRP-5599: Script daemon output remove info about script type and source code`

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

Before running the unit tests, get the latest snapshot and build the java libraries, and incorporate dependencies:

```bash
git checkout GROUPER_7_BRANCH
git pull

mvn -f grouper-parent clean package
mvn -f grouper dependency:copy-dependencies
```

Unit testing requires an associated database. Use the following Docker command to start with a blank PostgreSQL one:

```bash
docker run --name grouper-ci-pgsql -d -e "POSTGRES_USER=grouper" -e "POSTGRES_PASSWORD=test" -e "POSTGRES_DB=grouper" -p 15432:5432  postgres -N 1000
```

To run the full suite of tests:

```bash
CP=grouper/misc/ci-test/confForTestPGSQL
CP=$CP:$(compgen -G "grouper/target/grouper-[0-9].[0-9].[0-9]*.jar" | grep -v -- '-sources.jar' | tr '\n' ':' | sed -e 's/::/:/;s/:$//')
CP=$CP:"grouper/target/dependency/*"
CP=$CP:grouper/conf

java -classpath "$CP" \
  -Dgrouper.allow.db.changes=true \
  -Dgrouper.home=./ \
  -Xms80m -Xmx640m \
  --add-opens java.base/java.lang=ALL-UNNAMED \
  --add-opens java.base/java.util=ALL-UNNAMED \
  --add-opens java.sql/java.sql=ALL-UNNAMED \
  edu.internet2.middleware.grouper.AllTests \
  -all -noprompt > test.log 2>&1
```

To run a specific suite, Add the suffix of the suite after the AllTests class:

- MembershipFinderTest
- MembershipSaveTest
- CompositeSaveTest
- TestStemFinder
- TestComposite
- TestGroupType
- TestSession
- TestGrouperVersion
- TestStem
- TestField
- TestCompositeU
- TestStemApi
- TestStemIntegration
- TestGroupTypeIncludeExclude
- TestCompositeI
- TestCompositeModel
- TestGrouperSession
- TestRegistrySubject
- PrivilegeGroupInheritanceSaveTest
- (not normally run) edu.internet2.middleware.grouperClient.AllClientConfigTests
- abac.AllAbacTests
- app.AllAppTests
- attr.AllAttributeTests
- audit.AllAuditTests
- changeLog.AllChangeLogTests
- client.AllClientTests
- cfg.AllConfigTests
- entity.AllEntityTests
- (not normally run) ddl.AllDdlTests
- app.duo.AllDuoProvisionerTests
- app.duo.role.AllDuoRoleProvisionerTests
- externalSubjects.AllExternalSubjectTests
- filter.AllFilterTests
- app.google.AllGoogleProvisionerTests
- group.AllGroupTests
- cache.AllGrouperCacheTests
- ui.AllGrouperUiTests
- hibernate.AllHibernateTests
- hooks.AllHooksTests
- internal.dao.AllInternalDaoTests
- log.AllLogTests
- member.AllMemberTests
- membership.AllMembershipTests
- messaging.AllMessagingTests
- misc.AllMiscTests
- permissions.AllPermissionsTests
- plugins.AllPluginsTests
- pit.AllPITTests
- privs.AllPrivsTests
- rules.AllRulesTests
- service.AllServiceTests
- sqlCache.AllSqlCacheTests
- stem.AllStemTests
- subj.AllSubjectTests
- tableIndex.AllTableIndexTests
- userData.AllUserDataTests
- util.AllUtilTests
- validator.AllValidatorTests
- xml.AllXmlTests
- xmpp.AllXmppTests

After the test completes, delete the running `grouper-ci-pgsql` Docker container.


##   AI/GSH Development Resources (grouper/misc/aiGsh/)

This directory contains comprehensive reference materials for AI-assisted development of Grouper Shell (GSH)
scripts, SQL queries, and understanding Grouper's architecture:

### GSH Script Development Files

aiGshInstructions.txt (1.7 KB)
- Instructions for AI tools on how to write GSH templates
- Key requirements:
  - Use Java syntax compatible with Groovy (no multi-line statements)
  - All SQL queries should be in triple-quoted strings
  - Input variables must start with gsh_input_
  - GSH templates should extend GshTemplateV2
  - Always use standard imports regardless of necessity

aiGsh.txt (105 KB)
- Comprehensive training file with GSH script examples
- Contains labeled code blocks demonstrating:
  - Input variable handling
  - Database queries and JDBC operations
  - Group/stem/member operations
  - Attribute management
  - Provisioning tasks
  - Error handling patterns
  - Validation logic
  - Template output formatting

aiGshSql.txt (10 KB)
- Collection of SQL query examples for common Grouper operations:
  - Selecting group members by subject ID
  - Querying attribute assignments to users
  - Finding groups with specific attributes
  - Membership history queries (point-in-time)
  - Admin privilege queries
  - Complex membership analysis queries
- References wiki documentation at spaces.at.internet2.edu

### Database Schema Reference

aiGshDdl.txt (561 KB)
- Complete PostgreSQL DDL schema for Grouper database
- Includes:
  - All table definitions (groups, stems, members, attributes, etc.)
  - Indexes and constraints
  - PostgreSQL-specific functions (e.g., grouper_to_timestamp)
  - View definitions for membership and provisioning
- Note: Grouper supports PostgreSQL (recommended), Oracle, and MySQL

### Documentation Resources

grouperWikiAi.txt (53 KB)
- Extracted Grouper wiki documentation in text format
- Contains user guides and operational procedures
- Includes information on:
  - Member management workflows
  - UI navigation and privilege requirements
  - Step-by-step operational guides

grouperWs_swagger.json (485 KB)
- OpenAPI/Swagger 2.0 specification for Grouper Web Services v4
- Comprehensive REST API documentation including:
  - All WS operations (messaging, groups, stems, members, etc.)
  - Request/response schemas
  - Authentication requirements
  - Endpoint paths and parameters
- Base URL format: https://grouper.institution.edu/grouper-ws/servicesRest/vA_A_MAF/

Usage Notes

These files serve as knowledge base resources for:
- AI-assisted GSH script generation
- Understanding Grouper database schema for custom queries
- Web Services API integration
- Learning Grouper operational patterns

When writing GSH scripts, always reference aiGshInstructions.txt for coding standards and aiGsh.txt for implementation patterns.

### Deployment architecture

Grouper runs in Docker containers.  Generally the UI, WS, and daemon run in different containers and each layer has one to many nodes so do not assume that you can set things in static variables and all traffic will see that data.  State must be stored in the database or elsewhere.

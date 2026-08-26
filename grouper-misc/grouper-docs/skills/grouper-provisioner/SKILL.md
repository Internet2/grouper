---
name: grouper-provisioner
description: >
  Guide for creating a new Grouper provisioner from scratch, including all required Java classes,
  configuration, mock service handler, tests, base properties, externalized text, Hibernate mappings,
  and documentation. Use this skill whenever the user wants to create a new provisioner, add a new
  target system integration, or asks what pieces are needed for a provisioner. Also trigger when
  the user says things like "new provisioner", "provision to X", "create provisioner for",
  "what files does a provisioner need", or "provisioner checklist".
---

# Grouper Provisioner Creation Checklist

When building a new Grouper provisioner for a target system, the following pieces are required.
Use an existing provisioner (e.g. Datadog, TrueFoundry) as a reference implementation.
All provisioner source files go under `grouper/src/grouper/edu/internet2/middleware/grouper/app/{provisionerName}/`.

---

## 1. Model Classes (Java)

Domain objects representing the target system's entities. Each model class typically needs:
- Fields with getters/setters
- `fromJson(JsonNode)` — parse from API response
- `toJson()` / `toSomeEndpointJson()` — serialize for API requests
- `toProvisioningGroup()` / `toProvisioningEntity()` — convert to Grouper provisioning objects
- `fromProvisioningGroup()` / `fromProvisioningEntity()` — convert from Grouper provisioning objects
- `createTable*()` static method — DDL for the mock database table
- `toString()` override

Typical model classes:
- **Group** (e.g. `TrueFoundryGroup.java`) — teams, roles, or other group-like objects
- **User** (e.g. `TrueFoundryUser.java`) — user/entity objects
- **Membership** (e.g. `TrueFoundryMembership.java`) — membership/association objects

## 2. Hibernate Mappings (HBM XML)

One `.hbm.xml` file per model class, in the same package directory. These map model classes to
mock database tables for testing.

- `{Name}Group.hbm.xml`
- `{Name}User.hbm.xml`
- `{Name}Membership.hbm.xml`

Must also register in `Hib3DAO.java` — add `<mapping resource="...hbm.xml"/>` entries.

## 3. API Commands Class

Static methods wrapping HTTP calls to the target API. Pattern:

- `{Name}ApiCommands.java`
- Private `executeMethod()` for HTTP calls with auth, logging, error handling
- Private `attachAuthentication()` for auth headers
- Public methods for each API operation (retrieve, create, update, delete)
- Higher-level public methods that encapsulate retrieve-modify-write patterns
  (e.g. `addTeamMember` retrieves current state, modifies, PUTs back)
- Lower-level methods that are only used internally should be private
- `parseIgnoreSet()` and `isIgnored()` helpers for ignore filtering

## 4. Log Class

Simple static logging utility.

- `{Name}Log.java`
- Static method to log debug maps with elapsed time

## 5. Provisioner Class

Main entry point extending `GrouperProvisioner`.

- `{Name}Provisioner.java`
- Override `grouperTargetDaoClass()` — return the TargetDao class
- Override `grouperProvisioningConfigurationClass()` — return the config class
- Override `registerProvisioningBehaviors()` — set membership type (typically `membershipObjects`)

## 6. Provisioner Configuration Class

Extends `GrouperProvisioningConfiguration`. Holds provisioner-specific config properties.

- `{Name}ProvisionerConfiguration.java`
- Fields for each config property with getters/setters
- Override `configureSpecificSettings()` — read config from `retrieveConfigString()`
- Use `GrouperUtil.defaultIfBlank()` for defaults
- Use `GrouperUtil.booleanValue()` for boolean configs

## 7. Target DAO Class

Extends `GrouperProvisionerTargetDaoBase`. Implements CRUD operations against the target.

- `{Name}TargetDao.java`
- Override `loggingStart()` / `loggingStop()` for HTTP client logging
- Implement retrieval methods: `retrieveAllGroups`, `retrieveAllEntities`, `retrieveEntity`, `retrieveGroup`
- Implement mutation methods: `insertEntity`, `insertGroup`, `updateEntity`, `updateGroup`, `deleteEntity`, `deleteGroup`
- Implement membership methods: `insertMemberships`, `deleteMemberships` (plural for batch)
- Override `registerGrouperProvisionerDaoCapabilities()` — declare supported operations
- Call `addTargetDaoTimingInfo()` in finally blocks

## 8. Mock Service Handler

Simulates the target API for testing. Registered in `MockServiceServlet.java`.

- `{Name}MockServiceHandler.java`
- Extends `MockServiceHandler`
- Routes HTTP methods/paths to handler methods
- Reads/writes mock database tables via `HibernateSession` or `GcDbAccess`
- Must accurately simulate target API behavior (e.g. role replacement, conflict handling)

Must also register in `MockServiceServlet.java` — add routing for the mock URL path.

## 9. Provisioning Configuration Class (UI Registration)

Extends `ProvisioningConfiguration`. Maps the UI config system to the provisioner class.
This is what makes the provisioner appear in the UI dropdown.

- `{Name}ProvisioningConfiguration.java` (note: different from `{Name}ProvisionerConfiguration.java`)
- Override `getConfigFileName()` — return `ConfigFileName.GROUPER_LOADER_PROPERTIES`
- Override `getConfigItemPrefix()` — return `"provisioner." + configId + "."`
- Override `getConfigIdRegex()` — return `"^(provisioner)\\.([^.]+)\\.(.*)$"`
- Override `getPropertySuffixThatIdentifiesThisConfig()` — return `"class"`
- Override `getPropertyValueThatIdentifiesThisConfig()` — return the provisioner class name

Must also register in `ProvisioningConfiguration.java` — add to `configClassNamesList` in the static block, and add the import.

## 10. Base Properties

`grouper/conf/grouper-loader.base.properties` — add a section for the new provisioner with
commented-out property definitions. Each property needs `{valueType, order, ...}` metadata
for the UI to render configuration forms. Include:

- `class` (readOnly)
- External system config ID (dropdown from `WsBearerTokenExternalSystem` or similar)
- Provisioner-specific settings (ignore lists, feature flags, etc.)
- `targetGroupAttribute.$i$.name` with dropdown of valid attribute names
- `targetEntityAttribute.$i$.name` with dropdown of valid attribute names

## 11. Externalized Text (i18n)

`grouper/conf/grouperText/grouper.textNg.en.us.base.properties` — add entries for:

- `config.{Name}ProvisionerConfiguration.title` — display name in UI dropdown
- `config.{Name}ProvisionerConfiguration.description` — description shown in UI
- `config.{Name}ProvisionerConfiguration.attribute.{attrName}.label` — label for each config property
- `config.{Name}ProvisionerConfiguration.attribute.{attrName}.description` — description for each config property
- `config.{Name}ProvisionerConfiguration.attribute.targetGroupAttribute.i.name.label`
- `config.{Name}ProvisionerConfiguration.attribute.targetGroupAttribute.i.name.description`
- `config.{Name}ProvisionerConfiguration.attribute.targetEntityAttribute.i.name.label`
- `config.{Name}ProvisionerConfiguration.attribute.targetEntityAttribute.i.name.description`
- Any metadata labels/descriptions for membership metadata attributes

## 12. Test Classes

Under `grouper/src/test/edu/internet2/middleware/grouper/app/{provisionerName}/`:

- **`All{Name}ProvisionerTests.java`** — JUnit test suite aggregator
- **`{Name}ProvisionerTestConfigInput.java`** — builder-pattern config input (configId, extraConfig map, groupOfUsersToProvision)
- **`{Name}ProvisionerTestUtils.java`** — static helpers:
  - `setup{Name}ExternalSystem()` — configure mock external system endpoint
  - `configureProvisionerSuffix()` — set a single provisioner config property
  - `configure{Name}Provisioner()` — set all default config + full sync + incremental jobs
- **`{Name}ProvisionerTest.java`** — extends `GrouperProvisioningBaseTest`:
  - API-level tests (direct mock DB + API command tests)
  - Provisioner full sync tests (gated behind `tomcatRunTests()`)
  - Provisioner incremental sync tests (gated behind `tomcatRunTests()`)
  - Helper methods for test setup, mock data creation, provisioning attribute attachment

## 13. Documentation (HTML)

Four HTML documents in `grouper/temp/docs/` (or `grouper/temp/trash/` for institution-specific):

1. **`{name}ExternalSystem.html`** — how to configure the external system connection (endpoint, auth)
2. **`{name}Provisioner.html`** — user-facing provisioner doc: config properties table, folder structure, attribute tables, CRUD operations, behavioral notes
3. **`{name}ProvisionerDeveloperNotes.html`** — API reference with request/response examples for each endpoint, paging details, rate limiting, error codes
4. **`{Name}_Institution.html`** (in `temp/trash/`) — institution-specific setup: folder paths, group links, provisioning updater/reader groups, specific configuration choices

## 14. SyncObjectMetadata (Optional)

If the provisioner has custom membership metadata (e.g. team manager flag):

- `{Name}SyncObjectMetadata.java`
- Registers metadata fields that appear on provisioning attribute assignments in the UI

---

## Files Modified in Existing Code

When creating a new provisioner, the following existing files need modifications:

| File | Change |
|---|---|
| `ProvisioningConfiguration.java` | Add import and register in `configClassNamesList` |
| `Hib3DAO.java` | Add `<mapping resource="...hbm.xml"/>` for each model class |
| `MockServiceServlet.java` | Register the mock service handler for the URL path |
| `grouper-loader.base.properties` | Add provisioner config section |
| `grouper.textNg.en.us.base.properties` | Add externalized text entries |
| `AllAppTests.java` or suite file | Optionally add test suite reference |

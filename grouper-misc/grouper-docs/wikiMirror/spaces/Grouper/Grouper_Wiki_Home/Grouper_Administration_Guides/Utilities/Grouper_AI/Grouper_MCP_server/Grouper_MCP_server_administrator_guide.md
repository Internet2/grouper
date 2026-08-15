---
title: "Grouper MCP server - administrator guide"
space: Grouper
pageId: 28554349
version: 19
lastUpdated: 2026-07-24T16:53:01.139Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide
---

## Introduction

This guide is for Grouper administrators and operators who want to enable, configure, and manage the built-in MCP server. It covers enabling MCP, all configuration properties, authorization groups that control who can use MCP, registration security, and multi-container deployment considerations.

For a high-level introduction to the Grouper MCP server, see the [overview](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547487/Grouper+MCP+server). For end-user setup instructions and tool documentation, see the [user guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554356/Grouper+MCP+server+-+user+guide). For architecture and protocol details, see the [technical reference](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554339/Grouper+MCP+server+-+technical+reference).

## Enabling the MCP server

The MCP server and OAuth authentication are controlled by two separate configuration properties in `grouper.hibernate.properties`:

- `**[grouper.is](http://grouper.is).mcp**` – Enables the MCP protocol.
  
  - **On a WS environment** (`[grouper.is.ws](http://grouper.is.ws) = true`): loads the MCP protocol servlet.
  - **On a UI environment** (`[grouper.is](http://grouper.is).ui = true`): enables the MCP info page.
- `**[grouper.is](http://grouper.is).oauth**` – Enables OAuth 2.1 with PKCE authentication.
  
  - **On a WS environment** (`[grouper.is.ws](http://grouper.is.ws) = true`): loads the OAuth token and registration servlets.
  - **On a UI environment** (`[grouper.is](http://grouper.is).ui = true`): enables the OAuth consent page.

Both properties must be set to `true` on both the **WS** and **UI** containers for full MCP-with-OAuth functionality. If you only need MCP with WS authentication (HTTP Basic or container auth) and no OAuth, you can enable `[grouper.is](http://grouper.is).mcp` alone on the WS container.

To enable MCP with OAuth, set the following in your Grouper configuration or as environment variables:

`# In grouper.hibernate.properties (set on both UI and WS containers) grouper.is.mcp = true grouper.is.oauth = true`Or via environment variables (which the EL config expressions read automatically):

`GROUPER_MCP=true GROUPER_OAUTH=true`The MCP protocol servlet requires `[grouper.is.ws](http://grouper.is.ws) = true` because it is registered inside the WS servlet initialization block. The OAuth servlets also require `[grouper.is.ws](http://grouper.is.ws) = true`. Both the WS module and the UI module must be running for the OAuth flow to work (WS handles token exchange; UI handles user authentication and consent). If using normal WS authentication (HTTP Basic or container auth) instead of OAuth, only the WS module with `[grouper.is](http://grouper.is).mcp = true` is required.

## Configuration

### grouper.hibernate.properties

| Property | Default | Description |
| --- | --- | --- |
| `[grouper.is](http://grouper.is).mcp` | false (via `GROUPER_MCP` env var) | Enable MCP. On a WS environment this loads the MCP protocol servlet. On a UI environment this enables the MCP info page. |
| `[grouper.is](http://grouper.is).oauth` | false (via `GROUPER_OAUTH` env var) | Enable OAuth 2.1 with PKCE authentication. On a WS environment this loads the OAuth token and registration servlets. On a UI environment this enables the OAuth consent page. |

### MCP settings (grouper.properties)

| Property | Default | Description |
| --- | --- | --- |
| `grouper.mcp.auth.oauth` | true | Allow OAuth JWT Bearer authentication for MCP. |
| `grouper.mcp.auth.httpBasic` | false | Allow HTTP Basic authentication for MCP. Requires `[grouper.is.ws](http://grouper.is.ws).basicAuthn = true` in `grouper.hibernate.properties`. |
| `grouper.mcp.auth.container` | false | Allow container-managed authentication for MCP (e.g. Tomcat valve, Apache module). |
| `grouper.mcp.auth.customAuthClass` | false | Allow custom authentication class for MCP (`ws.security.non-rampart.authentication.class`). |
| `[grouper.ws](http://grouper.ws).url` | (blank) | Base URL of the WS (used for MCP and other WS operations), e.g. `[https://grouper.example.edu/grouper-ws](https://grouper.example.edu/grouper-ws)`. Displayed on the MCP info page in the UI. Configured at the top of grouper.base.properties below grouper.ui.url. The MCP endpoint URL will be this value with `/mcp` appended. |
| `grouper.mcp.logClientErrors` | false | Log MCP client errors at WARN level. Enable when debugging MCP connection issues. |
| `grouper.mcp.sqlGrouperExternalSystem` | grouper | The database connection name used when the AI queries the Grouper database (i.e. when `externalSystemId` is `"grouper"` or not specified). Defaults to `grouper` (the main Grouper database connection). Administrators can set this to a different external system that points to a read-only database user or a read replica for additional security. The external system must be configured under `grouperClient.jdbc.{name}.*` in `grouper.client.properties`. |
| `grouper.mcp.tools.allow` | (empty) | Comma-separated list of MCP tool names to allow. If blank (the default), all tools are allowed (subject to the user's group membership and consent scopes). If set, only the listed tools are available. Deny list takes precedence over allow list (effective tools = allow minus deny). Example: `group_find, group_get_members, group_has_member, group_save` |
| `grouper.mcp.tools.deny` | (empty) | Comma-separated list of MCP tool names to deny. If blank (the default), no tools are denied. Deny list takes precedence over allow list (effective tools = allow minus deny). Tools on the deny list will not appear in `tools/list` and will return an access-denied error if called directly. Example: `sql_select, sql_get_schema, admin_daemon_job_run` |
| `grouper.mcp.instructions` | Grouper is an enterprise access management system for managing groups, folders, memberships, privileges, and attributes. Use the doc_search tool to find institutional documentation before attempting operations you are unsure about. | Instructions sent to the AI client in the MCP `initialize` response. Customize this to give the AI client guidance specific to your institution — for example, highlighting that documentation should be consulted early and often, describing your folder naming conventions, or noting institutional policies. This text appears as the `instructions` field in the initialize response and is typically displayed as a system prompt by the AI client. Newlines can be embedded with `\n`. |

### OAuth settings (grouper.properties)

These properties control the OAuth 2.1 token lifecycle used by MCP clients that authenticate via the OAuth flow.

| Property | Default | Description |
| --- | --- | --- |
| `grouper.oauth.accessToken.expirationSeconds` | 14400 (4 hours) | JWT access token lifetime in seconds |
| `grouper.oauth.authorizationCode.expirationSeconds` | 600 (10 minutes) | Authorization code lifetime in seconds |
| `grouper.oauth.logAuthDebug` | false | Log OAuth authentication debug info at WARN level. When enabled, logs the authorization header type and remote address on every MCP request, and the public key hash when the OAuth signing key is loaded from config. Useful for diagnosing OAuth/MCP connection and authentication issues in multi-container deployments. Disable (default) in normal operation to reduce log volume. |
| `grouper.mcp.oauth.requireReadwriteDataScope` | true | Whether to require users to specify data scope restrictions (folders, groups, and/or subjects) when granting read-write access on the OAuth consent page. When true (default), at least one folder, group, or subject must be entered. When false, the data scope fields are optional and users can grant unrestricted read-write access. |

### OAuth registration settings (grouper.properties)

These properties control how MCP clients register as OAuth clients via dynamic client registration (RFC 7591). Registration is anonymous (no authentication required) and is protected by the redirect URI allowlist, IP-based rate limiting, and the fact that registration alone grants zero access — the user must still complete the OAuth consent flow and be in the proper MCP authorization groups.

| Property | Default | Description |
| --- | --- | --- |
| `grouper.oauth.registration.enabled` | true | If false, dynamic client registration is disabled entirely. The `/mcp/oauth/register` endpoint returns 403. |
| `grouper.oauth.registration.rateLimitPerIpPerHour` | 20 | Maximum number of registration requests allowed per IP address per hour. Set to 0 to disable rate limiting. |
| `grouper.oauth.redrectUri.<configId>.regex` | (none) | Regex pattern that registered redirect URIs must match. Multiple patterns can be configured with different `configId` values. A redirect URI is allowed if it matches **any** configured pattern. If no patterns are configured, all redirect URIs are rejected (secure-by-default). Validated at both client registration time and at the authorization/consent endpoint. |

**Redirect URI patterns are required.** If no `grouper.oauth.redrectUri.*.regex` patterns are configured, all OAuth client registrations will be rejected. For development, start with a localhost pattern:

`# Allow localhost redirect URIs for development grouper.oauth.redrectUri.localhost.regex = ^http://localhost(:\\d+)?/.*$ # Allow a production application grouper.oauth.redrectUri.myApp.regex = ^https://myapp\\.example\\.edu/.*$`

### MCP authorization groups (grouper.properties)

See the Authorization groups section below for the full list of MCP authorization group properties and their defaults.

### Auto-managed configuration (database-stored)

The following properties are automatically generated and stored in the database-backed `grouper.properties` configuration. They should not normally be set manually.

| Property | Description |
| --- | --- |
| `grouper.oauth.signingKey.privateKey` | RSA private key (Base64-encoded, auto-encrypted by Morph framework) |
| `grouper.oauth.signingKey.publicKey` | RSA public key (Base64-encoded) |

## Authorization groups

MCP access is controlled by Grouper groups that are autocreated at startup. Membership in these groups determines what each user can do via MCP. Note that wheel group membership does **not** automatically grant MCP access — users must be explicitly added to the appropriate MCP groups.

| Property | Default Group | Purpose |
| --- | --- | --- |
| `grouper.mcp.users.wsAuthnAllowed` | `etc:mcp:mcpUsersWsAuthnAllowed` | Members can use MCP via WS authentication (HTTP Basic, container auth). Not required for OAuth users. |
| `grouper.mcp.users.readonly` | `etc:mcp:mcpUsersReadonly` | Members can use MCP tools with read-only access |
| `grouper.mcp.users.readwrite` | `etc:mcp:mcpUsersReadwrite` | Members can use MCP tools with read-write access |
| `grouper.mcp.users.canRunSqlReadonly` | `etc:mcp:mcpUsersCanRunSqlReadonly` | Members can run read-only SQL queries via MCP |
| `grouper.mcp.users.adminReadonly` | `etc:mcp:mcpUsersAdminReadonly` | Members can run admin commands readonly via MCP. Must also be a Grouper sysadmin or readonly sysadmin. |
| `grouper.mcp.users.adminReadWrite` | `etc:mcp:mcpUsersAdminReadWrite` | Members can run admin commands readwrite via MCP. Must also be a Grouper sysadmin. |
| `grouper.mcp.users.canRegisterConfidentialOAuthClient` | `etc:mcp:mcpUsersCanRegisterConfidentialOAuthClient` | Members can register confidential OAuth clients (with client secret) from the MCP info page in the UI. Useful for server-side applications like LibreChat that can securely store a client secret. Grouper sysadmins can always register confidential clients regardless of this group. |

Note: These groups are autocreated by Grouper at startup (when `configuration.autocreate.system.groups = true`, which is the default). Administrators populate these groups to grant MCP access to individual users or other groups.

**OAuth provides additional consent-based restriction.** When users authenticate via OAuth, the consent page only shows scope checkboxes for groups the user is actually a member of (read-write membership also shows the read-only scope, and admin read-write also shows admin read-only), and the user can choose to grant fewer scopes than their group memberships would allow. These consent choices are enforced on every MCP request in addition to group membership. For example, a user in the readwrite group can choose to grant only readonly access to a particular MCP client. With WS authentication (HTTP Basic, container auth) there is no consent flow, so authorization is based solely on group membership. This makes OAuth the preferred authentication method for interactive AI use.

**Read-write scope restrictions.** When a user selects the read-write scope on the OAuth consent page, they can specify scope restrictions: folder paths (limiting which folders the client can modify groups in, maximum 10), group paths (limiting which specific groups can be modified, maximum 10), or subject IDs/identifiers (limiting which subjects can be managed, maximum 50). Folders are validated for existence and the total number of groups across all specified folders must be less than 500. These restrictions are encoded in the JWT access token and enforced on every read-write tool call. Categories left blank are unrestricted — for example, if a user only enters folder paths but leaves subjects blank, the client can modify groups in those folders for any subject. Conversely, if a user only enters subject IDs, those subjects can be operated on with any group. At least one category must have a value; if all categories are left blank, no read-write operations are permitted. By default (`grouper.mcp.oauth.requireReadwriteDataScope = true`), at least one folder, group, or subject must be specified. Set to `false` to make data scope restrictions optional, allowing users to grant unrestricted read-write access.

**Scope enforcement for assignment-on-assignment.** When a read-write tool call uses an `_asgn` attribute assign type (e.g. `group_asgn`, `stem_asgn`), the MCP server resolves the owner attribute assignment to determine the underlying group, stem, or subject. Scope restrictions are then checked against that underlying owner, not just the assignment ID. This ensures that, for example, an OAuth client scoped to folder `school:departments` cannot assign metadata on a marker attribute that belongs to a group in `school:clubs`.

**Trailing colon tolerance.** Folder scope values entered with a trailing colon (e.g. `school:departments:`) are handled correctly — the trailing colon is stripped before matching. This prevents user confusion when entering folder paths.

### Summary of tool access for authorization groups

| Tool name | mcpUsersAdminReadWrite | mcpUsersAdminReadonly | mcpUsersCanRunSqlReadonly | mcpUsersReadonly | mcpUsersReadwrite |
| --- | --- | --- | --- | --- | --- |
| admin_config_search |  |  |  |  |  |
| admin_daemon_job_message |  |  |  |  |  |
| admin_daemon_job_run |  |  |  |  |  |
| admin_daemon_logs |  |  |  |  |  |
| admin_daemon_names |  |  |  |  |  |
| admin_external_system_get |  |  |  |  |  |
| ldap |  |  |  |  |  |
| sql_get_schema |  |  |  |  |  |
| sql_select |  |  |  |  |  |
| attribute_assignment_get |  |  |  |  |  |
| attribute_assignment_save |  |  |  |  |  |
| attribute_def_name_find |  |  |  |  |  |
| audit_get |  |  |  |  |  |
| doc_search |  |  |  |  |  |
| entity_get |  |  |  |  |  |
| entity_get_groups |  |  |  |  |  |
| folder_delete |  |  |  |  |  |
| folder_find |  |  |  |  |  |
| group_add_member |  |  |  |  |  |
| group_delete |  |  |  |  |  |
| group_find |  |  |  |  |  |
| group_get_members |  |  |  |  |  |
| group_has_member |  |  |  |  |  |
| group_remove_member |  |  |  |  |  |
| group_save |  |  |  |  |  |
| institutional_tools |  |  |  |  |  |
| memberships_get |  |  |  |  |  |
| privilege_assign |  |  |  |  |  |
| privilege_get |  |  |  |  |  |

An `X` in both a readonly and readwrite column reflects that readwrite group members inherit readonly access (and admin readwrite members inherit admin readonly access). `folder_delete`, `group_delete`, and `group_save` additionally require the OAuth client to hold group or folder readwrite scope. `institutional_tools` is available to readonly members for read-only GSH templates; running write-capable templates requires readwrite.

## SQL readonly tools

The MCP server includes SQL readonly tools that allow authorized users to explore the Grouper database schema (and other configured databases) and run read-only SQL queries. These tools are gated by the `grouper.mcp.users.canRunSqlReadonly` group (and the `grouper_sql_readonly` OAuth consent scope for OAuth-authenticated users).

| Tool | Description |
| --- | --- |
| `sql_get_schema` | Database schema discovery with three actions: `listExternalSystems` returns the available database connections (always includes `"grouper"`); `listTables` returns table and view names for a given external system; `tableInfo` returns DDL or column metadata for a specific table. See [external database connections](#sql-external-systems) below. |
| `sql_select` | Executes a read-only SQL SELECT query and returns the results as JSON. Results are paged with `pageSize` (default 500, max 5000) and `pageNumber` (1-based, default 1). Set `countOnly` to `true` to return just the row count without fetching data. Use `externalSystemId` to query a different database connection. |
| `sql_select_count` | (Backward compatibility) Equivalent to `sql_select` with `countOnly = true`. Accepts the same `sql` and `externalSystemId` parameters. |

### SQL security

All SQL tools enforce strict security measures:

- Only `SELECT` statements are allowed. DML (`INSERT`, `UPDATE`, `DELETE`, `MERGE`) and DDL (`CREATE`, `ALTER`, `DROP`, `TRUNCATE`) keywords are rejected.
- Semicolons are prohibited (no multi-statement execution).
- Results are paged (max 5000 rows per page) and truncated at 1,000,000 characters of JSON output.
- `CALL` and `EXEC` statements are also blocked.
- The JDBC connection is set to read-only mode (`connection.setReadOnly(true)`) as defense-in-depth, so the database driver will reject any write operations.
- The Grouper database connection can be configured via `grouper.mcp.sqlGrouperExternalSystem` to point to a read-only database user or a read replica.

### External database connections

In addition to the Grouper database (always available as `"grouper"`), administrators can configure additional database connections that the AI can query through the MCP SQL tools. This is useful for querying source systems, data warehouses, or other institutional databases alongside the Grouper database.

Each external database must have a JDBC connection configured under `grouperClient.jdbc.{id}.*` in `grouper.client.properties`. To make it available to the MCP SQL tools, the administrator configures which tables and views are discoverable by the AI for that connection.

#### Per-external-system configuration

| Property | Description |
| --- | --- |
| `grouper.mcp.sql.{id}.sqlTablesViews` | Comma-separated list of table or view names (optionally schema-qualified) that should be discoverable via `sql_get_schema` for this external system. Column metadata and comments (on PostgreSQL and Oracle) are retrieved from the database catalog and cached for 1 hour.   Example: `grouper.mcp.sql.hr_db.sqlTablesViews = hr.employees, hr.departments` |
| `grouper.mcp.sql.{id}.sqlTablesViewsQuery` | A SQL query that returns table/view names as a single column. Executed against the external system's database. Results are cached for 1 hour. Can be used instead of (or in addition to) `sqlTablesViews`.   Example: `grouper.mcp.sql.hr_db.sqlTablesViewsQuery = select table_schema \|\| '.' \|\| table_name from information_schema.tables where table_schema = 'hr'` |
| `grouper.mcp.sql.{id}.documentationForAiClient` | Optional documentation string for this external system that is returned to the AI client when it calls `sql_get_schema` with `action = 'listExternalSystems'`. Use this to help the AI understand what data is in this database.   Example: `grouper.mcp.sql.hr_db.documentationForAiClient = HR database containing employee and department records` |

**Grouper database extras.** For the Grouper database itself, the base table and view list comes from the built-in DDL schema file. You can add additional tables/views (e.g., custom views created by the institution) using `grouper.mcp.sql.grouper.sqlTablesViews` or `grouper.mcp.sql.grouper.sqlTablesViewsQuery`. These are additive to the built-in schema. For non-Grouper external systems, the table list comes entirely from the per-system config.

**External system validation.** The Grouper database (`"grouper"`) is always available. Other external systems must have at least `grouper.mcp.sql.{id}.sqlTablesViews` or `grouper.mcp.sql.{id}.sqlTablesViewsQuery` configured, otherwise the AI will receive an error when attempting to query them. This ensures administrators explicitly opt in each database.

#### Example configuration

`# The Grouper database uses a read-only replica grouper.mcp.sqlGrouperExternalSystem = grouperReadOnly # Add custom views to the Grouper schema discovery grouper.mcp.sql.grouper.sqlTablesViews = my_custom_view, other_schema.my_table # Configure an external HR database grouper.mcp.sql.hr_db.sqlTablesViews = hr.employees, hr.departments grouper.mcp.sql.hr_db.documentationForAiClient = HR database containing employee and department records # Configure an external data warehouse with dynamic table discovery grouper.mcp.sql.warehouse.sqlTablesViewsQuery = select table_schema || '.' || table_name from information_schema.tables where table_schema = 'analytics' grouper.mcp.sql.warehouse.documentationForAiClient = Analytics data warehouse with enrollment and course data`

## Document search tools (RAG)

The MCP server includes a `doc_search` tool that provides full-text search over institutional documentation using Apache Lucene. This enables AI assistants to search through wiki pages, knowledge base articles, and other documentation that is not directly accessible on the filesystem.

The tool is gated by the readonly authorization group (`grouper.mcp.users.readonly`) and the `readonly` OAuth consent scope. It is only advertised to AI clients when at least one document search source is configured.

### How it works

1. Institutions manage their own content tables in any database accessible to Grouper (including the Grouper database itself). Content can be populated by a web spider, import script, or any other process.
2. Grouper configuration properties define SQL queries to read from these tables. The queries must alias columns to standard names (`grouper_content`, `grouper_url`, `grouper_name`).
3. On each WS node, Grouper builds an in-memory Lucene index from the query results. Content is automatically chunked into ~800 token pieces with overlap for optimal search results.
4. The index is rebuilt periodically (configurable interval, default 1 hour).

### Configuration

Each document search source has a unique config ID. Multiple sources can be configured.

| Property | Required | Description |
| --- | --- | --- |
| `grouper.mcp.docSearch.<configId>.externalSystemId` | No (default: grouper) | Database external system to query. Must match a `grouperClient.jdbc.<id>.*` connection. |
| `grouper.mcp.docSearch.<configId>.query` | Yes | SQL query to retrieve documents. Must alias columns as `grouper_content` (required), `grouper_name` (required), and `grouper_url` (optional). Documents without a `grouper_name` value are skipped during indexing. |
| `grouper.mcp.docSearch.<configId>.documentationForAiClient` | Yes | Description included in the tool definition so the AI client knows what content is available and when to search this source. |
| `grouper.mcp.docSearch.<configId>.reindexIntervalSeconds` | No (default: 3600) | How often to rebuild the Lucene index from the database, in seconds. |

#### Example configuration

`# Create a content table (institution-managed, any schema) # CREATE TABLE my_wiki_content ( # url VARCHAR(2000) NOT NULL, # title VARCHAR(500), # content TEXT NOT NULL # ); # Configure the doc search source grouper.mcp.docSearch.campusWiki.externalSystemId = grouper grouper.mcp.docSearch.campusWiki.query = select content as grouper_content, url as grouper_url, title as grouper_name from my_wiki_content grouper.mcp.docSearch.campusWiki.documentationForAiClient = Campus wiki documentation about Grouper policies, access management procedures, and group naming conventions grouper.mcp.docSearch.campusWiki.reindexIntervalSeconds = 1800`

### Built-in data dictionary source

In addition to SQL-based sources, the MCP server automatically indexes Grouper's data field and data row dictionary as a built-in source called `grouperDataDictionary`. This indexes the `descriptionHtml` and `zeroToManyExamplesHtml` from each data field and data row configuration (converted from HTML to markdown before indexing).

**Privacy realm filtering:** Each data field and data row is associated with a privacy realm. At search time, results are filtered so that only entries the user can access (view, read, or update in the privacy realm, or entries in a public realm) are returned. The tool is not advertised to users who have no access to any privacy realm and no other doc search sources configured.

To disable the data dictionary source:

`grouper.mcp.docSearch.dataDictionary.enable = false`

### Memory usage

The Lucene index is stored in-memory (no filesystem required, works in read-only containers). For typical institutional documentation (a few MB of markdown), the index uses less than 50 MB of heap. The index rebuilds from the database in seconds.

## LDAP tools

The MCP server includes an LDAP search tool that allows the AI to query LDAP directories configured as Grouper external systems. The `ldap` tool is gated by the admin readonly authorization group (`grouper.mcp.users.adminReadonly`) and the `admin_readonly` OAuth consent scope, the same authorization as other admin readonly tools.

### Enabling LDAP external systems for MCP

To make an LDAP external system available for MCP queries, the administrator must add at least one `grouper.mcp.ldap.<id>.*` configuration property. The external system ID must match an `ldap.<id>.*` connection configured in `grouper-loader.properties`.

| Property | Description |
| --- | --- |
| `grouper.mcp.ldap.<id>.baseDn` | Default base DN for LDAP searches when the AI does not specify one. Strongly recommended. Example: `dc=example,dc=edu` |
| `grouper.mcp.ldap.<id>.documentationForAiClient` | Optional documentation string sent to the AI when it lists LDAP external systems. Helps the AI understand what data is in each LDAP directory (e.g., “Campus directory with people, groups, and service accounts”). |

#### Example configuration

`# Make the "personLdap" LDAP connection available for MCP grouper.mcp.ldap.personLdap.baseDn = ou=people,dc=example,dc=edu grouper.mcp.ldap.personLdap.documentationForAiClient = Campus people directory with uid, cn, mail, eduPersonAffiliation attributes`

### LDAP tool limits

The `ldap` tool enforces the following limits to prevent excessive data retrieval:

- **Max 2,500 entries** per search (enforced via the LDAP size limit).
- **Max 10,000 total attribute values** across all entries. If this limit is reached while iterating through results, the response is truncated and a `truncated: true` flag is set.
- **Max 1,000,000 characters** in the response text.

The LDAP tool is rate-limited under the `admin_readonly` throttle category (default: 60 calls per minute per user).

## Admin tools

The MCP server includes administrative tools for Grouper system administrators. Admin readonly tools are gated by the `grouper.mcp.users.adminReadonly` group (and the `admin_readonly` OAuth consent scope for OAuth-authenticated users). Admin readwrite tools are gated by the `grouper.mcp.users.adminReadWrite` group (and the `admin_readwrite` OAuth consent scope). The user must also be a Grouper sysadmin (or readonly sysadmin for readonly tools).

### Admin readonly tools

| Tool | Description |
| --- | --- |
| `admin_daemon_job_message` | Returns the job message for a specific daemon job log entry by its `id`. Returns the first 20,000 characters of the message (from `job_message` or `job_message_clob`). Also includes the job name, status, and start time for context. |
| `admin_daemon_logs` | Returns information from `grouper_loader_log` for daemon job runs. Returns the most recent 100 rows ordered by start time descending. The job message is not returned (use `admin_daemon_job_message` for that). Filters: `jobName` (exact match), `status` (case-insensitive), `startedAfter`, `startedBefore` (format: yyyy-MM-dd or yyyy/MM/dd HH:mm:ss). At least one of `jobName` or `status` is required. |
| `admin_config_search` | Searches Grouper configuration properties. Supports two search modes: `lucene` (default) for full-text search on config keys and values using a Lucene in-memory index, and `regex` for Java regex matching against config key names only. Sensitive values (passwords, secrets, private keys) are automatically masked as `*******`. Optionally filter by a specific config file (e.g., `grouper.properties`, `grouper-loader.properties`). Lucene results include metadata: where configured (base/override/database), default value, EL expression, comment, value type, and required flag. Returns up to 500 results. |
| `admin_daemon_names` | Searches for distinct daemon job names in the `grouper_loader_log` table. Takes a search string that is split by whitespace into terms. Each term is matched against `job_name` using case-insensitive LIKE. Users can include `%` wildcards in each term; terms without wildcards are automatically wrapped with `%`. Returns up to 200 matching job names. |
| `admin_external_system_get` | Look up users in external systems (Azure, Duo, SCIM, Box, Google, Remedy, Remedy Digital Marketplace, TeamDynamix, FreshService Requesters) configured in Grouper. Two actions: `listExternalSystems` discovers which external systems are configured for user lookups; `getUser` translates a Grouper subject to the external system user identifier via a JEXL expression and retrieves the user record. See [external system user lookup](#external-system-user-lookup) below for configuration details. |

### Admin readwrite tools

| Tool | Description |
| --- | --- |
| `admin_daemon_job_run` | Triggers a daemon job to run on the Grouper daemon server. Takes a `jobName` parameter (the exact job name as it appears in `grouper_loader_log`). The job is triggered asynchronously via the Quartz scheduler on the daemon server. Returns a confirmation that the job was triggered. Use `admin_daemon_names` first to find valid job names, and `admin_daemon_logs` to check the status after triggering. |

## External system user lookup

The MCP server includes an admin tool for looking up users in external systems configured in Grouper. This is useful for troubleshooting provisioning issues by verifying what a user looks like in an external system (Azure, Duo, SCIM, Box, Google, Remedy, Remedy Digital Marketplace, TeamDynamix, or FreshService Requesters) directly through an AI agent.

The `admin_external_system_get` tool is an admin readonly tool gated by the `grouper.mcp.users.adminReadonly` group (and the `admin_readonly` OAuth consent scope for OAuth-authenticated users).

The tool works by:

1. Resolving a Grouper subject from the provided subject ID or identifier.
2. Translating the subject to the external system's user identifier using a configurable JEXL expression.
3. Querying the external system API (Azure Graph, Duo Admin, SCIM 2.0, Box, Google Directory, Remedy, Digital Marketplace, TeamDynamix, or FreshService) to retrieve the user record.
4. Returning the external system user data as JSON.

### Configuration

For each external system that should be available for user lookups, the administrator must configure the following properties in `grouper.properties` (or the database configuration):

| Property | Description |
| --- | --- |
| `grouper.mcp.adminExternalSystem.<configId>.subjectIdTranslationJexl` | A JEXL expression that translates a Grouper `Subject` object to the external system's user identifier string. The expression receives a `subject` variable (of type `edu.internet2.middleware.subject.Subject`) and should return the string value to use for the lookup.   Example: `${subject.getAttributeValue('eppn')}` |
| `grouper.mcp.adminExternalSystem.<configId>.externalSystemLookupField` | The field name in the external system to search by. The available fields depend on the external system type (see table below). |
| `grouper.mcp.adminExternalSystem.<configId>.externalSystemType` | (Optional) Override the auto-detected external system type. Required when the connector class is ambiguous — for example, both SCIM and FreshService Requesters use `WsBearerTokenExternalSystem`. Valid values: `azure`, `duo`, `scim`, `box`, `google`, `remedy`, `remedyDigitalMarketplace`, `teamDynamix`, `freshserviceRequesters`. |
| `grouper.mcp.adminExternalSystem.<configId>.documentationForAiClient` | (Optional) A documentation string that is sent to the AI client when it lists external systems. Helps the AI understand what data is in each external system and when to use it.   Example: `Azure AD for campus users provisioned via Grouper` |

The `<configId>` must match the config ID of a configured external system connector (e.g., the `myAzure` in `grouper.azureConnector.myAzure.*`).

#### Available lookup fields by external system type

| Type | Lookup fields | Notes |
| --- | --- | --- |
| Azure | `userPrincipalName`, `id` | Uses Microsoft Graph API. The lookup field maps to the Azure user attribute used in the `$filter` query parameter or batch request. |
| Duo | `username`, `id` | Uses Duo Admin API. `username` uses the `/admin/v1/users?username=` endpoint; `id` uses `/admin/v1/users/{id}`. |
| SCIM | `userName`, `id`, `email`, `externalId` | Uses SCIM 2.0 API. `id` uses direct resource path `/Users/{id}`; other fields use SCIM filter syntax `field eq "value"`. |
| Box | `login`, `id`, `name` | Uses Box API. `id` uses direct resource path `/users/{id}`; `login` and `name` use the `filter_term` parameter with exact matching. |
| Google | `primaryEmail`, `id` | Uses Google Directory API. Both `id` and `primaryEmail` are accepted as the user key in the `/users/{userKey}` endpoint. |
| FreshService Requesters | `email`, `id`, `externalId` | Uses FreshService Requesters API. `id` uses `/api/v2/requesters/{id}`; `email` uses the `email=` parameter; `externalId` uses the query syntax. Requires `externalSystemType = freshserviceRequesters` in config. |
| Remedy | `remedyLoginId` | Uses BMC Remedy ITSM API. Looks up users by their Remedy login ID. |
| Remedy Digital Marketplace | `loginName` | Uses BMC Remedy Digital Marketplace API. Looks up users by their login name. |
| TeamDynamix | `externalId`, `username`, `id` | Uses TeamDynamix API. `id` uses direct resource path `/people/{id}`; `externalId` and `username` use the search endpoint with field name matching. |

**External system type detection.** The tool automatically detects the external system type by matching the `configId` against configured connectors. Azure connectors (`grouper.azureConnector.*`), Duo connectors (`grouper.duoConnector.*`), Box connectors (`grouperClient.boxConnector.*`), Google connectors (`grouper.googleConnector.*`), Remedy connectors (`grouper.remedyConnector.*`), Remedy Digital Marketplace connectors (`grouper.remedyDigitalMarketplaceConnector.*`), TeamDynamix connectors (`grouper.teamDynamix.*`), and WsBearerToken connectors (`grouper.wsBearerToken.*`, used for SCIM) are supported. For WsBearerToken-based systems where auto-detection is ambiguous (SCIM vs. FreshService Requesters), set `externalSystemType` explicitly.

### Tool reference

#### Action: listExternalSystems

Returns all external systems configured for MCP user lookups.

| Parameter | Required | Description |
| --- | --- | --- |
| `action` | Yes | `"listExternalSystems"` |

**Example response:**

`{ "externalSystemCount" : 2, "externalSystems" : [ { "configId" : "myAzure", "type" : "azure", "lookupField" : "userPrincipalName", "subjectIdTranslationJexl" : "${subject.getAttributeValue('eppn')}", "documentation" : "Azure AD for campus users provisioned via Grouper" }, { "configId" : "myDuo", "type" : "duo", "lookupField" : "username", "subjectIdTranslationJexl" : "${subject.getId()}" } ] }`

#### Action: getUser

Looks up a user in an external system by translating a Grouper subject.

| Parameter | Required | Description |
| --- | --- | --- |
| `action` | Yes | `"getUser"` |
| `externalSystemConfigId` | Yes | The external system config ID (from `listExternalSystems`). |
| `subjectIdOrIdentifier` | Yes | A Grouper subject ID or identifier to look up. |
| `subjectSourceId` | No | Optional subject source ID to disambiguate subjects. |

**Example response (user found):**

`{ "externalSystemConfigId" : "myAzure", "externalSystemType" : "azure", "lookupField" : "userPrincipalName", "translatedLookupValue" : "jsmith@example.com", "grouperSubjectId" : "jsmith", "grouperSubjectSourceId" : "jdbc", "userFound" : true, "user" : { "accountEnabled" : true, "displayName" : "John Smith", "id" : "abc-123-def", "mailNickname" : "jsmith", "onPremisesImmutableId" : null, "userPrincipalName" : "jsmith@example.com" } }`**Example response (user not found):**

`{ "externalSystemConfigId" : "myAzure", "externalSystemType" : "azure", "lookupField" : "userPrincipalName", "translatedLookupValue" : "jsmith@example.com", "grouperSubjectId" : "jsmith", "grouperSubjectSourceId" : "jdbc", "userFound" : false }`

### Example configurations

#### Azure (Microsoft Entra ID)

Look up users by their UPN, translated from the subject's `eppn` attribute:

`grouper.mcp.adminExternalSystem.myAzure.subjectIdTranslationJexl = ${subject.getAttributeValue('eppn')} grouper.mcp.adminExternalSystem.myAzure.externalSystemLookupField = userPrincipalName grouper.mcp.adminExternalSystem.myAzure.documentationForAiClient = Azure AD for campus users provisioned via Grouper`

#### Duo

Look up users by their Duo username, translated from the Grouper subject ID:

`grouper.mcp.adminExternalSystem.myDuo.subjectIdTranslationJexl = ${subject.getId()} grouper.mcp.adminExternalSystem.myDuo.externalSystemLookupField = username grouper.mcp.adminExternalSystem.myDuo.documentationForAiClient = Duo MFA system for two-factor authentication`

#### SCIM 2.0

Look up users by their SCIM userName, translated from the subject's email attribute:

`grouper.mcp.adminExternalSystem.myScim.subjectIdTranslationJexl = ${subject.getAttributeValue('email')} grouper.mcp.adminExternalSystem.myScim.externalSystemLookupField = userName grouper.mcp.adminExternalSystem.myScim.documentationForAiClient = SCIM endpoint for provisioning to cloud HR system`

#### Box

`grouper.mcp.adminExternalSystem.myBox.subjectIdTranslationJexl = ${subject.getAttributeValue('email')} grouper.mcp.adminExternalSystem.myBox.externalSystemLookupField = login grouper.mcp.adminExternalSystem.myBox.documentationForAiClient = Box cloud content management for file sharing and collaboration`

#### Google (Workspace)

`grouper.mcp.adminExternalSystem.myGoogle.subjectIdTranslationJexl = ${subject.getAttributeValue('email')} grouper.mcp.adminExternalSystem.myGoogle.externalSystemLookupField = primaryEmail grouper.mcp.adminExternalSystem.myGoogle.documentationForAiClient = Google Workspace directory for campus users`

#### FreshService Requesters

Note: `externalSystemType` must be set explicitly because FreshService Requesters use the same `WsBearerTokenExternalSystem` connector class as SCIM:

`grouper.mcp.adminExternalSystem.myFresh.subjectIdTranslationJexl = ${subject.getAttributeValue('email')} grouper.mcp.adminExternalSystem.myFresh.externalSystemLookupField = email grouper.mcp.adminExternalSystem.myFresh.externalSystemType = freshserviceRequesters grouper.mcp.adminExternalSystem.myFresh.documentationForAiClient = FreshService ITSM for helpdesk requesters`

#### Remedy (BMC ITSM)

`grouper.mcp.adminExternalSystem.myRemedy.subjectIdTranslationJexl = ${subject.getId()} grouper.mcp.adminExternalSystem.myRemedy.externalSystemLookupField = remedyLoginId grouper.mcp.adminExternalSystem.myRemedy.documentationForAiClient = BMC Remedy ITSM for incident management`

#### Remedy Digital Marketplace

`grouper.mcp.adminExternalSystem.myDigitalMarketplace.subjectIdTranslationJexl = ${subject.getId()} grouper.mcp.adminExternalSystem.myDigitalMarketplace.externalSystemLookupField = loginName grouper.mcp.adminExternalSystem.myDigitalMarketplace.documentationForAiClient = BMC Digital Marketplace for service catalog`

#### TeamDynamix

`grouper.mcp.adminExternalSystem.teamdx.subjectIdTranslationJexl = ${subject.getId()} grouper.mcp.adminExternalSystem.teamdx.externalSystemLookupField = externalId grouper.mcp.adminExternalSystem.teamdx.documentationForAiClient = TeamDynamix IT service management and project portfolio`**Subject attributes.** The JEXL expression can use any method on the `Subject` interface, including `subject.getId()`, `subject.getName()`, `subject.getDescription()`, `subject.getAttributeValue('attributeName')`, and `subject.getSourceId()`. The available attributes depend on the subject source configuration.

## Institutional tools (GSH templates)

The `institutional_tools` MCP tool allows deployers to expose institution-specific GSH templates to AI agents via MCP. This enables institutions to make their custom automation scripts available as MCP tools without writing any MCP-specific code — they simply enable the `mcpEnabled` property on existing GSH templates.

### Requirements

A GSH template can be exposed via MCP when **all** of the following conditions are met:

- The template is **enabled** (`enabled = true`)
- The template has `mcpEnabled = true`

All four `securityRunType` values are supported:

- `wheel` — only wheel/root users can see and execute the template
- `specifiedGroup` — only members of the configured group can see and execute (wheel users can also run these)
- `privilegeOnObject` — always listed in schema (authorization is checked at execution time based on the user's privilege on the specific owner group or folder)
- `everyone` — all MCP users can see and execute the template

### Authorization levels

The `institutional_tools` tool requires membership in at least the MCP **readonly** group. Each template is additionally classified as either **readonly** or **readwrite**:

- Templates with `mcpReadonly = true` can be seen and executed by users who only have MCP readonly access.
- Templates with `mcpReadonly = false` (the default) require MCP readwrite access to see and execute.

Users with MCP readwrite access can see and execute both readonly and readwrite templates. The template's own security checks (`securityRunType`) still apply on top of the MCP access level.

For OAuth users, the appropriate consent scope (`readonly` or `readwrite`) must also be granted.

**Scope-based filtering:** When readwrite data-scope restrictions are active (e.g., via OAuth consent), non-readonly templates that require a group or folder owner are automatically hidden from users whose approved scopes only include subjects. For example, if a user’s readwrite scope is restricted to specific subjects only (no groups or folders), templates configured with `showOnGroups = true` or `showOnFolders = true` will not appear in the tool listing, `schema` response, or be executable. This ensures users only see templates they can meaningfully interact with given their scope.

### Configuration

To enable a GSH template for MCP, add the `mcpEnabled` property to the template's configuration (in the UI wizard, database, or properties file):

`# Enable MCP access for this template grouperGshTemplate.myTemplateConfigId.mcpEnabled = true`Optionally, mark the template as readonly for MCP (so users with only readonly access can execute it):

`# Allow readonly MCP users to execute this template grouperGshTemplate.myTemplateConfigId.mcpReadonly = true`

#### Input scope restrictions

For non-readonly templates, individual inputs can be configured to require authorization scope validation. This restricts the input values an AI agent can pass, limiting them to the user's approved readwrite scopes (as configured on the OAuth consent page).

`# Require that the targetFolder input is within the user's approved folder scopes grouperGshTemplate.myTemplateConfigId.input.0.mcpScopeType = folders`The `mcpScopeType` dropdown offers three options:

| Value | Description |
| --- | --- |
| `folders` | Validates each value against the user's approved readwrite folder scopes (exact match or under an approved folder) |
| `groups` | Validates each value against the user's approved readwrite group and folder scopes (exact group match or group under an approved folder) |
| `subjects` | Validates each value against the user's approved readwrite subject scopes (exact match) |

When a scope-restricted input contains multiple values (comma-separated), each value is split, trimmed, and validated individually. If any value is outside the user's approved scope, the execution is denied with an error message.  
The `mcpScopeType` option only appears in the UI wizard when `mcpEnabled` is `true` and `mcpReadonly` is `false`. Readonly templates do not need scope restrictions since they are not expected to modify data.

### How it works

1. When the AI agent lists available MCP tools, the `institutional_tools` tool is only included if there are MCP-enabled templates accessible to the authenticated user (respecting security run types and readonly/readwrite access). The tool’s description dynamically lists the names of available templates, giving the AI model immediate awareness of what institutional tools exist without a separate call.
2. The AI agent can call `institutional_tools` with `action = "schema"` to get full details about available templates. The response includes each template's config ID, name, description, security run type, whether it is readonly (`mcpReadonly`), input definitions (with types, required flags, validation rules, and `mcpScopeType` if applicable), and whether it operates on groups, folders, or both. Input names are returned without the `gsh_input_` prefix for cleaner AI interaction.
3. The AI agent calls `institutional_tools` with `action = "execute"`, providing the `configId`, optional owner context (`ownerType`, `ownerGroupName` or `ownerStemName`), and input values. Input keys can be provided with or without the `gsh_input_` prefix.
4. The tool enforces MCP readonly/readwrite access, scope restrictions on applicable inputs, and the template's own security checks.
5. The template executes using the deployer-configured `runAsType` with the MCP user's identity for security checks and auditing.

### Example configuration

`# Enable a "Create Working Group" template for MCP (readwrite) grouperGshTemplate.createWorkingGroup.enabled = true grouperGshTemplate.createWorkingGroup.templateType = gsh grouperGshTemplate.createWorkingGroup.templateName = Create Working Group grouperGshTemplate.createWorkingGroup.templateDescription = Creates a new working group with standard structure grouperGshTemplate.createWorkingGroup.securityRunType = specifiedGroup grouperGshTemplate.createWorkingGroup.groupUuidCanRun = app:mcp:mcpTemplateRunners grouperGshTemplate.createWorkingGroup.mcpEnabled = true grouperGshTemplate.createWorkingGroup.runAsType = GrouperSystem grouperGshTemplate.createWorkingGroup.showOnFolders = true grouperGshTemplate.createWorkingGroup.folderShowType = allFolders grouperGshTemplate.createWorkingGroup.numberOfInputs = 2 grouperGshTemplate.createWorkingGroup.input.0.name = gsh_input_parentFolder grouperGshTemplate.createWorkingGroup.input.0.label = Parent Folder grouperGshTemplate.createWorkingGroup.input.0.description = The folder to create the group in grouperGshTemplate.createWorkingGroup.input.0.type = string grouperGshTemplate.createWorkingGroup.input.0.required = true grouperGshTemplate.createWorkingGroup.input.0.mcpScopeType = folders grouperGshTemplate.createWorkingGroup.input.1.name = gsh_input_groupExtension grouperGshTemplate.createWorkingGroup.input.1.label = Group Extension grouperGshTemplate.createWorkingGroup.input.1.description = The short name for the new group grouperGshTemplate.createWorkingGroup.input.1.type = string grouperGshTemplate.createWorkingGroup.input.1.required = true grouperGshTemplate.createWorkingGroup.input.1.validationType = regex grouperGshTemplate.createWorkingGroup.input.1.validationRegex = ^[a-zA-Z0-9_]+$ grouperGshTemplate.createWorkingGroup.input.1.validationMessage = Only letters, numbers, and underscores grouperGshTemplate.createWorkingGroup.gshTemplate = ... # Enable a readonly "List Group Members" template for MCP grouperGshTemplate.listGroupMembers.enabled = true grouperGshTemplate.listGroupMembers.templateType = gsh grouperGshTemplate.listGroupMembers.templateName = List Group Members grouperGshTemplate.listGroupMembers.templateDescription = Lists members of a group grouperGshTemplate.listGroupMembers.securityRunType = everyone grouperGshTemplate.listGroupMembers.mcpEnabled = true grouperGshTemplate.listGroupMembers.mcpReadonly = true grouperGshTemplate.listGroupMembers.runAsType = GrouperSystem grouperGshTemplate.listGroupMembers.showOnGroups = true grouperGshTemplate.listGroupMembers.numberOfInputs = 0 grouperGshTemplate.listGroupMembers.gshTemplate = ...`The `institutional_tools` tool can be included in or excluded from the `grouper.mcp.tools.allow` and `grouper.mcp.tools.deny` lists just like any other MCP tool. To disable it entirely, add `institutional_tools` to the deny list.

## Protected resources

MCP write tools (`group_save`, `group_delete`, `folder_delete`, `group_add_member`, `group_remove_member`, `privilege_assign`, `attribute_assignment_save`) enforce server-side protection that blocks modifications to critical system groups and stems. This prevents AI agents or other MCP clients from inadvertently modifying security-sensitive resources.

### What is protected

- **Everything under the built-in objects stem** (configured via `grouper.rootStemForBuiltinObjects`, default `etc`). Any group or stem whose fully qualified name starts with `etc:` (or equals `etc`) is protected. This includes all auto-created system groups, attribute folders, workflow configuration, provisioning settings, etc.
- **Explicitly configured system groups** that may have been moved outside the `etc` stem by an administrator. The following config properties are checked, and their resolved values are protected regardless of location:
  
  - `groups.wheel.group` (sysadmin group)
  - `groups.wheel.viewonly.group` (sysadmin viewers)
  - `groups.wheel.readonly.group` (sysadmin readers)
  - `grouper.mcp.users.readonly`
  - `grouper.mcp.users.readwrite`
  - `grouper.mcp.users.wsAuthnAllowed`
  - `grouper.mcp.users.canRunSqlReadonly`
  - `grouper.mcp.users.adminReadonly`
  - `grouper.mcp.users.adminReadWrite`
  - `grouper.mcp.users.canRegisterConfidentialOAuthClient`
  - `[security.show.all.folders.if.in](http://security.show.all.folders.if.in).group`
  - `deprovisioning.admin.group`
  - `workflow.editorsGroup`
  - `[ws.client.user.group.name](http://ws.client.user.group.name)` (if configured)

### Blocked operations

- **group_save**: Cannot create or update any group under the `etc` stem, and cannot modify any explicitly protected group.
- **folder_delete**: Cannot delete any stem under the `etc` stem, and cannot delete any explicitly protected stem.
- **group_delete**: Cannot delete any group under the `etc` stem, and cannot delete any explicitly protected group.
- **group_add_member / group_remove_member**: Cannot add or remove members from any protected group.
- **privilege_assign**: Cannot assign or revoke privileges on any protected group or stem.
- **attribute_assignment_save**: Cannot assign attributes on any protected group or stem. For assignment-on-assignment operations (e.g. `group_asgn`), the server resolves the underlying owner of the marker attribute assignment and validates protected resources and OAuth scope against that owner. For example, assigning a configuration attribute on a marker that is assigned to a protected group will be denied.

### Stem rename protection

When a stem rename tool is available, stems with more than 5 child objects (groups + sub-stems, counted recursively) cannot be renamed via MCP. This prevents accidental renaming of large folder hierarchies.

The protected resource list is computed from configuration at first access and cached for the lifetime of the JVM. If you change the configuration properties that define system groups, a restart is required for the MCP protection to reflect the new values.

## Audit logging

Every MCP tool call is logged to the `grouper_mcp_tool_log` database table. This includes the tool name, request arguments, response (or error message), timing information, the authenticated user, and a soft link to the OAuth client registration (if applicable).

### Audit table columns

| Column | Description |
| --- | --- |
| `internal_id` | Auto-incrementing BIGINT primary key |
| `oauth_client_internal_id` | Soft link to `grouper_oauth_client.internal_id` (nullable, not a FK, so audits survive if the registration is deleted) |
| `subject_id` | Subject ID of the authenticated user |
| `subject_source_id` | Subject source ID of the authenticated user |
| `tool_name` | Name of the MCP tool (e.g. `group_find`, `group_add_member`) |
| `tool_category` | Category: `readonly`, `readwrite`, `sql`, `admin_readonly`, `admin_readwrite` |
| `request` | JSON arguments of the tool call (truncated to 4000 chars) |
| `response_or_error` | Result text or error message (truncated to 4000 chars) |
| `is_error` | `T` if the call resulted in an error, `F` otherwise |
| `started_millis` | Millis since 1970 when the call started |
| `duration_micros` | Duration of the call in microseconds |

### Cleanup

Old audit log entries are automatically deleted by the `MAINTENANCE__cleanLogs` daemon. Configure the retention period in `grouper-loader.properties`:

# days to retain MCP tool log rows. -1 = forever. default 365 loader.retain.db.mcp_tool_log.days = 365 

## Throttling

The MCP server enforces per-user, per-category rate limits to protect system performance. Rate limits are checked against the audit table before each tool call. If a user exceeds the limit, the call is rejected with an error message and the rejected call is itself logged.

### Tool categories and default limits

| Category | Tools | Default (calls/min) | Config property |
| --- | --- | --- | --- |
| `readonly` | group_find, folder_find, group_get_members, etc. | 200 | `grouper.mcp.throttle.readonly.defaultCallsPerMinute` |
| `readwrite` | folder_delete, group_add_member, group_delete, group_remove_member, group_save, etc. | 60 | `grouper.mcp.throttle.readwrite.defaultCallsPerMinute` |
| `sql` | sql_select, sql_get_schema | 30 | `grouper.mcp.throttle.sql.defaultCallsPerMinute` |
| `admin_readonly` | admin_daemon_names, admin_daemon_logs, etc. | 60 | `grouper.mcp.throttle.admin_readonly.defaultCallsPerMinute` |
| `admin_readwrite` | admin_daemon_job_run | 10 | `grouper.mcp.throttle.admin_readwrite.defaultCallsPerMinute` |

Set any limit to `-1` to disable throttling for that category.

### Per-user overrides

Operators can override the default limits for specific users by adding properties in `grouper.properties`:

# Override readonly limit for user jsmith to 500 calls/min grouper.mcp.throttle.override.jsmith.readonly.callsPerMinute = 500 # Override readwrite limit for user jsmith to 120 calls/min grouper.mcp.throttle.override.jsmith.readwrite.callsPerMinute = 120 # Disable throttling entirely for a service account grouper.mcp.throttle.override.svc_account.readonly.callsPerMinute = -1 Throttle counts are cached for 10 seconds per user+category to avoid querying the database on every request. In the worst case, a user could slightly exceed their limit during a cache window before the new count is loaded.

## Registration security

OAuth dynamic client registration is anonymous (no authentication required). This is safe because registration alone grants zero access — the user must still complete the full OAuth consent flow and be a member of the appropriate MCP authorization groups to actually use MCP.

Registration is protected by three mechanisms:

- **Redirect URI allowlist:** Every redirect URI submitted during registration must match at least one configured `grouper.oauth.redrectUri.*.regex` pattern. If no patterns are configured, all registrations are rejected (secure-by-default). This prevents arbitrary clients from registering with open redirect URIs.
- **IP-based rate limiting:** Each IP address is limited to `grouper.oauth.registration.rateLimitPerIpPerHour` registrations per hour (default 20). Excess requests receive HTTP 429 (Too Many Requests). This prevents database pollution from automated registration abuse.
- **Kill switch:** Registration can be disabled entirely by setting `grouper.oauth.registration.enabled = false`. When disabled, the registration endpoint returns HTTP 403.

**Why anonymous registration is safe:** MCP clients discover the `registration_endpoint` from well-known OAuth metadata and POST to it automatically. Many MCP clients do not support custom Authorization headers on registration requests. Since registration only creates a `client_id` and does not grant any access, authentication on registration is unnecessary complexity. The redirect URI allowlist ensures only approved applications can complete the OAuth flow, and the user must still authenticate and consent through the Grouper UI.

## Multi-container / load-balanced deployments

The MCP server is designed for load-balanced environments **without sticky sessions**. No per-request state is stored in memory — every request is independently authenticated via the JWT token, and all persistent state lives in the shared database.

- **Stateless MCP sessions:** The MCP protocol requires a `Mcp-Session-Id` header, and the server generates one on `initialize`. However, session IDs are **not tracked in memory**. The server only verifies that the header is present on non-initialize requests. Since the JWT already authenticates every request independently, in-memory session tracking is unnecessary and would break with load balancing.
- **Database-backed OAuth state:** All OAuth state (clients, authorization codes, pending requests, signing keys) is stored in the shared Grouper database, not in local memory. This means the authorization code can be created on one Grouper UI container and exchanged for a token on a different Grouper WS container.
- **Shared signing keys:** The JWT signing key pair is persisted in the database-backed configuration, so all containers sign and verify tokens with the same key. Keys are re-read from config every 5 minutes, so key changes propagate across containers without a restart.
- **Race condition handling:** If multiple containers attempt to generate the signing key simultaneously, the implementation handles this gracefully by falling back to loading the key stored by the winner.
- **Performance caches:** Group membership checks for MCP authorization use a local cache with a 60-second TTL. This means group membership changes take effect within 60 seconds across all containers. These caches self-populate from the database independently on each container.

### Diagnostics

To diagnose MCP authentication issues in a multi-container environment:

1. Enable `grouper.oauth.logAuthDebug = true` in the database config. This logs the auth header type, remote address, and public key hash on each container. Verify all containers show the same public key hash.
2. From GSH, call `GrouperOAuthSigningKey.verifyKeyPair()` to verify that the public and private keys in config form a valid pair (signs a test JWT and verifies it).
3. JWT verification failures are always logged at WARN level (`OAuth JWT verification failed: ...`) regardless of the `logAuthDebug` setting.

For details on the OAuth flow and how the database tables support multi-container deployments, see the [technical reference](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554339/Grouper+MCP+server+-+technical+reference).

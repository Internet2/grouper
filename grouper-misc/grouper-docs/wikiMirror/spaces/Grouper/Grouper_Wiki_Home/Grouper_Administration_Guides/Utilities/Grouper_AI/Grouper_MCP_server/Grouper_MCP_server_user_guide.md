---
title: "Grouper MCP server - user guide"
space: Grouper
pageId: 28554356
version: 14
lastUpdated: 2026-07-12T17:46:13.611Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554356/Grouper+MCP+server+-+user+guide
---

## Introduction

This guide is for end users who want to connect an AI tool (such as Claude, Cursor, or VS Code Copilot) to a Grouper instance via the built-in MCP server. It covers the UI info page where you can find your connection details, step-by-step client setup examples, and the full documentation for all available MCP tools.

For a high-level introduction to the Grouper MCP server, see the [overview](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547487/Grouper+MCP+server). For configuration and enabling instructions, see the [administrator guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide). For architecture and protocol details, see the [technical reference](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554339/Grouper+MCP+server+-+technical+reference).

All MCP tool calls are audited in a database table for security and compliance. The MCP server also enforces configurable per-category rate limits to protect system performance. See the [administrator guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide) for details on audit logging and throttling configuration.

## UI info page

All logged-in Grouper users can access the MCP info page from the **Miscellaneous** screen in the Grouper UI. Click **Model Context Protocol (MCP)** to view:

- **MCP server URL:** The URL to use when configuring your MCP client.
- **Your MCP access:** Shows which MCP operations you are authorized for (read-only, read-write, SQL read-only) and which authentication methods are available to you.
- **Tool call history:** Shows your recent MCP tool calls with timing and status.
- **Connected applications:** Shows AI applications you have authorized to access Grouper on your behalf, with the ability to remove them.
- **Register confidential OAuth client:** If you are a Grouper sysadmin or a member of the `mcpUsersCanRegisterConfidentialOAuthClient` group, you will see a form to register a confidential OAuth client (with client secret) for server-side applications like LibreChat. After registration, the page displays the client ID, client secret, authorization URL, token URL, and scope. **Copy the client secret immediately** — it is only shown once and cannot be retrieved later.

If MCP is not enabled on the environment, the page will display a warning. If OAuth is not enabled, the OAuth consent flow will not be available and users will need to use WS authentication instead. Contact your Grouper administrator to enable these features.

## Client setup examples

The Grouper MCP server is a remote HTTP streaming MCP server with OAuth authentication. It is designed to work with agentic AI tools — AI assistants that can autonomously discover and call tools on your behalf. Market-leading agentic AI tools include **Claude Code**, **Claude Desktop**, **OpenAI Codex**, and **Cursor**.

In general, connecting to the Grouper MCP server involves two steps:

1. **Add the MCP server** — In your AI tool's settings or via a command, add a new remote MCP server using the URL shown on your Grouper MCP info page (e.g. `[https://grouper.example.edu/grouper-ws/mcp](https://grouper.example.edu/grouper-ws/mcp)`).
2. **Authenticate** — The first time you connect, the tool will open your browser for you to log in to Grouper and approve the requested permissions on a consent page.

Below are setup instructions for popular agentic AI tools.

### Claude Code (command line)

[Claude Code](https://docs.anthropic.com/en/docs/claude-code) is Anthropic's agentic command-line tool. To add the Grouper MCP server:

1. Run the following command in your terminal:`claude mcp add --transport http grouper https://grouper.example.edu/grouper-ws/mcp`
2. Start a Claude Code session, then type `/mcp` to see your MCP servers and authenticate. Claude Code will open your browser to complete the OAuth login and consent flow.
3. Once authenticated, you can interact with Grouper by asking Claude questions in natural language (e.g. “What groups is jsmith a member of?”). Claude will automatically call the appropriate Grouper MCP tools.

### Claude Desktop (app)

[Claude Desktop](https://claude.ai/download) is Anthropic's desktop application with built-in MCP support.

1. Open **Settings** (gear icon).
2. Go to **Connectors**.
3. Click **Add custom connector** at the bottom.
4. Enter your Grouper MCP server URL (e.g. `[https://grouper.example.edu/grouper-ws/mcp](https://grouper.example.edu/grouper-ws/mcp)`).
5. Click **Add**. Claude will open your browser to complete the OAuth login and consent flow when you first use a Grouper tool.

### OpenAI Codex (command line)

[OpenAI Codex](https://openai.com/index/introducing-codex/) is OpenAI's agentic command-line tool. To add the Grouper MCP server:

1. Add the server:`codex mcp add grouper --url https://grouper.example.edu/grouper-ws/mcp`
2. Log in to the server via OAuth:`codex mcp login grouper`This will open your browser to complete the Grouper OAuth login and consent flow.
3. Once authenticated, you can ask Codex questions about Grouper and it will call the appropriate MCP tools automatically.

### Cursor

[Cursor](https://www.cursor.com/) is an AI-powered code editor with built-in MCP support.

1. Open **Cursor Settings**.
2. Navigate to **Tools & Integrations**.
3. Click **New MCP Server**.
4. Enter the server name (e.g. `grouper`) and your Grouper MCP server URL (e.g. `[https://grouper.example.edu/grouper-ws/mcp](https://grouper.example.edu/grouper-ws/mcp)`).
5. Click **Connect** to authorize. Cursor will open your browser to complete the OAuth login and consent flow.

### Other AI tools

Any agentic AI tool that supports remote MCP servers with HTTP streaming transport should work with the Grouper MCP server. Look for an option to add or connect to a remote MCP server in your tool's settings, and enter the URL shown on your Grouper MCP info page. The tool will handle the OAuth authentication flow automatically.

### Server-side applications (confidential clients)

Server-side applications like **LibreChat** can securely store a client secret, so they use a *confidential* OAuth client instead of the public PKCE flow used by CLI tools. To set up a server-side application:

1. Go to the MCP info page in the Grouper UI (Miscellaneous → Model Context Protocol).
2. In the **Register confidential OAuth client** section, enter a client name (e.g. `LibreChat`) and the redirect URI for your application.
3. Click **Register confidential client**.
4. Copy the displayed **Client ID**, **Client Secret**, **Authorization URL**, **Token URL**, and **Scope** into your application's OAuth configuration. The client secret is only shown once.

This section is only visible to Grouper sysadmins and members of the `mcpUsersCanRegisterConfidentialOAuthClient` group. The redirect URI must match one of the allowed patterns configured by your Grouper administrator.

### Consent and read-write scope restrictions

When you first connect, your browser will open a Grouper consent page where you choose which permissions to grant. You will only see permission options that your Grouper administrator has authorized for you.

If you check the **Read-write operations** checkbox, you are required to specify the scope of the read-write access by entering at least one of the following:

- **Folder ID paths** – Comma-separated list of folder paths (e.g. `school:departments, org:teams`). The AI tool can modify any group within these folders (recursively). Maximum 10 folders, and the total number of groups across all specified folders must be less than 500.
- **Group ID paths** – Comma-separated list of specific group paths (e.g. `school:departments:faculty, org:teams:admins`). The AI tool can modify these specific groups. Maximum 10 groups.
- **Subject IDs or identifiers** – Comma-separated list of subject IDs or identifiers / login names (e.g. `jsmith, jdoe`). The AI tool can only add or remove these specific subjects as members. Maximum 50 subjects.

These restrictions are enforced on every read-write tool call. If both folders and groups are specified, a group is in scope if it matches a listed group path *or* is within a listed folder. If subjects are specified, every subject involved in a read-write operation must also be in the list. Categories left blank are unrestricted — for example, if you only enter folder paths but leave subjects blank, the AI tool can modify groups in those folders for any subject. Conversely, if you only enter subject IDs but leave folders and groups blank, those subjects can be operated on with any group. At least one category must have a value; if all categories are left blank, no read-write operations are permitted.

The consent page validates your entries before submission. It verifies that the folders and groups you entered exist in Grouper, and that the total number of groups across all specified folders does not exceed 500.

## Available MCP tools

The MCP server exposes Grouper operations as tools that MCP clients can auto-discover and auto-invoke. By interacting with AI in natural language the AI will decide when to call a tool. If you can enable consent so you can approve the AI when it runs a command, that is recommended. There is no way to undo an operation that AI invokes.

### admin_config_search

Search Grouper configuration properties. Supports two search modes: `lucene` (default) for full-text search on config keys and values using a Lucene in-memory index, and `regex` for Java regex matching against config key names only. Sensitive values (passwords, secrets, private keys) are automatically masked as `*******`. Optionally filter by a specific config file. Requires membership in the MCP admin readonly group. For OAuth users, the `admin_readonly` consent scope must also be granted.

The Lucene config index is built in-memory on each WS node from all Grouper configuration files and rebuilt periodically (every hour). It indexes config keys (with dots tokenized as separate words) and non-sensitive values for full-text search.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `searchRegex` | string | Yes | Search query. When `searchType` is `lucene` (default), this is a Lucene query for full-text search on config keys and values (e.g. `provisioner`, `mcp AND docSearch`, `grouper.mcp.*`). When `searchType` is `regex`, this is a Java regex pattern matched against config key names (case-insensitive), e.g. `.*provisioner.*`, `grouper\.mcp\..*`. |
| `searchType` | string | No (default: lucene) | Search mode. `lucene` (default) uses a Lucene full-text index for searching config keys and values. `regex` uses Java regex matching on config key names only. Allowed values: `lucene`, `regex`. |
| `configFile` | string | No | Filter by a specific config file. Available values: `grouper.properties`, `grouper-loader.properties`, `grouper.client.properties`, `grouper.cache.properties`, `grouper-ui.properties`, `grouper-ws.properties`, `subject.properties`. If not specified, searches all config files. |

**Response:** Returns a JSON object with `matchCount`, `searchType` (`lucene` or `regex`), and a `configs` array of objects. Each config entry contains:

- `key` — the configuration property key
- `value` — the current value (masked as `*******` for sensitive configs)
- `configFile` — which config file this property belongs to
- `sensitive` — `true` if the value is a password/secret (only present when true)

When using Lucene search, each entry may also include:

- `configuredIn` — where the value is set: base properties file, override file, or `database`
- `defaultValue` — the default/base value for the property
- `elScript` — the Expression Language script if the property uses EL configuration
- `comment` — documentation comment from the properties file
- `valueType` — the expected type (e.g. `boolean (true or false)`, `text`, `integer`, `password`)
- `required` — `true` if the property is required (only present when true)

If more than 500 results match (regex mode), the list is truncated with a `truncated` flag and `message`.

### admin_daemon_job_message

Retrieve the job message for a specific daemon job log entry by its row ID. Returns the first 20,000 characters of the message (from the `job_message` column, or `job_message_clob` if `job_message` is null). Also returns the job name, status, and start time for context. Use `admin_daemon_logs` first to find the `id` of the log entry you want. Requires membership in the MCP admin readonly group. For OAuth users, the `admin_readonly` consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `id` | string | Yes | The `id` of the `grouper_loader_log` row to retrieve the message for. |

**Response:** Returns a JSON object with `jobName`, `status`, `startedTime`, `jobMessage` (first 20,000 characters), and `truncated` (true if the message was longer than 20,000 characters).

### admin_daemon_job_run

Trigger a daemon job to run on the Grouper daemon server. The job is triggered asynchronously via the Quartz scheduler. Use `admin_daemon_names` to find valid job names, and `admin_daemon_logs` to check the status after triggering. Requires membership in the MCP admin **readwrite** group. For OAuth users, the `admin_readwrite` consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `jobName` | string | Yes | The exact daemon job name to trigger (as it appears in `grouper_loader_log`). |

**Response:** Returns a JSON object with `jobName`, `status` (`TRIGGERED`), and a `message` confirming that the job was triggered on the daemon server.

### admin_daemon_logs

Retrieve daemon job log entries from the `grouper_loader_log` table. Returns the most recent 100 rows ordered by start time (descending). Does *not* return the `job_message` (use `admin_daemon_job_message` for that). At least one of `jobName` or `status` must be provided. Requires membership in the MCP admin readonly group. For OAuth users, the `admin_readonly` consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `jobName` | string | No* | Exact job name to filter by. At least one of `jobName` or `status` is required. |
| `status` | string | No* | Filter by job status (case-insensitive). Common values: `SUCCESS`, `ERROR`, `STARTED`, `RUNNING`, `CONFIG_ERROR`, `SUBJECT_PROBLEMS`, `WARNING`. |
| `startedAfter` | string | No | Filter for jobs started after this date/time. Format: `yyyy-MM-dd` or `yyyy-MM-dd HH:mm:ss` (also accepts `/` as separator). |
| `startedBefore` | string | No | Filter for jobs started before this date/time. Same format as `startedAfter`. |

**Response:** Returns a JSON object with `rowCount` and a `rows` array of log entries. Each entry includes: `id`, `job_name`, `status`, `started_time`, `ended_time`, `millis`, `job_type`, `job_description`, `host`, `insert_count`, `update_count`, `delete_count`, `total_count`, `unresolvable_subject_count`, `parent_job_name`, `last_updated`. If more than 100 results match, only the most recent 100 are returned with a `truncated` flag.

### admin_daemon_names

Search for daemon job names in the Grouper loader log. Takes a search string that is split by whitespace into terms, where each term is matched against the `job_name` column using case-insensitive LIKE. You can include `%` as a wildcard in each term; if a term does not contain `%`, it is automatically wrapped with `%` on both sides. All terms must match (AND logic). Returns distinct job names. Requires membership in the MCP admin readonly group. For OAuth users, the `admin_readonly` consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `searchString` | string | Yes | Search string for daemon job names. Split by whitespace into terms. Each term is matched against `job_name` using LIKE (case-insensitive). Use `%` as a wildcard. If a term does not contain `%`, it is automatically wrapped with `%`. All terms must match (AND logic). Examples: `provisioner`, `SQL%config`, `CHANGE_LOG%recent`. |

**Response:** Returns a JSON object with `jobNameCount` and a `jobNames` array of distinct job name strings. If more than 200 results are found, the list is truncated with a `truncated` flag and `message`.

### admin_external_system_get

Look up users in external systems (Azure, Duo, SCIM, Box, Google, Remedy, Remedy Digital Marketplace, TeamDynamix, FreshService Requesters) configured in Grouper. This tool is useful for troubleshooting provisioning issues by verifying what a user looks like in an external system. Requires membership in the MCP admin readonly group. For OAuth users, the `admin_readonly` consent scope must also be granted.

This tool supports two actions:

#### Action: listExternalSystems

Returns all external systems configured for MCP user lookups. Use this to discover available external system config IDs before calling `getUser`.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `action` | string | Yes | `"listExternalSystems"` |

**Response:** Returns a JSON object with `externalSystemCount` and an `externalSystems` array. Each entry contains `configId`, `type` (azure, duo, scim, box, google, remedy, remedyDigitalMarketplace, teamDynamix, or freshserviceRequesters), `lookupField`, `subjectIdTranslationJexl`, and optionally `documentation` (a description configured by the administrator to help the AI understand the external system).

**Example response:**

`{ "externalSystemCount" : 2, "externalSystems" : [ { "configId" : "myAzure", "type" : "azure", "lookupField" : "userPrincipalName", "subjectIdTranslationJexl" : "${subject.getAttributeValue('eppn')}", "documentation" : "Azure AD for campus users provisioned via Grouper" }, { "configId" : "myDuo", "type" : "duo", "lookupField" : "username", "subjectIdTranslationJexl" : "${subject.getId()}" } ] }`

#### Action: getUser

Looks up a user in an external system by translating a Grouper subject. The Grouper subject is resolved from the provided subject ID or identifier, then translated to the external system's user identifier via a JEXL expression configured by the administrator.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `action` | string | Yes | `"getUser"` |
| `externalSystemConfigId` | string | Yes | The external system config ID (from `listExternalSystems`). |
| `subjectIdOrIdentifier` | string | Yes | A Grouper subject ID or identifier to look up in the external system. |
| `subjectSourceId` | string | No | Optional subject source ID to disambiguate subjects with the same ID in different sources. |

**Response:** Returns a JSON object with the external system config ID, type, lookup field, translated lookup value, Grouper subject details, a `userFound` boolean, and (if found) the full `user` object from the external system.

**Example response (user found in Azure):**

`{ "externalSystemConfigId" : "myAzure", "externalSystemType" : "azure", "lookupField" : "userPrincipalName", "translatedLookupValue" : "jsmith@example.com", "grouperSubjectId" : "jsmith", "grouperSubjectSourceId" : "jdbc", "userFound" : true, "user" : { "accountEnabled" : true, "displayName" : "John Smith", "id" : "abc-123-def", "mailNickname" : "jsmith", "onPremisesImmutableId" : null, "userPrincipalName" : "jsmith@example.com" } }`**Example response (user not found):**

`{ "externalSystemConfigId" : "myAzure", "externalSystemType" : "azure", "lookupField" : "userPrincipalName", "translatedLookupValue" : "jsmith@example.com", "grouperSubjectId" : "jsmith", "grouperSubjectSourceId" : "jdbc", "userFound" : false }`

### attribute_assignment_get

Get attribute assignments for groups, stems, members, or other objects. Provides a simplified interface to the Grouper WS `getAttributeAssignments` operation. Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `attributeAssignType` | string | Yes | The type of assignment to query: `group`, `stem`, `member`, `imm_mem`, `any_mem`, `attr_def`, `group_asgn`, `stem_asgn`, `member_asgn`, `imm_mem_asgn`, `attr_def_asgn`, `any_mem_asgn`. |
| `attributeDefNameName` | string | No | Filter by attribute def name. |
| `ownerGroupName` | string | No | Filter by owner group. |
| `ownerStemName` | string | No | Filter by owner stem. |
| `ownerSubjectId` | string | No | Filter by owner subject. |
| `ownerSubjectSourceId` | string | No | Owner subject source ID (used with `ownerSubjectId`). |
| `action` | string | No | Filter by action name. |

**Response:** Returns a JSON array of assignments, each with `attributeAssignId` (UUID), `attributeAssignType`, `attributeDefNameName`, `ownerGroupName`, `ownerStemName`, `ownerSubjectId`, `action`, and `values` (array of strings). Only non-blank fields are included.

### attribute_assignment_save

Assigns, adds, removes, or replaces attribute assignments on groups, stems, members, or other objects. Delegates to the existing Grouper WS `attribute_assignment_save` operation. Requires membership in the MCP readwrite group. For OAuth users, the `readwrite` consent scope must also be granted. Note: system groups and stems under the Grouper built-in objects stem (default `etc`) are protected and cannot be modified via MCP.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `attributeAssignType` | string | Yes | The type of assignment: `group`, `stem`, `member`, `imm_mem`, `any_mem`, `attr_def`, `group_asgn`, `stem_asgn`, `member_asgn`, `imm_mem_asgn`, `attr_def_asgn`, `any_mem_asgn`. |
| `attributeAssignOperation` | string | Yes | The operation: `assign_attr`, `add_attr`, `remove_attr`, `replace_attrs`. |
| `attributeDefNameName` | string | Yes | The attribute definition name to assign (fully qualified). |
| `ownerGroupName` | string | No | Owner group name for the assignment. |
| `ownerStemName` | string | No | Owner stem name for the assignment. |
| `ownerSubjectId` | string | No | Owner subject ID for the assignment. |
| `ownerSubjectSourceId` | string | No | Owner subject source ID (used with `ownerSubjectId`). |
| `ownerAttributeAssignId` | string | No | UUID of an existing attribute assignment to assign metadata attributes on (assignment-on-assignment). Use with an `_asgn` attributeAssignType (e.g. `group_asgn`). The ID is returned as `attributeAssignId` when assigning the initial attribute. |
| `action` | string | No | Action name, defaults to `assign`. |
| `values` | array of strings | No | Attribute values to assign. When values are provided, the server automatically uses the `assign_value` value operation. |
| `assignmentNotes` | string | No | Notes about the assignment. |

**Response:** Returns a JSON array of results, each with `changed` (boolean) and `attributeAssigns` array containing assignment details including `attributeAssignId` (UUID), `attributeAssignType`, `attributeDefNameName`, `ownerGroupName`, `ownerStemName`, and `action`.

#### Assignment-on-assignment (e.g. attestation)

To configure features like attestation that use name/value pair attributes on a marker assignment, use a two-step workflow:

1. **Assign the marker attribute to the group** with `attributeAssignType=group`. The response includes an `attributeAssignId` UUID for the new assignment.
2. **Assign configuration attributes on that assignment** using `attributeAssignType=group_asgn` and passing the UUID from step 1 as `ownerAttributeAssignId`. Include `values` to set the configuration value.

**Example – enable attestation with email notifications on a group:**

`// Step 1: assign the attestation marker { "attributeAssignType": "group", "attributeAssignOperation": "assign_attr", "attributeDefNameName": "etc:attribute:attestation:attestation", "ownerGroupName": "my:folder:myGroup" } // Response includes attributeAssignId, e.g. "abc-123-def-456" // Step 2: assign a config attribute on the marker assignment { "attributeAssignType": "group_asgn", "attributeAssignOperation": "assign_attr", "attributeDefNameName": "etc:attribute:attestation:attestationSendEmail", "ownerAttributeAssignId": "abc-123-def-456", "values": ["true"] }`**Scope enforcement for assignment-on-assignment.** When using an `_asgn` assign type with `ownerAttributeAssignId`, the server resolves the underlying owner of that assignment (the group, stem, or subject the marker is assigned to) and validates both protected-resource rules and OAuth scope restrictions against it. For example, if your OAuth scope is limited to folder `school:departments`, you cannot assign metadata on a marker attribute that belongs to a group in a different folder. The scope validation covers all owner types: group, stem, member, effective membership (any_mem), immediate membership (imm_mem), and attribute definition (checked against the attribute def's parent folder).

### attribute_def_name_find

Find attribute definition names by scope (partial name match) or exact name. Supports paging. Delegates to the existing Grouper WS `attribute_def_name_find` operation. Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `scope` | string | No | Search scope for partial name matching. |
| `splitScope` | boolean | No | If true, split scope by whitespace and search each term. |
| `attributeDefName` | string | No | Look up by exact attribute def name. |
| `attributeAssignType` | string (enum) | No | Filter by assignment type: `group`, `stem`, `member`, `group_asgn`, `stem_asgn`, `member_asgn`, `imm_mem`, `imm_mem_asgn`, `attr_def`, `attr_def_asgn`, `any_mem`, `any_mem_asgn`. |
| `attributeDefType` | string (enum) | No | Filter by def type: `attr`, `domain`, `limit`, `perm`, `service`, `type`. |
| `pageSize` | integer | No (default: 50) | Number of results per page. |
| `pageNumber` | integer | No (default: 1) | Page number (1-based). |

**Response:** Returns a JSON array of attribute def names, each with `name`, `uuid`, `description`, `displayName`, and `attributeDefName`.

### audit_get

Retrieve audit log entries with optional filtering by type, group, stem, subject, date range, and who performed the action. Supports paging. Delegates to the existing Grouper WS `audit_get` operation. Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `auditType` | string | No | Filter by audit type (e.g., `group`, `membership`). |
| `auditActionId` | string | No | Filter by audit action ID. |
| `groupName` | string | No | Filter to audits for this group. |
| `stemName` | string | No | Filter to audits for this stem. |
| `subjectId` | string | No | Filter to audits about this subject. |
| `subjectSourceId` | string | No | Subject source ID (used with `subjectId`). |
| `actionsPerformedBySubjectId` | string | No | Filter to actions performed by this subject. |
| `actionsPerformedBySubjectSourceId` | string | No | Source ID for the subject who performed the actions. |
| `fromDate` | string | No | Start of date range, format `yyyy/MM/dd HH:mm:ss.SSS`. |
| `toDate` | string | No | End of date range, format `yyyy/MM/dd HH:mm:ss.SSS`. |
| `pageSize` | integer | No (default: 50) | Number of entries per page. |

**Response:** Returns a JSON array of audit entries, each with `id`, `actionName`, `auditCategory`, `timestamp`, and an `entries` object with key-value pairs from audit columns.

### doc_search

Search institutional documentation or retrieve specific document chunks using Apache Lucene. Supports four actions: `query` (default) for full-text search, `retrieveChunk` for retrieving specific chunks by name and index (useful for getting additional context around a search result), `listSourceConfigIds` for listing available documentation sources with descriptions, and `listNames` for listing document names within a given source.

This tool is only available when at least one document search source is available to the user. Sources include administrator-configured SQL queries and the built-in `grouperDataDictionary` source which indexes data field and data row descriptions. Data dictionary results are filtered by privacy realm access — only entries the user can view/read/update are returned.

The tool description dynamically includes documentation about each configured source so the AI client knows what content is searchable.

Requires membership in the MCP readonly group. For OAuth users, the `readonly` consent scope must also be granted.

The Lucene index is built in-memory on each WS node from configured database queries and rebuilt periodically (default: every hour). No additional infrastructure is required.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `action` | string | No (default: query) | The action to perform: `query` searches documents by keyword; `retrieveChunk` retrieves specific chunks by sourceConfigId, name, and chunkIndexes; `listSourceConfigIds` lists available documentation sources with descriptions; `listNames` lists document names for a given sourceConfigId (max 1000, indicates if truncated). |
| `query` | string | Yes (for query action) | The search query. Use natural language terms related to the topic you want to find. The search uses full-text matching with stemming. |
| `maxResults` | integer | No (default: 10) | Maximum number of document chunks to return (max 50). Only for `query` action. |
| `sourceConfigId` | string | No (for query), Yes (for retrieveChunk and listNames) | Limit search to a specific documentation source by its config ID. Required for `retrieveChunk` and `listNames` actions. |
| `name` | string | No (at least one of name or url required for retrieveChunk) | Document name. Use the exact `name` value from a previous search result. |
| `url` | string | No (at least one of name or url required for retrieveChunk) | Document URL. Use the exact `url` value from a previous search result. |
| `chunkIndexes` | array of integers | Yes (for retrieveChunk) | Array of chunk indexes to retrieve (max 50). Use `chunkIndex` and `totalChunksForDocument` from search results to determine which chunks to request. Required for `retrieveChunk` action. |
| `searchType` | string | No (default: keyword) | Search mode for the `query` action. `keyword` (default) performs simple keyword matching (special characters are escaped). `lucene` allows full Lucene query syntax including AND, OR, wildcards, phrases, etc. Allowed values: `keyword`, `lucene`. |

**Response (query/retrieveChunk):** Returns a JSON object with a `results` array and `totalResults` count. Each result contains `content` (the document chunk text), `url` (source URL, if available), `name` (document name), `sourceConfigId`, `score` (relevance score, for query action), `chunkIndex` (position of the chunk within the original document), and `totalChunksForDocument` (total number of chunks for this document). Use `totalChunksForDocument` to know how many chunks exist, then use the `retrieveChunk` action to fetch additional chunks for more context.

**Response (listSourceConfigIds):** Returns a JSON object with a `sources` array and `totalSources` count. Each source contains `sourceConfigId` and `description`.

**Response (listNames):** Returns a JSON object with a `names` array, `totalNames` count, and optionally `truncated` (boolean) and `notice` if the list exceeds 1000 names.

### entity_get

Looks up Grouper subjects by subject ID, subject identifier, or search string. Can optionally filter results to members of a specific group. Delegates to the existing Grouper WS `entity_get` operation. Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

Provide exactly one of `subjectId`, `subjectIdentifier`, or `searchString`.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `subjectId` | string | One of three* | The subject ID to look up. Mutually exclusive with `subjectIdentifier` and `searchString`. |
| `subjectIdentifier` | string | One of three* | The subject identifier to look up (e.g., login ID or eppn). Mutually exclusive with `subjectId` and `searchString`. |
| `searchString` | string | One of three* | Free-form search string to find subjects (e.g., name or partial match). May return multiple results. Mutually exclusive with `subjectId` and `subjectIdentifier`. |
| `sourceIds` | array of strings | No | Restrict the lookup to specific subject sources |
| `groupName` | string | No | Group name to filter subjects by group membership (e.g., `stem1:stem2:groupName`). Only subjects who are members of this group will be returned. |
| `memberFilter` | string (enum) | No | Membership filter when `groupName` is specified. One of: `All` (default), `Immediate` (direct members only), `Effective` (indirect members only), `Composite`, `NonImmediate`. |
| `privilegeListName` | string (enum) | No | Privilege list name to filter by instead of membership when `groupName` is specified. One of: `admins`, `updaters`, `readers`, `viewers`, `optins`, `optouts`, `groupAttrReaders`, `groupAttrUpdaters`. If omitted, filters by the standard membership list. |
| `includeSubjectDetail` | boolean | No (default: false) | If true, return extended subject attributes |
| `subjectAttributeNames` | array of strings | No | Specific attribute names to return. If `includeSubjectDetail` is true and this is empty, all configured attributes are returned. |

* Exactly one of `subjectId`, `subjectIdentifier`, or `searchString` must be provided.

**Response:** Returns a JSON object (single result) or array (multiple results) with `subjectId`, `name`, `sourceId`, and optionally an `attributes` object with key-value pairs.

### entity_get_groups

Find which groups a subject belongs to. Optionally filter by stem scope, membership type, and field. Supports paging. Delegates to the existing Grouper WS `entity_get_groups` operation. Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `subjectId` | string | One of two* | Subject to find groups for. Mutually exclusive with `subjectIdentifier`. |
| `subjectIdentifier` | string | One of two* | Subject identifier. Mutually exclusive with `subjectId`. |
| `subjectSourceId` | string | No | Subject source ID to restrict the subject lookup. |
| `memberFilter` | string (enum) | No | Membership filter type. One of: `All`, `Immediate`, `Effective`, `Composite`, `NonImmediate`. |
| `privilegeListName` | string (enum) | No | Privilege list name to retrieve instead of membership. One of: `admins`, `updaters`, `readers`, `viewers`, `optins`, `optouts`, `groupAttrReaders`, `groupAttrUpdaters`. If omitted, returns groups the subject is a member of. |
| `scope` | string | No | Scope string to filter groups (e.g., `stem1:stem2:`). |
| `stemName` | string | No | Stem name to scope the search. |
| `stemScope` | string (enum) | No | `ONE_LEVEL` or `ALL_IN_SUBTREE`. |
| `pageSize` | integer | No | Number of groups per page. |
| `pageNumber` | integer | No | Page number (1-based). |

* Must provide exactly one of `subjectId` or `subjectIdentifier`.

**Response:** Returns a JSON array of groups, each with `name`, `extension`, `displayExtension`, `description`, and `uuid`.

### folder_delete

Delete a Grouper stem (folder). The stem is looked up by name and permanently deleted. Requires membership in the MCP readwrite group. For OAuth users, the `readwrite` consent scope must also be granted. Note: system stems and stems under the Grouper built-in objects stem (default `etc`) are protected and cannot be deleted via MCP.

**Security:** The calling user must have STEM_ADMIN privilege on the stem to delete it. If the calling user does not have sufficient privileges, the operation will fail with an access denied error.

#### Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `stemName` | string | Yes | The fully qualified stem name to delete (e.g., `stem1:stem2`). |

**Response:** Returns a JSON object with `resultCode` (`DELETE`), `success` (boolean), `name` (the deleted stem name), `displayExtension`, and `uuid`.

### folder_find

Search for Grouper stems (folders) by name, parent stem, or attribute. Supports exact and approximate name matching, and searching within a specific parent stem. Delegates to the existing Grouper WS `folder_find` operation. Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `stemQueryFilterType` | string | Yes | Type of search: `FIND_BY_STEM_NAME`, `FIND_BY_STEM_NAME_APPROXIMATE`, `FIND_BY_STEM_UUID`, `FIND_BY_APPROXIMATE_ATTRIBUTE`. |
| `stemName` | string | No | Stem name to search for. Used with exact or approximate name searches. |
| `stemUuid` | string | No | Stem UUID. Used with `FIND_BY_STEM_UUID`. |
| `parentStemName` | string | No | Parent stem name to search within. |
| `parentStemNameScope` | string | No | `ONE_LEVEL` or `ALL_IN_SUBTREE` (default). |
| `stemAttributeValue` | string | No | Attribute value to search for. Used with `FIND_BY_APPROXIMATE_ATTRIBUTE`. |
| `includeGdgTypes` | boolean | No | If true, include Grouper Deployment Guide (GDG) type names (e.g., policy, ref, basis, manual, app, org, test, service, readOnly, etc.) for each folder in the results. These are different from typeOfGroups (group, role, entity) which is a structural classification. Defaults to false. |

**Response:** Returns a JSON object with `totalStemsReturned` and a `stems` array. Each stem includes `name`, `displayName`, `extension`, `description`, and `uuid`. If `includeGdgTypes` is true, each stem also includes a `gdgTypes` array with GDG type names (e.g., "policy", "ref", "basis", "manual").

### group_add_member

Add one or more subjects as members of a Grouper group. Each subject is identified by `subjectId` or `subjectIdentifier` (and optionally `sourceId`). Supports setting membership enabled/disabled dates for time-limited or future-dated memberships. Delegates to the existing Grouper WS `group_add_member` operation. Requires membership in the MCP readwrite group. For OAuth users, the `readwrite` consent scope must also be granted. Note: system groups and stems under the Grouper built-in objects stem (default `etc`) are protected and cannot be modified via MCP.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `groupName` | string | Yes | The fully qualified group name to add members to (e.g., `stem1:stem2:groupName`). |
| `subjects` | array of objects | Yes | Array of subjects to add. Each object must have either `subjectId` or `subjectIdentifier` (mutually exclusive), and optionally `sourceId`. |
| `replaceAllExisting` | boolean | No (default: false) | If true, replace all existing members of the group with the provided subjects. Use with caution. |
| `fieldName` | string | No | Field (list) name for the membership. Defaults to `members` (the standard membership list). |
| `disabledTime` | string | No | Date when this membership will be disabled, in format `yyyy/MM/dd HH:mm:ss.SSS`. Used for time-limited memberships. |
| `enabledTime` | string | No | Date when this membership will be enabled, in format `yyyy/MM/dd HH:mm:ss.SSS`. Used for future-dated provisioning. |

**Subject object fields:**

| Field | Type | Description |
| --- | --- | --- |
| `subjectId` | string | The subject ID. Mutually exclusive with `subjectIdentifier`. |
| `subjectIdentifier` | string | The subject identifier (e.g., login ID or eppn). Mutually exclusive with `subjectId`. |
| `sourceId` | string | Optional source ID to restrict the subject lookup. |

**Response:** Returns a JSON object (single subject) or array (multiple subjects) with per-subject results including `resultCode` (e.g., `SUCCESS_CREATED`, `SUCCESS_ALREADY_EXISTED`), `success` (boolean), `subjectId`, `name`, and `sourceId`.

### group_delete

Delete a Grouper group. The group is looked up by name and permanently deleted. Requires membership in the MCP readwrite group. For OAuth users, the `readwrite` consent scope must also be granted. Note: system groups and stems under the Grouper built-in objects stem (default `etc`) are protected and cannot be deleted via MCP.

**Security:** The calling user must have ADMIN privilege on the group to delete it. If the calling user does not have sufficient privileges, the operation will fail with an access denied error.

#### Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `groupName` | string | Yes | The fully qualified group name to delete (e.g., `stem1:stem2:groupName`). |

**Response:** Returns a JSON object with `resultCode` (`DELETE`), `success` (boolean), `name` (the deleted group name), `displayExtension`, and `uuid`.

### group_find

Search for Grouper groups by name, stem, or attribute. Supports exact and approximate name matching, searching within a specific stem, and paging/sorting. Delegates to the existing Grouper WS `group_find` operation. Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `queryFilterType` | string | Yes | Type of search: `FIND_BY_GROUP_NAME_EXACT`, `FIND_BY_GROUP_NAME_APPROXIMATE`, `FIND_BY_STEM_NAME`, `FIND_BY_GROUP_UUID`, `FIND_BY_APPROXIMATE_ATTRIBUTE`. |
| `groupName` | string | No | Group name to search for. Used with exact or approximate name searches. |
| `groupUuid` | string | No | Group UUID. Used with `FIND_BY_GROUP_UUID`. |
| `stemName` | string | No | Stem name to search within. Used with `FIND_BY_STEM_NAME` or to scope an approximate name search. |
| `stemNameScope` | string | No | `ONE_LEVEL` or `ALL_IN_SUBTREE` (default). |
| `groupAttributeValue` | string | No | Attribute value to search for. Used with `FIND_BY_APPROXIMATE_ATTRIBUTE`. |
| `typeOfGroups` | string | No | Comma-separated types: group, role, entity. Default is all. |
| `pageSize` | integer | No | Number of results per page (default 50). |
| `pageNumber` | integer | No | Page number, 1-indexed (default 1). |
| `sortString` | string | No | Field to sort by: name, displayName, extension, displayExtension. |
| `ascending` | boolean | No | Sort ascending (true, default) or descending (false). |
| `includeGdgTypes` | boolean | No | If true, include Grouper Deployment Guide (GDG) type names (e.g., policy, ref, basis, manual, app, org, test, service, readOnly, etc.) for each group in the results. These are different from typeOfGroups (group, role, entity) which is a structural classification. Defaults to false. |
| `includeGroupEligibilityRequirement` | boolean | No | If true, include membership eligibility requirements (e.g., requireEmployee, requireAffiliate) for each group. These are configured requirements that restrict who can be added as a member (the member must also belong to a specified population group). Returned as a comma-separated string of requirement configIds. Defaults to false. |
| `includeProvisioning` | boolean | No | If true, include provisioning target names for each group that is being provisioned to external systems (e.g., LDAP, Active Directory, Google). Only targets the authenticated user is allowed to view are returned (user must be WHEEL/ROOT/VIEWONLY_ROOT or a member of the target's `groupAllowedToView` group). Returned as a comma-separated string. Defaults to false. |
| `includeCompositeInfo` | boolean | No | If true, include composite (factor) information for each group. A composite group is one whose membership is defined by a set operation on two other groups. If a group is a composite group, the response will include `isComposite` (true/false), and if true, a `compositeInfo` object with `compositeType` (union, intersection, or complement), `leftFactorGroupName`, and `rightFactorGroupName`. Defaults to false. |

**Response:** Returns a JSON object with `totalGroupsReturned`, `pageSize`, `pageNumber`, and a `groups` array. Each group includes `name`, `displayName`, `extension`, `description`, `uuid`, and `typeOfGroup`. If `includeGdgTypes` is true, each group also includes a `gdgTypes` array with GDG type names (e.g., "policy", "ref", "basis", "manual"). If `includeGroupEligibilityRequirement` is true, each group may also include an `eligibilityRequirement` string with comma-separated configIds (e.g., "requireEmployee, requireAffiliate") indicating membership eligibility restrictions. If `includeProvisioning` is true, each group may also include a `provisioning` string with comma-separated provisioning target names (e.g., "ldapProvisioner, googleProvisioner") indicating which external systems the group is being provisioned to. If `includeCompositeInfo` is true, each group includes an `isComposite` boolean, and if the group is a composite group, a `compositeInfo` object with `compositeType` (union, intersection, or complement), `leftFactorGroupName`, and `rightFactorGroupName`.

### group_get_members

Get the members of a group with paging support. Defaults to returning 50 members per page to prevent excessive results. Delegates to the existing Grouper WS `group_get_members` operation. Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `groupName` | string | Yes | The fully qualified group name to get members of (e.g., `stem1:stem2:groupName`). |
| `memberFilter` | string (enum) | No | Membership filter type. One of: `All`, `Immediate`, `Effective`, `Composite`, `NonImmediate`. |
| `privilegeListName` | string (enum) | No | Privilege list name to retrieve instead of membership. One of: `admins`, `updaters`, `readers`, `viewers`, `optins`, `optouts`, `groupAttrReaders`, `groupAttrUpdaters`. If omitted, returns the standard membership list. |
| `sourceIds` | string | No | Comma-separated source IDs to filter by. |
| `pageSize` | integer | No (default: 50) | Number of members per page. |
| `pageNumber` | integer | No (default: 1) | Page number (1-based). |

**Response:** Returns a JSON object with `pageSize`, `pageNumber`, and a `subjects` array. Each subject has `subjectId`, `name`, and `sourceId`.

### group_get_permissions

Retrieve the permission assignments for a group (role). Permissions in Grouper allow you to define fine-grained access controls by assigning named permissions to roles. This tool shows which permissions are currently assigned to a given role, with optional filtering by action name (for example, `read` or `write`). Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `groupName` | string | Yes | The fully qualified name of the group (role) to look up permissions for (e.g., `stem1:stem2:roleName`). |
| `action` | string | No | Filter results to only permissions with this action (e.g., `read`, `write`). If omitted, permissions for all actions are returned. |

**Response:** Returns a JSON array of permission assignments. Each entry includes the `permissionName` (the attribute definition name), `permissionDefName` (the attribute definition), `action`, `roleName`, `permissionType` (such as `role` or `role_subject`), and `enabled` status. If the permission is assigned to a specific subject within the role, the `subjectId` and `sourceId` are also included.

### group_has_member

Check if one or more subjects are members of a Grouper group. Each subject is identified by `subjectId` or `subjectIdentifier` (and optionally `sourceId`). Returns the membership status for each subject. Supports point-in-time queries to check historical membership. Delegates to the existing Grouper WS `group_has_member` operation. Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `groupName` | string | Yes | The fully qualified group name to check membership in (e.g., `stem1:stem2:groupName`). |
| `subjects` | array of objects | Yes | Array of subjects to check. Each object must have either `subjectId` or `subjectIdentifier` (mutually exclusive), and optionally `sourceId`. |
| `memberFilter` | string (enum) | No | Membership filter type. One of: `All` (default), `Immediate` (direct members only), `Effective` (indirect members only), `Composite`, `NonImmediate`. Cannot be used with point-in-time queries. |
| `privilegeListName` | string (enum) | No | Privilege list name to check instead of membership. One of: `admins`, `updaters`, `readers`, `viewers`, `optins`, `optouts`, `groupAttrReaders`, `groupAttrUpdaters`. If omitted, checks the standard membership list. |
| `pointInTimeFrom` | string | No | Start of point-in-time query range, in format `yyyy/MM/dd HH:mm:ss.SSS`. Used to check historical membership. If specified without `pointInTimeTo`, the range is from this time to now. |
| `pointInTimeTo` | string | No | End of point-in-time query range, in format `yyyy/MM/dd HH:mm:ss.SSS`. Used to check historical membership. If specified without `pointInTimeFrom`, the range is from the earliest point in time to this time. |

**Subject object fields:**

| Field | Type | Description |
| --- | --- | --- |
| `subjectId` | string | The subject ID. Mutually exclusive with `subjectIdentifier`. |
| `subjectIdentifier` | string | The subject identifier (e.g., login ID or eppn). Mutually exclusive with `subjectId`. |
| `sourceId` | string | Optional source ID to restrict the subject lookup. |

**Response:** Returns a JSON object (single subject) or array (multiple subjects) with per-subject results including `resultCode` (e.g., `IS_MEMBER`, `IS_NOT_MEMBER`, `SUBJECT_NOT_FOUND`), `isMember` (boolean), `subjectId`, `name`, and `sourceId`.

### group_remove_member

Remove one or more subjects from a Grouper group. Each subject is identified by `subjectId` or `subjectIdentifier` (and optionally `sourceId`). Delegates to the existing Grouper WS `group_remove_member` operation. Requires membership in the MCP readwrite group. For OAuth users, the `readwrite` consent scope must also be granted. Note: system groups and stems under the Grouper built-in objects stem (default `etc`) are protected and cannot be modified via MCP.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `groupName` | string | Yes | The fully qualified group name to remove members from (e.g., `stem1:stem2:groupName`). |
| `subjects` | array of objects | Yes | Array of subjects to remove. Each object must have either `subjectId` or `subjectIdentifier` (mutually exclusive), and optionally `sourceId`. |
| `fieldName` | string | No | Field (list) name for the membership. Defaults to `members` (the standard membership list). |

**Subject object fields:**

| Field | Type | Description |
| --- | --- | --- |
| `subjectId` | string | The subject ID. Mutually exclusive with `subjectIdentifier`. |
| `subjectIdentifier` | string | The subject identifier (e.g., login ID or eppn). Mutually exclusive with `subjectId`. |
| `sourceId` | string | Optional source ID to restrict the subject lookup. |

**Response:** Returns a JSON object (single subject) or array (multiple subjects) with per-subject results including `resultCode`, `success` (boolean), `subjectId`, `name`, and `sourceId`.

### group_rename

Rename a Grouper group by changing its extension (short name). The group stays in the same parent stem but gets a new extension, which changes its fully qualified name. Requires membership in the MCP readwrite group. For OAuth users, the `readwrite` consent scope must also be granted. Note: system groups and stems under the Grouper built-in objects stem (default `etc`) are protected and cannot be renamed via MCP.

**Security:** The calling user must have ADMIN privilege on the group to rename it. If the calling user does not have sufficient privileges, the operation will fail with an access denied error.

#### Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `groupName` | string | Yes | The current fully qualified group name (e.g., `stem1:stem2:oldName`). |
| `newExtension` | string | Yes | The new short name (extension) for the group (e.g., `newName`). The group will be renamed to `parentStem:newExtension`. |

**Response:** Returns a JSON object with `resultCode`, `success` (boolean), `previousName` (the original fully qualified name), `name` (the new fully qualified name), `newExtension`, `displayExtension`, `description`, and `uuid`.

### group_save

Create, update, or manage a Grouper group. Uses an action-based approach where the `action` parameter determines which operation to perform. This design prevents accidental data loss by using partial updates instead of full replacements, and provides dedicated actions for managing group types, composites, eligibility requirements, and provisioners. Requires membership in the MCP readwrite group. For OAuth users, the `readwrite` consent scope must also be granted. Note: system groups and stems under the Grouper built-in objects stem (default `etc`) are protected and cannot be modified via MCP.

**Security:** All operations run as the calling user's Grouper session, so standard Grouper privileges are enforced. Each action has its own privilege requirements as noted below. If the calling user does not have sufficient privileges, the operation will fail with an access denied error.

#### Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `action` | string (enum) | Yes | The operation to perform: `createGroup`, `createOrUpdateGroup`, `updateGroupPart`, `addGroupType`, `removeGroupType`, `addComposite`, `updateComposite`, `removeComposite`, `addEligibilityRequirement`, `removeEligibilityRequirement`, `addProvisioner`, `removeProvisioner`. |
| `groupName` | string | Yes | Fully qualified group name (e.g., `stem1:stem2:groupName`). Required for all actions. |
| `description` | string | No | Group description. Used with `createGroup`, `createOrUpdateGroup`, and `updateGroupPart`. |
| `displayExtension` | string | No | Display extension (friendly name). Used with `createGroup`, `createOrUpdateGroup`, and `updateGroupPart`. |
| `typeOfGroup` | string | No | Type of group: `group`, `role`, `entity`. Used with `createGroup` and `createOrUpdateGroup`. |
| `objectType` | string | No | Object type name: `basis`, `ref`, `policy`, `etc`, `grouperSecurity`, `org`, `app`, `service`, `readOnly`, `test`, `manual`, `intermediate`. Required for `addGroupType` and `removeGroupType`. |
| `dataOwner` | string | No | Data owner for the object type. Applicable for types: ref, basis, policy, org, manual. Used with `addGroupType`. |
| `memberDescription` | string | No | Member description for the object type. Applicable for types: ref, basis, policy, org, manual. Used with `addGroupType`. |
| `serviceName` | string | No | Service name for the object type. Applicable for type: app. Used with `addGroupType`. |
| `compositeType` | string (enum) | No | Composite type: `COMPLEMENT` (left minus right), `INTERSECTION` (left and right). Required for `addComposite` and `updateComposite`. |
| `leftGroupName` | string | No | Fully qualified name of the left factor group. Required for `addComposite` and `updateComposite`. |
| `rightGroupName` | string | No | Fully qualified name of the right factor group. Required for `addComposite` and `updateComposite`. |
| `configId` | string | No | Eligibility requirement config ID from the `grouper.membershipRequirement.{configId}.*` configuration. Required for `addEligibilityRequirement` and `removeEligibilityRequirement`. |
| `targetName` | string | No | Provisioning target name. Required for `addProvisioner` and `removeProvisioner`. |

#### Actions

- **createGroup** — Create a new group. Fails if the group already exists. Parent stems are created automatically if they don't exist. *Privilege: CREATE on the parent stem.* Uses: `groupName`, `description`, `displayExtension`, `typeOfGroup`.
- **createOrUpdateGroup** — Create a new group or update an existing one. If the group does not exist it is created (parent stems are created automatically). If the group already exists, only explicitly provided fields are changed; other settings are preserved. *Privilege: CREATE on the parent stem (for insert) or ADMIN on the group (for update).* Uses: `groupName`, `description`, `displayExtension`, `typeOfGroup`.
- **updateGroupPart** — Update specific fields on an existing group without replacing all settings. Only fields that are explicitly provided will be changed; other settings are preserved. *Privilege: ADMIN on the group.* Uses: `groupName`, `description`, `displayExtension`.
- **addGroupType** — Assign an object type to a group. *Privilege: ADMIN on the group.* Uses: `groupName`, `objectType`, and optionally `dataOwner`, `memberDescription`, `serviceName`.
- **removeGroupType** — Remove an object type from a group. *Privilege: ADMIN on the group.* Uses: `groupName`, `objectType`.
- **addComposite** — Make a group a composite of two other groups. *Privilege: UPDATE on the owner group, READ on both factor groups.* Uses: `groupName`, `compositeType`, `leftGroupName`, `rightGroupName`.
- **updateComposite** — Update the composite type or factor groups on an existing composite group. Fails if the group does not have a composite. *Privilege: UPDATE on the owner group, READ on both factor groups.* Uses: `groupName`, `compositeType`, `leftGroupName`, `rightGroupName`.
- **removeComposite** — Remove the composite definition from a group. *Privilege: UPDATE on the group.* Uses: `groupName`.
- **addEligibilityRequirement** — Assign an eligibility requirement to restrict who can be a member of the group. Members must be in the requirement's population group. Existing members who don't meet the requirement are automatically removed. *Privilege: GROUP_ATTR_UPDATE on the group and ATTR_UPDATE on the requirement's attribute definition.* Uses: `groupName`, `configId`.
- **removeEligibilityRequirement** — Remove an eligibility requirement from a group. Existing members remain unchanged. *Privilege: GROUP_ATTR_UPDATE on the group and ATTR_UPDATE on the requirement's attribute definition.* Uses: `groupName`, `configId`.
- **addProvisioner** — Enable provisioning of a group to a target. Validates that the target exists and the user is authorized for the target. *Privilege: membership in the provisioning target's `groupAllowedToAssign` group, or wheel/root.* Uses: `groupName`, `targetName`.
- **removeProvisioner** — Disable provisioning of a group from a target. *Privilege: membership in the provisioning target's `groupAllowedToAssign` group, or wheel/root.* Uses: `groupName`, `targetName`.

**Response:** Returns a JSON object with `action` (the action performed), `resultCode` (e.g., `INSERT`, `UPDATE`, `NO_CHANGE`, `DELETE`), `success` (boolean), and `groupName`. For `createGroup` and `updateGroupPart`, also includes `name`, `displayExtension`, `description`, and `uuid`. For `addComposite`, includes `compositeType`, `leftGroupName`, and `rightGroupName`. For eligibility requirement actions, includes `configId` and `requireGroupName` (on add). For provisioner actions, includes `targetName`.

### institutional_tools

Discover and execute institution-specific tools (GSH templates) that the deployer has made available via MCP. GSH templates are configurable scripts in Grouper that automate common administrative tasks. When a deployer marks a template as MCP-enabled, it becomes available through this tool for any security run type (`wheel`, `specifiedGroup`, `privilegeOnObject`, or `everyone`). This tool operates at the **readonly** MCP authorization level, meaning any user with MCP readonly access can call it. However, individual templates may require readwrite access based on their `mcpReadonly` configuration — templates not marked as readonly will only be visible to and executable by users with readwrite access. For OAuth users, the `readonly` consent scope is sufficient for readonly templates; the `readwrite` consent scope is required for non-readonly templates.

**Dynamic tool listing:** This tool is only advertised in the MCP `tools/list` response when there are MCP-enabled templates available for the authenticated user. If no templates are accessible (based on the user’s security context and readwrite access), the `institutional_tools` tool will not appear at all. When templates are available, the tool’s description dynamically includes the names of the available tools (e.g., “Available tools: Create Working Group, Lookup Memberships”), giving the AI model immediate visibility into what institutional tools can be used without needing to call the `schema` action first.

**Scope-based filtering:** For users with readwrite data-scope restrictions, non-readonly templates that require a group or folder owner are filtered out if the user’s approved scopes do not include any groups or folders (i.e., the user is scoped to subjects only). A template that executes on groups requires the user to have at least one group in scope; a template that executes on folders requires at least one folder in scope. This filtering applies to the tool listing, the `schema` response, and execution.

This tool supports two actions:

#### Action: schema

Returns all MCP-enabled templates the authenticated user is authorized to execute, including their configuration ID, display name, description, input definitions, access level, and whether they execute on a group name, folder name, or both. Templates marked as `mcpReadonly` are visible to all MCP users; other templates are only visible to users with readwrite access.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `action` | string | Yes | `"schema"` |

**Response:** Returns a JSON object with `toolCount` and a `tools` array. Each entry contains:

- `configId` — the template configuration ID (used in the `execute` action)
- `name` — display name of the template
- `description` — description of what the template does
- `securityRunType` — how the template enforces security: `wheel`, `specifiedGroup`, `privilegeOnObject`, or `everyone`
- `mcpReadonly` — boolean, whether this template is available at the readonly access level
- `executeOnGroupName` — boolean, whether this template can be run on a group
- `executeOnFolderName` — boolean, whether this template can be run on a folder
- `inputs` — array of input definitions, each with:
  
  - `name` — the input name (without the internal `gsh_input_` prefix; both the short name and the full `gsh_input_`-prefixed name are accepted when executing)
  - `type` — `string`, `boolean`, `integer`, or `file`
  - `required` — boolean
  - `label` — display label
  - `description` — description of the input
  - `defaultValue` — default value (if configured)
  - `formElement` — UI form element type (`text`, `textarea`, `dropdown`, etc.)
  - `validation` — (optional) object with `type`, `regex`, `message`
  - `maxLength` — (optional) maximum character length
  - `mcpScopeType` — (optional, only on non-readonly templates) one of `folders`, `groups`, or `subjects`. When present, the input value is validated against the user’s approved OAuth readwrite scopes before execution

**Example response (readwrite template with scope validation):**

`{ "toolCount" : 2, "tools" : [ { "configId" : "createWorkingGroup", "name" : "Create Working Group", "description" : "Creates a new working group with standard structure", "securityRunType" : "wheel", "mcpReadonly" : false, "executeOnGroupName" : false, "executeOnFolderName" : true, "inputs" : [ { "name" : "groupExtension", "type" : "string", "required" : true, "label" : "Group Extension", "description" : "The short name for the new group", "formElement" : "text", "validation" : { "type" : "regex", "regex" : "^[a-zA-Z0-9_]+$", "message" : "Only letters, numbers, and underscores" }, "maxLength" : 100 }, { "name" : "targetFolder", "type" : "string", "required" : true, "label" : "Target Folder", "description" : "The folder where the group will be created", "formElement" : "text", "mcpScopeType" : "folders" } ] }, { "configId" : "lookupMemberships", "name" : "Lookup Memberships", "description" : "Lists memberships for a given group", "securityRunType" : "everyone", "mcpReadonly" : true, "executeOnGroupName" : true, "executeOnFolderName" : false, "inputs" : [ { "name" : "maxResults", "type" : "integer", "required" : false, "label" : "Max Results", "description" : "Maximum number of results to return", "defaultValue" : "100", "formElement" : "text" } ] } ] }`

#### Action: execute

Executes an MCP-enabled template by config ID with the provided inputs. The template is executed using the deployer-configured run-as type (e.g., GrouperSystem) with the MCP user as the current user for security checks. For non-readonly templates, any inputs configured with an `mcpScopeType` are validated against the user’s approved OAuth readwrite scopes before execution proceeds.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `action` | string | Yes | `"execute"` |
| `configId` | string | Yes | The template config ID (from the `schema` action). |
| `ownerType` | string | No | `"group"` or `"stem"`. Specifies the context object type. Check the schema to see if the template supports groups, folders, or both. |
| `ownerGroupName` | string | No* | Fully qualified group name. Required when `ownerType` is `"group"`. |
| `ownerStemName` | string | No* | Fully qualified folder name. Required when `ownerType` is `"stem"`. |
| `inputs` | object | No | Key-value pairs of input values. Keys are the input names as shown in the `schema` response (e.g., `"groupExtension"`). The full `gsh_input_`-prefixed form (e.g., `"gsh_input_groupExtension"`) is also accepted. All input values are strings. |

**Response:** Returns a JSON object with `configId`, `success` (boolean), `valid` (boolean, whether input validation passed), `outputLines` (array of `{messageType, text}` objects with message types `"success"`, `"info"`, or `"error"`), `validationLines` (array of `{inputName, text}` objects if validation failed), and optionally `wsOutput` (custom output set by the template) and `error` (error message if execution failed).

**Example response (success):**

`{ "configId" : "createWorkingGroup", "success" : true, "valid" : true, "outputLines" : [ { "messageType" : "success", "text" : "Created group: ref:workingGroups:myNewGroup" } ] }`**Example response (validation error):**

`{ "configId" : "createWorkingGroup", "success" : false, "valid" : false, "validationLines" : [ { "inputName" : "groupExtension", "text" : "Only letters, numbers, and underscores" } ] }`**Example response (scope validation denied):**

`{ "error" : "Access denied: input 'targetFolder' value 'app:restricted:folder' is not within the user's approved readwrite scope for folders" }`

## The deployer controls which templates are available via MCP through the `mcpEnabled` configuration property on each GSH template. All security run types (`wheel`, `specifiedGroup`, `privilegeOnObject`, and `everyone`) are supported. The `mcpReadonly` flag determines whether readonly MCP users can see and execute the template, and the per-input `mcpScopeType` setting enables OAuth scope validation on non-readonly templates. See the admin guide for configuration details.

### ldap

Search LDAP directories configured as Grouper external systems. Use action `listExternalSystems` to discover available LDAP connections with their default base DN and documentation. Use action `filter` to execute an LDAP search with a filter string and return matching entries with their attributes. **Requires admin readonly access.**

#### Parameters

| Name | Type | Required | Description |
| --- | --- | --- | --- |
| `action` | string (enum) | Yes | `listExternalSystems` or `filter`. |
| `externalSystemId` | string | For `filter` | LDAP external system ID. Must match an `ldap.<id>.*` connection configured in Grouper. Use `listExternalSystems` to discover available IDs. |
| `baseDn` | string | No | Base DN for the LDAP search (e.g., `ou=people,dc=example,dc=edu`). Falls back to the default base DN configured for the external system. |
| `searchScope` | string (enum) | No | `OBJECT_SCOPE`, `ONELEVEL_SCOPE`, or `SUBTREE_SCOPE` (default). |
| `filter` | string | For `filter` | LDAP filter string (e.g., `(uid=jsmith)`, `(&(objectClass=person)(sn=Smith))`). |
| `attributes` | array of string | No | Attribute names to return (e.g., `["uid", "cn", "mail"]`). If not specified, returns all attributes. |

#### Response (`listExternalSystems`)

Returns a JSON object with an `externalSystems` array. Each element has `id`, and optionally `baseDn` and `documentation`.

#### Response (`filter`)

Returns a JSON object with `entryCount`, `totalAttributeValues`, `truncated` (boolean), and an `entries` array. Each entry has `dn` (the distinguished name) and `attributes` (an object mapping attribute names to arrays of string values).

#### Limits

- Max 2,500 entries per search.
- Max 10,000 total attribute values across all entries. If exceeded, the response is truncated and `truncated` is set to `true`.
- Max 1,000,000 characters in the response text.

### memberships_get

Get memberships and privileges from Grouper. Returns membership details including start date (`startTime`), end date (`endTime`), membership type, and list name. Can query by group names, subject IDs/identifiers, stem names, and/or attribute definition names. Supports point-in-time queries for historical membership data. Delegates to the existing Grouper WS `getMemberships` operation. Requires membership in the MCP readonly or readwrite group.

#### Parameters

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `groupNames` | array of strings | No | Array of fully qualified group names to query memberships for. |
| `subjectIds` | array of strings | No | Array of subject IDs to query memberships for. |
| `subjectIdentifiers` | array of strings | No | Array of subject identifiers (e.g., usernames) to query memberships for. |
| `subjectSourceIds` | array of strings | No | Array of source IDs to restrict subject lookups to specific sources. |
| `stemNames` | array of strings | No | Array of fully qualified stem (folder) names to query privileges for. |
| `attributeDefNames` | array of strings | No | Array of attribute definition names to query privileges for. |
| `memberFilter` | string (enum) | No | Membership filter type. One of: `All` (default), `Immediate`, `Effective`, `Composite`, `NonImmediate`. |
| `privilegeListName` | string (enum) | No | Privilege list name to query instead of membership. One of: `admins`, `updaters`, `readers`, `viewers`, `optins`, `optouts`, `groupAttrReaders`, `groupAttrUpdaters`. If omitted, returns the standard membership list. |
| `scopeStemName` | string | No | Stem name to limit results to memberships within a specific stem (folder). Used with `scopeType`. |
| `scopeType` | string (enum) | No | How deep under `scopeStemName` to search. `ONE_LEVEL` = immediate children only, `ALL_IN_SUBTREE` = all descendants (default). |
| `enabled` | string (enum) | No | Filter by enabled status. `T` = enabled only (default), `F` = disabled only, `A` = all. |
| `pointInTimeFrom` | string | No | Start of point-in-time query range, in format `yyyy/MM/dd HH:mm:ss.SSS`. Used to query historical membership data. |
| `pointInTimeTo` | string | No | End of point-in-time query range, in format `yyyy/MM/dd HH:mm:ss.SSS`. Used to query historical membership data. |
| `pageSize` | integer | No | Number of memberships to return per page. Defaults to 50. |
| `pageNumber` | integer | No | Page number to return (1-based). Defaults to 1. |

At least one of `groupNames`, `subjectIds`, `subjectIdentifiers`, `stemNames`, or `attributeDefNames` is required.

#### Response

Returns a JSON object with `totalMemberships` (count) and a `memberships` array. Each membership includes `membershipId`, `groupName` (or `ownerStemName` or `ownerAttributeDefName`), `subjectId`, `subjectSourceId`, `listName`, `listType`, `membershipType`, `enabled`, `startTime` (start date), `endTime` (end date), and `createTime`.

### privilege_assign

Assigns or revokes a privilege on a group or stem for a subject. Delegates to the existing Grouper WS `privilege_assign` operation. Requires membership in the MCP readwrite group. For OAuth users, the `readwrite` consent scope must also be granted. Note: system groups and stems under the Grouper built-in objects stem (default `etc`) are protected and cannot be modified via MCP.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `subjectId` | string | One of two* | The subject to grant/revoke privilege for. Mutually exclusive with `subjectIdentifier`. |
| `subjectIdentifier` | string | One of two* | Subject identifier. Mutually exclusive with `subjectId`. |
| `subjectSourceId` | string | No | Subject source ID to restrict the subject lookup. |
| `groupName` | string | One of two** | Group to assign privilege on. Mutually exclusive with `stemName`. |
| `stemName` | string | One of two** | Stem to assign privilege on. Mutually exclusive with `groupName`. |
| `privilegeType` | string | Yes | `access` for group privileges, `naming` for stem privileges. |
| `privilegeName` | string | Yes | The privilege name (e.g., `read`, `view`, `update`, `admin`, `optin`, `optout`, `groupAttrRead`, `groupAttrUpdate` for groups; `stem`, `create`, `stemAdmin`, `stemAttrRead`, `stemAttrUpdate` for stems). |
| `allowed` | boolean | Yes | `true` to grant the privilege, `false` to revoke it. |

* Must provide either `subjectId` or `subjectIdentifier`.  
** Must provide either `groupName` or `stemName`.

**Response:** Returns a JSON object with `resultCode` and `success` (boolean).

### privilege_get

Get privileges on a group or stem, optionally filtered by subject and/or privilege type. Delegates to the existing Grouper WS `privilege_get` operation. Requires membership in the MCP readonly or readwrite group. For OAuth users, the `readonly` (or `readwrite`) consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `subjectId` | string | No | Subject to check privileges for. Mutually exclusive with `subjectIdentifier`. |
| `subjectIdentifier` | string | No | Subject identifier. Mutually exclusive with `subjectId`. |
| `subjectSourceId` | string | No | Subject source ID to restrict the subject lookup. |
| `groupName` | string | One of two* | Group to check privileges on. Mutually exclusive with `stemName`. |
| `stemName` | string | One of two* | Stem to check privileges on. Mutually exclusive with `groupName`. |
| `privilegeType` | string (enum) | No | `access` for group privileges, `naming` for stem privileges. |
| `privilegeName` | string | No | Specific privilege name to filter by. |

* At least one of `groupName` or `stemName` must be provided.

**Response:** Returns a JSON array of privilege results, each with `privilegeName`, `privilegeType`, `allowed`, `subjectId`, `subjectName`, and either `groupName` or `stemName`.

### sql_get_schema

Get database schema information. Supports three actions for discovering databases, listing tables, and getting detailed table information. Can query the Grouper database or other configured external database connections. Requires membership in the MCP SQL readonly group. For OAuth users, the `sql_readonly` consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `action` | string (enum) | Yes | The action to perform: `listExternalSystems` – returns available database connections; `listTables` – returns table/view names for an external system; `tableInfo` – returns DDL or column metadata for a specific table. |
| `externalSystemId` | string | No (default: `"grouper"`) | External system ID identifying which database to query. Defaults to `"grouper"` (the Grouper database). Use `listExternalSystems` to discover available IDs. |
| `tableName` | string | Required for `tableInfo` | Table or view name to get info for. Case-insensitive. |

**Response (listExternalSystems):** Returns a JSON object with an `externalSystems` array. Each entry has `id` (the external system ID to use in other calls), `isGrouperDb` (boolean), and optional `documentation` (administrator-provided description of the database).

**Response (listTables):** Returns a JSON object with `tableCount`, `viewCount`, a `tables` array of table names, and a `views` array of view names. For the Grouper database, includes both built-in tables and any extras configured by the administrator. For other external systems, includes only administrator-configured tables.

**Response (tableInfo, standard Grouper table):** Returns the full CREATE TABLE or CREATE VIEW DDL statement for the specified table or view.

**Response (tableInfo, extra/external table):** Returns a JSON object with `name`, `type` (TABLE or VIEW), `comment` (table/view comment from the database, if available), `source` (`database_metadata`), and a `columns` array. Each column has `name`, `type`, `size`, `nullable`, and `comment` (column comment from the database, if available).

Returns an error if the table or view is not found, or if the external system is not configured.

### sql_select

Execute a read-only SQL SELECT query against the Grouper database (or another configured external system) and return the results as a JSON array of row objects. Only SELECT statements are allowed; INSERT, UPDATE, DELETE, and DDL statements are rejected. Results are paged — use `pageSize` and `pageNumber` to navigate through large result sets. An `ORDER BY` clause is required when paging beyond page 1 to ensure deterministic results. Set `countOnly` to `true` to return just the row count without fetching data. The query runs on a read-only JDBC connection for defense-in-depth. Use `sql_get_schema` with `action = 'listExternalSystems'` to discover available databases, `'listTables'` to see table/view names, and `'tableInfo'` to get column details. Requires membership in the MCP SQL readonly group. For OAuth users, the `sql_readonly` consent scope must also be granted.

| Parameter | Type | Required | Description |
| --- | --- | --- | --- |
| `sql` | string | Yes | The SQL SELECT query to execute. Must be a SELECT statement. Do not include a trailing semicolon. Do not include LIMIT or OFFSET clauses; use the `pageSize` and `pageNumber` parameters instead. |
| `countOnly` | boolean | No (default: false) | If `true`, return only the row count without fetching data. Useful for checking result size before fetching. When `true`, `pageSize` and `pageNumber` are ignored. |
| `pageSize` | integer | No (default: 500) | Number of rows per page (max 5000). Use `countOnly` first to check total rows if the query may return many rows. Ignored when `countOnly` is `true`. |
| `pageNumber` | integer | No (default: 1) | Page number, 1-based. Use with `pageSize` to page through large result sets. The SQL query must include an `ORDER BY` clause when using `pageNumber` > 1 to ensure deterministic results across pages. Ignored when `countOnly` is `true`. |
| `externalSystemId` | string | No (default: `"grouper"`) | External system ID identifying which database to query. Defaults to the Grouper database. The external system must be configured by the administrator. Use `sql_get_schema` with `action = 'listExternalSystems'` to see which external systems are available. |

**Response (normal):** Returns a JSON object with `rowCount` (rows returned on this page), `pageNumber`, `pageSize`, and a `rows` array. Each row is an object with column names as keys and string values. If the response exceeds 1,000,000 characters it is truncated with a message suggesting a more specific query or smaller page size.

**Response (countOnly):** Returns a JSON object with a single `count` field containing the number of rows.

Note: Additional MCP tools may be added over time to expose more Grouper operations.

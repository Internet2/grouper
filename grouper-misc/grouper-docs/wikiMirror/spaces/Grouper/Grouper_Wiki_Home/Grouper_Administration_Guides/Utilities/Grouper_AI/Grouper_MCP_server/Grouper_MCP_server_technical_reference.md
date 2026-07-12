---
title: "Grouper MCP server - technical reference"
space: Grouper
pageId: 28554339
version: 8
lastUpdated: 2026-07-12T15:27:07.547Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554339/Grouper+MCP+server+-+technical+reference
---

## Introduction

This document covers the architecture, protocol details, and security model of the Grouper MCP server. It is intended for developers, integrators, and anyone who needs to understand how the MCP server works at a technical level.

For a high-level introduction to the Grouper MCP server, see the [overview](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547487/Grouper+MCP+server). For end-user setup instructions and tool documentation, see the [user guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554356/Grouper+MCP+server+-+user+guide). For enabling, configuration, and authorization group management, see the [administrator guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide).

## Architecture

The MCP implementation spans two Grouper modules:

### Web Services module (grouper-ws)

- **MCP protocol servlet** – Handles JSON-RPC 2.0 messages (initialize, tools/list, tools/call, ping) over HTTP POST. Supports authentication via OAuth JWT bearer tokens or normal WS authentication (HTTP Basic / container auth).
- **OAuth token servlet** – Exchanges authorization codes for JWT access tokens (with PKCE verification). Also handles dynamic client registration (RFC 7591).
- **Well-known metadata servlet** – Serves OAuth 2.0 Authorization Server Metadata (RFC 8414) so MCP clients can auto-discover endpoints.
- **Protected resource metadata servlet** – Serves OAuth 2.0 Protected Resource Metadata (RFC 9728) so MCP clients know which authorization server protects the MCP endpoint.
- **MCP tool handlers** – Individual classes implementing each MCP tool (e.g., `entity_get`).

### UI module (grouper-ui)

- **Authorization endpoint** – The OAuth 2.1 authorization endpoint where users authenticate (via the institution's existing auth mechanism) and see a consent page to approve or deny the MCP client's access request.
- **Consent page (JSP)** – Displays the requesting application name, the logged-in user, and permission scope checkboxes. Only scopes for which the user is a member of the corresponding authorization group are shown (read-only, read-write, SQL read-only). Read-write group membership also shows the read-only scope, and admin read-write group membership also shows the admin read-only scope. The user selects the desired scopes and can approve or deny. When the read-write scope is selected, three text areas are shown requiring the user to specify at least one of: folder paths, group paths, or subject IDs/identifiers. Categories left blank are fully restricted (e.g., if only subjects are specified, no groups can be modified). These scope restrictions are validated via an AJAX request (checking folder/group existence and total group counts) before the form is submitted. The selected scopes and scope restrictions are stored as a JSON consent_details field in the authorization code and flow through to the JWT access token as claims.

### Core module (grouper)

- **OAuth data model** – Hibernate-mapped entities for OAuth clients, authorization codes, pending authorization requests, and the JWT signing key pair.
- **OAuth store** – DAO layer for persisting and retrieving all OAuth state. All data is stored in the shared Grouper database, so the flow works correctly in load-balanced multi-container deployments.

## Endpoints

All paths below are relative to the Grouper WS context path (e.g., `/grouper-ws`).

| Endpoint | Method | Purpose | Authentication |
| --- | --- | --- | --- |
| `/mcp` | POST | Main MCP protocol endpoint (JSON-RPC 2.0 messages) | Bearer JWT token or HTTP Basic / container auth (required) |
| `/mcp` | DELETE | Session termination | None |
| `/mcp/oauth/register` | POST | Dynamic client registration (RFC 7591) | None (anonymous, rate-limited) |
| `/mcp/oauth/token` | POST | Token exchange (authorization code + PKCE verifier for JWT) | None (public client + PKCE) |
| `/.well-known/oauth-authorization-server` | GET | Authorization Server Metadata (RFC 8414) | None |
| `/.well-known/openid-configuration` | GET | OIDC Discovery fallback (same servlet, adds OIDC-specific fields) | None |
| `/.well-known/oauth-protected-resource` | GET | Protected Resource Metadata (RFC 9728) | None |

Additionally, the OAuth token and registration endpoints are mapped at the context root as RFC 8414 fallback paths for MCP clients that construct endpoint URLs differently:

- `/register` (POST) – Same as `/mcp/oauth/register`
- `/token` (POST) – Same as `/mcp/oauth/token`

The **authorization endpoint** is served by the Grouper UI module (not the WS module):

| Endpoint | Method | Purpose |
| --- | --- | --- |
| `/grouperUi/app/UiV2OAuth.authorize` | GET | OAuth authorization & consent page (user-facing) |
| `/grouperUi/app/UiV2OAuth.submitAuthorize` | POST | Consent form submission (approve/deny) |

Note on filters: The `/mcp/*` path is behind the standard Grouper WS logging filter and service filter (for request logging and Grouper session setup). The OAuth endpoints (`/mcp/oauth/*`, `/register`, `/token`) and the well-known metadata endpoints are **not** behind the WS authentication filter — they handle their own authentication or are public discovery endpoints.

### MCP protocol details

The MCP server supports the following JSON-RPC 2.0 methods:

| Method | Description |
| --- | --- |
| `initialize` | Creates a new MCP session. Returns protocol version, server info, capabilities, and optional `instructions` for the AI client. The `instructions` field contains guidance text configured via the `grouper.mcp.instructions` property (see Admin Guide > MCP settings). The server responds with an `Mcp-Session-Id` header. |
| `notifications/initialized` | Client acknowledgment after initialization (returns 202 Accepted). |
| `tools/list` | Returns the list of available tools with their input schemas. |
| `tools/call` | Invokes a tool by name with the provided arguments. |
| `ping` | Keep-alive / health check (returns empty result). |

## OAuth 2.1 authorization flow

The Grouper MCP server uses the **OAuth 2.1 Authorization Code flow with PKCE**. This is the flow required by the MCP specification. The entire flow is initiated automatically by the MCP client; the user only needs to interact with the consent page in their browser.

### Step-by-step flow

1. **Dynamic client registration:** The MCP client registers itself by POSTing to `/mcp/oauth/register` with its `redirect_uris` (and optionally a `client_name`). Registration is anonymous (no authentication required) and rate-limited. Redirect URIs must match the configured allowlist patterns. Grouper returns a `client_id`. This step only needs to happen once per client.
2. **PKCE challenge generation:** The MCP client generates a random `code_verifier` (a high-entropy string) and computes `code_challenge = BASE64URL(SHA-256(code_verifier))`.
3. **Authorization request:** The MCP client opens the user's browser to the Grouper UI authorization endpoint with the following parameters:`GET /grouperUi/app/UiV2OAuth.authorize ?client_id=... &redirect_uri=... &response_type=code &code_challenge=... &code_challenge_method=S256 &state=... &scope=...`
4. **User authentication:** The user authenticates through the institution's existing authentication mechanism (Shibboleth, CAS, etc.) that protects the Grouper UI. There is no separate MCP-specific login — it uses the same authentication infrastructure as the rest of the Grouper UI.
5. **Consent page:** Grouper displays a consent page showing the requesting application name, the logged-in user, and checkboxes for the permission scopes the user is eligible to grant. Only scopes for which the user is a member of the corresponding authorization group are shown. Read-write group membership also shows the read-only scope, and admin read-write also shows admin read-only. The user selects the desired scopes and clicks **Approve** or **Deny**.
6. **Authorization code redirect:** If approved, Grouper generates a one-time authorization code, stores it in the database, and redirects the user's browser back to the MCP client's `redirect_uri` with the `code` and `state` parameters.
7. **Token exchange:** The MCP client sends the authorization code, `code_verifier`, `client_id`, and `redirect_uri` to the token endpoint:`POST /mcp/oauth/token Content-Type: application/x-www-form-urlencoded grant_type=authorization_code &code=... &client_id=... &code_verifier=... &redirect_uri=...`Grouper verifies the PKCE challenge, validates the authorization code, and returns a signed JWT access token.
8. **Authenticated MCP requests:** The MCP client includes the JWT in the `Authorization: Bearer <token>` header on all subsequent requests to the `/mcp` endpoint.

### Denial flow

If the user denies the request on the consent page, Grouper redirects to the MCP client's `redirect_uri` with `error=access_denied` and an appropriate `error_description`.

## Endpoint discovery (well-known metadata)

MCP clients use a multi-step discovery process to find the authorization, token, and registration endpoints. This follows the standards in the MCP specification.

### Step 1: protected resource metadata (RFC 9728)

When an MCP client first connects to `/mcp` without a token, it receives a `401 Unauthorized` response with a `WWW-Authenticate` header:

`WWW-Authenticate: Bearer resource_metadata="https://grouper.example.edu/grouper-ws/.well-known/oauth-protected-resource"`The client fetches that URL and receives metadata identifying the MCP resource and which authorization server protects it:

`{ "resource": "https://grouper.example.edu/grouper-ws/mcp", "authorization_servers": ["https://grouper.example.edu/grouper-ws/mcp"], "bearer_methods_supported": ["header"] }`

### Step 2: authorization server metadata (RFC 8414)

The client then fetches `/.well-known/oauth-authorization-server` to discover the OAuth endpoints:

`{ "issuer": "https://grouper.example.edu/grouper-ws", "authorization_endpoint": "https://grouper.example.edu/grouper/grouperUi/app/UiV2OAuth.authorize", "token_endpoint": "https://grouper.example.edu/grouper-ws/mcp/oauth/token", "registration_endpoint": "https://grouper.example.edu/grouper-ws/mcp/oauth/register", "response_types_supported": ["code"], "grant_types_supported": ["authorization_code"], "token_endpoint_auth_methods_supported": ["none"], "code_challenge_methods_supported": ["S256"] }`The `/.well-known/openid-configuration` path is also supported as a fallback. When requested via that path, the response additionally includes OIDC Discovery fields (`jwks_uri`, `subject_types_supported`, `id_token_signing_alg_values_supported`) for clients that attempt OIDC-style discovery.

## Security model

The Grouper MCP server is designed with a defense-in-depth security approach, leveraging well-established standards and multiple layers of protection.

For registration security and authorization group configuration, see the [registration security](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide) and [authorization groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide) sections of the [administrator guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide).

### OAuth 2.1 with PKCE

- **PKCE is mandatory.** Every authorization request must include a `code_challenge` using the S256 method. The plain method is not supported. This prevents authorization code interception attacks even if an attacker can observe the authorization code in transit.
- **Public client model.** The token endpoint authentication method is `none`, meaning there is no client secret required at the token endpoint. Security relies entirely on PKCE (the `code_verifier` is never transmitted over the authorization channel and must be presented at the token endpoint).
- **Dynamic client registration** follows RFC 7591 and allows MCP clients to register and receive a `client_id`. Registration is anonymous and rate-limited. Redirect URIs are validated against the configured allowlist at both registration and authorization time. See the [registration security](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide) section of the administrator guide for details.

### JWT access tokens

- **RS256 (RSA with SHA-256) signatures.** Access tokens are signed using an RSA 2048-bit key pair. The private key is stored in the Grouper database configuration and is automatically encrypted by Grouper's Morph encryption framework.
- **Token claims** include the Grouper subject ID, subject source ID, OAuth client ID, issuer, issued-at time, expiration, a unique JWT ID (jti), and granted scope claims (`grouper_readonly`, `grouper_readwrite`, `grouper_sql_readonly`, `grouper_admin_readonly`, `grouper_admin_readwrite`) based on the user's consent selections. When the read-write scope is granted with scope restrictions, additional list claims are included: `grouper_readwrite_folders` (JSON array of folder paths), `grouper_readwrite_groups` (JSON array of group paths), and `grouper_readwrite_subjects` (JSON array of subject IDs/identifiers). These are enforced on every read-write tool call.
- **Configurable expiration.** Access tokens expire after 1 hour by default (`grouper.oauth.accessToken.expirationSeconds`).
- **Signature verification on every request.** The MCP servlet verifies the JWT signature and expiration on every incoming request before processing any MCP operations.
- **Key pair auto-generation.** If no signing key pair exists, Grouper automatically generates one on first use and persists it in the database-backed configuration. This handles multi-container deployments seamlessly — all containers share the same key pair through the shared database.

### Authorization code security

- **One-time use.** Authorization codes are deleted from the database immediately after token exchange. If an attacker attempts to reuse a code, it will be rejected and the code is removed entirely.
- **Short expiration.** Authorization codes expire after 10 minutes by default (`grouper.oauth.authorizationCode.expirationSeconds`).
- **Client binding.** The `client_id` and `redirect_uri` on the token request are validated against the values stored with the authorization code.
- **Redirect URI whitelist.** At authorization time, the requested `redirect_uri` is validated against the set of URIs registered with the client. Unregistered URIs are rejected and the user is shown an error page (to prevent open redirect attacks).

### User authentication and consent

- **Institutional authentication.** Users authenticate through the same mechanism that protects the Grouper UI (Shibboleth, CAS, SAML, etc.). No separate password or credential is introduced by MCP.
- **Explicit consent with granular scopes.** Users must explicitly approve each MCP client's access request on a consent page that shows the requesting application name and the logged-in user. The consent page displays checkboxes for the permission scopes the user is eligible to grant (based on their authorization group memberships): read-only operations, read-write operations, SQL read-only queries, admin read-only, and admin read-write. When the read-write scope is selected, the user is required to specify at least one scope restriction: folder paths (limiting which folders the client can modify groups in), group paths (limiting which specific groups can be modified), or subject IDs/identifiers (limiting which subjects can be managed). Categories left blank are fully restricted — for example, if only subject IDs are specified, the client cannot modify any group. These are validated via AJAX before submission (checking existence of folders/groups and that total groups in specified folders is < 500). If the user is not a member of any MCP authorization group, the consent page returns an `access_denied` error. The granted scopes and scope restrictions are stored as a JSON object in the authorization code and included as claims in the JWT access token.
- **CSRF protection.** The consent form includes a CSRF token (via the OWASP CSRFGuard integration in the Grouper UI) to prevent cross-site request forgery attacks on the approve/deny action.

### Normal WS authentication (alternative)

As an alternative to the OAuth flow, the MCP server also accepts standard Grouper WS authentication. This is useful for server-to-server integrations, automated scripts, or environments where the OAuth flow is not practical.

- **HTTP Basic auth** – When `[grouper.is.ws](http://grouper.is.ws).basicAuthn = true` in `grouper.hibernate.properties`, the MCP endpoint accepts standard HTTP Basic authentication (the same credentials used for other Grouper WS calls).
- **Container-managed auth** – If the servlet container (e.g., Tomcat valve, Apache module) has already authenticated the user, the MCP servlet will use `request.getRemoteUser()` or `request.getUserPrincipal()`.
- **Custom authentication class** – If a custom authentication class is configured via `ws.security.non-rampart.authentication.class`, the MCP servlet will use it as a fallback.
- **Subject resolution** – The user ID from normal WS auth is resolved to a Grouper Subject using the same logic as other WS operations (including the `[ws.logged.in](http://ws.logged.in).subject.default.source` configuration).
- **Same authorization groups** – Regardless of authentication method, the user must still be a member of the appropriate MCP authorization group (`grouper.mcp.users.readonly` or `grouper.mcp.users.readwrite`) to access MCP tools. See the [authorization groups](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide) section of the administrator guide for details.

**OAuth is recommended over WS authentication for interactive use.** With OAuth, the user explicitly chooses which permission scopes to grant on the consent page, and these consent scopes are enforced on every MCP request *in addition to* group membership. For example, a user who is a member of both the read-only and read-write groups can choose to grant only read-only access to a particular MCP client. With WS authentication there is no consent flow, so authorization is based solely on group membership — a user in the read-write group automatically has read-write access with no opportunity to restrict it per client.

### Session management

- MCP sessions are identified by a server-generated `Mcp-Session-Id` header, created during the `initialize` handshake.
- All requests after initialization must include a valid session ID or they are rejected.
- Sessions can be explicitly terminated via HTTP DELETE.

### Transport security

- Token responses include `Cache-Control: no-store` and `Pragma: no-cache` headers to prevent tokens from being cached by proxies or browsers.
- Production deployments should always use HTTPS to protect tokens in transit.

## Database tables

The MCP implementation adds four new tables to the Grouper database schema: three for OAuth and one for audit logging.

### grouper_oauth_client

Stores registered OAuth clients (created via dynamic client registration).

| Column | Type | Description |
| --- | --- | --- |
| internal_id | BIGINT | Primary key (auto-incrementing via TableIndex) |
| client_id | VARCHAR(255) | Unique OAuth client identifier |
| client_name | VARCHAR(255) | Optional display name for consent page |
| redirect_uris | VARCHAR(4000) | Comma-separated registered redirect URIs |
| client_secret | VARCHAR(4000) | Auto-encrypted client secret. Used by confidential clients (e.g. server-side applications like LibreChat) that authenticate with client_secret_post. Public clients (e.g. Claude Code) do not have a secret and use PKCE instead. |
| registered_micros | BIGINT | Registration timestamp (micros since 1970) |
| member_internal_id | BIGINT | Member internal ID of the first user who got an authorization code (nullable) |
| code_count | BIGINT | Number of authorization codes issued for this client |
| last_code_micros | BIGINT | Timestamp of last authorization code issuance (micros since 1970) |

### grouper_oauth_code

Stores authorization codes during the OAuth flow. Codes are deleted after token exchange.

| Column | Type | Description |
| --- | --- | --- |
| internal_id | BIGINT | Primary key (auto-incrementing via TableIndex) |
| code | VARCHAR(255) | Unique authorization code |
| oauth_client_internal_id | BIGINT | Internal ID from grouper_oauth_client (soft link, not a FK) |
| redirect_uri | VARCHAR(4000) | Redirect URI from the authorization request |
| code_challenge | VARCHAR(255) | PKCE code challenge (S256) |
| code_challenge_method | VARCHAR(10) | PKCE method (always "S256") |
| member_internal_id | BIGINT | Member internal ID of the user who approved |
| is_used | VARCHAR(1) | Replay detection flag ("T" or "F") |
| created_micros | BIGINT | Creation timestamp (micros since 1970) |
| expires_micros | BIGINT | Expiration timestamp (micros since 1970) |
| consent_details | VARCHAR(4000) | JSON object with granted scopes (e.g., `{"readonly":true,"readwrite":false,"sqlReadonly":false}`) |

### grouper_oauth_pend_authz_req

Stores pending authorization requests between when the user lands on the consent page and when they approve or deny. Cleaned up after consent action or on expiration.

| Column | Type | Description |
| --- | --- | --- |
| internal_id | BIGINT | Primary key (auto-incrementing via TableIndex) |
| request_id | VARCHAR(255) | Public-facing request ID (used in the consent form) |
| oauth_client_internal_id | BIGINT | Internal ID from grouper_oauth_client (soft link, not a FK) |
| redirect_uri | VARCHAR(4000) | Redirect URI for this request |
| code_challenge | VARCHAR(255) | PKCE code challenge |
| code_challenge_method | VARCHAR(10) | PKCE method |
| state | VARCHAR(4000) | OAuth state parameter (opaque to server) |
| scope | VARCHAR(4000) | Requested scope |
| created_micros | BIGINT | Creation timestamp (micros since 1970) |
| expires_micros | BIGINT | Expiration timestamp (micros since 1970) |

### grouper_mcp_tool_log

Audit log of all MCP tool invocations. Every tool call (successful or failed) is recorded here. Old rows are cleaned up automatically by the `MAINTENANCE__cleanLogs` daemon job based on the `grouper.mcp.toolLog.retentionDays` configuration property (default 30 days).

| Column | Type | Description |
| --- | --- | --- |
| internal_id | BIGINT | Primary key (auto-incrementing via TableIndex) |
| oauth_client_internal_id | BIGINT | Internal ID of the OAuth client (nullable; null for WS-auth calls) |
| subject_id | VARCHAR(255) | Subject ID of the authenticated user |
| subject_source_id | VARCHAR(255) | Subject source ID of the authenticated user |
| tool_name | VARCHAR(255) | Name of the MCP tool invoked (e.g., `group_find`) |
| tool_category | VARCHAR(64) | Tool category for authorization (e.g., `readonly`, `readwrite`, `sqlReadonly`) |
| request | VARCHAR(4000) | JSON snippet of the tool request parameters (truncated if needed) |
| response_or_error | VARCHAR(4000) | JSON snippet of the response or error message (truncated if needed) |
| is_error | VARCHAR(1) | Whether the call resulted in an error ("T" or "F") |
| started_micros | BIGINT | Timestamp when the tool call started (micros since 1970) |
| duration_micros | BIGINT | Duration of the tool call in microseconds |

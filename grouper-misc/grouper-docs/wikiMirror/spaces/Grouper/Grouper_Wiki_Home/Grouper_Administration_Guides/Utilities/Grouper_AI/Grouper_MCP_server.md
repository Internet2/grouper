---
title: "Grouper MCP server"
space: Grouper
pageId: 28547487
version: 24
lastUpdated: 2026-07-12T15:26:46.316Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547487/Grouper+MCP+server
---

## Examples

- [Configuring MCP example](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554803/Grouper+MCP+configuration+example)
- [Daemon error bug](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554301/Grouper+MCP+example+daemon+error+bug)
- [Testing provisioning full and incremental](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554381/Grouper+MCP+example+testing+provisioning)
- [Troubleshooting daemon issues](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554319/Grouper+MCP+example+troubleshooting+daemon+issues)
- [GSH template and rules ticket](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554602/Grouper+MCP+example+GSH+template)

## Related documentation

- [Grouper MCP server - user guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554356/Grouper+MCP+server+-+user+guide) – Client setup, UI info page, and full tool documentation
- [Grouper MCP server - administrator guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide) – Enabling MCP, configuration, authorization groups, and multi-container deployments
- [Grouper MCP server - technical reference](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554339/Grouper+MCP+server+-+technical+reference) – Architecture, endpoints, OAuth 2.1 flow, security model, and database tables

## Overview

Starting with Grouper 7.0.0, Grouper includes a built-in **Model Context Protocol (MCP)** server. MCP is an open standard that allows AI assistants and other tools (referred to as "MCP clients") to interact with external systems through a well-defined protocol. The Grouper MCP server enables AI tools such as Claude, Cursor, VS Code Copilot, and others to query and interact with Grouper programmatically using natural-language-driven workflows.

The MCP server is part of the Grouper Web Services (WS) module. It exposes Grouper operations as "tools" that MCP clients can discover and invoke. The Grouper deployment controls who can access MCP and what they can do through authorization groups.

Two authentication mechanisms are supported:

1. **OAuth 2.1 with PKCE** (recommended for interactive AI use) – Users authenticate through the Grouper UI using the institution's existing authentication mechanism (Shibboleth, CAS, etc.) and approve access on a consent screen. No credentials are stored locally on the user's machine. With OAuth, users explicitly choose which permission scopes to grant (read-only, read-write, SQL read-only) on the consent page, and these consent choices are enforced on every MCP request in addition to group membership. This provides an extra layer of user-driven access control.
2. **Normal WS authentication** (for automated or server-to-server use) – HTTP Basic auth or container-managed auth, using the same authentication mechanism as other Grouper WS endpoints. With WS authentication there is no consent flow, so authorization is based solely on group membership. This makes OAuth the preferred method for interactive use, since the consent step gives users control over what they grant to each MCP client.

The built-in MCP server does not allow destructive operations such as deleting stems. Administrators control what each user can do via MCP authorization groups, e.g. read-only, read-write, SQL read-only, etc. Additionally, system groups and stems (under the Grouper built-in objects stem, default `etc`) are protected from modification via MCP, even for readwrite users. All tool calls are audited in a database table for security and compliance, and configurable per-category rate limits protect the system from abuse.

## What can it do?

The MCP server exposes Grouper operations as tools that MCP clients can discover and invoke. Currently there are 25 tools available, covering group and stem management (finding, creating, and updating groups and stems), membership operations (adding, removing, checking members, and retrieving membership details with dates), privilege management (viewing and assigning privileges), subject lookup, attribute operations (finding and assigning attributes), audit log retrieval, SQL read-only operations (database schema discovery, SQL SELECT queries with optional count-only mode, and support for multiple configured database connections), LDAP directory searches (querying configured LDAP external systems with filters and attribute retrieval), and administrative tools (daemon job search, daemon log retrieval, daemon job message retrieval, daemon job triggering, and configuration search). Additional tools may be added over time. For the full list of tools with their parameters and response formats, see the [user guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554356/Grouper+MCP+server+-+user+guide).

## Getting started

Depending on your role, see the appropriate guide:

- **For users** who want to connect an AI tool to Grouper: see the [Grouper MCP server - user guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554356/Grouper+MCP+server+-+user+guide). It covers client setup, the UI info page, and full tool documentation.
- **For administrators** who want to enable and configure MCP on a Grouper deployment: see the [Grouper MCP server - administrator guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide). It covers enabling MCP, configuration properties, authorization groups, and multi-container deployments.
- **For developers and integrators** who want to understand the architecture, protocol details, or security model: see the [Grouper MCP server - technical reference](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554339/Grouper+MCP+server+-+technical+reference). It covers architecture, endpoints, the OAuth 2.1 flow, endpoint discovery, security model, and database tables.

## Remote MCP (built-in) vs local MCP

There are two approaches to providing MCP access to Grouper: **remote MCP** (the built-in server described in this page) and **local MCP** (a command-line wrapper that runs on the user's machine and calls Grouper web services). The built-in remote MCP server is strongly recommended. Local MCP solutions have security risks and operational drawbacks.

### Why remote MCP is recommended

- **Consolidated implementation.** With a single built-in MCP server, there is no need for separate community-maintained MCP implementations. Early community MCP projects (such as those from Unicon and the University of Chicago) paved the way and demonstrated the value of MCP for Grouper. The built-in server incorporates lessons from those efforts. There may still be use cases for local MCP solutions, but in general the built-in server is the preferred path forward.
- **Authentication through Grouper UI with OAuth.** For interactive AI use cases (the primary use case), remote MCP authenticates end users through the Grouper UI using OAuth 2.1, which means users sign in through their institution's existing authentication mechanism (Shibboleth, CAS, etc.) rather than entering WS credentials directly into an MCP client. This avoids having credentials stored locally on the user's machine. For automated or server-to-server use cases, normal WS authentication (HTTP Basic or container auth) is available as a fallback. However, OAuth should be used by end users for interactive AI interactions.
- **Granular authorization.** The built-in server provides fine-grained control over who can use MCP and what they can do. Most users cannot access MCP at all; some are granted read-only access; others may have read-write access for certain operations. For example, no one can delete stems via MCP. Administrators control this through Grouper authorization groups.
- **Version tracking.** The built-in MCP server exactly tracks the version of Grouper you are running. With a local MCP solution, each Grouper upgrade could require all MCP users to update their local installation, which is an operational burden.
- **Server-side-only tools.** The built-in MCP server can expose tools that do not correspond to any existing WS operation. For example, a tool that returns the non-secret configuration of a provisioner (so an AI can troubleshoot it), or a tool that queries recent daemon job logs from the database. These server-side-only tools are not possible with a local MCP wrapper that can only call existing WS endpoints.
- **Redirect URI control.** Grouper operators have control over the registered redirect URIs for OAuth clients, giving them a say in where and how MCP is used.
- **User consent per operation.** The OAuth consent flow allows users to approve or disapprove of operations (or sets of operations) depending on what they are doing, providing an additional layer of user-driven access control.

### When local MCP may still apply

There are a few scenarios where a local MCP solution might still be relevant:

- **MCP not enabled by the Grouper administrator.** If the maintainer of the Grouper installation has not enabled the built-in MCP server but web services are available, a local MCP wrapper could still function. However, in practice this may violate the institution's usage policy — if the administrator has not turned on MCP, they may not want MCP-style access to be used.
- **Older Grouper versions.** If the institution is running a version of Grouper that predates the built-in MCP server (prior to 7.0.0), a local MCP wrapper may be the only option until the upgrade is completed.
- **AI client only supports local MCP.** Some AI tools may only support local (stdio-based) MCP transports and not the remote Streamable HTTP transport. In this case a local wrapper may be necessary as a bridge.

**Security note:** Local MCP solutions carry security risks. They typically require storing WS credentials on the user's machine, bypass the institution's OAuth consent flow, and lack the granular authorization controls of the built-in server. Remote MCP is strongly recommended wherever possible.

## History

The built-in Grouper MCP server builds on innovations from the Grouper community:

- **Unicon** created the first local MCP implementation for Grouper, demonstrating the potential of integrating AI tools with Grouper through the Model Context Protocol.
- **University of Chicago** created another local MCP implementation and successfully deployed it for their help desk. They gave a well-received demo showcasing how AI-assisted Grouper management can streamline administrative workflows.

The built-in Grouper MCP server is based on their innovations, bringing MCP support directly into the Grouper platform with added security controls (OAuth 2.1, consent-based scopes, authorization groups) and eliminating the need for local installations.

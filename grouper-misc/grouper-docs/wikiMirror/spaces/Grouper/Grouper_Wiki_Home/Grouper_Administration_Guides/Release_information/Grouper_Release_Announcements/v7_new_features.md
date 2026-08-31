---
title: "v7 new features"
space: Grouper
pageId: 172097588
version: 1
lastUpdated: 2026-08-30T19:09:15.440Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/172097588/v7+new+features
---

## New Features in Grouper v7

 Grouper v7 includes many helpful new features. This list of features is what is different from v6.

 See also [Grouper 7 Release Notes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549113/v7+Release+Notes)

 See also [v6 new features](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549713/v6+new+features)

 

| **MCP server** | [Grouper MCP server](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547487/Grouper+MCP+server) - lets an AI assistant work with Grouper through the Model Context Protocol |
| --- | --- |
| **OAuth for MCP clients** | [Grouper MCP server - administrator guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554349/Grouper+MCP+server+-+administrator+guide) - Grouper acts as the OAuth authorization server for MCP clients |
| **Convert a group to a composite in place** | Convert a group that already has members into a composite without deleting and re-adding the memberships, so there is no change log or point in time churn |
| **Provisioning sync-back** | Mirror what the provisioner reads and writes into the generic provisioning tables, so a full sync can resolve users, groups, and memberships from the cache instead of re-reading the target |
| **Group level incremental loader** | An incremental group table so the loader can sync a single group's metadata or memberships without a full sync |
| **Upgrade task screen** | Shows upgrade tasks recorded in attribute assignments that the running code does not expect |
| **All report configs screen** | Admin screen listing every report configuration across all folders and groups |

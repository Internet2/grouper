---
title: "Grouper external system references"
space: Grouper
pageId: 65634312
version: 3
lastUpdated: 2026-07-19T22:36:05.709Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/65634312/Grouper+external+system+references
---

This is for Grouper v7.3.2+, v6.3.1+, and v4.24.1+.

## Overview

On the external system view details screen (Miscellaneous > External systems > Actions > View details), a References section is shown above the configuration. It lists where in Grouper the external system is used, grouped by type, so you can see the impact before you edit or delete it.

Each row shows a type, a reference (a link when the target has its own view page, otherwise plain text), and a short description. If a single type has more than 100 references, only the first 100 are shown, with a note that the list was truncated.

## What is detected

- Provisioners (LDAP, SQL, and every other provisioner that selects an external system)
- SQL and attribute group loaders (the loader database connection)
- Reports (the report SQL connection)
- Subject sources (SQL `jdbcConfigId` or LDAP `ldapServerId`)
- Custom UI and GSH template SQL dropdown inputs
- Daemon jobs such as LDAP-to-SQL and SQL-to-Grouper sync
- MCP admin external system exposure (the `admin_external_system_get` tool)

## How it works

References are found by three complementary scans, whose results are combined:

- **Config editor references**: config keys whose metadata option list is driven by this external system's class (`optionValuesFromClass`). Real config keys are matched by structure against the metadata sample keys, so any config id resolves, not just the example ones.
- **Attribute references**: group and attribute loaders and report configs store the connection as an attribute value on the object rather than in config, so these are looked up by attribute.
- **MCP**: the MCP admin external system names the external system in the config key rather than in a value, so it is found by key.

## Notes and limitations

- The list is a best-effort detection and might not be complete. References defined only in flat config files (rather than in the database config), and any reference channel that is not yet enumerated, are not guaranteed to appear.
- Disabled configurations are included, since deleting the external system would still affect them.
- Blank or defaulted values are not treated as references, so the built-in `grouper` database connection does not list every loader and report that simply omitted the field.

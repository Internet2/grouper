---
title: "Grouper database standards"
space: GrIntDev
pageId: 48792786
version: 3
lastUpdated: 2026-07-12T06:45:42.294Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792786/Grouper+database+standards
---

When working with the Grouper database, keep this in mind

1. Do not create an object that starts with "grouper". That is reserved for the Grouper API.
2. You should not grant access to Grouper objects because they might be recreated. i.e. instead of granting SELECT to grouper_memberships_lw_v you should make a view that selects from that view and grant to the parent view. There could be performance implications there. Also views can get deleted cascade when underlying views get dropped and recreated.
3. Running the Grouper SQL Database Provisioner is a safe way to keep another copy of the data (even in the same database) and those structures will never be touched, and might have better performance (no joins in a sync'ed table)

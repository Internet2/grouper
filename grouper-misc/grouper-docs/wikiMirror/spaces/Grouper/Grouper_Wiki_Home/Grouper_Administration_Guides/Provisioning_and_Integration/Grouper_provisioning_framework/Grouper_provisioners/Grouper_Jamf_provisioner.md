---
title: "Grouper Jamf provisioner"
space: Grouper
pageId: 110985217
version: 1
lastUpdated: 2026-08-06T14:49:13.298Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/110985217/Grouper+Jamf+provisioner
---

See also: [Jamf external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/110952449/Jamf+external+system) and [developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/107380739/Grouper+Jamf+provisioner+developer+notes).

Provisions Grouper group memberships into Jamf Pro admin **accounts** and account **groups** (roles) via the Jamf Pro Classic API (XML). Provisioning type is `membershipObjects`.

## Model

In Jamf, privileges live on an account group (a "role"); an account gains those privileges by being a member of the role. That maps directly onto Grouper:

| Grouper | Jamf | What Grouper does |
| --- | --- | --- |
| target group | account group (role) | **read only** -- resolve by name; never create, update, delete, or rename. Jamf admins own the role and its privilege set. |
| target entity | admin account | **create only** -- created (Group Access, random password) if missing so it can be added to a role; never updated or deleted. |
| target membership | account in a role's members | **full control** -- added on join, removed on leave. The account itself is left in place on removal. |

Accounts are matched and created by `name` = lowercased EPPN (`pennkey@upenn.edu`). Because the Classic API has no atomic add/remove for account groups, membership is applied by retrieve-modify-write of the whole member list; only `<name>`/`<members>` are sent, so a membership change never rewrites a role's privileges.

## Configuration properties

| Property | Required? | Description |
| --- | --- | --- |
| class | Yes | `edu.internet2.middleware.grouper.app.jamf.JamfProvisioner` |
| jamfExternalSystemConfigId | Yes | The WsBearerToken external system id (Jamf base URL + OAuth client credentials). |
| jamfNewAccountAccessLevel | No | Access level stamped on accounts Grouper creates. Defaults to `Group Access` (privileges come from role membership). |
| jamfIgnoreAccountNames | No | Comma-separated account names (EPPNs) never created, added, or removed. Protect break-glass / service admins here. |
| jamfIgnoreRoleNames | No | Comma-separated role names filtered from retrieve; their membership is never touched. |

Sync-back is supported: the `fullSyncUsersFromSyncBack`, `fullSyncMembershipsFromSyncBack`, and `fullSyncGroupsFromSyncBack` options let a full sync reconstruct from the native mirror. Membership is the main win -- role membership otherwise costs one detail call per role.

## Target attributes

**Group (role)**: `id`, `name` (link key), `accessLevel`, `privilegeSet`, `site`. Match by `name`.

**Entity (account)**: `id` (target-assigned on create), `name` = EPPN (match key), `fullName`, `email`, `accessLevel`.

## CRUD operations

| Operation | Supported | API call |
| --- | --- | --- |
| retrieve roles | Yes | GET /JSSResource/accounts, GET /accounts/groupid/{id} |
| insert / update / delete role | No | roles are read-only |
| retrieve account | Yes | GET /accounts/username/{name} |
| insert account | Yes | POST /accounts/userid/0 (XML) |
| update / delete account | No | accounts are create-only |
| add / remove membership | Yes | PUT /accounts/groupid/{id} (full member list) |

## Behavioral notes

- A configured role must already exist in Jamf; if the role name cannot be resolved, provisioning that group errors rather than creating a role.
- Grouper only manages accounts it creates and role memberships it owns; it never modifies directory-linked accounts or accounts/roles on the ignore lists.
- On removal from a Grouper group, the account is removed from the role only -- the account is not deleted.

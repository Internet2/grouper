---
title: "Grouper Interfolio provisioner"
space: Grouper
pageId: 28555855
version: 4
lastUpdated: 2026-07-12T15:27:15.475Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555855/Grouper+Interfolio+provisioner
---

## What it does

The Interfolio provisioner keeps users in [Interfolio](https://www.interfolio.com/) (the faculty information system) in sync with a Grouper group's membership. When a subject is a member of a provisionable group, the provisioner makes sure that person exists as an Interfolio user, creating or updating them through the Interfolio IAM API and looking them up through the byc/core users search.

This is an **entity-only** provisioner: it manages **users** only (no target groups or memberships). When it creates a user it also grants product access: it subscribes the user to **RPT** (always) and, if the `enableFs` config is on, to **FS** (faculty search). When a user is removed from the provisionable group, it removes that access - unsubscribes from RPT (and FS if enabled).

"Delete" here means **remove product access**, not remove the Interfolio account: Interfolio has no hard delete for our credentials, and a user removed from the group keeps their account but loses RPT/FS access. The Faculty180 / FAR product is not handled here.

- Provisioner class: `edu.internet2.middleware.grouper.app.interfolio.InterfolioProvisioner`
- Provisioning type: `membershipObjects` (but it operates on entities only - no target groups or memberships)
- Authentication and hosts are configured on the external system (HMAC; an IAM host for create/update and a byc/core host for search and subscribe/unsubscribe).

## Configuration

| Property | Default | Description |
| --- | --- | --- |
| `interfolioExternalSystemConfigId` | (required) | The Interfolio external system to provision to. |
| `enableFs` | `true` | Also grant/remove FS (faculty search) access in addition to RPT. RPT is always granted on create and removed on delete; FS is gated by this flag. Set to `false` to provision RPT only. |

The provisioner supports **sync back**: when enabled, the users it reads off Interfolio (via the users search) are captured into the generic `grouper_prov_user` reporting table - target_user_id is the pid, and by default first_name, last_name, and email are captured (operators can capture other users/search fields via `nativeAttributesEntities`).

## External system

Configure the connection (HMAC keys, database id, and the IAM and byc/core URLs) first:

[Grouper Interfolio external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549839/Grouper+Interfolio+external+system)

## Developer notes

API endpoints, request/response examples, the mock table, and behaviors:

Grouper Interfolio provisioner developer notes

## Attributes

These are the target entity attributes you map when configuring the provisioner. The id is the Interfolio person id (pid) and is read-only; the others are the account fields sent to Interfolio.

| Grouper attribute name | Type | Required? | Interfolio field | Description |
| --- | --- | --- | --- | --- |
| `id` | String (numeric) | Yes | `pid` | Interfolio-assigned person id. **Read-only / select-only.** Do not translate from Grouper; configure it as a target attribute and cache it in an attribute value cache so the provisioner can link the user on later runs. |
| `institution_user_id` | String | Yes | `institution_user_id` | The UID / PennKey. **Immutable** once the user is created - Interfolio rejects any change to it. Typically translated from the subject id. |
| `email` | String | Yes | `email` | Email address. Interfolio uniqueness-checks this; a duplicate causes a create to fail. Used as the entity matching attribute (see below). |
| `first_name` | String | Yes (for create) | `first_name` | First name. |
| `last_name` | String | Yes (for create) | `last_name` | Last name. |
| `saml_id` | String | No | `saml_id` | The SSO id (e.g. [pennkey@upenn.edu](mailto:pennkey@upenn.edu)). Accepted on create/update but not returned by Interfolio. |
| `user_type` | String | No | `user_type` | Typically the static value `internal`. |

**Matching:** match entities on `email`. The byc/core users search returns pid + name + email but **not** institution_user_id or saml_id, so email is the attribute available for finding an existing Interfolio user.

## How to use it

1. Configure the [Interfolio external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549839/Grouper+Interfolio+external+system) and use its Test button to confirm the credentials authenticate.
2. Create a provisioner in the Grouper UI, choose **Interfolio**, and point it at that external system.
3. Map the target entity attributes from the table above (id read-only/cached; institution_user_id, email, first_name, last_name, and optionally saml_id and user_type translated from the subject).
4. Set the entity matching attribute to `email`.
5. Mark a folder or group as provisionable to this provisioner. Its members are the users that get created / updated in Interfolio.
6. Run a full sync (or let the daemons run). The provisioner will:
  
  - **Create** a user that is in the group but not yet in Interfolio (fails if the email already exists).
  - **Update** a user that exists, sending the full attribute set (institution_user_id must stay the same - it is immutable).
  - **Look up** existing users by email via the byc/core search.

When a subject is removed from the group, the provisioner **deprovisions** them - it unsubscribes the user from RPT (and FS if `enableFs` is on). The Interfolio account itself is not deleted (Interfolio does not authorize a hard delete for our credentials); the person simply loses RPT/FS access.

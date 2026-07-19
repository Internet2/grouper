---
title: "Grouper Interfolio provisioner developer notes"
space: GrIntDev
pageId: 48792485
version: 4
lastUpdated: 2026-07-12T17:27:28.179Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792485/Grouper+Interfolio+provisioner+developer+notes
---

## Log in to UI / docs

- (RPT / FS login) [https://logic.interfolio.com](https://logic.interfolio.com)
- (Faculty180 / FAR login) [https://faculty180.interfolio.com](https://faculty180.interfolio.com)
- (API docs) [Interfolio API documentation](https://product-help.interfolio.com/en_US/technical-resources/about-interfolio-apis-and-documentation)
- (Faculty180 RESTful API / HMAC) [https://faculty180.interfolio.com/swagger/ui/](https://faculty180.interfolio.com/swagger/ui/)

## External system

Interfolio external system documentation

## Authentication

All hosts use the same HMAC-SHA1 "INTF" scheme. The string to sign is the verb, three newlines, the timestamp, a newline, then the request string (everything after the host, **including the query string**):

signingString = VERB + "\n\n\n" + timestamp + "\n" + requestString signature = base64( hmacSha1( privateKey, signingString ) ) Headers on every request: TimeStamp: 2026-06-29 18:52:46 (UTC, yyyy-MM-dd HH:mm:ss) Authorization: INTF {publicKey}:{signature}Because the query string is part of the signature, the path-and-query must be built once and used both as the URL and as the signed request string, or the signature will not match (HTTP 401).

## Provisioner general

This is an **entity-only** provisioner: it creates / updates / looks up users. There are no group or membership objects (granting product access via subscribe/unsubscribe is not synced because Interfolio has no read-back of "who is subscribed").

Interfolio has more than one API host:

- **IAM API** (`iamUrl`, e.g. `[https://iam-api.interfolio.com](https://iam-api.interfolio.com)`) - create and update users.
- **byc/core API** (`bycUrl`, e.g. `[https://logic.interfolio.com](https://logic.interfolio.com)`) - search users; subscribe / unsubscribe RPT and FS.

Paging (search): start at `page=1` with a `limit` (e.g. 100), and increment the page until a page returns fewer than `limit` rows. The whole institution is returned when no `search` term is passed. `{tenant_id}` below is the configured `databaseId`.

## User

CRUD: select (search), insert, update. No delete (Interfolio returns 403 - not authorized for our credentials). Primary key: pid.

| mock_interfolio_user |
| --- |
| Column | Type | Description |
| pid | varchar(40) | stable cross-product person id; assigned by Interfolio (IAM returns it as "pid"); not null after create, readonly; the provisioner key |
| byc_id | varchar(40) | byc-internal user id (returned as "id" by users/search) |
| institution_user_id | varchar(256) | UID / PennKey; immutable once set |
| saml_id | varchar(256) | [pennkey@example.com](mailto:pennkey@example.com); accepted on create/update but not echoed back |
| user_type | varchar(64) | typically "internal" |
| first_name | varchar(256) |  |
| last_name | varchar(256) |  |
| email | varchar(256) | uniqueness-checked by Interfolio; duplicate -> 400 |
| rpt | varchar(1) | T or F; subscribed to RPT (byc-tenure) |
| fs | varchar(1) | T or F; subscribed to FS (byc-search) |

Mappable provisioning attributes: id (pid), institution_user_id, saml_id, user_type, first_name, last_name, email

Note: institution_user_id and saml_id are NOT returned by users/search (only by the IAM create/update response); the provisioner therefore matches on email.

## Search users (get all)

byc/core host. No `search` term returns the whole institution, paged. Returns pid + id + name + email (not UID / saml_id).

GET {bycUrl}/byc/core/tenure/{tenant_id}/institutions/{tenant_id}/users/search?limit=100&page=1 200 { "limit": 100, "page": 1, "total_count": 22035, "results": [ { "id": 1000001, "pid": "8000001", "first_name": "John", "last_name": "Smith", "email": "jsmith@example.com", "external_user": false, "role": null, "administrator_unit_names": [], "administrator_unit_ids": [], "evaluator_unit_names": [], "evaluator_unit_ids": [], "titles": [] } ] }

## Create user

IAM host. Returns the new user with its `pid` (numeric). Duplicate email -> 400.

POST {iamUrl}/iam/{tenant_id}/users Content-Type: application/json { "institution_user_id": "jsmith", "saml_id": "jsmith@example.com", "user_type": "internal", "first_name": "John", "last_name": "Smith", "email": "jsmith@example.com" } 200 { "pid": 8000001, "first_name": "John", "last_name": "Smith", "email": "jsmith@example.com", "institution_user_id": "jsmith", "user_type": "internal" }Duplicate email response (400):

400 { "errors": [ { "field": "", "message": "Validation failed: Email address jsmith@example.com already exists for an Interfolio account at this Institution. Try signing in instead." } ], "error_class": "ActiveRecord::RecordInvalid" }

## Update user

IAM host. Full replace - send every attribute. `institution_user_id` is immutable; sending a changed value -> 400.

PUT {iamUrl}/iam/{tenant_id}/users/{pid} Content-Type: application/json { "institution_user_id": "jsmith", "saml_id": "jsmith@example.com", "user_type": "internal", "first_name": "Johnny", "last_name": "Smith", "email": "jsmith@example.com" } 200 (returns the same shape as create)Changed institution_user_id response (400):

400 { "errors": [ { "field": "", "message": "Validation failed: Institution user id can't be changed." } ], "error_class": "ActiveRecord::RecordInvalid" }

## Subscribe to RPT (grant access)

byc/core host. service segment `byc-tenure` = RPT. No request body.

POST {bycUrl}/byc-tenure/{tenant_id}/users/{pid}/subscribe 200

## Subscribe to FS (grant access)

byc/core host. service segment `byc-search` = FS (faculty search). No request body.

POST {bycUrl}/byc-search/{tenant_id}/users/{pid}/subscribe 200

## Unsubscribe from RPT / FS (remove access)

byc/core host. Note this is a **PUT** (subscribe is a POST).

PUT {bycUrl}/byc-tenure/{tenant_id}/users/{pid}/unsubscribe (RPT) PUT {bycUrl}/byc-search/{tenant_id}/users/{pid}/unsubscribe (FS) 200

## Not supported (with our credentials)

- **Read a user by id:** `GET {iamUrl}/iam/{tenant_id}/users/{pid}` returns 400 "Invalid URL or method" - the IAM API has no get-by-id; use users/search instead.
- **Delete a user:** `DELETE {iamUrl}/iam/{tenant_id}/users/{pid}` returns 403 "You are not authorized to access this page" (CanCan::AccessDenied) - the endpoint exists but our key lacks delete permission.
- **FAR / Faculty180:** `{faculty180}/fars/{tenant_id}/users/{pid}/(un)subscribe` and `/api.php/*` return 401 / 403 - Faculty180 is a separate auth realm needing its own API key + string database id (its `/api.php` calls sign the path AFTER /api.php and require an `INTF-DatabaseID` header).

## Sync back

Sync back is supported (entity-only). The users read from the users/search endpoint are captured into the generic `grouper_prov_user` table by `InterfolioProvisioningTargetNativeSync`, hooked at the `GrouperInterfolioApiCommands.searchUsers` read seam. target_user_id is the pid; default captured attributes are first_name, last_name, email (configurable via nativeAttributesEntities). There is no group or membership sync back.

## Mock service

URL path for testing mock service: `/grouper/mockServices/interfolio`. Both the IAM and byc/core URLs point at this one mock; it dispatches by path and writes to `mock_interfolio_user`. It reproduces the real behaviors above (assigns a pid on create, 400 on duplicate email, 400 on changed institution_user_id, paged search, rpt/fs flags toggled by subscribe/unsubscribe).

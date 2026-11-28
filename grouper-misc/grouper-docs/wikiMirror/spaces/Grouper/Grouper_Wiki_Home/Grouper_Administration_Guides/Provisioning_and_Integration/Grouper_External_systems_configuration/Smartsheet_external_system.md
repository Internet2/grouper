---
title: "Smartsheet external system"
space: Grouper
pageId: 297631755
version: 1
lastUpdated: 2026-10-14T05:04:09.698Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/297631755/Smartsheet+external+system
---

See also: [Grouper Smartsheet provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/297533449/Grouper+Smartsheet+provisioner) and [Smartsheet API authentication](https://developers.smartsheet.com/api/smartsheet/guides/basics/authentication)

Available in Grouper v4.27.0+, v6.6.0+ and v7.9.0+ (GRP-7485).

The Smartsheet provisioner uses a standard Grouper `WsBearerToken` external system. Smartsheet API 2.0 authenticates with an API access token sent as `Authorization: Bearer <token>`.

## The token account

The token acts as the Smartsheet user who generated it. Use a **dedicated service account** (not a person's own account) that is a **System Admin**:

- Adding, updating, deactivating, reactivating and removing users, and changing seat types, are System Admin only
- User `status` and the account flags (admin, licensedSheetCreator, groupAdmin, resourceViewer) are only returned to a System Admin
- Creating, updating and deleting groups and their members needs Group Admin or System Admin; groups Grouper creates are owned by this account

List the service account's email in the provisioner's `smartsheetIgnoreUserEmails` so Grouper never changes it (for example, never deactivates it or removes its System Admin flag when an admin group is configured).

To create the token, log in to Smartsheet as the service account: profile icon -> "Apps & Integrations" -> "API Access" -> "Generate new access token". The token is shown once.

## Configuration

| Setting | Value | Notes |
| --- | --- | --- |
| httpAuthnType | bearerToken |  |
| endpoint | https://api.smartsheet.com | API host without `/2.0`. EU: `https://api.smartsheet.eu`, AU: `https://api.smartsheet.au`, Smartsheet Gov: `https://api.smartsheetgov.com` |
| accessTokenPassword | the API token | Encrypted by Grouper |
| testUrlSuffix | /2.0/users?pageSize=1 | For the "Test" button |
| testHttpMethod | GET |  |
| testHttpResponseCode | 200 |  |
| testUrlResponseBodyRegex | .*totalCount.* |  |

## Example configuration properties

```
grouper.wsBearerToken.smartsheetProd.httpAuthnType = bearerToken
grouper.wsBearerToken.smartsheetProd.endpoint = https://api.smartsheet.com
grouper.wsBearerToken.smartsheetProd.accessTokenPassword = (the API token)
grouper.wsBearerToken.smartsheetProd.testUrlSuffix = /2.0/users?pageSize=1
grouper.wsBearerToken.smartsheetProd.testHttpMethod = GET
grouper.wsBearerToken.smartsheetProd.testHttpResponseCode = 200
grouper.wsBearerToken.smartsheetProd.testUrlResponseBodyRegex = .*totalCount.*
```

## Verifying connectivity

```
curl -s "https://api.smartsheet.com/2.0/users?pageSize=1" -H "Authorization: Bearer YOUR_TOKEN"
```

A successful response is JSON with `pageNumber`, `totalPages`, `totalCount` and a `data` array of users. If the users have no `status` field, the token account is not a System Admin.

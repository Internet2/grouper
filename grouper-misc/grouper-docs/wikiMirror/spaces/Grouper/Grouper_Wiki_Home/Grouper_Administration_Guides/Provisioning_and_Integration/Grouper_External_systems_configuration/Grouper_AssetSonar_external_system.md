---
title: "Grouper AssetSonar external system"
space: Grouper
pageId: 269582340
version: 2
lastUpdated: 2026-10-04T03:25:16.870Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/269582340/Grouper+AssetSonar+external+system
---

See also: [AssetSonar provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/269516802/Grouper+AssetSonar+provisioner).

The AssetSonar provisioner connects through a standard **WsBearerToken** external system (the generic web service connection), configured to send AssetSonar's plain `token` header. There is no AssetSonar-specific external system type.

## Configuration

Create a WsBearerToken external system in the Grouper UI, then pick it as the provisioner's `assetSonarExternalSystemConfigId`:

```properties
grouper.wsBearerToken.myAssetSonar.endpoint = https://school.assetsonar.com
grouper.wsBearerToken.myAssetSonar.httpAuthnType = bearerToken
grouper.wsBearerToken.myAssetSonar.accessTokenPassword = <REST API token>
grouper.wsBearerToken.myAssetSonar.httpHeader = token
grouper.wsBearerToken.myAssetSonar.prependBearerTokenPrefix = false
grouper.wsBearerToken.myAssetSonar.testUrlSuffix = /members.api
grouper.wsBearerToken.myAssetSonar.testUrlResponseBodyRegex = ^.*status.*$
```

| Property | Value | Notes |
| --- | --- | --- |
| endpoint | tenant root url | The provisioner appends `/members.api` and `/members/<id>.api`. |
| accessTokenPassword | REST API token | Encrypted at rest and masked in the UI. |
| httpHeader | `token` | AssetSonar does not use `Authorization`. |
| prependBearerTokenPrefix | `false` | AssetSonar wants the bare token, no `Bearer` prefix. |
| testUrlSuffix, testUrlResponseBodyRegex | `/members.api`, `^.*status.*$` | The test button: a 200 with a member list (every member has a `status`). A bad token or url fails on the response code. |
| delayAfterEachCallInMs | optional | Pause after every call (reads and writes) if the tenant returns 502s under load. |

The access tier role ids are provisioner properties (`assetSonarStaffUserRoleId`, `assetSonarAgentRoleId`, `assetSonarAdministratorRoleId`), not part of the connection. See the provisioner page.

## Obtaining the token

In AssetSonar: "Settings" -> "Add Ons" -> "API Integration". Enable it and generate an access token. The token acts as the member it belongs to; prefer the least-privileged member that can create and update members over an Administrator.

## Security

- The token can create and deactivate any member; treat it as a tenant-wide credential and rotate it if exposed.
- AssetSonar also offers SCIM, but the provisioner does not use it (SCIM only sees login-capable members), so no SCIM credential is configured.

## History

Before GRP-7433 (v7) this was a custom AssetSonar external system (`grouper.assetSonarConnector.<id>.*`) with an `apiToken` property. To migrate: create the WsBearerToken external system above with the same id, move the three role ids to the provisioner, run the test button, and delete the old `grouper.assetSonarConnector.*` properties.

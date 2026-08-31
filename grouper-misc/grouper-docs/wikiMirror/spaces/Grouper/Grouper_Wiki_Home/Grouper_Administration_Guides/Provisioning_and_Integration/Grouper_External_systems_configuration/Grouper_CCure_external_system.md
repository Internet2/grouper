---
title: "Grouper CCure external system"
space: Grouper
pageId: 132022298
version: 1
lastUpdated: 2026-08-15T18:34:33.848Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/132022298/Grouper+CCure+external+system
---

C·CURE 9000 is an access control and security management system from Software House / Johnson Controls. This external system holds the connection to its web service API so the CCure provisioner can read Personnel and Clearances and write clearance pairs.

 The API is the **victor Web Service**. It is license gated: the "Victor Web Service for End-Users" license must be enabled on the CCure system before the API responds at all. There is no public API reference; the endpoint behavior Grouper relies on is written up in [Grouper CCure provisioner developer notes](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/131956793/Grouper+CCure+provisioner+developer+notes).

 

## Get the API credentials

 Request the following from whoever administers your CCure system:

 

1. **Username and password** - a CCure operator account authorized for the web service.
2. **Client name** - the client registration name, agreed with the CCure administrator. CCure validates this on every login alongside the credentials.
3. **Client id and client version** - optional, but if the CCure system is configured to require them the login fails without them.
4. **Endpoint** - the base URL of the web service.

 

## Configure the external system

 **External system type**: CCure

 

| Field | Required | Description |
| --- | --- | --- |
| Endpoint | Yes | Base web service URL, e.g. `https://ccure.example.com/ccure/`. Do **not** include the `/api/...` part - every command adds its own path. |
| Username | Yes | CCure operator account. |
| Password | Yes | Password for that account. Stored encrypted. |
| Client name | Yes | Client registration name, e.g. `Internet2 - Grouper - Integration`. |
| Client ID | No | Client GUID, if your CCure system requires one. |
| Client version | No | Client version string, e.g. `2.9`. |
| Personnel page size | No | Rows per page when reading Personnel. Default 2000. |
| Clearance pair page size | No | Rows per page when reading clearance pairs. Default 2000. |
| Proxy URL / proxy type | No | Proxy to reach the endpoint, if needed. |
| Test URL suffix | No | Path appended to the endpoint for the connection test. See below. |
| Test HTTP method | No | Method for the test call. Defaults to `GET`. |
| Test HTTP response code | No | Response code the test expects. |
| Test URL response body regex | No | Regex the test response body must match. |

 

## Authentication

 CCure uses session login rather than a bearer token. Grouper posts the credentials to `/api/Authenticate/Login`, and CCure returns a `session-id` response header plus a token in the body. Both are sent on every later request - the session id as a header, the token as a `token` URL parameter. Grouper calls `/api/Authenticate/Logout` when it is finished.

 Sessions are a limited resource on the CCure side, so a provisioning run logs in once and logs out at the end rather than per request.

 

## Test the external system

 Click "Test" on the external system screen. The test always logs in and logs out, which on its own proves the credentials, client name, and endpoint are right. If a test URL suffix is filled in, the test also calls that URL between the login and the logout.

 A useful test URL is a clearance that cannot exist, so a healthy system answers "not found":

 

| Field | Value |
| --- | --- |
| Test URL suffix | `/api/Objects/Get/Clearance/0` |
| Test HTTP method | `GET` |
| Test HTTP response code | `404` |

 Common failures:

 

- `CCure authentication failed, code=401` with "User not in system" - the username, password, or client name does not match what CCure expects. CCure returns the same 401 for all three, so check all of them.
- A connection error naming `null/api/Authenticate/Login` - the endpoint is not set.
- A test response code mismatch - the endpoint is reachable but the test URL suffix or expected code is wrong.

 If a test URL suffix is set but no test HTTP response code is, the test throws rather than reporting a clean error. Set both fields or neither.

 

## grouper-loader.properties

 
```
grouper.CCureExternalSystem.ccureProd.endpoint = https://ccure.example.com/ccure/
grouper.CCureExternalSystem.ccureProd.username = ccureUser
grouper.CCureExternalSystem.ccureProd.password = *******
grouper.CCureExternalSystem.ccureProd.clientName = Internet2 - Grouper - Integration
grouper.CCureExternalSystem.ccureProd.clientId = ffffffff-ffff-ffff-ffff-ffffffffffff
grouper.CCureExternalSystem.ccureProd.clientVersion = 2.9
grouper.CCureExternalSystem.ccureProd.testUrlSuffix = /api/Objects/Get/Clearance/0
grouper.CCureExternalSystem.ccureProd.testHttpMethod = GET
grouper.CCureExternalSystem.ccureProd.testHttpResponseCode = 404
```

 

## Use the external system

 [Grouper CCure provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/131956770/Grouper+CCure+provisioner)

 

## Grouper development team testing

 Set `grouper.is.mockServices = true` in grouper.hibernate.properties (or set env var `GROUPER_MOCK_SERVICES=true`).

 URL path for testing mock service: `/grouper/mockServices/ccure`

 The mock validates the posted credentials against a real external system config rather than hardcoding them. It reads `grouperTest.ccure.mock.configId` from grouper.properties to decide which config, falling back to the placeholder config id `myCCure`. Because grouper.properties refreshes slowly in a separate JVM, tests use the `myCCure` fallback and write nothing to grouper.properties.

 
```
grouper.CCureExternalSystem.myCCure.endpoint = http://localhost:8080/grouper/mockServices/ccure
grouper.CCureExternalSystem.myCCure.username = ccureUser
grouper.CCureExternalSystem.myCCure.password = ccurePassword
grouper.CCureExternalSystem.myCCure.clientName = Internet2 - Grouper - Integration
grouper.CCureExternalSystem.myCCure.clientId = ffffffff-ffff-ffff-ffff-ffffffffffff
grouper.CCureExternalSystem.myCCure.clientVersion = 2.9
```

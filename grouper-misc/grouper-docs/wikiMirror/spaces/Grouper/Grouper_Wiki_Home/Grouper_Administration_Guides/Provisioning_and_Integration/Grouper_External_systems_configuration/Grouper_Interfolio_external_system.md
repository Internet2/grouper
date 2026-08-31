---
title: "Grouper Interfolio external system"
space: Grouper
pageId: 28549839
version: 4
lastUpdated: 2026-07-12T15:27:06.592Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549839/Grouper+Interfolio+external+system
---

Interfolio is a faculty information system. This external system holds the connection to the Interfolio APIs so the Interfolio provisioner can create and update users and look them up.

Interfolio has more than one API host, all authenticated with the same HMAC-SHA1 "INTF" scheme:

- **IAM API** (`[https://iam-api.interfolio.com](https://iam-api.interfolio.com)`) - create and update users.
- **byc/core API** (`[https://logic.interfolio.com](https://logic.interfolio.com)`) - search users and grant/remove access to the products RPT (review, promotion, and tenure) and FS (faculty search) via subscribe/unsubscribe.

Note: the Faculty180 / FAR API (`[https://faculty180.interfolio.com](https://faculty180.interfolio.com)`) is a separate authorization realm with its own credentials and database id, and is not configured here.

## Get the API credentials

Interfolio uses HMAC authentication. Request the following from your Interfolio representative (the same key pair works for both the IAM and byc/core APIs):

1. **Public key** - the INTF public key.
2. **Private key** - the INTF private key (secret). Used to sign each request.
3. **Database id** - the numeric Interfolio tenant id used in the URL path (Interfolio assigns this to your institution; it differs between the production and sandbox tenants).

Each request is signed as `VERB\n\n\n<timestamp>\n<path-and-query>`, HMAC-SHA1'd with the private key, base64-encoded, and sent as `Authorization: INTF {publicKey}:{signature}` with a `TimeStamp` header. Grouper builds these headers for you; you only need the keys and database id.

## Configure the external system

**External system type**: Interfolio

**Public key**: the INTF public key

**Private key**: the INTF private key (secret)

**Database id**: the Interfolio tenant id, e.g. `12345`

**byc/core URL**: the base url for the byc/core API (search, subscribe, unsubscribe), without anything on the path, e.g. [https://logic.interfolio.com](https://logic.interfolio.com)

**IAM URL**: the base url for the IAM API (create, update users), without anything on the path, e.g. [https://iam-api.interfolio.com](https://iam-api.interfolio.com)

## Test the external system

Unlike a generic web service external system, the Interfolio external system has **no test URL suffix or test response body regex fields to fill out**. The connectivity test is built in: it signs and calls a real, lightweight Interfolio endpoint for you.

To run it, on the external system screen:

1. Fill out the five connection fields above (public key, private key, database id, byc/core URL, IAM URL).
2. Click **Test** (or save). Grouper runs `InterfolioExternalSystem.test()`, which first checks that all five properties are present, then makes this call:GET {bycUrl}/byc/core/tenure/{databaseId}/institutions/{databaseId}/users/search?limit=1&page=1
3. A **successful** test shows no errors - the INTF HMAC signature was accepted (HTTP 200) and the byc/core credentials and database id are valid.
4. A **failed** test shows the error, for example:
  
  - `Undefined or blank property: grouper.interfolio.{configId}.privateKey` - a required field is missing.
  - An HTTP 401 / 403 - the public/private key or database id is wrong.

Notes:

- The test exercises only the **byc/core** host (search). The IAM host is not called by the test; the first user create/update will exercise that.
- The search with `limit=1` is deliberately lightweight - it returns a single user row, not the whole roster.

grouper-loader.properties

grouper.interfolio.interfolioProd.publicKey = yourInterfolioPublicKey grouper.interfolio.interfolioProd.privateKey = ******* grouper.interfolio.interfolioProd.databaseId = 12345 grouper.interfolio.interfolioProd.bycUrl = https://logic.interfolio.com grouper.interfolio.interfolioProd.iamUrl = https://iam-api.interfolio.com 

## Use the external system

[Grouper Interfolio provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555855/Grouper+Interfolio+provisioner)

## Grouper development team testing

Set this in grouper.hibernate.properties (or set env var: GROUPER_MOCK_SERVICES=true)

URL path for testing mock service: `/grouper/mockServices/interfolio`

Both the byc/core URL and the IAM URL point at the one mock for tests; it dispatches by path. The test helper `InterfolioProvisionerTestUtils.setupInterfolioExternalSystem()` stores the external system config (config id `intfTest`) pointing both URLs at the mock servlet.

grouper.interfolio.intfTest.publicKey = fakePublicKey grouper.interfolio.intfTest.privateKey = fakePrivateKey grouper.interfolio.intfTest.databaseId = 12345 grouper.interfolio.intfTest.bycUrl = http://localhost:8080/grouper/mockServices/interfolio/ grouper.interfolio.intfTest.iamUrl = http://localhost:8080/grouper/mockServices/interfolio/

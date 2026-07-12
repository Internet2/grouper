---
title: "Grouper Freshservice external system"
space: Grouper
pageId: 28547422
version: 13
lastUpdated: 2026-07-01T05:46:48.528Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547422/Grouper+Freshservice+external+system
---

## Get the API key

1. **Log in** to your Freshservice portal as an Agent or Administrator.
2. Click on your **profile picture/name** in the top right corner.
3. Select **Profile Settings**.
4. On the right side of the page, locate the **API Key** section (usually below the "Change Password" section).
5. **Copy** the API key.

## Configure the external system

**External system type**: Web service

**Authentication type**: Basic auth

**Standard user/password order**: false

**User name**: X (literally a capital X)

**Password**: API key

**URL**: Is the base url without anything on path, e.g. [https://domainname.freshservice.com](https://domainname.freshservice.com)

- Note: just because you have a custom domain name in the UI does not mean you can use that custom UI for web services

**Test URL suffix**: /api/v2/requesters

**Test response body regex**:

```
.*requesters.*
```

grouper-loader.properties

```
grouper.wsBearerToken.freshServiceProd.basicAuthPassword = *******
grouper.wsBearerToken.freshServiceProd.basicAuthStandardUserOrder = false
grouper.wsBearerToken.freshServiceProd.basicAuthUser = X
grouper.wsBearerToken.freshServiceProd.endpoint = https://somedomainname.freshservice.com
grouper.wsBearerToken.freshServiceProd.httpAuthnType = basicAuth
grouper.wsBearerToken.freshServiceProd.testUrlResponseBodyRegex = .*requesters.*
grouper.wsBearerToken.freshServiceProd.testUrlSuffix = /api/v2/requesters

```

## Use the external system

[Grouper Freshservice provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554244/Grouper+Freshservice+requester+provisioner)

## Grouper development team testing

Set this in grouper.hibernate.properties (or set env var: GROUPER_MOCK_SERVICES=true)

URL path for testing mock service: /grouper/mockServices/freshRequester

Set this in grouper.properties

```
grouperTest.exampleFreshRequester.mockExternalSystem.configId = freshServiceDev
```

---
title: "GrouperShell (gsh) HTTP client (GrouperHttpClient)"
space: Grouper
pageId: 28547649
version: 7
lastUpdated: 2026-07-01T05:46:27.550Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547649/GrouperShell+gsh+HTTP+client+GrouperHttpClient
---

HTTP call. Use this for all HTTP calls as a client

> import edu.internet2.middleware.grouper.util.*;  
>  GrouperHttpClient grouperHttpCall = new GrouperHttpClient(); grouperHttpCall.assignUrl(url); grouperHttpCall.assignGrouperHttpMethod("POST"); grouperHttpCall.addHeader("Content-Type", "application/json"); grouperHttpCall.addHeader("Authorization", "Bearer " + bearerToken); grouperHttpCall.assignBody(body); grouperHttpCall.executeRequest(); if (grouperHttpCall.getResponseCode() != 200) { throw new RuntimeException("Error connecting to '" + url + "': " + grouperHttpCall.getResponseCode()); } String json = grouperHttpCall.getResponseBody();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/util/GrouperHttpClient.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/util/GrouperHttpClient.html)

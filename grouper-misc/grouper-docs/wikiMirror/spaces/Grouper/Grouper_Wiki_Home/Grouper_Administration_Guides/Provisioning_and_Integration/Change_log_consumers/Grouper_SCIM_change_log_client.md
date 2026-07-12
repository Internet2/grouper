---
title: "Grouper SCIM change log client"
space: Grouper
pageId: 28548072
version: 38
lastUpdated: 2026-07-12T06:32:54.556Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548072/Grouper+SCIM+change+log+client
---

> The SCIM change log client described here was an experimental feature introduced in Grouper 2.2. It is still present in Grouper v4 but was **removed in v6**, and it was never completed (see Known limitations below). New deployments should use the SCIM provisioner in the Grouper provisioning framework instead.

 

## Overview

 Grouper can write group information out to [SCIM](http://www.simplecloud.info/) endpoints. Grouper acts as a SCIM *client*: it emits SCIM calls to a downstream endpoint as group and membership changes happen. This is not an alternative to the Grouper Web Service, and it is distinct from the Grouper TIER SCIM server, which consumes SCIM events *as a server*.

 At present the user id in the consuming system must match the subject id in Grouper.

 

## Activation

 To activate the SCIM support, copy the `grouper-scim-VERSION.jar` into your Grouper lib directory. The jar can be found in the `grouperScim` directory under `grouper-misc`. Then add the following to your `grouper.properties` file:

 
```text
scim.endpoint = URL TO SCIM ENDPOINT
scim.user = username
scim.password = password

```

 The URL should point to the Groups endpoint of the SCIM server. At this time only HTTP BASIC authentication is supported for accessing an endpoint. `scim.user` should be a user who has admin rights to the Groups endpoint, and `scim.password` should be that user's password.

 Next, in `grouper-loader.properties`, add a new change log consumer:

 
```text
changeLog.consumer.scim.class = edu.internet2.middleware.grouper.scim.ScimChangeLogConsumer

```

 Then restart the Grouper loader.

 

## How Grouper operations translate to SCIM operations

 The following operations are supported:

 

1. Create / remove group
2. Add / remove member

 

## Known limitations

 This module was based on a use case that no longer exists and was never finished. It lacks the following:

 

- Ability to bulk sync
- Ability to verify sync integrity
- Ability to map a Grouper subject to a SCIM User

 

## See also

 [Grouper TIER SCIM server](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549538/Grouper+SCIM+web+service)

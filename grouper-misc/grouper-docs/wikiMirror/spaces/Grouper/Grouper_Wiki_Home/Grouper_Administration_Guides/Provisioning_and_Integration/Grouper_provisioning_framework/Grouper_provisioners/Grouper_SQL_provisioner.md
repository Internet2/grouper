---
title: "Grouper SQL provisioner"
space: Grouper
pageId: 28554228
version: 21
lastUpdated: 2026-07-01T05:40:53.359Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554228/Grouper+SQL+provisioner
---

> See also [Grouper Provisioning Framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544760/Grouper+provisioning+framework)
> 
> See also [Grouper SQL d](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545506/Grouper+SQL+database+sync)[atabase provisioning](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545506/Grouper+SQL+database+sync)

> The info on this page applies to Grouper v4 and above.

Groups, users, and memberships can be provisioned to SQL tables. This is in v2.6.5+

Configure a provisioner for SQL where each column of the group / entity / membership tables are attributes of the group / entity / membership.

Groups and entities can have another table for multi-valued attributes.

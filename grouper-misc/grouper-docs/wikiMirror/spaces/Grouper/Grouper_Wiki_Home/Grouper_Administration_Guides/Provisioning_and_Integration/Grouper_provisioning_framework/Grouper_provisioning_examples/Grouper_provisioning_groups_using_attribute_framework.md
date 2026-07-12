---
title: "Grouper provisioning groups using attribute framework"
space: Grouper
pageId: 28555244
version: 10
lastUpdated: 2026-07-01T05:38:41.828Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555244/Grouper+provisioning+groups+using+attribute+framework
---

This is for Grouper 4.5.3+

Generally in provisioning you can use metadata to attach extra information to provisioning object.

If you already have information in the [attribute framework](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544741/Grouper+attribute+framework), or you want to share attributes across multiple provisioners, you can use the following strategy. Note this works for any target, but this example is LDAP.

If you want to have multiple attribute strategies, you need to get those into one table/view first (e.g. with Grouper SQL sync)

Note your attributes can be structured however you want, here are some examples, but if you don't see them here, it doesn't mean its not supported.

## Setup an attribute using name/values pairs (example 1)

Create attribute def

Create attribute name

Assign the attribute

SQL query to see attribute assignments

Make a view of attribute assignments (do not start view name with "grouper")

## Setup an attribute using marker attribute and metadata name/values pairs (example 2)

Create marker attribute def

Create marker attribute name

Create metadata attribute def

Create attribute name

Assign the attribute

SQL query to see attribute assignments

Make a view of attribute assignments (do not start view name with "grouper")

## Configure external group attributes in provisioner

Configure the external group table/view

Use those attributes

```
${grouperProvisioningGroup. retrieveAttributeValueString('groupAttributeResolverSql__posix_id')}
```

## See the attribute value in the target

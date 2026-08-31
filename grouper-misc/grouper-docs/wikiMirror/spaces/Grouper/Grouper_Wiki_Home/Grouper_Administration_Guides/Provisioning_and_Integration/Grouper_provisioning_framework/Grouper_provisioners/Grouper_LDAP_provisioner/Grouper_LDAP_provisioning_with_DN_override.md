---
title: "Grouper LDAP provisioning with DN override"
space: Grouper
pageId: 28559953
version: 7
lastUpdated: 2026-07-01T05:36:37.568Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559953/Grouper+LDAP+provisioning+with+DN+override
---

> The info on this page applies to Grouper 2.6 and above.

You can override individual DNs to point some groups to other locations in LDAP. You can override certain DNs or the provisioner can be configured to assume all provisioned groups have an overridden DN.

Set this setting to true:

Assign group metadata when marking group as provisionable to the DN to point to

Grouper will automatically translate the DN in the object model

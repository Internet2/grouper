---
title: "GrouperShell (gsh) Provisionable groups finder (ProvisionableGroupFinder)"
space: Grouper
pageId: 28549257
version: 5
lastUpdated: 2026-07-01T05:42:25.493Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549257/GrouperShell+gsh+Provisionable+groups+finder+ProvisionableGroupFinder
---

Use this class to find provisioning attributes on groups

Sample call

> ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder(); GrouperProvisioningAttributeValue attributeValue = provisionableGroupFinder.assignGroup(group).assignTargetName("ldapProvTest") .findProvisionableGroupAttributeValue();

Sample call to find multiple provisioning attributes on a group

> ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder(); Set<GrouperProvisioningAttributeValue> provisionableStemAttributeValues = provisionableGroupFinder.assignGroupName(group.getName()).findProvisionableGroupAttributeValues();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/provisioning/ProvisionableGroupFinder.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/provisioning/ProvisionableGroupFinder.html)

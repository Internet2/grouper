---
title: "GrouperShell (gsh) Provisionable folder finder (ProvisionableStemFinder)"
space: Grouper
pageId: 28549017
version: 5
lastUpdated: 2026-07-01T05:42:57.152Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549017/GrouperShell+gsh+Provisionable+folder+finder+ProvisionableStemFinder
---

Use this class to find provisioning attributes on stems

Sample call

> ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder(); GrouperProvisioningAttributeValue attributeValue = provisionableStemFinder.assignStem(stem).assignTargetName("ldapProvTest") .findProvisionableStemAttributeValue();

Sample call to find multiple provisioning attributes on a stem

> ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder(); Set<GrouperProvisioningAttributeValue> provisionableStemAttributeValues = provisionableStemFinder.assignStemName(stem.getName()).findProvisionableStemAttributeValues();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/provisioning/ProvisionableStemFinder.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/provisioning/ProvisionableStemFinder.html)

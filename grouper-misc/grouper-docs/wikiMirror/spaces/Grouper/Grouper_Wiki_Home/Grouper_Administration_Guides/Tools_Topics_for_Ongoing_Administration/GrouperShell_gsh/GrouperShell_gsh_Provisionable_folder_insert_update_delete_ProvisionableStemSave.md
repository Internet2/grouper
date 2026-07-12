---
title: "GrouperShell (gsh) Provisionable folder insert / update / delete (ProvisionableStemSave)"
space: Grouper
pageId: 28548455
version: 5
lastUpdated: 2026-07-01T05:44:29.555Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548455/GrouperShell+gsh+Provisionable+folder+insert+update+delete+ProvisionableStemSave
---

Use this class to add/edit/delete provisioning attributes on stems

Sample call

> ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave(); GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest").assignMetadataString("md_testInput", "testValue").assignStem(stem).save(); System.out.println(provisionableStemSave.getSaveResultType()); // INSERT, DELETE, NO_CHANGE, or UPDATE

Sample call to delete provisioning attributes from a stem

> ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave(); provisionableStemSave.assignTargetName("ldapProvTest") .assignSaveMode(SaveMode.DELETE).assignStem(stem).save();

Sample call to update only single attribute

> ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave(); GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest") .assignPolicyGroupOnly(true) .assignReplaceAllSettings(false) .assignStem(stem).save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/provisioning/ProvisionableStemSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/provisioning/ProvisionableStemSave.html)

---
title: "GrouperShell (gsh) Provisionable groups insert / update / delete (ProvisionableGroupSave)"
space: Grouper
pageId: 28547816
version: 6
lastUpdated: 2026-07-01T05:46:06.089Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547816/GrouperShell+gsh+Provisionable+groups+insert+update+delete+ProvisionableGroupSave
---

Use this class to add/edit/delete provisioning attributes on groups

Sample call

> ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave(); GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest").assignMetadataString("md_testInput", "testValue").assignGroup(group).save(); System.out.println(provisionableGroupSave.getSaveResultType()); // INSERT, DELETE, NO_CHANGE, or UPDATE

Sample call to delete provisioning attributes from a group

> ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave(); provisionableGroupSave.assignTargetName("ldapProvTest") .assignSaveMode(SaveMode.DELETE).assignGroupName(group.getName()).save();

Sample call to update only single attribute

> ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave(); provisionableGroupSave.assignTargetName("ldapProvTest") .assignProvision(true) .assignReplaceAllSettings(false) .assignGroup(group).save();

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/provisioning/ProvisionableGroupSave.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/app/provisioning/ProvisionableGroupSave.html)

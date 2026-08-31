---
title: "Programmatic access to Grouper - remove group provisioner"
space: Grouper
pageId: 28549672
version: 6
lastUpdated: 2026-07-01T05:41:24.747Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549672/Programmatic+access+to+Grouper+-+remove+group+provisioner
---

## Programmatic access to Grouper - remove group provisioner

This class is used to programmatically remove a provisioner from a group.

### Remove provisioner with name "myProvisioner" from a group with name: "test:test":

 GrouperUiBrowserProvisioningRemoveGroup grouperUiBrowserProvisioningRemoveGroup = new GrouperUiBrowserProvisioningRemoveGroup(page)  
.assignGroupToRemoveName("test:test").assignProvisionerName("myProvisioner").browse();

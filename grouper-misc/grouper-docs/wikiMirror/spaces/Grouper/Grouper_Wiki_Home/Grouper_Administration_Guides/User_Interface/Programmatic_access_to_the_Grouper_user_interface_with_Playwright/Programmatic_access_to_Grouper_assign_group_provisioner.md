---
title: "Programmatic access to Grouper - assign group provisioner"
space: Grouper
pageId: 28549654
version: 6
lastUpdated: 2026-07-01T05:41:27.630Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549654/Programmatic+access+to+Grouper+-+assign+group+provisioner
---

## Programmatic access to Grouper - assign group provisioner

This class is used to programmatically assign a group provisioner.

### Assign provisioner with name "myProvisioner" to a group with name: "test:test":

 GrouperUiBrowserProvisioningAssignGroup grouperUiBrowserProvisioningAssignGroup = new GrouperUiBrowserProvisioningAssignGroup(page)  
.assignGroupToAssignName("test:test").assignProvisionerName("myProvisioner").browse();

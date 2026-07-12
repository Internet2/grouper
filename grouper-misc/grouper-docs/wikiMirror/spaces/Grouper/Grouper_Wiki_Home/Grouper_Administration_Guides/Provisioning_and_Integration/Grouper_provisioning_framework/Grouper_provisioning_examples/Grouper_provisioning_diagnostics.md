---
title: "Grouper provisioning diagnostics"
space: Grouper
pageId: 28555849
version: 5
lastUpdated: 2026-07-01T05:37:16.083Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555849/Grouper+provisioning+diagnostics
---

Setting up and troubleshooting a provisioner is a difficult task. The provisioning diagnostics screen in the Grouper UI helps by running a provisioner's tasks and printing the information, logs, and error messages they produce. You can run the same diagnostics from GrouperShell (GSH).

> **Version:** Provisioning diagnostics are part of the Grouper provisioning framework, available in v2.5+.

> **Privileges:** Running diagnostics requires elevated access. In the UI, the "Diagnostics" action is available to Grouper sysadmins (who can edit provisioner configurations) and to users granted view access to that provisioner configuration. The GSH example below starts a root session (`GrouperSession.startRootSession()`), so it must be run by a Grouper administrator.

## Run diagnostics from the UI

On a provisioner's configuration, choose the "Diagnostics" action. Because these diagnostics run inside the UI, only run small jobs — provision a handful of groups and then run diagnostics. Diagnostics will not run while another provisioning job is running, so make sure no large full or incremental jobs are running first. For large-scale diagnostics, run them from GSH on a container with plenty of memory (see below).

## Run diagnostics from GSH

Run the diagnostics for a configured provisioner from GrouperShell (GSH), replacing `myProvisioner` with your provisioner's config id:

```java
import edu.internet2.middleware.grouper.app.provisioning.*;
GrouperSession.startRootSession();
GrouperProvisioner provisioner = GrouperProvisioner.retrieveProvisioner("myProvisioner");
provisioner.initialize(GrouperProvisioningType.diagnostics);
provisioner.provision(GrouperProvisioningType.diagnostics);
```

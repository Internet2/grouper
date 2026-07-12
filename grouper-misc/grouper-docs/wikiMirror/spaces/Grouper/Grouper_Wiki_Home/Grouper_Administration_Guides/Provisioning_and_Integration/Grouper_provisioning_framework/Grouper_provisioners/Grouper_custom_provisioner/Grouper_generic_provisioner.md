---
title: "Grouper generic provisioner"
space: Grouper
pageId: 28560419
version: 6
lastUpdated: 2026-07-01T05:35:36.675Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560419/Grouper+generic+provisioner
---

## Overview

 Generally a provisioner in the framework (built-in or custom) has a first-class provisioner implementation: it appears in the drop-down and has its own specific configuration, validation, and so on.

 However, if you want to implement a custom provisioner without writing all the classes and configuration to make that happen, you can use the **generic provisioner**. Implement a DAO class and register it in the provisioner. All configuration specific to this provisioner should live elsewhere in the configuration, not with the provisioner configuration or external system.

 The generic provisioner is available in Grouper `v2.6.19+` and `v4+`.

 > **Privileges:** creating or editing a provisioner configuration requires Grouper sysadmin (wheel group) or root privileges. A non-sysadmin subject can be granted read-only view of a specific provisioner configuration.

 

 

## Example

 The following child page walks through a worked example of using the generic provisioner:

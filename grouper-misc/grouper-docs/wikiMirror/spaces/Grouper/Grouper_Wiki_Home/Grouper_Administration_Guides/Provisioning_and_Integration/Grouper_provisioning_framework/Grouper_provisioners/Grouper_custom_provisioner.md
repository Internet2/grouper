---
title: "Grouper custom provisioner"
space: Grouper
pageId: 28555586
version: 3
lastUpdated: 2026-01-16T20:56:12.046Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555586/Grouper+custom+provisioner
---

## Summary

There are three ways to implement a custom provisioner:

1. [GSH template provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560403/GSH+template+provisioner): write the provisioning implementation in a "provisioning" GSH template
2. [Generic provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560419/Grouper+generic+provisioner): only has a DAO class configured in Grouper
3. [Custom provisioner](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28560415/Grouper+custom+provisioner+documentation): an institution can implement code and configuration to have a provisioner that is similar to a Grouper built-in provisioner

Generally a provisioner in the framework (built-in or custom) will have a first class provisioner implementation. i.e. you see it in the drop down, it has specific configuration and validation etc.

However, if you want to implement a custom generic provisioner and not write all the classes and configs to make that happen, you can use the generic provisioner. Just implement a DAO class and register it in the provisioner. All the configs specific to this provisioner should live elsewhere in the configuration, not with the provisioner configuration or external system.

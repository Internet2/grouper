---
title: "Template wizard - Policy group - Example"
space: Grouper
pageId: 28549466
version: 9
lastUpdated: 2026-07-01T05:41:58.303Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549466/Template+wizard+-+Policy+group+-+Example
---

This is in Grouper 2.4.0 UI patch #38

## Add a policy group to a folder

You will see options with some selected by default

That creates this structure

## Configure lockout groups and require groups

You can have lockout groups and require groups that users can use in policies even if they cannot READ them. Configure in grouper.properties

```
##################################
## Lockout groups.  Could be used for other things, but used for policy group templates at least
## if there is no allowed group, then anyone could use it
##################################

# group name of a lockout group
# {valueType: "group", regex: "^grouper\\.lockoutGroup\\.name\\.\\d+$"}
grouper.lockoutGroup.name.0 = ref:lockout

# allowed to use this lockout group.  If not configured, anyone could use
# {valueType: "group", regex: "^grouper\\.lockoutGroup\\.allowedToUse\\.\\d+$"}
grouper.lockoutGroup.allowedToUse.0 = ref:lockoutAllowedToUse

##################################
## Require groups.  Could be used for other things, but used for policy group templates at least
## if there is no allowed group, then anyone could use it
##################################

# group name of a require group
# {valueType: "group", regex: "^grouper\\.requireGroup\\.name\\.\\d+$"}
grouper.requireGroup.name.0 = ref:active

# allowed to use this require group.  If not configured, anyone could use
# {valueType: "group", regex: "^grouper\\.lockoutGroup\\.requireGroup\\.\\d+$"}
grouper.requireGroup.allowedToUse.0 = ref:activeCanUse

# group name of a require group
# {valueType: "group", regex: "^grouper\\.requireGroup\\.name\\.\\d+$"}
grouper.requireGroup.name.1 = ref:employee

# allowed to use this require group.  If not configured, anyone could use
# {valueType: "group", regex: "^grouper\\.lockoutGroup\\.requireGroup\\.\\d+$"}
grouper.requireGroup.allowedToUse.1 = ref:employeeCanUse

```

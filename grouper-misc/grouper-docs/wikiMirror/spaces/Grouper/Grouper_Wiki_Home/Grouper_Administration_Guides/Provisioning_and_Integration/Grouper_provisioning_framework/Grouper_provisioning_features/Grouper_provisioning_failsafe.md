---
title: "Grouper provisioning failsafe"
space: Grouper
pageId: 28555489
version: 13
lastUpdated: 2026-07-01T05:38:07.345Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555489/Grouper+provisioning+failsafe
---

> Provisioning failsafe is available in Grouper `v2.6.6+`.

 

## Overview

 If too many objects change during a single provisioning run, that can be a sign the provisioner has gone rogue, and the run should not proceed. Totals are based on the total number of provisionable objects.

 Failsafe guards against two things a rogue provisioner can do:

 

1. Deleting objects that it shouldn't.
2. Changing attributes that it shouldn't.

 

## Global defaults

 You can set global defaults that apply to every provisioner. Take any provisioner property key and change the first part to start with `provisionerDefault`.

 Example for `grouper-loader.properties` (confirm against `grouper-loader.base.properties` in case property keys change):

 
```
provisionerDefault.failsafeUse = true
provisionerDefault.failsafeMaxPercentRemove = 20
provisionerDefault.failsafeMaxOverallPercentGroupsRemove = 20
provisionerDefault.failsafeMaxOverallPercentMembershipsRemove = 20

```

 

## Per-provisioner settings

 Each provisioner also has local settings, including an "allow" override for a specific date. Configure these in the UI. As above, confirm against `grouper-loader.base.properties` in case property keys change.

 

## Configuration reference

 The full set of failsafe configuration keys, as documented in `grouper-loader.base.properties`:

  
```
# Show failsafe options
# {valueType: "boolean", order: 120000, defaultValue: "false", subSection: "failsafe"}
# provisioner.genericProvisioner.showFailsafe =

# If the loader should check to see too many users were removed, if so, then error out and
# wait for manual intervention.  This setting means have global defaults.  If there are local settings
# those will still be used.
# {valueType: "string", order: 121000, formElement: "dropdown", subSection: "failsafe", showEl: "${showFailsafe}", optionValues: ["false", "true"]}
# provisioner.genericProvisioner.failsafeUse =

# if sending email on loader failsafe issues.  Default to true if there are email addresses to send to
# {valueType: "string", order: 122000, formElement: "dropdown", subSection: "failsafe", showEl: "${showFailsafe}", optionValues: ["false", "true"]}
# provisioner.genericProvisioner.failsafeSendEmail =

# If a group has a size less than this (default 200), then make changes including blanking it out.
# if -1 then do not have a global default
# {valueType: "integer", order: 123000, subSection: "failsafe", showEl: "${showFailsafe}"}
# provisioner.genericProvisioner.failsafeMinGroupSize =

# if a group with more members than the loader.failsafe.minGroupSize have more than this percent (default 30)
# removed, then log it as error, fail the job, and don't actually remove the members
# In order to run the job, an admin would need to change this param in the config,
# and run the job manually, then change this config back.
# if -1 then do not have a global max percent remove
# {valueType: "integer", order: 124000, subSection: "failsafe", showEl: "${showFailsafe}"}
# provisioner.genericProvisioner.failsafeMaxPercentRemove =

# Only applicable if the number of managed groups (i.e. match the groupLikeString) that have
# members in Grouper before the loader starts is at least this amount.
# {valueType: "integer", order: 125000, subSection: "failsafe", showEl: "${showFailsafe}"}
# provisioner.genericProvisioner.failsafeMinManagedGroups =

# If the group list meets the criteria above and the percentage of groups that are managed by
# the loader (i.e. match the groupLikeString) that currently have members in Grouper but
# wouldn't after the job runs is greater than this percentage, then don't remove members,
# log it as an error and fail the job.  An admin would need to approve the failsafe or change this param in the config,
# and run the job manually, then change this config back.
# {valueType: "integer", order: 126000, subSection: "failsafe", showEl: "${showFailsafe}"}
# provisioner.genericProvisioner.failsafeMaxOverallPercentGroupsRemove =

# This does not work for grouper loader currently.  If the group list meets the criteria above and the
# percentage of memberships that are managed by
# the loader (i.e. match the groupLikeString) that currently have members in Grouper but
# wouldn't after the job runs is greater than this percentage, then don't remove members,
# log it as an error and fail the job.  An admin would need to approve the failsafe or change this param in the config,
# and run the job manually, then change this config back.
# {valueType: "integer", order: 127000, subSection: "failsafe", showEl: "${showFailsafe}"}
# provisioner.genericProvisioner.failsafeMaxOverallPercentMembershipsRemove =

# If the overall membership count of the job is less than this amount then trigger a failsafe (do not run the job).
# -1 means disable this failsafe.  There is no default value for this configuration.
# {valueType: "integer", order: 128000, subSection: "failsafe", showEl: "${showFailsafe}"}
# provisioner.genericProvisioner.failsafeMinOverallNumberOfMembers =

```

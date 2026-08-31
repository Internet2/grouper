---
title: "Grouper deprovisioning getting started"
space: Grouper
pageId: 28549251
version: 11
lastUpdated: 2026-07-01T05:42:26.503Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549251/Grouper+deprovisioning+getting+started
---

This page walks through enabling Grouper deprovisioning and the related `grouper.properties` settings. Deprovisioning is available in Grouper v2.4+.

## Identify affiliations

Decide on the affiliations to be deprovisioned (probably start with one, e.g. "employee" or "member"). You can pick multiple, and you can add more at a different time.

## Configure grouper.properties

Add the deprovisioning settings to `grouper.properties`. The block below shows the available keys with their defaults:

```text
###################################
## Deprovisioning
###################################

# if deprovisioning should be enabled
deprovisioning.enable = true

# comma separated affiliations for deprovisioning e.g. employee, student, etc
# these need to be alphanumeric suitable for properties keys for further config or for group extensions
deprovisioning.affiliations =

# Group name of the group that identifies generally if an entity is in this affiliation. So if a group is
# deprovisioned by various affiliations, then only deprovision if the entity in the group is not in any
# affiliation eligible group.
# e.g. VPN is deprovisioned by affiliations employee and student. If the person is no longer an employee,
# but is still a student, then dont deprovision.
# for example deprovisioning.affiliation_<affiliationName>.groupNameMeansInAffiliation set to a:b:c
# deprovisioning.affiliation_employee.groupNameMeansInAffiliation = community:employee

# number of minutes to cache deprovisioned members / admins
deprovisioning.cacheMembersForMinutes = 5

# number of seconds to wait for refresh before giving up and using failsafe (if caching)
deprovisioning.cacheFailsafeSeconds = 10

# folder where system objects are for deprovisioning
# e.g. managersWhoCanDeprovision_<affiliationName>
# e.g. usersWhoHaveBeenDeprovisioned_<affiliationName>
deprovisioning.systemFolder = $$grouper.rootStemForBuiltinObjects$$:deprovisioning

# autocreate the deprovisioning groups
deprovisioning.autocreate.groups = true

# default if the loader should not let deprovisioned users in that affiliation in loader jobs
deprovisioning.autoChangeLoader = true

# users in this group who are admins of a affiliation but who are not Grouper SysAdmins, will be
# able to deprovision from all grouper groups/objects, not just groups they have access to UPDATE/ADMIN
deprovisioning.admin.group = $$deprovisioning.systemFolder$$:deprovisioningAdmins

# number of days in deprovisioning group.  Should be the amount of time for systems of record to catch up
# and for people to change external systems of record in manual processes
deprovisioning.defaultNumberOfDaysInDeprovisioningGroup = 14

# number of objects shown in the body of deprovisioning email
deprovisioning.email.object.count = 100

# deprovisioning reminder email subject
deprovisioning.reminder.email.subject = You have $objectCount$ objects that have suggested users to be deprovisioned

# deprovisioning reminder email body (links and objects are added dynamically)
deprovisioning.reminder.email.body = You need to review the memberships of the following objects.  Review the memberships of each object and click: Group actions -> Deprovisioning -> Members of this object have been reviewed
deprovisioning.reminder.email.body.greaterThan100 = There are $remaining$ more objects to be reviewed.

# if you want vetos when people are deprovisioned
grouperHook.MembershipVetoIfDeprovisionedHook.autoRegister = true
```

## Deprovisioning managers

Identify the deprovisioning managers and add them to the managers group. For example, if your `grouper.rootStemForBuiltinObjects` is "etc" and your deprovisioning affiliation is "employee", then the group would be:

```
etc:deprovisioning:managersWhoCanDeprovision_employee
```

If you don't identify managers, then your Grouper admins can deprovision people. Affiliation admins listed in `deprovisioning.admin.group` can deprovision from all Grouper groups and objects, not just those they have `UPDATE`/`ADMIN` on.

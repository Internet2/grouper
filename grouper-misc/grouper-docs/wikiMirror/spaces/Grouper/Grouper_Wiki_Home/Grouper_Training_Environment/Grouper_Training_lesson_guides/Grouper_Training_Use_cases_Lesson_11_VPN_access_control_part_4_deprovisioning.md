---
title: "Grouper Training - Use cases - Lesson 11: VPN access control part 4 - deprovisioning"
space: Grouper
pageId: 28545548
version: 5
lastUpdated: 2026-04-22T01:11:40.348Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545548/Grouper+Training+-+Use+cases+-+Lesson+11+VPN+access+control+part+4+-+deprovisioning
---

## Setup

### Create groups in etc:deprovisioning:

- managersWhoCanDeprovision_employees
- managersWhoCanDeprovision_students
- usersWhoHaveBeenDeprovisioned_employees
- usersWhoHaveBeenDeprovisioned_students

### Configure

```
deprovisioning.enable = true
deprovisioning.affiliations = employees, students

deprovisioning.affiliation_employees.groupNameMeansInAffiliation = ref:role:all_facstaff
deprovisioning.affiliation_students.groupNameMeansInAffiliation = basis:sis:prog_status:all:ac

```

## Deprovision a user

ITS is aware that rkirby is no longer an employee. They were in the Security dept and had a high level of access. Loader jobs will remove them from basis groups, but we want to make sure they are taken out of ad hoc groups.

First, let's simulate that they have access to the VPN.

- Add rkirby to ref:certs:network_aup_acknowledged (ok to do as banderson; don't need to go through the custom UI)

Go to Miscellaneous > Deprovisioning. This item link only appears once we enabled deprovisioning through properties

Choose "employees" from the dropdown

Choose Deprovisioning actions > Deprovision User

Notice the list of their groups is only the direct memberships.

The loader process will eventually remove them from the basis group, so we can uncheck that box. We just need to take them out of the network_aup_acknowledged group.

Click on Deprovision user and remove access

Check network_aup_acknowledged to see they are removed

## Don't allow deprovisioned users back into the network AUP group

Group network_aup_acknowledged > Deprovisioning > Deprovisioning actions > Edit deprovisioning settings

Affiliation: Employees

Note defaults, save it

Try to add back rkirby as banderson (don't try self-service custom UI, it's a bug)

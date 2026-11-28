---
title: "Grouper Training - Use cases - Lesson 11: VPN access control part 4 - deprovisioning"
space: Grouper
pageId: 28545548
version: 6
lastUpdated: 2026-11-18T19:38:52.535Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545548/Grouper+Training+-+Use+cases+-+Lesson+11+VPN+access+control+part+4+-+deprovisioning
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Learning objectives

- Configure deprovisioning by affiliation
- Deprovision a user from ad hoc groups
- Keep deprovisioned users from being added back to a group

## Setup

### Create the deprovisioning groups

Create these four groups in the folder "etc:deprovisioning". For each one:

1. In the left menu, click "Browse folders", click "Root", click "etc", then click "deprovisioning".
2. Click "Folder actions" → "Create new group".
3. "Group name": paste the group name (copy boxes below), then click "Save".
  
  
  ```
  managersWhoCanDeprovision_employees
  ```
  
  
  ```
  managersWhoCanDeprovision_students
  ```
  
  
  ```
  usersWhoHaveBeenDeprovisioned_employees
  ```
  
  
  ```
  usersWhoHaveBeenDeprovisioned_students
  ```

### Configure deprovisioning

1. In the left menu, click "Miscellaneous", then click "Configure".
2. Click "Configuration files".
3. Click "Config actions" → "Import config file".
4. "Import configuration with": select "Copy/paste configuration entries".
5. "Configuration file type": select "grouper.properties".
6. "Configuration file contents": paste this, then click "Submit":
  
  
  ```
  deprovisioning.enable = true
  deprovisioning.affiliations = employees, students
  
  deprovisioning.affiliation_employees.groupNameMeansInAffiliation = ref:role:all_facstaff
  deprovisioning.affiliation_students.groupNameMeansInAffiliation = basis:sis:prog_status:all:ac
  ```

## Deprovision a user

ITS is aware that rkirby is no longer an employee. They were in the Security dept and had a high level of access. Loader jobs will remove them from basis groups, but we want to make sure they are taken out of ad hoc groups.

### Simulate that rkirby has VPN access

Do this as banderson. You do not need the custom UI.

1. Go to the group "ref:certs:network_aup_acknowledged" and click "Add members".
2. In "Member name or ID", paste this, pick rkirby from the list, and click "Add":
  
  
  ```
  rkirby
  ```

### Deprovision rkirby

1. In the left menu, click "Miscellaneous", then click "Deprovisioning". This link only appears once deprovisioning is enabled in the config.
2. "Affiliation": select "employees", then click "Submit".
3. Click "Deprovisioning actions" → "Deprovision User".
4. In "Member name or ID", paste this, pick rkirby from the list, and click "View user access to deprovision":
  
  
  ```
  rkirby
  ```
5. Notice the list of their groups is only the direct memberships.
6. The loader will eventually remove them from the basis group, so uncheck that box. Keep "ref:certs:network_aup_acknowledged" checked.
7. Click "Deprovision user and remove access".
8. Go to the group "ref:certs:network_aup_acknowledged" and check that rkirby is no longer a member.

## Don't allow deprovisioned users back into the network AUP group

1. Go to the group "ref:certs:network_aup_acknowledged".
2. Click "Group actions" → "Deprovisioning".
3. Click "Deprovisioning actions" → "Edit deprovisioning settings".
4. "Affiliation": select "employees".
5. Review the default settings, then click "Save".
6. As banderson, try to add rkirby back: click "Add members", paste this in "Member name or ID", and click "Add". Use the regular UI, not the self-service custom UI (it has a bug):
  
  
  ```
  rkirby
  ```

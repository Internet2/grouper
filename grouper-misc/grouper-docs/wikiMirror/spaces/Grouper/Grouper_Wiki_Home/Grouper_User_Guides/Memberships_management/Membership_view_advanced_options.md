---
title: "Membership view advanced options"
space: Grouper
pageId: 28545447
version: 12
lastUpdated: 2026-07-01T05:47:13.070Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545447/Membership+view+advanced+options
---

## Membership UI Advanced Options

There is an Advanced button when viewing a group's members. This allows additional features - custom composites, enabled/disabled dates, and point in time audit queries.

The UI also has a page that allows you to see all the group memberships under a folder. And the same features exist there as well. This can be accessed under the "More" tab when viewing a folder.

As of Grouper v2.5, there is the ability for attribute assignments on memberships.

### Custom composites

There's a new feature that allows you to perform a custom composite on a membership list. This can help with use cases involving attestation, auditing and deprovisioning. So for example, you can quickly and easily see all the users in your group who are not employees (without actually creating another composite group). This requires configuration in grouper.properties and grouper.text.en.us.properties. For example:

```
grouper.properties:

# {valueType: "string", regex: "^grouper\\.membership\\.customComposite\\.uiKey\\.\\d+$"}
grouper.membership.customComposite.uiKey.0 = customCompositeMinusEmployees
# {valueType: "string", regex: "^grouper\\.membership\\.customComposite\\.compositeType\\.\\d+$"}
grouper.membership.customComposite.compositeType.0 = complement
# {valueType: "group", regex: "^grouper\\.membership\\.customComposite\\.groupName\\.\\d+$"}
grouper.membership.customComposite.groupName.0 = ref:activeEmployees

# {valueType: "string", regex: "^grouper\\.membership\\.customComposite\\.uiKey\\.\\d+$"}
grouper.membership.customComposite.uiKey.1 = customCompositeIntersectIt
# {valueType: "string", regex: "^grouper\\.membership\\.customComposite\\.compositeType\\.\\d+$"}
grouper.membership.customComposite.compositeType.1 = intersection
# {valueType: "group", regex: "^grouper\\.membership\\.customComposite\\.groupName\\.\\d+$"}
grouper.membership.customComposite.groupName.1 = org:centralIt:staff:itStaff

grouper.text.en.us.properties:

customCompositeMinusEmployees = Members who are not employees
customCompositeIntersectIt = Members who are IT people
```

### Enabled/disabled dates

Previously, the Grouper UI had an issue where if a membership was disabled, then that membership wouldn't be visible in the UI. A disabled membership could exist because the start date is in the future or if there's a disabled date in the past. The UI now exposes this information.

Note that this view is only currently available for direct memberships. Indirect memberships are more complicated since they can involve multiple enabled/disabled dates. However, now if you trace an indirect membership, it will show you if any enabled/disabled dates exist on each of the memberships in the path.

### Point in time audit queries

Grouper has had point in time auditing since v2.0 but none of that information has been exposed in the UI. It's only been available through web services, GSH, and direct database queries. There's now basic information available (and likely more to come later).

### Show and filter group memberships under a folder

New option under "More"

You can see all the group memberships under the folder there. And you have the same filtering options. For example, now you can quickly/easily find all the memberships that don't belong to employees.

### Attribute Assignments on Memberships

The ability to add attribute assignments on memberships is only available when looking at an entity (subject) and seeing memberships.   
It is not available when on a group and looking at memberships.  
  
Start on the subject screen, in the Actions drop down next to groups. Select Attribute Assignments.

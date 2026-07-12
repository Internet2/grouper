---
title: "Group summary screen"
space: Grouper
pageId: 28544847
version: 12
lastUpdated: 2026-07-01T05:47:58.126Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544847/Group+summary+screen
---

## Group summary

In Grouper V5.21.0+, the new *Group Summary* screen is now the default view when a group is loaded in the UI. This screen highlights several key data points about the group, giving users a clear overview at a glance. As part of this update, the top collapsible (hide/show) section has also been moved to the summary screen. To navigate to the members screen, click the "Members" tab.

The initial screen is minimal so it will display quickly. There is a button "Show more information" to run the expensive queries and expand the summary.

The summary screen is the default group screen since:

- performance improvement for large groups
- large groups do not need to display the first page of sorted members
- the information on the summary screen can be more useful for users

If you want the default group screen to be the members screen (legacy behavior), set this in the grouper-ui.properties

```
# if the viewGroup default group screen defaults to the summary tab.  if not set or true, then summary tab, else members tab
# {valueType: "boolean", defaultValue: "true"}
uiV2.group.viewGroupDefaultToSummaryTab = false
```

## Group summary screen sections

| Section Name | Description | View type | Who can see? |
| --- | --- | --- | --- |
| Types | Shows object types such as basis, ref, etc for a group | default | Viewers |
| Memberships | Count of total members | default | Readers |
| Shows membership details such as    - Count of total members - Count of non-group members - Count of direct members - If the group is member of any other groups | more information |
| Privileges | Shows privileges details such as    - Count of total entities with privileges on the group - Count of non-group entities with privileges on the group - Count of total entities with direct privileges on group - List of direct groups with privileges on the group if the count is less than five - If the group has privileges on any other groups - List of groups where the current group has any privileges on (if the count is less than five) | more information | Admins |
| Loader | Shows loader details such as    - Loader type e.g SQL. LDAP - SQL query or LDAP filter | default | Admins |
| Composites | - Shows if the group is a composite owner and shows left, right, and the type of relationship - Shows if the group is a part of another composite owner group | default | Admins |
| Provisioning | - Shows the count of provisioning targets for the group - If the count is less than ten, shows the name of the targets | default | Admins |
| Attestation | - Shows if the group has attestation and the last certification date | default | Updaters |
| Attributes (*non-built-in only*) | - Shows the count of non-built-in (loader, attestation, etc) attributes assigned to the group | more information | Attribute readers |
| Rules | - Shows the count of rules assigned to the group - Shows the count of rules where this group is a part of | default | Readers |
| Recent membership changes | - Shows the count of memberships added and removed in the last month | more information | Readers |
| Recent audits | - Shows the count of audits in the last month for the group | more information | Readers |
| Configuration | - Shows the count of database configurations in which this group is referenced | more information | SysAdmins (wheel) |
| Fields | Shows several key details for the group such as    - Name - Path - Id path - Alternate ID path - ID - Created timestamp - Creator - Last edited timestamp - Last edited by - Type - ID index - UUID | default | Viewers |

The screenshots below illustrates the new layout:

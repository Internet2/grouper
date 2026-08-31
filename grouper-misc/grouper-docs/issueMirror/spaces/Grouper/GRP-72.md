---
key: GRP-72
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-72
type: New Feature
status: Resolved
resolution: Fixed
priority: Minor
reporter: Gary Brown <gary.brown@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-12-11T14:24:02.234+0000
updated: 2010-06-07T13:42:23.668+0000
resolved: 2010-06-07T13:42:23.726+0000
components: [UI]
fixVersions: [1.6.0]
labels: []
links: []
---

# GRP-72  Allow sites to disable 'editing' of groups (+- attributes, members and privs) in the UI

Many groups in Grouper will be loaded from authoritative sources. In general these groups should not be edited using the Grouper UI, however, there may be cases where 'empty' groups are created to promote consistency where a group membership may be updated but the group itself not deleted. This can be managed to some extent by appropriate granting (or not) of privileges, however, as GrouperSystem / Wheel group members always have ADMIN a mistake could be made.

I intend to allow a 'hook' in the UI which will allow sites to control whether some aspects of groups can ever be edited in the UI. Currently, at Bristol, I have a group type:

<groupType name="loader_maintained">
<attribute name="attr">y</attribute>
<attribute name="loader">GrouperLoader</attribute>
<attribute name="members-list">n</attribute>
<attribute name="privs">y</attribute>
</groupType>

which I intend to 'test'. I probably need to extend this so that additional group types could be added and related attributes managed in the UI. The hook will not depend on a particular type - rules will be site-implemented, however, a default attribute-based implementation would make sense.

## Comments

### Gary Brown - 2008-01-09T13:33:58.044+0000

Implementation of UIGroupPrivilegeResolver obtained from UIGroupPrivilegeResolverFactory configured in media.properties: key=edu.internet2.middleware.grouper.ui.UIGroupPrivilegeResolver controls whether the current user can manage core attributes, custom attributes, members and privileges.  DefaultUIGroupPrivilegeResolver works using standard Access interface rules. This class can be subclassed to provide site specific logic

### Gary Brown - 2010-06-07T13:38:37.897+0000

Privilege checks are not carried out when viewing group summary or editing attributes

### Gary Brown - 2010-06-07T13:42:23.666+0000

Have updated PopulateEditGroupAttributesAction to make the UiGroupPrivilegeResolver available, and modified JSP templates to check it for user privileges
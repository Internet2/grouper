---
key: GRP-43
cloud_key: 
onprem_url: https://todos.internet2.edu/browse/GRP-43
type: Improvement
status: Resolved
resolution: Fixed
priority: Trivial
reporter: James Cramton <jcramton@example.com>
assignee: Gary Brown <gary.brown@example.com>
created: 2007-09-20T18:58:03.575+0000
updated: 2007-10-16T12:21:55.657+0000
resolved: 2007-10-16T12:21:55.681+0000
components: [UI]
fixVersions: [1.2.1]
labels: []
links: []
---

# GRP-43  Find New Members page should default to 'member' privilege

Most day-to-day use of the Grouper UI at Brown requires instructional technology staff to add members to groups. Currently, the Find New Members page lists 7 privileges (member, optin, optout, view, read, update, and admin). Staff were forgetting to check the "member" checkbox, as required to grant membership to the group. Brown implemented a hack in the jsp controlling this display to check the "member" checkbox by default. The change is on line 39 of grouper-ui/webapp/WEB-INF/jsp/searchForPrivAssignmentListHeaderView.jsp  

<span class="checkbox"><input type="checkbox" name="privileges" value="member"  id="privMember" /> 

changes to 

<span class="checkbox"><input type="checkbox" name="privileges" value="member"  id="privMember" checked="checked" /> 

This is a hack that may not be applicable to all circumstances, and may lead to confusion in cases where membership is not a commonly assigned privilege. For Brown, it makes sense to use this hack, because the only cases when we assign anything other than member privileges are exceptions, and the admins assigning alternate privileges deliberately check the settings.

## Comments

### James Cramton - 2007-09-20T18:58:54.900+0000

The Member checkbox should be checked by default for Brown's typical use cases.

### Gary Brown - 2007-09-25T13:37:14.633+0000

U. Chicago also asked for this feature and it has been implemented in the 1.2.1 API (which hasn't been released, but is tagged in CVS and should work with the 1.2.0 API)

U. Chicago also asked that if there were only one search result that it also be checked. I have extended both features to privileges, so that if you list subjects with an Access / Naming privilege and then select to assign this privilege to others, the privilege will automatically be checked.

## Attachments
- edu.brown.SearchNewMembersDefaultMemberPrivs.jpg (137708 bytes) - by James Cramton on 2007-09-20T18:58:54.815+0000
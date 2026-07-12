---
title: "Universal Subject Daemon Utility (USDU)"
space: Grouper
pageId: 28548392
version: 23
lastUpdated: 2026-07-12T15:26:54.521Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548392/Universal+Subject+Daemon+Utility+USDU
---

## Universal Subject Daemon Utility (USDU)

> As of Grouper v2.4, updates were made to allow [deleting subjects after being unresolvable for X days.](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554830/USDU+delete+subjects+after+unresolvable+for+X+days)
> 
> As of Grouper v2.5, USDU has been changed to perform other functions as well. See [Grouper USDU v2.5+](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554283/Grouper+USDU+v2.5).   
> Because of these changes, USDU is now run using the [daemon](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545241/Grouper+Daemon).

The Unresolvable Subject Deletion Utility (USDU) finds and deletes memberships for subjects which can not be found by their source.

An unresolvable subject is a subject that can not be found by its source. A subject may be unresolvable because of a temporary or permanent source failure, or because it was removed from its source before memberships or privileges were deleted or revoked.

A future version may extend the Source class to provide more efficient lookups of subjects.

### Details

This utility finds and deletes memberships and privileges. It is possible for an unresolvable subject to be a creator or modifier of a group, in that case, calling Group.getCreateSubject() or Group.getModifySubject() will result in a SubjectNotFoundException.

Unresolvable subjects are not deleted from the grouper_members table. If an unresolvable subject becomes resolvable again, it will retain its member uuid.

**See Also**

[Subject Change Daemon](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554199/Subject+change+daemon)

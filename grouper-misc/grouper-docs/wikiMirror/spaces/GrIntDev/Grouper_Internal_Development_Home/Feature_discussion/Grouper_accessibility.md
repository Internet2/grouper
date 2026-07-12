---
title: "Grouper accessibility"
space: GrIntDev
pageId: 48793113
version: 37
lastUpdated: 2026-07-12T06:46:06.908Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793113/Grouper+accessibility
---

University of Colorado accessibility report

AUL Report (Accessibility and Usability Testing - Office of Info Technology - U of Colorado - Boulder)

Issue #1: [GRP-1380](https://bugs.internet2.edu/jira/browse/GRP-1380): "Add members” displays new content not apparent to a screenreader

```
commit ff831df553c7735d467a004c4f3b537088b861ca
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sat Oct 22 16:13:20 2016 -0700
    GRP-1380 - Add focus and aria attributes when Add Members button is clicked
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupHeader.jsp                    |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp |  2 +-
 grouper-ui/webapp/grouperExternal/public/assets/js/grouperUi.js               | 17 +++++++++++++++++
 3 files changed, 19 insertions(+), 2 deletions(-)
 
```

Issue #2: [GRP-1379](https://bugs.internet2.edu/jira/browse/GRP-1379): UI Accessibility Improvements - Search Icon Not Labelled

```
commit 8c6e4d068c115c5854404fd83ca25196548120e3
Date: Sat Oct 22 14:49:16 2016 -0700
GRP-1379 - Add aria-label to search box at the top of the page
grouper-ui/webapp/WEB-INF/grouperUi2/index/index.jsp | 2 +-
1 file changed, 1 insertion(+), 1 deletion(-)
```

Issue #3: [GRP-1394](https://bugs.internet2.edu/jira/browse/GRP-1394): UI Accessibility Improvements - Add role=button to Add Members link

```
commit 05a9b800e217c14b925f175cde9cd71da3514c76
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sat Oct 22 16:32:34 2016 -0700
    GRP-1394 - Add role=button to Add Members link
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp | 5 ++++-
 1 file changed, 4 insertions(+), 1 deletion(-)
 
commit dcf32457dd996558b5ce8cd4f0a699de1e449e47
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Nov 13 17:04:02 2016 -0800

    GRP-1394 - Add role=button to hyperlinks which act as buttons

 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefEdit.jsp                      |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefMoreActionsButtonContents.jsp |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefPrivileges.jsp                |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/newAttributeDef.jsp                       |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/viewAttributeDef.jsp                      |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupContents.jsp                                |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupPrivileges.jsp                              |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/newGroup.jsp                                     |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivileges.jsp             |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsGroupPrivileges.jsp                    |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsMemberships.jsp                        |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsMembershipsContents.jsp                |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsStemPrivileges.jsp                     |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/viewGroup.jsp                                    |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/groupImport/groupImport.jsp                            | 14 +++++++-------
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroups.jsp                                  |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroupsJoin.jsp                              |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroupsJoinContents.jsp                      |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroupsMemberships.jsp                       |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroupsMembershipsContents.jsp               |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/newStem.jsp                                       |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemCopy.jsp                                      |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemDelete.jsp                                    |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemEdit.jsp                                      |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemMoreActionsButtonContents.jsp                 |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemMove.jsp                                      |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemPrivileges.jsp                                |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/viewStem.jsp                                      |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivileges.jsp         |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivileges.jsp                |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivileges.jsp                 |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/viewSubject.jsp                                |  2 +-
 32 files changed, 51 insertions(+), 51 deletions(-)
```

Issue #4: [GRP-1397](https://bugs.internet2.edu/jira/browse/GRP-1397): “Member name or ID” field appears to be mislabeled and identified as a combo-box through use of ARIA role

```
commit 921f8ca3ab36a31ee3d91d1300e745a7436dff6c
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sat Oct 29 09:10:09 2016 -0700
    GRP-1397 - Attach the label to correct input field
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupHeader.jsp | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
```

Issue #5: [GRP-1398](https://bugs.internet2.edu/jira/browse/GRP-1398): UI Accessibility Improvements: Pull down menus throughout site do not indicate their function as a pull-down menu or announce changed state once selected

```
commit 4e40a118ec44a62f29a5c44693485f57d74b682a
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sat Oct 29 12:02:31 2016 -0700
    GRP-1398 - Toggle aria-expande based on pull down menu status
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp | 6 ++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupMoreTab.jsp                   | 5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemMoreActionsButtonContents.jsp   | 8 ++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemMoreTab.jsp                     | 5 +++--
 4 files changed, 16 insertions(+), 8 deletions(-)
 
commit 0af8c7e3da765840f0dba4108d13769c000b4388
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Mon Nov 14 08:17:43 2016 -0800

    GRP-1398 - Add aria roles and javascript for the pull down menus to make them accessibility compliant

 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefContents.jsp                  | 7 +++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefMoreActionsButtonContents.jsp | 8 ++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefMoreTab.jsp                   | 5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefPrivilegeContents.jsp         | 9 +++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupContents.jsp                                | 8 ++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupPrivilegeContents.jsp                       | 8 ++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivilegesContents.jsp     | 9 +++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsGroupPrivilegesContents.jsp            | 9 +++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsMembershipsContents.jsp                | 9 +++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsStemPrivilegesContents.jsp             | 9 +++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/index/index.jsp                                        | 8 ++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/index/indexColumnMenu.jsp                              | 7 +++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemPrivilegeContents.jsp                         | 9 +++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectContents.jsp                            | 9 +++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectMoreTab.jsp                             | 9 +++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivilegesContents.jsp | 9 +++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivilegesContents.jsp        | 9 +++++++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivilegesContents.jsp         | 9 +++++++--
 18 files changed, 114 insertions(+), 36 deletions(-)
```

Issue #6: [GRP-1399](https://bugs.internet2.edu/jira/browse/GRP-1399): UI Accessibility Improvements: Section tabs read as links

```
commit 2eb2f135608a8a41bd89ab4dc85c6f8b8042b6bc
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sat Oct 29 14:11:22 2016 -0700
    GRP-1399 - Add tabs accessibility
 grouper-misc/grouper.client-2.3.0/javadoc/api/script.js                                      |  30 ++++++++++++++++
 grouper-ui/webapp/WEB-INF/grouperUi2/group/assignedToGroupInheritedPrivilegesInvolvement.jsp |   4 +--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupPrivileges.jsp                               |   3 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupViewAudits.jsp                               |  18 +++++-----
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivileges.jsp              |  18 +++++-----
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsGroupPrivileges.jsp                     |  16 +++++----
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsMemberships.jsp                         |  18 +++++-----
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsPrivilegesInheritedFromFolders.jsp      |   3 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsStemPrivileges.jsp                      |  18 +++++-----
 grouper-ui/webapp/WEB-INF/grouperUi2/group/viewGroup.jsp                                     |  20 ++++++-----
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/privilegesInheritedToObjects.jsp                   |   3 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemPrivileges.jsp                                 |   3 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/thisFoldersPrivilegesInheritedFromFolders.jsp      |   3 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/viewStem.jsp                                       |   5 +--
 grouper-ui/webapp/grouperExternal/public/assets/js/grouperUi.js                              | 132 ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++-
 15 files changed, 237 insertions(+), 57 deletions(-)
 
commit 79a7c9d906d08534bb07bde870e5db2424056b1b
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Thu Dec 22 19:07:01 2016 -0800

    GRP-1399 - Add aria selected and role tab to tabs so that they are read as tabs and not links

 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefMoreTab.jsp                  |   2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefPrivileges.jsp               |   4 +-
 .../WEB-INF/grouperUi2/attributeDef/thisAttributeDefsPrivilegesInheritedFromFolders.jsp    |   4 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/viewAttributeDef.jsp                     |   4 +-
 .../webapp/WEB-INF/grouperUi2/group/assignedToGroupInheritedPrivilegesInvolvement.jsp      |   7 ++-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupMoreTab.jsp                                |   2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupPrivileges.jsp                             |   7 ++-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupProvisioning.jsp                           |   7 ++-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupViewAudits.jsp                             |   5 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivileges.jsp            |   5 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsGroupPrivileges.jsp                   |   5 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsMemberships.jsp                       |   5 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsPrivilegesInheritedFromFolders.jsp    |   8 ++-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsStemPrivileges.jsp                    |   5 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/viewGroup.jsp                                   |   5 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroups.jsp                                 |   6 +--
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroupsJoin.jsp                             |   6 +--
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroupsMemberships.jsp                      |   6 +--
 grouper-ui/webapp/WEB-INF/grouperUi2/myStems/myStems.jsp                                   |   6 +--
 grouper-ui/webapp/WEB-INF/grouperUi2/myStems/myStemsContainingAttributesImanage.jsp        |   6 +--
 grouper-ui/webapp/WEB-INF/grouperUi2/myStems/myStemsContainingGroupsImanage.jsp            |   6 +--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/privilegesInheritedToObjects.jsp                 |   8 ++-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemMoreTab.jsp                                  |   2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemPrivileges.jsp                               |   8 ++-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemViewAudits.jsp                               |   4 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/thisFoldersPrivilegesInheritedFromFolders.jsp    |   8 ++-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/viewStem.jsp                                     |   7 ++-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectMoreTab.jsp                            |   2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivileges.jsp        |   8 +--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivileges.jsp               |   8 +--
 .../webapp/WEB-INF/grouperUi2/subject/thisSubjectsInheritedPrivilegesInvolvement.jsp       |   8 +--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivileges.jsp                |   8 +--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/viewSubject.jsp                               |   8 +--
 grouper-ui/webapp/grouperExternal/public/assets/js/grouperUi.js                            | 132 ---------------------------------------------
 34 files changed, 86 insertions(+), 236 deletions(-)
```

Issue #7: [GRP-1400](https://bugs.internet2.edu/jira/browse/GRP-1400): UI Accessibility Improvements: Group membership entity table lacks column header for fourth column

```
commit a467a1e4ca19fe8af75408ee9a8898002029cc46
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sat Oct 29 14:43:58 2016 -0700
    GRP-1400 - Add labels for checkboxes and Actions header in the group detail page
 grouper-ui/conf/grouperText/grouper.text.en.us.base.properties | 4 ++++
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupContents.jsp   | 4 ++--
 2 files changed, 6 insertions(+), 2 deletions(-)
```

Issue #8: [GRP-1407](https://bugs.internet2.edu/jira/browse/GRP-1407): UI Accessibility Improvements: Placeholder text is not “visible” to a screenreader

```
commit 6ee2598fdbcc47479f1fc552c4852cf48ee68aaa
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sat Nov 5 22:11:11 2016 -0700

    GRP-1407 - Add placeholder text in the entity name text field

 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefPrivileges.jsp        | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupPrivileges.jsp                      | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivileges.jsp     | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsGroupPrivileges.jsp            | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsStemPrivileges.jsp             | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemPrivileges.jsp                        | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivileges.jsp | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivileges.jsp        | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivileges.jsp         | 2 +-
 9 files changed, 9 insertions(+), 9 deletions(-)
```

Issue #9: [GRP-1408](https://bugs.internet2.edu/jira/browse/GRP-1408): UI Accessibility Improvements: On the Grouper home page, “Create new group” is not read by screen readers until after all of the main content is read

```
commit 9e32426d1be97d7127abfe48f460a612a35c2be4
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Nov 6 09:03:23 2016 -0800

    GRP-1408 - Change the html to have the left box appear before the main content in the right side

 grouper-ui/webapp/WEB-INF/grouperUi2/index/index.jsp | 15 +++++++++------
 1 file changed, 9 insertions(+), 6 deletions(-)
```

Issue #10: [GRP-1409](https://bugs.internet2.edu/jira/browse/GRP-1409): UI Accessibility Improvements: Error (and success) messages are displayed above the page heading and all content, unapparent to screenreaders

```
commit b80e10deacd503efdd65ac3098f5815ed77886ee
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Nov 6 10:04:19 2016 -0800

    GRP-1409 - Add role=alert to the messages

 grouper-ui/webapp/grouperExternal/public/assets/js/grouper.js   | 8 ++++----
 grouper-ui/webapp/grouperExternal/public/assets/js/grouperUi.js | 2 +-
 2 files changed, 5 insertions(+), 5 deletions(-)
```

Issue #11: [GRP-1410](https://bugs.internet2.edu/jira/browse/GRP-1410): UI Accessibility Improvements: The bread crumb above the main content is not identified as a bread crumb to the screen reader, losing its value as a navigation tool for the screen reader user.

```
commit df9350af39b7dd8b6f8f41c234633da884d8d290
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Nov 6 10:32:42 2016 -0800

    GRP-1410 - Create a hidden header element for the breadcrumb

 grouper-ui/java/src/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiObjectBase.java | 1 +
 1 file changed, 1 insertion(+)
 
commit ff34124cf3eada597df60b5697fc4556a80375e5
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Dec 4 12:51:46 2016 -0800

    GRP-1410 - Add breadcrumb for UI accessibility

 grouper-ui/conf/grouperText/grouper.text.en.us.base.properties                              | 2 ++
 grouper-ui/java/src/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiObjectBase.java | 5 ++++-
 2 files changed, 6 insertions(+), 1 deletion(-)
 
commit 546da77e38a62eba9f06a5cebe35668d52a1cb74
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Wed Dec 7 07:29:53 2016 -0800

    GRP-1410 Escape xml before rendering the breadcrumb aria label

 grouper-ui/java/src/edu/internet2/middleware/grouper/grouperUi/beans/api/GuiObjectBase.java | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
```

Issue #12: [GRP-1431](https://bugs.internet2.edu/jira/browse/GRP-1431): Two links with label "More" appears on the same page

```
commit 7a674e24b22f0f8f0609668327c98fde9eb5065a
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Dec 4 13:39:00 2016 -0800

    GRP-1431 - Add aria-label property to More links

 grouper-ui/conf/grouperText/grouper.text.en.us.base.properties                              | 7 +++++++
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefContents.jsp                  | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefHeader.jsp                    | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefMoreActionsButtonContents.jsp | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefMoreTab.jsp                   | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefPrivilegeContents.jsp         | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupContents.jsp                                | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupHeader.jsp                                  | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp               | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupMoreTab.jsp                                 | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupPrivilegeContents.jsp                       | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivilegesContents.jsp     | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsGroupPrivilegesContents.jsp            | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsMembershipsContents.jsp                | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsStemPrivilegesContents.jsp             | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/index/index.jsp                                        | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/index/indexColumnMenu.jsp                              | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemHeader.jsp                                    | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemMoreActionsButtonContents.jsp                 | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemMoreTab.jsp                                   | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemPrivilegeContents.jsp                         | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectContents.jsp                            | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectMoreTab.jsp                             | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivilegesContents.jsp | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivilegesContents.jsp        | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivilegesContents.jsp         | 2 +-
 26 files changed, 32 insertions(+), 25 deletions(-)
 
commit 6bf84485149919b37ac012af851c64eab541590c
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Dec 4 13:47:33 2016 -0800

    GRP-1431 - Add aria-label property to Subject More links

 grouper-ui/conf/grouperText/grouper.text.en.us.base.properties | 3 ++-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectHeader.jsp | 2 +-
 2 files changed, 3 insertions(+), 2 deletions(-)
 
commit 46ba9f90877dd49d3b01789faeafd25c442bc63b
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Dec 4 14:35:12 2016 -0800

    GRP-1431 - Show different aria-label for More Actions links

 grouper-ui/conf/grouperText/grouper.text.en.us.base.properties                              | 15 ++++++++++-----
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefHeader.jsp                    |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefMoreActionsButtonContents.jsp |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupHeader.jsp                                  |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupMoreActionsButtonContents.jsp               |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemHeader.jsp                                    |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemMoreActionsButtonContents.jsp                 |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectHeader.jsp                              |  2 +-
 8 files changed, 17 insertions(+), 12 deletions(-)
 
commit 907b129f17dff9204e0ea1db47cdb03d80e069ac
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Dec 4 15:12:32 2016 -0800

    GRP-1431 - Add properties file changes

 grouper-ui/conf/grouperText/grouper.text.en.us.base.properties | 3 +++
 1 file changed, 3 insertions(+)

commit f5ca46b0caa6c17e6ee495a26a85b305f20c037f
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Dec 4 15:11:48 2016 -0800

    GRP-1431 - Show different aria label for memebrship and attribute name

 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefContents.jsp | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupContents.jsp               | 6 +++---
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/subjectContents.jsp           | 2 +-
 3 files changed, 5 insertions(+), 5 deletions(-)
```

Issue #13: [GRP-1432](https://bugs.internet2.edu/jira/browse/GRP-1432): Display of custom privilege choices are not announced

```
commit 56c795b3a6ca7b36a1f31049f7f5c50fe9984c8c
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Dec 4 15:55:36 2016 -0800

    GRP-1432 - Announce addition/deletion of Custom Privileges section from add memebers page

 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupHeader.jsp      |  6 +++---
 grouper-ui/webapp/grouperExternal/public/assets/js/grouperUi.js | 26 ++++++++++++++++++++++++++
 2 files changed, 29 insertions(+), 3 deletions(-)
 
commit e0b22ba777b746fb9d86b02245d0dc3230513eb5
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Dec 4 16:21:11 2016 -0800

    GRP-1432 - Announce privileges section for stems as well

 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupHeader.jsp      |  4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemHeader.jsp        |  6 +++---
 grouper-ui/webapp/grouperExternal/public/assets/js/grouperUi.js | 24 ++++++++++++++++--------
 3 files changed, 21 insertions(+), 13 deletions(-)
```

Issue #14: [GRP-1436](https://bugs.internet2.edu/jira/browse/GRP-1436): “Quick Links” is read as a button whereas it is actually an expandable list.

```
commit f24b2eab75c68caee11ef6a5d22e9f16c97920f6
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Wed Dec 7 08:10:43 2016 -0800

    GRP-1436 - Add aria attributes to Quick Links

 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefMoreActionsButtonContents.jsp | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/index/index.jsp                                        | 8 ++++++--
 2 files changed, 7 insertions(+), 3 deletions(-)
```

Issue# 15: [GRP-1437](https://bugs.internet2.edu/jira/browse/GRP-1437): UI Accessibility Improvements: The graphic after “Browse Folders” is not labeled and is read only as “clickable.”

```
commit a7a7d67509fe11eee8ed9b8228d615d6509af2c2
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Wed Dec 7 08:30:52 2016 -0800

    GRP-1437 - Add aria label to refresh folder browse icon and change hard coded english aria labels to property file

 grouper-ui/conf/grouperText/grouper.text.en.us.base.properties                      | 3 +++
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefPrivileges.jsp        | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupPrivileges.jsp                      | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivileges.jsp     | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsGroupPrivileges.jsp            | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsStemPrivileges.jsp             | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/index/index.jsp                                | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemPrivileges.jsp                        | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivileges.jsp | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivileges.jsp        | 2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivileges.jsp         | 2 +-
 11 files changed, 14 insertions(+), 11 deletions(-)
```

Issue# 16: [GRP-1442](https://bugs.internet2.edu/jira/browse/GRP-1442): UI Accessibility Improvements: The Recent Activity listing (defined as a table to the screen readers) on the Grouper home page has no meaningful labels

```
commit 83a0b16cb86e4e899ab94c94a311f147a44f0b59
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Mon Dec 12 07:43:25 2016 -0800

    GRP-1442 - Add table header to recent activity table on the home page

 grouper-ui/conf/grouperText/grouper.text.en.us.base.properties | 6 ++++++
 grouper-ui/webapp/WEB-INF/grouperUi2/index/indexMain.jsp       | 7 ++++++-
 2 files changed, 12 insertions(+), 1 deletion(-)
```

Issue# 17: [GRP-1478](https://bugs.internet2.edu/jira/browse/GRP-1478): UI Accessibility Improvements: Privileges interface not accessible to keyboard-only or screenreader users

```
commit 99476687e75ce79d86b978a6bb997d16edfdc92e
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sun Jan 29 00:36:07 2017 -0800

    GRP-1478 - Make the privileges checkboxes which are icons more accessible for screenreaders

 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefPrivilegeContents.jsp         | 7 ++++---
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupPrivilegeContents.jsp                       | 7 ++++---
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivilegesContents.jsp     | 7 ++++---
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsGroupPrivilegesContents.jsp            | 7 ++++---
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsStemPrivilegesContents.jsp             | 7 ++++---
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemPrivilegeContents.jsp                         | 7 ++++---
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivilegesContents.jsp | 7 ++++---
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivilegesContents.jsp        | 7 ++++---
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivilegesContents.jsp         | 7 ++++---
```

Issue# 18: [GRP-1482](https://bugs.internet2.edu/jira/browse/GRP-1482): UI Accessibility Improvements: The revised display of entity names is not announced after applying a filter to the entity list.

```
commit 03f7b8126509ff0fcaaaac2cd71e5606baa69343
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Fri Feb 3 23:19:03 2017 -0800

    GRP-1482 - Announce when the filter is applied and the results are updated

 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefPrivileges.jsp        | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/viewAttributeDef.jsp              | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupPrivileges.jsp                      | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivileges.jsp     | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsGroupPrivileges.jsp            | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsMemberships.jsp                | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsStemPrivileges.jsp             | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/viewGroup.jsp                            | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/index/myActivity.jsp                           | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/index/myFavorites.jsp                          | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/index/myServices.jsp                           | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroups.jsp                          | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroupsJoin.jsp                      | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/myGroups/myGroupsMemberships.jsp               | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/myStems/myStems.jsp                            | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/myStems/myStemsContainingAttributesImanage.jsp | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/myStems/myStemsContainingGroupsImanage.jsp     | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemPrivileges.jsp                        | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/viewStem.jsp                              | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivileges.jsp | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivileges.jsp        | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivileges.jsp         | 4 ++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/viewSubject.jsp                        | 4 ++--
 23 files changed, 46 insertions(+), 46 deletions(-)
```

Issue# 19: [GRP-1544](https://bugs.internet2.edu/jira/browse/GRP-1544): Fix accessibility issues

```
commit 261488c93bd258a66bce543e529c8ae1bdb74b29
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sat May 20 22:41:44 2017 -0700

    GRP-1544: Change the error message for accessibility

 grouper-ui/webapp/grouperExternal/public/assets/dojo/grouper/nls/grouperDojo_en-us.js | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)

commit 15fa67c9c91c030c43bd1b638d48f0453d7be2c8
Author: Vivek Sachdeva <erviveksachdeva@gmail.com>
Date:   Sat May 20 21:58:24 2017 -0700

    GRP-1544 : Fix accessibility issues

 grouper-ui/conf/grouperText/grouper.text.en.us.base.properties                              | 37 ++++++++++++++++++++++++++++++++++++-
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefContents.jsp                  |  5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/attributeDef/attributeDefPrivilegeContents.jsp         |  5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupContents.jsp                                |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupHeader.jsp                                  |  8 ++++----
 grouper-ui/webapp/WEB-INF/grouperUi2/group/groupPrivilegeContents.jsp                       |  5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsAttributeDefPrivilegesContents.jsp     |  5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsGroupPrivilegesContents.jsp            |  5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsMembershipsContents.jsp                |  5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/group/thisGroupsStemPrivilegesContents.jsp             |  5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/index/indexMain.jsp                                    |  2 +-
 grouper-ui/webapp/WEB-INF/grouperUi2/stem/stemPrivilegeContents.jsp                         |  5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsAttributeDefPrivilegesContents.jsp |  5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsGroupPrivilegesContents.jsp        |  5 +++--
 grouper-ui/webapp/WEB-INF/grouperUi2/subject/thisSubjectsStemPrivilegesContents.jsp         |  5 +++--
 grouper-ui/webapp/grouperExternal/public/assets/css/bootstrap.css                           | 15 ++++++++-------
 16 files changed, 83 insertions(+), 36 deletions(-)
```

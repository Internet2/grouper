---
title: "Programmatic access to Grouper - edit a group"
space: Grouper
pageId: 28549631
version: 6
lastUpdated: 2026-07-01T05:41:30.599Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549631/Programmatic+access+to+Grouper+-+edit+a+group
---

## Programmatic access to Grouper - edit a group

This class is used to programmatically edit a group. It navigates to the group, then clicks group actions and selects edit group.  
If group editing fields (groupDisplayExtension, groupExtension, or description) have been filled, they will be entered. Before  
confirming, it is made sure that the alternate Id path of the group is not updated. Finally, the group edit confirmation  
message is received

### Edit a group:

 GrouperUiBrowserGroupEdit grouperUiBrowserGroupEdit = new GrouperUiBrowserGroupEdit(grouperPage)  
.assignGroupToEditName("test:test22").assignGroupExtension("testeditedagain").assignDescription("this is the edited description").browse();

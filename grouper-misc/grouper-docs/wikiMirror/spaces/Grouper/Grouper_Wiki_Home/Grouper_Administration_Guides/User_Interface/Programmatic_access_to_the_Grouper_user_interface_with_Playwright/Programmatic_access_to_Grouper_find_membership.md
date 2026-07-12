---
title: "Programmatic access to Grouper - find membership"
space: Grouper
pageId: 28549666
version: 5
lastUpdated: 2026-07-01T05:41:25.742Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549666/Programmatic+access+to+Grouper+-+find+membership
---

## Programmatic access to Grouper - find membership

Class used to programmatically find a membership. It navigates to the group, then pages through the entity list until finding the checkbox with the custom attribute matching the subjects combo id. Then it sets the membershipFound boolean to true and breaks out of the paging loop.

### Find a membership by subject object:

 GrouperUiBrowserMembershipAdd grouperUiBrowserMembershipAdd = new GrouperUiBrowserMembershipAdd(page)  
.assignGroupToAddToName("test:test").assignSubjectIdentifier("test.subject.1").browse();

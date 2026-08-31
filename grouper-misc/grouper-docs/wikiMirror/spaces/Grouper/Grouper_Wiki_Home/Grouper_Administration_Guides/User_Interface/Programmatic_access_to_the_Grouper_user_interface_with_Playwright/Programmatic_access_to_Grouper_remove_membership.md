---
title: "Programmatic access to Grouper - remove membership"
space: Grouper
pageId: 28549625
version: 5
lastUpdated: 2026-07-01T05:41:31.486Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549625/Programmatic+access+to+Grouper+-+remove+membership
---

## Programmatic access to Grouper - remove membership

This class is used to programmatically remove a subject from a group.

### Remove subject with subjectId: "test.subject.1" to group with name: "test:test":

GrouperUiBrowserMembershipRemove grouperUiBrowserMembershipRemove = new GrouperUiBrowserMembershipRemove(page)  
 .assignGroupToRemoveFromName("test:test").assignSubjectId("test.subject.1").browse();

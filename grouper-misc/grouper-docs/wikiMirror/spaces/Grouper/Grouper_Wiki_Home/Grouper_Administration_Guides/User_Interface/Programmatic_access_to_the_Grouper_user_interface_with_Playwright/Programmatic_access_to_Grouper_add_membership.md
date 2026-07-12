---
title: "Programmatic access to Grouper - add membership"
space: Grouper
pageId: 28549619
version: 5
lastUpdated: 2026-07-01T05:41:33.964Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549619/Programmatic+access+to+Grouper+-+add+membership
---

## Programmatic access to Grouper - add membership

This class is used to programmatically add a subject to a group.

### Add subject with subjectId: "test.subject.1" to group with name: "test:test":

 GrouperUiBrowserMembershipAdd grouperUiBrowserMembershipAdd = new GrouperUiBrowserMembershipAdd(page)  
.assignGroupToAddToName("test:test").assignSubjectId("test.subject.1").browse();

---
title: "Programmatic access to Grouper - delete a group"
space: Grouper
pageId: 28549604
version: 4
lastUpdated: 2026-07-01T05:41:41.362Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549604/Programmatic+access+to+Grouper+-+delete+a+group
---

# Programmatic access to Grouper - delete a group

This class is used to programmatically delete a group.

### Delete group with name "test:test":

 GrouperUiBrowserGroupDelete grouperUiBrowserGroupDelete = new GrouperUiBrowserGroupDelete(page)  
.assignGroupToDeleteName("test:test").browse();

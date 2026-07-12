---
title: "Programmatic access to Grouper - view custom ui"
space: Grouper
pageId: 28549637
version: 6
lastUpdated: 2026-07-01T05:41:29.658Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549637/Programmatic+access+to+Grouper+-+view+custom+ui
---

## Programmatic access to Grouper - view custom ui

Run a custom Ui.

### Run Custom Ui:

 GrouperUiBrowserCustomUiView grouperUiBrowserCustomUiView = new GrouperUiBrowserCustomUiView(page)  
.assignConfigId("myCustomUiConfigId").browse();

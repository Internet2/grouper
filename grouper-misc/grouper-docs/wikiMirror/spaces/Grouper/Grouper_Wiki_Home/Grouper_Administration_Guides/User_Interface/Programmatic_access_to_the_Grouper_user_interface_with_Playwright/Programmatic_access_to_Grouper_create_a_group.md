---
title: "Programmatic access to Grouper - create a group"
space: Grouper
pageId: 28549592
version: 4
lastUpdated: 2026-07-01T05:41:43.342Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549592/Programmatic+access+to+Grouper+-+create+a+group
---

## Programmatic access to Grouper - create a group

This class is used to programmatically run a GSH template. This class can run templates on groups, stems, or in the GSH templates page in Miscellaneous.

Run Gsh template in misellaneous>gsh templates. Add desired input values. Store each of the error, info, and success message outputs as lists.

> GrouperUiBrowserTemplateRun grouperUiBrowserTemplateRun = new GrouperUiBrowserTemplateRun( grouperPage).assignGshTemplateConfigId("validateGrouper"). assignSecondsToWait(20).addInputValue("gsh_input_expectedVersion", "1.2.3"). addInputValue("gsh_input_textarea", "textAreaInput"). addInputValue("gsh_input_dropdown", "first"). addInputValue("gsh_input_password", "passwordInput").browse(); List messageErrors = rouperUiBrowserTemplateRun.getGrouperUiBrowserDaemonErrors(); List messageInfos = rouperUiBrowserTemplateRun.getGrouperUiBrowserDaemonErrors(); List messageSuccesses = rouperUiBrowserTemplateRun.getGrouperUiBrowserDaemonErrors();

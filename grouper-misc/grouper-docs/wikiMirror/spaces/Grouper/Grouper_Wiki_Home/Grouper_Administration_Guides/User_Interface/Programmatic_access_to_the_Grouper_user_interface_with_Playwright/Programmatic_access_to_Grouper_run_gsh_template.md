---
title: "Programmatic access to Grouper - run gsh template"
space: Grouper
pageId: 28549660
version: 6
lastUpdated: 2026-07-01T05:41:26.697Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28549660/Programmatic+access+to+Grouper+-+run+gsh+template
---

## Programmatic access to Grouper - run gsh template

This class is used to programmatically run a GSH template. This class can run templates on groups, stems, or in the GSH templates page.

### Run Gsh template in misellaneous>gsh templates. Add desired input values. Store each of the error, info, and success message outputs as lists:

GrouperUiBrowserTemplateRun grouperUiBrowserTemplateRun = new GrouperUiBrowserTemplateRun(  
 grouperPage).assignGshTemplateConfigId("validateGrouper")  
 .assignSecondsToWait(20)  
 .addInputValue("gsh_input_expectedVersion", "1.2.3")  
 .addInputValue("gsh_input_textarea", "textAreaInput")  
 .addInputValue("gsh_input_dropdown", "first")  
 .addInputValue("gsh_input_password", "passwordInput").browse()  
List<String> messageErrors = rouperUiBrowserTemplateRun.getGrouperUiBrowserDaemonErrors();  
List<String> messageInfos = rouperUiBrowserTemplateRun.getGrouperUiBrowserDaemonErrors();  
List<String> messageSuccesses = rouperUiBrowserTemplateRun.getGrouperUiBrowserDaemonErrors();

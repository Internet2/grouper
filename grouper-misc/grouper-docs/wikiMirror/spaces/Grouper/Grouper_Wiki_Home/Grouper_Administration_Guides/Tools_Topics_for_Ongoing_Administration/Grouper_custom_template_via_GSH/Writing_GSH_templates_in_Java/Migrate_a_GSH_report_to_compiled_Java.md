---
title: "Migrate a GSH report to compiled Java"
space: Grouper
pageId: 28555839
version: 2
lastUpdated: 2026-07-01T05:37:18.731Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555839/Migrate+a+GSH+report+to+compiled+Java
---

## Today vs compiled

A report whose type is GSH stores its Groovy body inline on the report config (the "GSH script" field). To convert: create a compiled GSH template (`templateType=report`, `templateMode=compiled`) whose body extends `GrouperTemplateReport` and implements `runReport`, then on the report config edit screen put its config id in the **GSH template config id (compiled)** field. When that field is set, the compiled template runs instead of the inline GSH script (keep the report type as GSH).

The report runtime is the same: populate `gshReportRuntime.getGrouperReportData()` with `setHeaders(List<String>)` and `setData(List<String[]>)`. The owning group/stem and subject are available on the runtime.

## Before (interpreted report body)

// gsh_builtin_gshReportRuntime is prepended GrouperReportData data = gsh_builtin_gshReportRuntime.getGrouperReportData(); List headers = new ArrayList(); headers.add("subjectId"); data.setHeaders(headers); // ... add rows ... data.setData(rows); 

## After (compiled GrouperTemplateReport)

package edu.institution.grouper.gsh; import java.util.ArrayList; import java.util.List; import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateReport; import edu.internet2.middleware.grouper.app.reports.GrouperReportData; import edu.internet2.middleware.grouper.app.reports.GshReportRuntime; public class StaleMembersReport extends GrouperTemplateReport { @Override public void runReport(GshReportRuntime gshReportRuntime) { GrouperReportData grouperReportData = gshReportRuntime.getGrouperReportData(); List<String> headers = new ArrayList<String>(); headers.add("subjectId"); headers.add("lastLogin"); grouperReportData.setHeaders(headers); List<String[]> rows = new ArrayList<String[]>(); // ... add rows of new String[] {subjectId, lastLogin} ... grouperReportData.setData(rows); } }

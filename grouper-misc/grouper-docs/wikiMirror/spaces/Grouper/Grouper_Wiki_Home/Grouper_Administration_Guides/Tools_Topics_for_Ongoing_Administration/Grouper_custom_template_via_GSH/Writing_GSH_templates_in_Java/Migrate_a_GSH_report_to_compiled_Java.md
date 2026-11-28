---
title: "Migrate a GSH report to compiled Java"
space: Grouper
pageId: 28555839
version: 3
lastUpdated: 2026-11-27T18:42:15.186Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555839/Migrate+a+GSH+report+to+compiled+Java
---

## Today vs compiled

A report whose type is GSH stores its Groovy body inline on the report config (the "GSH script" field). To convert: create a compiled GSH template (`templateType=report`, `templateMode=compiled`) whose body extends `GrouperTemplateReport` and implements `runReport`, then on the report config edit screen (keep the report type as GSH): in v7.10.0+, set "GSH source" to "Compiled GSH report template" and enter the template's config id in "GSH template config id (compiled)"; before v7.10.0, put the config id in "GSH template config id (compiled)", and it runs instead of the inline GSH script. In v7.10.0+ an inline GSH script can be at most 4000 characters, so a longer report must be a compiled template.

The report runtime is the same: populate `gshReportRuntime.getGrouperReportData()` with `setHeaders(List<String>)` and `setData(List<String[]>)`. The owning group/stem and subject are available on the runtime.

## Before (interpreted report body)

// gsh_builtin_gshReportRuntime is prepended GrouperReportData data = gsh_builtin_gshReportRuntime.getGrouperReportData(); List headers = new ArrayList(); headers.add("subjectId"); data.setHeaders(headers); // ... add rows ... data.setData(rows); 

## After (compiled GrouperTemplateReport)

package edu.institution.grouper.gsh; import java.util.ArrayList; import java.util.List; import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateReport; import edu.internet2.middleware.grouper.app.reports.GrouperReportData; import edu.internet2.middleware.grouper.app.reports.GshReportRuntime; public class StaleMembersReport extends GrouperTemplateReport { @Override public void runReport(GshReportRuntime gshReportRuntime) { GrouperReportData grouperReportData = gshReportRuntime.getGrouperReportData(); List<String> headers = new ArrayList<String>(); headers.add("subjectId"); headers.add("lastLogin"); grouperReportData.setHeaders(headers); List<String[]> rows = new ArrayList<String[]>(); // ... add rows of new String[] {subjectId, lastLogin} ... grouperReportData.setData(rows); } }

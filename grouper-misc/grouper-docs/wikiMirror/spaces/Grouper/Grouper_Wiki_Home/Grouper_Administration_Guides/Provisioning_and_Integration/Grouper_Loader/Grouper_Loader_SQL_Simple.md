---
title: "Grouper - Loader SQL Simple"
space: Grouper
pageId: 28548064
version: 7
lastUpdated: 2026-07-01T05:45:26.118Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548064/Grouper+-+Loader+SQL+Simple
---

## SQL Loader configuration

This is a brief overview of how to use the Simple SQL Loader.

### Configuring using the UI

**Step 1** - Create a group

**Step 2** - While inside group, select "Group actions" drop down and then "Loader"

**Step 3** - Select "Loader Actions" and "Edit loader configuration"

**Step 4** - Select "Yes, has loader configuration"

**Step 5** - Under "Source type" select "SQL"

**Step 6** - Loader type select "SQL_SIMPLE: the SQL query loads the members of this group"

**Step 7** - Select the database from the list that will be acting as your data source

**Step 8** - Paste in your SQL query.   
**Note**: your query will need to return one of the following: SUBJECT_ID or SUBJECT_IDENTIFIER or SUBJECT_ID_OR_IDENTIFIER. (SUBJECT_ID has the best performance, and SUBJECT_IDENTIFIER and SUBJECT_ID_OR_IDENTIFIER are slower since they require subject API lookups)

**Step 9** - Select Schedule type (Default is "CRON")

**Step 10**- Set the "Schedule"

**Step 11** - Set the "Priority" (Optional) - 5 is the default

**Step 12** - Require members in other group(s) (Optional) - comma separated list

**Step 13** - "Customize failsafe" (Optional) - default is "No, do not customize failsafe"

**Step 14** - Click "Save"

### **Configuring using GSH**

gs = GrouperSession.startRootSession()   
group = new GroupSave(gs).assignName("%path to Group (will create if it doesn't exist)%").assignCreateParentStemsIfNotExist(true).save()  
group.addType(GroupTypeFinder.find("grouperLoader"))  
group.setAttribute("grouperLoaderType", "SQL_SIMPLE")  
group.setAttribute("grouperLoaderScheduleType", "CRON")  
group.setAttribute("grouperLoaderQuartzCron", "%CRON schedule%")  
group.setAttribute("grouperLoaderDbName", "%Name of your Database%")  
group.setAttribute("grouperLoaderQuery", "%YOUR SQL QUERY%");  
edu.internet2.middleware.grouper.app.loader.GrouperLoaderType.scheduleLoads();

### Configuration Example in Table

| Attribute name | Value |
| --- | --- |
| Source type | SQL |
| Loader type | SQL_SIMPLE |
| Database name | jdbc |
| Query | SELECT 'jdbc' AS subject_source_id, subjectId AS subject_id FROM subject WHERE subjectId IN ('test.subject.0', 'test.subject.1', 'test.subject.2') |
| Schedule type | CRON |
| Cron schedule | 0 0 6 * * ? |

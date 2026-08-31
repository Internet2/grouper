---
title: "Grouper loader SQL group list example"
space: Grouper
pageId: 28554799
version: 10
lastUpdated: 2026-07-12T15:27:10.343Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554799/Grouper+loader+SQL+group+list+example
---

| Attribute name | Example Value | Default Value | Optional? | Note |
| --- | --- | --- | --- | --- |
| Source type | SQL |  |  |  |
| Loader type | SQL_GROUP_LIST |  |  |  |
| Database name | jdbc |  |  |  |
| Query | SELECT 'jdbc' AS subject_source_id, subjectId AS subject_id, 'test:testGroup1' AS group_name FROM subject UNION SELECT 'jdbc' AS subject_source_id, subjectId AS subject_id, 'test:testGroup2' AS group_name FROM subject |  |  |  |
| Schedule type | CRON |  |  |  |
| Cron schedule | 0 0 6 * * ? |  |  |  |
| Priority | 5 | 5 | Yes | Higher number = higher priority |
| Require members in other group(s) | stema:stemb:group1 |  | Yes | Users returned by query will be excluded if they are not in this group |
| Group query (metadata on groups) | See below (optional) |  | Yes |  |
| Groups like sql part | test: |  | Yes | Path to destination for groups created, required for the job to clean up groups/users that are no longer returned |
| Sync display names configuration | "Sync display name after base folder name" or "Levels from group to sync display name" |  | Yes | Adds additional field to the form asking for the "base folder path" or "levels from group" |
| Group type | addIncludeExclude |  | Yes |  |
| Customize failsafe | No | No | Yes | If set to yes it will give you a series of options for configuring [failsafes](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554941/Grouper+failsafes) |

Optional, if you want a group query, do something like this, and change the Query too

| Attribute name | Value |
| --- | --- |
| Query | SELECT 'jdbc' AS subject_source_id, subjectId AS subject_id, 'test:testGroup1_systemOfRecord' AS group_name FROM subject UNION SELECT 'jdbc' AS subject_source_id, subjectId AS subject_id, 'test:testGroup2_systemOfRecord' AS group_name FROM subject |
| Group query | SELECT 'test:testGroup1_systemOfRecord' AS group_name FROM subject WHERE subjectId = 'test.subject.0' UNION SELECT 'test:testGroup2_systemOfRecord' AS group_name FROM subject WHERE subjectId = 'test.subject.0' |
|  |  |

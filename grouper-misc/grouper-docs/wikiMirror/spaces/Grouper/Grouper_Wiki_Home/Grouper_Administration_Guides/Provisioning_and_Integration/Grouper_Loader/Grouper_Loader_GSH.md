---
title: "Grouper - Loader GSH"
space: Grouper
pageId: 28547778
version: 5
lastUpdated: 2026-07-01T05:46:12.481Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547778/Grouper+-+Loader+GSH
---

## 

## Option 1

This is a proposed API for a new GSH loader job type. Similar to the LDAP and SQL loader configuration, there will be a simple and a groups_list loader type. The simple type will load a single group with a set of subjects. The groups_list will return a list of group + subject combinations, and collate the results to sync the set of groups.

Grouper loader GSH example

| Attribute name | Example value | Description |
| --- | --- | --- |
| Source type | GSH | grouper memberships are from the results of a GSH script |
| Loader type | GSH_SIMPLE | the script manages either members of this group (GSH_SIMPLE) or members of multiple groups (GSH_GROUP_LIST) |
| Script | see below |  |
| Schedule type | CRON | Can be CRON (recommended) or START_TO_START_INTERVAL |
| Cron schedule | 0 0 6 * * ? | Quartz cron-like string (if CRON schedule type): Seconds Minutes Hours Day-of-Month Month Day-of-Week Year (optional field). |
| Require members in other group(s) | (none) |  |
| Schedule job | Yes, schedule and enable this job | if this job should immediately be scheduled and enabled |

```groovy
// these will be set for the incoming script
LoaderJobBean gsh_builtin_loaderJobBean
GshLoaderJobResults gsh_builtin_gshLoaderJobResults
GrouperSession session = gsh_builtin_grouperSession // same as gsh_builtin_loaderJobBean.getGrouperSession()

Hib3GrouperLoaderLog hib3GrouploaderLogOverall = gsh_builtin_loaderJobBean.getHib3GrouploaderLogOverall()

boolean hadSubjectErrors = false

Group group1 = new GroupSave().assignName("app:test:group1").assignCreateParentStemsIfNotExist(true).save()
Group group2 = new GroupSave().assignName("app:test:group2").assignCreateParentStemsIfNotExist(true).save()

['1000001', '1000002', '1000003', '1000004'].each { subjectId ->
    Subject subject = SubjectFinder.findByIdAndSource(subjectId, "demo", false)
    if (subject == null) {
        hib3GrouploaderLogOverall.appendJobMessage("Unable to find subject with id '${subjectId}'")
        hadSubjectErrors = true
    } else {
        GshLoaderJobResults.add(group1, subject)
        GshLoaderJobResults.add(group2, subject)
    }
}

if (hadSubjectErrors) {
    hib3GrouploaderLogOverall.setStatus("WARNING")
}
```

Note that, unlike the SQL loader type, groups resolved to Group objects in the results. This means that the script must manage group creation for any new groups that don't yet exist. An object cache can be used to speed up the process, storing groups in a HashMap after lookup or creation.

The result set in the corresponding GSH_SIMPLE script will not need a group, so results will be added with method `GshLoaderJobResults.add(subject)`.

## Option 2

Same as SQL pretty much, use the same attributes

| Attribute name | Example value | Description |
| --- | --- | --- |
| Source type | GSH | grouper memberships are from the results of a GSH script |
| Loader type | GSH_SIMPLE | the script manages either members of this group (GSH_SIMPLE) or members of multiple groups (GSH_GROUP_LIST) |
| Script | see below | store as config in grouper.properties, we can discuss how that is managed...  note: there is only one script input that handles group memberships and list of groups |
| All other options |  | same as SQL with exception of SQL query and SQL group query |

Output is exactly like SQL, let Grouper create groups, resolve subject, and manage memberships efficiently

```groovy
// these will be set for the incoming script
LoaderJobBean gsh_builtin_loaderJobBean
GshLoaderJobMembershipResults gsh_builtin_gshLoaderJobMembershipResults
GshLoaderJobMembershipResults gsh_builtin_gshLoaderJobGroupResults
GrouperSession session = gsh_builtin_grouperSession // same as gsh_builtin_loaderJobBean.getGrouperSession()

Hib3GrouperLoaderLog hib3GrouploaderLogOverall = gsh_builtin_loaderJobBean.getHib3GrouploaderLogOverall()  

// do some logic

gsh_builtin_gshLoaderJobMembershipResults.addMembershipRow(new GshLoaderMembershipRow().assignGroupName("a:b:c").assignSubjectId("123456").assignSourceId("mySubjectSourceId"));
gsh_builtin_gshLoaderJobMembershipResults.addMembershipRow(new GshLoaderMembershipRow().assignGroupName("a:b:d").assignSubjectId("123457").assignSourceId("mySubjectSourceId"));

gsh_builtin_gshLoaderJobGroupResults.addGroupRow(new GshLoaderGroupRow().assignGroupName("a:b:d").assignGroupDisplayName("A:B:My whatever group"));

```

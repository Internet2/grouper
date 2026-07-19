---
title: "Grouper Training - Use cases - Lesson 07: Notifications"
space: Grouper
pageId: 28544798
version: 34
lastUpdated: 2026-04-22T01:35:04.369Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544798/Grouper+Training+-+Use+cases+-+Lesson+07+Notifications
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

Assign this config first: Miscellaneous > External Systems > SMTP:

- Group UUIDs/names email allow: test:emailAddressAdmins

(add to existing with comma if something already there)

Create group: `test:emailAddressAdmins`

Add *dgilmore* to `test:emailAddressAdmins`

## Rule to email on membership adds

### Method 1 (new v5 UI feature)

Create group: `test:testNotifications`

Group actions > Rules

Rule pattern: Send email after new membership

Email addresses: `test:emailAddressAdmins@grouper, banderson@example.com`

Email subject: `Person was added to group ${groupName}`

Email body:

```
Hello, Just letting you know ${safeSubject.name} was added to the group ${groupDisplayExtension}. Regards.
```

Submit

Add dbrown to testNotifications

Check email in the Mail server (may need to wait up to a minute to show up)

> From: grouper@example.com  
> Subject: Person was added to group test:testNotifications  
> To: Dawn.Gilmore@example.com, banderson@example.com
> 
> Hello, Just letting you know Douglas Brown was added to the group testNotifications. Regards.

## Daily summary

Create new daemon: *testDailyEmail*

Daemon type: Notification job

Email type: summary

Population type: groupMembership

Name of group: `test:testNotifications`

Name of group to email summary: `test:emailAddressAdmins`

Email summary only if records/members exist: `true`

Template of email subject: `People who need training`

Template of email body:

```
<html>
hello ${subject_name},
 <br /><br />
This many people have issues: ${size(listOfRecordMaps)}
 <br /><br /><ul>
$$ for (var recordMap : listOfRecordMaps) {
<li>Record subject ID: ${recordMap.get('subject_description')} </li>
$$ }
</ul>
</html>
```

Search for job `OTHER_JOB_testDailyEmail` and *Run job now*.

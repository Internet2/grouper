---
title: "Grouper Training - Use cases - Lesson 07: Notifications"
space: Grouper
pageId: 28544798
version: 36
lastUpdated: 2026-11-18T19:13:55.170Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544798/Grouper+Training+-+Use+cases+-+Lesson+07+Notifications
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

In this lesson, anything you type is in a box with a copy button on the right. Buttons, menus, and labels to click or select are in double quotes.

## Learning objectives

- Send an email when someone is added to a group (rule)
- Send a daily summary email of group members (notification daemon)

## Hands on

### Allow email to a group

Grouper can email the members of a group (e.g. test:emailAddressAdmins@grouper), but only for groups listed in the SMTP external system.

1. In the left menu, click "Miscellaneous", then click "External systems".
2. On the row for "smtp", click "Actions" → "Edit details".
3. "Group UUIDs/names email allow": paste this. If the field already has a value, type a comma at the end of it first:
  
  
  ```
  test:emailAddressAdmins
  ```
4. Click "Submit".

### Create the email admins group

1. In the left menu, click "Browse folders", click "Root", then click the folder "test".
2. Click "Folder actions" → "Create new group".
3. "Group name":
  
  
  ```
  emailAddressAdmins
  ```
4. Click "Save". You land on the new group page "test:emailAddressAdmins".
5. Click "Add members".
6. In "Member name or ID", paste this, pick "Dawn Gilmore" from the list, and click "Add":
  
  
  ```
  dgilmore
  ```

## Rule to email on membership adds

### Create the group

1. In the left menu, click "Browse folders", click "Root", then click the folder "test".
2. Click "Folder actions" → "Create new group".
3. "Group name":
  
  
  ```
  testNotifications
  ```
4. Click "Save". You land on the new group page "test:testNotifications".

### Add the rule

1. On the group "test:testNotifications", click "Group actions" → "Rules".
2. Click "Rules actions" → "Add rule".
3. "Rule pattern": select "Send email after new membership".
4. "Email addresses":
  
  
  ```
  test:emailAddressAdmins@grouper, banderson@example.edu
  ```
5. "Email subject":
  
  
  ```
  Person was added to group ${groupName}
  ```
6. "Email body":
  
  
  ```
  Hello, Just letting you know ${safeSubject.name} was added to the group ${groupDisplayExtension}. Regards.
  ```
7. Click "Save".

### Test the rule

1. Go to the group "test:testNotifications" and click "Add members".
2. In "Member name or ID", paste this, pick "Douglas Brown" from the list, and click "Add":
  
  
  ```
  dbrown
  ```
3. Check the email in the mail server (open it from the GTE jump page). It can take up to a minute to show up.

The email looks like this:

> From: grouper@internet2.edu.invalid  
> Subject: Person was added to group test:testNotifications  
> To: Dawn.Gilmore@mock.edu.invalid, banderson@example.edu
> 
> Hello, Just letting you know Douglas Brown was added to the group testNotifications. Regards.

## Daily summary

### Create the notification daemon

1. In the left menu, click "Miscellaneous", then click "Daemon jobs".
2. Click "Daemon actions" → "Add daemon".
3. "Config id":
  
  
  ```
  testDailyEmail
  ```
4. "Daemon type": select "Notification job".
5. "Email type": select "summary".
6. "Population type": select "groupMembership".
7. "Name of group":
  
  
  ```
  test:testNotifications
  ```
8. "Name of group to email summary":
  
  
  ```
  test:emailAddressAdmins
  ```
9. "Email summary only if records/members exist": select "true".
10. "Template of email subject":
  
  
  ```
  People who need training
  ```
11. "Template of email body":
  
  
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
12. Click "Submit".

### Run the daemon

1. In the left menu, click "Miscellaneous", then click "Daemon jobs".
2. Type this in the filter box and click "Apply filter":
  
  
  ```
  OTHER_JOB_testDailyEmail
  ```
3. On the row for OTHER_JOB_testDailyEmail, click "Job actions" → "Run job now".
4. Check the email in the mail server. Dawn Gilmore gets a summary listing the members of test:testNotifications.

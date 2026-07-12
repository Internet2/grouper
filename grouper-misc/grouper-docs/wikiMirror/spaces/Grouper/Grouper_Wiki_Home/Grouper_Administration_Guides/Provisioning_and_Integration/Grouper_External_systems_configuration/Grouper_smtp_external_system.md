---
title: "Grouper smtp external system"
space: Grouper
pageId: 28548200
version: 9
lastUpdated: 2026-07-01T05:45:04.940Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548200/Grouper+smtp+external+system
---

This is updated for 2.5.47+. See the API page: [GrouperShell (gsh) Email smtp (GrouperEmail)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548181/GrouperShell+gsh+Email+smtp+GrouperEmail)

Several features of Grouper use mail. You need to have an SMTP service to connect Grouper to. There is an external system for SMTP on the external systems screen

You cannot add a new SMTP external system since only one can exist and it already exists.

You cannot delete the SMTP external system since it can only be disabled.

## Edit the SMTP settings

Generally you should configure at least this

| Config file | Property name | Description |
| --- | --- | --- |
| grouper.properties | **mail.smtp.server** | the SMTP server that will accept mail sent to the user, set to "testing" if you want to log instead of send |
| grouper.properties | **mail.from.address** | default email address where mail from grouper will come from |
| grouper.properties | **mail.smtp.user** | Optional. username used with authenticate with the smtp server |
| grouper.properties | **mail.smtp.pass** | Optional. password associated with **mail.smtp.user** |

## Test smtp

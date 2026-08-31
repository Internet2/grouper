---
title: "GrouperShell (gsh) Email smtp (GrouperEmail)"
space: Grouper
pageId: 28548181
version: 7
lastUpdated: 2026-07-12T15:26:51.820Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548181/GrouperShell+gsh+Email+smtp+GrouperEmail
---

Use this utility to send email from Grouper. Many of these methods are new as of v2.5.47+. The original "set" methods have been there since v1.4+

Configured from the smtp external system: [Grouper smtp external system](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548200/Grouper+smtp+external+system)

Unlike most other method chaining classes, you need to call assignRunAsRoot(true) before adding subject and group lookups if you dont want to check security

Sample call to send an email:

> new GrouperEmail().setTo("email@domain.com").setBody("email body").setSubject("email subject").send();

Send an email to a subject:

> new GrouperEmail().assignRunAsRoot(true).addSubjectIdentifierToSendTo("mySourceId", "someNetId").setBody("email body").setSubject("email subject").send();

Send an email to a group by name:

> new GrouperEmail().assignRunAsRoot(true).addGroupNameToSendTo("a:b:c", true).setBody("email body").setSubject("email subject").send();

You need to configure email address in your person subject source to send to subjects

At least one "to" address is required.

To debug emails, set debug to true in the smtp external system, and set the log4j.properties entry:

> log4j.logger.edu.internet2.middleware.grouper.util.GrouperEmail = DEBUG

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/util/GroupEmail.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/util/GrouperEmail.html)

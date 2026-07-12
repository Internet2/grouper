---
title: "Grouper email notifications"
space: GrIntDev
pageId: 48792812
version: 6
lastUpdated: 2026-07-12T07:01:17.112Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792812/Grouper+email+notifications
---

Note, you can send an email to group:name@grouper, and if that is in the allow list in grouper.properties, it will send to members of the group

```
# comma separated group name and uuid's to be allow email addresses to dereference.
# for instance: a:b:c@grouper, def345@grouper<br />
# If a configuration enters in one of those email addresses, and it is in this allow list, then
# dereference the group and member and send email to their individual email addresses.  Note that 
# groups in this list can have their members discovered so treat their membership as non private.
# using the uuid@grouper gives a little bit of obscurity since the uuid of the group needs to be known
# is it is still security by obscurity which is not true security.  There is a max of 100 members it will
# send to
# {valueType: "string", multiple: true}
mail.smtp.groupUuidAndNameEmailDereferenceAllow =

```

In 2.5.0 patches, improve the Grouper email API and features. Here is a list of things we can do

1. Allow batched emails (store in database and send later, then delete)
2. Daemon to send batched emails
3. Show emails from database to admins
4. User preferences about batching
5. Email from UI for group
6. Email admins (or other priv) from UI for group
7. Allow emails to members / admins / privs from WS
8. Allow access to view messages from UI for user
9. Register for notifications on groups / folders
10. Have better grouping for attestation / reports / etc so that comma separated email addresses are not used, but people with subject attributes (or listserv addresses
11. Use new Grouper email api features like bcc
12. Have an admin UI to configure email servers

---
title: "Grouper Loader LDAP simple from hasMember"
space: Grouper
pageId: 28554333
version: 5
lastUpdated: 2026-07-01T05:40:40.325Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554333/Grouper+Loader+LDAP+simple+from+hasMember
---

If you need to dereference a simple LDAP loader group, and you have an LDAP user hasMember attribute you can use that in your filter and retrieve users who have that attribute and value. In this case the "hasMember" attribute is named "description".

Here is one user

Here is the filter

This is the loader config

Run the loader and see two users

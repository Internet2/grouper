---
title: "GrouperShell (gsh) ldap session (LdapSessionUtils)"
space: Grouper
pageId: 28547785
version: 4
lastUpdated: 2026-07-01T05:46:11.498Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547785/GrouperShell+gsh+ldap+session+LdapSessionUtils
---

Use this class to establish ldap session

Sample call

> LdapSessionUtils.ldapSession().list("personLdap", "ou=Groups,dc=example,dc=edu", LdapSearchScope.SUBTREE_SCOPE, "(objectClass=groupOfNames)", GrouperUtil.toArray(GrouperUtil.toList("objectClass", "cn", "member", "businessCategory"), String.class), null);

## Options

Java docs: [https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/ldap/LdapSessionUtils.html](https://software.internet2.edu/grouper/doc/2.5.x/grouper/apidocs/edu/internet2/middleware/grouper/ldap/LdapSessionUtils.html)

---
title: "Grouper Training - Loader - Lesson: LDAP list of groups loader"
space: Grouper
pageId: 28544460
version: 11
lastUpdated: 2026-07-12T15:26:21.658Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544460/Grouper+Training+-+Loader+-+Lesson+LDAP+list+of+groups+loader
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

- Navigate to ref folder
- Create new group:
  
  
  ```
  ldapGroupsConfig
  ```
- Group description:
  
  
  ```
  Config group for an LDAP list of groups job to sync LDAP groups from ou=groups to ref:ldapLoadedGroups
  ```
- Group actions → Loader
- Loader actions → Edit loader configuration
- Source type: LDAP
- Loader type: LDAP_LIST_OF_GROUPS
- Server ID: demo
- LDAP Filter:
  
  
  ```
  (objectClass=groupOfNames)
  ```
- Subject attribute name:
  
  
  ```
  member
  ```
- Base DN:
  
  
  ```
  ou=groups,dc=internet2,dc=edu
  ```
- Schedule:
  
  
  ```
  13 17 * * * ?
  ```
- Subject source ID: eduLDAP
- Subject lookup type: subjectIdentifier
- Search scope: ONE_LEVEL
- Subject expression
  
  
  ```
  ${loaderLdapElUtils.convertDnToSpecificValue(subjectId)}
  ```
- Extra LDAP attributes: cn
- Groups SQL 'like' configuration
  
  
  ```
  ref:ldapLoadedGroups:%
  ```
- Group name expression:
  
  
  ```
  ref:ldapLoadedGroups:${groupAttributes['cn']}
  ```
- Group description expression
  
  
  ```
  Group automatically sync'ed from LDAP
  ```
- Run diagnostics
- Schedule loader process
- Run job
- View loader logs
- See group(s) and users
- Run job again

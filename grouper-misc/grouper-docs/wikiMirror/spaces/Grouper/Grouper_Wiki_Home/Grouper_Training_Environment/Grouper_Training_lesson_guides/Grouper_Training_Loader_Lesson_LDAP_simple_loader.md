---
title: "Grouper Training - Loader - Lesson: LDAP simple loader"
space: Grouper
pageId: 28544475
version: 7
lastUpdated: 2026-07-12T15:26:22.062Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544475/Grouper+Training+-+Loader+-+Lesson+LDAP+simple+loader
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

- Navigate to ref folder
- Create new group:   
  baLastName
- Group description:
  
  
  ```
  People whose name starts with ba (case insensitive)
  ```
- Group actions → Loader
- Loader actions → Edit loader configuration
- Source type: LDAP
- Loader type: LDAP_SIMPLE
- Server ID: demo
- LDAP Filter:
  
  
  ```
  (&(objectClass=person)(sn=ba*))
  ```
- Subject attribute name:
  
  
  ```
  employeeNumber
  ```
- Base DN:
  
  
  ```
  ou=people,dc=internet2,dc=edu
  ```
- Schedule:
  
  
  ```
  41 34 * * * ?
  ```
- Subject source ID: eduLDAP
- Subject lookup type: subjectId
- Search scope: ONE_LEVEL
- Run diagnostics
- Schedule loader process
- Run job
- View loader logs
- See users
- Run job again

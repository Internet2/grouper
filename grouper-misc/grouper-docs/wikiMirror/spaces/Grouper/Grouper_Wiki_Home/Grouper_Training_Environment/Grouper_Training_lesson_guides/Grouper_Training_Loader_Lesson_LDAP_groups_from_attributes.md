---
title: "Grouper Training - Loader - Lesson: LDAP groups from attributes"
space: Grouper
pageId: 28544435
version: 15
lastUpdated: 2026-07-12T15:26:20.780Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544435/Grouper+Training+-+Loader+-+Lesson+LDAP+groups+from+attributes
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

Create Group: ref:lastNameLoaderConfig

Group actions > Loader > Edit loader configuration

> Loader: Yes
> 
> Source type: LDAP
> 
> Loader type: LDAP_GROUPS_FROM_ATTRIBUTES
> 
> Server ID: demo
> 
> Filter: (objectClass=person)
> 
> Subject attribute name: employeeNumber
> 
> Search base DN: ou=people,dc=internet2,dc=edu
> 
> Schedule: 43 37 * * * ?
> 
> Subject source id: eduLDAP
> 
> Subject lookup type: subjectId
> 
> Group attribute name: sn
> 
> Groups SQL 'like' configuration: ref:lastName:%
> 
> Group name expression: ref:lastName:${groupAttribute}

Loader actions > Loader diagnostics > Run loader diagnostics

Loader actions > Schedule loader process

Loader actions > Run loader process to sync group

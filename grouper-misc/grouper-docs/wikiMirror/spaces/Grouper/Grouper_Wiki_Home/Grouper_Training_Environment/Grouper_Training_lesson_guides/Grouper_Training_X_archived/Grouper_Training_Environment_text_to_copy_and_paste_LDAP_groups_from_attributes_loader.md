---
title: "Grouper Training Environment - text to copy and paste - LDAP groups from attributes loader"
space: Grouper
pageId: 28547594
version: 4
lastUpdated: 2025-03-16T23:38:32.326Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28547594/Grouper+Training+Environment+-+text+to+copy+and+paste+-+LDAP+groups+from+attributes+loader
---

- Navigate to ref folder
- Create new group:
  
  
  ```
  ldapLastNameGroupsConfig
  ```
- Group description:
  
  
  ```
  Config group for an LDAP groups from attributes job to sync LDAP users from ou=people to ref:ldapLastNames based on the person's lower-case last name if the name starts with 'a' or 'b'
  ```
- Group actions → Loader
- Loader actions → Edit loader configuration
- Source type: LDAP
- Loader type: LDAP_GROUPS_FROM_ATTRIBUTES
- Server ID: demo
- LDAP Filter:
  
  
  ```
  (objectClass=person)
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
  46 39 * * * ?
  ```
- Subject source ID: eduLDAP
- Subject lookup type: subjectId
- Search scope: ONE_LEVEL
- Attribute filter
  
  
  ```
  ${attributeValue.toLowerCase().startsWith('a') || attributeValue.toLowerCase().startsWith('b')}
  ```
- Group attribute name: sn
- Groups SQL 'like' configuration
  
  
  ```
  ref:ldapLastNames:%
  ```
- Group name expression:
  
  
  ```
  ref:ldapLastNames:${groupAttribute.toLowerCase()}
  ```
- Group description expression
  
  
  ```
  Group based on lower case last name automatically sync'ed from LDAP
  ```
- Run diagnostics
- Schedule loader process
- Run job
- View loader logs
- See groups in the ref:ldapLastNames folder
- Run job again, see how long it takes to run

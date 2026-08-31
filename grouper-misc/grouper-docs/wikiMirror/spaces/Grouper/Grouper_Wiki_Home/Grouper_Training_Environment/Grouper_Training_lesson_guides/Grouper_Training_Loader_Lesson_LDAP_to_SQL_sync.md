---
title: "Grouper Training - Loader - Lesson: LDAP to SQL sync"
space: Grouper
pageId: 28544389
version: 12
lastUpdated: 2026-07-12T15:26:18.022Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544389/Grouper+Training+-+Loader+-+Lesson+LDAP+to+SQL+sync
---

**Getting started**

[Connect to your VM](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543735/Connecting+to+the+Grouper+Training+Environment+VM)

**Lesson steps**

Base DN

```
ou=people,dc=internet2,dc=edu
```

LDAP filter for browser

```
givenName=A*
```

LDAP filter for loader

```
(givenName=A*)
```

Attributes

```
employeeNumber,givenName
```

Create table

```
create table ldap_a_names
(employee_number varchar(20) not null,
 first_name varchar(100) not null,
 CONSTRAINT ldap_a_names_pk
   PRIMARY KEY (employee_number));
```

Table name

```
ldap_a_names
```

Columns and attributes

```
employee_number -> employeeNumber
first_name -> givenName
```

SQL query

```
select concat('ref:iam:aNames:', first_name) as group_name, employee_number as subject_id, 'eduLDAP' as subject_source_id from ldap_a_names
```

Cron

```
17 28 6 * * ?
```

Groups like sql part

```
ref:iam:aNames:%
```

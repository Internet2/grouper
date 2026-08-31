---
title: "LDAP to SQL sync simple example"
space: Grouper
pageId: 28554458
version: 2
lastUpdated: 2026-07-01T05:40:23.829Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554458/LDAP+to+SQL+sync+simple+example
---

## Identify data to sync to SQL

In this case we want a user attribute as employeeID and an attribute value. This is the free LDAP browser Apache Directory Studio

LDAP data

## Create a table

Note, in this case we want this in a different database from our Grouper database, and its a different database vendor as well (oracle vs postgres). It is already an external system

```
CREATE TABLE PENN_KITE_CENTER_OVERRIDE
(
  PENN_ID               VARCHAR2(8 CHAR)        NOT NULL,
  CENTER_CODE_OVERRIVE  VARCHAR2(2 CHAR)        NOT NULL
);

CREATE UNIQUE INDEX PENN_KITE_CENTER_OVERRIDE_PK ON PENN_KITE_CENTER_OVERRIDE
(PENN_ID);

ALTER TABLE PENN_KITE_CENTER_OVERRIDE ADD (
  CONSTRAINT PENN_KITE_CENTER_OVERRIDE_PK
  PRIMARY KEY
  (PENN_ID)
  USING INDEX PENN_KITE_CENTER_OVERRIDE_PK);

```

## Configure the Grouper LDAP to SQL daemon

Note, we already have an LDAP external system to this AD

## Run the daemon

It took half a second to load 2000 records

```
dbConnection: pennCommunity, baseDn: OU=People,OU=UnivOfPennsylvania,DC=kite,DC=upenn,DC=edu, filter: (&(extensionattribute3=*)(employeeID=*)), ldapConnection: pennKiteAd, numberOfColumns: 2, searchScope: SUBTREE_SCOPE, tableName: PENN_KITE_CENTER_OVERRIDE, extraAttributes: null, dbRows: 0, dbUniqueKeys: 0, ldapRecords: 2614, deletesCount: 0, deletesMillis: 13, insertsCount: 2614, insertsMillis: 163, updatesCount: 0, updatesMillis: 7
```

## See the data in the table

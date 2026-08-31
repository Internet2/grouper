---
title: "LDAP to SQL sync attribute example"
space: Grouper
pageId: 28555342
version: 2
lastUpdated: 2026-07-01T05:38:28.418Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555342/LDAP+to+SQL+sync+attribute+example
---

This is in Grouper v2.6.1+.

## Identify data to sync to SQL

In this case we want users. The single valued attributes are the uid, dn, and a sample expression (date). The multi-valued attributes are: objectClasses and uids. This is the free LDAP browser Apache Directory Studio

## Create tables

Note, in this case we want this in a different database from our Grouper database, and its a different database vendor as well (oracle vs postgres). It is already an external system. This is hsql but you can create in any database

```
CREATE TABLE TESTGROUPER_LDAPSYNC (
	THE_DN VARCHAR(200) NOT NULL,
	CN VARCHAR(200),
	THE_DATE DATE,
	CONSTRAINT SYS_PK_86204 PRIMARY KEY (THE_DN)
);
CREATE UNIQUE INDEX SYS_IDX_SYS_PK_86204_86205 ON PUBLIC.PUBLIC.TESTGROUPER_LDAPSYNC (THE_DN);

```

The attribute value table must have these three columns with varchar types (size it for your data). This table is synced from the one and only one ldap-to-sql job

```
CREATE TABLE PUBLIC.PUBLIC.TESTGROUPER_LDAPSYNC_ATTR (
	LDAP_ID VARCHAR(200) NOT NULL,
	ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
	ATTRIBUTE_VALUE VARCHAR(200)
);
CREATE INDEX TESTGROUPER_LDAPSYNC_ATTR_LDAP_ID_IDX ON PUBLIC.PUBLIC.TESTGROUPER_LDAPSYNC_ATTR (LDAP_ID,ATTRIBUTE_NAME);
```

## Configure the Grouper LDAP to SQL daemon

Note, we already have an LDAP external system to this LDAP

grouper-loader.properties

```
otherJob.ldapToSqlTest.class = edu.internet2.middleware.grouper.app.ldapToSql.LdapToSqlSyncDaemon
otherJob.ldapToSqlTest.ldapSqlAttribute.0.ldapName = dn
otherJob.ldapToSqlTest.ldapSqlAttribute.0.ldapTranslationType = ldapAttribute
otherJob.ldapToSqlTest.ldapSqlAttribute.0.sqlColumn = the_dn
otherJob.ldapToSqlTest.ldapSqlAttribute.0.uniqueKey = true
otherJob.ldapToSqlTest.ldapSqlAttribute.1.ldapName = uid
otherJob.ldapToSqlTest.ldapSqlAttribute.1.ldapTranslationType = ldapAttribute
otherJob.ldapToSqlTest.ldapSqlAttribute.1.sqlColumn = cn
otherJob.ldapToSqlTest.ldapSqlAttribute.2.ldapTranslationType = translation
otherJob.ldapToSqlTest.ldapSqlAttribute.2.sqlColumn = the_date
otherJob.ldapToSqlTest.ldapSqlAttribute.2.translation = ${'2015-04-22 00:00:00.0'}
otherJob.ldapToSqlTest.ldapSqlBaseDn = ou=People,dc=example,dc=edu
otherJob.ldapToSqlTest.ldapSqlDbConnection = grouper
otherJob.ldapToSqlTest.ldapSqlFilter = (uid=*)
otherJob.ldapToSqlTest.ldapSqlHasMultiValuedTable = true
otherJob.ldapToSqlTest.ldapSqlIdColumn = the_dn
otherJob.ldapToSqlTest.ldapSqlLdapConnection = personLdap
otherJob.ldapToSqlTest.ldapSqlMultiValuedAttributes = objectClass, uid
otherJob.ldapToSqlTest.ldapSqlMultiValuedTableName = testgrouper_ldapsync_attr
otherJob.ldapToSqlTest.ldapSqlNumberOfAttributes = 3
otherJob.ldapToSqlTest.ldapSqlSearchScope = SUBTREE_SCOPE
otherJob.ldapToSqlTest.ldapSqlTableName = testgrouper_ldapsync
otherJob.ldapToSqlTest.quartzCron = 0 03 5 * * ?

```

## Run the daemon

It took half a second to load 14000 records

```
dbConnection: grouper, baseDn: ou=People,dc=example,dc=edu, filter: (uid=*), ldapConnection: personLdap, numberOfColumns: 3, searchScope: SUBTREE_SCOPE, tableName: testgrouper_ldapsync, hasMultiValuedTable: true, multiValuedTableName: testgrouper_ldapsync_attr, multiValuedIdColumn: the_dn, multiValuedAttributes: objectClass, uid, extraAttributes: null, dbRows: 0, dbUniqueKeys: 0, dbMultiValuedAttributes: 0, ldapRecords: 2000, ldapMultiValuedAttributes: 12000, deletesCount: 0, deletesMillis: 0, insertsCount: 14000, insertsMillis: 120, updatesCount: 0, updatesMillis: 0, insertsIntendedCount: 24000
```

## See the data in the tables

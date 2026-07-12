---
title: "LDAP to SQL sync"
space: Grouper
pageId: 28548344
version: 8
lastUpdated: 2026-07-01T05:44:43.817Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548344/LDAP+to+SQL+sync
---

Sync data from an LDAP filter to a database table (v2.5.55+)

You can sync to one table for single valued attributes or to two tables for multi-valued attributes (v2.6.1+).

The data will be type-case to the type of the database columns. Strings, numbers, and dates are supported. For the attribute table, strings will be used.

## Configure LDAP to SQL sync

As a Grouper admin go to Miscellaneous → Daemon jobs, add a new daemon job of type LDAP to SQL sync

## See the SQL table with the data

## Daemon logs (from UI)

```
dbConnection: grouper, baseDn: ou=Groups,dc=example,dc=edu, filter: (objectClass=groupOfUniqueNames2), ldapConnection: personLdap, numberOfColumns: 3, searchScope: SUBTREE_SCOPE, tableName: testgrouper_ldapsync, extraAttributes: null, dbRows: 1, dbUniqueKeys: 1, ldapRecords: 0, deletesCount: 1, deletesMillis: 661, insertsCount: 0, insertsMillis: 1, updatesCount: 0, updatesMillis: 0
```

## Sample config (grouper-loader.properties)

```
otherJob.ldapToSqlTest.class = edu.internet2.middleware.grouper.app.ldapToSql.LdapToSqlSyncDaemon
otherJob.ldapToSqlTest.ldapSqlAttribute.0.ldapName = dn
otherJob.ldapToSqlTest.ldapSqlAttribute.0.sqlColumn = the_dn
otherJob.ldapToSqlTest.ldapSqlAttribute.0.uniqueKey = true
otherJob.ldapToSqlTest.ldapSqlAttribute.1.ldapName = cn
otherJob.ldapToSqlTest.ldapSqlAttribute.1.sqlColumn = cn
otherJob.ldapToSqlTest.ldapSqlAttribute.2.sqlColumn = the_date
otherJob.ldapToSqlTest.ldapSqlAttribute.2.translation = ${'2015-04-22 00:00:00.0'}
otherJob.ldapToSqlTest.ldapSqlBaseDn = ou=Groups,dc=example,dc=edu
otherJob.ldapToSqlTest.ldapSqlDbConnection = grouper
otherJob.ldapToSqlTest.ldapSqlFilter = (objectClass=groupOfUniqueNames)
otherJob.ldapToSqlTest.ldapSqlLdapConnection = personLdap
otherJob.ldapToSqlTest.ldapSqlNumberOfAttributes = 3
otherJob.ldapToSqlTest.ldapSqlSearchScope = SUBTREE_SCOPE
otherJob.ldapToSqlTest.ldapSqlTableName = testgrouper_ldapsync
otherJob.ldapToSqlTest.quartzCron = 0 03 5 * * ?
```

## Sample JEXL

Note: these configs should not have the EL checkbox checked since they shouldnt be evaluated by the configuration engine

#### Do a regex from a JEXL script

LDAP attribute "somethingPacked" has value: {fruit=apple}:{color=red}:{flavor=sweet}

This JEXL will return "Red". Note: java regex curlies need to be escaped with backslash for some reason

```
${var theMatcher = java.util.regex.Pattern.compile('^.*\{color=([^}]*)\}.*$').matcher(ldapAttribute__somethingPacked); theMatcher.matches() ? theMatcher.group(1) : null}
```

If 2.6.0+ you can use:

```
${edu.internet2.middleware.grouper.util.GrouperUtil.regexGroup('^.*\{color=([^}]*)\}.*$', ldapAttribute__somethingPacked)}
```

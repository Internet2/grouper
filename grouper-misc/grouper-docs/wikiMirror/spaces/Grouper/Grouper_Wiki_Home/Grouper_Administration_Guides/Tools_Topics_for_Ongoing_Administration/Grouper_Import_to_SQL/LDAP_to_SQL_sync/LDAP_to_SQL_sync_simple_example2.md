---
title: "LDAP to SQL sync simple example2"
space: Grouper
pageId: 28554659
version: 1
lastUpdated: 2021-09-16T17:53:57.578Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554659/LDAP+to+SQL+sync+simple+example2
---

## Look at LDAP at data to get. Lets get uid and a translation

## Make a SQL table

## Configure the Grouper LDAP to SQL daemon

Note, we already have an LDAP external system to this LDAP

## Run job

## Config

```
otherJob.ldapToSqlDemo2.class = edu.internet2.middleware.grouper.app.ldapToSql.LdapToSqlSyncDaemon
otherJob.ldapToSqlDemo2.ldapSqlAttribute.0.ldapName = uid
otherJob.ldapToSqlDemo2.ldapSqlAttribute.0.sqlColumn = uid
otherJob.ldapToSqlDemo2.ldapSqlAttribute.0.uniqueKey = true
otherJob.ldapToSqlDemo2.ldapSqlAttribute.1.sqlColumn = email_address
otherJob.ldapToSqlDemo2.ldapSqlAttribute.1.translation = ${ldapAttribute__uid + '@example.com'}
otherJob.ldapToSqlDemo2.ldapSqlBaseDn = ou=People,dc=example,dc=edu
otherJob.ldapToSqlDemo2.ldapSqlDbConnection = grouper
otherJob.ldapToSqlDemo2.ldapSqlFilter = (&(uid=*)(objectClass=person))
otherJob.ldapToSqlDemo2.ldapSqlLdapConnection = personLdap
otherJob.ldapToSqlDemo2.ldapSqlNumberOfAttributes = 2
otherJob.ldapToSqlDemo2.ldapSqlSearchScope = SUBTREE_SCOPE
otherJob.ldapToSqlDemo2.ldapSqlTableName = test_ldap
otherJob.ldapToSqlDemo2.quartzCron = 0 03 5 * * ?
```

## See data in database

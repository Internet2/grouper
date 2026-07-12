---
title: "Copy data from ldap to sql with grouper api"
space: Grouper
pageId: 28544561
version: 5
lastUpdated: 2021-09-16T17:10:15.496Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544561/Copy+data+from+ldap+to+sql+with+grouper+api
---

**This is an****outdated wiki please see [this instead](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548344/LDAP+to+SQL+sync)**

```
// add an ldap connection in grouper-loader.properties
ldap.personLdap.url = ldap://ldap.andrew.cmu.edu:389/dc=cmu,dc=edu

// add a table in the grouper database
CREATE TABLE some_test (
    uid varchar(255) PRIMARY KEY
);

// run a program to sync from ldap to sql (will run in gsh or java).  
// Note, in java you need to run GrouperStartup.startup() first, in GSH you dont need to do that

Set uidsInLdapSet = new HashSet(LdapSessionUtils.ldapSession().list(String.class, "personLdap", null, LdapSearchScope.SUBTREE_SCOPE, "(cmuAndrewId=hanq*)", "uid")); 

Set uidsInDatabaseSet = new HashSet(new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("grouper").sql("select uid from some_test").selectList(String.class));

Set uidsToDelete = new HashSet(uidsInDatabaseSet);
uidsToDelete.removeAll(uidsInLdapSet);
System.out.println("deletions: " + uidsToDelete.size());
List batchBindVars = new ArrayList();
for (Object uidObject : uidsToDelete) { batchBindVars.add(GrouperUtil.toList(uidObject));}
new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("grouper").sql("delete from some_test where uid = ?").batchBindVars(batchBindVars).executeBatchSql();

Set uidsToInsert = new HashSet(uidsInLdapSet);
uidsToInsert.removeAll(uidsInDatabaseSet);
System.out.println("insertions: " + uidsToInsert.size());
batchBindVars = new ArrayList();
for (Object uidObject : uidsToInsert) { batchBindVars.add(GrouperUtil.toList(uidObject));}
new edu.internet2.middleware.grouperClient.jdbc.GcDbAccess().connectionName("grouper").sql("insert into some_test (uid) values (?)").batchBindVars(batchBindVars).batchSize(1000).executeBatchSql();
 
```

Original program did not intelligently insert/update as needed

```
// add an ldap connection in grouper-loader.properties
ldap.personLdap.url = ldap://ldap.andrew.cmu.edu:389/dc=cmu,dc=edu

// add a table in the grouper database
CREATE TABLE some_test (
    uid varchar(255) PRIMARY KEY
);

// run a program to sync from ldap to sql (will run in gsh or java).  
// Note, in java you need to run GrouperStartup.startup() first, in GSH you dont need to do that

List uids = LdapSessionUtils.ldapSession().list(String.class, "personLdap", null, LdapSearchScope.SUBTREE_SCOPE, "(cmuAndrewId=hanq*)", "uid"); 
     
uids = new ArrayList(new LinkeHashSet(uids));
     
List batchBindVars = new ArrayList();
for (Object uidObject : uids) { batchBindVars.add(GrouperUtil.toList(uidObject));}
     
new GcDbAccess().connectionName("grouper").sql("delete from some_test").executeSql();
new GcDbAccess().connectionName("grouper").sql("insert into some_test (uid) values (?)").batchBindVars(batchBindVars).batchSize(1000).executeBatchSql();

```

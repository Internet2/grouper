---
title: "Grouper loader LDAP example from installer"
space: Grouper
pageId: 28559933
version: 5
lastUpdated: 2017-06-20T19:33:21.885Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28559933/Grouper+loader+LDAP+example+from+installer
---

### Use mysql

```
Make a mysql schema, utf8, utf8_bin collation: grouper_temp.  
Make a user grouper_temp/grouper_temp.  
Grant all on grouper_temp schema to grouper_temp user.
```

### Get the installer

```
[mchyzer@i2midev6 ~]$ cd /tmp
[mchyzer@i2midev6 tmp]$ mkdir loaderExample
[mchyzer@i2midev6 tmp]$ cd loaderExample/
[mchyzer@i2midev6 loaderExample]$ pwd
/tmp/loaderExample
[mchyzer@i2midev6 loaderExample]$ wget https://software.internet2.edu/grouper/release/2.3.0/grouperInstaller.jar
--2017-06-20 17:52:58--  https://software.internet2.edu/grouper/release/2.3.0/grouperInstaller.jar
Resolving software.internet2.edu (software.internet2.edu)... 2001:48a8:68fe::52, 207.75.164.52
Connecting to software.internet2.edu (software.internet2.edu)|2001:48a8:68fe::52|:443... connected.
HTTP request sent, awaiting response... 200 OK
Length: 2411730 (2.3M) [application/x-java-archive]
Saving to: â€˜grouperInstaller.jarâ€™

100%[==============================================================================>] 2,411,730   --.-K/s   in 0.03s   

2017-06-20 17:52:58 (90.9 MB/s) - â€˜grouperInstaller.jarâ€™ saved [2411730/2411730]

[mchyzer@i2midev6 loaderExample]$ java -version
java version "1.7.0_13"

[mchyzer@i2midev6 loaderExample]$ java -jar grouperInstaller.jar 

```

### Generally accept the defaults except

```
Enter the database URL [jdbc:hsqldb:hsql://localhost:9001/grouper]: jdbc:mysql://localhost:3306/grouper_temp
Database user [sa]: grouper_temp
Database password (note, you aren't setting the pass here, you are using an existing pass, this will be echoed back) [<blank>]: grouper_temp

[mchyzer@i2midev6 ~]$ cd /tmp
[mchyzer@i2midev6 tmp]$ mkdir loaderExample
[mchyzer@i2midev6 tmp]$ cd loaderExample/
 [mchyzer@i2midev6 loaderExample]$ pwd
/tmp/loaderExample
[mchyzer@i2midev6 loaderExample]$ wget https://software.internet2.edu/grouper/release/2.3.0/grouperInstaller.jar
[mchyzer@i2midev6 loaderExample]$ java -version
java version "1.7.0_13"
[mchyzer@i2midev6 loaderExample]$ java -jar grouperInstaller.jar
Select the defaults except:

Do you want to use the default and included hsqldb database (t|f)? [t]: f
Enter the database URL [jdbc:hsqldb:hsql://localhost:9001/grouper]: jdbc:mysql://localhost:3306/grouper_temp
Database user [sa]: grouper_temp
Database password (note, you aren't setting the pass here, you are using an existing pass, this will be echoed back) [<blank>]: grouper_temp
… install patches …
Do you want to init the database (delete all existing grouper tables, add new ones) (t|f)? t
… install the test subjects and quickstart data …
Do you want to install the user interface (t|f)? [t]:
What ports do you want tomcat to run on (HTTP, JK, shutdown): [8080, 8009, 8005]: 8600, 8601, 8602
… install ui patches …
Enter the GrouperSystem password: pass
Do you want to install web services (t|f)? [t]: f
Do you want to install the web services client (t|f)? [t]: f
Do you want to install the provisioning service provider next generation (t|f)? [t]: f
Do you want to install the provisioning service provider (t|f)? [t]: f
Do you want to install the grouper ws scim (t|f)? [t]: f
```

### Lets work from UI, set the log4j

```
[mchyzer@i2midev6 loaderExample]$ emacs grouper.ui-2.3.0/dist/grouper/WEB-INF/classes/log4j.properties

Add this line:

log4j.logger.edu.internet2.middleware.grouper.app.loader = DEBUG
```

### Edit the sources

```
[mchyzer@i2midev6 loaderExample]$ emacs grouper.ui-2.3.0/dist/grouper/WEB-INF/classes/subject.properties

Add the source from here:

https://spaces.at.internet2.edu/display/Grouper/Grouper+Loader+LDAP+examples
 
#########################################
## Configuration for source id: cmuDirectory
## Source configName: cmuDirectory
#########################################
subjectApi.source.cmuDirectory.id = cmuDirectory
 
# this is a friendly name for the source
subjectApi.source.cmuDirectory.name = CMU Directory
 
# type is not used all that much.  Can have multiple types, comma separate.  Can be person, group, application
subjectApi.source.cmuDirectory.types = person
 
# the adapter class implements the interface: edu.internet2.middleware.subject.Source
# adapter class must extend: edu.internet2.middleware.subject.provider.BaseSourceAdapter
# edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter2  :  if doing JDBC this should be used if possible.  All subject data in one table/view.
# edu.internet2.middleware.grouper.subj.GrouperJdbcSourceAdapter   :  oldest JDBC source.  Put freeform queries in here
# edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter   :  used for LDAP
subjectApi.source.cmuDirectory.adapterClass = edu.internet2.middleware.grouper.subj.GrouperJndiSourceAdapter
 
# e.g. com.sun.jndi.ldap.LdapCtxFactory
subjectApi.source.cmuDirectory.param.INITIAL_CONTEXT_FACTORY.value = com.sun.jndi.ldap.LdapCtxFactory
 
# e.g. ldap://localhost:389
subjectApi.source.cmuDirectory.param.PROVIDER_URL.value = ldap://ldap.andrew.cmu.edu:389
 
# e.g. simple, none, sasl_mech
subjectApi.source.cmuDirectory.param.SECURITY_AUTHENTICATION.value = none
 
# ldap attribute which is the subject id.  e.g. exampleEduRegID   Each subject has one and only one subject id.  Generally it is opaque and permanent.
subjectApi.source.cmuDirectory.param.SubjectID_AttributeType.value = guid
 
# if the subject id should be changed to lower case after reading from datastore.  true or false
subjectApi.source.cmuDirectory.param.SubjectID_formatToLowerCase.value = false
 
# attribute which is the subject name
subjectApi.source.cmuDirectory.param.Name_AttributeType.value = cn
 
# attribute which is the subject description
subjectApi.source.cmuDirectory.param.Description_AttributeType.value = cn
 
# the 1st sort attribute for lists on screen that are derived from member table (e.g. search for member in group)
# you can have up to 5 sort attributes
subjectApi.source.cmuDirectory.param.sortAttribute0.value = cn
 
# the 1st search attribute for lists on screen that are derived from member table (e.g. search for member in group)
# you can have up to 5 search attributes
subjectApi.source.cmuDirectory.param.searchAttribute0.value = searchAttribute0
 
# attribute name of the email attribute
subjectApi.source.cmuDirectory.param.emailAttributeName.value = mail
 
#searchSubject: find a subject by ID.  ID is generally an opaque and permanent identifier, e.g. 12345678.
#  Each subject has one and only on ID.  Returns one result when searching for one ID.
 
# sql is the sql to search for the subject by id.  %TERM% will be subsituted by the id searched for
subjectApi.source.cmuDirectory.search.searchSubject.param.filter.value = (& (guid=%TERM%) (objectclass=cmuPerson))
 
# Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
subjectApi.source.cmuDirectory.search.searchSubject.param.scope.value = SUBTREE_SCOPE
 
# base dn to search in
subjectApi.source.cmuDirectory.search.searchSubject.param.base.value = ou=person,dc=cmu,dc=edu
 
#searchSubjectByIdentifier: find a subject by identifier.  Identifier is anything that uniquely
#  identifies the user, e.g. jsmith or jsmith@institution.edu.
#  Subjects can have multiple identifiers.  Note: it is nice to have if identifiers are unique
#  even across sources.  Returns one result when searching for one identifier.
 
# sql is the sql to search for the subject by identifier.  %TERM% will be subsituted by the identifier searched for
subjectApi.source.cmuDirectory.search.searchSubjectByIdentifier.param.filter.value = (& (cmuAndrewCommonNamespaceId=%TERM%) (objectclass=cmuPerson))
 
# Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
subjectApi.source.cmuDirectory.search.searchSubjectByIdentifier.param.scope.value = SUBTREE_SCOPE
 
# base dn to search in
subjectApi.source.cmuDirectory.search.searchSubjectByIdentifier.param.base.value = ou=person,dc=cmu,dc=edu
 
#   search: find subjects by free form search.  Returns multiple results.
 
# sql is the sql to search for the subject by free form search.  %TERM% will be subsituted by the text searched for
subjectApi.source.cmuDirectory.search.search.param.filter.value = (& (|(guid=%TERM%)(|(cn=*%TERM%*)(cmuAndrewCommonNamespaceId=*%TERM%*)))(objectclass=cmuPerson))
 
# Scope Values can be: OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
subjectApi.source.cmuDirectory.search.search.param.scope.value = SUBTREE_SCOPE
 
# base dn to search in
subjectApi.source.cmuDirectory.search.search.param.base.value = ou=person,dc=cmu,dc=edu
 
# attributes from ldap object to become subject attributes.  comma separated
subjectApi.source.cmuDirectory.attributes = cn, guid, cmuAndrewCommonNamespaceId, mail
 
# internal attributes are used by grouper only not exposed to code that uses subjects.  comma separated
subjectApi.source.cmuDirectory.internalAttributes = searchAttribute0
```

```
[mchyzer@i2midev6 loaderExample]$ mv grouper.ui-2.3.0/dist/grouper/WEB-INF/classes/sources.xml  grouper.ui-2.3.0/dist/grouper/WEB-INF/classes/sources.xml.bak
```

### Edit the grouper loader properties

```
[mchyzer@i2midev6 loaderExample]$ emacs grouper.ui-2.3.0/dist/grouper/WEB-INF/classes/grouper-loader.properties 
 
#################################
## LDAP connections
#################################
# specify the ldap connection with user, pass, url
# the string after "ldap." is the ID of the connection, and it should not have
# spaces or other special chars in it.  In this case is it "personLdap"
 
ldap.personLdap.url = ldap://ldap.andrew.cmu.edu/dc=cmu,dc=edu
ldap.personLdap.user = 
ldap.personLdap.pass = 
```

### Add the LDAP from attributes source from here

[Grouper Loader LDAP examples](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554595/Grouper+Loader+LDAP+examples)

```
[mchyzer@i2midev6 loaderExample]$ ./apache-tomcat-8.5.12/bin/shutdown.sh
Using CATALINA_BASE:   /tmp/loaderExample/apache-tomcat-8.5.12
Using CATALINA_HOME:   /tmp/loaderExample/apache-tomcat-8.5.12
Using CATALINA_TMPDIR: /tmp/loaderExample/apache-tomcat-8.5.12/temp
Using JRE_HOME:        /home/mchyzer/software/java
Using CLASSPATH:       /tmp/loaderExample/apache-tomcat-8.5.12/bin/bootstrap.jar:/tmp/loaderExample/apache-tomcat-8.5.12/bin/tomcat-juli.jar
[mchyzer@i2midev6 loaderExample]$ ./apache-tomcat-8.5.12/bin/startup.sh
Using CATALINA_BASE:   /tmp/loaderExample/apache-tomcat-8.5.12
Using CATALINA_HOME:   /tmp/loaderExample/apache-tomcat-8.5.12
Using CATALINA_TMPDIR: /tmp/loaderExample/apache-tomcat-8.5.12/temp
Using JRE_HOME:        /home/mchyzer/software/java
Using CLASSPATH:       /tmp/loaderExample/apache-tomcat-8.5.12/bin/bootstrap.jar:/tmp/loaderExample/apache-tomcat-8.5.12/bin/tomcat-juli.jar
Tomcat started.
[mchyzer@i2midev6 loaderExample]$

```

### Login and make a loader folder / group

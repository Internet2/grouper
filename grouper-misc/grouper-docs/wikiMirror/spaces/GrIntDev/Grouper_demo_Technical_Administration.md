---
title: "Grouper demo Technical Administration"
space: GrIntDev
pageId: 48791930
version: 42
lastUpdated: 2026-07-12T07:01:04.889Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48791930/Grouper+demo+Technical+Administration
---

See [info for signing onto the Grouper demo here](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541850/Grouper+Demo)

There is a Grouper demo site that hosts all the Grouper tools (or it can if you like). Note, this is not a production level service, this is only to show how Grouper works. There is no service level agreement, we might turn it off, do maintenance, etc.

[https://grouperdemo.internet2.edu/](https://grouperdemo.internet2.edu/)

This is built similarly to the Grouper cloud documentation.

If you want access to it, please email grouper-dev@internet2.edu with what userid(s) you would like, and what you would like to use. We can make sure it is available, and assign you a password. We have the quickstart data set there, but you can get a stem to create what you like. We might even be able to get you SSH credentials so you can run GSH...

### Run v5 SSL and port forward

```
docker run --name postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=pass -d -p 5432:5432 postgres:14
```

v5 generic

```
docker run --name grouper -e GROUPERSYSTEM_QUICKSTART_PASS=pass -e GROUPER_MORPHSTRING_ENCRYPT_KEY=abc123     -e GROUPER_DATABASE_PASSWORD=pass -e GROUPER_DATABASE_USERNAME=postgres     -e GROUPER_DATABASE_URL=jdbc:postgresql://172.17.0.1:5432/postgres -e GROUPER_AUTO_DDL_UPTOVERSION='v5.*.*'     -d -p 8444:8443 i2incommon/grouper:5.8.7 quickstart
```

v4 tomcat ssl

```
docker run --name grouper -e GROUPERSYSTEM_QUICKSTART_PASS=pass -e GROUPER_MORPHSTRING_ENCRYPT_KEY=abc123     -e GROUPER_DATABASE_PASSWORD=pass -e GROUPER_DATABASE_USERNAME=postgres     -e GROUPER_DATABASE_URL=jdbc:postgresql://172.17.0.1:5432/postgres -e GROUPER_AUTO_DDL_UPTOVERSION='v4.*.*' -e GROUPER_RUN_APACHE=false -e GROUPER_TOMCAT_HTTPS_PORT=8443 -e GROUPER_SSL_CERT_FILE=/opt/container_files/certs/client/localhost.pem -e GROUPER_SSL_KEY_FILE=/opt/container_files/certs/keys/localhost.key  -d -p 8444:8443 i2incommon/grouper:4.11.3 quickstart
```

### Port forward mysql

```
[mchyzer@login1 ~]$ ssh -L 3306:localhost:3306 i2midev6
```

### Add WS user

A grouper admin can add a WS user to the demo server:

```
[appadmin@i2midev1 bin]$ sudo htpasswd /etc/httpd/conf.d/users.pass username
[appadmin@i2midev1 bin]$ cd /opt/grouper/2.0.0/grouper.apiBinary-2.0.1/bin
gsh 0% grouperSession = GrouperSession.startRootSession();
gsh 1% addSubject("username", "person", "User Name");
gsh 2% addMember("etc:webServiceClientUsers", "username");

Run this SQL:
INSERT INTO subjectattribute (subjectId, NAME, VALUE, searchValue)
VALUES ('username', 'loginid', 'username', 'username');
INSERT INTO subjectattribute (subjectId, NAME, VALUE, searchValue)
VALUES ('username', 'name', 'User Name', 'user name');
INSERT INTO subjectattribute (subjectId, NAME, VALUE, searchValue)
VALUES ('username', 'description', 'User Name', 'user name');
 
COMMIT;

Add user to /etc/httpd/conf.d/grouper.conf

  Require user vsachdeva test jsmith rjohnson etc

Bounce apache:

/sbin/service httpd restart
```

Create a folder in the users folder somewhere, and grant access to this and the UI user id of the user

Try the client. [Download](http://www.internet2.edu/grouper/release/2.0.3/grouper.clientBinary-2.0.3.tar.gz)

Configure the grouper.client.properties

```
grouperClient.webService.url = https://grouperdemo.internet2.edu/grouper-ws_v2_0_0/servicesRest
grouperClient.webService.login = username
grouperClient.webService.password = ***

```

Run the client as a test

```
C:\temp\demoClient\grouper.clientBinary-2.0.3>java -jar grouperClient.jar --operation=groupSaveWs --name=users:misc:username:testGroup
Success: T: code: SUCCESS_INSERTED: users:misc:username:testGroup

```

### Status

We have anonymous status to monitor the health of the demo server

[https://grouperdemo.internet2.edu/status_grouper_v2_3/status?diagnosticType=all](https://grouperdemo.internet2.edu/status_grouper_v2_3/status?diagnosticType=all)

Can add an apache directive (outside of authn)

```
ProxyPass /status_grouper_v2_3/status ajp://localhost:8131/grouper_v2_3/status
```

### Admin notes

Get the new releases:

```
[appadmin@i2midev1 bin]$ cd /opt/grouper/1.6.1/
[appadmin@i2midev1 1.6.1]$ wget http://www.internet2.edu/grouper/release/1.6.1/grouper.apiBinary-1.6.1.tar.gz
[appadmin@i2midev1 1.6.1]$ tar xzvf grouper.apiBinary-1.6.1.tar.gz
[appadmin@i2midev1 1.6.1]$ wget http://www.internet2.edu/grouper/release/1.6.1/grouper.ui-1.6.1.tar.gz
[appadmin@i2midev1 1.6.1]$ tar xzvf grouper.ui-1.6.1.tar.gz
[appadmin@i2midev1 1.6.1]$ wget http://www.internet2.edu/grouper/release/1.6.1/grouper.ws-1.6.1.tar.gz
[appadmin@i2midev1 1.6.1]$ tar xzvf grouper.ws-1.6.1.tar.gz
[appadmin@i2midev1 1.6.1]$ wget http://www.internet2.edu/grouper/release/1.6.1/grouper.clientBinary-1.6.1.tar.gz
[appadmin@i2midev1 1.6.1]$ tar xzvf grouper.clientBinary-1.6.1.tar.gz

```

### API

Configure the grouper.properties:

```
[appadmin@i2midev1 ~]$ emacs /opt/grouper/1.6.1/grouper.apiBinary-1.6.1/conf/grouper.properties

```

```
groups.wheel.use = true
configuration.autocreate.system.groups = true

configuration.autocreate.group.name.0 = etc:webServiceClientUsers
configuration.autocreate.group.description.0 = users allowed to log in to the UI
configuration.autocreate.group.subjects.0 = GrouperSystem, mchyzer

configuration.autocreate.group.name.1 = etc:sysadmingroup
configuration.autocreate.group.description.1 = sys admin users
configuration.autocreate.group.subjects.1 = mchyzer

```

Configure the grouper.hibernate.properties:

```
[appadmin@i2midev1 ~]$ emacs /opt/grouper/1.6.1/grouper.apiBinary-1.6.1/conf/grouper.hibernate.properties

```

```
hibernate.dialect = org.hibernate.dialect.MySQL5Dialect
hibernate.connection.driver_class = com.mysql.jdbc.Driver
hibernate.connection.url = jdbc:mysql://localhost:3306/grouper_v1_6_1
hibernate.connection.username = grouper_v1_6_1
hibernate.connection.password = ****

```

Init the registry, [download this file](https://raw.githubusercontent.com/Internet2/grouper/master/grouper-qs-builder/quickstart.xml) into quickstart.xml, and [this file](https://raw.githubusercontent.com/Internet2/grouper/master/grouper-qs-builder/subjects.sql) into subjects.sql (get the right one from right tag/branch)

```
[appadmin@i2midev1 bin]$ wget -O quickstart.xml http://anonsvn.internet2.edu/cgi-bin/viewvc.cgi/i2mi/tags/GROUPER_1_6_1/grouper-qs-builder/quickstart.xml?view=co
[appadmin@i2midev1 bin]$ wget -O subjects.sql http://anonsvn.internet2.edu/cgi-bin/viewvc.cgi/i2mi/tags/GROUPER_1_6_1/grouper-qs-builder/subjects.sql?view=co
[appadmin@i2midev1 bin]$ ./gsh.sh -registry -drop -runscript
[appadmin@i2midev1 bin]$ ./gsh.sh -test -all
[appadmin@i2midev1 bin]$ ./gsh.sh -registry -runsqlfile subjects.sql
[appadmin@i2midev1 bin]$ ./gsh.sh -xmlimportold GrouperSystem quickstart.xml

```

Add a user (if not already there):

```
[mchyzer@i2midev1 bin]$ sudo htpasswd /etc/httpd/conf.d/users.pass username
gsh 0% addSubject("mchyzer", "person", "Chris Hyzer");
gsh 0% addMember("etc:sysadmingroup", "mchyzer");

//insert other subject data with SQL (e.g. for mchyzer)
INSERT INTO subjectattribute (subjectId, NAME, VALUE, searchValue)
VALUES ('mchyzer', 'loginid', 'mchyzer', 'mchyzer');
INSERT INTO subjectattribute (subjectId, NAME, VALUE, searchValue)
VALUES ('mchyzer', 'name', 'Chris Hyzer', 'chris hyzer');
INSERT INTO subjectattribute (subjectId, NAME, VALUE, searchValue)
VALUES ('mchyzer', 'description', 'Chris Hyzer', 'chris hyzer');

COMMIT;

```

### UI

Edit the UI build.properties:

```
[appadmin@i2midev1 grouper.ui-1.6.1]$ cd /opt/grouper/1.6.1/grouper.ui-1.6.1
[appadmin@i2midev1 grouper.ui-1.6.1]$ ant default
[appadmin@i2midev1 grouper.ui-1.6.1]$ emacs build.properties

grouper.folder=/opt/grouper/1.6.1/grouper.apiBinary-1.6.1
should.copy.context.xml.to.metainf=false
webapp.name=grouper_v1_6_1
default.webapp.folder=/opt/apache-tomcat-6.0.26/webapps/${webapp.name}

```

Run "ant default" for the UI

Edit proxy_ajp.conf

```
ProxyPass /grouper_v1_6_1/ ajp://localhost:8009/grouper_v1_6_1/
ProxyPass /grouper-ws_v1_6_1/ ajp://localhost:8009/grouper-ws_v1_6_1/

```

Edit the jump page:

```
[mchyzer@i2midev1 ~]$ sudo emacs /var/www/html/index.html

```

Move where logs are:

```
emacs /opt/tomcat6/webapps/grouper_v1_6_2/WEB-INF/classes/log4j.properties

## Grouper API error logging
log4j.appender.grouper_error = org.apache.log4j.RollingFileAppender
log4j.appender.grouper_error.File = /opt/tomcat6/logs/grouper-ui_v1_6_1/grouper_error.log
log4j.appender.grouper_error.MaxFileSize = 1000KB
log4j.appender.grouper_error.MaxBackupIndex = 1
log4j.appender.grouper_error.layout = org.apache.log4j.PatternLayout
log4j.appender.grouper_error.layout.ConversionPattern = %d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n
#log4j.appender.grouper_error.layout.ConversionPattern = %d{ISO8601}: %m%n

# Loggers

## Default logger; will log *everything*
log4j.rootLogger = ERROR, grouper_error

## All Internet2 (warn to grouper_error per default logger)
log4j.logger.edu.internet2.middleware = WARN

```

### WS

Get the build with a command like this:

```
[appadmin@i2midev1 grouper2.0]$ /usr/bin/scp -B i2mibuild:/home/mchyzer/tmp/grouperAll/build_mchyzer/grouper.ws-2.0.0.tar.gz .

```

Edit the WS build.properties

```
[appadmin@i2midev1 grouper-ws]$ cd /opt/grouper/1.6.2/grouper.ws-1.6.2/grouper-ws
[appadmin@i2midev1 grouper-ws]$ emacs build.properties

```

```
grouper.dir=/opt/grouper/1.6.1/grouper.apiBinary-1.6.1
webapp.name=grouper-ws_v1_6_1

```

Edit the grouper-ws.properties

```
[appadmin@i2midev1 grouper-ws]$ emacs conf/grouper-ws.properties

```

```
ws.client.user.group.name = etc:webServiceClientUsers

```

Remove the authentication part of web.xml since apache does this on the demo server and tomcat doesnt need to

```
[appadmin@i2midev1 grouper.ws-1.6.2]$ cd /opt/grouper/1.6.2/grouper.ws-1.6.2/grouper-ws
[appadmin@i2midev1 grouper-ws]$ emacs webapp/WEB-INF/web.xml

Remove:

<security-constraint>
<web-resource-collection>
<web-resource-name>Web services</web-resource-name>
<url-pattern>/services/*</url-pattern>
</web-resource-collection>
<auth-constraint>
<role-name>grouper_user</role-name>
</auth-constraint>
</security-constraint>

<security-constraint>
<web-resource-collection>
<web-resource-name>Web services</web-resource-name>
<url-pattern>/servicesRest/*</url-pattern>
</web-resource-collection>
<auth-constraint>
<!-- NOTE: This role is not present in the default users file -->
<role-name>grouper_user</role-name>
</auth-constraint>
</security-constraint>

<!-- Define the Login Configuration for this Application -->
<login-config>
<auth-method>BASIC</auth-method>
<realm-name>Grouper Application</realm-name>
</login-config>
<!-- Security roles referenced by this web application -->
<security-role>
<description>
The role that is required to log in to web service
</description>
<role-name>grouper_user</role-name>
</security-role>

```

Build and copy the war to tomcat

```
[appadmin@i2midev1 grouper-ws]$ mkdir /tmp/trash
[appadmin@i2midev1 grouper-ws]$ mv /opt/tomcat6/webapps/grouper-ws_v1_6_1* /tmp/trash
[appadmin@i2midev1 grouper-ws]$ ant dist
[appadmin@i2midev1 grouper-ws]$ cp build/dist/grouper-ws_v1_6_1.war /opt/tomcat6/webapps/
[appadmin@i2midev1 grouper-ws]$ /sbin/service tomcat6 restart

```

Edit the grouper-ws log4j.properties

```
appadmin@i2midev1 logs$ emacs /opt/tomcat6/webapps/grouper-ws_v1_6_1/WEB-INF/classes/log4j.properties

## Grouper API error logging
log4j.appender.grouper_error = org.apache.log4j.RollingFileAppender
log4j.appender.grouper_error.File = /opt/tomcat6/logs/grouper-ws_v1_6_1/grouper_error.log
log4j.appender.grouper_error.MaxFileSize = 1000KB
log4j.appender.grouper_error.MaxBackupIndex = 1
log4j.appender.grouper_error.layout = org.apache.log4j.PatternLayout
log4j.appender.grouper_error.layout.ConversionPattern = %d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n
#log4j.appender.grouper_error.layout.ConversionPattern = %d{ISO8601}: %m%n

# Loggers

## Default logger; will log *everything*
log4j.rootLogger = ERROR, grouper_error

## All Internet2 (warn to grouper_error per default logger)
log4j.logger.edu.internet2.middleware = WARN

```

### Client

Edit the grouper.client.properties, make a new tarball, put on download site:

```
[appadmin@i2midev1 1.6.1]$ cd /opt/grouper/1.6.1
[appadmin@i2midev1 1.6.1]$ emacs grouper.clientBinary-1.6.1/grouper.client.properties

```

```
# url of web service, should include everything up to the first resource to access
# e.g. http://groups.school.edu:8090/grouper-ws/servicesRest
# e.g. https://groups.school.edu/grouper-ws/servicesRest
grouperClient.webService.url = https://grouperdemo.internet2.edu/grouper-ws_v1_6_1/servicesRest

# kerberos principal used to connect to web service
grouperClient.webService.login =

# password for shared secret authentication to web service
# or you can put a filename with an encrypted password
grouperClient.webService.password =

```

```
[appadmin@i2midev1 tmp]$ cd /tmp
[appadmin@i2midev1 tmp]$ mkdir grouper.clientBinary-1.6.1
[appadmin@i2midev1 tmp]$ cd grouper.clientBinary-1.6.1
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ cp /opt/grouper/1.6.2/grouper.clientBinary-1.6.1/* .
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ rm *~
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ ls
BINARY-README.txt grouperClient.jar grouper.client.usage.example.txt
grouper.client.example.properties grouper.client.properties grouper.client.usage.txt
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ cd ..
[appadmin@i2midev1 tmp]$ tar zcvf grouper.clientBinary-1.6.1.tar.gz grouper.clientBinary-1.6.1
grouper.clientBinary-1.6.1/
grouper.clientBinary-1.6.1/grouper.client.usage.example.txt
grouper.clientBinary-1.6.1/grouperClient.jar
grouper.clientBinary-1.6.1/grouper.client.properties
grouper.clientBinary-1.6.1/BINARY-README.txt
grouper.clientBinary-1.6.1/grouper.client.example.properties
grouper.clientBinary-1.6.1/grouper.client.usage.txt
[root@i2midev1 1.6.1]# cp /tmp/grouper.clientBinary-1.6.1.tar.gz /var/www/html/grouper/1.6.1

```

Test the grouper client

```
[appadmin@i2midev1 grouperClient_1.6.1]$ /opt/grouper/1.6.1
[appadmin@i2midev1 1.6.1]$ mkdir grouper.clientDownloadUseThis
[appadmin@i2midev1 grouper.clientDownloadUseThis]$ wget https://grouperdemo.internet2.edu/grouper/1.6.2/grouper.clientBinary-1.6.1.tar.gz
[appadmin@i2midev1 grouper.clientDownloadUseThis]$ tar xzvf grouper.clientBinary-1.6.1.tar.gz
[appadmin@i2midev1 grouperClient_1.6.1]$ emacs grouper.clientBinary-1.6.1/grouper.client.properties
[appadmin@i2midev1 grouperClient_1.6.1]$ cd grouper.clientBinary-1.6.1
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=groupSaveWs --name=test:testGroup --createParentStemsIfNotExist=T
Success: T: code: SUCCESS_INSERTED: test:testGroup
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=addMemberWs --groupName=test:testGroup --subjectIds=GrouperSystem
Index 0: success: T: code: SUCCESS: GrouperSystem
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=getMembersWs --groupNames=test:testGroup
GroupIndex 0: success: T: code: SUCCESS: group: test:testGroup: subjectIndex: 0: GrouperSystem
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=hasMemberWs --groupName=test:testGroup --subjectIds=GrouperSystem
Index 0: success: T: code: IS_MEMBER: GrouperSystem: true
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=deleteMemberWs --groupName=test:testGroup --subjectIds=GrouperSystem
Index 0: success: T: code: SUCCESS: GrouperSystem
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=getGroupsWs --subjectIds=GrouperSystem
SubjectIndex 0: success: T: code: SUCCESS: subject: GrouperSystem: groupIndex: 0: etc:webServiceClientUsers
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=stemSaveWs --name=test
Success: T: code: SUCCESS_NO_CHANGES_NEEDED: test
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=groupDeleteWs --groupNames=test:testGroup1
Index 0: success: T: code: SUCCESS_GROUP_NOT_FOUND: test:testGroup1
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=stemDeleteWs --stemNames=test2
Index 0: success: T: code: SUCCESS_STEM_NOT_FOUND: test2
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=getGrouperPrivilegesLiteWs --groupName=test:testGroup --subjectId=GrouperSystem
Index 0: success: T: code: SUCCESS: group: test:testGroup: subject: GrouperSystem: access: admin
Index 1: success: T: code: SUCCESS: group: test:testGroup: subject: GrouperSystem: access: read
Index 2: success: T: code: SUCCESS: group: test:testGroup: subject: GrouperSystem: access: update
Index 3: success: T: code: SUCCESS: group: test:testGroup: subject: GrouperSystem: access: view
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=assignGrouperPrivilegesLiteWs --groupName=test:testGroup --subjectId=GrouperSystem --privilegeName=admin --allowed=true
Success: T: code: SUCCESS_ALLOWED_ALREADY_EXISTED: group: test:testGroup: subject: GrouperSystem: access: admin
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=aStem:aGroup
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=findGroupsWs --queryFilterType=FIND_BY_GROUP_NAME_APPROXIMATE --groupName=test:testGroup
Index 0: name: test:testGroup, displayName: test:testGroup
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=findStemsWs --stemQueryFilterType=FIND_BY_STEM_NAME_APPROXIMATE --stemName=test:testGroup
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=memberChangeSubjectWs --oldSubjectId=test.subject.qqq --newSubjectId=test.subject.www
Error with grouper client, check the logs: Bad response from web service: resultCode: PROBLEM_WITH_CHANGE, There were 0 successes and 1 failures of changing members subjects.
Error 0, result index: 0, code: MEMBER_NOT_FOUND, message: Subject: WsSubjectLookup[
cause=edu.internet2.middleware.grouper.exception.MemberNotFoundException: Cant find member with subjectId: 'test.subject.qqq',
subjectFindResult=SUBJECT_NOT_FOUND,
memberFindResult=MEMBER_NOT_FOUND,subjectId=test.subject.qqq] had problems: MEMBER_NOT_FOUND
Aug 14, 2010 11:36:41 AM edu.internet2.middleware.grouperClient.GrouperClient main
SEVERE: Bad response from web service: resultCode: PROBLEM_WITH_CHANGE, There were 0 successes and 1 failures of changing members subjects.
Error 0, result index: 0, code: MEMBER_NOT_FOUND, message: Subject: WsSubjectLookup[
cause=edu.internet2.middleware.grouper.exception.MemberNotFoundException: Cant find member with subjectId: 'test.subject.qqq',
subjectFindResult=SUBJECT_NOT_FOUND,
memberFindResult=MEMBER_NOT_FOUND,subjectId=test.subject.qqq] had problems: MEMBER_NOT_FOUND
edu.internet2.middleware.grouperClient.ws.GcWebServiceError: Bad response from web service: resultCode: PROBLEM_WITH_CHANGE, There were 0 successes and 1 failures of changing members subjects.
Error 0, result index: 0, code: MEMBER_NOT_FOUND, message: Subject: WsSubjectLookup[
cause=edu.internet2.middleware.grouper.exception.MemberNotFoundException: Cant find member with subjectId: 'test.subject.qqq',
subjectFindResult=SUBJECT_NOT_FOUND,
memberFindResult=MEMBER_NOT_FOUND,subjectId=test.subject.qqq] had problems: MEMBER_NOT_FOUND
at edu.internet2.middleware.grouperClient.ws.GrouperClientWs.handleFailure(GrouperClientWs.java:247)
at edu.internet2.middleware.grouperClient.api.GcMemberChangeSubject.execute(GcMemberChangeSubject.java:214)
at edu.internet2.middleware.grouperClient.GrouperClient.memberChangeSubject(GrouperClient.java:665)
at edu.internet2.middleware.grouperClient.GrouperClient.main(GrouperClient.java:363)
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=getMembershipsWs --groupNames=test:testGroup
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=addMemberWs --groupName=test:testGroup --subjectIds=GrouperSystem
Index 0: success: T: code: SUCCESS: GrouperSystem
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=getMembershipsWs --groupNames=test:testGroup
Index: 0: group: test:testGroup, subject: GrouperSystem, list: members, type: immediate, enabled: T
[appadmin@i2midev1 grouper.clientBinary-1.6.1]$ java -jar grouperClient.jar --operation=getSubjectsWs --subjectIds=GrouperSystem
Index: 0: success: T, code: SUCCESS, subject: GrouperSystem

```

### Build 2.0 from SVN:

```
GrouperApi:
[mchyzer@i2mibuild bin]$ buildGrouper.sh trunk
[mchyzer@i2midev1 grouper2.0]$ cd /tmp/grouper2.0/
[mchyzer@i2midev1 grouper2.0]$ sftp i2mibuild
sftp> get /home/mchyzer/tmp/grouper/build_mchyzer/grouper.apiBinary-2.0.0.tar.gz
[appadmin@i2midev1 2.0.0]$ cd /opt/grouper/2.0.0
[appadmin@i2midev1 2.0.0]$ rm -rf grouper.apiBinary-2.0.0*
[appadmin@i2midev1 2.0.0]$ cp /tmp/grouper2.0/grouper.apiBinary-2.0.0.tar.gz .
[appadmin@i2midev1 2.0.0]$ tar xzvf grouper.apiBinary-2.0.0.tar.gz
[appadmin@i2midev1 2.0.0]$ cp -Rv filesGrouper/* grouper.apiBinary-2.0.0/
[appadmin@i2midev1 2.0.0]$ cd grouper.apiBinary-2.0.0/bin
[appadmin@i2midev1 bin]$ ./gsh.sh
gsh 0% grouperSession = GrouperSession.startRootSession();
gsh 1% addStem(null, "test", "test");
gsh 2% addGroup("test", "testGroup0", "testGroup0");
gsh 4% addGroup("test", "testGroup1", "testGroup1");
gsh 5% addGroup("test", "testGroup2", "testGroup2");
gsh 6% addGroup("test", "testGroup3", "testGroup3");
gsh 7% grantPriv("test:testGroup0", "GrouperAll", AccessPrivilege.UPDATE);
gsh 8% grantPriv("test:testGroup1", "GrouperAll", AccessPrivilege.UPDATE);
gsh 9% grantPriv("test:testGroup2", "GrouperAll", AccessPrivilege.UPDATE);
gsh 10% grantPriv("test:testGroup3", "GrouperAll", AccessPrivilege.UPDATE);
gsh 11% grantPriv("etc:externalSubjectInviters", "GrouperAll", AccessPrivilege.UPDATE);

GrouperUi:
[mchyzer@i2mibuild bin]$ buildGrouperUi.sh trunk
[mchyzer@i2midev1 grouper2.0]$ cd /tmp/grouper2.0/
[mchyzer@i2midev1 grouper2.0]$ sftp i2mibuild
sftp> get /home/mchyzer/tmp/grouperUi/build_mchyzer/grouper.ui-2.0.0.tar.gz
[mchyzer@i2midev1 grouper2.0]$ sudo su - appadmin
[appadmin@i2midev1 ~]$ cd /opt/grouper/2.0.0/
[appadmin@i2midev1 2.0.0]$ rm -rf grouper.ui-2.0.0*
[appadmin@i2midev1 2.0.0]$ cp /tmp/grouper2.0/grouper.ui-2.0.0.tar.gz .
[appadmin@i2midev1 2.0.0]$ tar xzf grouper.ui-2.0.0.tar.gz
[appadmin@i2midev1 2.0.0]$ cp -Rv filesGrouperUi/* grouper.ui-2.0.0/
[appadmin@i2midev1 2.0.0]$ cd grouper.ui-2.0.0
[appadmin@i2midev1 grouper.ui-2.0.0]$ ant clean
[appadmin@i2midev1 2.0.0]$ cd /opt/grouper/2.0.0/
[appadmin@i2midev1 2.0.0]$ cp -Rv filesGrouperUiTomcat/* /opt/tomcats/tomcat_e/webapps/grouper_v2_0_0/

[appadmin@i2midev1 2.0.0]$ /sbin/service tomcat_e restart

```

## Social SAML gateway

### Cirrus integration

```
[root@i2midev6 social-metadata]# pwd
/etc/shibboleth/social-metadata
[root@i2midev6 social-metadata]# wget https://grouper.proxy.cirrusidentity.com/saml2/idp/metadata.php
[root@i2midev6 social-metadata]# mv metadata.php cirrus.xml

emacs /etc/shibboleth/shibboleth.xml

            <MetadataProvider type="XML" legacyOrgNames="true" file="social-metadata/cirrus.xml"/>

[root@i2midev6 shibboleth]# /sbin/service shibd restart

[root@i2midev6 shibboleth-ds]# pwd
/etc/shibboleth-ds
[root@i2midev6 shibboleth-ds]# diff idpselect_config.js idpselect_config.js.20180502 
46,47c46,47
<         'idpEntry.label': 'Or enter your organization\'s name.  If you don\'t belong to a federated Identity Provider, or if you can\'t log in with your IdP, enter "Social login by Cirrus" from the list to log in with a social identity.',
<         'idpEntry.NoPreferred.label': 'Enter your organization\'s name.  If you don\'t belong to a federated Identity Provider, or if you can\'t log in with your IdP, enter "Social login by Cirrus" from the list to log in with a social identity.',
---
>         'idpEntry.label': 'Or enter your organization\'s name',
>         'idpEntry.NoPreferred.label': 'Enter your organization\'s name',
[root@i2midev6 shibboleth-ds]# diff idpselect.css idpselect.css.20180502
(div.IdPSelectTextDiv)
51c51
<     /* height: 3.5ex; */ /* Add some height to separate the text from the boxes */
---
>     height: 3.5ex; /* Add some height to separate the text from the boxes */

```

### Previous version

There is a Social SAML gateway so facebook and google can login to the demo server UI. If there are problems with this email: Ewing, Bill [mailto:BEwing@[utsystem.edu](http://utsystem.edu)], Gary, James <jgary@[utsystem.edu](http://utsystem.edu)>

## MySQL add user

(set old passwords 0 avoid authentication plugin issues

```
SET old_passwords=0;
create USER 'readonly'@'%' IDENTIFIED BY 'xxx';
flush privileges;
GRANT Select ON *.* TO 'readonly'@'%';
FLUSH PRIVILEGES;
```

## Migration to i2midev6

March, 2017, we migrated from i2mibuild and i2midev1 to i2midev6. Note, to get to i2midev6 you have to ssh to login.internet2.edu first. Same with webprod3

Manage mysql:

```
[root@i2midev6 ~]# systemctl start mariadb
[root@i2midev6 ~]# systemctl restart shibd
```

Open a port of firewall

```
[root@i2midev6 zones]# firewall-cmd --permanent--add-port=15672/tcpsuccess
```

sdf

[root@i2midev6 zones]# firewall-cmd --permanent --add-port=15672/tcp

success

For sysadmin support email: [techsupport@internet2.edu](mailto:techsupport@internet2.edu)

Nagios is connected to the status servlet and will email the list: [grouper-sysadmin@internet2.edu](mailto:grouper-sysadmin@internet2.edu)

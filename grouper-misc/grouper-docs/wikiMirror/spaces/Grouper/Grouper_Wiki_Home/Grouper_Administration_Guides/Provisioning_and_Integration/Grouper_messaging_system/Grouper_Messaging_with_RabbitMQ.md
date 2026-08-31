---
title: "Grouper Messaging with RabbitMQ"
space: Grouper
pageId: 28548652
version: 28
lastUpdated: 2026-07-01T05:43:54.282Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548652/Grouper+Messaging+with+RabbitMQ
---

> The info on this page applies to Grouper 2.5+

The Grouper Messaging system interface has a RabbitMQ implementation.

### To make this work

Note, these instructions are from August 7, 2017, might get outdated.

You need updates to the grouper client which is either 2.3.0 api patch #76, or get the [2.3.1 snapshot client](https://software.internet2.edu/grouper/release/2.3.0/grouper.clientBinary-2.3.1-SNAPSHOT.tar.gz)

You need the [grouper rabbitMQ messaging connector](https://software.internet2.edu/grouper/release/2.3.0/grouper.rabbitMq-2.3.0.tar.gz) (can install this with grouper installer see below)

- The installer will copy the jars to the lib dir of Grouper

Configure the grouper.client.properties like [the example](https://github.com/Internet2/grouper/blob/master/grouper-misc/grouper-messaging-rabbitmq/src/main/resources/grouper.client.rabbitMq.example.properties)

Use the messaging view loader, WS, client, etc. See below for a loader example

### Configure the grouper-loader.properties to send change log notifications

```
#####################################
## Messaging integration with change log
#####################################
# note, change "messagingSample" in key to be the name of the consumer.  e.g. changeLog.consumer.someNameAnyName.class
changeLog.consumer.rabbitMqMessagingSample.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer

changeLog.consumer.rabbitMqMessagingSample.publisher.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbMessagingPublisher
changeLog.consumer.rabbitMqMessagingSample.publisher.messagingSystemName = rabbitmqSystem
# note, routingKey property is valid only for rabbitmq. For other messaging systems, it is ignored.
changeLog.consumer.rabbitMqMessagingSample.publisher.routingKey = 
## queue or topic
changeLog.consumer.rabbitMqMessagingSample.publisher.messageQueueType = queue
changeLog.consumer.rabbitMqMessagingSample.publisher.queueOrTopicName = sampleQueue
## this is optional if not using "id" for subjectId, need to be a subject attribute in the sources.xml
#changeLog.consumer.rabbitMqMessagingSample.publisher.addSubjectAttributes = email

changeLog.consumer.rabbitMqMessagingSample.regexRoutingKeyReplacementDefinition = replaceFirst('^hawaii.edu:', 'group.modify.').replaceFirst('(:enrolled|:waitlisted|:withdrawn)$', '')
changeLog.consumer.rabbitMqMessagingSample.replaceRoutingKeyColonsWithPeriods = true

```

Filter messages with something like this:

```
changeLog.consumer.rabbitMqMessagingSample.elfilter = event.eventType =~ ['GROUP_ADD', 'GROUP_UPDATE', 'GROUP_DELETE', 'MEMBERSHIP_ADD', 'MEMBERSHIP_UPDATE', 'MEMBERSHIP_DELETE'] and event.groupName =~ '^app:.*' and event.groupName !~ '.*:filter_sources$'
```

Note, you need to create the rabbitmq queue (you can do this with the admin console: [http://localhost:15672/#/queues](http://localhost:15672/#/queues)

### Messages

Here are some sample messages from the ESB connector

```
{  
  "encrypted":false,
  "esbEvent":[  
    {  
      "changeOccurred":false,
      "createdOnMicros":1502100000596000,
      "eventType":"MEMBERSHIP_ADD",
      "fieldName":"members",
      "groupId":"ccf74f3b4d0743428f7d72a14d8d81db",
      "groupName":"test:testLoader",
      "id":"484190ca24e54ea7a6ac9e7d26089afa",
      "membershipType":"flattened",
      "sequenceNumber":"790",
      "sourceId":"jdbc",
      "subjectId":"test.subject.2"
    }
  ]
}
```

```
{  
  "encrypted":false,
  "esbEvent":[  
    {  
      "changeOccurred":false,
      "createdOnMicros":1502133203662000,
      "displayName":"etc:sysadminReadonly",
      "eventType":"GROUP_ADD",
      "id":"ae0dde273bd0472c8257f36ffccf20ad",
      "name":"etc:sysadminReadonly",
      "parentStemId":"4fd44656ad6f423eaaddbd896fdc1aaa",
      "sequenceNumber":"793"
    }
  ]
}

```

### Install this connector via the installer

Get the latest installer, run it, install, you can skip most stuff, rabbitmq is at the end

```
Non-fatal ERROR: the environment variable JAVA_HOME requires to be invoked with Java 1.7+ JDK (not JRE), but was: 1.6
Press <enter> to continue... <using autorun property grouperInstaller.autorun.javaInvalid>: ''
Do you want to 'install' a new installation of grouper, 'upgrade' an existing installation,
  'patch' an existing installation, 'admin' utilities, or 'createPatch' for Grouper developers
  (enter: 'install', 'upgrade', 'patch', 'admin', 'createPatch' or blank for the default) [install]: <using autorun property grouperInstaller.autorun.actionEgInstallUpgradePatch>: ''
Enter in the Grouper install directory (note: better if no spaces or special chars) [C:/temp/temp/grouperInstaller]: <using autorun property grouperInstaller.autorun.installDirectory>: ''
Enter in a Grouper temp directory to download tarballs (note: better if no spaces or special chars) [C:\temp\temp\grouperInstaller\tarballs]: 
Enter the default IP address for checking ports (just hit enter to accept the default unless on a machine with no network, might want to change to 127.0.0.1): [0.0.0.0]: <using autorun property grouperInstaller.autorun.defaultIpAddressForPorts>: ''
Installing grouper version: 2.3.0
File exists: C:\temp\temp\grouperInstaller\tarballs\grouper.apiBinary-2.3.0.tar.gz, should we use the local file (t|f)? [t]: 
Unzipped file exists: C:\temp\temp\grouperInstaller\tarballs\grouper.apiBinary-2.3.0.tar, use unzipped file (t|f)? [t]: 
Untarred dir exists: C:\temp\temp\grouperInstaller\grouper.apiBinary-2.3.0, use untarred dir (t|f)? [t]: 
Do you want to use the default and included hsqldb database (t|f)? [t]: f

##################################

Example mysql URL: jdbc:mysql://localhost:3306/grouper
Example oracle URL: jdbc:oracle:thin:@server.school.edu:1521:sid
Example hsqldb URL: jdbc:hsqldb:hsql://localhost:9001/grouper
Example postgres URL: jdbc:postgresql://localhost:5432/database
Example mssql URL: jdbc:sqlserver://localhost:3280;databaseName=grouper

Enter the database URL [jdbc:hsqldb:hsql://localhost:9001/grouper]: 
Database user [sa]: 
Database password (note, you aren't setting the pass here, you are using an existing pass, this will be echoed back) [<blank>]: 
Do you want this script to start the hsqldb database (note, it must not be running in able to start) (t|f)? [t]: f

Do you want to init the database (delete all existing grouper tables, add new ones) (t|f)? f
Do you want to add quickstart subjects to DB (t|f)? [t]: f
Do you want to add quickstart data to registry (t|f)? [t] f
Do you want to install the user interface (t|f)? [t]: f
File exists: C:\temp\temp\grouperInstaller\tarballs\apache-ant-1.8.2-bin.tar.gz, should we use the local file (t|f)? [t]: 
Unzipped file exists: C:\temp\temp\grouperInstaller\tarballs\apache-ant-1.8.2-bin.tar, use unzipped file (t|f)? [t]: 
Untarred dir exists: C:\temp\temp\grouperInstaller\tarballs\apache-ant-1.8.2, use untarred dir (t|f)? [t]: 
Enter the tomcat version (8.5.12 or 6.0.35) [8.5.12]: 
File exists: C:\temp\temp\grouperInstaller\tarballs\apache-tomcat-8.5.12.tar.gz, should we use the local file (t|f)? [t]: 
Unzipped file exists: C:\temp\temp\grouperInstaller\tarballs\apache-tomcat-8.5.12.tar, use unzipped file (t|f)? [t]: 
Untarred dir exists: C:\temp\temp\grouperInstaller\apache-tomcat-8.5.12, use untarred dir (t|f)? [t]: 
Do you want to set the tomcat memory limit (t|f)? [t]: 
Editing file: C:\temp\temp\grouperInstaller\apache-tomcat-8.5.12\bin\catalina.bat
 - old max memory value is same as new value: 512M
 - old permgen memory value is same as new value: 256M
Editing file: C:\temp\temp\grouperInstaller\apache-tomcat-8.5.12\bin\catalina.sh
 - old max memory value is same as new value: 512M
 - old permgen memory value is same as new value: 256M
What ports do you want tomcat to run on (HTTP, JK, shutdown): [8701, 8702, 8703]: 
Enter the GrouperSystem password: pass
Do you want to set the GrouperSystem password in C:\temp\temp\grouperInstaller\apache-tomcat-8.5.12\conf\tomcat-users.xml? [t]: f
Do you want to install web services (t|f)? [t]: f
Do you want to install the web services client (t|f)? [t]: f
Do you want to install the provisioning service provider next generation (t|f)? [t]: f
Do you want to install the provisioning service provider (t|f)? [t]: f

##################################
Looking for conflicting jars

Do you want to start the Grouper loader (daemons)?
  (note, if it is already running, you need to stop it now, check the task manager for java.exe) (t|f)? [f]: 
Do you want to install the grouper ws scim (t|f)? [t]: f
Do you want to install grouper rabbitMQ messaging (t|f)? [f]: y
File exists: C:\temp\temp\grouperInstaller\tarballs\grouper.rabbitMq-2.3.0.tar.gz, should we use the local file (t|f)? [t]: 
Unzipped file exists: C:\temp\temp\grouperInstaller\tarballs\grouper.rabbitMq-2.3.0.tar, use unzipped file (t|f)? [t]: 
Untarred dir exists: C:\temp\temp\grouperInstaller\grouper.rabbitMq-2.3.0, use untarred dir (t|f)? [t]: 
Where do you want the Grouper RabbitMQ messaging connector installed? C:\temp\temp\grouperInstaller\grouper.apiBinary-2.3.0
Copying C:\temp\temp\grouperInstaller\grouper.rabbitMq-2.3.0\lib\amqp-client-4.0.2.jar to C:\temp\temp\grouperInstaller\grouper.apiBinary-2.3.0\lib\grouper\amqp-client-4.0.2.jar
Skipping file that exists in destination: C:\temp\temp\grouperInstaller\grouper.apiBinary-2.3.0\lib\grouper\slf4j-api-1.6.1.jar
Copying C:\temp\temp\grouperInstaller\grouper.rabbitMq-2.3.0\lib\grouperRabbitMq.jar to C:\temp\temp\grouperInstaller\grouper.apiBinary-2.3.0\lib\grouper\grouperRabbitMq.jar
Skipping file that exists in destination: C:\temp\temp\grouperInstaller\grouper.apiBinary-2.3.0\lib\grouper\slf4j-log4j12.jar
##################################

Configure your grouper.client.properties based on this file C:\temp\temp\grouperInstaller\grouper.rabbitMq-2.3.0\grouper.client.rabbitMq.example.properties

##################################

##################################

Installation success!

Run the installer's 'admin' function to get information and manage about your installation (db, tomcat, logs, etc)

##################################

```

### Install RabbitMQ with container

```
docker run -d --hostname my-rabbit --name some-rabbit -e RABBITMQ_DEFAULT_USER=user -e RABBITMQ_DEFAULT_PASS=password  -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Login to [http://localhost:15672/](http://localhost:15672/) with user/password

Default port is 5672

### Install RabbitMQ

On windows, install [erlang](http://www.erlang.org/downloads), then install [RabbitMQ](https://www.rabbitmq.com/)

Get the management ui working (do this as an administrator)

```
C:\Program Files\RabbitMQ Server\rabbitmq_server-3.6.10\sbin>rabbitmq-plugins.bat enable rabbitmq_management
```

Restart RabbitMQ service as an administrator (e.g. in windows in local services console)

Login to [http://localhost:15672/](http://localhost:15672/) with guest/guest

Default port is 5672

On Mac, Download RabbitMQ from [https://www.rabbitmq.com/install-standalone-mac.html](https://www.rabbitmq.com/install-standalone-mac.html). Unzip the file and go to sbin directory. Run ./rabbitmq-server. It will launch rabbimq server.

To access the web ui, run ./rabbitmq-plugins enable rabbitmq_management from sbin directory.

Login to [http://localhost:15672/](http://localhost:15672/) with guest/guest

**Note**: Grouper Rabbitmq messaging doesn't support acknowledging the individual messages.

Grouper Rabbitmq messaging supports TLS version 1.1 and 1.2. The instructions to configure Rabbitmq for TLS are at: [https://www.rabbitmq.com/ssl.htm](https://www.rabbitmq.com/ssl.html)l.

Set the following properties in grouper.client.properties file

```
################################
## Grouper Messaging System
################################

# name of a messaging system.  note, "rabbitmqSystem" can be arbitrary
grouper.messaging.system.rabbitmqSystem.name = rabbitmqSystem

# class that implements edu.internet2.middleware.grouperClient.messaging.GrouperMessagingSystem
grouper.messaging.system.rabbitmqSystem.class = edu.internet2.middleware.grouperMessagingRabbitmq.GrouperMessagingRabbitmqSystem

# host address of rabbitmq queue
grouper.messaging.system.rabbitmqSystem.host = localhost

# virtual host of rabbitmq queue
grouper.messaging.system.rabbitmqSystem.virtualhost =

# port of rabbitmq queue
grouper.messaging.system.rabbitmqSystem.port =

grouper.messaging.system.rabbitmqSystem.defaultPageSize = 10

grouper.messaging.system.rabbitmqSystem.maxPageSize = 50

# default system settings to this messaging system, note, there is only one level of inheritance. (optional)
grouper.messaging.system.rabbitmqSystem.defaultSystemName = rabbitmqSystem

#username
grouper.messaging.system.rabbitmqSystem.username = guest

#pass
grouper.messaging.system.rabbitmqSystem.password = guest

# set the following three properties if you want to use TLS connection to rabbitmq. All three need to be populated.
# TLS Version
grouper.messaging.system.rabbitmqSystem.tlsVersion = TLSv1.1

# path to trust store file 
grouper.messaging.system.rabbitmqSystem.pathToTrustStore = 

# trust passphrase
grouper.messaging.system.rabbitmqSystem.trustPassphrase =

```

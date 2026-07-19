---
title: "Grouper Training Environment"
space: Grouper
pageId: 28541839
version: 141
lastUpdated: 2026-07-12T15:26:00.895Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541839/Grouper+Training+Environment
---

Welcome to Virtual Grouper training.

The [Grouper Training Environment (GTE)](https://github.internet2.edu/docker/grouper_training) is a set of lesson plans, training exercises, and supporting Docker modules. Students are able to bring up a full training environment by running a simple command. The GTE provides all the necessary components and configuration to go from learning basic Grouper operations, to exploring the access governance concepts presented in the [Grouper Deployment Guide (GDG)](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541813/Grouper+Deployment+Guide). The focus of the GTE is Grouper installation, configuration, design, and operation. Little to no technical experience is required to complete the Grouper training.

The training environment will be made available to students via Amazon Web Services (AWS). The student will only need a connection to the Internet and a suitable SSH client (e.g. OpenSSH on UNIX/Linux/Mac systems or PuTTY on Windows). IP addresses and credentials will be provided during the class.

## Session information

- We will be taking short breaks throughout the day
- To keep things flowing well, please refrain from using the Zoom chat window. Instead, use the dedicated Slack channel for your discussions!
- VMs will be opened up several days before the class. They will remain open for two weeks after the class so that you can practice the lab exercises.
- Ask ALL of your questions. There are no dumb questions.

## Pre-work

1. Connect to your AWS instance from your computer
2. Install "Kahoot!" (learning and trivia app) on your phone or preferably a device not used by zoom
3. Install the Zoom app if you do not already have it
  
  1. It is nice if you have a camera available that you can turn on from time to time so we can all put names to faces
4. Install the Slack app if you do not already have it and make sure you are in the grouper-school-spring2022 channel
5. Review these commands that you will need (located in the GTE)
6. If you do not know linux,  [here is a 5 minute lesson](https://www.youtube.com/watch?v=bb_ApuH15YE)
7. Unix text editor, you need to be able to edit a file (there are only a few exercises that require this) with nano, vi, or emacs. If you know how to use one of these, then you are good. If not:
  
  1. "nano" is easiest, [here is a 2 minute lesson](https://www.youtube.com/watch?v=GYz81gB-79w)
  2. If you prefer “vi”,  [here is an 8 minute lesson](https://www.youtube.com/watch?v=pU2k776i2Zw)
8. Read the  [Grouper Deployment Guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541813/Grouper+Deployment+Guide)

## Computer setup the morning of training

1. If you have a phone or device not used for zoom, have that available with Kahoot! running. 
  
  1. You do not need to sign in to Kahoot but when using it please enter your real first and last name
2. If you have multiple monitors available, that would be useful but not required  
  
  
  1. Might be nice to not use a separate computer for Zoom and exercises, so you can share your screen if needed
3. Open the Slack app to the class channel
4. Open the Zoom app and connect to the Main zoom meeting (pinned from slack channel)
  
  1. Please mute yourself and either keep your camera on or be prepared to turn it on while speaking so we can put names to faces
5. Open your terminal or SSH client (from pre-work <above> and slack pin) and connect to your GTE instance  
  
  
  1. Run this command
    
    
    ```
    ./gte 101.1.1
    ```
  2. Leave that terminal/ssh window open. Be prepared to reconnect if your SSH tunnel disconnects (e.g. when your browser stops working)
6. Close all your browsers and tabs
7. Open Chrome (or firefox if you don't have Chrome)
  
  1. Tab 1: GTE Jump page: [https://localhost:8443/](https://localhost:8443/)
  2. Tab 2: Grouper admin account:  [https://localhost:8443/grouper/](https://localhost:8443/grouper/) (banderson/password)
  3. Tab 3: [Text to copy/paste](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545593/Grouper+Training+-+Container+-+Lesson+Maturity+-1+quickstart)
  4. Tab 4: [Grouper training VM documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543607/Grouper+training+VM+documentation)
  5. Tab 5: SQL manager: [https://localhost:8443/phpmyadmin/](https://localhost:8443/phpmyadmin/) (root/<no password>)
  6. Tab 6: LDAP manager: [https://localhost:8443/phpldapadmin/](https://localhost:8443/phpldapadmin/) (cn=root,dc=internet2,dc=edu/password)
8. We need another Grouper non-admin session. Either open an incognito Chrome window (if other tabs are not incognito), or open a different browser (e.g. Firefox instead of Chrome)
  
  1. Tab 1: Grouper non-admin account:  [https://localhost:8443/grouper/](https://localhost:8443/grouper/) (jsmith/password)
9. Open a text editor e.g. notepad (windows) or notes (mac)

## Important links

[Grouper training VM documentation](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28543607/Grouper+training+VM+documentation) - Simple commands to run GTE courses and interact with the environment

Text to copy/paste - Text from slides to copy and paste easily

[Grouper Deployment Guide](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541813/Grouper+Deployment+Guide) - Grouper concepts, standards, and best practices

[Kahoot](https://www.kahoot.it) - for periodic quizes

## Schedule

Approximate daily schedule

- 12:00 - 1:15 - Lessons
- 1:15 - 1:30 - Break
- 1:30 - 2:30 - Lessons
- 2:30 - 2:45- Break
- 2:45 - 3:45 - Lessons
- 3:45 - 4:00 - Break
- 4:00 - 5:00 - Lessons

## GTE app links (once connected and a lesson is started)

| Name | Link | Credentials | Description |
| --- | --- | --- | --- |
| [Jump page](https://localhost:8443/) | [https://localhost:8443/](https://localhost:8443/) |  | Links applications |
| [Grouper](https://localhost:8443/grouper/) | [https://localhost:8443/grouper/](https://localhost:8443/grouper/) | Admin: banderson/password   Civilian: jsmith/password | Grouper UI application |
| [Database manager](https://localhost:8443/phpmyadmin/) | [https://localhost:8443/phpmyadmin/](https://localhost:8443/phpmyadmin/) | root / <no password> | Phpmyadmin Mysql database manager |
| [LDAP manager](https://localhost:8443/phpldapadmin/) | [https://localhost:8443/phpldapadmin/](https://localhost:8443/phpldapadmin/) | username: cn=root,dc=internet2,dc=edu   password: password | Phpldapadmin LDAP administration |
| [Messaging manager](https://localhost:8443/rabbitmq/) | [https://localhost:8443/rabbitmq/](https://localhost:8443/rabbitmq/) | username: guest   password: guest | Rabbitmq messaging administration |
| [Shibboleth attributes](https://localhost:8443/app) | [https://localhost:8443/app](https://localhost:8443/app) |  | Simple screen to show login state |

## Other info linked from slack "bookmarks"

## Corrections on 101 material

The slides are generally up to date, but the movies are out of date. Thanks for your patience. Note, you can change the playback speed (e.g. 1.5x or 2x) to process these quicker

**Note:**

- The GTE used to have a SQL browser embedded. This is not the case any more. Read the "Database browser" section of this page to configure DBeaver or another Postgres browser. Lesson 101.9.2 shows how to set up DBeaver connections.
- If you are not using the command from the password file to connect to ssh (e.g. if you use putty or secure crt) then you should port forward local port 8432 to remote localhost 5432, and local port 8389 to remote localhost 389

**101.4 attributes**: (for Nov 2024 training and earlier only) if you see a video called 101.1_attributes and one called grouper101.4-rev, just do the rev one, you can skip the non-rev one (its a little out of date)

1. After configuring the job, you will not see it in the job list
2. Import this into the config for grouper-loader.properties (Miscellaneous → Configure → Configuration files → Import)
  
  
  ```
  otherJob.ldapAnames.ldapSqlDbConnection = grouper
  otherJob.ldapAnames.class = edu.internet2.middleware.grouper.app.ldapToSql.LdapToSqlSyncDaemon
  otherJob.ldapAnames.quartzCron = 8 7 6 * * ?
  ```
3. Note, that import fixed a bug in Grouper where the class and schedule where not saved, but also marks the employee number attribute as unique
4. Schedule jobs. Since there was changes to the configs, after the wizard, this is telling the daemons to look in the configs for daemons to schedule
5. The daemon should run successfully

## Database browser

The postgres database in the GTE has no browser in the GTE (since the container went multiarch in 2024/03). If you have a postgres admin tool you can use that, or if you want a suggestion, install DBeaver community edition (free).

1. Make sure you dont have anything listening on 8432 on your computer (or map other ports). You can assume nothing is and its probably a good assumption.
2. The connection string in the password file maps ports 8432 (postgres). If you are not using the command from the password file to connect to ssh (e.g. if you use putty or secure crt) then you should port forward local port 8432 to remote localhost 5432
3. Connect to mysql from DBeaver or another postgres browser to the three databases
  
  
  
  | Host | Port | Database | User | Pass |
  | --- | --- | --- | --- | --- |
  | localhost | 8432 | grouper | grouper | pass |
  | localhost | 8432 | sis | sis_owner | pass |
  | localhost | 8432 | hr | hr_owner | pass |
4. Right click on the DatabaseNavigator pane, Create → Connection
5. Name the connection under General
6. Under Connection settings, put in the host (localhost), port (8432), database (these differ for the three connections: grouper, hr, sis), username (Grouper), and password (pass)
7. You end up with three connections

## Improved LDAP browser

The ldap browser in the GTE is web based browser and might not be the easiest to use or might not be what you are familiar with. If you have an LDAP browser you can use that, or if you want a suggestion, install Apache Directory Studio (free). Note Apache Directory Studio can read and write LDAP data.

1. Make sure you dont have anything listening on 8389 on your computer (or map other ports). You can assume nothing is and its probably a good assumption.
2. The connection string in the password file maps ports 8389 (ldap). If you are not using the command from the password file to connect to ssh (e.g. if you use putty or secure crt) then you should port forward local port 8389 to remote localhost 389
3. When starting a GTE module, add ldap  
    
  FROM
  
  
  ```
  gte <container name>
  ```
  
  TO
  
  
  ```
  gte --ldap <container name>
  ```
4. Connect to ldap from Apache directory studio or another ldap browser
  
  
  ```
  localhost
  8389
  cn=root,dc=internet2,dc=edu
  password
  ```

## Help

If you have any questions about the workshop or its technical content, please send a note to Jean at jeanc@example.com.

You can also use the dedicated Slack channel for an even faster response.

## Run locally

1. First install docker on mac or docker desktop on windows (or linux)
2. Make sure you are using the right version
3. Then spin up the GTE:

```
C:\Users\mchyzer-local> docker run -d -p 8443:443 -p 8432:5432 -p 8389:389 -e GROUPER_TOMCAT_LOG_ACCESS=false --name 101.1.1 tier/gte:101.1.1-202604
a282e12af384105b810fb2d1fd67ad450a3ef1fa0ea039c6083348a979cef3d5

```

Note, it has been reported that if ldap or other services do not start that this needs to be run in the container:

```
ulimit -n 1024

```

## Run WS via grouperClient for GTE

There is an on-demand training course for this: Administration - Web services

The GTE is configured to run web services in the context "grouper-ws", and uses built-in Grouper WS authn with basic auth (in your institution you might be using Kerberos or JWT or LDAP etc). This means the passwords are stored encrypted in the database, and assigned with GSH.

```
[tomcat@eb09ffa14bb5 classes]$ env | grep WS
GROUPER_WS_GROUPER_AUTH=true
GROUPERWS_URL_CONTEXT=grouper-ws
GROUPER_WS=true
```

1. Make a group of users who can call WS (probably not necessary but it is a best practice). In the "etc" folder, make a group:  
  
  ```
  webServiceClientUsers
  ```
2. Add banderson to that group
3. Configure that group to control who can call WS. Import this config into grouper-ws properties or just add one config in the UI.  
  
  ```
  ws.client.user.group.name = etc:webServiceClientUsers
  ```
4. Set a password for banderson  
    
  In your VM command line:
  
  
  ```
  gte-gsh
  ```
  
    
  When you get the GSH prompt:  
  
  ```
  new GrouperPasswordSave().assignApplication(GrouperPassword.Application.WS).assignUsername("banderson").assignPassword("password").save();
  
  
  :q
  ```
5. You can do this from your local computer outside of the container. This is from a Mac, you can do this from windows as well. Assumes you have a compatible "java" in your path  
    
  
  ```
  mchyzer@chriss-mbp-6 grouper_v5 % cd /tmp
  mchyzer@chriss-mbp-6 /tmp % wget https://repo1.maven.org/maven2/edu/internet2/middleware/grouper/grouperClient/5.22.3/grouperClient-5.22.3.jar
  mchyzer@chriss-mbp-6 /tmp % mv grouperClient-5.22.3.jar grouperClient.jar 
  ```
6. Make a grouper.client.properties file in that directory  
  
  ```
  grouperClient.webService.url = https://localhost:8443/grouper-ws/servicesRest
  grouperClient.webService.login = banderson
  grouperClient.webService.password = password
  grouperClient.https.customSocketFactory = edu.internet2.middleware.grouperClient.ssl.EasySslSocketFactory
  
  
  ```
7. Call a web service with the client  
  
  ```
  mchyzer@chriss-mbp-6 /tmp % java -jar grouperClient.jar --operation=getMembersWs --groupNames=basis:hr:employee:dept:10410:affiliate --debug=true
  Reading resource: grouper.client.properties, from: /private/tmp/grouper.client.properties
  WebService: connecting to URL: 'https://localhost:8443/grouper-ws/servicesRest/5.22.3/groups'
  WebService: connecting as user: 'banderson'
  
  ################ REQUEST START (indented) ###############
  
  POST /grouper-ws/servicesRest/5.22.3/groups HTTP/1.1
  Connection: close
  Authorization: Basic xxxxxxxxxxxxxxxx
  User-Agent: Jakarta Commons-HttpClient/3.1
  Host: localhost:8443
  Content-Length: 103
  Content-Type: application/json
  
  {
    "WsRestGetMembersRequest":{
      "wsGroupLookups":[
        {
          "groupName":"basis:hr:employee:dept:10410:affiliate"
        }
      ]
    }
  }
  
  ################ REQUEST END ###############
  
  
  
  ################ RESPONSE START (indented) ###############
  
  HTTP/1.1 200
  Date: Thu, 20 Nov 2025 19:34:08 GMT
  Server: Apache/2.4.62 (Rocky Linux) OpenSSL/3.2.2
  Set-Cookie: JSESSIONID=xxxxxxxxxxxx; HttpOnly
  X-Grouper-resultCode: SUCCESS
  X-Grouper-success: T
  X-Grouper-resultCode2: NONE
  Content-Type: application/json;charset=UTF-8
  Connection: close
  Transfer-Encoding: chunked
  
  {
    "WsGetMembersResults":{
      "resultMetadata":{
        "resultCode":"SUCCESS",
        "resultMessage":"Success for: clientVersion: 5.22.3, wsGroupLookups: Array size: 1: [0]: WsGroupLookup[pitGroups=[],groupName=basis:hr:employee:dept:10410:affiliate]\n\n, memberFilter: All, includeSubjectDetail: false, actAsSubject: null, fieldName: null, subjectAttributeNames: null\n, paramNames: \n, params: null\n, sourceIds: null\n, pointInTimeFrom: null, pointInTimeTo: null, pageSize: null, pageNumber: null, sortString: null, ascending: null",
        "success":"T"
      },
      "responseMetadata":{
        "resultWarnings":", Client version: 5.22.3 is greater than (major/minor) server version: 5.20.5, Client version: 5.22.3 is greater than (major/minor) server version: 5.20.5",
        "millis":"173",
        "serverVersion":"5.20.5"
      },
      "results":[
        {
          "wsGroup":{
            "extension":"affiliate",
            "typeOfGroup":"group",
            "displayExtension":"Alumni Relations affiliate",
            "description":"Alumni Relations affiliate auto-created by grouperLoader",
            "displayName":"basis:Human Resources:Employee:Department:Alumni Relations (10410):Alumni Relations affiliate",
            "name":"basis:hr:employee:dept:10410:affiliate",
            "uuid":"9981e48588ae4adaa9e637e566a25ca0",
            "idIndex":"1000097",
            "enabled":"T"
          },
          "wsSubjects":[
            {
              "resultCode":"SUCCESS",
              "success":"T",
              "memberId":"6dd5b03ebd934a2f8e83314abaedc758",
              "id":"800000578",
              "sourceId":"eduLDAP"
            },
            {
              "resultCode":"SUCCESS",
              "success":"T",
              "memberId":"443667355520433da11d41b55a338822",
              "id":"800001267",
              "sourceId":"eduLDAP"
            }
          ]
          ,
          "resultMetadata":{
            "resultCode":"SUCCESS",
            "success":"T"
          }
        }
      ]
    }
  }
  
  ################ RESPONSE END ###############
  
  
  Output template: GroupIndex ${groupIndex}: success: ${resultMetadata.success}: code: ${resultMetadata.resultCode}: group: ${wsGroup.name}: subjectIndex: ${subjectIndex}: ${wsSubject.id}, available variables: wsGetMembersResults, grouperClientUtils, groupIndex, wsGetMembersResult, wsGroup, resultMetadata, subjectIndex, wsSubject
  GroupIndex 0: success: T: code: SUCCESS: group: basis:hr:employee:dept:10410:affiliate: subjectIndex: 0: 800000578
  GroupIndex 0: success: T: code: SUCCESS: group: basis:hr:employee:dept:10410:affiliate: subjectIndex: 1: 800001267
  Elapsed time: 621ms
  mchyzer@chriss-mbp-6 /tmp % 
  
  
  ```

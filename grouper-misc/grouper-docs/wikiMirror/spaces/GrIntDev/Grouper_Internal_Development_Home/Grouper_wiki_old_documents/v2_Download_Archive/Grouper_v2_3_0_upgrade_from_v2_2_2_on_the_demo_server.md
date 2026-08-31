---
title: "Grouper v2.3.0 upgrade from v2.2.2 on the demo server"
space: GrIntDev
pageId: 48793405
version: 11
lastUpdated: 2026-07-12T07:01:41.157Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793405/Grouper+v2.3.0+upgrade+from+v2.2.2+on+the+demo+server
---

We will be cloning and upgrading the grouper demo server WS, UI, and loader. We want to keep the 2.2.2 version up there.

### Tomcats for UI and WS

We have a bunch of tomcats, and will list the 2.2 tomcats and assign new ones

| Tomcat name | Grouper Version | Function |
| --- | --- | --- |
| tomcat_d | 2.2.2 | UI |
| tomcat_i | 2.2.2 | WS |
| tomcat_f | 2.3.0 | UI |
| tomcat_g | 2.3.0 | WS |

### Migrate data to new database

Lets create the new schemas. I use SqlYog for MySQL to create a schema, set the pass, and migrate data. Note, using UTF-8 and bin collation. I exported a script and imported but the views werent there so I ran

```
./gsh -registry -deep -runscript
```

### Copy the UI and WS

```
[appadmin@i2midev1 tomcat_f]$ rm -rf webapps/grouper_v1_6
[appadmin@i2midev1 tomcat_f]$ rm -rf logs/
Display all 108 possibilities? (y or n)
[appadmin@i2midev1 tomcat_f]$ rm -rf logs/*
[appadmin@i2midev1 tomcat_f]$ /sbin/service tomcat_f stop
Shutting down tomcat_f Tomcat services: 
Using CATALINA_BASE:   /opt/tomcats/tomcat_f
Using CATALINA_HOME:   /opt/tomcat6base
Using CATALINA_TMPDIR: /opt/tomcats/tomcat_f/temp
Using JRE_HOME:        /opt/javas/java_f
Using CLASSPATH:       /opt/tomcat6base/bin/bootstrap.jar
Waiting for exit...
[appadmin@i2midev1 tomcats]$ mv tomcat_f/webapps/grouper_v2_2 tomcat_f/webapps/grouper_v2_3
 
 
[appadmin@i2midev1 tomcat_g]$ /sbin/service tomcat_g stop
Shutting down tomcat_g Tomcat services: 
Using CATALINA_BASE:   /opt/tomcats/tomcat_g
Using CATALINA_HOME:   /opt/tomcat6base
Using CATALINA_TMPDIR: /opt/tomcats/tomcat_g/temp
Using JRE_HOME:        /opt/javas/java_g
Using CLASSPATH:       /opt/tomcat6base/bin/bootstrap.jar
Waiting for exit...
Waiting for exit...
Waiting for exit...
[appadmin@i2midev1 tomcat_g]$ rm -rf webapps/grouper-ws_v2_0_0*
[appadmin@i2midev1 tomcat_g]$ cp -R ../tomcat_i/webapps/grouper-ws_v2_2 webapps
[appadmin@i2midev1 tomcat_g]$ cp -R ../tomcat_i/clients .
[appadmin@i2midev1 tomcats]$ mv tomcat_g/webapps/grouper-ws_v2_2 tomcat_g/webapps/grouper-ws_v2_3
 
 
[appadmin@i2midev1 tomcats]$ cp -R tomcat_d_gsh tomcat_f_gsh
[appadmin@i2midev1 tomcats]$ mv tomcat_f_gsh/webapps/grouper_v2_2 tomcat_f_gsh/webapps/grouper_v2_3

```

Edit the apache config

```
ProxyPass /grouper_v2_3/ ajp://localhost:8131/grouper_v2_3/
 
ProxyPass /grouper-ws_v2_3/ ajp://localhost:8141/grouper-ws_v2_3/
 
#match anything that is not grouperExternal
<LocationMatch ^/grouper_v2_3[^/]*/(?!grouperExternal/).*>
  AuthType shibboleth
  ShibRequestSetting requireSession 1
  require valid-user
</LocationMatch>

#match anything that is grouperExternal, note, this could be a different authn system
<LocationMatch ^/grouper_v2_3[^/]*/grouperExternal/.*>
  AuthType shibboleth
  ShibRequestSetting requireSession 1
  require valid-user
</LocationMatch>
 
<LocationMatch ^/grouper-ws_v2_3.*>
  AuthType Basic
  AuthName "By Invitation Only"
  AuthUserFile /etc/httpd/conf.d/users.pass
  Require valid-user
</LocationMatch>
```

Point the config files to point to new database:

- change morphString.properties, encrypt new passes in external files
- edit the three grouper.hibernate.properties files
- edit grouper.properties (ui url and email pass)
- change the log4j.properties
- copy the v2_2 init.d scripts and chkconfig it so its a service and starts at startup

Bring up the UI/WS/loader. Note it will be pointing to the new database, new tomcat, but with 2.2.2 code and schema. Try it out

UI: [https://grouperdemo.internet2.edu/grouper_v2_3/](https://grouperdemo.internet2.edu/grouper_v2_3/)

WS: [https://grouperdemo.internet2.edu/grouper-ws_v2_3/tierApiAuthz/v1/Groups/name:test:testGroup/Members/id:test?indent=true](https://grouperdemo.internet2.edu/grouper-ws_v2_3/tierApiAuthz/v1/Groups/name:test:testGroup/Members/id:test?indent=true)

loader: [appadmin@i2midev1 classes]$ /sbin/service grouper_v2_3_loader start

### Upgrade UI / WS / loader

- Stop the services:
  
  
  ```
  /sbin/service tomcat_f stop
  /sbin/service tomcat_g stop
  /sbin/service grouper_v2_3_loader stop
  ```
- Run the installer in upgrade mode on the loader, ui, and ws
  
  
  ```
  [appadmin@i2midev1 grouper_2_3_upgrade]$  wget http://software.internet2.edu/grouper/release/2.3.0/grouperInstaller.jar
  [appadmin@i2midev1 grouper_2_3_upgrade]$ java -jar grouperInstaller.jar 
   
  UI or WS
   
  /opt/tomcats/tomcat_f/webapps/grouper_v2_3
  /opt/tomcats/tomcat_f_gsh/webapps/grouper_v2_3
  /opt/tomcats/tomcat_g/webapps/grouper-ws_v2_3
  
  
  
  ```
- Start the services
  
  
  ```
  /sbin/service tomcat_f stop
  /sbin/service tomcat_g stop
  /sbin/service grouper_v2_3_loader stop
  ```

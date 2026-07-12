---
title: "Improve GSH"
space: GrIntDev
pageId: 48792861
version: 11
lastUpdated: 2026-07-12T17:02:40.585Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792861/Improve+GSH
---

- Grouper 2.3 uses BeanShell. There's no tab completion, history, etc.
- Grouper 2.3 with patches and Grouper 2.4+ switches to Groovy as the default shell.
- The goal was to keep any existing GSH scripts that deployers may have written as compatible as possible with the [new Groovy based GSH.](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh)
- Built in GSH commands (e.g. obliterateStem) were modified to work with both BeanShell and Groovy.
- There's an option to switch back to BeanShell by specifying the following in grouper.properties
  
  
  
  | `gsh.useLegacy =``true` |
  | --- |
- gsh.sh and gsh.bat were not changed.
- Users can still use another shell as they did before (e.g. [https://github.com/wgthom/groovysh4grouper](https://github.com/wgthom/groovysh4grouper))

This is how [GSH](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545249/GrouperShell+gsh) would start up before the 2.3 patch that contains this change (with an example built in gsh command - obliterateStem). (There's no change.):

```
shilen@Shilens-MacBook-Pro-2 grouper (GRP-1553) $ ./bin/gsh.sh 
Using GROUPER_HOME: /Users/shilen/Desktop/internet2/github2/grouper
Using GROUPER_CONF: /Users/shilen/Desktop/internet2/github2/grouper/conf
Using JAVA: java
using MEMORY: 64m-750m
Grouper starting up: version: 2.3.0, build date: 2017/06/02 16:18:52, env: <no label configured>
grouper.properties read from: /Users/shilen/Desktop/internet2/github2/grouper/conf/grouper.properties
Grouper current directory is: /Users/shilen/Desktop/internet2/github2/grouper
log4j.properties read from:   /Users/shilen/Desktop/internet2/github2/grouper/conf/log4j.properties
Grouper is logging to file:   /Users/shilen/Desktop/internet2/github2/grouper/logs/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /Users/shilen/Desktop/internet2/github2/grouper/conf/grouper.hibernate.properties
grouper.hibernate.properties: sa@jdbc:hsqldb:hsql://localhost:9001/grouper
subject.properties read from: /Users/shilen/Desktop/internet2/github2/grouper/conf/subject.properties
sources.xml read from:        /Users/shilen/Desktop/internet2/github2/grouper/conf/sources.xml
sources configured in:        sources.xml
sources.xml        groupersource id: g:gsa
sources.xml        groupersource id: grouperEntities
sources.xml        jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Grouper warning: jarfile mismatch, expecting name: 'subject.jar' size: LinkedHashSet size: 2: [0]: 271917
[1]: 266056
 manifest version: 2.2.0.  However the jar detected is: /Users/shilen/Desktop/internet2/github2/grouper/lib/grouper/subject.jar, name: subject.jar size: 271917 manifest version: 2.3.0
Type help() for instructions
gsh 0% obliterateStem("etc:grouperUi", true, true)
Would obliterate stem: etc:grouperUi
Would be done deleting group: etc:grouperUi:grouperUiUserData
Would be done obliterating stem: etc:grouperUi
true
gsh 1% 

```

And after the patch to use Groovy:

```
shilen@Shilens-MacBook-Pro-2 grouper (GRP-1553) $ ./bin/gsh.sh 
Using GROUPER_HOME: /Users/shilen/Desktop/internet2/github2/grouper
Using GROUPER_CONF: /Users/shilen/Desktop/internet2/github2/grouper/conf
Using JAVA: java
using MEMORY: 64m-750m
Grouper starting up: version: 2.3.0, build date: 2017/06/02 16:18:52, env: <no label configured>
grouper.properties read from: /Users/shilen/Desktop/internet2/github2/grouper/conf/grouper.properties
Grouper current directory is: /Users/shilen/Desktop/internet2/github2/grouper
log4j.properties read from:   /Users/shilen/Desktop/internet2/github2/grouper/conf/log4j.properties
Grouper is logging to file:   /Users/shilen/Desktop/internet2/github2/grouper/logs/grouper_error.log, at min level WARN for package: edu.internet2.middleware.grouper, based on log4j.properties
grouper.hibernate.properties: /Users/shilen/Desktop/internet2/github2/grouper/conf/grouper.hibernate.properties
grouper.hibernate.properties: sa@jdbc:hsqldb:hsql://localhost:9001/grouper
subject.properties read from: /Users/shilen/Desktop/internet2/github2/grouper/conf/subject.properties
sources.xml read from:        /Users/shilen/Desktop/internet2/github2/grouper/conf/sources.xml
sources configured in:        sources.xml
sources.xml        groupersource id: g:gsa
sources.xml        groupersource id: grouperEntities
sources.xml        jdbc source id:   jdbc: GrouperJdbcConnectionProvider
Grouper warning: jarfile mismatch, expecting name: 'subject.jar' size: LinkedHashSet size: 2: [0]: 271917
[1]: 266056
 manifest version: 2.2.0.  However the jar detected is: /Users/shilen/Desktop/internet2/github2/grouper/lib/grouper/subject.jar, name: subject.jar size: 271917 manifest version: 2.3.0
Type help() for instructions
Groovy Shell (2.4.11, JVM: 1.8.0_121)
Type ':help' or ':h' for help.
---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
groovy:000> :load /Users/shilen/Desktop/internet2/github2/grouper//conf/groovysh.profile
groovy:000> obliterateStem("etc:grouperUi", true, true)
Would obliterate stem: etc:grouperUi
Would be done deleting group: etc:grouperUi:grouperUiUserData
Would be done obliterating stem: etc:grouperUi
===> true
groovy:000> 
```

(That first load command is done automatically on start up.)

Also, other parameters to gsh (e.g. -registry, -runarg, etc) continue working as before.

And normal Java calls continue to work as well, e.g.

```
groovy:000> grouperSession = GrouperSession.startRootSession()
===> 5c455cfc3511413abdb20276c45f8d7d,'GrouperSystem','application'
groovy:000> new GroupSave(grouperSession).assignName("stem1:a").assignCreateParentStemsIfNotExist(true).save();
===> Group[name=stem1:a,uuid=535a12b070e54474bf67f0e538adebe8]
groovy:000> 
```

See also

Grouper Shell

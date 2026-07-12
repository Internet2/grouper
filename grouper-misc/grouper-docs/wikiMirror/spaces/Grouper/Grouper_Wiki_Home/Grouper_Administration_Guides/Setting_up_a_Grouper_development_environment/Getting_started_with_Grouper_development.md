---
title: "Getting started with Grouper development"
space: Grouper
pageId: 48824688
version: 7
lastUpdated: 2015-01-28T22:53:04.946Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/48824688/Getting+started+with+Grouper+development
---

### Background

The purpose of this document is to describe the necessary set of steps needed to assist a developer reach a functional grouper deployment from the grouper source tree.

### Requirements

1. The environment must have the Java Development Toolkit installed. Ensure that JAVA_HOME is set to the JDK installation folder. JDK 6~8 should all work.

2. The environment must have Apache Tomcat installed. Ensure that CATALINA_HOME is set to the Tomcat installation folder. Tomcat 6 is the preferred version.

### Source Checkout

Checkout the grouper source from the project source repository:

```bash
git checkout git@github.com:Internet2/grouper.git
```

### Build

1. Navigate to grouper-ui\webapp\WEB-INF\build.properties and specify the path to your Tomcat installation folder.

```bash
...
domain=/Users/apache-tomcat/webapps
serverhome=/Users/apache-tomcat
```

2. From the grouper root directory, execute:

```bash
ant all
```

The result will be **GroupsManager.war** that will exist inside the **${domain}** directory.

### Test

1. Modify your **${server}/conf/tomcat-users.xml** file to include the following snippet:

```html/xml
<role rolename="grouper_user"/>
<user username="GrouperSystem" password="GrouperSystem" roles="grouper_user"/>
```

2. Open up a command prompt, and navigate to grouper/grouper directory and execute:

```html/xml
start-hsql.[sh|bat]
```

This will spin up an HSQL database instance. Keep this running in the background.

3. Navigate to [http://localhost:8080/GroupsManager](http://localhost:8080/GroupsManager), authenticate with **GrouperSystem/GrouperSystem** and you should see the grouper home page.

### Debugging

1. In your Tomcat\bin directory, locate the **startup.[bat|sh]** and include the following lines at the top:

```bash
set JPDA_ADDRESS=8000
set JPDA_TRANSPORT=dt_socket
```

2. In your IDE (Eclipse or IntelliJ IDEA) set up a remote debugger that would connect to port 8000. Here is a snapshot of what it would look like in IDEA:

3. Restart Tomcat. In the tomcat log, you should see an entry that says:

```bash
Listening for transport dt_socket at address: 8000
...
```

4. In your IDE, attach the debugger to the new remote debugger (Tomcat above) you created by starting a new debug session.

5. Set breakpoints and interact with grouper. The debugger should halt on each breakpoint.

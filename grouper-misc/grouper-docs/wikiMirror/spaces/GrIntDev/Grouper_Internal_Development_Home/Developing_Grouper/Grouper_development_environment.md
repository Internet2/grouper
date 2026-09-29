---
title: "Grouper development environment"
space: GrIntDev
pageId: 48793047
version: 24
lastUpdated: 2026-09-25T20:15:11.373Z
url: https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48793047/Grouper+development+environment
---

This page is for Grouper developers.

Note the coding standards: [Grouper developers coding standards](https://docs.grouper.internet2.edu/wiki/pages/createpage.action?spaceKey=Grouper&title=Grouper%20developers%20coding%20standards)

# General tips

- You should be using Eclipse, but you can also use the IDE of your choice
  
  
  
  - You should have webtools with tomcat 8.5
- For profiling we use YourKit  
    
  [http://yourkit.com](http://yourkit.com)
  
  
  
  - YourKit supports open source projects with innovative and intelligent tools for monitoring and profiling Java and .NET applications.
  - YourKit is the creator of [YourKit Java Profiler](https://urldefense.proofpoint.com/v2/url?u=https-3A__www.yourkit.com_java_profiler_&d=DwIDaQ&c=imBPVzF25OnBgGmVOlcsiEgHoG1i6YHLR0Sj_gZ4adc&r=sWqutME58phurE0oO57Icg&m=Hx-iCxUAiVhWKxOvpb8KWW_KufUXtyhEHeB5_n6gzQY&s=hziAbLdeLAoCIsGneodTSEmmycB3XQoM9Zwr3M-LMYg&e=), [YourKit .NET Profiler](https://urldefense.proofpoint.com/v2/url?u=https-3A__www.yourkit.com_.net_profiler_&d=DwIDaQ&c=imBPVzF25OnBgGmVOlcsiEgHoG1i6YHLR0Sj_gZ4adc&r=sWqutME58phurE0oO57Icg&m=Hx-iCxUAiVhWKxOvpb8KWW_KufUXtyhEHeB5_n6gzQY&s=rdZ5AY9ZS_5MZ-Zlt49N7TAKnJLuUT_pzM2cjBOD7Zw&e=), [YourKit YouMonitor](https://urldefense.proofpoint.com/v2/url?u=https-3A__www.yourkit.com_youmonitor_&d=DwIDaQ&c=imBPVzF25OnBgGmVOlcsiEgHoG1i6YHLR0Sj_gZ4adc&r=sWqutME58phurE0oO57Icg&m=Hx-iCxUAiVhWKxOvpb8KWW_KufUXtyhEHeB5_n6gzQY&s=wEduGbneXT80mHkt9fKw19WlMA3Le88SfkjP2aFEN9E&e=)
- Checkout the git repo for GROUPER_2_5_BRANCH (main)
- Install mysql (or whatever database you want, probably better to not use hsql, though you could)
- Dbeaver
  
  
  
  - Make sure to set to not do uppercase (doesnt work on mysql linux)
- You can use as a maven project, or ant project
- Do an ant build
- Configure the grouper-hibernate.properties for database
- Do a gsh -registry -runscript
- Start GSH and run some commands
- Have the UI include a source folder from the API for source and conf
- Add a tomcat to eclipse
- Do an "ant dev" in the UI to get the libs in WEB-INF/lib
- Download the required tomcat version (e.g. tomcat 8.5)
- Make sure API has all libs exported
- ui and ws (and others) have a project dependency on grouper)
- Map the webapp dir to tomcat, UI should start
- Add tomcat users for GrouperSystem and test.subject.0, set your password
- Do an "ant dev" in the WS to get the libs in WEB-INF/lib

Map the webapp dir to tomcat, WS should start, try a call from the client

# Eclipse setup

## Project setup

TODO

## Checkstyle plugin

Grouper uses [Checkstyle](https://checkstyle.sourceforge.io/) configuration files to scan for general style issues in source code. These are in the grouper-parent/src/checkstyle project directory, and can be used to set up IDE background checking on Java files.

Go to Help→Eclipse Marketplace, and install "Checkstyle Plugin (eclipse-cs)". Click OK if needed to install unsigned content

Configure via File→ Properties→ Checkstyle→ Local Check Configurations

Click New... to create the local configuration. Choose Type "Project Relative Configuration". The location of the file will be under /grouper-parent/src/checkstyle. File checkstyle-legacy.xml just has a few checks and is geared toward older code with more issues, while checkstyle.xml has more checks and is better for newer files.

On the main tab, check "Checkstyle active for this project", choose the newly created configuration. To adjust the number of warnings, you can check more boxes under the "Exclude from checking" choices.

Sample output:

# IntelliJ IDEA setup

## Project setup

Projects -> Open

{GIT}/grouper/grouper-parent/pom.xml -> Open as Project

Wait a few minutes (6+) while it downloads dependencies and structures the subprojects

When it's finished, you should see a folder in bold for all the subprojects associated with the release:

## Checkstyle plugin

Install the plugin: File -> Settings -> Plugins -> CheckStyle IDEA

Restart the IDE as directed

note88d5fffb6a7bIn order to make the Checkstyle IDEA plugin work in modern IDEA versions, you must make these changes if they are not already upstreamed in Git. Otherwise, the plugin will fail to consume Grouper’s Checkstyle configs, let alone run them.

In order to make the Checkstyle IDEA plugin work in modern IDEA versions, you must make these changes if they are not already upstreamed in Git. Otherwise, the plugin will fail to consume Grouper’s Checkstyle configs, let alone run them.

Ensure your local copy of the Maven POM uses at least version 3.6.0 of the Maven Checkstyle plugin:

```diff
--- a/grouper-parent/pom.xml
+++ b/grouper-parent/pom.xml
@@ -49,14 +49,14 @@
         <maven.build.timestamp.format>yyyy/MM/dd HH:mm:ss Z</maven.build.timestamp.format>
 
         <!-- Plugin Versions -->
-        <maven-checkstyle-plugin.version>3.1.0</maven-checkstyle-plugin.version>
+        <maven-checkstyle-plugin.version>3.6.0</maven-checkstyle-plugin.version>
         <maven-javadoc-plugin.version>3.12.0</maven-javadoc-plugin.version>
         <maven-jar-plugin.version>2.5</maven-jar-plugin.version>
         <maven-source-plugin.version>2.3</maven-source-plugin.version>
```

Then, adjust `{GIT}/grouper-parent/src/checkstyle/checkstyle.xml` to fix breaking Checkstyle changes in versions [8.24](https://checkstyle.sourceforge.io/release-notes-old-8-0-8-34.html#Release_8.24) and [10.22.0](https://checkstyle.org/release-notes-old-8-35-10-26.html#Release_10.22.0). Namely, the `LineLength` module must be moved to the same level as `TreeWalker`, and the `JavadocVariable` module needs its configured property name to change from `scope` to `accessModifiers`, respectively.

```diff
--- a/grouper-parent/src/checkstyle/checkstyle.xml
+++ b/grouper-parent/src/checkstyle/checkstyle.xml
@@ -37,7 +37,9 @@ limitations under the License.
     <property name="eachLine" value="true"/>
     <property name="fileExtensions" value="java,xml"/>
   </module>
-
+  <module name="LineLength">
+    <property name="max" value="200"/>
+  </module>
   <module name="RegexpSingleline">
     <property name="format" value="\s+$"/>
     <property name="message" value="Line has trailing spaces."/>
@@ -94,9 +96,6 @@ limitations under the License.
   <module name="TreeWalker">
     <property name="tabWidth" value="2"/>
 
-    <module name="LineLength">
-      <property name="max" value="200"/>
-    </module>
 
     <!--<module name="AvoidStarImport"/>--><!-- want this? -->
     <module name="IllegalImport"/>
@@ -136,7 +135,7 @@ limitations under the License.
     -->
     <module name="MissingDeprecated"/>
     <module name="JavadocVariable">
-      <property name="scope" value="public"/>
+      <property name="accessModifiers" value="public"/>
     </module>
 
   </module>
```

File -> Settings -> Tools -> Checkstyle

Add configuration file:

- Name: Grouper checkstyle
- Location: {GIT}/grouper-parent/src/checkstyle/checkstyle.xml

Add configuration file:

- Name: Grouper checkstyle - legacy
- Location: {GIT}/grouper-parent/src/checkstyle/checkstyle-legacy.xml

Choose checkstyle version 8.23 (the Grouper checkstyle.xml is not compatible with higher versions)

> As of September 2026, compatibility with *much* more recent versions of Checkstyle can be attained via two small changes described above. The changes above were tested and working with at least Checkstyle version 14.1.0.

When you create a new Java class, there is a Checkstyle docked tab. You can scan your class (use the Grouper checkstyle and not the legacy one) before committing to make sure standards have been met.

## Coding defaults

- Editor -> Code style:
  
  
  
  - Scheme: Project
  - Line separator: \n (applies to new files)
  - Hard wrap: 200 characters
  - Visual guides: 120, 200
  - Java:
    
    
    
    - Tabs and indents:
      
      
      
      - Tab size: 2
      - Indent: 2
      - Continuation indent: 8
      - (Off) Keep indents on empty lines (actual use of indented blank lines in Grouper is about 50/50)
    - Imports:
      
      
      
      - Class count to use import with '*': 999 (i.e., never collapse imports)
      - Names count to use import with '*': 999 (i.e., never collapse imports)
- Editor -> File and Code Templates:
  
  
  
  - Scheme: Project
  - For Class, Interface, and Enum, move `#parse("File Header.java")` from the second to the first line
  - Includes -> File Header (should already be defined), paste this

> /****  
> * Copyright 2022 Internet2  
> *  
> * Licensed under the Apache License, Version 2.0 (the "License");  
> * you may not use this file except in compliance with the License.  
> * You may obtain a copy of the License at  
> *  
> * http://www.apache.org/licenses/LICENSE-2.0  
> *  
> * Unless required by applicable law or agreed to in writing, software  
> * distributed under the License is distributed on an "AS IS" BASIS,  
> * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
> * See the License for the specific language governing permissions and  
> * limitations under the License.  
> ***/

# Miscellaneous

## Patch java example

This is not really Grouper specific, but if you want to patch a jar in a webapp, here is an example:

```
 cd /tmp
 mkdir grouper
 cd grouper
 mkdir src
 cd src
 unzip /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/lib/grouper.jar
 ### note, might want to delete all other source and class files except the one you need ###
 emacs edu/internet2/middleware/grouper/app/loader/GrouperLoaderType.java

 javac -cp .:/opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/lib/subject.jar:/opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/lib/commons-lang.jar:/opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/lib/log4j.jar:/opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/lib/hibernate.jar:/opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/lib/commons-logging.jar:/opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/lib/DdlUtils.jar:/opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/lib/quartz.jar:/opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/lib/commons-collections.jar:/opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/lib/grouper.jar -sourcepath . edu/internet2/middleware/grouper/app/loader/GrouperLoaderType.java

 mkdir /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/classes/edu/internet2
 mkdir /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/classes/edu/internet2/middleware
 mkdir /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/classes/edu/internet2/middleware/grouper
 mkdir /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/classes/edu/internet2/middleware/grouper/app
 mkdir /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader
 cp edu/internet2/middleware/grouper/app/loader/GrouperLoaderType* /opt/appserv/tomcat_3c/webapps/fastGrouperProdDaemon/WEB-INF/classes/edu/internet2/middleware/grouper/app/loader

```

# **See also**

[Grouper developers coding standards](https://grouper.atlassian.net/wiki/spaces/GrIntDev/pages/48792568/Grouper+developers+coding+standards)

Page down here for a section on Guidelines for Contribs to Grouper

- [Community Contributions](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28541859/Community+Contributions)

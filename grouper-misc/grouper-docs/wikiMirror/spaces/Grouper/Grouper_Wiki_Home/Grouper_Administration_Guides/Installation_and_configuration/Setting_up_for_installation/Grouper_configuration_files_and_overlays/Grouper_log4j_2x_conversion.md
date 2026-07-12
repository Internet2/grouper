---
title: "Grouper log4j 2x conversion"
space: Grouper
pageId: 28554504
version: 9
lastUpdated: 2026-07-01T05:40:18.753Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554504/Grouper+log4j+2x+conversion
---

This is for 2.6.8+

## Legacy log4j.properties

There were two log4 properties files depending on if logging to container or to file

log4j.properties (file)

```

#
# Copyright 2014 Internet2
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#${grouper.home} will be substituted with the System property "grouper.home", which must have a trailing \ or / 
# depending on your OS. Of course you can use absolute paths if you prefer 

#
# log4j Configuration
# $Id: log4j.example.properties,v 1.13 2009-12-18 13:56:51 tzeller Exp $
#

# Appenders

## Log messages to stderr
log4j.appender.grouper_stderr = org.apache.log4j.ConsoleAppender
log4j.appender.grouper_stderr.Target = System.err
log4j.appender.grouper_stderr.layout = org.apache.log4j.PatternLayout
log4j.appender.grouper_stderr.layout.ConversionPattern = %d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n
 
## Grouper API error logging
log4j.appender.grouper_error = org.apache.log4j.DailyRollingFileAppender
log4j.appender.grouper_error.File = /opt/grouper/logs/grouper.log
log4j.appender.grouper_error.DatePattern = '.'yyyy-MM-dd
log4j.appender.grouper_error.MaxBackupIndex = 30
log4j.appender.grouper_error.layout = org.apache.log4j.PatternLayout
log4j.appender.grouper_error.layout.ConversionPattern = %d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n
 
log4j.appender.grouper_daemon = org.apache.log4j.DailyRollingFileAppender
log4j.appender.grouper_daemon.File = /opt/grouper/logs/grouperDaemon.log
log4j.appender.grouper_daemon.DatePattern = '.'yyyy-MM-dd
log4j.appender.grouper_daemon.MaxBackupIndex = 30
log4j.appender.grouper_daemon.layout = org.apache.log4j.PatternLayout
log4j.appender.grouper_daemon.layout.ConversionPattern = %d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n
 
log4j.appender.grouper_pspng = org.apache.log4j.DailyRollingFileAppender
log4j.appender.grouper_pspng.File = /opt/grouper/logs/pspng.log
log4j.appender.grouper_pspng.DatePattern = '.'yyyy-MM-dd
log4j.appender.grouper_pspng.MaxBackupIndex = 30
log4j.appender.grouper_pspng.layout = org.apache.log4j.PatternLayout
log4j.appender.grouper_pspng.layout.ConversionPattern = %d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n
 
log4j.appender.grouper_provisioning = org.apache.log4j.DailyRollingFileAppender
log4j.appender.grouper_provisioning.File = /opt/grouper/logs/provisioning.log
log4j.appender.grouper_provisioning.DatePattern = '.'yyyy-MM-dd
log4j.appender.grouper_provisioning.MaxBackupIndex = 30
log4j.appender.grouper_provisioning.layout = org.apache.log4j.PatternLayout
log4j.appender.grouper_provisioning.layout.ConversionPattern = %d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n
 
 
# Loggers
 
## Default logger; will log *everything*
log4j.rootLogger = ERROR, grouper_stderr, grouper_error
 
 ## All Internet2 (warn to grouper_error per default logger)
log4j.logger.edu.internet2.middleware = WARN

log4j.logger.edu.internet2.middleware.grouper.app.loader.GrouperLoaderLog = DEBUG, grouper_daemon
log4j.additivity.edu.internet2.middleware.grouper.app.loader.GrouperLoaderLog = false
 
log4j.logger.edu.internet2.middleware.grouper.pspng = INFO, grouper_pspng
log4j.additivity.edu.internet2.middleware.grouper.pspng = false

log4j.logger.edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectLog = DEBUG, grouper_provisioning
log4j.additivity.edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectLog = false

log4j.logger.edu.internet2.middleware.grouper.app.syncToGrouper.SyncToGrouperFromSqlDaemon = DEBUG

log4j.logger.edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogCommands = DEBUG

log4j.logger.edu.internet2.middleware.grouper.stem.StemViewPrivilegeEsbListener = DEBUG

log4j.logger.edu.internet2.middleware.grouper.stem.StemViewPrivilegeFullDaemonLogic = DEBUG

#######################################################
##Optional settings for debug logs
#######################################################

## Hooks debug info
#log4j.logger.edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook = DEBUG
#log4j.logger.edu.internet2.middleware.grouper.Group = DEBUG

#log4j.logger.edu.internet2.middleware.grouper.hooks.examples.GroupTypeSecurityHook = DEBUG

# added by grouper-installer
log4j.logger.org.apache.tools.ant = WARN

log4j.logger.edu.internet2.middleware.grouper.util.PerformanceLogger = INFO
```

log4j.properties (container pipe)

```

#
# Copyright 2014 Internet2
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#${grouper.home} will be substituted with the System property "grouper.home", which must have a trailing \ or / 
# depending on your OS. Of course you can use absolute paths if you prefer 

#
# log4j Configuration
# $Id: log4j.example.properties,v 1.13 2009-12-18 13:56:51 tzeller Exp $
#

# Appenders

## Grouper API error logging
log4j.appender.grouper_error                            = org.apache.log4j.FileAppender
log4j.appender.grouper_error.file                       = /tmp/logpipe
log4j.appender.grouper_error.append                     = true
log4j.appender.grouper_error.layout                     = org.apache.log4j.PatternLayout
log4j.appender.grouper_error.layout.ConversionPattern   = __GROUPER_LOG_PREFIX__;grouper_error.log;${ENV};${USERTOKEN};%d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n
#log4j.appender.grouper_error.layout.ConversionPattern   = %d{ISO8601}: %m%n

log4j.appender.grouper_daemon = org.apache.log4j.DailyRollingFileAppender
log4j.appender.grouper_daemon.File = /tmp/logpipe
log4j.appender.grouper_daemon.append = true
log4j.appender.grouper_daemon.layout = org.apache.log4j.PatternLayout
log4j.appender.grouper_daemon.layout.ConversionPattern = __GROUPER_LOG_PREFIX__;grouperDaemon.log;${ENV};${USERTOKEN};%d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n

log4j.appender.grouper_pspng = org.apache.log4j.FileAppender
log4j.appender.grouper_pspng.File = /tmp/logpipe
log4j.appender.grouper_pspng.append = true
log4j.appender.grouper_pspng.layout = org.apache.log4j.PatternLayout
log4j.appender.grouper_pspng.layout.ConversionPattern = __GROUPER_LOG_PREFIX__;pspng.log;${ENV};${USERTOKEN};%d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n

log4j.appender.grouper_provisioning                               = org.apache.log4j.FileAppender
log4j.appender.grouper_provisioning.file                          = /tmp/logpipe
log4j.appender.grouper_provisioning.append                        = true
log4j.appender.grouper_provisioning.layout                        = org.apache.log4j.PatternLayout
log4j.appender.grouper_provisioning.layout.ConversionPattern      = __GROUPER_LOG_PREFIX__;provisioning.log;${ENV};${USERTOKEN};%d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n

# Loggers

## Default logger; will log *everything*
log4j.rootLogger  = ERROR, grouper_error

## All Internet2 (warn to grouper_error per default logger)
log4j.logger.edu.internet2.middleware = WARN

log4j.logger.edu.internet2.middleware.grouper.app.loader.GrouperLoaderLog = DEBUG, grouper_daemon
log4j.additivity.edu.internet2.middleware.grouper.app.loader.GrouperLoaderLog = false
 
log4j.logger.edu.internet2.middleware.grouper.pspng = INFO, grouper_pspng
log4j.additivity.edu.internet2.middleware.grouper.pspng = false

log4j.logger.edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectLog = DEBUG, grouper_provisioning
log4j.additivity.edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectLog = false

log4j.logger.edu.internet2.middleware.grouper.app.syncToGrouper.SyncToGrouperFromSqlDaemon = DEBUG

log4j.logger.edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogCommands = DEBUG

log4j.logger.edu.internet2.middleware.grouper.stem.StemViewPrivilegeEsbListener = DEBUG

log4j.logger.edu.internet2.middleware.grouper.stem.StemViewPrivilegeFullDaemonLogic = DEBUG

#######################################################
##Optional settings for debug logs
#######################################################

## Hooks debug info
#log4j.logger.edu.internet2.middleware.grouper.hooks.examples.GroupTypeTupleIncludeExcludeHook = DEBUG
#log4j.logger.edu.internet2.middleware.grouper.Group = DEBUG

#log4j.logger.edu.internet2.middleware.grouper.hooks.examples.GroupTypeSecurityHook = DEBUG

# added by grouper-installer
log4j.logger.org.apache.tools.ant = WARN

log4j.logger.edu.internet2.middleware.grouper.util.PerformanceLogger = INFO
```

## New log4j2.xml

This is in the container: [https://github.internet2.edu/docker/grouper/tree/2.6.7.1/container_files/api](https://github.internet2.edu/docker/grouper/tree/2.6.7.1/container_files/api)

```
<?xml version="1.0" encoding="utf-8"?>
<Configuration status="info">
    <Properties>
        <Property name="layout">%d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n</Property>
    </Properties>
    <Appenders>
        <File name="CATALINA" fileName="/tmp/logpipe">
            <PatternLayout pattern="tomee;catalina.out;${env:ENV};${env:USERTOKEN};${layout}"/>
        </File>
        <Console name="stderr" target="SYSTEM_ERR">
          <PatternLayout pattern="__GROUPER_LOG_PREFIX__;${ENV};${USERTOKEN};${layout}"/>
        </Console>
        <File name="__LOGPIPE__grouper_error" fileName="/tmp/logpipe">
            <PatternLayout pattern="__GROUPER_LOG_PREFIX__;grouper_error.log;${ENV};${USERTOKEN};${layout}"/>
        </File>
        <RollingFile name="__FILE__grouper_error" fileName="/opt/grouper/logs/grouper.log" filePattern="/opt/grouper/logs/grouper.log.%d{yyyy-MM-dd}" >
            <PatternLayout pattern="__GROUPER_LOG_PREFIX__;grouper_error.log;${ENV};${USERTOKEN};${layout}"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1"/>
            </Policies>
            <DefaultRolloverStrategy max="30" />
        </RollingFile>
        <File name="__LOGPIPE__grouper_daemon" fileName="/tmp/logpipe">
            <PatternLayout pattern="__GROUPER_LOG_PREFIX__;grouperDaemon.log;${ENV};${USERTOKEN};${layout}"/>
        </File>
        <RollingFile name="__FILE__grouper_daemon" fileName="/opt/grouper/logs/grouperDaemon.log" filePattern="/opt/grouper/logs/grouperDaemon.log.%d{yyyy-MM-dd}" >
            <PatternLayout pattern="__GROUPER_LOG_PREFIX__;grouperDaemon.log;${ENV};${USERTOKEN};${layout}"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1"/>
            </Policies>
            <DefaultRolloverStrategy max="30" />
        </RollingFile>
        <File name="__LOGPIPE__grouper_pspng" fileName="/tmp/logpipe">
            <PatternLayout pattern="__GROUPER_LOG_PREFIX__;pspng.log;${ENV};${USERTOKEN};${layout}"/>
        </File>
        <RollingFile name="__FILE__grouper_pspng" fileName="/opt/grouper/logs/pspng.log" filePattern="/opt/grouper/logs/pspng.log.%d{yyyy-MM-dd}" >
            <PatternLayout pattern="__GROUPER_LOG_PREFIX__;pspng.log;${ENV};${USERTOKEN};${layout}"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1"/>
            </Policies>
            <DefaultRolloverStrategy max="30" />
        </RollingFile>
        <File name="__LOGPIPE__grouper_provisioning" fileName="/tmp/logpipe">
            <PatternLayout pattern="__GROUPER_LOG_PREFIX__;provisioning.log;${ENV};${USERTOKEN};${layout}"/>
        </File>
        <RollingFile name="__FILE__grouper_provisioning" fileName="/opt/grouper/logs/provisioning.log" filePattern="/opt/grouper/logs/provisioning.log.%d{yyyy-MM-dd}" >
            <PatternLayout pattern="__GROUPER_LOG_PREFIX__;provisioning.log;${ENV};${USERTOKEN};${layout}"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1"/>
            </Policies>
            <DefaultRolloverStrategy max="30" />
        </RollingFile>     

        <!--MOREAPPENDERS-->

    </Appenders>
    <Loggers>
        <Root level="error">
            <AppenderRef ref="grouper_error"/>
        </Root>
        <Logger name="org.apache.catalina" level="info" additivity="false">
            <AppenderRef ref="CATALINA"/>
        </Logger>
        <Logger name="edu.internet2.middleware" level="warn" additivity="false">
            <AppenderRef ref="grouper_error"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.app.loader.GrouperLoaderLog" level="debug" additivity="false">
            <AppenderRef ref="grouper_daemon"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.pspng" level="info" additivity="false">
            <AppenderRef ref="grouper_pspng"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectLog" level="debug" additivity="false">
            <AppenderRef ref="grouper_provisioning"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.app.syncToGrouper.SyncToGrouperFromSqlDaemon" level="debug" additivity="false">
            <AppenderRef ref="grouper_error"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogCommands" level="debug" additivity="false">
            <AppenderRef ref="grouper_error"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.stem.StemViewPrivilegeEsbListener" level="debug" additivity="false">
            <AppenderRef ref="grouper_error"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.stem.StemViewPrivilegeFullDaemonLogic" level="debug" additivity="false">
            <AppenderRef ref="grouper_error"/>
        </Logger>
        <Logger name="org.apache.tools.ant" level="warn" additivity="false">
            <AppenderRef ref="grouper_error"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.util.PerformanceLogger" level="info" additivity="false">
            <AppenderRef ref="grouper_error"/>
        </Logger>
        <!--MORELOGGERS-->
    </Loggers>
</Configuration>
```

## Working example of log4j2.xml

```
<?xml version="1.0" encoding="utf-8"?>
<Configuration status="info">
    <Properties>
        <Property name="layout">%d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %x - %m%n</Property>
    </Properties>
    <Appenders>
        <Console name="stderr" target="SYSTEM_ERR">
          <PatternLayout pattern="${layout}"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="error">
            <AppenderRef ref="stderr"/>
        </Root>
        <Logger name="edu.internet2.middleware" level="warn" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.app.loader.GrouperLoaderLog" level="debug" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.pspng" level="info" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectLog" level="debug" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.app.syncToGrouper.SyncToGrouperFromSqlDaemon" level="debug" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLogCommands" level="debug" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.stem.StemViewPrivilegeEsbListener" level="debug" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.stem.StemViewPrivilegeFullDaemonLogic" level="debug" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>
        <Logger name="org.apache.tools.ant" level="warn" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>
        <Logger name="edu.internet2.middleware.grouper.util.PerformanceLogger" level="info" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>
        
    </Loggers>
</Configuration>
```

Both files (log to files or logpipe) are merged into one config file.

These variables are substituted

- __GROUPER_LOG_PREFIX__ from the startup for various types of servers (ws/ui/daemon)
- __LOGPIPE__ removed if logging to log pipe container
- __FILE__ removed if logging to file
- <!--MORELOGGERS--> will be replaced with the contents of /opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.additionalLoggers.xml.txt
  
  - If you want more loggers, put them in that file e.g.
    
    
    ```
    <Logger name="edu.internet2.middleware.grouper.a.b.c" level="debug" additivity="false">
      <AppenderRef ref="grouper_error"/>
    </Logger>
    ```
- <!--MOREAPPENDERS--> will be replaced with the contents of /opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.additionalAppenders.xml.txt
  
  - If you want more appenders, put them in that file e.g.
    
    
    ```
    <RollingFile name="some_appender_name" fileName="/opt/grouper/logs/someFile.log"
        filePattern="/opt/grouper/logs/someFile.log.%d{yyyy-MM-dd}" >
      <PatternLayout pattern="someFile.log;${ENV};${USERTOKEN};${layout}"/>
      <Policies>
        <TimeBasedTriggeringPolicy interval="1"/>
      </Policies>
      <DefaultRolloverStrategy max="30" />
    </RollingFile>
    ```

## Customize the log4j2.xml

You can:

- (v2.6.8+) Put appenders in XML form in /opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.additionalAppenders.xml.txt
- For loggers you have some options:
  
  - Put loggers in XML form in /opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.additionalLoggers.xml.txt
  - Put loggers in the [grouper.properties or database config](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554619/Grouper+logging+dynamic+configuration)
- You can overlay the /opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.xml
  
  - Note, include whatever is needed for tomee since it is copied to /opt/tomee/conf/log4j2.xml

## Grouper and tomee share a config

The /opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.xml is copied to /opt/tomee/conf/log4j2.xml

## Log4j1 no longer needed

You no longer need to provide this VM argument

```
-Dlog4j1.compatibility=true
```

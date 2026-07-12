---
title: "Grouper logging dynamic configuration"
space: Grouper
pageId: 28554619
version: 6
lastUpdated: 2026-07-01T05:39:58.928Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28554619/Grouper+logging+dynamic+configuration
---

In Grouper v2.6.8+ you can dynamically configure logging in Grouper.

Ideally you could use the built-in log4j2.xml, configure if you want to log to file or logpipe, and override log levels in the grouper.properties or in the database.

If you change the configuration in the DB (via the UI), then all Grouper instances pointing to the DB will dynamically reconfigure to the new levels. Note, anything in grouperClient (look at the package) will not work with this. You need to make changes to the log4j config file for those.

By default the configuration will be checked for updates every minute

grouper.properties

```
# if should check for dynamic updates
# {valueType: "boolean", defaultValue: "true" }
# grouper.logging.dynamicUpdates.run =

# sleep for this amount of seconds after each run
# {valueType: "boolean", defaultValue: "60" }
# grouper.logging.dynamicUpdates.checkAfterSeconds =

```

If there is a change from the current logging settings, those will be applied based on these settings

```
# this is the package or class to log, required
# {valueType: "string", regex: "^grouper\\.logger\\.[^.]+\\.name$"}
# grouper.logger.<myLoggerSettingConfigId>.name = 

# required log level, should be one of: off, fatal, error, warn, info, debug, trace, all
# {valueType: "string", regex: "^grouper\\.logger\\.[^.]+\\.level$", formElement: "dropdown", optionValues: ["off", "fatal", "error", "warn", "info", "debug", "trace", "all"] }
# grouper.logger.<myLoggerSettingConfigId>.level = 

# which appender to send logs to (optional), default to the appender for the class logs to, or default to: grouper_error
# e.g. CATALINA, stderr, grouper_error, grouper_daemon, grouper_pspng, grouper_provisioning, grouper_ws, grouper_ws_longRunning
# {valueType: "string", regex: "^grouper\\.logger\\.[^.]+\\.appender$"}
# grouper.logger.<myLoggerSettingConfigId>.appender = 

```

e.g.

```
grouper.logger.WsGrouperLdapAuthentication.name = edu.internet2.middleware.grouper.ws.security.WsGrouperLdapAuthentication
grouper.logger.WsGrouperLdapAuthentication.level = DEBUG
```

e.g. WS logs

```
grouper.logger.grouper_ws.name = edu.internet2.middleware.grouper.ws.util.GrouperWsLog
grouper.logger.grouper_ws.level = debug
```

## Troubleshooting

You can log this process to troubleshoot:

```
In: /opt/grouper/grouperWebapp/WEB-INF/classes/log4j2.additionalLoggers.xml.txt

        <Logger name="edu.internet2.middleware.grouper.log.GrouperLoggingDynamicConfig" level="debug" additivity="false">
            <AppenderRef ref="stderr"/>
        </Logger>

Or dynamically in grouper.properties

grouper.logger.GrouperLoggingDynamicConfig.name = edu.internet2.middleware.grouper.log.GrouperLoggingDynamicConfig
grouper.logger.GrouperLoggingDynamicConfig.level = debug
```

Log entry:

```
2022-02-26T15:12:34,871: [Thread-8] WARN  GrouperLoggingDynamicConfig.checkForUpdates(232) - [] - Dynamically adding logger config 'edu.internet2.middleware.grouper.log.GrouperLoggingDynamicConfigTest'

2022-02-26T15:12:34,871: [Thread-8] DEBUG GrouperLoggingDynamicConfig.checkForUpdates(329) - [] - configIdsSize: 2, appendersToAdd: 1, appendersToDelete: 0, addingLogger_edu.internet2.middleware.grouper.log.GrouperLoggingDynamicConfigTest: true, level_edu.internet2.middleware.grouper.log.GrouperLoggingDynamicConfigTest: DEBUG, needsToAddLogger_edu.internet2.middleware.grouper.log.GrouperLoggingDynamicConfigTest: false, needsToChangeLogger_edu.internet2.middleware.grouper.log.GrouperLoggingDynamicConfigTest: true, appender_edu.internet2.middleware.grouper.log.GrouperLoggingDynamicConfigTest: stderr, currentLevel_edu.internet2.middleware.grouper.log.GrouperLoggingDynamicConfigTest: DEBUG

```

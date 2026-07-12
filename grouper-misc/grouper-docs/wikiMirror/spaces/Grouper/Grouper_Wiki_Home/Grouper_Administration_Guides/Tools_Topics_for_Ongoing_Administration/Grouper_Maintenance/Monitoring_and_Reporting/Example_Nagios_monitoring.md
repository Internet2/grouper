---
title: "Example Nagios monitoring"
space: Grouper
pageId: 28555658
version: 5
lastUpdated: 2026-07-12T15:27:14.553Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28555658/Example+Nagios+monitoring
---

## 

## Monitoring Grouper Functions

Here are tips for monitoring Grouper functions:

- Set up Nagios to check the Grouper Diagnostics [web service status page](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548954/Grouper+health+check+endpoint+healthcheck+status+diagnostics) to be sure the daemons are running and the DB is up etc

- Set up your logs email you so you can see when people have errors (log4j.properties):

```
log4j.appender.grouper_mail=org.apache.log4j.net.SMTPAppender
log4j.appender.grouper_mail.To=address@school.edu
log4j.appender.grouper_mail.From=noreply@school.edu
log4j.appender.grouper_mail.SMTPHost=smtp.server.edu
log4j.appender.grouper_mail.Threshold=ERROR
log4j.appender.grouper_mail.BufferSize=100
log4j.appender.grouper_mail.Subject=Grouper <at:var at:name="envName" /> Error
log4j.appender.grouper_mail.layout= org.apache.log4j.PatternLayout
log4j.appender.grouper_mail.layout.ConversionPattern   = %d{ISO8601}: [%t] %-5p %C{1}.%M(%L) - %m%n

# Loggers

## Default logger; will log *everything*
log4j.rootLogger = WARN, grouper_error

log4j.logger.edu = ERROR,  grouper_mail
log4j.logger.com = ERROR,  grouper_mail
log4j.logger.org = ERROR,  grouper_mail
```

- Periodically check a service that uses XMPP to make sure the messaging is still working

- Check the [daily Grouper report](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545058/Grouper+overall+summary+administrative+report) to get a summary of the total state of your Grouper installation

## Setting Up Notifications

- For XMPP Notification, see XMPP documentation

- For email notification of items such as membership changes, disable date changes, or permission changes, use [rules](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28545173/Grouper+rules) and follow the examples for use cases on email notification.

#### See Also

[Grouper Diagnostics](https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548954/Grouper+health+check+endpoint+healthcheck+status+diagnostics)

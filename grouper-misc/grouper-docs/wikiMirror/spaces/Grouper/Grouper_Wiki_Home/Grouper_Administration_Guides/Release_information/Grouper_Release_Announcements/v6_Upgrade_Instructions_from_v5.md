---
title: "v6 Upgrade Instructions from v5"
space: Grouper
pageId: 28548206
version: 4
lastUpdated: 2026-07-12T06:32:54.986Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28548206/v6+Upgrade+Instructions+from+v5
---

## Summary

Since most of the major changes in v6 are already incorporated into v5, there are only a few areas of concern when upgrading. Make these preparations when upgrading from v5 to v6.

1. Removed unsupported legacy features, unlikely in use. The other legacy provisioners were already gone from v5, but these are additionally removed in v6
  
  1. grouperScim (legacy unsupported version of SCIM, not the supported one)
  2. grouperActivemq (legacy unsupported version of activeMq for a specific use case)
  3. grouper-aws-changelog (legacy unsupported version of AWS for a specific use case)
  4. grouper-tierApiAuthz-connector, tierInstrumentationCollector (unused legacy function)
  5. grouper-messaging-activemq (removed since v5.1.0)
  6. grouper-messaging-aws (removed since v5.1.0)
  7. grouper-messaging-rabbitmq (removed since v5.1.0)
2. The installer jar (grouper-installer-a.b.c.jar) has been removed. If you have a workflow task that was using it, migrate off of it
3. Some Java libraries have significant upgrades or have been removed. Check your gsh scripts (templates, daemon jobs, batch scripts) and custom Java code for usage of:
  
  1. commons-httpclient (classes org.apache.commons.httpclient.*)
  2. json-lib (classes net.sf.json.*) - migrate to Jackson
  3. commons-lang (classes org.apache.commons.lang.*) - migrate to commons-lang3, **this is common in many scripts**
  4. ldaptive V1 and ldaptive-unboundid (migrate to ldaptive V2, major API changes)
  5. org.json (classes org.json.*)
  6. okhttp3 and Retrofit2 (only used by the removed legacy azure provisioner)
4. The Lite UI is totally removed in V6. If you were relying on legacy functionality from it, migrate to other solutions

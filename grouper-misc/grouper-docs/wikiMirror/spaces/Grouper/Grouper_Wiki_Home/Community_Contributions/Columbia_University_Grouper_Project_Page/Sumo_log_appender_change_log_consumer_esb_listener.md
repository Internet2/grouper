---
title: "Sumo log appender change log consumer esb listener"
space: Grouper
pageId: 28544153
version: 3
lastUpdated: 2026-07-01T05:48:48.271Z
url: https://grouper.atlassian.net/wiki/spaces/Grouper/pages/28544153/Sumo+log+appender+change+log+consumer+esb+listener
---

## Get the jar (no dependencies)

[https://github.com/SumoLogic/sumo-log4j-appender](https://github.com/SumoLogic/sumo-log4j-appender)

[https://repo1.maven.org/maven2/com/sumologic/plugins/log4j/sumo-log4j-appender/2.12/sumo-log4j-appender-2.12.jar](https://repo1.maven.org/maven2/com/sumologic/plugins/log4j/sumo-log4j-appender/2.12/sumo-log4j-appender-2.12.jar)

## Java of the listener

```
/**
 * @author mchyzer
 * $Id$
 */
package edu.columbia.sumoLog;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 *
 */
public class ColumbiaSumoLogAppender extends EsbListenerBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ColumbiaSumoLogAppender.class);

  /**
   * 
   */
  public ColumbiaSumoLogAppender() {
  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#dispatchEvent(java.lang.String, java.lang.String)
   */
  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    
    LOG.debug(eventJsonString);
    
    return true;
  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#disconnect()
   */
  @Override
  public void disconnect() {
  }

}

```

## Configure log4j.properties

```
## Log messages to stderr
log4j.appender.sumo                           = com.sumologic.log4j.SumoLogicAppender
log4j.appender.sumo.layout                    = org.apache.log4j.PatternLayout
log4j.appender.sumo.layout.ConversionPattern  = %d{yyyy-MM-dd HH:mm:ss,SSS Z} [%t] %-5p %c - %m%n
log4j.appender.sumo.url                       =<YOUR_URL_HERE>
# Optional parameters for Metadata
log4j.appender.sumo.sourceName                =<YOUR SOURCE NAME>
log4j.appender.sumo.sourceHost                =<YOUR SOURCE HOST>
log4j.appender.sumo.sourceCategory            =<YOUR SOURCE CATEGORY>

log4j.logger.edu.columbia.sumoLog.ColumbiaSumoLogAppender = DEBUG, sumo
log4j.additivity.edu.columbia.sumoLog.ColumbiaSumoLogAppender = false

```

## Configure grouper-loader.properties

```
changeLog.consumer.boxEsb.class = edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbConsumer
changeLog.consumer.boxEsb.quartzCron = 0 * * * * ?
#changeLog.consumer.boxEsb.elfilter = 
changeLog.consumer.boxEsb.publisher.class = edu.columbia.sumoLog.ColumbiaSumoLogAppender

```

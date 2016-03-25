/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;


/**
 *
 */
public class ChangeLogConsumerToMessage extends ChangeLogConsumerBase {

  /**
   * 
   */
  public ChangeLogConsumerToMessage() {
  }

  /**
   * @see edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase#processChangeLogEntries(java.util.List, edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata)
   */
  @Override
  public long processChangeLogEntries(List<ChangeLogEntry> changeLogEntryList,
      ChangeLogProcessorMetadata changeLogProcessorMetadata) {

    long lastProcessed = -1;

    String messagingSystemName = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." 
        + changeLogProcessorMetadata.getConsumerName() + ".messagingSystemName");
    String queueOrTopic = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("changeLog.consumer." 
        + changeLogProcessorMetadata.getConsumerName() + ".queueOrTopic");

    
    for (ChangeLogEntry changeLogEntry : GrouperUtil.nonNull(changeLogEntryList)) {

      try {
        String json = changeLogEntry.toJson(true);
        GrouperMessagingEngine.send(new GrouperMessageSendParam().assignGrouperMessageSystemName(messagingSystemName).assignQueueOrTopicName(queueOrTopic).addMessageBody(json));
        lastProcessed = changeLogEntry.getSequenceNumber();
      } catch (Exception e) {
        LOG.error("Error processing event: " + changeLogEntry.getId(), e);
        changeLogProcessorMetadata.registerProblem(e, "Error processing event: " + changeLogEntry.getId() + ", " + e.getMessage(), changeLogEntry.getSequenceNumber());
        break;
      }
    }
    return lastProcessed;
  
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(ChangeLogConsumerToMessage.class);
  

}

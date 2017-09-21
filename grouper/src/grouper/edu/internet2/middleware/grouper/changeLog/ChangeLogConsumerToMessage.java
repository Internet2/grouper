/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.changeLog;

import java.util.List;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
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
    String queueOrTopicName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("changeLog.consumer." 
        + changeLogProcessorMetadata.getConsumerName() + ".queueOrTopicName");
    String messageQueueType = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("changeLog.consumer." 
        + changeLogProcessorMetadata.getConsumerName() + ".messageQueueType");
    GrouperMessageQueueType grouperMessageQueueType = GrouperMessageQueueType.valueOfIgnoreCase(messageQueueType, true);
    
    String routingKey = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." 
        + changeLogProcessorMetadata.getConsumerName() + ".routingKey", "");
    
    boolean autocreateObjects = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.messaging.settings.autocreate.objects", true);
    
    for (ChangeLogEntry changeLogEntry : GrouperUtil.nonNull(changeLogEntryList)) {

      try {
        String json = changeLogEntry.toJson(true);
        GrouperMessagingEngine.send(new GrouperMessageSendParam().assignGrouperMessageSystemName(messagingSystemName)
            .assignAutocreateObjects(autocreateObjects)
            .assignQueueType(grouperMessageQueueType).assignQueueOrTopicName(queueOrTopicName).addMessageBody(json), routingKey);
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

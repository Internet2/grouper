/**
 * Copyright 2018 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.internet2.middleware.grouper.changeLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   
    String exchangeType = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." 
        + changeLogProcessorMetadata.getConsumerName() + ".exchangeType", "");

    Map<String, Object> queueArguments = null;
    for (int i=0;i<100;i++) {
      String key = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
              + changeLogProcessorMetadata.getConsumerName() + ".queueArgs." + i + ".key");
      if (key == null || "".equals(key)) {
        break;
      }

      String value = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer."
              + changeLogProcessorMetadata.getConsumerName() + ".queueArgs." + i + ".value");
      if (queueArguments == null) {
        queueArguments = new HashMap<>();
      }
      queueArguments.put(key, value);
    }

    boolean autocreateObjects = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.messaging.settings.autocreate.objects", true);
    
    for (ChangeLogEntry changeLogEntry : GrouperUtil.nonNull(changeLogEntryList)) {

      try {
        String json = changeLogEntry.toJson(true);
        GrouperMessagingEngine.send(new GrouperMessageSendParam().assignGrouperMessageSystemName(messagingSystemName)
            .assignAutocreateObjects(autocreateObjects)
            .assignQueueType(grouperMessageQueueType)
            .assignQueueOrTopicName(queueOrTopicName)
            .addMessageBody(json)
            .assignRoutingKey(routingKey)
            .assignExchangeType(exchangeType)
            .assignQueueArguments(queueArguments));
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

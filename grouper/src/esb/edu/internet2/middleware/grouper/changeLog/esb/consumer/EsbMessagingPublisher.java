/**
 * Copyright 2014 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
/*
 * @author Rob Hebron
 */

package edu.internet2.middleware.grouper.changeLog.esb.consumer;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageQueueType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageSendParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;

/**
 *
 * Publishes Grouper events to messaging
 *
 */
public class EsbMessagingPublisher extends EsbListenerBase {

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(EsbMessagingPublisher.class);

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#dispatchEvent(java.lang.String, java.lang.String)
   */
  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    String messagingSystemName = GrouperLoaderConfig.retrieveConfig()
        .propertyValueString("changeLog.consumer."
            + consumerName + ".publisher.messagingSystemName", "grouperBuiltinMessaging");
    String queueOrTopicName = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "changeLog.consumer."
            + consumerName + ".publisher.queueOrTopicName");
    
    String messageQueueType = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("changeLog.consumer." 
        + consumerName + ".publisher.messageQueueType");
    GrouperMessageQueueType grouperMessageQueueType = GrouperMessageQueueType.valueOfIgnoreCase(messageQueueType, true);
    
    String routingKey = GrouperLoaderConfig.retrieveConfig().propertyValueString("changeLog.consumer." 
        + consumerName + ".publisher.routingKey", "");
    
    GrouperMessagingEngine.send(new GrouperMessageSendParam()
        .assignGrouperMessageSystemName(messagingSystemName)
        .assignQueueType(grouperMessageQueueType)
        .addMessageBody(eventJsonString).assignQueueOrTopicName(queueOrTopicName), routingKey);
    return true;
  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#disconnect()
   */
  @Override
  public void disconnect() {
    // Unused, client does not maintain a persistent connection in this version

  }

}

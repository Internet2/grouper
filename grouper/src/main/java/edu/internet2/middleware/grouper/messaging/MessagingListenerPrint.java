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

package edu.internet2.middleware.grouper.messaging;

import java.util.Collection;

import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;


/**
 *
 */
public class MessagingListenerPrint extends MessagingListenerBase {

  /**
   * 
   */
  public MessagingListenerPrint() {
  }

  /**
   * @see edu.internet2.middleware.grouper.messaging.MessagingListenerBase#processMessages(java.lang.String, java.lang.String, java.util.Collection, edu.internet2.middleware.grouper.messaging.MessagingListenerMetadata)
   */
  @Override
  public void processMessages(String messageSystemName, String queue,
      Collection<GrouperMessage> grouperMessageList,
      MessagingListenerMetadata messagingListenerMetadata) {
    
    for (GrouperMessage grouperMessage : grouperMessageList) {
      try {
        
        String json = grouperMessage.getMessageBody();
        
        //try to convert to change log entry
        try {
          Collection<ChangeLogEntry> changeLogEntries = ChangeLogEntry.fromJsonToCollection(json);
          for (ChangeLogEntry changeLogEntry : changeLogEntries) {
            System.out.println("Change log entry: " + changeLogEntry.getChangeLogType().getChangeLogCategory() +
                " -> " + changeLogEntry.getChangeLogType().getActionName() + ", " + changeLogEntry.getId());
          }
          System.out.println("Change log entry: " + json);
        } catch (Exception e) {
          System.out.println("Not change log entry: " + grouperMessage.getId() + ", " + json);
        }
        
        //mark it as processed
        GrouperMessagingEngine.acknowledge(new GrouperMessageAcknowledgeParam()
          .assignAcknowledgeType(GrouperMessageAcknowledgeType.mark_as_processed)
          .assignQueueName(queue).assignGrouperMessageSystemName(messageSystemName)
          .addGrouperMessage(grouperMessage));
        
      } catch (Exception e) {
        messagingListenerMetadata.registerProblem(e, "Problem in message: " + grouperMessage.getId(), grouperMessage.getId());
        break;
      }
    }
  }

}

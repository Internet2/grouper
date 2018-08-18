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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogConsumerBase;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogProcessorMetadata;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;


/**
 * convert a messaging listener to a change log consumer
 */
public class MessagingListenerToChangeLogConsumer extends MessagingListenerBase {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(MessagingListenerToChangeLogConsumer.class);

  /**
   * 
   */
  public MessagingListenerToChangeLogConsumer() {
  }

  /**
   * @see edu.internet2.middleware.grouper.messaging.MessagingListenerBase#processMessages(java.lang.String, java.lang.String, java.util.Collection, edu.internet2.middleware.grouper.messaging.MessagingListenerMetadata)
   */
  @Override
  public void processMessages(String messageSystemName, String queue,
      Collection<GrouperMessage> grouperMessageList,
      MessagingListenerMetadata messagingListenerMetadata) {
    
    String changeLogConsumerClassName = GrouperLoaderConfig.retrieveConfig().propertyValueString("messaging.listener." 
        + messagingListenerMetadata.getConsumerName() + ".changeLogConsumerClass");

    Class<ChangeLogConsumerBase> changeLogConsumerClass = GrouperUtil.forName(changeLogConsumerClassName);
    ChangeLogConsumerBase changeLogConsumerBase = GrouperUtil.newInstance(changeLogConsumerClass);
    
    int size = GrouperUtil.length(grouperMessageList);
    
    List<ChangeLogEntry> changeLogEntries = new ArrayList<ChangeLogEntry>();
    
    Iterator<GrouperMessage> grouperMessageIterator = grouperMessageList.iterator();

    Map<Long, GrouperMessage> changeLogIdToGrouperMessage = new LinkedHashMap<Long, GrouperMessage>();
    
    for (int i=0;i<size;i++) {
      
      GrouperMessage grouperMessage = grouperMessageIterator.next();
      
      try {
        Collection<ChangeLogEntry> changeLogEntriesForMessage = ChangeLogEntry.fromJsonToCollection(grouperMessage.getMessageBody());
        
        for (ChangeLogEntry changeLogEntry : GrouperUtil.nonNull(changeLogEntriesForMessage)) {
          changeLogIdToGrouperMessage.put(changeLogEntry.getSequenceNumber(), grouperMessage);
        }
        
        changeLogEntries.addAll(GrouperUtil.nonNull(changeLogEntriesForMessage));
        
      } catch (Exception e) {
        LOG.error("Cannot convert message body to change log entry: '" 
            + messagingListenerMetadata.getConsumerName() + "''" + grouperMessage.getMessageBody() + "'", e);
        //dont get this message again
        GrouperMessagingEngine.acknowledge(new GrouperMessageAcknowledgeParam().assignGrouperMessageSystemName(messageSystemName)
            .assignQueueName(queue).addGrouperMessage(grouperMessage).assignAcknowledgeType(GrouperMessageAcknowledgeType.mark_as_processed));
      }
    }
    
    if (GrouperUtil.length(changeLogEntries) == 0) {
      return;
    }
    
    ChangeLogProcessorMetadata changeLogProcessorMetadata = new ChangeLogProcessorMetadata();
    changeLogProcessorMetadata.setConsumerName(messagingListenerMetadata.getConsumerName());
    changeLogProcessorMetadata.setHib3GrouperLoaderLog(messagingListenerMetadata.getHib3GrouperLoaderLog());

    long lastProcessed = -1;
    
    try {
      lastProcessed = changeLogConsumerBase.processChangeLogEntries(changeLogEntries, changeLogProcessorMetadata);

      messagingListenerMetadata.setHadProblem(changeLogProcessorMetadata.isHadProblem());
      messagingListenerMetadata.setRecordException(changeLogProcessorMetadata.getRecordException());
      messagingListenerMetadata.setRecordProblemText(changeLogProcessorMetadata.getRecordProblemText());

    } catch (Exception e) {
      messagingListenerMetadata.setHadProblem(true);
      messagingListenerMetadata.setRecordException(e);
      messagingListenerMetadata.setRecordProblemText(e.getLocalizedMessage());
      lastProcessed = changeLogProcessorMetadata.getRecordExceptionSequence() -1;
    }
    
    if (lastProcessed > -1) {
      Set<String> messageIdAcked = new HashSet<String>();
      for (Long changeLogId : changeLogIdToGrouperMessage.keySet()) {
        GrouperMessage grouperMessage = changeLogIdToGrouperMessage.get(changeLogId);

        // multiple change log entries can be in one message
        if (messageIdAcked.contains(grouperMessage.getId())) {
           continue;
        }
        messageIdAcked.add(grouperMessage.getId());
        
        boolean firstError = true;
        
        if (changeLogId <= lastProcessed) {

          //all good, ack
          GrouperMessagingEngine.acknowledge(new GrouperMessageAcknowledgeParam().assignGrouperMessageSystemName(messageSystemName)
              .assignQueueName(queue).addGrouperMessage(grouperMessage).assignAcknowledgeType(GrouperMessageAcknowledgeType.mark_as_processed));
          
        } else {

          if (firstError) {
            messagingListenerMetadata.setRecordExceptionId(grouperMessage.getId());

            firstError = false;
          }
          
          changeLogProcessorMetadata.setHadProblem(true);

          //not processed correctly
          GrouperMessagingEngine.acknowledge(new GrouperMessageAcknowledgeParam().assignGrouperMessageSystemName(messageSystemName)
              .assignQueueName(queue).addGrouperMessage(grouperMessage).assignAcknowledgeType(GrouperMessageAcknowledgeType.return_to_queue));
        }
      }
    }
    
  }

}

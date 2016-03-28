/**
 * Copyright 2014 Internet2
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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.messaging;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageReceiveResult;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;


/**
 * manage messaging listeners
 */
public class MessagingListenerController {

  /**
   * logger 
   */
  private static final Log LOG = GrouperUtil.getLog(MessagingListenerController.class);
  
  /**
   * <pre>
   * call this method to process a batch of 100k (max) records of the change log... 
   * pass in a consumer name (nothing that people would use for a real change log consumer), that is used
   * to keep track of the last processed record, the loader log which will log process in the grouper loader
   * log table, and the processor which is the change log consumer base...
   * 
   * to test this, do your changes, e.g. add a member, delete a member, then call this:
   * 
   * GrouperLoader.runOnceByJobName(grouperSession, GrouperLoaderType.GROUPER_CHANGE_LOG_TEMP_TO_CHANGE_LOG);
   * 
   * then call this method...  e.g. the static example() method in this class
   * 
   * 
   * </pre>
   * @param listenerName name of configured consumer, or another name that is not configured (e.g. ldappcng)
   * @param hib3GrouploaderLog send an instance of this in so it can be logged to the DB...
   * @param messagingListenerBase is the instance that should handle the requests
   */
  public static void processRecords(String listenerName, Hib3GrouperLoaderLog hib3GrouploaderLog, MessagingListenerBase messagingListenerBase) {
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;

    if (LOG.isDebugEnabled()) {
      debugMap.put("consumerName", listenerName);
    }
    
    try {

      String messagingSystemName = GrouperLoaderConfig.retrieveConfig().propertyValueString("messaging.listener." + listenerName + ".messagingSystemName");
      String queueName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("messaging.listener." + listenerName + ".queueName");
      
      boolean autocreateObjects = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.messaging.settings.autocreate.objects", true);

      int numberOfTriesPerIteration = GrouperLoaderConfig.retrieveConfig().propertyValueInt("messaging.listener." + listenerName + ".numberOfTriesPerIteration", 3);
      int pollingTimeoutSeconds = GrouperLoaderConfig.retrieveConfig().propertyValueInt("messaging.listener." + listenerName + ".pollingTimeoutSeconds", 18);
      int sleepSecondsInBetweenIterations = GrouperLoaderConfig.retrieveConfig().propertyValueInt("messaging.listener." + listenerName + ".sleepSecondsInBetweenIterations", 0);
      int maxMessagesToReceiveAtOnce = GrouperLoaderConfig.retrieveConfig().propertyValueInt("messaging.listener." + listenerName + ".maxMessagesToReceiveAtOnce", 20);
      
      if (maxMessagesToReceiveAtOnce < 1) {
        maxMessagesToReceiveAtOnce = 1;
      }
      
      int maxOuterLoops = GrouperLoaderConfig.retrieveConfig().propertyValueInt("messaging.listener." + listenerName + ".maxOuterLoops", 50);
          
      if (maxOuterLoops < 1) {
        maxOuterLoops = 1;
      }
      
      long startTimeMillis = System.currentTimeMillis();
      long endTimeMillis = startTimeMillis + (1000 * (pollingTimeoutSeconds + sleepSecondsInBetweenIterations) * numberOfTriesPerIteration) - 200;
      
      int numberOfTries = 0;
      boolean foundRecords = false;
      boolean error = false;
      
      //lets only do 20000 records at a time (by 10s)
      OUTER: for (int i=0;i<maxOuterLoops;i++) {
        
        MessagingListenerMetadata messagingListenerMetadata = new MessagingListenerMetadata();
        messagingListenerMetadata.setHib3GrouperLoaderLog(hib3GrouploaderLog);
        messagingListenerMetadata.setConsumerName(listenerName);

        Collection<GrouperMessage> grouperMessages = null;
        
        //getSomeRecords
        for (int j=0;j<numberOfTriesPerIteration;j++) {
          GrouperMessageReceiveResult grouperMessageReceiveResult = GrouperMessagingEngine.receive(
              new GrouperMessageReceiveParam().assignLongPollMillis(pollingTimeoutSeconds * 1000)
                .assignAutocreateObjects(autocreateObjects)
                .assignGrouperMessageSystemName(messagingSystemName).assignQueueName(queueName).assignMaxMessagesToReceiveAtOnce(maxMessagesToReceiveAtOnce));
          numberOfTries++;
          grouperMessages = grouperMessageReceiveResult.getGrouperMessages();

          if (LOG.isDebugEnabled()) {
            debugMap.put(i + ": number of records found to process", GrouperUtil.length(grouperMessages));
          }

          if (GrouperUtil.length(grouperMessages) > 0) {
            foundRecords = true;
            break;
          }

          //we have tried a few times, no records
          if (!foundRecords && numberOfTries > numberOfTriesPerIteration) {
            break OUTER;
          }

          //we have processed records, and the time is up
          if (foundRecords && System.currentTimeMillis() > endTimeMillis) {
            break OUTER;
          }

          GrouperUtil.sleep(sleepSecondsInBetweenIterations * 1000);
        }
        
        //pass this to the consumer
        try {
          messagingListenerBase.processMessages(messagingSystemName, queueName, grouperMessages, messagingListenerMetadata);
          
          if (LOG.isDebugEnabled()) {
            debugMap.put(i + ": has error?", messagingListenerMetadata.isHadProblem());
          }

        } catch (Exception e) {
          if (LOG.isDebugEnabled()) {
            debugMap.put(i + ": error processing records", true);
          }
          LOG.error("Error", e);
          hib3GrouploaderLog.appendJobMessage("Error: " 
              + ExceptionUtils.getFullStackTrace(e));
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
          error = true;
        }
                
        if (messagingListenerMetadata.isHadProblem()) {
          if (LOG.isDebugEnabled()) {
            debugMap.put(i + ": hadProblem", true + ", " + messagingListenerMetadata.getRecordProblemText());
          }
          String errorString = "Error: " 
              + messagingListenerMetadata.getRecordProblemText()
              + ", messageId: " + messagingListenerMetadata.getRecordExceptionId()
              + ", " + ExceptionUtils.getFullStackTrace(messagingListenerMetadata.getRecordException());
          LOG.error(errorString);
          hib3GrouploaderLog.appendJobMessage(errorString);
          hib3GrouploaderLog.setStatus(GrouperLoaderStatus.ERROR.name());
          error = true;
        } else {

          hib3GrouploaderLog.addTotalCount(grouperMessages.size());
          
        }
        
        hib3GrouploaderLog.store();
        if (error) {
          break;
        }
      }
      if (LOG.isDebugEnabled()) {
        debugMap.put("totalRecordsProcessed", hib3GrouploaderLog.getTotalCount());
      }
  
      if (!error) {
        hib3GrouploaderLog.setStatus(GrouperLoaderStatus.SUCCESS.name());
      }
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }

    }
  }
  
}

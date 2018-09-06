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

package edu.internet2.middleware.grouper.app.loader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.db.GrouperLoaderDb;
import edu.internet2.middleware.grouper.messaging.MessagingListenerBase;
import edu.internet2.middleware.grouper.messaging.MessagingListenerMetadata;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessage;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeParam;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessageAcknowledgeType;
import edu.internet2.middleware.grouperClient.messaging.GrouperMessagingEngine;

/**
 * @author shilen
 */
public class GrouperLoaderIncrementalMessagingListener extends MessagingListenerBase {
  
  /**
   *
   */
  public GrouperLoaderIncrementalMessagingListener() {
  }
  
  /**
   * @see edu.internet2.middleware.grouper.messaging.MessagingListenerBase#processMessages(java.lang.String, java.lang.String, java.util.Collection, edu.internet2.middleware.grouper.messaging.MessagingListenerMetadata)
   */
  @Override
  public void processMessages(String messageSystemName, String queue,
      Collection<GrouperMessage> grouperMessageList,
      MessagingListenerMetadata messagingListenerMetadata) {
    
    if (grouperMessageList.size() == 0) {
      // nothing to do
      return;
    }

    Connection connection = null;
    PreparedStatement statement = null;

    try {
      // find the config for the incremental job
      String listenerName = messagingListenerMetadata.getConsumerName();
      String incrementalLoaderJobName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("messaging.listener." + listenerName + ".incrementalLoaderJobName");
      
      String databaseName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + incrementalLoaderJobName + ".databaseName");
      String tableName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + incrementalLoaderJobName + ".tableName");
      
      GrouperLoaderDb grouperLoaderDb = GrouperLoaderConfig.retrieveDbProfile(databaseName);
      
      connection = grouperLoaderDb.connection();
      connection.setAutoCommit(false);
      
      String sql = "insert into " + tableName + " (subject_id, subject_identifier, subject_id_or_identifier, subject_source_id, loader_group_name, timestamp) values (?, ?, ?, ?, ?, ?)";
      statement = connection.prepareStatement(sql);
      
      for (GrouperMessage grouperMessage : grouperMessageList) {
        try {
          String json = grouperMessage.getMessageBody();
  
          Map<String, String> data = GrouperUtil.jsonConvertFrom(json, LinkedHashMap.class);
          
          String subjectId = data.get("subjectId");
          String subjectIdentifier = data.get("subjectIdentifier");
          String subjectIdOrIdentifier = data.get("subjectIdOrIdentifier");
          String sourceId = data.get("sourceId");
          String subjectSourceId = data.get("subjectSourceId");
          String loaderGroupName = data.get("loaderGroupName");
          
          int subjectValues = 0;
          if (!StringUtils.isEmpty(subjectId)) {
            subjectValues++; 
          }
          if (!StringUtils.isEmpty(subjectIdentifier)) {
            subjectValues++; 
          }
          if (!StringUtils.isEmpty(subjectIdOrIdentifier)) {
            subjectValues++; 
          }
          
          if (subjectValues != 1) {
            throw new RuntimeException("Must pass in exactly one of subjectId, subjectIdentifier, subjectIdOrIdentifier");
          }
          
          if (StringUtils.isEmpty(loaderGroupName)) {
            throw new RuntimeException("loaderGroupName is required");
          }
          
          if (!StringUtils.isEmpty(subjectSourceId)) {
            sourceId = new String(subjectSourceId);
          }
          
          statement.setString(1, StringUtils.isEmpty(subjectId) ? null : subjectId);
          statement.setString(2, StringUtils.isEmpty(subjectIdentifier) ? null : subjectIdentifier);
          statement.setString(3, StringUtils.isEmpty(subjectIdOrIdentifier) ? null : subjectIdOrIdentifier);
          statement.setString(4, StringUtils.isEmpty(sourceId) ? null : sourceId);
          statement.setString(5, loaderGroupName);
          statement.setLong(6, System.currentTimeMillis());
          
          statement.executeUpdate();
          connection.commit();
  
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
    } catch (Exception e) {
      throw new RuntimeException("Problem initializing listener", e);
    } finally {
      GrouperUtil.closeQuietly(statement);
      GrouperUtil.closeQuietly(connection);
    }
  }
}

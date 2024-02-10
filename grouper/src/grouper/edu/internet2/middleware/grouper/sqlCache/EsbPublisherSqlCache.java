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
 */

package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

/**
 *
 * Publishes Grouper events to messaging
 *
 */
public class EsbPublisherSqlCache extends EsbListenerBase {

  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(EsbPublisherSqlCache.class);

  /**
   * debug map
   */
  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
  
  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(
      List<EsbEventContainer> esbEventContainers) {
 
    debugMap.clear();
    debugMap.put("method", "dispatchEventList");
    
    debugMap.put("eventCount", GrouperUtil.length(esbEventContainers));
    
    long currentTimeMillis = System.currentTimeMillis();

    Long startNanos = System.nanoTime();

    int eventsSkipped = 0;
    
    long minimumEventMicros = -1;

    try {

      ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();

      debugMap.put("lastSequenceAvailable", esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());

      Set<MultiKey> attributeAssignIdsFieldNamesEventMicros = new HashSet<MultiKey>();

      for (int i = esbEventContainers.size()-1; i>=0; i--) {
        EsbEventContainer esbEventContainer = esbEventContainers.get(i);
        
        EsbEvent esbEvent = esbEventContainer.getEsbEvent();
        
        String attributeDefNameName = esbEvent.getAttributeDefNameName();
        if (!StringUtils.equals(SqlCacheGroup.attributeDefNameNameListName(), attributeDefNameName)) {
          GrouperUtil.mapAddValue(debugMap, "wrongAttributeName", 1);
          eventsSkipped++;
          continue;
        }

        String fieldName = esbEvent.getPropertyNewValue();
        Field field = FieldFinder.find(fieldName, false);
        if (field == null || (!field.isGroupAccessField() && !field.isGroupListField())) {
          GrouperUtil.mapAddValue(debugMap, "wrongFieldName", 1);
        }

        boolean isAdd = false;
        EsbEventType esbEventType = esbEventContainer.getEsbEventType();
        
        // we just want the first one
        minimumEventMicros = minimumEventMicros == -1 ? esbEvent.getCreatedOnMicros() : minimumEventMicros;

        long eventMicros = -1;
        switch (esbEventType) {
          
          case ATTRIBUTE_ASSIGN_VALUE_ADD:
            GrouperUtil.mapAddValue(debugMap, "addCacheCount", 1);
            // -1 event micros means its an add
            isAdd = true;
            break;

          case ATTRIBUTE_ASSIGN_VALUE_DELETE:
            GrouperUtil.mapAddValue(debugMap, "removeCacheCount", 1);
            eventMicros = esbEvent.getCreatedOnMicros();
            isAdd = false;
            break;
          default: 
            eventsSkipped++;
            continue;
        }

        String attributeAssignId = esbEvent.getAttributeAssignId();
        
        attributeAssignIdsFieldNamesEventMicros.add(new MultiKey(attributeAssignId, fieldName, eventMicros));
        
      }

      // get the group names and see if still there
      Set<String> attributeAssignIdsToLookForExisting = new HashSet<String>();
      Set<String> attributeAssignIdsToLookForNonExisting = new HashSet<String>();
      
      for (MultiKey attributeAssignIdFieldNameEventMicro : attributeAssignIdsFieldNamesEventMicros) {
        attributeAssignIdsToLookForExisting.add((String)attributeAssignIdFieldNameEventMicro.getKey(0));
        attributeAssignIdsToLookForNonExisting.add((String)attributeAssignIdFieldNameEventMicro.getKey(0));
      }
      
      if (GrouperUtil.length(attributeAssignIdsToLookForExisting) == 0) {
        provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
        return provisioningSyncConsumerResult;
      }

      Map<String, MultiKey> existingAttributeAssignIdToGroupNameFieldName = SqlCacheGroupDao.retrieveExistingAttributeAssignments(attributeAssignIdsToLookForExisting);
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      attributeAssignIdsToLookForNonExisting.removeAll(existingAttributeAssignIdToGroupNameFieldName.keySet());
      
      Map<String, Set<MultiKey>> unassignedAttributeAssignIdToGroupNameFieldName = attributeAssignIdsToLookForNonExisting.size() == 0 ?
         new HashMap<>() : SqlCacheGroupDao.retrieveNonexistingAttributeAssignments(attributeAssignIdsToLookForNonExisting, minimumEventMicros);
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      Collection<MultiKey> existingGroupNamesFieldNames = existingAttributeAssignIdToGroupNameFieldName.values();
      
      //remove existing from non exists
      for (Set<MultiKey> unassignedGroupNamesFieldNames : GrouperUtil.nonNull(unassignedAttributeAssignIdToGroupNameFieldName).values()) {
        unassignedGroupNamesFieldNames.removeAll(existingGroupNamesFieldNames);
      }

      //ok, now we can start to find the group and field internal ids
      Set<String> groupNames = new HashSet<>();
      Set<String> fieldNames = new HashSet<>();

      for (MultiKey groupNamesFieldNames : existingGroupNamesFieldNames) {
        groupNames.add((String)groupNamesFieldNames.getKey(0));
        fieldNames.add((String)groupNamesFieldNames.getKey(1));
      }

      for (Set<MultiKey> unassignedGroupNamesFieldNamesSet : GrouperUtil.nonNull(unassignedAttributeAssignIdToGroupNameFieldName).values()) {
        for (MultiKey groupNamesFieldNames : unassignedGroupNamesFieldNamesSet) {
          groupNames.add((String)groupNamesFieldNames.getKey(0));
          fieldNames.add((String)groupNamesFieldNames.getKey(1));
        }
      }

      Map<String, Long> groupNameToInternalId = GroupFinder.findInternalIdsByNames(groupNames);
      GrouperDaemonUtils.stopProcessingIfJobPaused();

      Map<String, Long> fieldNameToInternalId = FieldFinder.findInternalIdsByNames(fieldNames);
      GrouperDaemonUtils.stopProcessingIfJobPaused();
      
      List<SqlCacheGroup> sqlGroupCachesToCreate = new ArrayList<SqlCacheGroup>();

      if (existingAttributeAssignIdToGroupNameFieldName.size() > 0) {

        Set<MultiKey> groupInternalIdsFieldInternalIds = new HashSet<MultiKey>();

        for (MultiKey groupNameFieldName : existingAttributeAssignIdToGroupNameFieldName.values()) {
          String groupName = (String)groupNameFieldName.getKey(0);
          String fieldName = (String)groupNameFieldName.getKey(1);
          Long groupInternalId = groupNameToInternalId.get(groupName);
          Long fieldInternalId = fieldNameToInternalId.get(fieldName);
          
          groupInternalIdsFieldInternalIds.add(new MultiKey(groupInternalId, fieldInternalId));
          
          SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();

          // add cache with future enabled date (convention for why)
          sqlCacheGroup.setEnabledOn(new Timestamp(currentTimeMillis + (365*24*60*60*1000L)));
          
          //TODO get sizes
          sqlCacheGroup.setGroupInternalId(groupInternalId);
          sqlCacheGroup.setFieldInternalId(fieldInternalId);
          
          sqlGroupCachesToCreate.add(sqlCacheGroup);
        }
        
        int insertedSqlCacheGroupSize = SqlCacheGroupDao.store(sqlGroupCachesToCreate);
        GrouperDaemonUtils.stopProcessingIfJobPaused();

        // add those membership to the cache
        int membershipAdds = SqlCacheMembershipDao.insertSqlCacheMembershipsAsNeededFromSource(existingGroupNamesFieldNames);
        GrouperDaemonUtils.stopProcessingIfJobPaused();

        debugMap.put("insertedSqlCacheGroupSize", insertedSqlCacheGroupSize);

        for (SqlCacheGroup sqlCacheGroup : sqlGroupCachesToCreate) {

          // reset the future enabled date
          sqlCacheGroup.setEnabledOn(new Timestamp(currentTimeMillis));

        }
        SqlCacheGroupDao.store(sqlGroupCachesToCreate);
        GrouperDaemonUtils.stopProcessingIfJobPaused();

        this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog().addInsertCount(membershipAdds + insertedSqlCacheGroupSize);
      }
      
      provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
      return provisioningSyncConsumerResult;

    } finally {
      
      debugMap.put("eventsSkipped", eventsSkipped);
      debugMap.put("tookMillis", ((System.nanoTime() - startNanos)/1000000L));
      this.getEsbConsumer().getChangeLogProcessorMetadata().getHib3GrouperLoaderLog().appendJobMessage(GrouperUtil.mapToString(debugMap));
    }
  }


  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#disconnect()
   */
  @Override
  public void disconnect() {
    // Unused, client does not maintain a persistent connection in this version

  }

  /**
   * @see edu.internet2.middleware.grouper.esb.listener.EsbListenerBase#dispatchEvent(java.lang.String, java.lang.String)
   */
  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new UnsupportedOperationException("Not implemented");
  }

}

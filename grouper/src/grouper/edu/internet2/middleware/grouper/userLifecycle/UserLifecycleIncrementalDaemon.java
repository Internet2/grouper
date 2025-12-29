package edu.internet2.middleware.grouper.userLifecycle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.dataField.GrouperDataField;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignHst;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldAssignHstDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldStructure;
import edu.internet2.middleware.grouper.dataField.GrouperDataFieldType;
import edu.internet2.middleware.grouper.dataField.GrouperDataRow;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignHst;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowAssignHstDao;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssignHst;
import edu.internet2.middleware.grouper.dataField.GrouperDataRowFieldAssignHstDao;
import edu.internet2.middleware.grouper.dictionary.GrouperDictionary;
import edu.internet2.middleware.grouper.dictionary.GrouperDictionaryDao;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class UserLifecycleIncrementalDaemon extends EsbListenerBase {
  
  private static Set<EsbEventType> validEventTypes = new HashSet<EsbEventType>();
  
  static {
    validEventTypes.add(EsbEventType.MEMBERSHIP_ADD);
    validEventTypes.add(EsbEventType.MEMBERSHIP_DELETE);
    validEventTypes.add(EsbEventType.DATA_FIELD_ASSIGN_DELETE);
    validEventTypes.add(EsbEventType.DATA_ROW_ASSIGN_DELETE);
  }

  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void disconnect() {
  }
  
  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(List<EsbEventContainer> esbEventContainers) {
    
    // Step 1 - prepare map of group internal ids, data field internal ids, data row internal ids, and folder id indexes to lifecycle event configs 
    // This step is performed so that we can make one query to fetch a bunch of data points instead of each one at a time
    List<GrouperLifecycleEventConfig> lifecycleEventConfigs = UserLifecycleEventConfigDao.selectAll();
    
    Map<Long, List<GrouperLifecycleEventConfig>> groupIdsAdd = new HashMap<>();
    Map<Long, List<GrouperLifecycleEventConfig>> groupIdsRemove = new HashMap<>();
    Map<Long, List<GrouperLifecycleEventConfig>> dataFieldInternalIds = new HashMap<>();
    Map<Long, List<GrouperLifecycleEventConfig>> dataRowInternalIds = new HashMap<>();
    Map<Long, List<GrouperLifecycleEventConfig>> folderIdIndexes = new HashMap<>();
    
    for (GrouperLifecycleEventConfig lifecycleEventConfig : lifecycleEventConfigs) {
      
      String configId = lifecycleEventConfig.getConfigId();
      
      String trigger = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".trigger");
      
      if (StringUtils.equals(trigger, "groupUserAdd")) {
        Long groupInternalId = lifecycleEventConfig.getGroupInternalId();
        groupIdsAdd.computeIfAbsent(groupInternalId, k -> new ArrayList<>()).add(lifecycleEventConfig);
      } else if (StringUtils.equals(trigger, "groupUserRemove")) {
        Long groupInternalId = lifecycleEventConfig.getGroupInternalId();
        groupIdsRemove.computeIfAbsent(groupInternalId, k -> new ArrayList<>()).add(lifecycleEventConfig);
      } else if (StringUtils.equals(trigger, "groupUserRemoveFromFolder")) {
        Long stemIdIndex = lifecycleEventConfig.getStemIdIndex();
        folderIdIndexes.computeIfAbsent(stemIdIndex, k -> new ArrayList<>()).add(lifecycleEventConfig);
        
      } else if (StringUtils.equals(trigger, "dataFieldRemove")) {
        Long dataFieldInternalId = lifecycleEventConfig.getDataFieldInternalId();
        dataFieldInternalIds.computeIfAbsent(dataFieldInternalId, k -> new ArrayList<>()).add(lifecycleEventConfig);
      } else if (StringUtils.equals(trigger, "dataRowRemove")) {
        Long dataRowInternalId = lifecycleEventConfig.getDataRowInternalId();
        dataRowInternalIds.computeIfAbsent(dataRowInternalId, k -> new ArrayList<>()).add(lifecycleEventConfig);
      }
        
    }
    
    // Step 2 - fetch groups, data fields related info so that we can use it later for jexl evaluation
    
    //User Add related info
    Map<String, Object[]> groupIdToGroupAttributesAdd = new HashMap<String, Object[]>();
    
    if (groupIdsAdd.size() > 0) {  
      Set<Long> groupInternalIdsAdd = groupIdsAdd.keySet();
      
      List<Object[]> groupsAttributes = new GcDbAccess().addBindVars(groupInternalIdsAdd)
      .sql("SELECT id, internal_id, name, display_name, extension, display_extension, description FROM grouper_groups WHERE internal_id in ( "+GrouperClientUtils.appendQuestions(groupInternalIdsAdd.size()) + ")")
      .selectList(Object[].class);
      
      for (Object[] groupAttributes: groupsAttributes) {
        groupIdToGroupAttributesAdd.put(GrouperUtil.stringValue(groupAttributes[0]), groupAttributes);
      }
    }
    
    //User remove related info
    Map<String, Object[]> groupIdToGroupAttributesRemove = new HashMap<String, Object[]>();
    if (groupIdsRemove.size() > 0) {      
      Set<Long> groupInternalIdsRemove = groupIdsRemove.keySet();

      List<Object[]> groupsAttributes = new GcDbAccess().addBindVars(groupInternalIdsRemove)
      .sql("SELECT id, internal_id, name, display_name, extension, display_extension, description FROM grouper_groups WHERE internal_id in ( "+GrouperClientUtils.appendQuestions(groupInternalIdsRemove.size()) + ")")
      .selectList(Object[].class);
      
      for (Object[] groupAttributes: groupsAttributes) {
        groupIdToGroupAttributesRemove.put(GrouperUtil.stringValue(groupAttributes[0]), groupAttributes);
      }
    }
    
    //folder remove related info
    if (folderIdIndexes.size() > 0) {
      Set<Long> folderInternalIdsRemove = folderIdIndexes.keySet();
      
      String sql = """
                select 
          gg.id                as group_id,     
          gg.internal_id       as group_internal_id,
          gg.name              as group_name,
          gg.display_name      as group_display_name,
          gg.extension         as group_extension,
          gg.display_extension as group_display_extension,
          gg.description       as group_description,
          gs.id_index          as group_stem_idIndex
        from grouper_groups gg
        join grouper_stem_set gss
          on gss.if_has_stem_id = gg.parent_stem
        join grouper_stems gs
          on gs.id = gss.then_has_stem_id
        where gs.id_index in (""" + 
        GrouperClientUtils.appendQuestions(folderInternalIdsRemove.size()) + 
            " ) and gg.enabled = 'T'";
      
      List<Object[]> groupsAttributes = new GcDbAccess().addBindVars(folderInternalIdsRemove).sql(sql).selectList(Object[].class);
      
      Map<Long, List<Long>> groupInternalIdToStemIdIndex = new HashMap<Long, List<Long>>(); // one group id can belong to multiple stem indexes
      for (Object[] groupAttributes: groupsAttributes) {
        groupIdToGroupAttributesRemove.put(GrouperUtil.stringValue(groupAttributes[0]), groupAttributes);
        groupInternalIdToStemIdIndex.computeIfAbsent(GrouperUtil.longValue(groupAttributes[1]), k -> new ArrayList<>()).add(GrouperUtil.longValue(groupAttributes[7]));
      }
      
      //associate all the groups under this folder with groupIdsRemove as well for later use
      for (Long groupInternalId: groupInternalIdToStemIdIndex.keySet()) {        
        List<GrouperLifecycleEventConfig> lifecycleEventConfigsForGroup = groupIdsRemove.computeIfAbsent(groupInternalId, k -> new ArrayList<>());
        
        List<Long> stemIdIndexes = groupInternalIdToStemIdIndex.get(groupInternalId);
        for (Long stemIdIndex: stemIdIndexes) {
         List<GrouperLifecycleEventConfig> configsAttachedToOneStem = folderIdIndexes.get(stemIdIndex); 
         lifecycleEventConfigsForGroup.addAll(configsAttachedToOneStem);
        }
      }
      
    }
    
    //data field remove related info
    Map<Long, MultiKey> dataFieldIdToDataFieldAttributes = new HashMap<Long, MultiKey>();
    
    if (dataFieldInternalIds.size() > 0) {
      
      StringBuilder dataFieldFinderSql = new StringBuilder("SELECT * FROM grouper_data_field WHERE internal_id  in ( "+GrouperClientUtils.appendQuestions(dataFieldInternalIds.size()) + " )" );
      
      GcDbAccess gcDbAccess = new GcDbAccess().sql(dataFieldFinderSql.toString()).addBindVars(dataFieldInternalIds.keySet());
      
      List<GrouperDataField> dataFields = gcDbAccess.selectList(GrouperDataField.class);
      
      for (GrouperDataField dataField: dataFields) {
        
        GrouperDataFieldConfig grouperDataFieldConfig = new GrouperDataEngine().getFieldConfigByAlias().get(dataField.getConfigId());
        GrouperDataFieldStructure fieldDataStructure = grouperDataFieldConfig.getFieldDataStructure();
        
        if (fieldDataStructure == GrouperDataFieldStructure.attribute) {
          
          long endTimeBeforeMicros = (System.currentTimeMillis() - 365*24*60*60L*1000) * 1000 ;
          
          List<GrouperDataFieldAssignHst> dataFieldAssingHistories = GrouperDataFieldAssignHstDao.selectByDataFieldInternalIdAndEndTimeBefore(dataField.getInternalId(), endTimeBeforeMicros);
          
          GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();
          if (fieldDataType == GrouperDataFieldType.string) {
            
            Set<Long> dictionaryInternalIds = new HashSet<>();
            
            for (GrouperDataFieldAssignHst assignHst: dataFieldAssingHistories) {
              GrouperDictionary dictionary = GrouperDictionaryDao.selectByInternalId(assignHst.getValueDictionaryInternalId());
              dictionaryInternalIds.add(assignHst.getValueDictionaryInternalId()); // not being used at the moment
              
              String stringValue = dictionary.getTheText();
              
              dataFieldIdToDataFieldAttributes.put(dataField.getInternalId(), new MultiKey(dataField.getConfigId(), stringValue));
              
            }
            
          } else if (fieldDataType == GrouperDataFieldType.integer) {
            // no need for dictionary in this case since the integer value is already in the GrouperDataFieldAssignHst table
            
            for (GrouperDataFieldAssignHst assignHst: dataFieldAssingHistories) {
              
              dataFieldIdToDataFieldAttributes.put(dataField.getInternalId(), new MultiKey(dataField.getConfigId(), assignHst.getValueInteger()));
              
            }
            
          }
          
        } else if (fieldDataStructure == GrouperDataFieldStructure.rowColumn) {
          
          
          long endTimeBeforeMicros = (System.currentTimeMillis() - 365*24*60*60L*1000) * 1000 ;
          
          
          List<GrouperDataRowFieldAssignHst> dataRowFieldAssingHistories = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalIdAndEndTimeBefore(dataField.getInternalId(), endTimeBeforeMicros);
          
          GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();
          if (fieldDataType == GrouperDataFieldType.string) {
            
            Set<Long> dictionaryInternalIds = new HashSet<>();
            
            for (GrouperDataRowFieldAssignHst assignHst: dataRowFieldAssingHistories) {
              GrouperDictionary dictionary = GrouperDictionaryDao.selectByInternalId(assignHst.getValueDictionaryInternalId());
              dictionaryInternalIds.add(assignHst.getValueDictionaryInternalId()); // not being used at the moment
              
              String stringValue = dictionary.getTheText();
              dataFieldIdToDataFieldAttributes.put(dataField.getInternalId(), new MultiKey(dataField.getConfigId(), stringValue));
              
            }
            
          } else if (fieldDataType == GrouperDataFieldType.integer) {
            // no need for dictionary in this case since the integer value is already in the GrouperDataFieldAssignHst table
            for (GrouperDataRowFieldAssignHst assignHst: dataRowFieldAssingHistories) {
              
              dataFieldIdToDataFieldAttributes.put(dataField.getInternalId(), new MultiKey(dataField.getConfigId(), assignHst.getValueInteger()));
              
            }
            
          }
          
        }
        
      }
      
    }
    
    //data row remove related info
    Map<Long, String> dataRowIdToDataRowAttributes = new HashMap<Long, String>();
    
    if (dataRowInternalIds.size() > 0) {
      
      StringBuilder dataRowsFinderSql = new StringBuilder("SELECT * FROM grouper_data_row WHERE internal_id  in ( "+GrouperClientUtils.appendQuestions(dataRowInternalIds.size()) + " )" );
      
      GcDbAccess gcDbAccess = new GcDbAccess().sql(dataRowsFinderSql.toString()).addBindVars(dataRowInternalIds.keySet());
      
      List<GrouperDataRow> dataRows = gcDbAccess.selectList(GrouperDataRow.class);
      
      for (GrouperDataRow dataRow: dataRows) {
        
        long endTimeBeforeMicros = (System.currentTimeMillis() - 365*24*60*60L*1000) * 1000 ;
        
        List<GrouperDataRowAssignHst> dataRowAssignHists = GrouperDataRowAssignHstDao.selectByDataRowInternalIdAndEndTimeBefore(dataRow.getInternalId(), endTimeBeforeMicros);
        
        for (GrouperDataRowAssignHst grouperDataRowAssignHst: dataRowAssignHists) {
          
          dataRowIdToDataRowAttributes.put(dataRow.getInternalId(), dataRow.getConfigId());
          
        }
      }
      
    }
    
    
    // Step 3 - get the last full sync job run and ignore all the events that occurred before that
    // We want to skip events that occurred before the last full sync
    Timestamp lastFullSyncSuccessStartTimestamp = new GcDbAccess().sql("select max(started_time) from grouper_loader_log where job_name = 'OTHER_JOB_userLifecycleFullDaemon' and status = 'SUCCESS' ").select(Timestamp.class);
    
    long lastFullSyncSuccessStartMillis = lastFullSyncSuccessStartTimestamp == null ? -1 : lastFullSyncSuccessStartTimestamp.getTime();
    
    Set<GrouperLifecycleEvent> lifecycleEvents = new HashSet<>(); //events to store in the end
    
    Set<EsbEventContainer> eligibleEventContainers = new HashSet<EsbEventContainer>();
    
    for (EsbEventContainer esbEventContainer : esbEventContainers) {
      if (esbEventContainer.getEsbEvent().getCreatedOnMicros() < lastFullSyncSuccessStartMillis*1000) {
        continue; // event occurred before the full daemon start time. The event was already taken care of within the full sync daemon
      }
      
      if (!validEventTypes.contains(esbEventContainer.getEsbEventType())) {
        continue;
      }
      
      eligibleEventContainers.add(esbEventContainer);
    }
    
    //Step 4 - collect member ids in a set so that we can get member internal ids for them for the next step
    // Again we don't want to make one query per member
    // only for membership add and delete, we need to make a sql query because for data field remove and data row remove
    // we already have the member internal id in the event
    Set<String> memberIds = new HashSet<String>();
    for (EsbEventContainer esbEventContainer : eligibleEventContainers) {
      if (esbEventContainer.getEsbEventType() == EsbEventType.MEMBERSHIP_DELETE) {
        String groupId = esbEventContainer.getEsbEvent().getGroupId();
        String memberId = esbEventContainer.getEsbEvent().getMemberId();
        
        if (!groupIdToGroupAttributesRemove.containsKey(groupId)) {
          continue; // group id is not part of user lifecycle event config
        }
        memberIds.add(memberId);
      } else if (esbEventContainer.getEsbEventType() == EsbEventType.MEMBERSHIP_ADD) {
        String groupId = esbEventContainer.getEsbEvent().getGroupId();
        String memberId = esbEventContainer.getEsbEvent().getMemberId();
        
        if (!groupIdToGroupAttributesAdd.containsKey(groupId)) {
          continue; // group id is not part of user lifecycle event config
        }
        memberIds.add(memberId);
      } else if (esbEventContainer.getEsbEventType() == EsbEventType.DATA_FIELD_ASSIGN_DELETE) {
        
        Long dataFieldInternalId = GrouperUtil.longValue(esbEventContainer.getEsbEvent().getDataFieldInternalId());
        if (!dataFieldInternalIds.containsKey(dataFieldInternalId)) {
          continue;
        }
        
      } else if (esbEventContainer.getEsbEventType() == EsbEventType.DATA_ROW_ASSIGN_DELETE) {
        
        Long dataRowInternalId = GrouperUtil.longValue(esbEventContainer.getEsbEvent().getDataRowInternalId());
        if (!dataRowIdToDataRowAttributes.containsKey(dataRowInternalId)) {
          continue;
        }
        
      } else if (esbEventContainer.getEsbEventType() == EsbEventType.DATA_ROWFIELD_ASSIGN_DELETE) {
        //todo
      }
    }
    
    //Step 5 - retrieve member internal ids from member ids
    Map<String, Long> memberIdToMemberInternalId = new HashMap<String, Long>();
    if (memberIds.size() > 0) {      
      List<Object[]> memberIdAndMemberInternalIds = new GcDbAccess()
          .sql("select id, internal_id from grouper_members where id in ( "+GrouperClientUtils.appendQuestions(memberIds.size()) + ")")
          .addBindVars(memberIds).selectList(Object[].class);
      
      for (Object[] memberIdAndInternalId: memberIdAndMemberInternalIds) {
        memberIdToMemberInternalId.put(GrouperUtil.stringValue(memberIdAndInternalId[0]), GrouperUtil.longValue(memberIdAndInternalId[1]));
      }
    }
    
    //Step 6 - This is the step where we're actually populating the grouper lifecycle event table
    for (EsbEventContainer esbEventContainer : eligibleEventContainers) {
      try {
        
        if (esbEventContainer.getEsbEventType() == EsbEventType.MEMBERSHIP_DELETE) {
          
          String groupId = esbEventContainer.getEsbEvent().getGroupId();
          String memberId = esbEventContainer.getEsbEvent().getMemberId();
          
          if (!groupIdToGroupAttributesRemove.containsKey(groupId)) {
            continue; // group id is not part of user lifecycle event config
          }
          
          if (!memberIdToMemberInternalId.containsKey(memberId)) {
            continue; // this shouldn't really happen  
          }
          
          Object[] groupAttributes = groupIdToGroupAttributesRemove.get(groupId);
          
          Map<String, Object> variableMap = new HashMap<>();
          variableMap.put("groupName", GrouperUtil.stringValue(groupAttributes[2]));
          variableMap.put("groupDisplayName", GrouperUtil.stringValue(groupAttributes[3]));
          variableMap.put("groupExtension", GrouperUtil.stringValue(groupAttributes[4]));
          variableMap.put("groupDisplayExtension", GrouperUtil.stringValue(groupAttributes[5]));
          variableMap.put("groupDescription", GrouperUtil.stringValue(groupAttributes[6]));
          
          List<GrouperLifecycleEventConfig> lifecycleEventConfigsForOneGroup = groupIdsRemove.get(GrouperUtil.longValue(groupAttributes[1]));
          
          for (GrouperLifecycleEventConfig lifecycleEventConfig: lifecycleEventConfigsForOneGroup) {
            
            String naturalLanguageDescriptionJexlPrivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+lifecycleEventConfig.getConfigId()+".naturalLanguageDescriptionJexlPrivileged");
            String naturalLanguageDescriptionJexlUnprivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+lifecycleEventConfig.getConfigId()+".naturalLanguageDescriptionJexlUnprivileged");
            
            Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
            Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
            
            Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
            Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
            
            GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
            grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
            grouperLifecycleEvent.setMemberInternalId(memberIdToMemberInternalId.get(memberId));
            grouperLifecycleEvent.setEventMicros(esbEventContainer.getEsbEvent().getCreatedOnMicros());
            grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
            grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
            lifecycleEvents.add(grouperLifecycleEvent);
            
          }
          
        } else if (esbEventContainer.getEsbEventType() == EsbEventType.MEMBERSHIP_ADD) {
          
          String groupId = esbEventContainer.getEsbEvent().getGroupId();
          String memberId = esbEventContainer.getEsbEvent().getMemberId();
          
          if (!groupIdToGroupAttributesAdd.containsKey(groupId)) {
            continue; // group id is not part of user lifecycle event config
          }
          
          if (!memberIdToMemberInternalId.containsKey(memberId)) {
            continue; // this shouldn't really happen  
          }
          
          Object[] groupAttributes = groupIdToGroupAttributesAdd.get(groupId);
          
          Map<String, Object> variableMap = new HashMap<>();
          variableMap.put("groupName", GrouperUtil.stringValue(groupAttributes[2]));
          variableMap.put("groupDisplayName", GrouperUtil.stringValue(groupAttributes[3]));
          variableMap.put("groupExtension", GrouperUtil.stringValue(groupAttributes[4]));
          variableMap.put("groupDisplayExtension", GrouperUtil.stringValue(groupAttributes[5]));
          variableMap.put("groupDescription", GrouperUtil.stringValue(groupAttributes[6]));
          
          List<GrouperLifecycleEventConfig> lifecycleEventConfigsForOneGroup = groupIdsAdd.get(GrouperUtil.longValue(groupAttributes[1]));
          
          for (GrouperLifecycleEventConfig lifecycleEventConfig: lifecycleEventConfigsForOneGroup) {
            
            String naturalLanguageDescriptionJexlPrivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+lifecycleEventConfig.getConfigId()+".naturalLanguageDescriptionJexlPrivileged");
            String naturalLanguageDescriptionJexlUnprivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+lifecycleEventConfig.getConfigId()+".naturalLanguageDescriptionJexlUnprivileged");
            
            Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
            Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
            
            Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
            Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
            
            GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
            grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
            grouperLifecycleEvent.setMemberInternalId(memberIdToMemberInternalId.get(memberId));
            grouperLifecycleEvent.setEventMicros(esbEventContainer.getEsbEvent().getCreatedOnMicros());
            grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
            grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
            lifecycleEvents.add(grouperLifecycleEvent);
            
          }
          
        } else if (esbEventContainer.getEsbEventType() == EsbEventType.DATA_FIELD_ASSIGN_DELETE) {
          
          Long dataFieldInternalId = GrouperUtil.longValue(esbEventContainer.getEsbEvent().getDataFieldInternalId());
          if (!dataFieldIdToDataFieldAttributes.containsKey(dataFieldInternalId)) {
            continue;
          }
          Long memberInternalId = GrouperUtil.longValue(esbEventContainer.getEsbEvent().getMemberInternalId());
          
          Map<String, Object> variableMap = new HashMap<>();
          variableMap.put("configId", dataFieldIdToDataFieldAttributes.get(dataFieldInternalId).getKey(0));
          variableMap.put("value", dataFieldIdToDataFieldAttributes.get(dataFieldInternalId).getKey(1));
          
          List<GrouperLifecycleEventConfig> lifecycleEventConfigsForOneGroup = dataFieldInternalIds.get(dataFieldInternalId);
          
          for (GrouperLifecycleEventConfig lifecycleEventConfig: lifecycleEventConfigsForOneGroup) {
            
            String naturalLanguageDescriptionJexlPrivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+lifecycleEventConfig.getConfigId()+".naturalLanguageDescriptionJexlPrivileged");
            String naturalLanguageDescriptionJexlUnprivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+lifecycleEventConfig.getConfigId()+".naturalLanguageDescriptionJexlUnprivileged");
            
            Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
            Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
            
            Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
            Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
            
            GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
            grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
            grouperLifecycleEvent.setMemberInternalId(memberInternalId);
            grouperLifecycleEvent.setEventMicros(esbEventContainer.getEsbEvent().getCreatedOnMicros());
            grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
            grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
            lifecycleEvents.add(grouperLifecycleEvent);
          }
          
        } else if (esbEventContainer.getEsbEventType() == EsbEventType.DATA_ROW_ASSIGN_DELETE) {
          
          Long dataRowInternalId = GrouperUtil.longValue(esbEventContainer.getEsbEvent().getDataRowInternalId());
          if (!dataRowIdToDataRowAttributes.containsKey(dataRowInternalId)) {
            continue;
          }
          Long memberInternalId = GrouperUtil.longValue(esbEventContainer.getEsbEvent().getMemberInternalId());
          
          Map<String, Object> variableMap = new HashMap<>();
          variableMap.put("configId", dataRowIdToDataRowAttributes.get(dataRowInternalId));
          
          List<GrouperLifecycleEventConfig> lifecycleEventConfigsForDataRow = dataRowInternalIds.get(dataRowInternalId);
          
          for (GrouperLifecycleEventConfig lifecycleEventConfig: lifecycleEventConfigsForDataRow) {
            
            String naturalLanguageDescriptionJexlPrivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+lifecycleEventConfig.getConfigId()+".naturalLanguageDescriptionJexlPrivileged");
            String naturalLanguageDescriptionJexlUnprivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+lifecycleEventConfig.getConfigId()+".naturalLanguageDescriptionJexlUnprivileged");
            
            Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
            Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
            
            Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
            Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
            
            GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
            grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
            grouperLifecycleEvent.setMemberInternalId(memberInternalId);
            grouperLifecycleEvent.setEventMicros(esbEventContainer.getEsbEvent().getCreatedOnMicros());
            grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
            grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
            lifecycleEvents.add(grouperLifecycleEvent);
          }
          
        }  else if (esbEventContainer.getEsbEventType() == EsbEventType.DATA_ROWFIELD_ASSIGN_DELETE) {
          
          Long dataFieldInternalId = GrouperUtil.longValue(esbEventContainer.getEsbEvent().getDataFieldInternalId());
          if (!dataFieldIdToDataFieldAttributes.containsKey(dataFieldInternalId)) {
            continue;
          }
          Long memberInternalId = GrouperUtil.longValue(esbEventContainer.getEsbEvent().getMemberInternalId());
          
          Map<String, Object> variableMap = new HashMap<>();
          variableMap.put("configId", dataFieldIdToDataFieldAttributes.get(dataFieldInternalId).getKey(0));
          variableMap.put("value", dataFieldIdToDataFieldAttributes.get(dataFieldInternalId).getKey(1));
          
          List<GrouperLifecycleEventConfig> lifecycleEventConfigsForOneGroup = dataFieldInternalIds.get(dataFieldInternalId);
          
          for (GrouperLifecycleEventConfig lifecycleEventConfig: lifecycleEventConfigsForOneGroup) {
            
            String naturalLanguageDescriptionJexlPrivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+lifecycleEventConfig.getConfigId()+".naturalLanguageDescriptionJexlPrivileged");
            String naturalLanguageDescriptionJexlUnprivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+lifecycleEventConfig.getConfigId()+".naturalLanguageDescriptionJexlUnprivileged");
            
            Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
            Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
            
            Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
            Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
            
            GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
            grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
            grouperLifecycleEvent.setMemberInternalId(memberInternalId);
            grouperLifecycleEvent.setEventMicros(esbEventContainer.getEsbEvent().getCreatedOnMicros());
            grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
            grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
            lifecycleEvents.add(grouperLifecycleEvent);
          }
          
        }  
        
      } catch (Exception e) {
        // TODO: handle exception
      }
      
    }
    new GcDbAccess().storeListToDatabase(new ArrayList<>(lifecycleEvents));
    
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog();
    
    hib3GrouperLoaderLog.setInsertCount(lifecycleEvents.size());
    hib3GrouperLoaderLog.store();
    
    
    // get events from the change log
    // keep only membership add, membership remove, data field delete/unassign, 
    // look at the class that has all the events to keep only relevant ones
    
    // look at the last full daemon run and toss all the events that took place before the last full run
    
    /**
     *  Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("groupName", GrouperUtil.stringValue(groupAttributes[0]));
        variableMap.put("groupDisplayName", GrouperUtil.stringValue(groupAttributes[1]));
        variableMap.put("groupExtension", GrouperUtil.stringValue(groupAttributes[2]));
        variableMap.put("groupDisplayExtension", GrouperUtil.stringValue(groupAttributes[3]));
        variableMap.put("groupDescription", GrouperUtil.stringValue(groupAttributes[4]));
        
        Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
        Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
        
        Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
        Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
     */
    
    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
    
    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
    
    return provisioningSyncConsumerResult;
    
  }



}

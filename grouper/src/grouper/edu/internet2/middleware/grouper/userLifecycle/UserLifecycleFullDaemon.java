package edu.internet2.middleware.grouper.userLifecycle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
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
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependency;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyType;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyTypeDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class UserLifecycleFullDaemon extends OtherJobBase {
  
  
  /** logger */
  protected static final Log LOG = edu.internet2.middleware.grouper.util.GrouperUtil.getLog(UserLifecycleFullDaemon.class);
  
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    
    List<GrouperLifecycleEventConfig> lifecycleEventConfigs = UserLifecycleEventConfigDao.selectAll();
    
    Set<GrouperLifecycleEvent> lifecycleEventsToStore = new HashSet<>();
    
    Collection<Long> groupInternalIds = new HashSet<>();
    
    for (GrouperLifecycleEventConfig lifecycleEventConfig: lifecycleEventConfigs) {
      
      String configId = lifecycleEventConfig.getConfigId();
      
      String trigger = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".trigger");
      String naturalLanguageDescriptionJexlPrivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".naturalLanguageDescriptionJexlPrivileged");
      String naturalLanguageDescriptionJexlUnprivileged = GrouperConfig.retrieveConfig().propertyValueString("grouperUserLifecycleEvent."+configId+".naturalLanguageDescriptionJexlUnprivileged");
      
      if (StringUtils.equals(trigger, "groupUserAdd")) {
        Long groupInternalId = lifecycleEventConfig.getGroupInternalId();
        
        groupInternalIds.add(groupInternalId); //this is for later when we need to insert/delete entries from the sql cache dependency table
        
        StringBuilder groupsFinderSql = new StringBuilder("SELECT name, display_name, extension, display_extension, description FROM grouper_groups WHERE internal_id = ? ");
        
        GcDbAccess gcDbAccess = new GcDbAccess().addBindVar(groupInternalId).sql(groupsFinderSql.toString());
        Object[] groupAttributes = gcDbAccess.select(Object[].class);
        if (groupAttributes == null) {
          LOG.warn("grouper lifecycle event config: '"+configId+"' does not have the correct group internal id: '"+groupInternalId+"'");
          continue;
        }
        
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("groupName", GrouperUtil.stringValue(groupAttributes[0]));
        variableMap.put("groupDisplayName", GrouperUtil.stringValue(groupAttributes[1]));
        variableMap.put("groupExtension", GrouperUtil.stringValue(groupAttributes[2]));
        variableMap.put("groupDisplayExtension", GrouperUtil.stringValue(groupAttributes[3]));
        variableMap.put("groupDescription", GrouperUtil.stringValue(groupAttributes[4]));
        
        Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
        Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
        
        Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
        Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
        
        StringBuilder membersFromMShipTable = new StringBuilder("""
            select  gscm.member_internal_id, gscm.flattened_add_timestamp  from grouper_sql_cache_group gscg, grouper_sql_cache_mship gscm, grouper_fields gf where gf.internal_id = gscg.field_internal_id and 
            gscm.sql_cache_group_internal_id = gscg.internal_id and 
            gscg.group_internal_id = ? and gf.name = 'members' and gscm.flattened_add_timestamp > ?
            """);
        
        long microsUntilLastYear = (System.currentTimeMillis() - 365*24*60*60L*1000) * 1000;
        
        List<Object[]> memberInternalIdsAndEventTime = new GcDbAccess().sql(membersFromMShipTable.toString()).addBindVar(groupInternalId).addBindVar(microsUntilLastYear).selectList(Object[].class);
        
        StringBuilder membersFromMShipHistoryTable = new StringBuilder("""
            select gscmh.member_internal_id, gscmh.end_time from grouper_sql_cache_group gscg, grouper_sql_cache_mship_hst gscmh, grouper_fields gf where gf.internal_id = gscg.field_internal_id and 
            gscg.internal_id = gscmh.sql_cache_group_internal_id and gscg.group_internal_id = ? and gf.name = 'members' and gscmh.start_time > ?
            """);
        
        List<Object[]> memberInternalIdsAndEventTimeFromHistoryTable = new GcDbAccess().sql(membersFromMShipHistoryTable.toString()).addBindVar(groupInternalId).addBindVar(microsUntilLastYear).selectList(Object[].class);
        
        Set<MultiKey> memberInternalIdsEventtimes = new HashSet<>();
        
        for (Object[] memberInternalIdAndEventTime: memberInternalIdsAndEventTime) {
          memberInternalIdsEventtimes.add(new MultiKey(memberInternalIdAndEventTime[0], memberInternalIdAndEventTime[1]));
        }
        
        for (Object[] memberInternalIdAndEventTime: memberInternalIdsAndEventTimeFromHistoryTable) {
          memberInternalIdsEventtimes.add(new MultiKey(memberInternalIdAndEventTime[0], memberInternalIdAndEventTime[1]));
        }
        
        for (MultiKey memberInternalIdEventTime: memberInternalIdsEventtimes) {
          GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
          grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
          grouperLifecycleEvent.setMemberInternalId(GrouperUtil.longValue(memberInternalIdEventTime.getKey(0)));
          grouperLifecycleEvent.setEventMicros(GrouperUtil.longValue(memberInternalIdEventTime.getKey(1)));
          grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
          grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
          lifecycleEventsToStore.add(grouperLifecycleEvent);
        }
        
      } else if (StringUtils.equals(trigger, "groupUserRemove")) {
        
        Long groupInternalId = lifecycleEventConfig.getGroupInternalId();
        groupInternalIds.add(groupInternalId); //this is for later when we need to insert/delete entries from the sql cache dependency table
        
        StringBuilder groupsFinderSql = new StringBuilder("SELECT * FROM grouper_groups WHERE internal_id = ? ");
        
        GcDbAccess gcDbAccess = new GcDbAccess().addBindVar(groupInternalId).sql(groupsFinderSql.toString());
        Object[] groupAttributes = gcDbAccess.select(Object[].class);
        if (groupAttributes == null) {
          LOG.warn("grouper lifecycle event config: '"+configId+"' does not have the correct group internal id: '"+groupInternalId+"'");
          continue;
        }
        
        Map<String, Object> variableMap = new HashMap<>();
        variableMap.put("groupName", GrouperUtil.stringValue(groupAttributes[0]));
        variableMap.put("groupDisplayName", GrouperUtil.stringValue(groupAttributes[1]));
        variableMap.put("groupExtension", GrouperUtil.stringValue(groupAttributes[2]));
        variableMap.put("groupDisplayExtension", GrouperUtil.stringValue(groupAttributes[3]));
        variableMap.put("groupDescription", GrouperUtil.stringValue(groupAttributes[4]));
        
        Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
        Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
        
        Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
        Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
        
        long microsUntilLastYear = (System.currentTimeMillis() - 365*24*60*60L*1000) * 1000 ;
        
        StringBuilder membersFromMShipHistoryTable = new StringBuilder("""
            select gscmh.member_internal_id, gscmh.end_time from grouper_sql_cache_group gscg, grouper_sql_cache_mship_hst gscmh, grouper_fields gf where gf.internal_id = gscg.field_internal_id and 
            gscg.internal_id = gscmh.sql_cache_group_internal_id and gscg.group_internal_id = ? and gf.name = 'members' and gscmh.end_time > ? 
            """);
        
        List<Object[]> memberInternalIdsAndEventTimeFromHistoryTable = new GcDbAccess().sql(membersFromMShipHistoryTable.toString()).addBindVar(groupInternalId).addBindVar(microsUntilLastYear).selectList(Object[].class);
        
        Set<MultiKey> memberInternalIdsEventtimes = new HashSet<>();
        
        for (Object[] memberInternalIdAndEventTime: memberInternalIdsAndEventTimeFromHistoryTable) {
          memberInternalIdsEventtimes.add(new MultiKey(memberInternalIdAndEventTime[0], memberInternalIdAndEventTime[1]));
        }
        
        for (MultiKey memberInternalIdEventTime: memberInternalIdsEventtimes) {
          GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
          grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
          grouperLifecycleEvent.setMemberInternalId(GrouperUtil.longValue(memberInternalIdEventTime.getKey(0)));
          grouperLifecycleEvent.setEventMicros(GrouperUtil.longValue(memberInternalIdEventTime.getKey(1)));
          grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
          grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
          lifecycleEventsToStore.add(grouperLifecycleEvent);
        }
        
        
      } else if (StringUtils.equals(trigger, "groupUserRemoveFromFolder")) {
        Long stemIdIndex = lifecycleEventConfig.getStemIdIndex();
        
        Stem stem = StemFinder.findByIdIndex(stemIdIndex, false, null);
        if (stem == null) {
          LOG.warn("grouper lifecycle event config: '"+configId+"' does not have the correct folder idIndex: '"+stemIdIndex+"'");
          continue;
        }
        
        // given stem id index, 
        /**
         * 
        
        select 
          gg.name              as group_name,
          gg.display_name      as group_display_name,
          gg.extension         as group_extension,
          gg.display_extension as group_display_extension,
          gg.description       as group_description
        from grouper_groups gg
        join grouper_stem_set gss
          on gss.if_has_stem_id = gg.parent_stem
        join grouper_stems gs
          on gs.id = gss.then_has_stem_id
        where gs.id_index = '1000047'             -- <- stem id_index
          and gg.enabled = 'T'
        order by gg.display_name;
         *  
         */
        
        for (Group group: stem.getChildGroups(Scope.SUB)) {
          
          Long groupInternalId = group.getInternalId();
          
          groupInternalIds.add(groupInternalId); //this is for later when we need to insert/delete entries from the sql cache dependency table
          
          Map<String, Object> variableMap = new HashMap<>();
          variableMap.put("groupName", group.getName());
          variableMap.put("groupDisplayName", group.getDisplayName());
          variableMap.put("groupExtension", group.getExtension());
          variableMap.put("groupDisplayExtension", group.getDisplayExtension());
          variableMap.put("groupDescription", group.getDescription());
          
          Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
          Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
          
          Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
          Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
          
          long microsUntilLastYear = (System.currentTimeMillis() - 365*24*60*60L*1000) * 1000 ;
          
          /**
           * select
            gscmh.member_internal_id,
            gscmh.end_time
          from grouper_sql_cache_mship_hst gscmh
          join grouper_sql_cache_group gscg
            on gscg.internal_id = gscmh.sql_cache_group_internal_id
          join grouper_fields gf
            on gf.internal_id = gscg.field_internal_id
          join grouper_groups gg
            on gg.internal_id = gscg.group_internal_id
          join grouper_stem_set gss
            on gss.if_has_stem_id = gg.parent_stem
          join grouper_stems gs
            on gs.id = gss.then_has_stem_id
          where gf.name = 'members'
            and gs.id_index = $1        -- <- ancestor stem id_index
            and gscmh.end_time > $2;
           */
          StringBuilder membersFromMShipHistoryTable = new StringBuilder("""
              select gscmh.member_internal_id, gscmh.end_time from grouper_sql_cache_group gscg, grouper_sql_cache_mship_hst gscmh, grouper_fields gf where gf.internal_id = gscg.field_internal_id and 
              gscg.internal_id = gscmh.sql_cache_group_internal_id and gscg.group_internal_id = ? and gf.name = 'members' and gscmh.end_time > ? 
              """);
          
          
          List<Object[]> memberInternalIdsAndEventTimeFromHistoryTable = new GcDbAccess().sql(membersFromMShipHistoryTable.toString()).addBindVar(groupInternalId).addBindVar(microsUntilLastYear).selectList(Object[].class);
          
          Set<MultiKey> memberInternalIdsEventtimes = new HashSet<>();
          
          for (Object[] memberInternalIdAndEventTime: memberInternalIdsAndEventTimeFromHistoryTable) {
            memberInternalIdsEventtimes.add(new MultiKey(memberInternalIdAndEventTime[0], memberInternalIdAndEventTime[1]));
          }
          
          for (MultiKey memberInternalIdEventTime: memberInternalIdsEventtimes) {
            GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
            grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
            grouperLifecycleEvent.setMemberInternalId(GrouperUtil.longValue(memberInternalIdEventTime.getKey(0)));
            grouperLifecycleEvent.setEventMicros(GrouperUtil.longValue(memberInternalIdEventTime.getKey(1)));
            grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
            grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
            lifecycleEventsToStore.add(grouperLifecycleEvent);
          }
          
        }
        
      } else if (StringUtils.equals(trigger, "dataFieldRemove")) {
        
        Long dataFieldInternalId = lifecycleEventConfig.getDataFieldInternalId();
        
        StringBuilder dataFieldFinderSql = new StringBuilder("SELECT * FROM grouper_data_field WHERE internal_id  = ? ");
        
        GcDbAccess gcDbAccess = new GcDbAccess().sql(dataFieldFinderSql.toString()).addBindVar(dataFieldInternalId);
        
        GrouperDataField dataField = gcDbAccess.select(GrouperDataField.class);
        if (dataField == null) {
          LOG.warn("grouper lifecycle event config: '"+configId+"' does not have the correct data field internal id: '"+dataFieldInternalId+"'");
          continue;
        }
        
        GrouperDataFieldConfig grouperDataFieldConfig = new GrouperDataEngine().getFieldConfigByAlias().get(dataField.getConfigId());
        GrouperDataFieldStructure fieldDataStructure = grouperDataFieldConfig.getFieldDataStructure();
        
        
        if (fieldDataStructure == GrouperDataFieldStructure.attribute) {
          
          long endTimeBeforeMicros = (System.currentTimeMillis() - 365*24*60*60L*1000) * 1000 ;
          
          List<GrouperDataFieldAssignHst> dataFieldAssingHistories = GrouperDataFieldAssignHstDao.selectByDataFieldInternalIdAndEndTimeBefore(dataFieldInternalId, endTimeBeforeMicros);
          
          GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();
          if (fieldDataType == GrouperDataFieldType.string) {
            
            Set<Long> dictionaryInternalIds = new HashSet<>();
            
            for (GrouperDataFieldAssignHst assignHst: dataFieldAssingHistories) {
              GrouperDictionary dictionary = GrouperDictionaryDao.selectByInternalId(assignHst.getValueDictionaryInternalId());
              dictionaryInternalIds.add(assignHst.getValueDictionaryInternalId()); // not being used at the moment
              
              String stringValue = dictionary.getTheText();
              Map<String, Object> variableMap = new HashMap<>();
              variableMap.put("configId", dataField.getConfigId());
              variableMap.put("value", stringValue);
              Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
              Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
              
              Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
              Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
              
              GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
              grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
              grouperLifecycleEvent.setMemberInternalId(assignHst.getMemberInternalId());
              grouperLifecycleEvent.setEventMicros(assignHst.getEndTime());
              grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
              grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
              lifecycleEventsToStore.add(grouperLifecycleEvent);
            }
            
          } else if (fieldDataType == GrouperDataFieldType.integer) {
            // no need for dictionary in this case since the integer value is already in the GrouperDataFieldAssignHst table
            
            for (GrouperDataFieldAssignHst assignHst: dataFieldAssingHistories) {
              
              Map<String, Object> variableMap = new HashMap<>();
              variableMap.put("configId", dataField.getConfigId());
              variableMap.put("value", assignHst.getValueInteger());
              Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
              Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
              
              Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
              Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
              
              GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
              grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
              grouperLifecycleEvent.setMemberInternalId(assignHst.getMemberInternalId());
              grouperLifecycleEvent.setEventMicros(assignHst.getEndTime());
              grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
              grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
              lifecycleEventsToStore.add(grouperLifecycleEvent);
              
            }
            
          }
          
        } else if (fieldDataStructure == GrouperDataFieldStructure.rowColumn) {
          
          
          long endTimeBeforeMicros = (System.currentTimeMillis() - 365*24*60*60L*1000) * 1000 ;
          
          
          List<GrouperDataRowFieldAssignHst> dataRowFieldAssingHistories = GrouperDataRowFieldAssignHstDao.selectByDataFieldInternalIdAndEndTimeBefore(dataFieldInternalId, endTimeBeforeMicros);
          
          GrouperDataFieldType fieldDataType = grouperDataFieldConfig.getFieldDataType();
          if (fieldDataType == GrouperDataFieldType.string) {
            
            Set<Long> dictionaryInternalIds = new HashSet<>();
            
            for (GrouperDataRowFieldAssignHst assignHst: dataRowFieldAssingHistories) {
              GrouperDictionary dictionary = GrouperDictionaryDao.selectByInternalId(assignHst.getValueDictionaryInternalId());
              dictionaryInternalIds.add(assignHst.getValueDictionaryInternalId()); // not being used at the moment
              
              String stringValue = dictionary.getTheText();
              Map<String, Object> variableMap = new HashMap<>();
              variableMap.put("configId", dataField.getConfigId());
              variableMap.put("value", stringValue);
              Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
              Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
              
              Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
              Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
              
              GrouperDataRowAssignHst dataRowAssignHst = assignHst.getDataRowAssignHst();
              
              GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
              grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
              grouperLifecycleEvent.setMemberInternalId(dataRowAssignHst.getMemberInternalId());
              grouperLifecycleEvent.setEventMicros(dataRowAssignHst.getEndTime());
              grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
              grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
              lifecycleEventsToStore.add(grouperLifecycleEvent);
            }
            
          } else if (fieldDataType == GrouperDataFieldType.integer) {
            // no need for dictionary in this case since the integer value is already in the GrouperDataFieldAssignHst table
            
            for (GrouperDataRowFieldAssignHst assignHst: dataRowFieldAssingHistories) {
              
              Map<String, Object> variableMap = new HashMap<>();
              variableMap.put("configId", dataField.getConfigId());
              variableMap.put("value", assignHst.getValueInteger());
              Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
              Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
              
              Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
              Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
              
              GrouperDataRowAssignHst dataRowAssignHst = assignHst.getDataRowAssignHst();
              
              GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
              grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
              grouperLifecycleEvent.setMemberInternalId(dataRowAssignHst.getMemberInternalId());
              grouperLifecycleEvent.setEventMicros(dataRowAssignHst.getEndTime());
              grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
              grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
              lifecycleEventsToStore.add(grouperLifecycleEvent);
              
            }
            
          }
          
        }
        
      } else if (StringUtils.equals(trigger, "dataRowRemove")) {
        
        Long dataRowInternalId = lifecycleEventConfig.getDataRowInternalId();
        
        StringBuilder groupsFinderSql = new StringBuilder("SELECT * FROM grouper_data_row WHERE internal_id  = ? ");
        
        GcDbAccess gcDbAccess = new GcDbAccess().sql(groupsFinderSql.toString()).addBindVar(dataRowInternalId);
        
        GrouperDataRow dataRow = gcDbAccess.select(GrouperDataRow.class);
        if (dataRow == null) {
          LOG.warn("grouper lifecycle event config: '"+configId+"' does not have the correct data row internal id: '"+dataRowInternalId+"'");
          continue;
        }
        
        long endTimeBeforeMicros = (System.currentTimeMillis() - 365*24*60*60L*1000) * 1000 ;
        
        List<GrouperDataRowAssignHst> dataRowAssignHists = GrouperDataRowAssignHstDao.selectByDataRowInternalIdAndEndTimeBefore(dataRowInternalId, endTimeBeforeMicros);
        
        
        for (GrouperDataRowAssignHst grouperDataRowAssignHst: dataRowAssignHists) {
          
          Map<String, Object> variableMap = new HashMap<>();
          variableMap.put("configId", dataRow.getConfigId());
          Object naturalLanguageDescriptionJexlPrivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlPrivileged, variableMap, true, false, true);
          Object naturalLanguageDescriptionJexlUnprivilegedResult = GrouperUtil.substituteExpressionLanguageScript(naturalLanguageDescriptionJexlUnprivileged, variableMap, true, false, true);
          
          Long dictionaryInternalIdForJexlPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlPrivilegedResult));
          Long dictionaryInternalIdForJexlUnPrivileged = GrouperDictionaryDao.findOrAdd(GrouperUtil.stringValue(naturalLanguageDescriptionJexlUnprivilegedResult));
          
          GrouperLifecycleEvent grouperLifecycleEvent = new GrouperLifecycleEvent();
          grouperLifecycleEvent.setGroupLifecycleEventConfigInternalId(lifecycleEventConfig.getInternalId());
          grouperLifecycleEvent.setMemberInternalId(grouperDataRowAssignHst.getMemberInternalId());
          grouperLifecycleEvent.setEventMicros(grouperDataRowAssignHst.getEndTime());
          grouperLifecycleEvent.setNaturalLanguagePrivilegeDictionaryInternalId(dictionaryInternalIdForJexlPrivileged);
          grouperLifecycleEvent.setNaturalLanguageUnPrivilegeDictionaryInternalId(dictionaryInternalIdForJexlUnPrivileged);
          lifecycleEventsToStore.add(grouperLifecycleEvent);
          
        }
      }
    }
    
    
    
    //we need to check if lifecycle events are already there
    List<GrouperLifecycleEvent> lifecycleEventsList = new ArrayList<>(lifecycleEventsToStore);
    int batchSize = 50;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(lifecycleEventsToStore, batchSize, false);
    
    //retrieve already existing lifecycle events
    List<Object[]> lifecycleEventAttributes = new ArrayList<Object[]>();
    for (int i=0; i<numberOfBatches; i++) {
      
      List<GrouperLifecycleEvent> oneBatchOfLifecycleEvents = GrouperUtil.batchList(lifecycleEventsList, batchSize, i);
      GcDbAccess gcDbAccess = new GcDbAccess();
      StringBuilder sqlBuilder = new StringBuilder("select gle.grpr_lcycl_evnt_cnfg_intrnl_id, gle.member_internal_id, gle.event_micros from grouper_lifecycle_event gle where ");
      boolean first = true;
      for (GrouperLifecycleEvent groupIdMemberIdLifecycleEventId: oneBatchOfLifecycleEvents) {
        if (!first) {
          sqlBuilder.append(" or ");
        }
        sqlBuilder.append(" (gle.grpr_lcycl_evnt_cnfg_intrnl_id = ? and gle.member_internal_id = ? and gle.event_micros = ?) ");
        first = false;
        
        gcDbAccess.addBindVar(groupIdMemberIdLifecycleEventId.getGroupLifecycleEventConfigInternalId())
        .addBindVar(groupIdMemberIdLifecycleEventId.getMemberInternalId())
        .addBindVar(groupIdMemberIdLifecycleEventId.getEventMicros());
      }
      
      
      lifecycleEventAttributes.addAll(gcDbAccess.sql(sqlBuilder.toString()).selectList(Object[].class));
      
    }
    
    Set<MultiKey> alreadyHavingInLifecycleEvents = new HashSet<>();
    
    for (Object[] lifecycleEventAttribute: lifecycleEventAttributes) {
      alreadyHavingInLifecycleEvents.add(new MultiKey( GrouperUtil.longValue(lifecycleEventAttribute[0]), 
          GrouperUtil.longValue(lifecycleEventAttribute[1]), 
          GrouperUtil.longValue(lifecycleEventAttribute[2])));
    }
    
    //now remove the ones from lifecycleEventsToStore that already exist
    List<GrouperLifecycleEvent> finalListToStore = new ArrayList<>();
    for (GrouperLifecycleEvent lifecycleEvent: lifecycleEventsToStore) {
      
      MultiKey multiKey = new MultiKey(lifecycleEvent.getGroupLifecycleEventConfigInternalId(), lifecycleEvent.getMemberInternalId(), lifecycleEvent.getEventMicros());
      if (alreadyHavingInLifecycleEvents.contains(multiKey)) {
        continue;
      }
      
      finalListToStore.add(lifecycleEvent);
      
    }
    
    new GcDbAccess().storeListToDatabase(finalListToStore);
    otherJobInput.getHib3GrouperLoaderLog().setInsertCount(finalListToStore.size());
    otherJobInput.getHib3GrouperLoaderLog().store();
    
    Set<MultiKey> groupInternalIdsFieldInternalIds = new HashSet<>(); // we need to insert/delete entries from the sql cache dependency table
    for (Long groupInternalId: groupInternalIds) {
      MultiKey groupInternalIdFieldInternalId = new MultiKey(groupInternalId, Group.getDefaultList().getInternalId());
      groupInternalIdsFieldInternalIds.add(groupInternalIdFieldInternalId);
    }

    
    Collection<SqlCacheGroup> sqlCacheGroups = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(groupInternalIdsFieldInternalIds).values();
    
    SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryLifecycle = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", SqlCacheDependencyTypeDao.NAME_MSHIP_HISTORY_LIFECYCLE);
    
    //start with retrieving all existing ones so that we can remove the ones that are no longer associated with any lifecycle event configs later
    Set<SqlCacheDependency> allDependencies = new HashSet<>(SqlCacheDependencyDao.retrieveByDependencyTypeInternalId(sqlCacheDependencyTypeMshipHistoryLifecycle.getInternalId()));
    
    
    Set<MultiKey> ownerInternalIdsDependentInternalIds = new HashSet<>();
    for (SqlCacheGroup sqlCacheGroup : sqlCacheGroups) {
      ownerInternalIdsDependentInternalIds.add(new MultiKey(sqlCacheGroup.getInternalId(), sqlCacheGroup.getInternalId()));
    }
    Map<MultiKey, SqlCacheDependency> sqlCacheDependencies = SqlCacheDependencyDao.retrieveByDepTypeInternalIdAndOwnerInternalIdsDependentInternalIds(sqlCacheDependencyTypeMshipHistoryLifecycle.getInternalId(), ownerInternalIdsDependentInternalIds);
    // go through and see which ones don't have the mshipHistory_lifecycle dependency
    List<SqlCacheDependency> insertedSqlCacheDependencies = UserLifecycleEventConfiguration.addMembershipHistoryLifecycleDependencies(sqlCacheDependencyTypeMshipHistoryLifecycle, sqlCacheGroups, sqlCacheDependencies);
    
    //now we need to delete the ones that are not needed anymore
    Set<SqlCacheDependency> doNotDelete = new HashSet<SqlCacheDependency>();
    doNotDelete.addAll(insertedSqlCacheDependencies);
    doNotDelete.addAll(sqlCacheDependencies.values());
    
    allDependencies.removeAll(doNotDelete); // now the remaining ones we can delete
    new GcDbAccess().deleteFromDatabaseMultiple(allDependencies);
    
    return null;
  }
  
}


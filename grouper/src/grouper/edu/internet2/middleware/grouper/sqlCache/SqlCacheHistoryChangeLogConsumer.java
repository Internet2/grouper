package edu.internet2.middleware.grouper.sqlCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class SqlCacheHistoryChangeLogConsumer extends EsbListenerBase {
  
  private static final Log LOG = GrouperUtil.getLog(SqlCacheHistoryChangeLogConsumer.class);
  
  private SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryViaAttribute = null;;
  
  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public void disconnect() {
    // nothing
  }
    
  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(
      List<EsbEventContainer> esbEventContainers) {
    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
    
    for (EsbEventContainer esbEventContainer : esbEventContainers) {
      EsbEventType esbEventType = esbEventContainer.getEsbEventType();
      EsbEvent esbEvent = esbEventContainer.getEsbEvent();

      if (esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_ADD || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_DELETE) {
        String attributeDefNameName = esbEvent.getAttributeDefNameName();
        if (SqlCacheGroup.getSqlCacheHistoryAttributeNamesToFields().containsKey(attributeDefNameName)) {
          syncSqlCacheDependencyAndHistory(esbEvent.getOwnerId(), attributeDefNameName);
        }
      }
      
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    } 
    
    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
    return provisioningSyncConsumerResult;
  }
  
  private void syncSqlCacheDependencyAndHistory(String ownerId, String attributeDefNameName) {
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = this.getEsbConsumer().getChangeLogProcessorMetadata().getHib3GrouperLoaderLog();
    
    Field field = SqlCacheGroup.getSqlCacheHistoryAttributeNamesToFields().get(attributeDefNameName);
    AttributeDefName attributeDefName = GrouperDAOFactory.getFactory().getAttributeDefName().findByName(attributeDefNameName, true, null);
    Long groupInternalId = null;
    boolean isAssigned = false;
    if (field.isGroupAccessField() || field.getName().equals("members")) {
      Set<PITGroup> pitGroups = GrouperDAOFactory.getFactory().getPITGroup().findBySourceId(ownerId, false);
      if (pitGroups.size() > 0) {
        groupInternalId = pitGroups.iterator().next().getSourceInternalId();
      }
      
      Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(ownerId, false);
      if (group != null && group.isEnabled()) {
        AttributeAssign attributeAssign = group.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, false);
        if (attributeAssign != null && attributeAssign.isEnabled()) {
          isAssigned = true;
        }
      }
    } else if (field.isStemListField()) {
      Set<PITStem> pitStems = GrouperDAOFactory.getFactory().getPITStem().findBySourceId(ownerId, false);
      if (pitStems.size() > 0) {
        groupInternalId = pitStems.iterator().next().getSourceIdIndex();
      }
      
      Stem stem = GrouperDAOFactory.getFactory().getStem().findByUuid(ownerId, false);
      if (stem != null) {
        AttributeAssign attributeAssign = stem.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, false);
        if (attributeAssign != null && attributeAssign.isEnabled()) {
          isAssigned = true;
        }
      }
    } else if (field.isAttributeDefListField()) {
      Set<PITAttributeDef> pitAttributeDefs = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceId(ownerId, false);
      if (pitAttributeDefs.size() > 0) {
        groupInternalId = pitAttributeDefs.iterator().next().getSourceIdIndex();
      }
      
      AttributeDef attributeDef = GrouperDAOFactory.getFactory().getAttributeDef().findById(ownerId, false);
      if (attributeDef != null) {
        AttributeAssign attributeAssign = attributeDef.getAttributeDelegate().retrieveAssignment("assign", attributeDefName, false, false);
        if (attributeAssign != null && attributeAssign.isEnabled()) {
          isAssigned = true;
        }
      }
    }
    
    if (groupInternalId == null) {
      // not expected, just ignore
      return;
    }
    
    SqlCacheGroup sqlCacheGroup = SqlCacheGroupDao.retrieveByGroupInternalIdFieldInternalId(groupInternalId, field.getInternalId(), null);

    if (sqlCacheGroup == null) {
      // not expected, just ignore
      return;
    }
   
    if (sqlCacheDependencyTypeMshipHistoryViaAttribute == null) {
      sqlCacheDependencyTypeMshipHistoryViaAttribute = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", "mshipHistory_viaAttribute");
    }
    
    SqlCacheDependency sqlCacheDependency = SqlCacheDependencyDao.retrieveByDepTypeInternalIdOwnerInternalIdDependentInternalId(sqlCacheDependencyTypeMshipHistoryViaAttribute.getInternalId(), sqlCacheGroup.getInternalId(), sqlCacheGroup.getInternalId());
    
    if (isAssigned && sqlCacheDependency == null) {
      // need to add the dependency
      sqlCacheDependency = new SqlCacheDependency();
      sqlCacheDependency.setDependencyTypeInternalId(sqlCacheDependencyTypeMshipHistoryViaAttribute.getInternalId());
      sqlCacheDependency.setOwnerInternalId(sqlCacheGroup.getInternalId());
      sqlCacheDependency.setDependentInternalId(sqlCacheGroup.getInternalId());
      SqlCacheDependencyDao.store(sqlCacheDependency);
      
      hib3GrouperLoaderLog.addInsertCount(1);
      LOG.info("Added dependency for sqlCacheGroupInternalId=" + sqlCacheGroup.getInternalId());
      
      // sync the history
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(field.getId(), false);
      if (pitField == null) {
        // unexpected, just ignore
        return;
      }
      
      String pitOwnerId = null;
      
      if (field.isGroupAccessField() || field.getName().equals("members")) {
        PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceInternalIdActive(groupInternalId, false);
        if (pitGroup != null) {
          pitOwnerId = pitGroup.getId();
        }
      } else if (field.isStemListField()) {
        PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdIndexActive(groupInternalId, false);
        if (pitStem != null) {
          pitOwnerId = pitStem.getId();
        }
      } else if (field.isAttributeDefListField()) {
        PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdIndexActive(groupInternalId, false);
        if (pitAttributeDef != null) {
          pitOwnerId = pitAttributeDef.getId();
        }
      }
      
      if (pitOwnerId == null) {
        return;
      }
      
      Map<String, Long> pitIdToMemberInternalId = new HashMap<>();
        
      List<Object[]> pitMembersData = new GcDbAccess().sql("select distinct gpm.id, gpm.source_internal_id from grouper_pit_members gpm, grouper_pit_group_set gpgs, grouper_pit_memberships gpms where gpms.owner_id = gpgs.member_id and gpms.field_id = gpgs.member_field_id and gpm.id = gpms.member_id and gpgs.owner_id=? and gpgs.field_id=? and gpm.active='T'")
          .addBindVar(pitOwnerId)
          .addBindVar(pitField.getId())
          .selectList(Object[].class);
      for (Object[] pitMemberData : pitMembersData) {
        String pitId = (String)pitMemberData[0];
        long sourceInternalId = GrouperUtil.longObjectValue(pitMemberData[1], false);
        pitIdToMemberInternalId.put(pitId, sourceInternalId);
      }
      
      SqlCacheHistoryFullSyncDaemon.syncMembershipHistoryIndividual(sqlCacheGroup.getInternalId(), groupInternalId, pitOwnerId, field, pitField.getId(), pitIdToMemberInternalId, hib3GrouperLoaderLog);
    } else if (!isAssigned && sqlCacheDependency != null) {
      // need to delete the dependency
      new GcDbAccess().sql("delete from grouper_sql_cache_dependency where internal_id = ?").addBindVar(sqlCacheDependency.getInternalId()).executeSql();
      hib3GrouperLoaderLog.addDeleteCount(1);
      LOG.info("Deleted dependency for sqlCacheGroupInternalId=" + sqlCacheGroup.getInternalId());
      
      // check if any other reason to keep history
      int dependenciesFound = new GcDbAccess().sql("select count(1) from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscd.dep_type_internal_id = gscdt.internal_id and gscdt.dependency_category='mshipHistory' and owner_internal_id = ?")
        .addBindVar(sqlCacheGroup.getInternalId())
        .select(int.class);

      if (dependenciesFound == 0) {
        int rowsDeleted = new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst gscmh where gscmh.sql_cache_group_internal_id = ?")
            .addBindVar(sqlCacheGroup.getInternalId())
            .executeSql();
        hib3GrouperLoaderLog.addDeleteCount(rowsDeleted);
        LOG.info("Deleted membership history for sqlCacheGroupInternalId=" + sqlCacheGroup.getInternalId());
      }
    }
  }
}

package edu.internet2.middleware.grouper.abac;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationDaemonLogic;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEvent;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventContainer;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.EsbEventType;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
import edu.internet2.middleware.grouper.esb.listener.ProvisioningSyncConsumerResult;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependency;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyType;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyTypeDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
import edu.internet2.middleware.grouper.util.GrouperCallable;
import edu.internet2.middleware.grouper.util.GrouperFuture;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncHeartbeat;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * step 1: see if there are jexl loader script changes, and update the dependency tables
 */
public class GrouperLoaderJexlScriptIncremental extends EsbListenerBase{

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperLoaderJexlScriptIncremental.class);

  public GrouperLoaderJexlScriptIncremental() {
  }

  @Override
  public boolean dispatchEvent(String eventJsonString, String consumerName) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public void disconnect() {
    
  }

  /**
   * events to process
   */
  private List<EsbEventContainer> eventsToProcess;

  @Override
  public ProvisioningSyncConsumerResult dispatchEventList(List<EsbEventContainer> esbEventContainers) {

    eventsToProcess = esbEventContainers;

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = this.getChangeLogProcessorMetadata().getHib3GrouperLoaderLog();
    
    GcGrouperSync gcGrouperSync = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(GcGrouperSync.SCRIPTED_GROUPS);
    
    gcGrouperSync.setSyncEngine(GcGrouperSync.SCRIPTED_GROUPS);
    gcGrouperSync.getGcGrouperSyncDao().store();
    GcGrouperSyncJob gcGrouperSyncJob = gcGrouperSync.getGcGrouperSyncJobDao().jobRetrieveOrCreateBySyncType("incremental");
    gcGrouperSyncJob.waitForRelatedJobsToFinishThenRun(false);
    
    GcGrouperSyncHeartbeat gcGrouperSyncHeartbeat = new GcGrouperSyncHeartbeat();
    gcGrouperSyncHeartbeat.setGcGrouperSyncJob(gcGrouperSyncJob);
    gcGrouperSyncHeartbeat.setFullSync(false);
    gcGrouperSyncHeartbeat.addHeartbeatLogic(new Runnable() {
      @Override
      public void run() {
        
      }
    });
    if (!gcGrouperSyncHeartbeat.isStarted()) {
      gcGrouperSyncHeartbeat.runHeartbeatThread();
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    RuntimeException runtimeException = null;
    
    try {

      long currentTime = System.currentTimeMillis();

      // group ids to do a full group recalc, e.g. if a script changes or too many memberships change
      Set<String> groupIdsToRecalc = new HashSet<String>();

      Set<MultiKey> groupIdFieldIdsChanged = new HashSet<MultiKey>();
      
      Set<Long> dataFieldInternalIdsChanged = new HashSet<Long>();
      Set<Long> dataRowInternalIdsChanged = new HashSet<Long>();
      
      // attribute def name of scripts, to see if they change
      String jexlScriptStemName = GrouperAbac.jexlScriptStemName();
      String jexlScriptNameOfAttributeDefName = jexlScriptStemName + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT;
      String jexlLastGroupSyncNameOfAttributeDefName = jexlScriptStemName + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_LAST_GROUP_SYNC;

      // sourceId and subjectId to look up
      Set<MultiKey> sourceIdSubjectIds = new HashSet<MultiKey>();
      
      // go through each event, see what objects have changed
      for (EsbEventContainer esbEventContainer : esbEventContainers) {

        EsbEvent esbEvent = esbEventContainer.getEsbEvent();
        EsbEventType esbEventType = esbEventContainer.getEsbEventType();
        
        // see if the script was changed so we can recalculate the group
        // etc:attribute:abacJexlScript:grouperJexlScriptJexlScript
        if (esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_ADD || esbEventType == EsbEventType.ATTRIBUTE_ASSIGN_VALUE_DELETE) {
          String attributeDefNameName = esbEvent.getAttributeDefNameName();
          if (!StringUtils.equals(jexlScriptNameOfAttributeDefName, attributeDefNameName)) {
            continue;
          }
          
          String attributeAssignId = esbEvent.getAttributeAssignId();
          
          // batch these
          AttributeAssign attributeAssign = AttributeAssignFinder.findById(attributeAssignId, false);

          attributeAssign = AttributeAssignFinder.findById(attributeAssign.getOwnerAttributeAssignId(), false);

          String groupId = attributeAssign == null ? null : attributeAssign.getOwnerGroupId();
          
          if (!StringUtils.isBlank(groupId)) {
            groupIdsToRecalc.add(groupId);
          }
          
        }
        if (esbEventType == EsbEventType.MEMBERSHIP_ADD || esbEventType == EsbEventType.MEMBERSHIP_DELETE) {
          
          // group and field
          MultiKey groupIdFieldId = new MultiKey(esbEvent.getGroupId(), Group.getDefaultList().getId());
          groupIdFieldIdsChanged.add(groupIdFieldId);
          
          // source and subject
          MultiKey sourceIdSubjectId = new MultiKey(esbEvent.getSourceId(), esbEvent.getSubjectId());

          sourceIdSubjectIds.add(sourceIdSubjectId);
          

          
        }
        
        // if this is an abac attribute change
        if (esbEventType == EsbEventType.DATA_FIELD_ASSIGN_ADD || esbEventType == EsbEventType.DATA_FIELD_ASSIGN_DELETE) {

          Long dataFieldInternalId = GrouperUtil.longValue(esbEvent.getDataFieldInternalId(), -1);
          
          // if either is -1 then continue
          if (dataFieldInternalId == -1) {
            continue;
          }
          
          dataFieldInternalIdsChanged.add(dataFieldInternalId);
          
        }
        if (esbEventType == EsbEventType.DATA_ROW_ASSIGN_ADD || esbEventType == EsbEventType.DATA_ROW_ASSIGN_DELETE) {

          Long dataRowInternalId = GrouperUtil.longValue(esbEvent.getDataRowInternalId(), -1);
          
          if (dataRowInternalId == -1) {
            continue;
          }
          
          dataRowInternalIdsChanged.add(dataRowInternalId);
          
        }
        
        GrouperDaemonUtils.stopProcessingIfJobPaused();

      }
      
      // lookup all the groups to recalc
      Set<Group> groupsToRecalc = GrouperDAOFactory.getFactory().getGroup().findByUuids(groupIdsToRecalc, false);
      
      // get all the internal ids for the groups to recalc
      Set<Long> groupInternalIdsToRecalc = new HashSet<Long>();
      for (Group group : GrouperUtil.nonNull(groupsToRecalc)) {
        groupInternalIdsToRecalc.add(group.getInternalId());
      }

      // lets get the group cache internal ids for those
      Map<MultiKey, Long> groupIdFieldIdToCacheInternalId = GrouperUtil.nonNull(SqlCacheGroupDao.retrieveByGroupIdsFieldIdsToInternalId(groupIdFieldIdsChanged));
      
      Set<Long> cacheGroupInternalIds = new HashSet<Long>(groupIdFieldIdToCacheInternalId.values());
      
      // find the cache dependency type for abac groups
      SqlCacheDependencyType sqlCacheDependencyTypeAbacGroup = SqlCacheDependencyTypeDao.retrieveByName(SqlCacheDependencyTypeDao.NAME_ABAC_GROUP);
      
      // find the cache dependency type for abac rows
      SqlCacheDependencyType sqlCacheDependencyTypeAbacRow = SqlCacheDependencyTypeDao.retrieveByName(SqlCacheDependencyTypeDao.NAME_ABAC_ROW);

      // find the cache dependency type for abac data fields
      SqlCacheDependencyType sqlCacheDependencyTypeAbacAttribute = SqlCacheDependencyTypeDao.retrieveByName(SqlCacheDependencyTypeDao.NAME_ABAC_ATTRIBUTE);

      // find data field abac dependencies
      Map<Long, Set<Long>> ownerDataRowInternalIdsToDependentCacheGroupInternalIds = SqlCacheDependencyDao
          .retrieveByDepTypeInternalIdAndOwnerCacheInternalIds(sqlCacheDependencyTypeAbacRow.getInternalId(), dataRowInternalIdsChanged);
      
      // find data field abac dependencies
      Map<Long, Set<Long>> ownerDataFieldInternalIdsToDependentCacheGroupInternalIds = SqlCacheDependencyDao
          .retrieveByDepTypeInternalIdAndOwnerCacheInternalIds(sqlCacheDependencyTypeAbacAttribute.getInternalId(), dataFieldInternalIdsChanged);

      // find group abac dependencies
      Map<Long, Set<Long>> ownerCacheGroupInternalIdsToDependentCacheGroupInternalIds = SqlCacheDependencyDao
          .retrieveByDepTypeInternalIdAndOwnerCacheInternalIds(sqlCacheDependencyTypeAbacGroup.getInternalId(), cacheGroupInternalIds);

      // all the cache group internal ids
      Set<Long> allCacheGroupInternalIds = new HashSet<Long>();
      for (Set<Long> dependentCacheGroupInternalIds : ownerDataRowInternalIdsToDependentCacheGroupInternalIds.values()) {
        allCacheGroupInternalIds.addAll(dependentCacheGroupInternalIds);
      }
      for (Set<Long> dependentCacheGroupInternalIds : ownerDataFieldInternalIdsToDependentCacheGroupInternalIds.values()) {
        allCacheGroupInternalIds.addAll(dependentCacheGroupInternalIds);
      }
      for (Set<Long> dependentCacheGroupInternalIds : ownerCacheGroupInternalIdsToDependentCacheGroupInternalIds.values()) {
        allCacheGroupInternalIds.addAll(dependentCacheGroupInternalIds);
      }
      
      Map<Long, SqlCacheGroup> allCacheGroupInternalIdsToCacheGroup = SqlCacheGroupDao.retrieveByInternalIds(allCacheGroupInternalIds);
      Set<Long> allGroupInternalIds = new HashSet<Long>();
      for (SqlCacheGroup sqlCacheGroup : GrouperUtil.nonNull(allCacheGroupInternalIdsToCacheGroup.values())) {
        allGroupInternalIds.add(sqlCacheGroup.getGroupInternalId());
      }
      
      // get the groups
      Map<Long, Group> allGroupInternalIdsToGroup = GrouperDAOFactory.getFactory().getGroup().findByInternalIds(allGroupInternalIds, false, null);

      GcDbAccess gcDbAccessGroupSyncTimestamps = new GcDbAccess().sql("""
          select group_name, value_string from grouper_aval_asn_asn_group_v where attribute_def_name_name2 = '%s'
          """.formatted(jexlLastGroupSyncNameOfAttributeDefName)).selectMultipleColumnName("group_name");
      for (Group allGroup : allGroupInternalIdsToGroup.values()) {
        gcDbAccessGroupSyncTimestamps.addBindVar(allGroup.getName());
      }
      List<Object[]> groupNameLastSyncTimestamps = allGroupInternalIdsToGroup.size() == 0 ? new ArrayList<>() : gcDbAccessGroupSyncTimestamps.selectList(Object[].class);
      Map<String, Long> groupNameToLastSyncMillis = new HashMap<>();
      for (Object[] groupNameLastSyncTimestamp : GrouperUtil.nonNull(groupNameLastSyncTimestamps)) {
        String groupName = (String) groupNameLastSyncTimestamp[0];
        String lastSyncUtcMicros = (String) groupNameLastSyncTimestamp[1];
        if (StringUtils.isBlank(lastSyncUtcMicros)) {
          continue;
        }
        // convert to micros
        Long lastSyncUtcMillisLong = GrouperUtil.timestampIsoUtcMicrosConvertFromString(lastSyncUtcMicros).getTime();
        groupNameToLastSyncMillis.put(groupName, lastSyncUtcMillisLong);
      }
      
      Timestamp lastFullSyncSuccessStartTimestamp = new GcDbAccess()
          .sql(
              """
              select max(started_time) from grouper_loader_log gll 
              where job_name = 'OTHER_JOB_grouperLoaderJexlScriptFullSync'
              and status = 'SUCCESS' 
              """).select(Timestamp.class);
      
      long lastFullSyncSuccessStartMillis = lastFullSyncSuccessStartTimestamp == null ? -1 : lastFullSyncSuccessStartTimestamp.getTime();
      
      Map<MultiKey, Long> memberSourceIdSubjectIdToInternalId = MemberFinder.findInternalIdsByNames(sourceIdSubjectIds);

      Map<Long, Set<Long>> cacheGroupInternalIdToMemberInternalIdsToRecalc = new HashMap<>();
      

      // go through each event in pass 2 and see if there are dependencies and if the event is related to the group
      for (EsbEventContainer esbEventContainer : esbEventContainers) {

        EsbEvent esbEvent = esbEventContainer.getEsbEvent();
        EsbEventType esbEventType = esbEventContainer.getEsbEventType();

        // ignore events that changed before the last successful full sync started
        if (lastFullSyncSuccessStartMillis != -1 && esbEvent.getCreatedOnMicros() / 1000 < lastFullSyncSuccessStartMillis) {
          continue;
        }
        
        if (esbEventType == EsbEventType.MEMBERSHIP_ADD || esbEventType == EsbEventType.MEMBERSHIP_DELETE) {
          
          // group and field
          MultiKey groupIdFieldId = new MultiKey(esbEvent.getGroupId(), Group.getDefaultList().getId());
          
          // source and subject
          MultiKey sourceIdSubjectId = new MultiKey(esbEvent.getSourceId(), esbEvent.getSubjectId());

          Long cacheGroupInternalId = groupIdFieldIdToCacheInternalId.get(groupIdFieldId);
          
          if (cacheGroupInternalId == null) {
            continue;
          }
          
          // which scripted groups are dependent on this group
          Set<Long> dependentCacheGroupInternalIds = ownerCacheGroupInternalIdsToDependentCacheGroupInternalIds.get(cacheGroupInternalId);
          
          for (Long dependentCacheGroupInternalId : GrouperUtil.nonNull(dependentCacheGroupInternalIds)) {

            SqlCacheGroup dependentSqlCacheGroup = allCacheGroupInternalIdsToCacheGroup.get(dependentCacheGroupInternalId);
            if (dependentSqlCacheGroup == null) {
              continue;
            }
            // get the group
            Group dependentGroup = allGroupInternalIdsToGroup.get(dependentSqlCacheGroup.getGroupInternalId());
            if (dependentGroup == null) {
              continue;
            }
            
            // if we are recalculating the group, then skip
            if (groupInternalIdsToRecalc.contains(dependentGroup.getInternalId())) {
              continue;
            }
            
            // see if the group was group sync'ed after the change log message
            Long lastSyncMillis = groupNameToLastSyncMillis.get(dependentGroup.getName());
            if (lastSyncMillis != null && lastSyncMillis > esbEvent.getCreatedOnMicros() / 1000) {
              continue;
            }
            
            Set<Long> memberInternalIds = cacheGroupInternalIdToMemberInternalIdsToRecalc
                .get(dependentCacheGroupInternalId);

            if (memberInternalIds == null) {
              memberInternalIds = new HashSet<Long>();
              cacheGroupInternalIdToMemberInternalIdsToRecalc
                  .put(dependentCacheGroupInternalId, memberInternalIds);
            }

            Long memberInternalId = memberSourceIdSubjectIdToInternalId.get(sourceIdSubjectId);
            
            if (memberInternalId != null) {
              memberInternalIds.add(memberInternalId);
            }
            
          }

        }
        
        // if this is an abac attribute change
        if (esbEventType == EsbEventType.DATA_FIELD_ASSIGN_ADD || esbEventType == EsbEventType.DATA_FIELD_ASSIGN_DELETE) {

          Long dataFieldInternalId = GrouperUtil.longValue(esbEvent.getDataFieldInternalId(), -1);
          
          Long memberInternalId = GrouperUtil.longValue(esbEvent.getMemberInternalId(), -1);

          // if either is -1 then continue
          if (dataFieldInternalId == -1 || memberInternalId == -1) {
            continue;
          }
          
          Set<Long> dependentCacheGroupInternalIds = ownerDataFieldInternalIdsToDependentCacheGroupInternalIds.get(dataFieldInternalId);

          for (Long dependentCacheGroupInternalId : GrouperUtil.nonNull(dependentCacheGroupInternalIds)) {

            SqlCacheGroup dependentSqlCacheGroup = allCacheGroupInternalIdsToCacheGroup.get(dependentCacheGroupInternalId);
            if (dependentSqlCacheGroup == null) {
              continue;
            }
            // get the group
            Group dependentGroup = allGroupInternalIdsToGroup.get(dependentSqlCacheGroup.getGroupInternalId());
            if (dependentGroup == null) {
              continue;
            }
            
            // if we are recalculating the group, then skip
            if (groupInternalIdsToRecalc.contains(dependentGroup.getInternalId())) {
              continue;
            }
            
            // see if the group was group sync'ed after the change log message
            Long lastSyncMillis = groupNameToLastSyncMillis.get(dependentGroup.getName());
            if (lastSyncMillis != null && lastSyncMillis > esbEvent.getCreatedOnMicros() / 1000) {
              continue;
            }

            Set<Long> memberInternalIds = cacheGroupInternalIdToMemberInternalIdsToRecalc.get(dependentCacheGroupInternalId);

            if (memberInternalIds == null) {
              memberInternalIds = new HashSet<Long>();
              cacheGroupInternalIdToMemberInternalIdsToRecalc.put(dependentCacheGroupInternalId, memberInternalIds);
            }
            memberInternalIds.add(memberInternalId);

          }


        }
        if (esbEventType == EsbEventType.DATA_ROW_ASSIGN_ADD || esbEventType == EsbEventType.DATA_ROW_ASSIGN_DELETE) {

          Long dataRowInternalId = GrouperUtil.longValue(esbEvent.getDataRowInternalId(), -1);
          
          Long memberInternalId = GrouperUtil.longValue(esbEvent.getMemberInternalId(), -1);
          
          Set<Long> dependentCacheGroupInternalIds = ownerDataRowInternalIdsToDependentCacheGroupInternalIds.get(dataRowInternalId);

          for (Long dependentCacheGroupInternalId : GrouperUtil.nonNull(dependentCacheGroupInternalIds)) {

            SqlCacheGroup dependentSqlCacheGroup = allCacheGroupInternalIdsToCacheGroup.get(dependentCacheGroupInternalId);
            if (dependentSqlCacheGroup == null) {
              continue;
            }
            // get the group
            Group dependentGroup = allGroupInternalIdsToGroup.get(dependentSqlCacheGroup.getGroupInternalId());
            if (dependentGroup == null) {
              continue;
            }
            
            // if we are recalculating the group, then skip
            if (groupInternalIdsToRecalc.contains(dependentGroup.getInternalId())) {
              continue;
            }
            
            // see if the group was group sync'ed after the change log message
            Long lastSyncMillis = groupNameToLastSyncMillis.get(dependentGroup.getName());
            if (lastSyncMillis != null && lastSyncMillis > esbEvent.getCreatedOnMicros() / 1000) {
              continue;
            }

            Set<Long> memberInternalIds = cacheGroupInternalIdToMemberInternalIdsToRecalc.get(dependentCacheGroupInternalId);

            if (memberInternalIds == null) {
              memberInternalIds = new HashSet<Long>();
              cacheGroupInternalIdToMemberInternalIdsToRecalc.put(dependentCacheGroupInternalId, memberInternalIds);
            }

            memberInternalIds.add(memberInternalId);

          }


        }
        
        GrouperDaemonUtils.stopProcessingIfJobPaused();

      }
      
      // loop through cache groups to recalc and see if more than 1500 members, and if so, then recalc the group
      // otherwise, do the incremental sync
      int abacIncrementalMembersBeforeRecalcGroup = GrouperConfig.retrieveConfig().propertyValueInt("abacIncrementalMembersBeforeRecalcGroup", 1500);
      
      Iterator<Long> iterator = cacheGroupInternalIdToMemberInternalIdsToRecalc.keySet().iterator();
      
      while (iterator.hasNext()) {
        long cacheGroupInternalId = iterator.next();
        Set<Long> memberInternalIds = cacheGroupInternalIdToMemberInternalIdsToRecalc
            .get(cacheGroupInternalId);
        if (GrouperUtil.length(memberInternalIds) > abacIncrementalMembersBeforeRecalcGroup) {
          // lookup the cache group
          SqlCacheGroup sqlCacheGroup = allCacheGroupInternalIdsToCacheGroup.get(cacheGroupInternalId);
          
          if (sqlCacheGroup == null) {
            continue;
          }
          // lookup the group
          Group group = allGroupInternalIdsToGroup.get(sqlCacheGroup.getGroupInternalId());
          if (group == null) {
            continue;
          }
          // add this group to groups to recalc
          groupIdsToRecalc.add(group.getId());
          groupInternalIdsToRecalc.add(group.getInternalId());
          groupsToRecalc.add(group);
          // remove this from sync list
          iterator.remove();
        }
      }
      
      
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();

      // only load these if doing something
      if (GrouperUtil.length(cacheGroupInternalIdToMemberInternalIdsToRecalc) > 0 || GrouperUtil.length(groupsToRecalc) > 0) {

        GrouperConfig grouperConfig = GrouperConfig.retrieveConfig();

        grouperDataEngine.loadFieldsAndRows(grouperConfig);


      }

      
      if (GrouperUtil.length(cacheGroupInternalIdToMemberInternalIdsToRecalc) > 0) {
        
        Set<String> groupIds = new HashSet<String>();

        for (long cacheGroupInternalId : cacheGroupInternalIdToMemberInternalIdsToRecalc.keySet()) {

          SqlCacheGroup sqlCacheGroup = allCacheGroupInternalIdsToCacheGroup.get(cacheGroupInternalId);
          
          if (sqlCacheGroup == null) {
            continue;
          }
          Group group = allGroupInternalIdsToGroup.get(sqlCacheGroup.getGroupInternalId());
          if (group == null) {
            continue;
          }
          groupIds.add(group.getId());
          
        }
        
        AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(jexlScriptStemName + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);
        
        AttributeAssignFinder attributeAssignFinder = new AttributeAssignFinder().addAttributeDefNameId(attributeDefName.getId());
        attributeAssignFinder.assignOwnerGroupIds(groupIds);

        Set<AttributeAssign> attributeAssigns = attributeAssignFinder.findAttributeAssigns();
        
        // map of group id to attribute assign
        Map<String, AttributeAssign> groupIdToAttributeAssign = new HashMap<>();
        
        for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns)) {
          groupIdToAttributeAssign.put(attributeAssign.getOwnerGroupId(), attributeAssign);
        }

        debugMap.put("jexlScriptGroups", GrouperUtil.length(groupIdToAttributeAssign));

        int threadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob.consumer.grouperLoaderJexlScriptIncremental.threadPoolSize", 10);
        boolean useThreads = true;
        if (threadPoolSize <= 1) {
          useThreads = false;
        }
        
        List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
        List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();    
        
        for (long cacheGroupInternalId : cacheGroupInternalIdToMemberInternalIdsToRecalc.keySet()) {
          SqlCacheGroup sqlCacheGroup = allCacheGroupInternalIdsToCacheGroup.get(cacheGroupInternalId);
          if (sqlCacheGroup == null) {
            continue;
          }
          Group group = allGroupInternalIdsToGroup.get(sqlCacheGroup.getGroupInternalId());
          if (group == null) {
            continue;
          }
          
          AttributeAssign attributeAssign = groupIdToAttributeAssign.get(group.getId());
          if (attributeAssign == null) {
            continue;
          }
          
          Set<Long> memberInternalIds = cacheGroupInternalIdToMemberInternalIdsToRecalc
              .get(cacheGroupInternalId);

          if (GrouperUtil.length(memberInternalIds) == 0) {
            continue;
          }
          
          GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("grouperLoaderJexlSyncForOneGroup: " + group.getId()) {

            @Override
            public Void callLogic() {

              syncIncrementalGroup(debugMap, hib3GrouperLoaderLog, grouperDataEngine, attributeAssign, group, memberInternalIds);
              
              return null;
            }
          };
          
          if (!useThreads) {
            grouperCallable.callLogic();
          } else {
            GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable, true);
            futures.add(future);
            
            GrouperFuture.waitForJob(futures, threadPoolSize, callablesWithProblems);
          }
          
        }
        
        //wait for the rest
        GrouperFuture.waitForJob(futures, 0, callablesWithProblems);

        GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      }

      // if there are groups to full sync
      if (GrouperUtil.length(groupsToRecalc) > 0) {
        
        AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER, true);

        AttributeAssignFinder attributeAssignFinder = new AttributeAssignFinder()
            .addAttributeDefNameId(attributeDefName.getId());
        
        for (Group group : GrouperUtil.nonNull(groupsToRecalc)) {
          attributeAssignFinder.addOwnerGroupId(group.getId());
        }
        
        Set<AttributeAssign> attributeAssigns = attributeAssignFinder.findAttributeAssigns();
        
        Map<String, AttributeAssign> groupIdToAttributeAssign = new HashMap<>();
        
        for (AttributeAssign attributeAssign : GrouperUtil.nonNull(attributeAssigns)) {
          groupIdToAttributeAssign.put(attributeAssign.getOwnerGroupId(), attributeAssign);
        }

        List<SqlCacheDependency> allMshipHistoryAbacSqlCacheDependencies = null;
        {
          SqlCacheDependencyType sqlCacheDependencyTypeMshipHistoryAbac = SqlCacheDependencyTypeDao.retrieveByName(SqlCacheDependencyTypeDao.NAME_MSHIP_HISTORY_ABAC);
          
          // owner is history group
          // dependent is abac group
          
          allMshipHistoryAbacSqlCacheDependencies = SqlCacheDependencyDao.retrieveByDepTypeInternalIdAndDependentCacheInternalIds(sqlCacheDependencyTypeMshipHistoryAbac.getInternalId(), cacheGroupInternalIds);
        }
        Map<MultiKey, SqlCacheDependency> allMshipHistoryAbacSqlCacheDependenciesMap = Collections.synchronizedMap(new HashMap<>());
        for (SqlCacheDependency sqlCacheDependency : allMshipHistoryAbacSqlCacheDependencies) {
          allMshipHistoryAbacSqlCacheDependenciesMap.put(new MultiKey(sqlCacheDependency.getOwnerInternalId(), sqlCacheDependency.getDependentInternalId()), sqlCacheDependency);
        }

        Set<Long> sqlCacheGroupInternalIdsStillNeedingMshipHistory = Collections.synchronizedSet(new HashSet<Long>());
        
        int threadPoolSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob.consumer.grouperLoaderJexlScriptIncremental.threadPoolSize", 10);
        boolean useThreads = true;
        if (threadPoolSize <= 1) {
          useThreads = false;
        }
        
        List<GrouperFuture> futures = new ArrayList<GrouperFuture>();
        List<GrouperCallable> callablesWithProblems = new ArrayList<GrouperCallable>();    
        
        // do a group sync
        for (Group group : GrouperUtil.nonNull(groupsToRecalc)) {
          
          AttributeAssign attributeAssign = groupIdToAttributeAssign.get(group.getId());

          GrouperCallable<Void> grouperCallable = new GrouperCallable<Void>("grouperLoaderJexlSyncForOneGroup: " + group.getId()) {

            @Override
            public Void callLogic() {

              GrouperLoaderJexlScriptFullSync.syncFullGroup(debugMap, hib3GrouperLoaderLog, grouperDataEngine,
                  attributeAssign, group, allMshipHistoryAbacSqlCacheDependenciesMap,
                  sqlCacheGroupInternalIdsStillNeedingMshipHistory);
              
              // assign the last group sync attribute
              attributeAssign.getAttributeValueDelegate().assignValue(jexlLastGroupSyncNameOfAttributeDefName, 
                  GrouperUtil.timestampIsoUtcMicrosConvertToString(new Timestamp(currentTime)));
              
              return null;
            }
          };
          
          if (!useThreads) {
            grouperCallable.callLogic();
          } else {
            GrouperFuture<Void> future = GrouperUtil.executorServiceSubmit(GrouperUtil.retrieveExecutorService(), grouperCallable, true);
            futures.add(future);
            
            GrouperFuture.waitForJob(futures, threadPoolSize, callablesWithProblems);
          }
          
        }
        
        //wait for the rest
        GrouperFuture.waitForJob(futures, 0, callablesWithProblems);

        GrouperCallable.tryCallablesWithProblems(callablesWithProblems);
      }      
    } catch (RuntimeException re) {
      runtimeException = re;
    } finally {
      GcGrouperSyncHeartbeat.endAndWaitForThread(gcGrouperSyncHeartbeat);
      debugMap.put("finalLog", true);
      synchronized (GrouperAttestationDaemonLogic.class) {
        try {
          if (gcGrouperSyncJob != null) {
            gcGrouperSyncJob.assignHeartbeatAndEndJob();
          }
        } catch (RuntimeException re2) {
          debugMap.put("exception2", GrouperClientUtils.getFullStackTrace(re2));
          if (runtimeException == null) {
            throw re2;
          }
          
        }
      }
      
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
      
    }

    
    ProvisioningSyncConsumerResult provisioningSyncConsumerResult = new ProvisioningSyncConsumerResult();
    
    provisioningSyncConsumerResult.setLastProcessedSequenceNumber(esbEventContainers.get(esbEventContainers.size()-1).getSequenceNumber());
    
    return provisioningSyncConsumerResult;
    
  }
  
  public static void syncIncrementalGroup(Map<String, Object> debugMap,
      Hib3GrouperLoaderLog hib3GrouperLoaderLog, GrouperDataEngine grouperDataEngine,
      AttributeAssign attributeAssign, Group group,Set<Long> memberInternalIds) {
    Group theGroup = group;
    
    GcDbAccess gcDbAccessOrig = new GcDbAccess();
    String script = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT);
    GrouperJexlScriptAnalysis analyzeJexlScript = GrouperLoaderJexlScriptFullSync.analyzeJexlScript(grouperDataEngine, script);

    
    //  String includeInternalSourcesString = attributeAssign.getAttributeValueDelegate().retrieveValueString(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_INCLUDE_INTERNAL_SOURCES);
    //  boolean includeInternalSources = GrouperUtil.booleanValue(includeInternalSourcesString, false);
    
    //System.out.println(script);
    
    
    GrouperJexlScriptSql grouperJexlScriptSql = GrouperLoaderJexlScriptFullSync.generateJexlSql(grouperDataEngine, gcDbAccessOrig, analyzeJexlScript); 
    
    List<Long> memberInternalIdsList = new ArrayList<Long>(memberInternalIds);
    // batch these up in batches of 200
    
    int batchSize = 200;
    int batchCount = GrouperUtil.batchNumberOfBatches(memberInternalIdsList, batchSize, false);
    
    for (int i = 0; i < batchCount; i++) {
      List<Long> memberInternalIdsBatch = GrouperUtil.batchList(memberInternalIdsList, batchSize, i);
      GcDbAccess gcDbAccessBatch = gcDbAccessOrig.cloneDbAccess();
      String sql = "select id from grouper_members gm where gm.subject_source != 'g:gsa' and  ( " + grouperJexlScriptSql.getWhereClause() 
        + " ) and gm.internal_id in (" + GrouperClientUtils.appendQuestions(GrouperUtil.length(memberInternalIdsBatch)) + ")";
      for (Long memberInternalId : memberInternalIdsBatch) {
        gcDbAccessBatch.addBindVar(memberInternalId);
      }
      Set<String> memberIdsInJexl = new HashSet<String>(gcDbAccessBatch.sql(sql).selectList(String.class));
      
      GcDbAccess gcDbAccessPrevious = new GcDbAccess().sql("select distinct gms.member_id from grouper_memberships gms, grouper_members gm "
          + "where owner_group_id = ? and field_id = ? and mship_type = 'immediate' and gms.member_id = gm.id "
          + "and gm.internal_id in (" + GrouperClientUtils.appendQuestions(GrouperUtil.length(memberInternalIdsBatch)) + ")")
          .addBindVar(attributeAssign.getOwnerGroupId())
          .addBindVar(Group.getDefaultList().getId());
      for (Long memberInternalId : memberInternalIdsBatch) {
        gcDbAccessPrevious.addBindVar(memberInternalId);
      }
      
      Set<String> previousMemberIds = new HashSet<String>(gcDbAccessPrevious.selectList(String.class));

      Set<String> insertMemberIds = new HashSet<>(memberIdsInJexl);
      insertMemberIds.removeAll(previousMemberIds);
      
      Set<String> deleteMemberIds = new HashSet<>(previousMemberIds);
      deleteMemberIds.removeAll(memberIdsInJexl);
      
      if (theGroup == null) {
        LOG.error("Error group not found '" + attributeAssign.getOwnerGroupId() + "'");
        GrouperUtil.mapAddValue(debugMap, "errorsGroupNull", 1);
        return;
      }
   
      Set<String> memberIdsToInsertOrDelete = new HashSet<String>(insertMemberIds);
      memberIdsToInsertOrDelete.addAll(deleteMemberIds);
      
      Set<Member> members = GrouperDAOFactory.getFactory().getMember().findByIds(memberIdsToInsertOrDelete, null);
      
      Map<String, Member> memberIdToUser = new HashMap<String, Member>();
      
      for (Member member : GrouperUtil.nonNull(members)) {
        memberIdToUser.put(member.getId(), member);
      }
      
      for (String memberId : insertMemberIds) {
        try {
          Member member = memberIdToUser.get(memberId);
          theGroup.addMember(member.getSubject(), false);
        } catch (RuntimeException re) {
          GrouperUtil.mapAddValue(debugMap, "errorsAddMember", 1);
          debugMap.put("exceptionAddGroupName", theGroup.getName());
          debugMap.put("exceptionAddMemberId", memberId);
          debugMap.put("exceptionAddMember", GrouperUtil.getFullStackTrace(re));
          LOG.error("Error adding memberId '" + memberId + "' to group: '" + theGroup.getName() + "'", re);
        }
      }
   
      for (String memberId : deleteMemberIds) {
        try {
          Member member = memberIdToUser.get(memberId);
          theGroup.deleteMember(member.getSubject(), false);
        } catch (RuntimeException re) {
          GrouperUtil.mapAddValue(debugMap, "errorsDeleteMember", 1);
          debugMap.put("exceptionDeleteGroupName", theGroup.getName());
          debugMap.put("exceptionDeleteMemberId", memberId);
          debugMap.put("exceptionDeleteMember", GrouperUtil.getFullStackTrace(re));
          LOG.error("Error deleting memberId '" + memberId + "' from group: '" + theGroup.getName() + "'", re);
        }
      }
      
      GrouperUtil.mapAddValue(debugMap, "inserts", insertMemberIds.size());
      if (hib3GrouperLoaderLog != null) {
        hib3GrouperLoaderLog.addInsertCount(insertMemberIds.size());
      }
      GrouperUtil.mapAddValue(debugMap, "deletes", deleteMemberIds.size());
      if (hib3GrouperLoaderLog != null) {
        hib3GrouperLoaderLog.addDeleteCount(deleteMemberIds.size());
      }
      
    }
    
  }


}

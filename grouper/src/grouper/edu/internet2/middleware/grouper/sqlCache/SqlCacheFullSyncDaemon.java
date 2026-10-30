package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderJob;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

@DisallowConcurrentExecution
public class SqlCacheFullSyncDaemon extends OtherJobBase {
  private static final Log LOG = GrouperUtil.getLog(SqlCacheFullSyncDaemon.class);
  
  private Map<Long, String> fieldInternalIdToPITId;
  private Map<String, Long> pitIdToFieldInternalId;
  private Map<Long, Field> fieldInternalIdToField;
  
  private Map<Long, String> groupInternalIdToPITId = new HashMap<>();
  private Map<Long, String> stemIdIndexToPITId = new HashMap<>();
  private Map<Long, String> attributeDefIdIndexToPITId = new HashMap<>();
  
  private Map<String, Long> pitIdToMemberInternalId = new HashMap<>();
  private Map<String, Long> pitIdToGroupInternalId = new HashMap<>();
  private Map<String, Long> pitIdToStemIdIndex = new HashMap<>();
  private Map<String, Long> pitIdToAttributeDefIdIndex = new HashMap<>();

  private Set<MultiKey> largeGroupSetPairs = new HashSet<>();

  private OtherJobInput theOtherJobInput = null;
  private Map<String, Object> debugMap = null;
  
  private static boolean minimalMode = false;

  @Override
  public OtherJobOutput run(final OtherJobInput theOtherJobInput) {
    
    this.theOtherJobInput = theOtherJobInput;
    this.debugMap = new LinkedHashMap<String, Object>();

    try {
      // avoid when possible processing changes that are too recent if there's a backlog in change log temp
      long recentTimeMillis = System.currentTimeMillis();
      
      Long changeLogTempMinCreatedOnMicros = new GcDbAccess().sql("select min(created_on) from grouper_change_log_entry_temp").select(Long.class);
      if (changeLogTempMinCreatedOnMicros != null) {
        long changeLogTempMinCreatedOnMillis = changeLogTempMinCreatedOnMicros / 1000L;
        if (recentTimeMillis > changeLogTempMinCreatedOnMillis) {
          recentTimeMillis = changeLogTempMinCreatedOnMillis;
        }
      }
      
      Timestamp lastSuccessRunTimestamp = new GcDbAccess().sql("select max(started_time) from grouper_loader_log where job_name='OTHER_JOB_sqlCacheFullSync' and status='SUCCESS'").select(Timestamp.class);
      Long membershipSyncStartTimeMicros = lastSuccessRunTimestamp == null ? null : (lastSuccessRunTimestamp.getTime() * 1000L - 7200000000L);  // subtract a couple of hours to increase chances that we don't miss anything
      LOG.info("membershipSyncStartTimeMicros=" + membershipSyncStartTimeMicros);
      
      int maxObjectFieldPairMembershipSyncBatchSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob.sqlCacheFullSync.maxObjectFieldPairMembershipSyncBatchSize", 1);
  
      // cache some data
      Set<Field> fields = FieldFinder.findAll();
      Set<PITField> pitFields = GrouperDAOFactory.getFactory().getPITField().findBySourceIdsActive(fields.stream().map(Field::getId).toList());
      fieldInternalIdToPITId = pitFields.stream()
          .collect(Collectors.toMap(
              PITField::getSourceInternalId,
              PITField::getId
          ));
      pitIdToFieldInternalId = pitFields.stream()
          .collect(Collectors.toMap(
              PITField::getId,
              PITField::getSourceInternalId
          ));
      fieldInternalIdToField = fields.stream()
          .collect(Collectors.toMap(
              Field::getInternalId,
              field -> field
          ));
  
      List<Object[]> pitMembersData = new GcDbAccess().sql("select id, source_internal_id from grouper_pit_members where active='T' and source_internal_id is not null").selectList(Object[].class);
      for (Object[] pitMemberData : pitMembersData) {
        String pitId = (String)pitMemberData[0];
        long sourceInternalId = GrouperUtil.longObjectValue(pitMemberData[1], false);
        pitIdToMemberInternalId.put(pitId, sourceInternalId);
      }
      pitMembersData = null;
      LOG.info("Done retrieving data from grouper_pit_members");
      
      // skip PIT rows with a null id, they are reported as problems below (pitOwnersCannotCacheCount)
      List<Object[]> pitGroupsData = new GcDbAccess().sql("select id, source_internal_id from grouper_pit_groups where active='T' and source_internal_id is not null").selectList(Object[].class);
      for (Object[] pitGroupData : pitGroupsData) {
        String pitId = (String)pitGroupData[0];
        long sourceInternalId = GrouperUtil.longObjectValue(pitGroupData[1], false);
        groupInternalIdToPITId.put(sourceInternalId, pitId);
        pitIdToGroupInternalId.put(pitId, sourceInternalId);
      }
      pitGroupsData = null;
      LOG.info("Done retrieving data from grouper_pit_groups");
      
      List<Object[]> pitStemsData = new GcDbAccess().sql("select id, source_id_index from grouper_pit_stems where active='T' and source_id_index is not null").selectList(Object[].class);
      for (Object[] pitStemData : pitStemsData) {
        String pitId = (String)pitStemData[0];
        long sourceIdIndex = GrouperUtil.longObjectValue(pitStemData[1], false);
        stemIdIndexToPITId.put(sourceIdIndex, pitId);
        pitIdToStemIdIndex.put(pitId, sourceIdIndex);
      }
      pitStemsData = null;
      LOG.info("Done retrieving data from grouper_pit_stems");
      
      List<Object[]> pitAttributeDefsData = new GcDbAccess().sql("select id, source_id_index from grouper_pit_attribute_def where active='T' and source_id_index is not null").selectList(Object[].class);
      for (Object[] pitAttributeDefData : pitAttributeDefsData) {
        String pitId = (String)pitAttributeDefData[0];
        long sourceIdIndex = GrouperUtil.longObjectValue(pitAttributeDefData[1], false);
        attributeDefIdIndexToPITId.put(sourceIdIndex, pitId);
        pitIdToAttributeDefIdIndex.put(pitId, sourceIdIndex);
      }
      pitAttributeDefsData = null;
      LOG.info("Done retrieving data from grouper_pit_attribute_def");

      // STEP 0 - find active PIT groups/stems/attribute defs that cannot be cached: the PIT id is null or the
      //        live object no longer exists (PIT is out of sync).  These are skipped in STEP 3 so the rest of
      //        the sync can complete, and the job ends in ERROR at the end so someone fixes the PIT data
      long pitOwnersCannotCacheCount = 0;
      pitOwnersCannotCacheCount += findPitOwnersCannotCache("pitGroups", "grouper_pit_groups", "source_internal_id", "grouper_groups", recentTimeMillis);
      pitOwnersCannotCacheCount += findPitOwnersCannotCache("pitStems", "grouper_pit_stems", "source_id_index", "grouper_stems", recentTimeMillis);
      pitOwnersCannotCacheCount += findPitOwnersCannotCache("pitAttributeDefs", "grouper_pit_attribute_def", "source_id_index", "grouper_attribute_def", recentTimeMillis);
      LOG.info("Done checking for active PIT owners that cannot be cached, count=" + pitOwnersCannotCacheCount);

      // STEP 1 - find rows in grouper_sql_cache_group that should be disabled
      for (Field field : fields) {
        String sql = null;
        if (field.isGroupAccessField() || field.getName().equals("members")) {
          sql = "select * from grouper_sql_cache_group gscg where gscg.field_internal_id = ? and gscg.disabled_on is null and gscg.enabled_on < ? and not exists (select 1 from grouper_pit_groups gpg where gscg.group_internal_id = gpg.source_internal_id and active='T')";
        } else if (field.isStemListField()) {
          sql = "select * from grouper_sql_cache_group gscg where gscg.field_internal_id = ? and gscg.disabled_on is null and gscg.enabled_on < ? and not exists (select 1 from grouper_pit_stems gps where gscg.group_internal_id = gps.source_id_index and active='T')";
        } else if (field.isAttributeDefListField()) {
          sql = "select * from grouper_sql_cache_group gscg where gscg.field_internal_id = ? and gscg.disabled_on is null and gscg.enabled_on < ? and not exists (select 1 from grouper_pit_attribute_def gpad where gscg.group_internal_id = gpad.source_id_index and active='T')";
        }
        
        if (sql != null) {
          List<SqlCacheGroup> sqlCacheGroupsToUpdate = new GcDbAccess().sql(sql)
            .addBindVar(field.getInternalId())
            .addBindVar(new Date(recentTimeMillis))
            .selectList(SqlCacheGroup.class);
          for (SqlCacheGroup sqlCacheGroup : sqlCacheGroupsToUpdate) {
            sqlCacheGroup.setDisabledOn(new Timestamp(System.currentTimeMillis()));
            sqlCacheGroup.setMembershipSize(0);
          }
          
          SqlCacheGroupDao.store(sqlCacheGroupsToUpdate);
          if (theOtherJobInput != null) {
            theOtherJobInput.getHib3GrouperLoaderLog().addDeleteCount(sqlCacheGroupsToUpdate.size());
          }
          incrementCountInDebugMap("sqlCacheGroupsDisabledCount", sqlCacheGroupsToUpdate.size());
        }
        
        LOG.info("Done checking for rows in grouper_sql_cache_group that should be disabled for field=" + field.getName());
        
        GrouperDaemonUtils.stopProcessingIfJobPaused();
      }
      
      // STEP 2 - find rows in grouper_sql_cache_group that should not be disabled
      for (Field field : fields) {
        String sql = null;
        if (field.isGroupAccessField() || field.getName().equals("members")) {
          sql = "select gscg.* from grouper_pit_groups gpg, grouper_sql_cache_group gscg where gscg.group_internal_id = gpg.source_internal_id and gscg.field_internal_id = ? and gscg.disabled_on is not null and gpg.active='T' and gpg.start_time < ? and gscg.enabled_on < ?";
        } else if (field.isStemListField()) {
          sql = "select gscg.* from grouper_pit_stems gps, grouper_sql_cache_group gscg where gscg.group_internal_id = gps.source_id_index and gscg.field_internal_id = ? and gscg.disabled_on is not null and gps.active='T' and gps.start_time < ? and gscg.enabled_on < ?";
        } else if (field.isAttributeDefListField()) {
          sql = "select gscg.* from grouper_pit_attribute_def gpad, grouper_sql_cache_group gscg where gscg.group_internal_id = gpad.source_id_index and gscg.field_internal_id = ? and gscg.disabled_on is not null and gpad.active='T' and gpad.start_time < ? and gscg.enabled_on < ?";
        }
        
        if (sql != null) {
          List<SqlCacheGroup> sqlCacheGroupsToUpdate = new GcDbAccess().sql(sql)
            .addBindVar(field.getInternalId())
            .addBindVar(recentTimeMillis * 1000L)
            .addBindVar(new Date(recentTimeMillis))
            .selectList(SqlCacheGroup.class);
          
          for (SqlCacheGroup sqlCacheGroup : sqlCacheGroupsToUpdate) {
            sqlCacheGroup.setDisabledOn(null);
          }
          
          SqlCacheGroupDao.store(sqlCacheGroupsToUpdate);
          if (theOtherJobInput != null) {
            theOtherJobInput.getHib3GrouperLoaderLog().addInsertCount(sqlCacheGroupsToUpdate.size());
          }
          incrementCountInDebugMap("sqlCacheGroupsEnabledCount", sqlCacheGroupsToUpdate.size());
        }
        
        LOG.info("Done checking for rows in grouper_sql_cache_group that should not be disabled for field=" + field.getName());
        
        GrouperDaemonUtils.stopProcessingIfJobPaused();
      }
      
      // STEP 3 - find rows in grouper_sql_cache_group that need to be added
      //        - need to account for entities having limited access fields
      //        - skip PIT owners with a null id or no live object (reported in STEP 0), else this would fail on a null id
      for (Field field : fields) {
        String sql = null;
        if (field.isEntityListField()) {
          sql = "select gpg.source_internal_id, gpg.start_time from grouper_pit_groups gpg where gpg.active='T' and gpg.start_time < ? and gpg.source_internal_id is not null and exists (select 1 from grouper_groups gg where gg.id = gpg.source_id) and not exists (select 1 from grouper_sql_cache_group gscg where gscg.group_internal_id = gpg.source_internal_id and gscg.field_internal_id = ?)";
        } else if (field.isGroupAccessField() || field.getName().equals("members")) {
          // make sure not an entity (the join to grouper_groups also skips PIT groups with no live group)
          sql = "select gpg.source_internal_id, gpg.start_time from grouper_pit_groups gpg, grouper_groups gg where gpg.source_id=gg.id and gg.type_of_group != 'entity' and gpg.active='T' and gpg.start_time < ? and gpg.source_internal_id is not null and not exists (select 1 from grouper_sql_cache_group gscg where gscg.group_internal_id = gpg.source_internal_id and gscg.field_internal_id = ?)";
        } else if (field.isStemListField()) {
          sql = "select gps.source_id_index, gps.start_time from grouper_pit_stems gps where gps.active='T' and gps.start_time < ? and gps.source_id_index is not null and exists (select 1 from grouper_stems gs where gs.id = gps.source_id) and not exists (select 1 from grouper_sql_cache_group gscg where gscg.group_internal_id = gps.source_id_index and gscg.field_internal_id = ?)";
        } else if (field.isAttributeDefListField()) {
          sql = "select gpad.source_id_index, gpad.start_time from grouper_pit_attribute_def gpad where gpad.active='T' and gpad.start_time < ? and gpad.source_id_index is not null and exists (select 1 from grouper_attribute_def gad where gad.id = gpad.source_id) and not exists (select 1 from grouper_sql_cache_group gscg where gscg.group_internal_id = gpad.source_id_index and gscg.field_internal_id = ?)";
        }
        
        if (sql != null) {
          List<Object[]> ownerInternalIdOrIdIndexAndStartTimeList = new GcDbAccess().sql(sql)
            .addBindVar(recentTimeMillis * 1000L)
            .addBindVar(field.getInternalId())
            .selectList(Object[].class);
          
          List<SqlCacheGroup> sqlCacheGroupsToInsert = new ArrayList<>();
          
          for (Object[] ownerInternalIdOrIdIndexAndStartTime : ownerInternalIdOrIdIndexAndStartTimeList) {
            long ownerInternalIdOrIdIndex = GrouperUtil.longObjectValue(ownerInternalIdOrIdIndexAndStartTime[0], false);
            long startTimeMicros = GrouperUtil.longObjectValue(ownerInternalIdOrIdIndexAndStartTime[1], false);
  
            SqlCacheGroup sqlCacheGroup = new SqlCacheGroup();
            sqlCacheGroup.setGroupInternalId(ownerInternalIdOrIdIndex);
            sqlCacheGroup.setFieldInternalId(field.getInternalId());
            sqlCacheGroup.setEnabledOn(new Timestamp(startTimeMicros / 1000L));
            sqlCacheGroupsToInsert.add(sqlCacheGroup);
          }
          
          if (sqlCacheGroupsToInsert.size() > 0) {
            List<Long> ids = TableIndex.reserveIds(TableIndexType.sqlGroupCache, sqlCacheGroupsToInsert.size());
            for (int i = 0; i < sqlCacheGroupsToInsert.size(); i++) {
              SqlCacheGroup sqlCacheGroup = sqlCacheGroupsToInsert.get(i);
              sqlCacheGroup.setTempInternalIdOnDeck(ids.get(i));
            }
            
            int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
            int numberOfBatches = GrouperUtil.batchNumberOfBatches(sqlCacheGroupsToInsert.size(), batchSize, true);
            for (int i = 0; i < numberOfBatches; i++) {
              List<SqlCacheGroup> sqlCacheGroupsToInsertBatch = GrouperUtil.batchList(sqlCacheGroupsToInsert, batchSize, i);
              SqlCacheGroupDao.store(sqlCacheGroupsToInsertBatch);
              if (theOtherJobInput != null) {
                theOtherJobInput.getHib3GrouperLoaderLog().addInsertCount(sqlCacheGroupsToInsertBatch.size());
              }
              incrementCountInDebugMap("sqlCacheGroupsInsertedCount", sqlCacheGroupsToInsertBatch.size());
            }
          }
        }
        
        LOG.info("Done checking for rows in grouper_sql_cache_group that need to be added for field=" + field.getName());
        
        GrouperDaemonUtils.stopProcessingIfJobPaused();
      }
      
      // STEP 4 - fix membership size for disabled groups
      {
        int count = new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = '0' where membership_size != '0' and disabled_on is not null").executeSql();
        if (theOtherJobInput != null) {
          theOtherJobInput.getHib3GrouperLoaderLog().addUpdateCount(count);
        }
        incrementCountInDebugMap("sqlCacheGroupsUpdatedMembershipSizeCount", count);
        
        LOG.info("Done fixing membership size for disabled groups");
        GrouperDaemonUtils.stopProcessingIfJobPaused();
      }
      
      // STEP 5 - delete from grouper_sql_cache_mship where rows have invalid references
      {
        int count = new GcDbAccess().sql("delete from grouper_sql_cache_mship gscm where not exists (select 1 from grouper_sql_cache_group gscg where gscg.internal_id = gscm.sql_cache_group_internal_id and gscg.disabled_on is null)").executeSql();
        if (theOtherJobInput != null) {
          theOtherJobInput.getHib3GrouperLoaderLog().addDeleteCount(count);
        }
        incrementCountInDebugMap("sqlCacheMshipsDeletedCount", count);
        
        count = new GcDbAccess().sql("delete from grouper_sql_cache_mship gscm where not exists (select 1 from grouper_pit_members gpm where gpm.source_internal_id = gscm.member_internal_id and gpm.active = 'T')").executeSql();
        if (theOtherJobInput != null) {
          theOtherJobInput.getHib3GrouperLoaderLog().addDeleteCount(count);
        }
        incrementCountInDebugMap("sqlCacheMshipsDeletedCount", count);
        
        LOG.info("Done deleting from grouper_sql_cache_mship where rows have invalid references");
        GrouperDaemonUtils.stopProcessingIfJobPaused();
      }
      
      // STEP 6 - fix grouper_sql_cache_mship and counts in grouper_sql_cache_group
      {
        int switchToEstimatedWhenGroupSetsLargerThan = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob.sqlCacheFullSync.switchToEstimatedStartDateWhenGroupSetsLargerThan", -1);
        int maxAllowedEstimatedStartDateGroups = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob.sqlCacheFullSync.maxAllowedEstimatedStartDateGroups", 50);

        largeGroupSetPairs = new HashSet<>();
        if (switchToEstimatedWhenGroupSetsLargerThan > -1) {
          List<Object[]> largeGroupSetData = new GcDbAccess()
              .sql("select owner_id, field_id from grouper_pit_group_set"
                 + " group by owner_id, field_id having count(*) > ?")
              .addBindVar((long) switchToEstimatedWhenGroupSetsLargerThan)
              .selectList(Object[].class);
          for (Object[] row : largeGroupSetData) {
            largeGroupSetPairs.add(new MultiKey((String) row[0], (String) row[1]));
          }

          Long totalGroupSetPairs = new GcDbAccess()
              .sql("select count(*) from (select 1 from grouper_pit_group_set group by owner_id, field_id) t")
              .select(Long.class);

          LOG.info("Groups that will use estimated start date (groupSetsLargerThan="
              + switchToEstimatedWhenGroupSetsLargerThan + "): "
              + largeGroupSetPairs.size() + " of "
              + GrouperUtil.defaultIfNull(totalGroupSetPairs, 0L) + " total");

          if (maxAllowedEstimatedStartDateGroups > -1
              && largeGroupSetPairs.size() > maxAllowedEstimatedStartDateGroups) {
            throw new RuntimeException(
                "Number of PIT membership groups switching to estimated start date (" + largeGroupSetPairs.size()
                + ") exceeds maxAllowedEstimatedStartDateGroups ("
                + maxAllowedEstimatedStartDateGroups + ")");
          }
        }

        Set<MultiKey> pitOwnerFieldRecentMembershipChanges = null;
        if (membershipSyncStartTimeMicros != null) {
          String pitOwnerFieldRecentMembershipChangesBaseSql = "select distinct gpgs1.owner_id, gpgs1.field_id from grouper_pit_group_set gpgs1, grouper_pit_memberships gpm1 where gpm1.owner_id = gpgs1.member_id and gpm1.field_id = gpgs1.member_field_id and ";
          List<Object[]> pitOwnerFieldRecentMembershipChangesTemp = new GcDbAccess().sql(pitOwnerFieldRecentMembershipChangesBaseSql + " gpm1.end_time is not null and gpm1.end_time > ?").addBindVar(membershipSyncStartTimeMicros).selectList(Object[].class);
          pitOwnerFieldRecentMembershipChangesTemp.addAll(new GcDbAccess().sql(pitOwnerFieldRecentMembershipChangesBaseSql + " gpgs1.end_time is not null and gpgs1.end_time > ?").addBindVar(membershipSyncStartTimeMicros).selectList(Object[].class));
          pitOwnerFieldRecentMembershipChangesTemp.addAll(new GcDbAccess().sql(pitOwnerFieldRecentMembershipChangesBaseSql + " gpm1.start_time > ?").addBindVar(membershipSyncStartTimeMicros).selectList(Object[].class));
          pitOwnerFieldRecentMembershipChangesTemp.addAll(new GcDbAccess().sql(pitOwnerFieldRecentMembershipChangesBaseSql + " gpgs1.start_time > ?").addBindVar(membershipSyncStartTimeMicros).selectList(Object[].class));
        
          pitOwnerFieldRecentMembershipChanges = new HashSet<MultiKey>();
          for (Object[] pitOwnerFieldRecentMembershipChange : pitOwnerFieldRecentMembershipChangesTemp) {
            Long fieldInternalId = pitIdToFieldInternalId.get(pitOwnerFieldRecentMembershipChange[1]);
            Long ownerInternalId = null;
            
            Field field = fieldInternalIdToField.get(fieldInternalId);
            if (field.isGroupAccessField() || field.getName().equals("members")) {
              ownerInternalId = pitIdToGroupInternalId.get(pitOwnerFieldRecentMembershipChange[0]);
            } else if (field.isStemListField()) {
              ownerInternalId = pitIdToStemIdIndex.get(pitOwnerFieldRecentMembershipChange[0]);
            } else if (field.isAttributeDefListField()) {
              ownerInternalId = pitIdToAttributeDefIdIndex.get(pitOwnerFieldRecentMembershipChange[0]);
            } else {
              continue;
            }
            
            if (fieldInternalId != null && ownerInternalId != null) {
              pitOwnerFieldRecentMembershipChanges.add(new MultiKey(ownerInternalId, fieldInternalId));
            }
          }
          
          LOG.info("pitOwnerFieldRecentMembershipChanges.size=" + pitOwnerFieldRecentMembershipChanges.size());
        }
        
        List<Object[]> sqlCacheGroupsData = new GcDbAccess().sql("select internal_id, group_internal_id, field_internal_id, membership_size, last_membership_sync from grouper_sql_cache_group where disabled_on is null").selectList(Object[].class);
        LOG.info("sqlCacheGroupsData.size=" + sqlCacheGroupsData.size());
        incrementCountInDebugMap("sqlCacheGroupsTotalCount", sqlCacheGroupsData.size());

        // sort by membership size desc but prioritze if not been sync'ed
        sqlCacheGroupsData.sort((o1, o2) -> {
          long s1 = ((Number) o1[3]).longValue();
          long s2 = ((Number) o2[3]).longValue();
  
          if (s1 == -1 && s2 != -1) {
            return -1;
          } else if (s2 == -1 && s1 != -1) {
            return 1;
          } else {
            return Long.compare(s2, s1);
          }
        });
        
        
        List<Object[]> sqlCacheGroupDataBatch = new ArrayList<>();
        Map<Long, Timestamp> lastMembershipSyncUpdates = new HashMap<>();
        
        long batchMembershipSize = 0;      
        int count = 0;
        int countWithoutSkips = 0;
        
        // now go through with the comparison and updates
        Iterator<Object[]> sqlCacheGroupsDataIterator = sqlCacheGroupsData.iterator();
        while (sqlCacheGroupsDataIterator.hasNext()) {
          count++;
          
          Object[] sqlCacheGroupData = sqlCacheGroupsDataIterator.next();
          long internalId = GrouperUtil.longObjectValue(sqlCacheGroupData[0], false);
          long ownerInternalId = GrouperUtil.longObjectValue(sqlCacheGroupData[1], false);
          long fieldInternalId = GrouperUtil.longObjectValue(sqlCacheGroupData[2], false);
          long membershipSize = GrouperUtil.longObjectValue(sqlCacheGroupData[3], false);
          Timestamp lastMembershipSync = GrouperUtil.timestampObjectValue(sqlCacheGroupData[4], true);
          
          // skip unless the object/field has never been sync'ed or there's not a last success time for this job or there's been changes to the object/field since the last success
          if (lastMembershipSync != null && membershipSyncStartTimeMicros != null) {
            if (!pitOwnerFieldRecentMembershipChanges.contains(new MultiKey(ownerInternalId, fieldInternalId))) {
              continue;
            }
          }
          
          Field field = fieldInternalIdToField.get(fieldInternalId);
          if (field.isGroupAccessField() || field.getName().equals("members")) {
            if (!groupInternalIdToPITId.containsKey(ownerInternalId)) {
              continue;
            }
          } else if (field.isStemListField()) {
            if (!stemIdIndexToPITId.containsKey(ownerInternalId)) {
              continue;
            }
          } else if (field.isAttributeDefListField()) {
            if (!attributeDefIdIndexToPITId.containsKey(ownerInternalId)) {
              continue;
            }
          } else {
            continue;
          }
          
          countWithoutSkips++;
          long logBatchProgressInterval = GrouperLoaderConfig.retrieveConfig().propertyValueInt("otherJob.sqlCacheFullSync.logBatchProgressInterval", 100000);
          
          if (countWithoutSkips % logBatchProgressInterval == 0) {
            if (theOtherJobInput != null) {
              theOtherJobInput.getHib3GrouperLoaderLog().setJobMessage("Working on membership sync " + count + " of " + sqlCacheGroupsData.size());
              LOG.info("processBatch querying cacheMemberships: Working on membership sync " + count + " of " + sqlCacheGroupsData.size());
              theOtherJobInput.getHib3GrouperLoaderLog().store();
            }
          }
  
          Object[] sqlCacheGroupDataModified = new Object[] { internalId, ownerInternalId, fieldInternalId, membershipSize };
          sqlCacheGroupDataBatch.add(sqlCacheGroupDataModified);
          
          if (membershipSize > 0) {
            batchMembershipSize += membershipSize;
          }
          
          if (membershipSize == -1 || batchMembershipSize >= 10000 || sqlCacheGroupDataBatch.size() >= maxObjectFieldPairMembershipSyncBatchSize) {
            Timestamp syncTimestamp = new Timestamp(System.currentTimeMillis());
            processBatch(sqlCacheGroupDataBatch);
            
            for (Object[] sqlCacheGroupDataProcessed : sqlCacheGroupDataBatch) {
              lastMembershipSyncUpdates.put((long)sqlCacheGroupDataProcessed[0], syncTimestamp);
            }
            
            sqlCacheGroupDataBatch.clear();
            batchMembershipSize = 0;
            if (LOG.isDebugEnabled()) {
              LOG.debug("Processed " + count);
            }
          }
          
          if (lastMembershipSyncUpdates.size() >= 500) {
            List<List<Object>> batchBindVars = new ArrayList<>();
            for (Long internalIdToUpdate : lastMembershipSyncUpdates.keySet()) {
              Timestamp syncTimestamp = lastMembershipSyncUpdates.get(internalIdToUpdate);
              batchBindVars.add(GrouperUtil.toListObject(syncTimestamp, internalIdToUpdate));
            }
  
            new GcDbAccess().sql("update grouper_sql_cache_group set last_membership_sync = ? where internal_id = ?").batchBindVars(batchBindVars).executeBatchSql();
            
            lastMembershipSyncUpdates.clear();
          }
        }
        
        if (sqlCacheGroupDataBatch.size() > 0) {
          Timestamp syncTimestamp = new Timestamp(System.currentTimeMillis());
          processBatch(sqlCacheGroupDataBatch);
          
          for (Object[] sqlCacheGroupDataProcessed : sqlCacheGroupDataBatch) {
            lastMembershipSyncUpdates.put((long)sqlCacheGroupDataProcessed[0], syncTimestamp);
          }
        }
        
        if (lastMembershipSyncUpdates.size() > 0) {
          List<List<Object>> batchBindVars = new ArrayList<>();
          for (Long internalIdToUpdate : lastMembershipSyncUpdates.keySet()) {
            Timestamp syncTimestamp = lastMembershipSyncUpdates.get(internalIdToUpdate);
            batchBindVars.add(GrouperUtil.toListObject(syncTimestamp, internalIdToUpdate));
          }
  
          new GcDbAccess().sql("update grouper_sql_cache_group set last_membership_sync = ? where internal_id = ?").batchBindVars(batchBindVars).executeBatchSql();        
        }
        
        // process more object/field pairs based on when it was last sync'ed for about an hour max
        if (membershipSyncStartTimeMicros != null && !minimalMode) {
          // stop after one hour
          long timeToStopMillis = System.currentTimeMillis() + 60*60*1000L;
          
          if (theOtherJobInput != null) {
            theOtherJobInput.getHib3GrouperLoaderLog().setJobMessage("Sync'ing other object/field pairs until: " + timeToStopMillis);
            theOtherJobInput.getHib3GrouperLoaderLog().store();
          }
          
          sqlCacheGroupsData.sort((o1, o2) -> {
            Timestamp s1 = GrouperUtil.timestampObjectValue(o1[4], true);
            Timestamp s2 = GrouperUtil.timestampObjectValue(o2[4], true);
  
            if (s1 == null && s2 != null) {
              return -1;
            } else if (s2 == null && s1 != null) {
              return 1;
            } else if (s1 == null && s2 == null) {
              return 0;
            } else {
              return Long.compare(s1.getTime(), s2.getTime());
            }
          });
          
          sqlCacheGroupsDataIterator = sqlCacheGroupsData.iterator();
          while (sqlCacheGroupsDataIterator.hasNext()) {          
            Object[] sqlCacheGroupData = sqlCacheGroupsDataIterator.next();
            
            if (System.currentTimeMillis() > timeToStopMillis) {
              break;
            }
            
            long internalId = GrouperUtil.longObjectValue(sqlCacheGroupData[0], false);
            long ownerInternalId = GrouperUtil.longObjectValue(sqlCacheGroupData[1], false);
            long fieldInternalId = GrouperUtil.longObjectValue(sqlCacheGroupData[2], false);
            long membershipSize = GrouperUtil.longObjectValue(sqlCacheGroupData[3], false);
            Timestamp lastMembershipSync = GrouperUtil.timestampObjectValue(sqlCacheGroupData[4], true);
            
            Field field = fieldInternalIdToField.get(fieldInternalId);
            if (field.isGroupAccessField() || field.getName().equals("members")) {
              if (!groupInternalIdToPITId.containsKey(ownerInternalId)) {
                continue;
              }
            } else if (field.isStemListField()) {
              if (!stemIdIndexToPITId.containsKey(ownerInternalId)) {
                continue;
              }
            } else if (field.isAttributeDefListField()) {
              if (!attributeDefIdIndexToPITId.containsKey(ownerInternalId)) {
                continue;
              }
            } else {
              continue;
            }
            
            if (lastMembershipSync == null || pitOwnerFieldRecentMembershipChanges.contains(new MultiKey(ownerInternalId, fieldInternalId))) {
              // this would have been done above
              continue;
            }
            
            Timestamp syncTimestamp = new Timestamp(System.currentTimeMillis());
            processBatch(Collections.singletonList(new Object[] { internalId, ownerInternalId, fieldInternalId, membershipSize }));
            new GcDbAccess().sql("update grouper_sql_cache_group set last_membership_sync = ? where internal_id = ?").addBindVar(syncTimestamp).addBindVar(internalId).executeSql();
          }
        }
      }

      // STEP 7 - the rest of the sync is done, now end in ERROR if STEP 0 found PIT owners that could not be cached
      if (pitOwnersCannotCacheCount > 0) {
        String errorMessage = pitOwnersCannotCacheCount + " active PIT groups/stems/attributeDefs were skipped since the PIT id is null "
            + "or the live object does not exist (PIT is out of sync, see pit*CannotCache in this message).  To fix: run "
            + "loaderRunOneJob(\"CHANGE_LOG_changeLogTempToChangeLog\") until grouper_change_log_entry_temp is empty, then "
            + "new SyncPITTables().showResults(true).syncAllPITTables(), then run this job again";
        debugMap.put("error", errorMessage);
        LOG.error(errorMessage);
        if (theOtherJobInput != null) {
          // OtherJobBase keeps an error status set here instead of changing it to SUCCESS
          theOtherJobInput.getHib3GrouperLoaderLog().setStatus(GrouperLoaderStatus.ERROR.name());
        }
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().setJobMessage(GrouperUtil.mapToString(debugMap));
        theOtherJobInput.getHib3GrouperLoaderLog().store();
      }
    }
    
    return null;
  }
  
  public void processBatch(List<Object[]> sqlCacheGroupDataBatch) {
    
    Map<MultiKey, Long> ownerInternalIdAndFieldInternalIdToInternalId = new HashMap<>();
    Map<Long, Long> internalIdToMembershipSize = new HashMap<>();
    
    for (Object[] sqlCacheGroupData : sqlCacheGroupDataBatch) {
      long internalId = (long)sqlCacheGroupData[0];
      long ownerInternalId = (long)sqlCacheGroupData[1];
      long fieldInternalId = (long)sqlCacheGroupData[2];
      long membershipSize = (long)sqlCacheGroupData[3];
      
      ownerInternalIdAndFieldInternalIdToInternalId.put(new MultiKey(ownerInternalId, fieldInternalId), internalId);
      internalIdToMembershipSize.put(internalId, membershipSize);
    }
    
    // query cache memberships
    GcDbAccess gcDbAccess = new GcDbAccess();
    StringBuilder sqlQueryCacheMemberships = new StringBuilder("select sql_cache_group_internal_id, member_internal_id, flattened_add_timestamp from grouper_sql_cache_mship where ");
    boolean isFirst = true;
    for (Object[] sqlCacheGroupData : sqlCacheGroupDataBatch) {
      long internalId = (long)sqlCacheGroupData[0];

      if (!isFirst) {
        sqlQueryCacheMemberships.append(" or ");
      }
      sqlQueryCacheMemberships.append(" sql_cache_group_internal_id = ? ");
      gcDbAccess.addBindVar(internalId);
      isFirst = false;
    }
    
    LOG.debug("processBatch querying cacheMemberships with internalId bind vars: " + sqlCacheGroupDataBatch.stream().map(row -> String.valueOf((long)row[0])).collect(java.util.stream.Collectors.joining(", ")));
    List<Object[]> cacheMemberships = gcDbAccess.sql(sqlQueryCacheMemberships.toString()).selectList(Object[].class);
    Map<Long, Set<Long>> cacheMembershipsSqlCacheGroupInternalIdToMembers = new HashMap<>();
    Map<MultiKey, Long> cacheMembershipsFlattenedAddTimeMicros = new HashMap<>();
    for (Object[] cacheMembership : cacheMemberships) {
      long sqlCacheGroupInternalId = GrouperUtil.longObjectValue(cacheMembership[0], false);
      long memberInternalId = GrouperUtil.longObjectValue(cacheMembership[1], false);
      long flattenedAddTimestamp = GrouperUtil.longObjectValue(cacheMembership[2], false);

      if (cacheMembershipsSqlCacheGroupInternalIdToMembers.get(sqlCacheGroupInternalId) == null) {
        cacheMembershipsSqlCacheGroupInternalIdToMembers.put(sqlCacheGroupInternalId, new HashSet<>());
      }
      cacheMembershipsSqlCacheGroupInternalIdToMembers.get(sqlCacheGroupInternalId).add(memberInternalId);
      
      cacheMembershipsFlattenedAddTimeMicros.put(new MultiKey(sqlCacheGroupInternalId, memberInternalId), flattenedAddTimestamp);
    }
    
    // split batch by query strategy: exact (historical) vs. estimated (active-only)
    List<Object[]> exactBatch = new ArrayList<>();
    List<Object[]> estimatedBatch = new ArrayList<>();
    for (Object[] sqlCacheGroupData : sqlCacheGroupDataBatch) {
      long ownerInternalId = (long) sqlCacheGroupData[1];
      long fieldInternalId = (long) sqlCacheGroupData[2];
      Field field = fieldInternalIdToField.get(fieldInternalId);
      String pitOwnerId = null;
      if (field.isGroupAccessField() || field.getName().equals("members")) {
        pitOwnerId = groupInternalIdToPITId.get(ownerInternalId);
      } else if (field.isStemListField()) {
        pitOwnerId = stemIdIndexToPITId.get(ownerInternalId);
      } else if (field.isAttributeDefListField()) {
        pitOwnerId = attributeDefIdIndexToPITId.get(ownerInternalId);
      }
      String pitFieldId = fieldInternalIdToPITId.get(fieldInternalId);
      if (!largeGroupSetPairs.isEmpty()
          && largeGroupSetPairs.contains(new MultiKey(pitOwnerId, pitFieldId))) {
        LOG.debug("Using estimated start date query for pitOwnerId=" + pitOwnerId
            + " pitFieldId=" + pitFieldId);
        estimatedBatch.add(sqlCacheGroupData);
      } else {
        exactBatch.add(sqlCacheGroupData);
      }
    }

    Map<Long, Set<Long>> pitMembershipsSqlCacheGroupInternalIdToMembers = new HashMap<>();
    Map<MultiKey, Long> pitMembershipsFlattenedAddTimeMicros = new HashMap<>();

    if (!exactBatch.isEmpty()) {
      queryPitMembershipsExact(exactBatch, ownerInternalIdAndFieldInternalIdToInternalId,
          pitMembershipsSqlCacheGroupInternalIdToMembers, pitMembershipsFlattenedAddTimeMicros);
    }
    if (!estimatedBatch.isEmpty()) {
      queryPitMembershipsEstimated(estimatedBatch, ownerInternalIdAndFieldInternalIdToInternalId,
          pitMembershipsSqlCacheGroupInternalIdToMembers, pitMembershipsFlattenedAddTimeMicros);
    }

    // now compare
    List<SqlCacheMembership> sqlCacheMembershipsToInsert = new ArrayList<>();
    List<List<Object>> bindVarsSqlCacheMshipDeletes = new ArrayList<>();
    List<List<Object>> bindVarsSqlCacheMshipUpdates = new ArrayList<>();
    //List<List<Object>> bindVarsSqlCacheGroupMembershipSizeUpdate = new ArrayList<>();
    Set<Long> sqlCacheGroupIdsForMembershipSizeUpdate = new HashSet<>();

    for (MultiKey ownerInternalIdAndFieldInternalIdMultiKey : ownerInternalIdAndFieldInternalIdToInternalId.keySet()) {
      long sqlCacheGroupInternalId = ownerInternalIdAndFieldInternalIdToInternalId.get(ownerInternalIdAndFieldInternalIdMultiKey);
      
      Set<Long> cacheMembershipsMemberInternalIds = cacheMembershipsSqlCacheGroupInternalIdToMembers.get(sqlCacheGroupInternalId);
      Set<Long> pitMembershipsMemberInternalIds = pitMembershipsSqlCacheGroupInternalIdToMembers.get(sqlCacheGroupInternalId);
      
      if (cacheMembershipsMemberInternalIds == null) {
        cacheMembershipsMemberInternalIds = new HashSet<>();
      }
      
      if (pitMembershipsMemberInternalIds == null) {
        pitMembershipsMemberInternalIds = new HashSet<>();
      }
      
      // memberships processed
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().addTotalCount(pitMembershipsMemberInternalIds.size());
      }
      incrementCountInDebugMap("sqlCacheMshipsTotalProcessedCount", pitMembershipsMemberInternalIds.size());

      Set<Long> cacheMembershipsMemberInternalIdsToAdd = new HashSet<>(pitMembershipsMemberInternalIds);
      cacheMembershipsMemberInternalIdsToAdd.removeAll(cacheMembershipsMemberInternalIds);
      
      Set<Long> cacheMembershipsMemberInternalIdsToDelete = new HashSet<>(cacheMembershipsMemberInternalIds);
      cacheMembershipsMemberInternalIdsToDelete.removeAll(pitMembershipsMemberInternalIds);
      
      Set<Long> cacheMembershipsMemberInternalIdsUnchanged = new HashSet<>(cacheMembershipsMemberInternalIds);
      cacheMembershipsMemberInternalIdsUnchanged.retainAll(pitMembershipsMemberInternalIds);
      
      for (Long cacheMembershipsMemberInternalIdToAdd : cacheMembershipsMemberInternalIdsToAdd) {
        SqlCacheMembership sqlCacheMembershipToInsert = new SqlCacheMembership();
        long membershipAddedLong = pitMembershipsFlattenedAddTimeMicros.get(new MultiKey(ownerInternalIdAndFieldInternalIdMultiKey.getKey(0), ownerInternalIdAndFieldInternalIdMultiKey.getKey(1), cacheMembershipsMemberInternalIdToAdd));
        sqlCacheMembershipToInsert.setFlattenedAddTimestamp(membershipAddedLong);
        sqlCacheMembershipToInsert.setMemberInternalId(cacheMembershipsMemberInternalIdToAdd);
        sqlCacheMembershipToInsert.setSqlCacheGroupInternalId(sqlCacheGroupInternalId);
        sqlCacheMembershipsToInsert.add(sqlCacheMembershipToInsert);
      }
      
      for (Long cacheMembershipsMemberInternalIdToDelete : cacheMembershipsMemberInternalIdsToDelete) {
        bindVarsSqlCacheMshipDeletes.add(GrouperUtil.toListObject(cacheMembershipsMemberInternalIdToDelete, sqlCacheGroupInternalId));
      }
      
      for (Long cacheMembershipsMemberInternalIdUnchanged : cacheMembershipsMemberInternalIdsUnchanged) {
        long pitMembershipAddedLongMicros = pitMembershipsFlattenedAddTimeMicros.get(new MultiKey(ownerInternalIdAndFieldInternalIdMultiKey.getKey(0), ownerInternalIdAndFieldInternalIdMultiKey.getKey(1), cacheMembershipsMemberInternalIdUnchanged));
        long cacheMembershipAddedLongMicros = cacheMembershipsFlattenedAddTimeMicros.get(new MultiKey(sqlCacheGroupInternalId, cacheMembershipsMemberInternalIdUnchanged));

        if (pitMembershipAddedLongMicros != cacheMembershipAddedLongMicros) {
          bindVarsSqlCacheMshipUpdates.add(GrouperUtil.toListObject(pitMembershipAddedLongMicros, cacheMembershipsMemberInternalIdUnchanged, sqlCacheGroupInternalId));
        }
      }
      
      if (sqlCacheMembershipsToInsert.size() > 0 ||
          bindVarsSqlCacheMshipDeletes.size() > 0 ||
          bindVarsSqlCacheMshipUpdates.size() > 0) {
        sqlCacheGroupIdsForMembershipSizeUpdate.add(sqlCacheGroupInternalId);
      } else if (pitMembershipsMemberInternalIds.size() != internalIdToMembershipSize.get(sqlCacheGroupInternalId)) {
        if (internalIdToMembershipSize.get(sqlCacheGroupInternalId) < 0) {
          sqlCacheGroupIdsForMembershipSizeUpdate.add(sqlCacheGroupInternalId);
        } else {
          // re-check since it's likely the counts just changed while this task was running
          Long newMembershipSize = new GcDbAccess().sql("select membership_size from grouper_sql_cache_group gscg where gscg.internal_id = ?").addBindVar(sqlCacheGroupInternalId).select(Long.class);
          if (newMembershipSize != null && pitMembershipsMemberInternalIds.size() != newMembershipSize) {
            sqlCacheGroupIdsForMembershipSizeUpdate.add(sqlCacheGroupInternalId);
          }
        }
      }
    }
    
    // store
    int numberOfInserts = SqlCacheMembershipDao.store(sqlCacheMembershipsToInsert, null, true, true, true);
    
    if (theOtherJobInput != null) {
      theOtherJobInput.getHib3GrouperLoaderLog().addInsertCount(numberOfInserts);
    }
    incrementCountInDebugMap("sqlCacheMshipsInsertedCount", numberOfInserts);

    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);

    if (bindVarsSqlCacheMshipDeletes.size() > 0) {
      new GcDbAccess().sql("delete from grouper_sql_cache_mship where member_internal_id = ? and sql_cache_group_internal_id = ?").batchSize(batchSize).batchBindVars(bindVarsSqlCacheMshipDeletes).executeBatchSql();
    
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().addDeleteCount(bindVarsSqlCacheMshipDeletes.size());
      }
      incrementCountInDebugMap("sqlCacheMshipsDeletedCount", bindVarsSqlCacheMshipDeletes.size());
    }

    if (bindVarsSqlCacheMshipUpdates.size() > 0) {
      new GcDbAccess().sql("update grouper_sql_cache_mship set flattened_add_timestamp = ? where member_internal_id = ? and sql_cache_group_internal_id = ?").batchSize(batchSize).batchBindVars(bindVarsSqlCacheMshipUpdates).executeBatchSql();
    
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().addUpdateCount(bindVarsSqlCacheMshipUpdates.size());
      }
      incrementCountInDebugMap("sqlCacheMshipsUpdatedCount", bindVarsSqlCacheMshipUpdates.size());
    }
    
    if (sqlCacheGroupIdsForMembershipSizeUpdate.size() > 0) {
      for (Long id : sqlCacheGroupIdsForMembershipSizeUpdate) {
        gcDbAccess = new GcDbAccess();

        StringBuilder sql = new StringBuilder("update grouper_sql_cache_group gscg set membership_size = (select count(*) from grouper_sql_cache_mship gscm where gscm.sql_cache_group_internal_id = ?) where gscg.internal_id = ?");
        
        gcDbAccess.addBindVar(id);
        gcDbAccess.addBindVar(id);
        
        gcDbAccess.sql(sql.toString()).executeSql();
        
        if (theOtherJobInput != null) {
          theOtherJobInput.getHib3GrouperLoaderLog().addUpdateCount(1);
        }
        incrementCountInDebugMap("sqlCacheGroupsUpdatedMembershipSizeCount", 1);
      }
    }
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();
  }

  private void queryPitMembershipsExact(
      List<Object[]> sqlCacheGroupDataBatch,
      Map<MultiKey, Long> ownerInternalIdAndFieldInternalIdToInternalId,
      Map<Long, Set<Long>> pitMembershipsSqlCacheGroupInternalIdToMembers,
      Map<MultiKey, Long> pitMembershipsFlattenedAddTimeMicros) {

    GcDbAccess gcDbAccess = new GcDbAccess();
    StringBuilder sqlQueryPITMemberships = new StringBuilder(
        "select gpgs1.owner_id, gpgs1.field_id, gpm1.member_id,"
        + " gpgs1.start_time, gpm1.start_time, gpgs1.end_time, gpm1.end_time"
        + " from grouper_pit_group_set gpgs1, grouper_pit_memberships gpm1"
        + " where gpm1.owner_id = gpgs1.member_id and gpm1.field_id = gpgs1.member_field_id and ( ");
    boolean isFirst = true;
    for (Object[] sqlCacheGroupData : sqlCacheGroupDataBatch) {
      long ownerInternalId = (long) sqlCacheGroupData[1];
      long fieldInternalId = (long) sqlCacheGroupData[2];
      String pitFieldId = fieldInternalIdToPITId.get(fieldInternalId);
      Field field = fieldInternalIdToField.get(fieldInternalId);

      if (!isFirst) {
        sqlQueryPITMemberships.append(" or ");
      }
      sqlQueryPITMemberships.append(" (gpgs1.owner_id=? and gpgs1.field_id=?) ");

      if (field.isGroupAccessField() || field.getName().equals("members")) {
        gcDbAccess.addBindVar(groupInternalIdToPITId.get(ownerInternalId));
      } else if (field.isStemListField()) {
        gcDbAccess.addBindVar(stemIdIndexToPITId.get(ownerInternalId));
      } else if (field.isAttributeDefListField()) {
        gcDbAccess.addBindVar(attributeDefIdIndexToPITId.get(ownerInternalId));
      }

      gcDbAccess.addBindVar(pitFieldId);
      isFirst = false;
    }

    sqlQueryPITMemberships.append(")");
    sqlQueryPITMemberships.append(
        " and exists(select 1 from grouper_pit_group_set gpgs2, grouper_pit_memberships gpm2"
        + " where gpm2.owner_id = gpgs2.member_id and gpm2.field_id = gpgs2.member_field_id"
        + " and gpgs2.owner_id=gpgs1.owner_id and gpgs2.field_id=gpgs1.field_id"
        + " and gpm1.member_id = gpm2.member_id and gpgs2.active='T' and gpm2.active='T')");

    long start = System.currentTimeMillis();
    List<Object[]> pitMemberships = gcDbAccess.sql(sqlQueryPITMemberships.toString()).selectList(Object[].class);
    long diff = System.currentTimeMillis() - start;
    if (LOG.isDebugEnabled()) {
      LOG.debug("queryPitMembershipsExact time=" + diff + ", batch size=" + sqlCacheGroupDataBatch.size()
          + ", vars=" + gcDbAccess.getBindVars());
    }

    // sort based on end time desc
    pitMemberships.sort(new Comparator<Object[]>() {
      @Override
      public int compare(Object[] o1, Object[] o2) {
        Long o1GroupSetEndTimeMicros = GrouperUtil.longObjectValue(o1[5], true);
        Long olMembershipEndTimeMicros = GrouperUtil.longObjectValue(o1[6], true);
        Long o2GroupSetEndTimeMicros = GrouperUtil.longObjectValue(o2[5], true);
        Long o2MembershipEndTimeMicros = GrouperUtil.longObjectValue(o2[6], true);

        boolean o1Active = (o1GroupSetEndTimeMicros == null && olMembershipEndTimeMicros == null);
        boolean o2Active = (o2GroupSetEndTimeMicros == null && o2MembershipEndTimeMicros == null);

        if (o1Active && !o2Active) {
          return -1;
        } else if (!o1Active && o2Active) {
          return 1;
        } else if (o1Active && o2Active) {
          return 0;
        } else {
          Long o1EndTime = Math.min(o1GroupSetEndTimeMicros != null ? o1GroupSetEndTimeMicros : Long.MAX_VALUE, olMembershipEndTimeMicros != null ? olMembershipEndTimeMicros : Long.MAX_VALUE);
          Long o2EndTime = Math.min(o2GroupSetEndTimeMicros != null ? o2GroupSetEndTimeMicros : Long.MAX_VALUE, o2MembershipEndTimeMicros != null ? o2MembershipEndTimeMicros : Long.MAX_VALUE);
          return o2EndTime.compareTo(o1EndTime);
        }
      }
    });

    for (Object[] pitMembership : pitMemberships) {
      String pitOwnerId = (String) pitMembership[0];
      String pitFieldId = (String) pitMembership[1];
      String pitMemberId = (String) pitMembership[2];

      long groupSetStartTimeMicros = GrouperUtil.longObjectValue(pitMembership[3], false);
      long membershipStartTimeMicros = GrouperUtil.longObjectValue(pitMembership[4], false);
      long startTimeMicros = Math.max(groupSetStartTimeMicros, membershipStartTimeMicros);

      Long groupSetEndTimeMicros = GrouperUtil.longObjectValue(pitMembership[5], true);
      Long membershipEndTimeMicros = GrouperUtil.longObjectValue(pitMembership[6], true);
      Long endTimeMicros = null;
      if (groupSetEndTimeMicros == null && membershipEndTimeMicros != null) {
        endTimeMicros = membershipEndTimeMicros;
      } else if (groupSetEndTimeMicros != null && membershipEndTimeMicros == null) {
        endTimeMicros = groupSetEndTimeMicros;
      } else if (groupSetEndTimeMicros != null && membershipEndTimeMicros != null) {
        endTimeMicros = Math.min(groupSetEndTimeMicros, membershipEndTimeMicros);
      }

      if (endTimeMicros != null && startTimeMicros > endTimeMicros) {
        // this is invalid, ignore
        continue;
      }

      Long fieldInternalId = pitIdToFieldInternalId.get(pitFieldId);
      Long memberInternalId = pitIdToMemberInternalId.get(pitMemberId);
      if (memberInternalId == null) {
        // maybe it's a new member, try finding it
        PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findById(pitMemberId, false);
        if (pitMember != null) {
          memberInternalId = pitMember.getSourceInternalId();
          pitIdToMemberInternalId.put(pitMemberId, memberInternalId);
        }
      }

      Field field = fieldInternalIdToField.get(fieldInternalId);
      Long ownerInternalId = null;
      if (field.isGroupAccessField() || field.getName().equals("members")) {
        ownerInternalId = pitIdToGroupInternalId.get(pitOwnerId);
      } else if (field.isStemListField()) {
        ownerInternalId = pitIdToStemIdIndex.get(pitOwnerId);
      } else if (field.isAttributeDefListField()) {
        ownerInternalId = pitIdToAttributeDefIdIndex.get(pitOwnerId);
      }

      if (ownerInternalId == null || memberInternalId == null) {
        continue;
      }

      if (endTimeMicros == null) {
        Long sqlCacheGroupInternalId = ownerInternalIdAndFieldInternalIdToInternalId.get(new MultiKey(ownerInternalId, fieldInternalId));
        if (pitMembershipsSqlCacheGroupInternalIdToMembers.get(sqlCacheGroupInternalId) == null) {
          pitMembershipsSqlCacheGroupInternalIdToMembers.put(sqlCacheGroupInternalId, new HashSet<>());
        }
        pitMembershipsSqlCacheGroupInternalIdToMembers.get(sqlCacheGroupInternalId).add(memberInternalId);
      }

      MultiKey ownerFieldMemberMultiKey = new MultiKey(ownerInternalId, fieldInternalId, memberInternalId);
      Long existingStartTimeMicros = pitMembershipsFlattenedAddTimeMicros.get(ownerFieldMemberMultiKey);
      if (existingStartTimeMicros == null || existingStartTimeMicros > startTimeMicros) {

        // must be an active membership or one that ended after a previous one started
        if (endTimeMicros == null || (existingStartTimeMicros != null && endTimeMicros >= existingStartTimeMicros)) {
          pitMembershipsFlattenedAddTimeMicros.put(ownerFieldMemberMultiKey, startTimeMicros);
        }
      }
    }
  }

  private void queryPitMembershipsEstimated(
      List<Object[]> sqlCacheGroupDataBatch,
      Map<MultiKey, Long> ownerInternalIdAndFieldInternalIdToInternalId,
      Map<Long, Set<Long>> pitMembershipsSqlCacheGroupInternalIdToMembers,
      Map<MultiKey, Long> pitMembershipsFlattenedAddTimeMicros) {

    GcDbAccess gcDbAccess = new GcDbAccess();
    StringBuilder sqlQueryPITMemberships = new StringBuilder(
        "select gpgs.owner_id, gpgs.field_id, gpitm.source_internal_id,"
        + " min(greatest(gpgs.start_time, gpm.start_time))"
        + " from grouper_pit_group_set gpgs"
        + " join grouper_pit_memberships gpm"
        + "   on gpm.owner_id = gpgs.member_id and gpm.field_id = gpgs.member_field_id"
        + " join grouper_pit_members gpitm on gpitm.id = gpm.member_id"
        + " where gpgs.active = 'T' and gpm.active = 'T'"
        + " and gpitm.source_internal_id is not null and (");
    boolean isFirst = true;
    for (Object[] sqlCacheGroupData : sqlCacheGroupDataBatch) {
      long ownerInternalId = (long) sqlCacheGroupData[1];
      long fieldInternalId = (long) sqlCacheGroupData[2];
      String pitFieldId = fieldInternalIdToPITId.get(fieldInternalId);
      Field field = fieldInternalIdToField.get(fieldInternalId);

      String pitOwnerId = null;
      if (field.isGroupAccessField() || field.getName().equals("members")) {
        pitOwnerId = groupInternalIdToPITId.get(ownerInternalId);
      } else if (field.isStemListField()) {
        pitOwnerId = stemIdIndexToPITId.get(ownerInternalId);
      } else if (field.isAttributeDefListField()) {
        pitOwnerId = attributeDefIdIndexToPITId.get(ownerInternalId);
      }

      if (!isFirst) {
        sqlQueryPITMemberships.append(" or ");
      }
      sqlQueryPITMemberships.append("(gpgs.owner_id=? and gpgs.field_id=?)");
      gcDbAccess.addBindVar(pitOwnerId);
      gcDbAccess.addBindVar(pitFieldId);
      isFirst = false;
    }
    sqlQueryPITMemberships.append(") group by gpgs.owner_id, gpgs.field_id, gpitm.source_internal_id");

    long start = System.currentTimeMillis();
    List<Object[]> pitMemberships = gcDbAccess.sql(sqlQueryPITMemberships.toString()).selectList(Object[].class);
    long diff = System.currentTimeMillis() - start;
    if (LOG.isDebugEnabled()) {
      LOG.debug("queryPitMembershipsEstimated time=" + diff + ", batch size=" + sqlCacheGroupDataBatch.size()
          + ", vars=" + gcDbAccess.getBindVars());
    }

    for (Object[] pitMembership : pitMemberships) {
      String pitOwnerId = (String) pitMembership[0];
      String pitFieldId = (String) pitMembership[1];
      long memberInternalId = GrouperUtil.longObjectValue(pitMembership[2], false);
      long flattenedAddTimeMicros = GrouperUtil.longObjectValue(pitMembership[3], false);

      Long fieldInternalId = pitIdToFieldInternalId.get(pitFieldId);
      if (fieldInternalId == null) {
        continue;
      }
      Field field = fieldInternalIdToField.get(fieldInternalId);
      Long ownerInternalId = null;
      if (field.isGroupAccessField() || field.getName().equals("members")) {
        ownerInternalId = pitIdToGroupInternalId.get(pitOwnerId);
      } else if (field.isStemListField()) {
        ownerInternalId = pitIdToStemIdIndex.get(pitOwnerId);
      } else if (field.isAttributeDefListField()) {
        ownerInternalId = pitIdToAttributeDefIdIndex.get(pitOwnerId);
      }

      if (ownerInternalId == null) {
        continue;
      }

      Long sqlCacheGroupInternalId = ownerInternalIdAndFieldInternalIdToInternalId.get(new MultiKey(ownerInternalId, fieldInternalId));
      if (sqlCacheGroupInternalId == null) {
        continue;
      }

      if (pitMembershipsSqlCacheGroupInternalIdToMembers.get(sqlCacheGroupInternalId) == null) {
        pitMembershipsSqlCacheGroupInternalIdToMembers.put(sqlCacheGroupInternalId, new HashSet<>());
      }
      pitMembershipsSqlCacheGroupInternalIdToMembers.get(sqlCacheGroupInternalId).add(memberInternalId);

      pitMembershipsFlattenedAddTimeMicros.put(
          new MultiKey(ownerInternalId, fieldInternalId, memberInternalId),
          flattenedAddTimeMicros);
    }
  }

  /**
   * find active PIT owners (groups/stems/attribute defs) that cannot be put in the sql cache since the PIT id
   * is null or the live object does not exist.  Objects with pending changes in grouper_change_log_entry_temp
   * are in flight (e.g. a delete not processed yet) and are not counted.  Adds the count and a sample of names
   * to the debug map.
   * @param debugPrefix prefix for debug map keys, e.g. pitGroups
   * @param pitTableName e.g. grouper_pit_groups
   * @param pitIdColumnName e.g. source_internal_id
   * @param liveTableName e.g. grouper_groups
   * @param recentTimeMillis only look at PIT rows that started before this time
   * @return the number of PIT owners that cannot be cached
   */
  private long findPitOwnersCannotCache(String debugPrefix, String pitTableName, String pitIdColumnName, String liveTableName, long recentTimeMillis) {

    List<String> pitNames = new GcDbAccess().sql("select pit.name from " + pitTableName + " pit where pit.active='T' and pit.start_time < ?"
        + " and (pit." + pitIdColumnName + " is null or not exists (select 1 from " + liveTableName + " live where live.id = pit.source_id))"
        + " and not exists (select 1 from grouper_change_log_entry_temp temp where temp.string01 = pit.source_id)")
      .addBindVar(recentTimeMillis * 1000L)
      .selectList(String.class);

    if (pitNames.size() > 0) {
      debugMap.put(debugPrefix + "CannotCacheCount", pitNames.size());

      // just log a sample so the job message does not get huge
      List<String> pitNamesSample = pitNames.size() > 10 ? pitNames.subList(0, 10) : pitNames;
      debugMap.put(debugPrefix + "CannotCacheSample", GrouperUtil.join(pitNamesSample.iterator(), ", "));
      LOG.error("Active " + pitTableName + " rows that cannot be cached: " + pitNames.size() + ", sample: " + pitNamesSample);
    }

    return pitNames.size();
  }

  public static void runNowWithoutDaemon(boolean theMinimalMode) {
    Hib3GrouperLoaderLog hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
    hib3GrouperLoaderLog.setJobScheduleType("MANUAL_FROM_GSH");
    hib3GrouperLoaderLog.setJobName("OTHER_JOB_sqlCacheFullSync");
    
    try {
      if (theMinimalMode) {
        minimalMode = true;
      }
      
      GrouperLoaderJob.runJob(hib3GrouperLoaderLog, (Group)null, GrouperSession.staticGrouperSession());
    } finally {
      minimalMode = false;
    }
  }
  
  private void incrementCountInDebugMap(String property, long count) {
    if (debugMap != null) {
      if (debugMap.containsKey(property)) {
        long existingValue = (Long)debugMap.get(property);
        debugMap.put(property, (existingValue + count));
      } else {
        debugMap.put(property, count);
      }
    }
  }
}

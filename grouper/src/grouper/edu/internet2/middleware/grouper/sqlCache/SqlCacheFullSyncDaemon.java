package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITField;
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
  
  private Map<Long, String> memberInternalIdToPITId = new HashMap<>();
  private Map<Long, String> groupInternalIdToPITId = new HashMap<>();
  private Map<Long, String> stemIdIndexToPITId = new HashMap<>();
  private Map<Long, String> attributeDefIdIndexToPITId = new HashMap<>();
  
  private Map<String, Long> pitIdToMemberInternalId = new HashMap<>();
  private Map<String, Long> pitIdToGroupInternalId = new HashMap<>();
  private Map<String, Long> pitIdToStemIdIndex = new HashMap<>();
  private Map<String, Long> pitIdToAttributeDefIdIndex = new HashMap<>();
  
  private OtherJobInput theOtherJobInput = null;

  @Override
  public OtherJobOutput run(final OtherJobInput theOtherJobInput) {
    
    this.theOtherJobInput = theOtherJobInput;
    
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

    List<Object[]> pitMembersData = new GcDbAccess().sql("select id, source_internal_id from grouper_pit_members where active='T'").selectList(Object[].class);
    for (Object[] pitMemberData : pitMembersData) {
      String pitId = (String)pitMemberData[0];
      long sourceInternalId = GrouperUtil.longObjectValue(pitMemberData[1], false);
      memberInternalIdToPITId.put(sourceInternalId, pitId);
      pitIdToMemberInternalId.put(pitId, sourceInternalId);
    }
    pitMembersData = null;
    LOG.info("Done retrieving data from grouper_pit_members");
    
    List<Object[]> pitGroupsData = new GcDbAccess().sql("select id, source_internal_id from grouper_pit_groups where active='T'").selectList(Object[].class);
    for (Object[] pitGroupData : pitGroupsData) {
      String pitId = (String)pitGroupData[0];
      long sourceInternalId = GrouperUtil.longObjectValue(pitGroupData[1], false);
      groupInternalIdToPITId.put(sourceInternalId, pitId);
      pitIdToGroupInternalId.put(pitId, sourceInternalId);
    }
    pitGroupsData = null;
    LOG.info("Done retrieving data from grouper_pit_groups");
    
    List<Object[]> pitStemsData = new GcDbAccess().sql("select id, source_id_index from grouper_pit_stems where active='T'").selectList(Object[].class);
    for (Object[] pitStemData : pitStemsData) {
      String pitId = (String)pitStemData[0];
      long sourceIdIndex = GrouperUtil.longObjectValue(pitStemData[1], false);
      stemIdIndexToPITId.put(sourceIdIndex, pitId);
      pitIdToStemIdIndex.put(pitId, sourceIdIndex);
    }
    pitStemsData = null;
    LOG.info("Done retrieving data from grouper_pit_stems");
    
    List<Object[]> pitAttributeDefsData = new GcDbAccess().sql("select id, source_id_index from grouper_pit_attribute_def where active='T'").selectList(Object[].class);
    for (Object[] pitAttributeDefData : pitAttributeDefsData) {
      String pitId = (String)pitAttributeDefData[0];
      long sourceIdIndex = GrouperUtil.longObjectValue(pitAttributeDefData[1], false);
      attributeDefIdIndexToPITId.put(sourceIdIndex, pitId);
      pitIdToAttributeDefIdIndex.put(pitId, sourceIdIndex);
    }
    pitAttributeDefsData = null;
    LOG.info("Done retrieving data from grouper_pit_attribute_def");

    
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
      }
      
      LOG.info("Done checking for rows in grouper_sql_cache_group that should be disabled for field=" + field.getName());
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
      }
      
      LOG.info("Done checking for rows in grouper_sql_cache_group that should not be disabled for field=" + field.getName());
    }
    
    // STEP 3 - find rows in grouper_sql_cache_group that need to be added
    //        - need to account for entities having limited access fields
    for (Field field : fields) {
      String sql = null;
      if (field.isEntityListField()) {
        sql = "select gpg.source_internal_id, gpg.start_time from grouper_pit_groups gpg where gpg.active='T' and gpg.start_time < ? and not exists (select 1 from grouper_sql_cache_group gscg where gscg.group_internal_id = gpg.source_internal_id and gscg.field_internal_id = ?)";
      } else if (field.isGroupAccessField() || field.getName().equals("members")) {
        // make sure not an entity
        sql = "select gpg.source_internal_id, gpg.start_time from grouper_pit_groups gpg, grouper_groups gg where gpg.source_id=gg.id and gg.type_of_group != 'entity' and gpg.active='T' and gpg.start_time < ? and not exists (select 1 from grouper_sql_cache_group gscg where gscg.group_internal_id = gpg.source_internal_id and gscg.field_internal_id = ?)";
      } else if (field.isStemListField()) {
        sql = "select gps.source_id_index, gps.start_time from grouper_pit_stems gps where gps.active='T' and gps.start_time < ? and not exists (select 1 from grouper_sql_cache_group gscg where gscg.group_internal_id = gps.source_id_index and gscg.field_internal_id = ?)";
      } else if (field.isAttributeDefListField()) {
        sql = "select gpad.source_id_index, gpad.start_time from grouper_pit_attribute_def gpad where gpad.active='T' and gpad.start_time < ? and not exists (select 1 from grouper_sql_cache_group gscg where gscg.group_internal_id = gpad.source_id_index and gscg.field_internal_id = ?)";
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
          }
        }
      }
      
      LOG.info("Done checking for rows in grouper_sql_cache_group that need to be added for field=" + field.getName());
    }
    
    // STEP 4 - fix membership size for disabled groups
    {
      int count = new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = '0' where membership_size != '0' and disabled_on is not null").executeSql();
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().addUpdateCount(count);
      }
      
      LOG.info("Done fixing membership size for disabled groups");
    }
    
    // STEP 5 - delete from grouper_sql_cache_mship where rows have invalid references
    {
      int count = new GcDbAccess().sql("delete from grouper_sql_cache_mship where sql_cache_group_internal_id not in (select internal_id from grouper_sql_cache_group where disabled_on is null)").executeSql();
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().addDeleteCount(count);
      }
      
      count = new GcDbAccess().sql("delete from grouper_sql_cache_mship where member_internal_id not in (select source_internal_id from grouper_pit_members where active='T')").executeSql();
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().addDeleteCount(count);
      }
      
      LOG.info("Done deleting from grouper_sql_cache_mship where rows have invalid references");
    }
    
    // STEP 6 - fix grouper_sql_cache_mship and counts in grouper_sql_cache_group
    {
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
        
        if (countWithoutSkips % 100000 == 0) {
          if (theOtherJobInput != null) {
            theOtherJobInput.getHib3GrouperLoaderLog().setJobMessage("Working on membership sync " + count + " of " + sqlCacheGroupsData.size());
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
      if (membershipSyncStartTimeMicros != null) {
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
      
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().setJobMessage("Job completed successfully");
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
    
    List<Object[]> cacheMemberships = gcDbAccess.sql(sqlQueryCacheMemberships.toString()).selectList(Object[].class);
    Map<Long, Set<Long>> cacheMembershipsSqlCacheGroupInternalIdToMembers = new HashMap<>();
    Map<MultiKey, Long> cacheMembershipsFlattenedAddTimeMillis = new HashMap<>();
    for (Object[] cacheMembership : cacheMemberships) {
      long sqlCacheGroupInternalId = GrouperUtil.longObjectValue(cacheMembership[0], false);
      long memberInternalId = GrouperUtil.longObjectValue(cacheMembership[1], false);
      Timestamp flattenedAddTimestamp = GrouperUtil.timestampObjectValue(cacheMembership[2], false);

      if (cacheMembershipsSqlCacheGroupInternalIdToMembers.get(sqlCacheGroupInternalId) == null) {
        cacheMembershipsSqlCacheGroupInternalIdToMembers.put(sqlCacheGroupInternalId, new HashSet<>());
      }
      cacheMembershipsSqlCacheGroupInternalIdToMembers.get(sqlCacheGroupInternalId).add(memberInternalId);
      
      cacheMembershipsFlattenedAddTimeMillis.put(new MultiKey(sqlCacheGroupInternalId, memberInternalId), flattenedAddTimestamp.getTime());
    }
    
    // query pit memberships
    gcDbAccess = new GcDbAccess();
    //StringBuilder sqlQueryPITMemberships = new StringBuilder("select gpgs.owner_id, gpgs.field_id, gpm.member_id, gpgs.start_time, gpm.start_time from grouper_pit_group_set gpgs, grouper_pit_memberships gpm where gpm.owner_id = gpgs.member_id and gpm.field_id = gpgs.member_field_id and gpgs.active='T' and gpm.active='T' and ( ");
    StringBuilder sqlQueryPITMemberships = new StringBuilder("select gpgs1.owner_id, gpgs1.field_id, gpm1.member_id, gpgs1.start_time, gpm1.start_time, gpgs1.end_time, gpm1.end_time from grouper_pit_group_set gpgs1, grouper_pit_memberships gpm1 where gpm1.owner_id = gpgs1.member_id and gpm1.field_id = gpgs1.member_field_id and ( ");
    isFirst = true;
    for (Object[] sqlCacheGroupData : sqlCacheGroupDataBatch) {
      long ownerInternalId = (long)sqlCacheGroupData[1];
      long fieldInternalId = (long)sqlCacheGroupData[2];
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
    
    sqlQueryPITMemberships.append(" and exists(select 1 from grouper_pit_group_set gpgs2, grouper_pit_memberships gpm2 where gpm2.owner_id = gpgs2.member_id and gpm2.field_id = gpgs2.member_field_id and gpgs2.owner_id=gpgs1.owner_id and gpgs2.field_id=gpgs1.field_id and gpm1.member_id = gpm2.member_id and gpgs2.active='T' and gpm2.active='T')");
    
    long start = System.currentTimeMillis();
    List<Object[]> pitMemberships = gcDbAccess.sql(sqlQueryPITMemberships.toString()).selectList(Object[].class);
    long diff = System.currentTimeMillis() - start;
    if (LOG.isDebugEnabled()) {
      LOG.debug("Query time=" + diff + ", batch size=" + sqlCacheGroupDataBatch.size() + ", vars=" + gcDbAccess.getBindVars());
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
    
    Map<Long, Set<Long>> pitMembershipsSqlCacheGroupInternalIdToMembers = new HashMap<>();
    Map<MultiKey, Long> pitMembershipsFlattenedAddTimeMicros = new HashMap<>();
    for (Object[] pitMembership : pitMemberships) {
      String pitOwnerId = (String)pitMembership[0];
      String pitFieldId = (String)pitMembership[1];
      String pitMemberId = (String)pitMembership[2];
      
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
      
      Set<Long> cacheMembershipsMemberInternalIdsToAdd = new HashSet<>(pitMembershipsMemberInternalIds);
      cacheMembershipsMemberInternalIdsToAdd.removeAll(cacheMembershipsMemberInternalIds);
      
      Set<Long> cacheMembershipsMemberInternalIdsToDelete = new HashSet<>(cacheMembershipsMemberInternalIds);
      cacheMembershipsMemberInternalIdsToDelete.removeAll(pitMembershipsMemberInternalIds);
      
      Set<Long> cacheMembershipsMemberInternalIdsUnchanged = new HashSet<>(cacheMembershipsMemberInternalIds);
      cacheMembershipsMemberInternalIdsUnchanged.retainAll(pitMembershipsMemberInternalIds);
      
      for (Long cacheMembershipsMemberInternalIdToAdd : cacheMembershipsMemberInternalIdsToAdd) {
        SqlCacheMembership sqlCacheMembershipToInsert = new SqlCacheMembership();
        long membershipAddedLong = pitMembershipsFlattenedAddTimeMicros.get(new MultiKey(ownerInternalIdAndFieldInternalIdMultiKey.getKey(0), ownerInternalIdAndFieldInternalIdMultiKey.getKey(1), cacheMembershipsMemberInternalIdToAdd));
        Timestamp membershipAdded = new Timestamp(membershipAddedLong / 1000);
        sqlCacheMembershipToInsert.setFlattenedAddTimestamp(membershipAdded);
        sqlCacheMembershipToInsert.setMemberInternalId(cacheMembershipsMemberInternalIdToAdd);
        sqlCacheMembershipToInsert.setSqlCacheGroupInternalId(sqlCacheGroupInternalId);
        sqlCacheMembershipsToInsert.add(sqlCacheMembershipToInsert);
      }
      
      for (Long cacheMembershipsMemberInternalIdToDelete : cacheMembershipsMemberInternalIdsToDelete) {
        bindVarsSqlCacheMshipDeletes.add(GrouperUtil.toListObject(cacheMembershipsMemberInternalIdToDelete, sqlCacheGroupInternalId));
      }
      
      for (Long cacheMembershipsMemberInternalIdUnchanged : cacheMembershipsMemberInternalIdsUnchanged) {
        long pitMembershipAddedLongMicros = pitMembershipsFlattenedAddTimeMicros.get(new MultiKey(ownerInternalIdAndFieldInternalIdMultiKey.getKey(0), ownerInternalIdAndFieldInternalIdMultiKey.getKey(1), cacheMembershipsMemberInternalIdUnchanged));
        long cacheMembershipAddedLongMillis = cacheMembershipsFlattenedAddTimeMillis.get(new MultiKey(sqlCacheGroupInternalId, cacheMembershipsMemberInternalIdUnchanged));

        if (Math.abs((pitMembershipAddedLongMicros / 1000) - cacheMembershipAddedLongMillis) > 1000) {
          bindVarsSqlCacheMshipUpdates.add(GrouperUtil.toListObject(new Timestamp(pitMembershipAddedLongMicros / 1000), cacheMembershipsMemberInternalIdUnchanged, sqlCacheGroupInternalId));
        }
      }
      
      if (pitMembershipsMemberInternalIds.size() != internalIdToMembershipSize.get(sqlCacheGroupInternalId) ||
          sqlCacheMembershipsToInsert.size() > 0 ||
          bindVarsSqlCacheMshipDeletes.size() > 0 ||
          bindVarsSqlCacheMshipUpdates.size() > 0) {
        //bindVarsSqlCacheGroupMembershipSizeUpdate.add(GrouperUtil.toListObject(pitMembershipsMemberInternalIds.size(), sqlCacheGroupInternalId));
        sqlCacheGroupIdsForMembershipSizeUpdate.add(sqlCacheGroupInternalId);
      }
    }
    
    // store
    int numberOfInserts = SqlCacheMembershipDao.store(sqlCacheMembershipsToInsert, null, true, true, true);
    
    if (theOtherJobInput != null) {
      theOtherJobInput.getHib3GrouperLoaderLog().addInsertCount(numberOfInserts);
    }
    
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);

    if (bindVarsSqlCacheMshipDeletes.size() > 0) {
      int[] rowsChanged = new GcDbAccess().sql("delete from grouper_sql_cache_mship where member_internal_id = ? and sql_cache_group_internal_id = ?").batchSize(batchSize).batchBindVars(bindVarsSqlCacheMshipDeletes).executeBatchSql();
    
      int count = 0;
      for (int i = 0; i < rowsChanged.length; i++) {
        int rowChanged = rowsChanged[i];
        count += rowChanged;
      }
      
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().addDeleteCount(count);
      }
    }
    
    if (bindVarsSqlCacheMshipUpdates.size() > 0) {
      int[] rowsChanged = new GcDbAccess().sql("update grouper_sql_cache_mship set flattened_add_timestamp = ? where member_internal_id = ? and sql_cache_group_internal_id = ?").batchSize(batchSize).batchBindVars(bindVarsSqlCacheMshipUpdates).executeBatchSql();
    
      int count = 0;
      for (int i = 0; i < rowsChanged.length; i++) {
        int rowChanged = rowsChanged[i];
        count += rowChanged;
      }
      
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().addUpdateCount(count);
      }
    }
    
    if (sqlCacheGroupIdsForMembershipSizeUpdate.size() > 0) {
      gcDbAccess = new GcDbAccess();

      //new GcDbAccess().sql("update grouper_sql_cache_group set membership_size = ? where internal_id = ?").batchBindVars(bindVarsSqlCacheGroupMembershipSizeUpdate).executeBatchSql();
      StringBuilder sql = new StringBuilder("update grouper_sql_cache_group gscg set membership_size = (select count(*) from grouper_sql_cache_mship gscm where gscg.internal_id=gscm.sql_cache_group_internal_id) where ");
      
      isFirst = true;
      for (Long id : sqlCacheGroupIdsForMembershipSizeUpdate) {
        if (!isFirst) {
          sql.append(" or ");
        }
        sql.append(" internal_id = ? ");
        gcDbAccess.addBindVar(id);
        isFirst = false;
      }
      
      int count = gcDbAccess.sql(sql.toString()).executeSql();
      
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().addUpdateCount(count);
      }
    }
  }
}

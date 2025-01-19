package edu.internet2.middleware.grouper.sqlCache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

@DisallowConcurrentExecution
public class SqlCacheHistoryFullSyncDaemon extends OtherJobBase {
  private static final Log LOG = GrouperUtil.getLog(SqlCacheHistoryFullSyncDaemon.class);
  
  private Map<Long, String> fieldInternalIdToPITId;
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
    
    // cache some data
    Set<Field> fields = FieldFinder.findAll();
    Set<PITField> pitFields = GrouperDAOFactory.getFactory().getPITField().findBySourceIdsActive(fields.stream().map(Field::getId).toList());
    fieldInternalIdToPITId = pitFields.stream()
        .collect(Collectors.toMap(
            PITField::getSourceInternalId,
            PITField::getId
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
    
    syncCacheDependency();
    
    deleteInvalidRows();
    
    syncMembershipHistory();
      
    if (theOtherJobInput != null) {
      theOtherJobInput.getHib3GrouperLoaderLog().setJobMessage("Job completed successfully");
      theOtherJobInput.getHib3GrouperLoaderLog().store();
    }
    
    return null;
  }
  
  private void syncMembershipHistory() {
    List<Object[]> sqlCacheGroupsData = new GcDbAccess().sql("select gscg.internal_id, gscg.group_internal_id, gscg.field_internal_id from grouper_sql_cache_group gscg, grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscg.internal_id=gscd.owner_internal_id and gscd.dep_type_internal_id=gscdt.internal_id and gscdt.dependency_category='mshipHistory'").selectList(Object[].class);
    for (Object[] sqlCacheGroupData : sqlCacheGroupsData) {
      long sqlCacheGroupInternalId = GrouperUtil.longObjectValue(sqlCacheGroupData[0], false);
      long ownerInternalId = GrouperUtil.longObjectValue(sqlCacheGroupData[1], false);
      long fieldInternalId = GrouperUtil.longObjectValue(sqlCacheGroupData[2], false);
      Field field = fieldInternalIdToField.get(fieldInternalId);
      String pitFieldId = fieldInternalIdToPITId.get(field.getInternalId());

      String pitOwnerId = null;
      
      if (field.isGroupAccessField() || field.getName().equals("members")) {
        pitOwnerId = groupInternalIdToPITId.get(ownerInternalId);
      } else if (field.isStemListField()) {
        pitOwnerId = stemIdIndexToPITId.get(ownerInternalId);
      } else if (field.isAttributeDefListField()) {
        pitOwnerId = attributeDefIdIndexToPITId.get(ownerInternalId);
      }
      
      if (pitOwnerId == null) {
        continue;
      }
      
      Hib3GrouperLoaderLog hib3GrouperLoaderLog = null;
      
      if (theOtherJobInput != null) {
        hib3GrouperLoaderLog = theOtherJobInput.getHib3GrouperLoaderLog();
      }
      
      syncMembershipHistoryIndividual(sqlCacheGroupInternalId, ownerInternalId, pitOwnerId, field, pitFieldId, pitIdToMemberInternalId, hib3GrouperLoaderLog);
      
      GrouperDaemonUtils.stopProcessingIfJobPaused();
    }
  }
  
  public static void syncMembershipHistory(SqlCacheGroup sqlCacheGroup, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    Field field = FieldFinder.findByInternalId(sqlCacheGroup.getFieldInternalId(), true);
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(field.getId(), false);
    if (pitField == null) {
      // unexpected, just ignore
      return;
    }
    
    String pitOwnerId = null;
    
    if (field.isGroupAccessField() || field.getName().equals("members")) {
      PITGroup pitGroup = GrouperDAOFactory.getFactory().getPITGroup().findBySourceInternalIdActive(sqlCacheGroup.getGroupInternalId(), false);
      if (pitGroup != null) {
        pitOwnerId = pitGroup.getId();
      }
    } else if (field.isStemListField()) {
      PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdIndexActive(sqlCacheGroup.getGroupInternalId(), false);
      if (pitStem != null) {
        pitOwnerId = pitStem.getId();
      }
    } else if (field.isAttributeDefListField()) {
      PITAttributeDef pitAttributeDef = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdIndexActive(sqlCacheGroup.getGroupInternalId(), false);
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
    
    SqlCacheHistoryFullSyncDaemon.syncMembershipHistoryIndividual(sqlCacheGroup.getInternalId(), sqlCacheGroup.getGroupInternalId(), pitOwnerId, field, pitField.getId(), pitIdToMemberInternalId, hib3GrouperLoaderLog);
  }
  
  public static void syncMembershipHistoryIndividual(long sqlCacheGroupInternalId, long ownerInternalId, String pitOwnerId, Field field, String pitFieldId, Map<String, Long> pitIdToMemberInternalId, Hib3GrouperLoaderLog hib3GrouperLoaderLog) {
    // we exclude retrieving pit rows that ended more than 2 years ago, but that might mean that start times saved in the cache history table might not take into account these old memberships when there's overlap.  Assuming for now that it doesn't matter since it's out of range for what we care about, but if we do, then they would need to be pulled in and filtered out later.
    long twoYearsAgoMicros = (System.currentTimeMillis() - 2*365*24*60*60*1000L) * 1000L;
    
    // query cache membership history
    GcDbAccess gcDbAccess = new GcDbAccess();
    List<Object[]> cacheMemberships = gcDbAccess.sql("select member_internal_id, start_time, end_time from grouper_sql_cache_mship_hst where sql_cache_group_internal_id = ?").addBindVar(sqlCacheGroupInternalId).selectList(Object[].class);
    Set<MultiKey> cacheMembershipMultiKeys = new HashSet<MultiKey>();
    for (Object[] cacheMembership : cacheMemberships) {
      long memberInternalId = GrouperUtil.longObjectValue(cacheMembership[0], false);
      long startTimeMicros = GrouperUtil.longObjectValue(cacheMembership[1], false);
      long endTimeMicros = GrouperUtil.longObjectValue(cacheMembership[2], false);
      
      cacheMembershipMultiKeys.add(new MultiKey(memberInternalId, startTimeMicros, endTimeMicros));
    }
    
    // query pit memberships
    gcDbAccess = new GcDbAccess();
    StringBuilder sqlQueryPITMemberships = new StringBuilder("select gpm1.member_id, gpgs1.start_time, gpm1.start_time, gpgs1.end_time, gpm1.end_time from grouper_pit_group_set gpgs1, grouper_pit_memberships gpm1 where gpm1.owner_id = gpgs1.member_id and gpm1.field_id = gpgs1.member_field_id and gpgs1.owner_id=? and gpgs1.field_id=? and (gpgs1.end_time is null or gpgs1.end_time > ?) and (gpm1.end_time is null or gpm1.end_time > ?)");
    
    gcDbAccess.addBindVar(pitOwnerId);      
    gcDbAccess.addBindVar(pitFieldId);
    gcDbAccess.addBindVar(twoYearsAgoMicros);
    gcDbAccess.addBindVar(twoYearsAgoMicros);
          
    List<Object[]> pitMemberships = gcDbAccess.sql(sqlQueryPITMemberships.toString()).selectList(Object[].class);
    
    // sort based on end time desc
    pitMemberships.sort(new Comparator<Object[]>() {
      @Override
      public int compare(Object[] o1, Object[] o2) {
        Long o1GroupSetEndTimeMicros = GrouperUtil.longObjectValue(o1[3], true);
        Long olMembershipEndTimeMicros = GrouperUtil.longObjectValue(o1[4], true);
        Long o2GroupSetEndTimeMicros = GrouperUtil.longObjectValue(o2[3], true);
        Long o2MembershipEndTimeMicros = GrouperUtil.longObjectValue(o2[4], true);
        
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
    
    Map<Long, Map<Long, Long>> memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros = new HashMap<>();
    Map<Long, Long> memberInternalIdToLastInsertedEndTimeMicros = new HashMap<>();
    
    for (Object[] pitMembership : pitMemberships) {
      String pitMemberId = (String)pitMembership[0];
      
      long groupSetStartTimeMicros = GrouperUtil.longObjectValue(pitMembership[1], false);
      long membershipStartTimeMicros = GrouperUtil.longObjectValue(pitMembership[2], false);
      long startTimeMicros = Math.max(groupSetStartTimeMicros, membershipStartTimeMicros);
      
      Long groupSetEndTimeMicros = GrouperUtil.longObjectValue(pitMembership[3], true);
      Long membershipEndTimeMicros = GrouperUtil.longObjectValue(pitMembership[4], true);
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
      
      Long memberInternalId = pitIdToMemberInternalId.get(pitMemberId);
      
      if (memberInternalId == null) {
        continue;
      }
      
      if (!memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros.containsKey(memberInternalId)) {
        // first time we're seeing this member.  add the flattened membership
        Long endTimeMicrosAdjusted = endTimeMicros == null ? -1 : endTimeMicros;

        memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros.put(memberInternalId, new LinkedHashMap<Long, Long>());
        memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros.get(memberInternalId).put(endTimeMicrosAdjusted, startTimeMicros);
        
        memberInternalIdToLastInsertedEndTimeMicros.put(memberInternalId, endTimeMicrosAdjusted);
      } else {
        Long lastInsertedEndTimeMicros = memberInternalIdToLastInsertedEndTimeMicros.get(memberInternalId);
        Long lastInsertedStartTimeMicros = memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros.get(memberInternalId).get(lastInsertedEndTimeMicros);
        
        if (endTimeMicros == null || endTimeMicros >= lastInsertedStartTimeMicros) {
          // check if there's an overlap that causes the start time for the flattened membership to decrease
          if (lastInsertedStartTimeMicros > startTimeMicros) {
            memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros.get(memberInternalId).put(lastInsertedEndTimeMicros, startTimeMicros);
          }
        } else {
          // looks like a new flattened membership
          memberInternalIdToLastInsertedEndTimeMicros.put(memberInternalId, endTimeMicros);
          memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros.get(memberInternalId).put(endTimeMicros, startTimeMicros);
        }
      }
    }
    
    memberInternalIdToLastInsertedEndTimeMicros = null;
    
    Set<MultiKey> pitMembershipMultiKeys = new HashSet<MultiKey>();
    for (long memberInternalId : memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros.keySet()) {
      for (long endTimeMicros : memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros.get(memberInternalId).keySet()) {
        if (endTimeMicros != -1) {
          long startTimeMicros = memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros.get(memberInternalId).get(endTimeMicros);
          pitMembershipMultiKeys.add(new MultiKey(memberInternalId, startTimeMicros, endTimeMicros));
        }
      }
    }

    memberInternalIdToEndTimeMicrosToEarliestStartTimeMicros = null;
    
    // now compare
    List<SqlCacheMembershipHst> sqlCacheMembershipHstsToInsert = new ArrayList<>();
    List<List<Object>> bindVarsSqlCacheMshipHstDeletes = new ArrayList<>();

    for (MultiKey pitMembershipMultiKey : pitMembershipMultiKeys) {
      if (!cacheMembershipMultiKeys.contains(pitMembershipMultiKey)) {
        SqlCacheMembershipHst sqlCacheMembershipHst = new SqlCacheMembershipHst();
        sqlCacheMembershipHst.setSqlCacheGroupInternalId(sqlCacheGroupInternalId);
        sqlCacheMembershipHst.setMemberInternalId((Long)pitMembershipMultiKey.getKey(0));
        sqlCacheMembershipHst.setStartTime((Long)pitMembershipMultiKey.getKey(1));
        sqlCacheMembershipHst.setEndTime((Long)pitMembershipMultiKey.getKey(2));
        sqlCacheMembershipHstsToInsert.add(sqlCacheMembershipHst);
      }
    }
    
    for (MultiKey cacheMembershipMultiKey : cacheMembershipMultiKeys) {
      if (!pitMembershipMultiKeys.contains(cacheMembershipMultiKey)) {
        // the primary key order - member_internal_id, sql_cache_group_internal_id, start_time
        bindVarsSqlCacheMshipHstDeletes.add(GrouperUtil.toListObject((Long)cacheMembershipMultiKey.getKey(0), sqlCacheGroupInternalId, (Long)cacheMembershipMultiKey.getKey(1)));
      }
    }
    
    // store
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);

    if (bindVarsSqlCacheMshipHstDeletes.size() > 0) {
      new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst where member_internal_id = ? and sql_cache_group_internal_id = ? and start_time = ?").batchSize(batchSize).batchBindVars(bindVarsSqlCacheMshipHstDeletes).executeBatchSql();
    
      if (hib3GrouperLoaderLog != null) {
        hib3GrouperLoaderLog.addDeleteCount(bindVarsSqlCacheMshipHstDeletes.size());
      }
    }
    
    int numberOfInserts = SqlCacheMembershipHstDao.store(sqlCacheMembershipHstsToInsert, null, true, true, true);
    
    if (hib3GrouperLoaderLog != null) {
      hib3GrouperLoaderLog.addInsertCount(numberOfInserts);
    }
  }
  
  private void deleteInvalidRows() {
    // delete memberships for groups that aren't being cached in history
    int count = new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst gscmh where not exists (select 1 from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscd.owner_internal_id = gscmh.sql_cache_group_internal_id and gscd.dep_type_internal_id=gscdt.internal_id and gscdt.dependency_category='mshipHistory')").executeSql();
    if (theOtherJobInput != null) {
      theOtherJobInput.getHib3GrouperLoaderLog().addDeleteCount(count);
    }
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();
      
    // delete invalid member ids
    count = new GcDbAccess().sql("delete from grouper_sql_cache_mship_hst gscm where not exists (select 1 from grouper_pit_members gpm where gpm.source_internal_id = gscm.member_internal_id and gpm.active = 'T')").executeSql();
    if (theOtherJobInput != null) {
      theOtherJobInput.getHib3GrouperLoaderLog().addDeleteCount(count);
    }
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();
  }
  
  private void syncCacheDependency() {

    // query cache data
    SqlCacheDependencyType sqlCacheDependencyType = SqlCacheDependencyTypeDao.retrieveByDependencyCategoryAndName("mshipHistory", "mshipHistory_viaAttribute");
    List<SqlCacheDependency> sqlCacheDependencies = SqlCacheDependencyDao.retrieveByDependencyTypeInternalId(sqlCacheDependencyType.getInternalId());
    Map<Long, SqlCacheDependency> sqlCacheGroupInternalIdToDependencyMap = new HashMap<>();
    for (SqlCacheDependency sqlCacheDependency : sqlCacheDependencies) {
      sqlCacheGroupInternalIdToDependencyMap.put(sqlCacheDependency.getOwnerInternalId(), sqlCacheDependency);
    }
    
    // query assignments
    AttributeDef attributeDef = AttributeDefFinder.findByName(SqlCacheGroup.sqlCacheableHistoryDefName(), true);
    
    List<Object[]> groupInternalIdAndAttributeNames = new GcDbAccess().sql("select gg.internal_id, gadn.name from grouper_attribute_assign gaa, grouper_attribute_def_name gadn, grouper_groups gg where gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id=? and gaa.enabled='T' and gg.id=gaa.owner_group_id").addBindVar(attributeDef.getId()).selectList(Object[].class);
    List<Object[]> stemIdIndexAndAttributeNames = new GcDbAccess().sql("select gs.id_index, gadn.name from grouper_attribute_assign gaa, grouper_attribute_def_name gadn, grouper_stems gs where gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id=? and gaa.enabled='T' and gs.id=gaa.owner_stem_id").addBindVar(attributeDef.getId()).selectList(Object[].class);
    List<Object[]> attributeDefIdIndexAndAttributeNames = new GcDbAccess().sql("select gad.id_index, gadn.name from grouper_attribute_assign gaa, grouper_attribute_def_name gadn, grouper_attribute_def gad where gaa.attribute_def_name_id = gadn.id and gadn.attribute_def_id=? and gaa.enabled='T' and gad.id=gaa.owner_attribute_def_id").addBindVar(attributeDef.getId()).selectList(Object[].class);
    
    Set<MultiKey> ownerInternalIdAndFieldInternalIdPerAssignments = new HashSet<MultiKey>();
    
    for (Object[] groupInternalIdAndAttributeName : groupInternalIdAndAttributeNames) {
      ownerInternalIdAndFieldInternalIdPerAssignments.add(new MultiKey(groupInternalIdAndAttributeName[0], SqlCacheGroup.getSqlCacheHistoryAttributeNamesToFields().get(groupInternalIdAndAttributeName[1]).getInternalId()));
    }
    
    for (Object[] stemIdIndexAndAttributeName : stemIdIndexAndAttributeNames) {
      ownerInternalIdAndFieldInternalIdPerAssignments.add(new MultiKey(stemIdIndexAndAttributeName[0], SqlCacheGroup.getSqlCacheHistoryAttributeNamesToFields().get(stemIdIndexAndAttributeName[1]).getInternalId()));
    }
    
    for (Object[] attributeDefIdIndexAndAttributeName : attributeDefIdIndexAndAttributeNames) {
      ownerInternalIdAndFieldInternalIdPerAssignments.add(new MultiKey(attributeDefIdIndexAndAttributeName[0], SqlCacheGroup.getSqlCacheHistoryAttributeNamesToFields().get(attributeDefIdIndexAndAttributeName[1]).getInternalId()));
    }
    
    Set<Long> sqlCacheGroupInternalIdsPerAssignments = new HashSet<Long>();
        
    Map<MultiKey, SqlCacheGroup> ownerInternalIdAndFieldInternalIdToSqlCacheGroup = SqlCacheGroupDao.retrieveByGroupInternalIdsFieldInternalIds(ownerInternalIdAndFieldInternalIdPerAssignments, null);
    for (MultiKey key : ownerInternalIdAndFieldInternalIdToSqlCacheGroup.keySet()) {
      SqlCacheGroup sqlCacheGroup = ownerInternalIdAndFieldInternalIdToSqlCacheGroup.get(key);
      
      if (sqlCacheGroup != null) {
        sqlCacheGroupInternalIdsPerAssignments.add(sqlCacheGroup.getInternalId());
      }
    }
    
    // now compare and update
    List<List<Object>> bindVarsForDelete = new ArrayList<>();
    Set<SqlCacheDependency> sqlCacheDependenciesForInsert = new HashSet<>();

    for (Long sqlCacheGroupInternalId : sqlCacheGroupInternalIdToDependencyMap.keySet()) {
      if (!sqlCacheGroupInternalIdsPerAssignments.contains(sqlCacheGroupInternalId)) {
        bindVarsForDelete.add(GrouperUtil.toListObject(sqlCacheGroupInternalIdToDependencyMap.get(sqlCacheGroupInternalId).getInternalId()));
      }
    }
    
    for (Long sqlCacheGroupInternalId : sqlCacheGroupInternalIdsPerAssignments) {
      if (!sqlCacheGroupInternalIdToDependencyMap.containsKey(sqlCacheGroupInternalId)) {
        SqlCacheDependency sqlCacheDependency = new SqlCacheDependency();
        sqlCacheDependency.setDependencyTypeInternalId(sqlCacheDependencyType.getInternalId());
        sqlCacheDependency.setOwnerInternalId(sqlCacheGroupInternalId);
        sqlCacheDependency.setDependentInternalId(sqlCacheGroupInternalId);
        sqlCacheDependenciesForInsert.add(sqlCacheDependency);
      }
    }
    
    if (bindVarsForDelete.size() > 0) {
      int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
      new GcDbAccess().batchSize(batchSize).sql("delete from grouper_sql_cache_dependency where internal_id = ?").batchBindVars(bindVarsForDelete).executeBatchSql();
    }
    
    if (theOtherJobInput != null) {
      theOtherJobInput.getHib3GrouperLoaderLog().addDeleteCount(bindVarsForDelete.size());
    }
    
    if (sqlCacheDependenciesForInsert.size() > 0) {
      SqlCacheDependencyDao.store(sqlCacheDependenciesForInsert, null, true, true, true);
      
      if (theOtherJobInput != null) {
        theOtherJobInput.getHib3GrouperLoaderLog().addInsertCount(sqlCacheDependenciesForInsert.size());
      }
    }
    
    GrouperDaemonUtils.stopProcessingIfJobPaused();
  }
}

package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * dao for sql cache memberships
 * @author mchyzer
 *
 */
public class SqlCacheMembershipDao {


  public SqlCacheMembershipDao() {
  }

  /**
   * 
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(SqlCacheMembership sqlCacheMembership) {
    sqlCacheMembership.storePrepare();
    boolean changed = new GcDbAccess().storeToDatabase(sqlCacheMembership);
    return changed;
  }

  /**
   * select grouper sync by id
   * @param theConnectionName
   * @param id
   * @return the sync
   */
  public static SqlCacheMembership retrieveByInternalId(Long id) {
    SqlCacheMembership sqlCacheMembership = new GcDbAccess()
        .sql("select * from grouper_sql_cache_mship where internal_id = ?").addBindVar(id).select(SqlCacheMembership.class);
    return sqlCacheMembership;
  }
  

  /**
   * 
   * @param sqlCacheMembership
   */
  public static void delete(SqlCacheMembership sqlCacheMembership) {
    new GcDbAccess().deleteFromDatabase(sqlCacheMembership);
  }

  /**
   * things to add to sql cache memberships.  5 fields in multikey: 
   * groupName, fieldName, sourceId, subjectId, millisSince1970whenMembershipStarted (Long)
   * @param groupNameFieldNameSourceIdSubjectIdStartedMillis
   * @return number of changes
   */
  public static int insertSqlCacheMembershipsIfCacheable(Collection<MultiKey> groupNameFieldNameSourceIdSubjectIdStartedMillis) {
    
    if (GrouperUtil.length(groupNameFieldNameSourceIdSubjectIdStartedMillis) == 0) {
      return 0;
    }
    
    long currentTimeMillis = System.currentTimeMillis();
    
    Set<MultiKey> groupNameFieldNames = new HashSet<>();
    
    Map<MultiKey, MultiKey> groupNameFieldNameSourceIdSubjectIdStartedMilliToGroupNameFieldName = new HashMap<>();
    
    for (MultiKey groupNameFieldNameSourceIdSubjectIdStartedMilli : groupNameFieldNameSourceIdSubjectIdStartedMillis) {
      String groupName = (String)groupNameFieldNameSourceIdSubjectIdStartedMilli.getKey(0);
      String fieldName = (String)groupNameFieldNameSourceIdSubjectIdStartedMilli.getKey(1);
      
      MultiKey groupNameFieldName = new MultiKey(groupName, fieldName);
      groupNameFieldNames.add(groupNameFieldName);
      groupNameFieldNameSourceIdSubjectIdStartedMilliToGroupNameFieldName.put(groupNameFieldNameSourceIdSubjectIdStartedMilli, groupNameFieldName);
      
    }
    
    // lets see which of these are cacheable groups
    Map<MultiKey, SqlCacheGroup> groupNameFieldNameToSqlCacheGroup = SqlCacheGroupDao.retrieveByGroupNamesFieldNames(groupNameFieldNames);
    
    List<MultiKey> groupNameFieldNameSourceIdSubjectIdStartedMillisList = new ArrayList<>(groupNameFieldNameSourceIdSubjectIdStartedMillis);
    
    Iterator<MultiKey> iterator = groupNameFieldNameSourceIdSubjectIdStartedMillisList.iterator();
    
    // filter out uncacheable
    while (iterator.hasNext()) {
      MultiKey groupNameFieldNameSourceIdSubjectIdStartedMilli = iterator.next();
      MultiKey groupNameFieldName = groupNameFieldNameSourceIdSubjectIdStartedMilliToGroupNameFieldName.get(groupNameFieldNameSourceIdSubjectIdStartedMilli);
      SqlCacheGroup sqlCacheGroup = groupNameFieldNameToSqlCacheGroup.get(groupNameFieldName);
      
      if (sqlCacheGroup == null || (sqlCacheGroup.getDisabledOn() != null && sqlCacheGroup.getDisabledOn().getTime() < currentTimeMillis)
          || (sqlCacheGroup != null && sqlCacheGroup.getEnabledOn() != null && sqlCacheGroup.getEnabledOn().getTime() > currentTimeMillis)) {
        iterator.remove();
      }

    }

    Map<MultiKey, MultiKey> groupNameFieldNameSourceIdSubjectIdStartedMilliToSourceIdSubjectId = new HashMap<>();
    Set<MultiKey> sourceIdSubjectIds = new HashSet<>();
    
    for (MultiKey groupNameFieldNameSourceIdSubjectIdStartedMilli : groupNameFieldNameSourceIdSubjectIdStartedMillisList) {
      String sourceId = (String)groupNameFieldNameSourceIdSubjectIdStartedMilli.getKey(2);
      String subjectId = (String)groupNameFieldNameSourceIdSubjectIdStartedMilli.getKey(3);
      
      MultiKey sourceIdSubjectId = new MultiKey(sourceId, subjectId);
      sourceIdSubjectIds.add(sourceIdSubjectId);
      groupNameFieldNameSourceIdSubjectIdStartedMilliToSourceIdSubjectId.put(groupNameFieldNameSourceIdSubjectIdStartedMilli, sourceIdSubjectId);
      
    }

    Map<MultiKey, Long> sourceIdSubjectIdToInternalId = MemberFinder.findInternalIdsByNames(sourceIdSubjectIds);
    
    List<SqlCacheMembership> sqlCacheMembershipsToInsert = new ArrayList<>();

    for (MultiKey groupNameFieldNameSourceIdSubjectIdStartedMilli : groupNameFieldNameSourceIdSubjectIdStartedMillisList) {

      MultiKey sourceIdSubjectId = groupNameFieldNameSourceIdSubjectIdStartedMilliToSourceIdSubjectId.get(groupNameFieldNameSourceIdSubjectIdStartedMilli);
      
      if (sourceIdSubjectId == null) {
        continue;
      }

      Long memberInternalId = sourceIdSubjectIdToInternalId.get(sourceIdSubjectId);
      
      if (memberInternalId == null) {
        continue;
      }
      
      MultiKey groupNameFieldName = groupNameFieldNameSourceIdSubjectIdStartedMilliToGroupNameFieldName.get(groupNameFieldNameSourceIdSubjectIdStartedMilli);
      
      if (groupNameFieldName == null) {
        continue;
      }
      
      SqlCacheGroup sqlCacheGroup = groupNameFieldNameToSqlCacheGroup.get(groupNameFieldName);
      
      if (sqlCacheGroup == null) {
        continue;
      }
      
      SqlCacheMembership sqlCacheMembership = new SqlCacheMembership();
      Long membershipAddedLong = (Long)groupNameFieldNameSourceIdSubjectIdStartedMilli.getKey(4);
      Timestamp membershipAdded = new Timestamp(membershipAddedLong);
      sqlCacheMembership.setFlattenedAddTimestamp(membershipAdded);
      sqlCacheMembership.setMemberInternalId(memberInternalId);
      sqlCacheMembership.setSqlCacheGroupInternalId(sqlCacheGroup.getInternalId());
      sqlCacheMembershipsToInsert.add(sqlCacheMembership);
      
    }   
    
    return SqlCacheMembershipDao.store(sqlCacheMembershipsToInsert);
  }
  
  /**
   * note the sql cache group record needs to be already inserted
   * @param groupNamesFieldNames
   * @return the number of inserts
   */
  public static int insertSqlCacheMembershipsAsNeededFromSource(Collection<MultiKey> groupNamesFieldNames) {
    
    Set<MultiKey> sqlGroupInternalIdsMemberInternalIds = new HashSet<MultiKey>();
    
    for (MultiKey groupNameFieldName : groupNamesFieldNames) {
      GcDbAccess gcDbAccess = new GcDbAccess();
      StringBuilder sql = new StringBuilder("select gscg.internal_id, gm.internal_id "
          + " from grouper_memberships_lw_v gmlv, grouper_groups gg, grouper_fields gf, grouper_members gm, grouper_sql_cache_group gscg "
          + " where group_name = ? and list_name = ?  "
          + " and gg.id = gmlv.group_id and gm.id = gmlv.member_id and gf.name = gmlv.list_name "
          + " and gscg.group_internal_id = gg.internal_id and gscg.field_internal_id = gf.internal_id " 
          + " and not exists (select 1 from grouper_sql_cache_mship gscm where gscm.sql_cache_group_internal_id = gscg.internal_id "
          + " and gscm.member_internal_id = gm.internal_id) ");
      gcDbAccess.sql(sql.toString());
      gcDbAccess.addBindVar((String)groupNameFieldName.getKey(0));
      gcDbAccess.addBindVar((String)groupNameFieldName.getKey(1));
      List<Object[]> sqlGroupInternalIdsMemberInternalIdsList = gcDbAccess.selectList(Object[].class);
      for (Object[] sqlGroupInternalIdMemberInternalId : sqlGroupInternalIdsMemberInternalIdsList) {
        sqlGroupInternalIdsMemberInternalIds.add(new MultiKey(
            GrouperUtil.longValue(sqlGroupInternalIdMemberInternalId[0]), GrouperUtil.longValue(sqlGroupInternalIdMemberInternalId[1])));
      }
    }
    
    List<SqlCacheMembership> sqlCacheMembershipsToInsert = new ArrayList<>();

    // TODO get the real time
    Long membershipAddedLong = System.currentTimeMillis();
    for (MultiKey sqlGroupInternalIdMemberInternalId :  sqlGroupInternalIdsMemberInternalIds) {
      SqlCacheMembership sqlCacheMembership = new SqlCacheMembership();
      Timestamp membershipAdded = new Timestamp(membershipAddedLong);
      sqlCacheMembership.setFlattenedAddTimestamp(membershipAdded);
      sqlCacheMembership.setSqlCacheGroupInternalId((Long)sqlGroupInternalIdMemberInternalId.getKey(0));
      sqlCacheMembership.setMemberInternalId((Long)sqlGroupInternalIdMemberInternalId.getKey(1));
      sqlCacheMembershipsToInsert.add(sqlCacheMembership);
      
    }
    
    return SqlCacheMembershipDao.store(sqlCacheMembershipsToInsert);
    
  }
  
  /**
   * things to delete to sql cache memberships.  4 fields in multikey: 
   * groupName, fieldName, sourceId, subjectId
   * @param groupNameFieldNameSourceIdSubjectIdStartedMillis
   * @return number of changes
   */
  public static int deleteSqlCacheMembershipsIfCacheable(Collection<MultiKey> groupNameFieldNameSourceIdSubjectIds) {
    
    if (GrouperUtil.length(groupNameFieldNameSourceIdSubjectIds) == 0) {
      return 0;
    }
    
    long currentTimeMillis = System.currentTimeMillis();

    Set<MultiKey> groupNameFieldNames = new HashSet<>();
    
    Map<MultiKey, MultiKey> groupNameFieldNameSourceIdSubjectIdToGroupNameFieldName = new HashMap<>();
    
    for (MultiKey groupNameFieldNameSourceIdSubjectId : groupNameFieldNameSourceIdSubjectIds) {
      String groupName = (String)groupNameFieldNameSourceIdSubjectId.getKey(0);
      String fieldName = (String)groupNameFieldNameSourceIdSubjectId.getKey(1);
      
      MultiKey groupNameFieldName = new MultiKey(groupName, fieldName);
      groupNameFieldNames.add(groupNameFieldName);
      groupNameFieldNameSourceIdSubjectIdToGroupNameFieldName.put(groupNameFieldNameSourceIdSubjectId, groupNameFieldName);
      
    }
    
    // lets see which of these are cacheable groups
    Map<MultiKey, SqlCacheGroup> groupNameFieldNameToSqlCacheGroup = SqlCacheGroupDao.retrieveByGroupNamesFieldNames(groupNameFieldNames);
    
    List<MultiKey> groupNameFieldNameSourceIdSubjectIdList = new ArrayList<>(groupNameFieldNameSourceIdSubjectIds);
    
    Iterator<MultiKey> iterator = groupNameFieldNameSourceIdSubjectIdList.iterator();
    
    // filter out uncacheable
    while (iterator.hasNext()) {
      MultiKey groupNameFieldNameSourceIdSubjectId = iterator.next();
      MultiKey groupNameFieldName = groupNameFieldNameSourceIdSubjectIdToGroupNameFieldName.get(groupNameFieldNameSourceIdSubjectId);
      SqlCacheGroup sqlCacheGroup = groupNameFieldNameToSqlCacheGroup.get(groupNameFieldName);
      
      if (sqlCacheGroup == null || (sqlCacheGroup.getDisabledOn() != null && sqlCacheGroup.getDisabledOn().getTime() < currentTimeMillis)
          || (sqlCacheGroup != null && sqlCacheGroup.getEnabledOn() != null && sqlCacheGroup.getEnabledOn().getTime() > currentTimeMillis)) {
        iterator.remove();
      }

    }

    Map<MultiKey, MultiKey> groupNameFieldNameSourceIdSubjectIdStartedMilliToSourceIdSubjectId = new HashMap<>();
    Set<MultiKey> sourceIdSubjectIds = new HashSet<>();
    
    for (MultiKey groupNameFieldNameSourceIdSubjectId : groupNameFieldNameSourceIdSubjectIds) {
      String sourceId = (String)groupNameFieldNameSourceIdSubjectId.getKey(2);
      String subjectId = (String)groupNameFieldNameSourceIdSubjectId.getKey(3);
      
      MultiKey sourceIdSubjectId = new MultiKey(sourceId, subjectId);
      sourceIdSubjectIds.add(sourceIdSubjectId);
      groupNameFieldNameSourceIdSubjectIdStartedMilliToSourceIdSubjectId.put(groupNameFieldNameSourceIdSubjectId, sourceIdSubjectId);
      
    }

    Map<MultiKey, Long> sourceIdSubjectIdToInternalId = MemberFinder.findInternalIdsByNames(sourceIdSubjectIds);
    
    List<SqlCacheMembership> sqlCacheMembershipsToInsert = new ArrayList<>();

    List<List<Object>> bindVarsAll = new ArrayList<>();
    
    for (MultiKey groupNameFieldNameSourceIdSubjectIdStartedMilli : groupNameFieldNameSourceIdSubjectIdList) {

      MultiKey sourceIdSubjectId = groupNameFieldNameSourceIdSubjectIdStartedMilliToSourceIdSubjectId.get(groupNameFieldNameSourceIdSubjectIdStartedMilli);
      
      if (sourceIdSubjectId == null) {
        continue;
      }

      Long memberInternalId = sourceIdSubjectIdToInternalId.get(sourceIdSubjectId);
      
      if (memberInternalId == null) {
        continue;
      }
      
      MultiKey groupNameFieldName = groupNameFieldNameSourceIdSubjectIdToGroupNameFieldName.get(groupNameFieldNameSourceIdSubjectIdStartedMilli);
      
      if (groupNameFieldName == null) {
        continue;
      }
      
      SqlCacheGroup sqlCacheGroup = groupNameFieldNameToSqlCacheGroup.get(groupNameFieldName);
      
      if (sqlCacheGroup == null) {
        continue;
      }
      
      bindVarsAll.add(GrouperUtil.toListObject(sqlCacheGroup.getInternalId(), memberInternalId));
      
      
    }   
    
    int[] rowsChanged = new GcDbAccess().sql("delete from grouper_sql_cache_mship where sql_cache_group_internal_id = ? and member_internal_id = ?")
      .batchBindVars(bindVarsAll).executeBatchSql();
    int result = 0;
    for (int rowChanged : rowsChanged) {
      result += rowChanged;
    }
    return result;
  }
  
  /**
   * select caches by group names and field names and source ids and subject ids
   * @param groupNamesFieldNamesSourceIdsSubjectIds
   * @return the caches if they exist by groupName and fieldName and source ids and subject ids
   */
  public static Map<MultiKey, SqlCacheMembership> retrieveByGroupNamesFieldNamesSourceIdsSubjectIds(Collection<MultiKey> groupNamesFieldNamesSourceIdsSubjectIds) {
    
    Map<MultiKey, SqlCacheMembership> result = new HashMap<>();

    if (GrouperUtil.length(groupNamesFieldNamesSourceIdsSubjectIds) == 0) {
      return result;
    }

    Set<MultiKey> groupNamesFieldNames = new HashSet<>();
    Set<MultiKey> sourceIdsSubjectIds = new HashSet<>();
    
    for (MultiKey groupNameFieldNameSourceIdSubjectId : groupNamesFieldNamesSourceIdsSubjectIds) {
      groupNamesFieldNames.add(new MultiKey(groupNameFieldNameSourceIdSubjectId.getKey(0), groupNameFieldNameSourceIdSubjectId.getKey(1)));
      sourceIdsSubjectIds.add(new MultiKey(groupNameFieldNameSourceIdSubjectId.getKey(2), groupNameFieldNameSourceIdSubjectId.getKey(3)));
    }
    
    // all fields and groups, note, some might not be there
    Map<MultiKey, Long> groupNameFieldNameToInternalId = SqlCacheGroupDao.retrieveByGroupNamesFieldNamesToInternalId(groupNamesFieldNames);

    Map<MultiKey, Long> sourceIdSubjectIdToInternalId = MemberFinder.findInternalIdsByNames(sourceIdsSubjectIds);

    Map<MultiKey, MultiKey> cacheGroupInternalIdMemberInternalIdToGroupNameFieldNameSourceIdSubjectId = new HashMap<>();

    List<MultiKey> sqlGroupInternalIdMemberInternalIdList = new ArrayList<MultiKey>();
    
    for (MultiKey groupNameFieldNameSourceIdSubjectId : groupNamesFieldNamesSourceIdsSubjectIds) {
      
      String groupName = (String)groupNameFieldNameSourceIdSubjectId.getKey(0);
      String fieldName = (String)groupNameFieldNameSourceIdSubjectId.getKey(1);
      String sourceId = (String)groupNameFieldNameSourceIdSubjectId.getKey(2);
      String subjectId = (String)groupNameFieldNameSourceIdSubjectId.getKey(3);

      Long cacheGroupInternalId = groupNameFieldNameToInternalId.get(new MultiKey(groupName, fieldName));
      Long memberInternalId = sourceIdSubjectIdToInternalId.get(new MultiKey(sourceId, subjectId));

      if (cacheGroupInternalId != null && memberInternalId != null) {
        MultiKey cacheGroupInternalIdMemberInternalId = new MultiKey(cacheGroupInternalId, memberInternalId);
        sqlGroupInternalIdMemberInternalIdList.add(cacheGroupInternalIdMemberInternalId);
        cacheGroupInternalIdMemberInternalIdToGroupNameFieldNameSourceIdSubjectId.put(cacheGroupInternalIdMemberInternalId, 
            new MultiKey(groupName, fieldName, sourceId, subjectId));
      }
    }

    // now we have a list of group internal ids and field internal ids we can find
    Map<MultiKey, SqlCacheMembership> groupInternalIdFieldInternalIdToCacheMembership = retrieveByCacheGroupInternalIdsMemberInternalIds(sqlGroupInternalIdMemberInternalIdList);
    
    for (MultiKey groupInternalIdFieldInternalId : groupInternalIdFieldInternalIdToCacheMembership.keySet()) {
      SqlCacheMembership sqlCacheMembership  = groupInternalIdFieldInternalIdToCacheMembership.get(groupInternalIdFieldInternalId);
      MultiKey groupNameFieldNameSourceIdSubjectId = 
          cacheGroupInternalIdMemberInternalIdToGroupNameFieldNameSourceIdSubjectId.get(groupInternalIdFieldInternalId);
      result.put(groupNameFieldNameSourceIdSubjectId, sqlCacheMembership);
    }

    return result;
  }

  /**
   * 
   * @param connectionName
   * @return number of changes
   */
  public static int store(Collection<SqlCacheMembership> sqlCacheMemberships) {
    if (GrouperUtil.length(sqlCacheMemberships) == 0) {
      return 0;
    }
    for (SqlCacheMembership sqlCacheMembership : sqlCacheMemberships) {
      sqlCacheMembership.storePrepare();
    }
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    return new GcDbAccess().storeBatchToDatabase(sqlCacheMemberships, batchSize);
  }


  /**
   * select caches by cache group internal ids and member internal ids
   * @param cacheGroupInternalIdsMemberInternalIds
   * @return the caches if they exist 
   */
  public static Map<MultiKey, SqlCacheMembership> retrieveByCacheGroupInternalIdsMemberInternalIds(Collection<MultiKey> cacheGroupInternalIdsMemberInternalIds) {
    
    Map<MultiKey, SqlCacheMembership> result = new HashMap<>();

    if (GrouperUtil.length(cacheGroupInternalIdsMemberInternalIds) == 0) {
      return result;
    }

    List<MultiKey> cacheGroupInternalIdsMemberInternalIdsList = new ArrayList<>(cacheGroupInternalIdsMemberInternalIds);
    
    // two bind vars in each record to retrieve
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900) / 2;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(cacheGroupInternalIdsMemberInternalIdsList), batchSize, false);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<MultiKey> batchOfCacheGroupInternalIdMemberInternalIdList = GrouperClientUtils.batchList(cacheGroupInternalIdsMemberInternalIdsList, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("select * from grouper_sql_cache_mship where ");
      
      GcDbAccess gcDbAccess = new GcDbAccess();
      
      for (int i=0;i<batchOfCacheGroupInternalIdMemberInternalIdList.size();i++) {
        if (i>0) {
          sql.append(" or ");
        }
        sql.append(" ( cache_group_internal_id = ? and member_internal_id = ? ) ");
        MultiKey cacheGroupInternalIdMemberInternalId = batchOfCacheGroupInternalIdMemberInternalIdList.get(i);
        gcDbAccess.addBindVar(cacheGroupInternalIdMemberInternalId.getKey(0));
        gcDbAccess.addBindVar(cacheGroupInternalIdMemberInternalId.getKey(1));
      }
      
      List<SqlCacheMembership> sqlCacheMemberships = gcDbAccess.sql(sql.toString()).selectList(SqlCacheMembership.class);
      
      for (SqlCacheMembership sqlCacheMembership : GrouperClientUtils.nonNull(sqlCacheMemberships)) {
        result.put(new MultiKey(sqlCacheMembership.getSqlCacheGroupInternalId(), sqlCacheMembership.getMemberInternalId()), sqlCacheMembership);
      }
      
    }
    return result;
  }

  /**
   * retrieve cache group by group name field name or created
   * @param sqlCacheMemberships
   */
  public static void retrieveOrCreateBySqlMembershipCache(Collection<SqlCacheMembership> sqlCacheMemberships) {
    
    if (GrouperUtil.length(sqlCacheMemberships) == 0) {
      return;
    }
    Set<MultiKey> cacheGroupInternalIdsMemberInternalIds = new HashSet<>();
    
    for (SqlCacheMembership sqlCacheMembership : sqlCacheMemberships) {
      cacheGroupInternalIdsMemberInternalIds.add(new MultiKey(sqlCacheMembership.getSqlCacheGroupInternalId(), sqlCacheMembership.getMemberInternalId()));
    }
    
    Map<MultiKey, SqlCacheMembership> existingGroupInternalIdsFieldInternalIdsToCacheMemberships = retrieveByCacheGroupInternalIdsMemberInternalIds(cacheGroupInternalIdsMemberInternalIds);
    
    List<SqlCacheMembership> sqlCacheMembershipsToCreate = new ArrayList<SqlCacheMembership>();
    
    for (SqlCacheMembership sqlCacheMembership : sqlCacheMemberships) {
      SqlCacheMembership existingCacheMembership = existingGroupInternalIdsFieldInternalIdsToCacheMemberships.get(new MultiKey(sqlCacheMembership.getSqlCacheGroupInternalId(), sqlCacheMembership.getMemberInternalId()));
      if (existingCacheMembership == null) {
        sqlCacheMembershipsToCreate.add(sqlCacheMembership);
      }
    }

    if (sqlCacheMembershipsToCreate.size() == 0) {
      return;
    }

    // get ids in one fell swoop
    List<Long> ids = TableIndex.reserveIds(TableIndexType.sqlMembershipCache, sqlCacheMembershipsToCreate.size());

    for (int i=0; i < sqlCacheMembershipsToCreate.size(); i++) {
      SqlCacheMembership sqlCacheMembership = sqlCacheMembershipsToCreate.get(i);
      sqlCacheMembership.setTempInternalIdOnDeck(ids.get(i));
      sqlCacheMembership.storePrepare();

    }

    int defaultBatchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.batchSize", 1000);

    new GcDbAccess().storeBatchToDatabase(sqlCacheMembershipsToCreate, defaultBatchSize);
    
  }
  


}

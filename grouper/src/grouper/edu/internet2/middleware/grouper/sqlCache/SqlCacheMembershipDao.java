package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.MemberFinder;
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
  public static SqlCacheMembership retrieveByCacheGroupInternalIdAndMemberInternalId(Long cacheGroupInternalId, Long memberInternalId) {
    SqlCacheMembership sqlCacheMembership = new GcDbAccess()
        .sql("select * from grouper_sql_cache_mship where sql_cache_group_internal_id = ? and member_internal_id = ?").addBindVar(cacheGroupInternalId).addBindVar(memberInternalId).select(SqlCacheMembership.class);
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
   * ownerId, fieldName, sourceId, subjectId, microsSince1970whenMembershipStarted (Long)
   * @param ownerIdFieldNameSourceIdSubjectIdStartedMicros
   * @param connection optionally pass connection to use
   * @return number of cache membership inserts
   */
  public static int insertSqlCacheMembershipsIfCacheable(Collection<MultiKey> ownerIdFieldNameSourceIdSubjectIdStartedMicros, Connection connection) {
    
    if (GrouperUtil.length(ownerIdFieldNameSourceIdSubjectIdStartedMicros) == 0) {
      return 0;
    }
        
    Set<MultiKey> ownerIdFieldNames = new HashSet<>();
    
    Map<MultiKey, MultiKey> ownerIdFieldNameSourceIdSubjectIdStartedMicroToOwnerIdFieldName = new HashMap<>();
    
    for (MultiKey ownerIdFieldNameSourceIdSubjectIdStartedMicro : ownerIdFieldNameSourceIdSubjectIdStartedMicros) {
      String ownerId = (String)ownerIdFieldNameSourceIdSubjectIdStartedMicro.getKey(0);
      String fieldName = (String)ownerIdFieldNameSourceIdSubjectIdStartedMicro.getKey(1);
      
      MultiKey ownerIdFieldName = new MultiKey(ownerId, fieldName);
      ownerIdFieldNames.add(ownerIdFieldName);
      ownerIdFieldNameSourceIdSubjectIdStartedMicroToOwnerIdFieldName.put(ownerIdFieldNameSourceIdSubjectIdStartedMicro, ownerIdFieldName);
      
    }
    
    Map<MultiKey, SqlCacheGroup> ownerIdFieldNameToSqlCacheGroup = SqlCacheGroupDao.retrieveByOwnerIdsFieldNames(ownerIdFieldNames, connection);
    
    List<MultiKey> ownerIdFieldNameSourceIdSubjectIdStartedMicrosList = new ArrayList<>(ownerIdFieldNameSourceIdSubjectIdStartedMicros);
    
    Iterator<MultiKey> iterator = ownerIdFieldNameSourceIdSubjectIdStartedMicrosList.iterator();
    
    // filter out uncacheable
    while (iterator.hasNext()) {
      MultiKey ownerIdFieldNameSourceIdSubjectIdStartedMicro = iterator.next();
      MultiKey ownerIdFieldName = ownerIdFieldNameSourceIdSubjectIdStartedMicroToOwnerIdFieldName.get(ownerIdFieldNameSourceIdSubjectIdStartedMicro);
      SqlCacheGroup sqlCacheGroup = ownerIdFieldNameToSqlCacheGroup.get(ownerIdFieldName);
      
      if (sqlCacheGroup == null || sqlCacheGroup.getDisabledOn() != null) {
        iterator.remove();
      }
    }

    Map<MultiKey, MultiKey> ownerIdFieldNameSourceIdSubjectIdStartedMicroToSourceIdSubjectId = new HashMap<>();
    Set<MultiKey> sourceIdSubjectIds = new HashSet<>();
    
    for (MultiKey ownerIdFieldNameSourceIdSubjectIdStartedMicro : ownerIdFieldNameSourceIdSubjectIdStartedMicrosList) {
      String sourceId = (String)ownerIdFieldNameSourceIdSubjectIdStartedMicro.getKey(2);
      String subjectId = (String)ownerIdFieldNameSourceIdSubjectIdStartedMicro.getKey(3);
      
      MultiKey sourceIdSubjectId = new MultiKey(sourceId, subjectId);
      sourceIdSubjectIds.add(sourceIdSubjectId);
      ownerIdFieldNameSourceIdSubjectIdStartedMicroToSourceIdSubjectId.put(ownerIdFieldNameSourceIdSubjectIdStartedMicro, sourceIdSubjectId);
      
    }

    Map<MultiKey, Long> sourceIdSubjectIdToInternalId = MemberFinder.findInternalIdsByNames(sourceIdSubjectIds);
    
    List<SqlCacheMembership> sqlCacheMembershipsToInsert = new ArrayList<>();
    Set<SqlCacheGroup> sqlCacheGroupsToUpdate = new HashSet<>();

    for (MultiKey ownerIdFieldNameSourceIdSubjectIdStartedMicro : ownerIdFieldNameSourceIdSubjectIdStartedMicrosList) {

      MultiKey sourceIdSubjectId = ownerIdFieldNameSourceIdSubjectIdStartedMicroToSourceIdSubjectId.get(ownerIdFieldNameSourceIdSubjectIdStartedMicro);
      
      if (sourceIdSubjectId == null) {
        continue;
      }

      Long memberInternalId = sourceIdSubjectIdToInternalId.get(sourceIdSubjectId);
      
      if (memberInternalId == null) {
        continue;
      }
      
      MultiKey ownerIdFieldName = ownerIdFieldNameSourceIdSubjectIdStartedMicroToOwnerIdFieldName.get(ownerIdFieldNameSourceIdSubjectIdStartedMicro);
      
      if (ownerIdFieldName == null) {
        continue;
      }
      
      SqlCacheGroup sqlCacheGroup = ownerIdFieldNameToSqlCacheGroup.get(ownerIdFieldName);
      
      if (sqlCacheGroup == null) {
        continue;
      }
      
      SqlCacheMembership sqlCacheMembership = new SqlCacheMembership();
      Long membershipAddedLong = (Long)ownerIdFieldNameSourceIdSubjectIdStartedMicro.getKey(4);
      sqlCacheMembership.setFlattenedAddTimestamp(membershipAddedLong);
      sqlCacheMembership.setMemberInternalId(memberInternalId);
      sqlCacheMembership.setSqlCacheGroupInternalId(sqlCacheGroup.getInternalId());
      sqlCacheMembershipsToInsert.add(sqlCacheMembership);
      
      sqlCacheGroup.setMembershipSize(sqlCacheGroup.getMembershipSize() + 1);
      sqlCacheGroupsToUpdate.add(sqlCacheGroup);
    }   
    
    int numberOfChanges =  SqlCacheMembershipDao.store(sqlCacheMembershipsToInsert, connection, true, true, true);
    
    SqlCacheGroupDao.store(sqlCacheGroupsToUpdate, connection, false);
    
    return numberOfChanges;
  }

  
  /**
   * Things to delete from sql cache memberships.  Also possibly add to history if cachable.  5 fields in multikey: 
   * ownerId, fieldName, sourceId, subjectId, microsSince1970whenMembershipEnded (Long)
   * @param ownerIdFieldNameSourceIdSubjectIdStartedMillis
   * @param connection optionally pass connection to use
   * @return number of cache membership deletes
   */
  public static int deleteSqlCacheMembershipsIfCacheable(Collection<MultiKey> ownerIdFieldNameSourceIdSubjectIdsEndedMicros, Connection connection) {
    
    if (GrouperUtil.length(ownerIdFieldNameSourceIdSubjectIdsEndedMicros) == 0) {
      return 0;
    }
    
    Set<MultiKey> ownerIdFieldNames = new HashSet<>();
    
    Map<MultiKey, MultiKey> ownerIdFieldNameSourceIdSubjectIdEndedMicrosToOwnerIdFieldName = new HashMap<>();
    
    for (MultiKey ownerIdFieldNameSourceIdSubjectIdEndedMicros : ownerIdFieldNameSourceIdSubjectIdsEndedMicros) {
      String ownerId = (String)ownerIdFieldNameSourceIdSubjectIdEndedMicros.getKey(0);
      String fieldName = (String)ownerIdFieldNameSourceIdSubjectIdEndedMicros.getKey(1);
      
      MultiKey ownerIdFieldName = new MultiKey(ownerId, fieldName);
      ownerIdFieldNames.add(ownerIdFieldName);
      ownerIdFieldNameSourceIdSubjectIdEndedMicrosToOwnerIdFieldName.put(ownerIdFieldNameSourceIdSubjectIdEndedMicros, ownerIdFieldName);
      
    }
    
    Map<MultiKey, SqlCacheGroup> ownerIdFieldNameToSqlCacheGroup = SqlCacheGroupDao.retrieveByOwnerIdsFieldNames(ownerIdFieldNames, connection);
    Map<Long, SqlCacheGroup> internalIdToSqlCacheGroup = new HashMap<>();
    for (SqlCacheGroup sqlCacheGroup : ownerIdFieldNameToSqlCacheGroup.values()) {
      internalIdToSqlCacheGroup.put(sqlCacheGroup.getInternalId(), sqlCacheGroup);
    }
    
    Set<Long> sqlCacheGroupIdsInHistory = retrieveSqlCacheGroupIdsCachedInHistory(internalIdToSqlCacheGroup.keySet(), connection);
    
    List<MultiKey> ownerIdFieldNameSourceIdSubjectIdEndedMicrosList = new ArrayList<>(ownerIdFieldNameSourceIdSubjectIdsEndedMicros);
    
    Iterator<MultiKey> iterator = ownerIdFieldNameSourceIdSubjectIdEndedMicrosList.iterator();
    
    // filter out uncacheable
    while (iterator.hasNext()) {
      MultiKey ownerIdFieldNameSourceIdSubjectIdEndedMicros = iterator.next();
      MultiKey ownerIdFieldName = ownerIdFieldNameSourceIdSubjectIdEndedMicrosToOwnerIdFieldName.get(ownerIdFieldNameSourceIdSubjectIdEndedMicros);
      SqlCacheGroup sqlCacheGroup = ownerIdFieldNameToSqlCacheGroup.get(ownerIdFieldName);
      
      if (sqlCacheGroup == null || sqlCacheGroup.getDisabledOn() != null) {
        iterator.remove();
      }
    }

    Map<MultiKey, MultiKey> ownerIdFieldNameSourceIdSubjectIdEndedMicrosToSourceIdSubjectId = new HashMap<>();
    Set<MultiKey> sourceIdSubjectIds = new HashSet<>();
    
    for (MultiKey ownerIdFieldNameSourceIdSubjectIdEndedMicros : ownerIdFieldNameSourceIdSubjectIdsEndedMicros) {
      String sourceId = (String)ownerIdFieldNameSourceIdSubjectIdEndedMicros.getKey(2);
      String subjectId = (String)ownerIdFieldNameSourceIdSubjectIdEndedMicros.getKey(3);
      
      MultiKey sourceIdSubjectId = new MultiKey(sourceId, subjectId);
      sourceIdSubjectIds.add(sourceIdSubjectId);
      ownerIdFieldNameSourceIdSubjectIdEndedMicrosToSourceIdSubjectId.put(ownerIdFieldNameSourceIdSubjectIdEndedMicros, sourceIdSubjectId);
      
    }

    Map<MultiKey, Long> sourceIdSubjectIdToInternalId = MemberFinder.findInternalIdsByNames(sourceIdSubjectIds);
    
    List<List<Object>> bindVarsAll = new ArrayList<>();
    Set<SqlCacheGroup> sqlCacheGroupsToUpdate = new HashSet<>();
    Set<MultiKey> sqlCacheGroupInternalIdsMemberInternalIdsForHistory = new HashSet<>();
    Map<MultiKey, Long> sqlCacheGroupInternalIdMemberInternalIdToEndedMicros = new HashMap<>();

    for (MultiKey ownerIdFieldNameSourceIdSubjectIdEndedMicros : ownerIdFieldNameSourceIdSubjectIdEndedMicrosList) {

      MultiKey sourceIdSubjectId = ownerIdFieldNameSourceIdSubjectIdEndedMicrosToSourceIdSubjectId.get(ownerIdFieldNameSourceIdSubjectIdEndedMicros);
      Long endedMicros = (Long)ownerIdFieldNameSourceIdSubjectIdEndedMicros.getKey(4);

      if (sourceIdSubjectId == null) {
        continue;
      }

      Long memberInternalId = sourceIdSubjectIdToInternalId.get(sourceIdSubjectId);
      
      if (memberInternalId == null) {
        continue;
      }
      
      MultiKey ownerIdFieldName = ownerIdFieldNameSourceIdSubjectIdEndedMicrosToOwnerIdFieldName.get(ownerIdFieldNameSourceIdSubjectIdEndedMicros);
      
      if (ownerIdFieldName == null) {
        continue;
      }
      
      SqlCacheGroup sqlCacheGroup = ownerIdFieldNameToSqlCacheGroup.get(ownerIdFieldName);
      
      if (sqlCacheGroup == null) {
        continue;
      }
      
      bindVarsAll.add(GrouperUtil.toListObject(memberInternalId, sqlCacheGroup.getInternalId()));
      MultiKey sqlCacheGroupInternalIdMemberInternalId = new MultiKey(sqlCacheGroup.getInternalId(), memberInternalId);
      
      if (sqlCacheGroupIdsInHistory.contains(sqlCacheGroup.getInternalId())) {
        sqlCacheGroupInternalIdsMemberInternalIdsForHistory.add(sqlCacheGroupInternalIdMemberInternalId);
      }
      
      sqlCacheGroup.setMembershipSize(sqlCacheGroup.getMembershipSize() - 1);
      sqlCacheGroupsToUpdate.add(sqlCacheGroup);
      sqlCacheGroupInternalIdMemberInternalIdToEndedMicros.put(sqlCacheGroupInternalIdMemberInternalId, endedMicros);
    }   
    
    Map<MultiKey, SqlCacheMembership> sqlCacheGroupInternalIdsMemberInternalIdsToSqlCacheMembershipsForHistory = retrieveByCacheGroupInternalIdsMemberInternalIds(sqlCacheGroupInternalIdsMemberInternalIdsForHistory, connection);
    
    // delete cache memberships
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    
    new GcDbAccess().connection(connection).batchSize(batchSize).sql("delete from grouper_sql_cache_mship where member_internal_id = ? and sql_cache_group_internal_id = ?")
      .batchBindVars(bindVarsAll).executeBatchSql();

    // update group counts
    SqlCacheGroupDao.store(sqlCacheGroupsToUpdate, connection, false);
    
    // add cache membership history
    Set<SqlCacheMembershipHst> sqlCacheMembershipHstsToAdd = new HashSet<>();
    for (MultiKey multiKey : sqlCacheGroupInternalIdsMemberInternalIdsToSqlCacheMembershipsForHistory.keySet()) {
      SqlCacheMembership sqlCacheMembershipDeleted = sqlCacheGroupInternalIdsMemberInternalIdsToSqlCacheMembershipsForHistory.get(multiKey);
      Long endedMicros = sqlCacheGroupInternalIdMemberInternalIdToEndedMicros.get(multiKey);
      
      if (sqlCacheMembershipDeleted != null && endedMicros != null) {
        SqlCacheMembershipHst sqlCacheMembershipHst = new SqlCacheMembershipHst();
        sqlCacheMembershipHst.setSqlCacheGroupInternalId(sqlCacheMembershipDeleted.getSqlCacheGroupInternalId());
        sqlCacheMembershipHst.setMemberInternalId(sqlCacheMembershipDeleted.getMemberInternalId());
        sqlCacheMembershipHst.setStartTime(sqlCacheMembershipDeleted.getFlattenedAddTimestamp());
        sqlCacheMembershipHst.setEndTime(endedMicros);
        
        sqlCacheMembershipHstsToAdd.add(sqlCacheMembershipHst);
      }
    }
    
    SqlCacheMembershipHstDao.store(sqlCacheMembershipHstsToAdd, connection, false, true, true);

    return bindVarsAll.size();
  }
  
  /**
   * @param sqlCacheGroupInternalId
   * @param connection optionally pass connection to use
   * @return number of changes
   */
  public static int deleteSqlCacheMembershipsBySqlCacheGroupInternalId(Long sqlCacheGroupInternalId, Connection connection) {
            
    int rowsChanged = new GcDbAccess().connection(connection).sql("delete from grouper_sql_cache_mship where sql_cache_group_internal_id = ?")
        .addBindVar(sqlCacheGroupInternalId).executeSql();
    
    return rowsChanged;
  }
  
  /**
   * select caches by group names and field names and source ids and subject ids
   * @param groupNamesFieldNamesSourceIdsSubjectIds
   * @return the caches if they exist by groupName and fieldName and source ids and subject ids
   */
  /*
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
  */
  
  /**
   * @param sqlCacheMemberships
   * @return number of changes
   */
  public static int store(Collection<SqlCacheMembership> sqlCacheMemberships) {
    return store(sqlCacheMemberships, null, false, false, false);
  }

  /**
   * @param sqlCacheMemberships
   * @param connection optionally pass connection to use
   * @param isInsert
   * @return number of changes
   */
  public static int store(Collection<SqlCacheMembership> sqlCacheMemberships, Connection connection, boolean isInsert, boolean retryBatchStoreFailures, boolean ignoreRetriedBatchStoreFailures) {
    if (GrouperUtil.length(sqlCacheMemberships) == 0) {
      return 0;
    }
    for (SqlCacheMembership sqlCacheMembership : sqlCacheMemberships) {
      sqlCacheMembership.storePrepare();
    }
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    return new GcDbAccess().connection(connection)
        .isInsert(isInsert)
        .retryBatchStoreFailures(retryBatchStoreFailures)
        .ignoreRetriedBatchStoreFailures(ignoreRetriedBatchStoreFailures)
        .storeBatchToDatabase(sqlCacheMemberships, batchSize);
  }


  /**
   * select caches by cache group internal ids and member internal ids
   * @param cacheGroupInternalIdsMemberInternalIds
   * @param connection
   * @return the caches if they exist 
   */
  public static Map<MultiKey, SqlCacheMembership> retrieveByCacheGroupInternalIdsMemberInternalIds(Collection<MultiKey> cacheGroupInternalIdsMemberInternalIds, Connection connection) {
    
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
      
      GcDbAccess gcDbAccess = new GcDbAccess().connection(connection);
      
      for (int i=0;i<batchOfCacheGroupInternalIdMemberInternalIdList.size();i++) {
        if (i>0) {
          sql.append(" or ");
        }
        sql.append(" ( member_internal_id = ? and sql_cache_group_internal_id = ? ) ");
        MultiKey cacheGroupInternalIdMemberInternalId = batchOfCacheGroupInternalIdMemberInternalIdList.get(i);
        gcDbAccess.addBindVar(cacheGroupInternalIdMemberInternalId.getKey(1));
        gcDbAccess.addBindVar(cacheGroupInternalIdMemberInternalId.getKey(0));
      }
      
      List<SqlCacheMembership> sqlCacheMemberships = gcDbAccess.sql(sql.toString()).selectList(SqlCacheMembership.class);
      
      for (SqlCacheMembership sqlCacheMembership : GrouperClientUtils.nonNull(sqlCacheMemberships)) {
        result.put(new MultiKey(sqlCacheMembership.getSqlCacheGroupInternalId(), sqlCacheMembership.getMemberInternalId()), sqlCacheMembership);
      }
      
    }
    return result;
  }
  
  /**
   * @param sqlCacheGroupIds
   * @param connection
   * @return sqlCacheGroupIds being cached in history
   */
  public static Set<Long> retrieveSqlCacheGroupIdsCachedInHistory(Set<Long> sqlCacheGroupIds, Connection connection) {
    
    Set<Long> result = new HashSet<>();

    if (GrouperUtil.length(sqlCacheGroupIds) == 0) {
      return result;
    }

    List<Long> sqlCacheGroupIdsList = new ArrayList<>(sqlCacheGroupIds);
    
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(sqlCacheGroupIdsList), batchSize, false);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<Long> batchOfSqlCacheGroupIdsList = GrouperClientUtils.batchList(sqlCacheGroupIdsList, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("select gscd.owner_internal_id from grouper_sql_cache_dependency gscd, grouper_sql_cache_depend_type gscdt where gscd.dep_type_internal_id=gscdt.internal_id and gscdt.dependency_category='mshipHistory' and ( ");
      
      GcDbAccess gcDbAccess = new GcDbAccess().connection(connection);
      
      for (int i=0;i<batchOfSqlCacheGroupIdsList.size();i++) {
        if (i>0) {
          sql.append(" or ");
        }
        sql.append(" owner_internal_id = ? ");
        Long sqlCacheGroupId = batchOfSqlCacheGroupIdsList.get(i);
        gcDbAccess.addBindVar(sqlCacheGroupId);
      }
      
      sql.append(" ) ");

      result.addAll(gcDbAccess.sql(sql.toString()).selectList(Long.class));
    }
    
    return result;
  }
}

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

/**
 * dao for sql cache mship hst
 * @author shilen
 *
 */
public class SqlCacheMembershipHstDao {


  public SqlCacheMembershipHstDao() {
  }

  /**
   * 
   * @param sqlCacheGroups
   * @param connection optionally pass connection to use
   * @param isInsert
   * @return number of changes
   */
  public static int store(Collection<SqlCacheMembershipHst> sqlCacheMembershipHsts, Connection connection, boolean isInsert, boolean retryBatchStoreFailures, boolean ignoreRetriedBatchStoreFailures) {
    if (GrouperUtil.length(sqlCacheMembershipHsts) == 0) {
      return 0;
    }
    for (SqlCacheMembershipHst sqlCacheMembershipHst : sqlCacheMembershipHsts) {
      sqlCacheMembershipHst.storePrepare();
    }
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    return new GcDbAccess().connection(connection)
        .isInsert(isInsert)
        .retryBatchStoreFailures(retryBatchStoreFailures)
        .ignoreRetriedBatchStoreFailures(ignoreRetriedBatchStoreFailures)
        .storeBatchToDatabase(sqlCacheMembershipHsts, batchSize);
  }
  
  /**
   * things to add to sql cache membership history.  6 fields in multikey: 
   * ownerId, fieldName, sourceId, subjectId, startedMicros, endedMicros
   * @param membershipsAddedAndDeleted
   * @param connection optionally pass connection to use
   * @return number of cache membership history inserts
   */
  public static int insertSqlCacheMembershipHstsIfCacheable(Collection<MultiKey> membershipsAddedAndDeleted, Connection connection) {
    
    if (GrouperUtil.length(membershipsAddedAndDeleted) == 0) {
      return 0;
    }

    Set<MultiKey> ownerIdFieldNames = new HashSet<>();
    
    Map<MultiKey, MultiKey> ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroToOwnerIdFieldName = new HashMap<>();
    
    for (MultiKey ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro : membershipsAddedAndDeleted) {
      String ownerId = (String)ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(0);
      String fieldName = (String)ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(1);
      
      MultiKey ownerIdFieldName = new MultiKey(ownerId, fieldName);
      ownerIdFieldNames.add(ownerIdFieldName);
      ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroToOwnerIdFieldName.put(ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro, ownerIdFieldName);
    }
    
    Map<MultiKey, SqlCacheGroup> ownerIdFieldNameToSqlCacheGroup = SqlCacheGroupDao.retrieveByOwnerIdsFieldNames(ownerIdFieldNames, connection);
    Map<Long, SqlCacheGroup> internalIdToSqlCacheGroup = new HashMap<>();
    for (SqlCacheGroup sqlCacheGroup : ownerIdFieldNameToSqlCacheGroup.values()) {
      internalIdToSqlCacheGroup.put(sqlCacheGroup.getInternalId(), sqlCacheGroup);
    }
    
    Set<Long> sqlCacheGroupIdsInHistory = SqlCacheMembershipDao.retrieveSqlCacheGroupIdsCachedInHistory(internalIdToSqlCacheGroup.keySet(), connection);
    
    List<MultiKey> ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroList = new ArrayList<>(membershipsAddedAndDeleted);
    
    Iterator<MultiKey> iterator = ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroList.iterator();
    
    // filter out uncacheable
    while (iterator.hasNext()) {
      MultiKey ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro = iterator.next();
      MultiKey ownerIdFieldName = ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroToOwnerIdFieldName.get(ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro);
      SqlCacheGroup sqlCacheGroup = ownerIdFieldNameToSqlCacheGroup.get(ownerIdFieldName);
      
      if (sqlCacheGroup == null || !sqlCacheGroupIdsInHistory.contains(sqlCacheGroup.getInternalId()) || sqlCacheGroup.getDisabledOn() != null) {
        iterator.remove();
      }
    }

    Map<MultiKey, MultiKey> ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroToSourceIdSubjectId = new HashMap<>();
    Set<MultiKey> sourceIdSubjectIds = new HashSet<>();
    
    for (MultiKey ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro : ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroList) {
      String sourceId = (String)ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(2);
      String subjectId = (String)ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(3);
      
      MultiKey sourceIdSubjectId = new MultiKey(sourceId, subjectId);
      sourceIdSubjectIds.add(sourceIdSubjectId);
      ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroToSourceIdSubjectId.put(ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro, sourceIdSubjectId);
      
    }

    Map<MultiKey, Long> sourceIdSubjectIdToInternalId = MemberFinder.findInternalIdsByNames(sourceIdSubjectIds);
    
    List<SqlCacheMembershipHst> sqlCacheMembershipHstsToInsert = new ArrayList<>();

    for (MultiKey ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro : ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroList) {

      MultiKey sourceIdSubjectId = ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroToSourceIdSubjectId.get(ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro);
      
      if (sourceIdSubjectId == null) {
        continue;
      }

      Long memberInternalId = sourceIdSubjectIdToInternalId.get(sourceIdSubjectId);
      
      if (memberInternalId == null) {
        continue;
      }
      
      MultiKey ownerIdFieldName = ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicroToOwnerIdFieldName.get(ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro);
      
      if (ownerIdFieldName == null) {
        continue;
      }
      
      SqlCacheGroup sqlCacheGroup = ownerIdFieldNameToSqlCacheGroup.get(ownerIdFieldName);
      
      if (sqlCacheGroup == null) {
        continue;
      }
      
      SqlCacheMembershipHst sqlCacheMembershipHst = new SqlCacheMembershipHst();
      Long membershipAddedLong = (Long)ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(4);
      Long membershipDeletedLong = (Long)ownerIdFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(5);
      sqlCacheMembershipHst.setMemberInternalId(memberInternalId);
      sqlCacheMembershipHst.setSqlCacheGroupInternalId(sqlCacheGroup.getInternalId());
      sqlCacheMembershipHst.setStartTime(membershipAddedLong);
      sqlCacheMembershipHst.setEndTime(membershipDeletedLong);
      sqlCacheMembershipHstsToInsert.add(sqlCacheMembershipHst);
    }   
    
    int numberOfChanges =  SqlCacheMembershipHstDao.store(sqlCacheMembershipHstsToInsert, connection, true, true, true);
        
    return numberOfChanges;
  }
}

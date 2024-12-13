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
   * ownerName, fieldName, sourceId, subjectId, startedMicros, endedMicros
   * @param membershipsAddedAndDeleted
   * @param connection optionally pass connection to use
   * @return number of cache membership history inserts
   */
  public static int insertSqlCacheMembershipHstsIfCacheable(Collection<MultiKey> membershipsAddedAndDeleted, Connection connection) {
    
    if (GrouperUtil.length(membershipsAddedAndDeleted) == 0) {
      return 0;
    }

    Set<MultiKey> ownerNameFieldNames = new HashSet<>();
    
    Map<MultiKey, MultiKey> ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroToOwnerNameFieldName = new HashMap<>();
    
    for (MultiKey ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro : membershipsAddedAndDeleted) {
      String ownerName = (String)ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(0);
      String fieldName = (String)ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(1);
      
      MultiKey ownerNameFieldName = new MultiKey(ownerName, fieldName);
      ownerNameFieldNames.add(ownerNameFieldName);
      ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroToOwnerNameFieldName.put(ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro, ownerNameFieldName);
    }
    
    Map<MultiKey, SqlCacheGroup> ownerNameFieldNameToSqlCacheGroup = SqlCacheGroupDao.retrieveByOwnerNamesFieldNames(ownerNameFieldNames, connection);
    Map<Long, SqlCacheGroup> internalIdToSqlCacheGroup = new HashMap<>();
    for (SqlCacheGroup sqlCacheGroup : ownerNameFieldNameToSqlCacheGroup.values()) {
      internalIdToSqlCacheGroup.put(sqlCacheGroup.getInternalId(), sqlCacheGroup);
    }
    
    Set<Long> sqlCacheGroupIdsInHistory = SqlCacheMembershipDao.retrieveSqlCacheGroupIdsCachedInHistory(internalIdToSqlCacheGroup.keySet(), connection);
    
    List<MultiKey> ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroList = new ArrayList<>(membershipsAddedAndDeleted);
    
    Iterator<MultiKey> iterator = ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroList.iterator();
    
    // filter out uncacheable
    while (iterator.hasNext()) {
      MultiKey ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro = iterator.next();
      MultiKey ownerNameFieldName = ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroToOwnerNameFieldName.get(ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro);
      SqlCacheGroup sqlCacheGroup = ownerNameFieldNameToSqlCacheGroup.get(ownerNameFieldName);
      
      if (sqlCacheGroup == null || !sqlCacheGroupIdsInHistory.contains(sqlCacheGroup.getInternalId()) || sqlCacheGroup.getDisabledOn() != null) {
        iterator.remove();
      }
    }

    Map<MultiKey, MultiKey> ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroToSourceIdSubjectId = new HashMap<>();
    Set<MultiKey> sourceIdSubjectIds = new HashSet<>();
    
    for (MultiKey ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro : ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroList) {
      String sourceId = (String)ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(2);
      String subjectId = (String)ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(3);
      
      MultiKey sourceIdSubjectId = new MultiKey(sourceId, subjectId);
      sourceIdSubjectIds.add(sourceIdSubjectId);
      ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroToSourceIdSubjectId.put(ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro, sourceIdSubjectId);
      
    }

    Map<MultiKey, Long> sourceIdSubjectIdToInternalId = MemberFinder.findInternalIdsByNames(sourceIdSubjectIds);
    
    List<SqlCacheMembershipHst> sqlCacheMembershipHstsToInsert = new ArrayList<>();

    for (MultiKey ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro : ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroList) {

      MultiKey sourceIdSubjectId = ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroToSourceIdSubjectId.get(ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro);
      
      if (sourceIdSubjectId == null) {
        continue;
      }

      Long memberInternalId = sourceIdSubjectIdToInternalId.get(sourceIdSubjectId);
      
      if (memberInternalId == null) {
        continue;
      }
      
      MultiKey ownerNameFieldName = ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicroToOwnerNameFieldName.get(ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro);
      
      if (ownerNameFieldName == null) {
        continue;
      }
      
      SqlCacheGroup sqlCacheGroup = ownerNameFieldNameToSqlCacheGroup.get(ownerNameFieldName);
      
      if (sqlCacheGroup == null) {
        continue;
      }
      
      SqlCacheMembershipHst sqlCacheMembershipHst = new SqlCacheMembershipHst();
      Long membershipAddedLong = (Long)ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(4);
      Long membershipDeletedLong = (Long)ownerNameFieldNameSourceIdSubjectIdStartedMicroEndedMicro.getKey(5);
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

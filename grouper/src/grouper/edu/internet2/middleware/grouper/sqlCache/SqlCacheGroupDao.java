package edu.internet2.middleware.grouper.sqlCache;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * dao for sql cache groups
 * @author mchyzer
 *
 */
public class SqlCacheGroupDao {


  public SqlCacheGroupDao() {
  }

  /**
   * 
   * @param sqlCacheGroup
   * @return true if changed
   */
  public static boolean store(SqlCacheGroup sqlCacheGroup) {
    sqlCacheGroup.storePrepare();
    boolean changed = new GcDbAccess().storeToDatabase(sqlCacheGroup);
    return changed;
  }
  
  /**
   * 
   * @param sqlCacheGroups
   * @return number of changes
   */
  public static int store(Collection<SqlCacheGroup> sqlCacheGroups) {
    return store(sqlCacheGroups, null, false);
  }

  /**
   * 
   * @param sqlCacheGroups
   * @param connection optionally pass connection to use
   * @param isInsert
   * @return number of changes
   */
  public static int store(Collection<SqlCacheGroup> sqlCacheGroups, Connection connection, boolean isInsert) {
    if (GrouperUtil.length(sqlCacheGroups) == 0) {
      return 0;
    }
    for (SqlCacheGroup sqlCacheGroup : sqlCacheGroups) {
      sqlCacheGroup.storePrepare();
    }
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    return new GcDbAccess().connection(connection).isInsert(isInsert).storeBatchToDatabase(sqlCacheGroups, batchSize);
  }

  /**
   * select grouper sync by id
   * @param theConnectionName
   * @param id
   * @return the sync
   */
  public static SqlCacheGroup retrieveByInternalId(Long id) {
    SqlCacheGroup sqlCacheGroup = new GcDbAccess()
        .sql("select * from grouper_sql_cache_group where internal_id = ?").addBindVar(id).select(SqlCacheGroup.class);
    return sqlCacheGroup;
  }
  

  /**
   * 
   * @param sqlCacheMembership
   */
  public static void delete(SqlCacheGroup sqlCacheGroup) {
    new GcDbAccess().deleteFromDatabase(sqlCacheGroup);
  }

  /**
   * select caches by group names and field names
   * @param groupNamesFieldNames
   * @return the caches if they exist by groupName and fieldName
   */
  public static Map<MultiKey, SqlCacheGroup> retrieveByOwnerNamesFieldNames(Collection<MultiKey> ownerNamesFieldNames) {
    return retrieveByOwnerNamesFieldNames(ownerNamesFieldNames, null);
  }
  
  /**
   * select caches by group names and field names
   * @param groupNamesFieldNames
   * @param connection optionally pass connection to use
   * @return the caches if they exist by groupName and fieldName
   */
  public static Map<MultiKey, SqlCacheGroup> retrieveByOwnerNamesFieldNames(Collection<MultiKey> ownerNamesFieldNames, Connection connection) {
    
    Map<MultiKey, SqlCacheGroup> result = new HashMap<>();

    if (GrouperUtil.length(ownerNamesFieldNames) == 0) {
      return result;
    }

    Set<String> fieldNames = new HashSet<>();
    Set<String> groupNames = new HashSet<>();
    Set<String> stemNames = new HashSet<>();
    Set<String> attributeDefNames = new HashSet<>();
    
    @SuppressWarnings("unchecked")
    Set<Field> fields = FieldFinder.findAll();
    Map<String, Field> fieldNameToField = new HashMap<>();
    for (Field field : fields) {
      fieldNameToField.put(field.getName(), field);
    }

    for (MultiKey ownerNameFieldName : ownerNamesFieldNames) {
      String ownerName = (String)ownerNameFieldName.getKey(0);
      String fieldName = (String)ownerNameFieldName.getKey(1);
      fieldNames.add(fieldName);      
      Field field = fieldNameToField.get(fieldName);
      
      if (field.isAttributeDefListField()) {
        attributeDefNames.add(ownerName);
      } else if (field.isStemListField()) {
        stemNames.add(ownerName);
      } else {
        groupNames.add(ownerName);
      }
    }
    
    // all fields and groups, note, some might not be there
    Map<String, Long> fieldNameToInternalId = FieldFinder.findInternalIdsByNames(fieldNames);
    Map<String, Long> groupNameToInternalId = GroupFinder.findInternalIdsByNames(groupNames);
    Map<String, Long> stemNameToIdIndex = StemFinder.findIdIndexesByNames(stemNames);
    Map<String, Long> attributeDefNameToIdIndex = AttributeDefFinder.findIdIndexesByNames(attributeDefNames);

    Map<MultiKey, MultiKey> cacheGroupInternalIdFieldInternalIdToOwnerNameFieldName = new HashMap<>();

    List<MultiKey> cacheGroupInternalIdFieldInternalIdList = new ArrayList<MultiKey>();
    for (MultiKey ownerNameFieldName : ownerNamesFieldNames) {
      
      String ownerName = (String)ownerNameFieldName.getKey(0);
      String fieldName = (String)ownerNameFieldName.getKey(1);
      Long fieldInternalId = fieldNameToInternalId.get(fieldName);
      
      Field field = fieldNameToField.get(fieldName);
      Long cacheGroupInternalId = null;
      
      if (field.isAttributeDefListField()) {
        cacheGroupInternalId = attributeDefNameToIdIndex.get(ownerName);
      } else if (field.isStemListField()) {
        cacheGroupInternalId = stemNameToIdIndex.get(ownerName);
      } else {
        cacheGroupInternalId = groupNameToInternalId.get(ownerName);
      }


      if (cacheGroupInternalId != null && fieldInternalId != null) {
        MultiKey cacheGroupInternalIdFieldInternalId = new MultiKey(cacheGroupInternalId, fieldInternalId);
        cacheGroupInternalIdFieldInternalIdList.add(cacheGroupInternalIdFieldInternalId);
        cacheGroupInternalIdFieldInternalIdToOwnerNameFieldName.put(cacheGroupInternalIdFieldInternalId, new MultiKey(ownerName, fieldName));
      }
    }

    // now we have a list of group internal ids and field internal ids we can find
    Map<MultiKey, SqlCacheGroup> cacheGroupInternalIdFieldInternalIdToCacheGroup = retrieveByGroupInternalIdsFieldInternalIds(cacheGroupInternalIdFieldInternalIdList, connection);
    
    for (MultiKey cacheGroupInternalIdFieldInternalId : cacheGroupInternalIdFieldInternalIdToCacheGroup.keySet()) {
      SqlCacheGroup sqlCacheGroup  = cacheGroupInternalIdFieldInternalIdToCacheGroup.get(cacheGroupInternalIdFieldInternalId);
      MultiKey ownerNameFieldName = 
          cacheGroupInternalIdFieldInternalIdToOwnerNameFieldName.get(cacheGroupInternalIdFieldInternalId);
      result.put(ownerNameFieldName, sqlCacheGroup);
    }

    return result;
  }

  /**
   * select caches by group names and field names
   * @param groupNamesFieldNames
   * @return the caches if they exist by groupName and fieldName
   */
  public static Map<MultiKey, SqlCacheGroup> retrieveByGroupNamesFieldNames(Collection<MultiKey> groupNamesFieldNames) {
    
    Map<MultiKey, SqlCacheGroup> result = new HashMap<>();

    if (GrouperUtil.length(groupNamesFieldNames) == 0) {
      return result;
    }

    Set<String> fieldNames = new HashSet<>();
    Set<String> groupNames = new HashSet<>();
    
    for (MultiKey groupNameFieldName : groupNamesFieldNames) {
      groupNames.add((String)groupNameFieldName.getKey(0));
      fieldNames.add((String)groupNameFieldName.getKey(1));
    }
    
    // all fields and groups, note, some might not be there
    Map<String, Long> fieldNameToInternalId = FieldFinder.findInternalIdsByNames(fieldNames);
    Map<String, Long> groupNameToInternalId = GroupFinder.findInternalIdsByNames(groupNames);

    Map<MultiKey, MultiKey> groupInternalIdFieldInternalIdToGroupNameFieldName = new HashMap<>();

    List<MultiKey> groupInternalIdFieldInternalIdList = new ArrayList<MultiKey>();
    for (MultiKey groupNameFieldName : groupNamesFieldNames) {
      
      String groupName = (String)groupNameFieldName.getKey(0);
      String fieldName = (String)groupNameFieldName.getKey(1);

      Long groupInternalId = groupNameToInternalId.get(groupName);
      Long fieldInternalId = fieldNameToInternalId.get(fieldName);

      if (groupInternalId != null && fieldInternalId != null) {
        MultiKey groupInternalIdFieldInternalId = new MultiKey(groupInternalId, fieldInternalId);
        groupInternalIdFieldInternalIdList.add(groupInternalIdFieldInternalId);
        groupInternalIdFieldInternalIdToGroupNameFieldName.put(groupInternalIdFieldInternalId, new MultiKey(groupName, fieldName));
      }
    }

    // now we have a list of group internal ids and field internal ids we can find
    Map<MultiKey, SqlCacheGroup> groupInternalIdFieldInternalIdToCacheGroup = retrieveByGroupInternalIdsFieldInternalIds(groupInternalIdFieldInternalIdList);
    
    for (MultiKey groupInternalIdFieldInternalId : groupInternalIdFieldInternalIdToCacheGroup.keySet()) {
      SqlCacheGroup sqlCacheGroup  = groupInternalIdFieldInternalIdToCacheGroup.get(groupInternalIdFieldInternalId);
      MultiKey groupNameFieldName = 
          groupInternalIdFieldInternalIdToGroupNameFieldName.get(groupInternalIdFieldInternalId);
      result.put(groupNameFieldName, sqlCacheGroup);
    }

    return result;
  }

  /**
   * select caches by group names and field names
   * @param groupNamesFieldNames
   * @return the caches if they exist by groupName and fieldName
   */
  public static Map<MultiKey, Long> retrieveByGroupNamesFieldNamesToInternalId(Collection<MultiKey> groupNamesFieldNames) {
    
    Map<MultiKey, Long> result = new HashMap<>();

    if (GrouperUtil.length(groupNamesFieldNames) == 0) {
      return result;
    }

    Map<MultiKey, SqlCacheGroup> groupNameFieldNameToSqlCacheGroup = retrieveByGroupNamesFieldNames(groupNamesFieldNames);
    
    for (MultiKey groupNameFieldName : groupNameFieldNameToSqlCacheGroup.keySet()) {
      SqlCacheGroup sqlCacheGroup = groupNameFieldNameToSqlCacheGroup.get(groupNameFieldName);
      result.put(groupNameFieldName, sqlCacheGroup.getInternalId());
    }
    return result;
  }
  
  /**
   * select caches by group internal ids and field internal ids
   * @param groupInternalIdsFieldInternalIds
   * @return the caches if they exist 
   */
  public static Map<MultiKey, SqlCacheGroup> retrieveByGroupInternalIdsFieldInternalIds(Collection<MultiKey> groupInternalIdsFieldInternalIds) {
    return retrieveByGroupInternalIdsFieldInternalIds(groupInternalIdsFieldInternalIds, null);
  }
  
  /**
   * select caches by group internal ids and field internal ids
   * @param groupInternalIdsFieldInternalIds
   * @param connection optionally pass connection to use
   * @return the caches if they exist 
   */
  public static Map<MultiKey, SqlCacheGroup> retrieveByGroupInternalIdsFieldInternalIds(Collection<MultiKey> groupInternalIdsFieldInternalIds, Connection connection) {
    
    Map<MultiKey, SqlCacheGroup> result = new HashMap<>();

    if (GrouperUtil.length(groupInternalIdsFieldInternalIds) == 0) {
      return result;
    }

    List<MultiKey> groupInternalIdFieldInternalIdList = new ArrayList<>(groupInternalIdsFieldInternalIds);
    
    // two bind vars in each record to retrieve
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900) / 2;
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(GrouperUtil.length(groupInternalIdFieldInternalIdList), batchSize, false);
    
    for (int batchIndex = 0; batchIndex<numberOfBatches; batchIndex++) {
      
      List<MultiKey> batchOfGroupInternalIdFieldInternalIdList = GrouperClientUtils.batchList(groupInternalIdFieldInternalIdList, batchSize, batchIndex);
      
      StringBuilder sql = new StringBuilder("select * from grouper_sql_cache_group where ");
      
      GcDbAccess gcDbAccess = new GcDbAccess().connection(connection);
      
      for (int i=0;i<batchOfGroupInternalIdFieldInternalIdList.size();i++) {
        if (i>0) {
          sql.append(" or ");
        }
        sql.append(" ( group_internal_id = ? and field_internal_id = ? ) ");
        MultiKey groupInternalIdFieldInternalId = batchOfGroupInternalIdFieldInternalIdList.get(i);
        gcDbAccess.addBindVar(groupInternalIdFieldInternalId.getKey(0));
        gcDbAccess.addBindVar(groupInternalIdFieldInternalId.getKey(1));
      }
      
      List<SqlCacheGroup> sqlCacheGroups = gcDbAccess.sql(sql.toString()).selectList(SqlCacheGroup.class);
      
      for (SqlCacheGroup sqlCacheGroup : GrouperClientUtils.nonNull(sqlCacheGroups)) {
        result.put(new MultiKey(sqlCacheGroup.getGroupInternalId(), sqlCacheGroup.getFieldInternalId()), sqlCacheGroup);
      }
      
    }
    return result;
  }

  /**
   * retrieve cache group by group name field name or created
   * @param sqlCacheGroups
   */
  public static SqlCacheGroup retrieveOrCreateBySqlGroupCache(SqlCacheGroup sqlCacheGroup) {
    if (sqlCacheGroup == null) {
      return null;
    }
    return retrieveOrCreateBySqlGroupCache(GrouperUtil.toList(sqlCacheGroup)).values().iterator().next();
  }

  /**
   * retrieve cache group by group name field name or created
   * @param sqlCacheGroups
   * @return groupInternalId / fieldInternalId to sql cache group
   */
  public static Map<MultiKey, SqlCacheGroup> retrieveOrCreateBySqlGroupCache(Collection<SqlCacheGroup> sqlCacheGroups) {
    
    Map<MultiKey, SqlCacheGroup> result = new HashMap<MultiKey, SqlCacheGroup>();

    if (GrouperUtil.length(sqlCacheGroups) == 0) {
      return result;
    }
    Set<MultiKey> groupInternalIdsFieldInternalIds = new HashSet<>();
    
    for (SqlCacheGroup sqlCacheGroup : sqlCacheGroups) {
      groupInternalIdsFieldInternalIds.add(new MultiKey(sqlCacheGroup.getGroupInternalId(), sqlCacheGroup.getFieldInternalId()));
    }
    
    Map<MultiKey, SqlCacheGroup> existingGroupInternalIdsFieldInternalIdsToCacheGroups = retrieveByGroupInternalIdsFieldInternalIds(groupInternalIdsFieldInternalIds);
    
    List<SqlCacheGroup> sqlCacheGroupsToCreate = new ArrayList<SqlCacheGroup>();
    
    for (SqlCacheGroup sqlCacheGroup : sqlCacheGroups) {
      SqlCacheGroup existingCacheGroup = existingGroupInternalIdsFieldInternalIdsToCacheGroups.get(new MultiKey(sqlCacheGroup.getGroupInternalId(), sqlCacheGroup.getFieldInternalId()));
      if (existingCacheGroup == null) {
        sqlCacheGroupsToCreate.add(sqlCacheGroup);
      } else {
        result.put(new MultiKey(existingCacheGroup.getGroupInternalId(), existingCacheGroup.getFieldInternalId()), sqlCacheGroup);
      }
    }

    if (sqlCacheGroupsToCreate.size() == 0) {
      return result;
    }

    // get ids in one fell swoop
    List<Long> ids = TableIndex.reserveIds(TableIndexType.sqlGroupCache, sqlCacheGroupsToCreate.size());

    for (int i=0; i < sqlCacheGroupsToCreate.size(); i++) {
      SqlCacheGroup sqlCacheGroup = sqlCacheGroupsToCreate.get(i);
      sqlCacheGroup.setTempInternalIdOnDeck(ids.get(i));
      sqlCacheGroup.storePrepare();
      result.put(new MultiKey(sqlCacheGroup.getGroupInternalId(), sqlCacheGroup.getFieldInternalId()), sqlCacheGroup);

    }

    //    internal_id int8 NOT NULL, -- internal integer id for this table.  Do not refer to this outside of Grouper.  This will differ per env (dev/test/prod)
    //    group_internal_id int8 NOT NULL, -- internal integer id for gruops which are cacheable
    //    field_internal_id int8 NOT NULL, -- internal integer id for the field which is the members or privilege which is cached
    //    membership_size int8 NOT NULL, -- approximate number of members of this group, used primarily to optimize batching
    //    membership_size_hst int8 NOT NULL, -- approximate number of rows of HST data for this group, used primarily to optimize batching
    //    created_on timestamp NOT NULL, -- when this row was created (i.e. when this group started to be cached)
    //    enabled_on timestamp NOT NULL, -- when this cache will be ready to use (do not use it while it is being populated)
    //    disabled_on timestamp NULL, -- when this cache should stop being used

    int defaultBatchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.batchSize", 1000);

    new GcDbAccess().storeBatchToDatabase(sqlCacheGroupsToCreate, defaultBatchSize);
    return result;
  }

}

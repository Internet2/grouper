package edu.internet2.middleware.grouper.sqlCache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GroupFinder;
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
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(SqlCacheGroup sqlCacheGroup) {
    sqlCacheGroup.storePrepare();
    boolean changed = new GcDbAccess().storeToDatabase(sqlCacheGroup);
    return changed;
  }

  /**
   * 
   * @param connectionName
   * @return number of changes
   */
  public static int store(Collection<SqlCacheGroup> sqlCacheGroups) {
    if (GrouperUtil.length(sqlCacheGroups) == 0) {
      return 0;
    }
    for (SqlCacheGroup sqlCacheGroup : sqlCacheGroups) {
      sqlCacheGroup.storePrepare();
    }
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    return new GcDbAccess().storeBatchToDatabase(sqlCacheGroups, batchSize);
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
      
      GcDbAccess gcDbAccess = new GcDbAccess();
      
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
   * find existing sql cache assignments by attribute assign id (of the assignment on assignment)
   * @param attributeAssignIds
   * @return the map of attributeAssignmentId to groupName and fieldName
   */
  public static Map<String, MultiKey> retrieveExistingAttributeAssignments(Collection<String> attributeAssignIds) {
    List<String> attributeAssignIdsList = new ArrayList<String>(attributeAssignIds);

    Map<String, MultiKey> existingAttributeAssignIdToGroupNameFieldName = new HashMap<String, MultiKey>();

    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(attributeAssignIdsList.size(), batchSize, false);

    for (int i=0;i<numberOfBatches;i++) {

      List<String> attributeAssignIdsBatch = GrouperUtil.batchList(attributeAssignIdsList, batchSize, i);

      GcDbAccess gcDbAccess = new GcDbAccess();
      
      gcDbAccess.addBindVar(SqlCacheGroup.attributeDefNameNameListName());
      
      // make sure its still there
      StringBuilder query = new StringBuilder("select gaaagv.group_name, gaaagv.value_string, gaaagv.attribute_assign_id2 from grouper_aval_asn_asn_group_v gaaagv "
          + "where gaaagv.attribute_def_name_name2 = ? and (  ");

      boolean isFirst = true;
      for (String attributeAssignId : attributeAssignIdsBatch) {
        if (!isFirst) {
          query.append(" or ");
        }
        query.append(" gaaagv.attribute_assign_id2 = ? ");
        gcDbAccess.addBindVar(attributeAssignId);
        isFirst = false;
      }
      query.append(" ) ");

      List<Object[]> groupNamesFieldNamesAttributeAssignIds = gcDbAccess.sql(query.toString()).selectList(Object[].class);
      for (Object[] groupNameFieldNameAttributeAssignId : GrouperUtil.nonNull(groupNamesFieldNamesAttributeAssignIds)) {
        String groupName = (String)groupNameFieldNameAttributeAssignId[0];
        String fieldName = (String)groupNameFieldNameAttributeAssignId[1];
        String attributeAssignId = (String)groupNameFieldNameAttributeAssignId[2];
        existingAttributeAssignIdToGroupNameFieldName.put(attributeAssignId, new MultiKey(groupName, fieldName));
      }
      
    }
    return existingAttributeAssignIdToGroupNameFieldName;
  }

  /**
   * find existing sql cache assignments by attribute assign id (of the assignment on assignment)
   * @param attributeAssignIds
   * @return the map of attributeAssignmentId to groupName and fieldName (could be multiple)
   */
  public static Map<String, Set<MultiKey>> retrieveNonexistingAttributeAssignments(Collection<String> attributeAssignIds, long minimumEventMicros) {
      List<String> attributeAssignIdsList = new ArrayList<String>(attributeAssignIds);

    Map<String, Set<MultiKey>> existingAttributeAssignIdToGroupNameFieldName = new HashMap<String, Set<MultiKey>>();

    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(attributeAssignIdsList.size(), batchSize, false);

    for (int i=0;i<numberOfBatches;i++) {

      List<String> attributeAssignIdsBatch = GrouperUtil.batchList(attributeAssignIdsList, batchSize, i);

      GcDbAccess gcDbAccess = new GcDbAccess();
      
      gcDbAccess.addBindVar(minimumEventMicros);
      gcDbAccess.addBindVar(SqlCacheGroup.attributeDefNameNameListName());
      
      // make sure its still there
      StringBuilder query = new StringBuilder("select gpg.name owner_group_name, gpaavv.value_string field_name, "
          + " gpaa_value.source_id value_attribute_assign_id from grouper_pit_attr_asn_value_v gpaavv, "
          + " grouper_pit_attribute_assign gpaa_value, grouper_pit_attribute_assign gpaa_owner, grouper_pit_groups gpg , "
          + " grouper_pit_attr_def_name gpadn where gpaavv.attribute_assign_id = gpaa_value.id "
          + " and gpaavv.owner_attribute_assign_id = gpaa_owner.id and gpaa_owner.owner_group_id = gpg.id "
          + " and gpaavv.end_time >= ? "
          + " and gpaavv.attribute_def_name_id = gpadn.id and gpadn.name = ? "
          + " and (  ");

      boolean isFirst = true;
      for (String attributeAssignId : attributeAssignIdsBatch) {
        if (!isFirst) {
          query.append(" or ");
        }
        query.append(" gpaa_value.source_id = ? ");
        gcDbAccess.addBindVar(attributeAssignId);
        isFirst = false;
      }
      query.append(" ) ");

      List<Object[]> groupNamesFieldNamesAttributeAssignIds = gcDbAccess.selectList(Object[].class);
      for (Object[] groupNameFieldNameAttributeAssignId : GrouperUtil.nonNull(groupNamesFieldNamesAttributeAssignIds)) {
        String groupName = (String)groupNameFieldNameAttributeAssignId[0];
        String fieldName = (String)groupNameFieldNameAttributeAssignId[1];
        String attributeAssignId = (String)groupNameFieldNameAttributeAssignId[2];
        Set<MultiKey> set = existingAttributeAssignIdToGroupNameFieldName.get(attributeAssignId);
        if (set == null) {
          set = new HashSet<>();
          existingAttributeAssignIdToGroupNameFieldName.put(attributeAssignId, set);
        }
        set.add(new MultiKey(groupName, fieldName));
      }
      
    }
    return existingAttributeAssignIdToGroupNameFieldName;
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

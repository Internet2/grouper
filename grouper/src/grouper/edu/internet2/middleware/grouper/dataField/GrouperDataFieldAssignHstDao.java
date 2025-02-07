package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

/**
 * dao for data field assign history
 */
public class GrouperDataFieldAssignHstDao {


  public GrouperDataFieldAssignHstDao() {
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataFieldAssignHst.class)).executeSql();
  }
  
  public static List<GrouperDataFieldAssignHst> selectByDataFieldInternalId(long dataFieldInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_field_assign_hst where data_field_internal_id = ?")
        .addBindVar(dataFieldInternalId)
        .selectList(GrouperDataFieldAssignHst.class);
  }

  public static List<GrouperDataFieldAssignHst> selectByMemberInternalId(long memberInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_field_assign_hst where member_internal_id = ?")
        .addBindVar(memberInternalId)
        .selectList(GrouperDataFieldAssignHst.class);
  }
  
  public static List<GrouperDataFieldAssignHst> selectByDataFieldInternalIds(Set<Long> dataFieldInternalIds) {
    
    if (dataFieldInternalIds == null || dataFieldInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_field_assign_hst ")
        .selectMultipleColumnName("data_field_internal_id")
        .bindVars(new ArrayList<Long>(dataFieldInternalIds))
        .selectList(GrouperDataFieldAssignHst.class);
  }
  
  /**
   * @param grouperDataFieldAssignHst
   * @return true if changed
   */
  public static boolean store(GrouperDataFieldAssignHst grouperDataFieldAssignHst) {
    
    GrouperUtil.assertion(grouperDataFieldAssignHst != null, "grouperDataFieldAssignHst is null");
    
    grouperDataFieldAssignHst.storePrepare();

    return new GcDbAccess().storeToDatabase(grouperDataFieldAssignHst);
  }
  
  /**
   * @param grouperDataFieldAssignHsts
   * @return number of changes
   */
  public static int store(Collection<GrouperDataFieldAssignHst> grouperDataFieldAssignHsts) {
    if (GrouperUtil.length(grouperDataFieldAssignHsts) == 0) {
      return 0;
    }
    
    for (GrouperDataFieldAssignHst grouperDataFieldAssignHst : grouperDataFieldAssignHsts) {
      grouperDataFieldAssignHst.storePrepare();
    }
    
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);

    return new GcDbAccess().storeBatchToDatabase(grouperDataFieldAssignHsts, batchSize);
  }

  public static void delete(GrouperDataFieldAssignHst grouperDataFieldAssignHst) {
    grouperDataFieldAssignHst.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataFieldAssignHst);
  }
  
  public static void delete(Collection<GrouperDataFieldAssignHst> grouperDataFieldAssignHsts) {
    for (GrouperDataFieldAssignHst grouperDataFieldAssignHst : grouperDataFieldAssignHsts) {      
      grouperDataFieldAssignHst.storePrepare();
    }
    
    new GcDbAccess().deleteFromDatabaseMultiple(grouperDataFieldAssignHsts);
  }
}

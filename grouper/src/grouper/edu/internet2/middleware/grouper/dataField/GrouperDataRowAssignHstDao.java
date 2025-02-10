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
public class GrouperDataRowAssignHstDao {


  public GrouperDataRowAssignHstDao() {
  }
  
  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataRowAssignHst.class)).executeSql();
  }
  
  public static List<GrouperDataRowAssignHst> selectByDataRowInternalId(long dataRowInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_row_assign_hst where data_row_internal_id = ?")
        .addBindVar(dataRowInternalId)
        .selectList(GrouperDataRowAssignHst.class);
  }
  
  public static List<GrouperDataRowAssignHst> selectByMemberInternalId(long memberInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_row_assign_hst where member_internal_id = ?")
        .addBindVar(memberInternalId)
        .selectList(GrouperDataRowAssignHst.class);
  }
  
  public static List<GrouperDataRowAssignHst> selectByDataRowInternalIds(Set<Long> dataRowInternalIds) {
    if (dataRowInternalIds == null || dataRowInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_row_assign_hst ")
        .selectMultipleColumnName("data_row_internal_id")
        .bindVars(new ArrayList<Long>(dataRowInternalIds))
        .selectList(GrouperDataRowAssignHst.class); 
  }

  /**
   * @param grouperDataRowAssignHst
   * @return true if changed
   */
  public static boolean store(GrouperDataRowAssignHst grouperDataRowAssignHst) {
    
    GrouperUtil.assertion(grouperDataRowAssignHst != null, "grouperDataRowAssignHst is null");
    
    grouperDataRowAssignHst.storePrepare();

    boolean changed = new GcDbAccess().storeToDatabase(grouperDataRowAssignHst);
    return changed;

  }
  
  /**
   * @param grouperDataRowAssignHsts
   * @return number of changes
   */
  public static int store(Collection<GrouperDataRowAssignHst> grouperDataRowAssignHsts) {
    if (GrouperUtil.length(grouperDataRowAssignHsts) == 0) {
      return 0;
    }
    
    for (GrouperDataRowAssignHst grouperDataRowAssignHst : grouperDataRowAssignHsts) {
      grouperDataRowAssignHst.storePrepare();
    }
    
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);

    return new GcDbAccess().storeBatchToDatabase(grouperDataRowAssignHsts, batchSize);
  }

  public static void delete(GrouperDataRowAssignHst grouperDataRowAssignHst) {
    grouperDataRowAssignHst.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataRowAssignHst);
  }
  
  public static void delete(Collection<GrouperDataRowAssignHst> grouperDataRowAssignHsts) {
    for (GrouperDataRowAssignHst grouperDataRowAssignHst : grouperDataRowAssignHsts) {      
      grouperDataRowAssignHst.storePrepare();
    }
    new GcDbAccess().deleteFromDatabaseMultiple(grouperDataRowAssignHsts);
  }
}

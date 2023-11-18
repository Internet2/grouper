package edu.internet2.middleware.grouper.dataField;

import java.util.List;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;

public class GrouperDataGlobalAssignDao {
  
  public GrouperDataGlobalAssignDao() {
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataGlobalAssign.class)).executeSql();
  }
  
  public static List<GrouperDataGlobalAssign> selectByDataFieldInternalId(long dataFieldInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_global_assign where data_field_internal_id = ?")
        .addBindVar(dataFieldInternalId)
        .selectList(GrouperDataGlobalAssign.class);

  }

  /**
   * @param grouperDataGlobalAssign
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDataGlobalAssign grouperDataGlobalAssign) {
    
    GrouperUtil.assertion(grouperDataGlobalAssign != null, "grouperDataGlobalAssign is null");
    
    grouperDataGlobalAssign.storePrepare();

    boolean changed = new GcDbAccess().storeToDatabase(grouperDataGlobalAssign);
    return changed;

  }  
  
  public static List<GrouperDataGlobalAssign> selectByProvider(Long dataProviderInternalId) {

    if (dataProviderInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataGlobalAssign> grouperDataGlobalAssigns = new GcDbAccess()
        .sql("select * from grouper_data_global_assign where data_provider_internal_id = ? ")
        .addBindVar(dataProviderInternalId).selectList(GrouperDataGlobalAssign.class);
    return grouperDataGlobalAssigns;
  }
  
  /**
   * 
   * @param grouperDataGlobalAssign 
   * @param connectionName
   */
  public static void delete(GrouperDataGlobalAssign grouperDataGlobalAssign) {
    grouperDataGlobalAssign.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataGlobalAssign);
  }

}

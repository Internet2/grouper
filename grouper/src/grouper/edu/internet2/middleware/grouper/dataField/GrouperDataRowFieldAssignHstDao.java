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
 * dao for data row field assign history
 */
public class GrouperDataRowFieldAssignHstDao {


  public GrouperDataRowFieldAssignHstDao() {
  }
  
  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataRowFieldAssignHst.class)).executeSql();
  }
  
  public static List<GrouperDataRowFieldAssignHst> selectByDataRowInternalId(long dataRowInternalId) {
    return new GcDbAccess().sql("select gdrfah.* from grouper_data_row_field_asn_hst gdrfah join grouper_data_row_assign_hst gdrah on gdrfah.data_row_assign_internal_id = gdrah.data_row_assign_internal_id where gdrah.data_row_internal_id = ?")
        .addBindVar(dataRowInternalId)
        .selectList(GrouperDataRowFieldAssignHst.class);
  }
  
  public static List<GrouperDataRowFieldAssignHst> selectByMemberInternalId(long memberInternalId) {
    return new GcDbAccess().sql("select gdrfah.* from grouper_data_row_field_asn_hst gdrfah join grouper_data_row_assign_hst gdrah on gdrfah.data_row_assign_internal_id = gdrah.data_row_assign_internal_id where gdrah.member_internal_id = ?")
        .addBindVar(memberInternalId)
        .selectList(GrouperDataRowFieldAssignHst.class);
  }
  
  public static List<GrouperDataRowFieldAssignHst> selectByDataFieldInternalId(long dataFieldInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_row_field_asn_hst where data_field_internal_id = ?")
        .addBindVar(dataFieldInternalId)
        .selectList(GrouperDataRowFieldAssignHst.class);
  }
  
  public static List<GrouperDataRowFieldAssignHst> selectByDataRowInternalIds(Set<Long> dataRowInternalIds) {
    if (dataRowInternalIds == null || dataRowInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select gdrfah.* from grouper_data_row_field_asn_hst gdrfah join grouper_data_row_assign_hst gdrah on gdrfah.data_row_assign_internal_id = gdrah.data_row_assign_internal_id ")
        .selectMultipleColumnName("gdrah.data_row_internal_id")
        .bindVars(new ArrayList<Long>(dataRowInternalIds))
        .selectList(GrouperDataRowFieldAssignHst.class);
    
  }
  
  public static List<GrouperDataRowFieldAssignHst> selectByDataFieldInternalIds(Set<Long> dataFieldInternalIds) {
    
    if (dataFieldInternalIds == null || dataFieldInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_row_field_asn_hst ")
        .selectMultipleColumnName("data_field_internal_id")
        .bindVars(new ArrayList<Long>(dataFieldInternalIds))
        .selectList(GrouperDataRowFieldAssignHst.class);
  }

  /**
   * @param grouperDataRowFieldAssignHst
   * @return true if changed
   */
  public static boolean store(GrouperDataRowFieldAssignHst grouperDataRowFieldAssignHst) {
    
    GrouperUtil.assertion(grouperDataRowFieldAssignHst != null, "grouperDataRowFieldAssignHst is null");
    
    grouperDataRowFieldAssignHst.storePrepare();

    boolean changed = new GcDbAccess().storeToDatabase(grouperDataRowFieldAssignHst);
    return changed;
  }
  
  /**
   * @param grouperDataRowFieldAssignHsts
   * @return number of changes
   */
  public static int store(Collection<GrouperDataRowFieldAssignHst> grouperDataRowFieldAssignHsts) {
    if (GrouperUtil.length(grouperDataRowFieldAssignHsts) == 0) {
      return 0;
    }
    
    for (GrouperDataRowFieldAssignHst grouperDataRowFieldAssignHst : grouperDataRowFieldAssignHsts) {
      grouperDataRowFieldAssignHst.storePrepare();
    }
    
    int batchSize = GrouperClientConfig.retrieveConfig().propertyValueInt("grouperClient.syncTableDefault.maxBindVarsInSelect", 900);

    return new GcDbAccess().storeBatchToDatabase(grouperDataRowFieldAssignHsts, batchSize);
  }
  
  /**
   * 
   * @param grouperDataRowFieldAssignHst
   */
  public static void delete(GrouperDataRowFieldAssignHst grouperDataRowFieldAssignHst) {
    grouperDataRowFieldAssignHst.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataRowFieldAssignHst);
  }
  
  public static void delete(Collection<GrouperDataRowFieldAssignHst> grouperDataRowFieldAssignHsts) {
    for (GrouperDataRowFieldAssignHst grouperDataRowFieldAssignHst : grouperDataRowFieldAssignHsts) {      
      grouperDataRowFieldAssignHst.storePrepare();
    }
    new GcDbAccess().deleteFromDatabaseMultiple(grouperDataRowFieldAssignHsts);
  }


}

package edu.internet2.middleware.grouper.dataField;

import java.util.List;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;

/**
 * dao for data row field assign
 * @author mchyzer
 *
 */
public class GrouperDataRowFieldAssignDao {


  public GrouperDataRowFieldAssignDao() {
  }

  public static List<GrouperDataRowFieldAssign> selectByProvider(Long dataProviderInternalId) {

    if (dataProviderInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = new GcDbAccess()
        .sql("select * from grouper_data_row_field_assign gdrfa where exists "
            + "(select 1 from grouper_data_row_assign gdra where gdrfa.data_row_assign_internal_id = gdra.internal_id and gdra.data_provider_internal_id = ? )")
        .addBindVar(dataProviderInternalId).selectList(GrouperDataRowFieldAssign.class);
    return grouperDataRowFieldAssigns;
  }

  public static List<GrouperDataRowFieldAssign> selectByDataFieldInternalId(long dataFieldInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_row_field_assign where data_field_internal_id = ?")
        .addBindVar(dataFieldInternalId)
        .selectList(GrouperDataRowFieldAssign.class);

  }
  
  public static List<GrouperDataRowFieldAssign> selectByDataRowInternalId(long dataRowInternalId) {
    return new GcDbAccess().sql("select gdrfa.* from grouper_data_row_field_assign gdrfa join grouper_data_row_assign gdra on gdrfa.data_row_assign_internal_id = gdra.internal_id where gdra.data_row_internal_id = ?")
        .addBindVar(dataRowInternalId)
        .selectList(GrouperDataRowFieldAssign.class);

  }
  
  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataRowFieldAssign.class)).executeSql();
  }

  /**
   * @param grouperDataRowFieldAssign
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDataRowFieldAssign grouperDataRowFieldAssign) {
    
    GrouperUtil.assertion(grouperDataRowFieldAssign != null, "grouperDataRowFieldAssign is null");
    
    grouperDataRowFieldAssign.storePrepare();

    boolean changed = new GcDbAccess().storeToDatabase(grouperDataRowFieldAssign);
    return changed;

  }  

  public static List<GrouperDataRowFieldAssign> selectByMarker(Long dataRowAssignInternalId, Long dataFieldInternalId) {

    if (dataRowAssignInternalId == null) {
      throw new NullPointerException();
    }
    if (dataFieldInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = new GcDbAccess()
        .sql("select * from grouper_data_row_field_assign where data_row_assign_internal_id = ? and data_field_internal_id = ? "
            + " and value_integer is null and value_dictionary_internal_id is null ")
        .addBindVar(dataRowAssignInternalId).addBindVar(dataFieldInternalId).selectList(GrouperDataRowFieldAssign.class);
    return grouperDataRowFieldAssigns;
  }
  
  /**
   * 
   * @param connectionName
   */
  public static void delete(GrouperDataRowFieldAssign grouperDataRowFieldAssign) {
    grouperDataRowFieldAssign.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataRowFieldAssign);
  }


}

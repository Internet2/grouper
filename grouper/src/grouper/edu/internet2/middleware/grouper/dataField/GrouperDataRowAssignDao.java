package edu.internet2.middleware.grouper.dataField;

import java.util.List;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;

/**
 * dao for data field assign
 * @author mchyzer
 *
 */
public class GrouperDataRowAssignDao {


  public GrouperDataRowAssignDao() {
  }

  public static List<GrouperDataRowAssign> selectByProvider(Long dataProviderInternalId) {

    if (dataProviderInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataRowAssign> grouperDataRowAssigns = new GcDbAccess()
        .sql("select * from grouper_data_row_assign where data_provider_internal_id = ? ")
        .addBindVar(dataProviderInternalId).selectList(GrouperDataRowAssign.class);
    return grouperDataRowAssigns;
  }


  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataRowAssign.class)).executeSql();
  }

  /**
   * @param grouperDataRowAssign
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDataRowAssign grouperDataRowAssign) {
    
    GrouperUtil.assertion(grouperDataRowAssign != null, "grouperDataRowAssign is null");
    
    grouperDataRowAssign.storePrepare();

    boolean changed = new GcDbAccess().storeToDatabase(grouperDataRowAssign);
    return changed;

  }  

  public static List<GrouperDataRowAssign> selectByMemberAndRow(Long memberInternalId, Long dataRowInternalId) {

    if (memberInternalId == null) {
      throw new NullPointerException();
    }
    
    if (dataRowInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataRowAssign> grouperDataRowAssigns = new GcDbAccess()
        .sql("select * from grouper_data_row_assign where member_internal_id = ? and data_row_internal_id = ? ")
        .addBindVar(memberInternalId).addBindVar(dataRowInternalId).selectList(GrouperDataRowAssign.class);
    return grouperDataRowAssigns;
  }

  /**
   * 
   * @param connectionName
   */
  public static void delete(GrouperDataRowAssign grouperDataRowAssign) {
    grouperDataRowAssign.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataRowAssign);
  }


}

package edu.internet2.middleware.grouper.dataField;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * dao for data field assign
 * @author mchyzer
 *
 */
public class GrouperDataFieldAssignDao {


  public GrouperDataFieldAssignDao() {
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataFieldAssign.class)).executeSql();
  }

  /**
   * @param grouperDataFieldAssign
   * @param connectionName
   * @return true if changed
   */
  public static boolean store(GrouperDataFieldAssign grouperDataFieldAssign) {
    
    GrouperUtil.assertion(grouperDataFieldAssign != null, "grouperDataFieldAssign is null");
    
    grouperDataFieldAssign.storePrepare();

    boolean changed = new GcDbAccess().storeToDatabase(grouperDataFieldAssign);
    return changed;

  }  

  public static List<GrouperDataFieldAssign> selectByMarker(Long memberInternalId, Long dataFieldInternalId) {

    if (memberInternalId == null) {
      throw new NullPointerException();
    }
    if (dataFieldInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataFieldAssign> grouperDataFieldAssigns = new GcDbAccess()
        .sql("select * from grouper_data_field_assign where member_internal_id = ? and data_field_internal_id = ? "
            + " and value_integer is null and value_dictionary_internal_id is null ")
        .addBindVar(memberInternalId).addBindVar(dataFieldInternalId).selectList(GrouperDataFieldAssign.class);
    return grouperDataFieldAssigns;
  }
  
  /**
   * 
   * @param connectionName
   */
  public static void delete(GrouperDataFieldAssign grouperDataFieldAssign) {
    grouperDataFieldAssign.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataFieldAssign);
  }


}

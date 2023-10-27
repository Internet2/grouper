package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

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
  
  public static List<GrouperDataRowAssign> selectByProviderAndMembers(Long dataProviderInternalId, Set<Long> memberInternalIds) {

    if (dataProviderInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataRowAssign> grouperDataRowAssigns = new ArrayList<GrouperDataRowAssign>();

    if (memberInternalIds.size() == 0) {
      return grouperDataRowAssigns;
    }

    int batchSize = 200;
    List<Long> memberInternalIdsList = new ArrayList<Long>(memberInternalIds);

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberInternalIdsList.size(), batchSize, true);
    for (int i=0;i<numberOfBatches;i++) {
      GcDbAccess gcDbAccess = new GcDbAccess();
      List<Long> batchMemberInternalIds = GrouperUtil.batchList(memberInternalIdsList, batchSize, i);

      StringBuilder sql = new StringBuilder("select * from grouper_data_row_assign where data_provider_internal_id = ? and member_internal_id in (");
      gcDbAccess.addBindVar(dataProviderInternalId);
      GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberInternalIds));
      for (Long memberId : batchMemberInternalIds) {
        gcDbAccess.addBindVar(memberId);
      }

      sql.append(")");

      List<GrouperDataRowAssign> currGrouperDataRowAssigns = gcDbAccess.sql(sql.toString()).selectList(GrouperDataRowAssign.class);
      grouperDataRowAssigns.addAll(currGrouperDataRowAssigns);
    }
   
    return grouperDataRowAssigns;
  }

  public static List<GrouperDataRowAssign> selectByDataRowInternalId(long dataRowInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_row_assign where data_row_internal_id = ?")
        .addBindVar(dataRowInternalId)
        .selectList(GrouperDataRowAssign.class);

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

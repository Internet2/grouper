package edu.internet2.middleware.grouper.dataField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.tableIndex.TableIndex;
import edu.internet2.middleware.grouper.tableIndex.TableIndexType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.GcPersistableHelper;
import edu.internet2.middleware.grouperClient.jdbc.GcTransactionCallback;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;

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
  
  public static List<GrouperDataRowAssign> selectByDataProviderInternalIds(Set<Long> dataProviderInternalIds) {

    
    if (dataProviderInternalIds == null || dataProviderInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_row_assign ")
        .selectMultipleColumnName("data_provider_internal_id")
        .bindVars(new ArrayList<Long>(dataProviderInternalIds))
        .selectList(GrouperDataRowAssign.class);
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
  
  public static List<GrouperDataRowAssign> selectByDataRowInternalIds(Set<Long> dataRowInternalIds) {
    if (dataRowInternalIds == null || dataRowInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_row_assign ")
        .selectMultipleColumnName("data_row_internal_id")
        .bindVars(new ArrayList<Long>(dataRowInternalIds))
        .selectList(GrouperDataRowAssign.class);
    
  }

  /**
   * delete all data if table is here
   */
  public static void reset() {
    new GcDbAccess().connectionName("grouper").sql("delete from " + GcPersistableHelper.tableName(GrouperDataRowAssign.class)).executeSql();
  }

  public static void store(Collection<GrouperDataRowAssign> grouperDataRowAssigns) {
    if (GrouperUtil.length(grouperDataRowAssigns) == 0) {
      return;
    }

    int internalIdsNeeded = 0;
    for (GrouperDataRowAssign grouperDataRowAssign : grouperDataRowAssigns) {
      if (grouperDataRowAssign.getTempInternalIdOnDeck() == null) {
        internalIdsNeeded++;
      }
    }
    
    List<Long> ids = TableIndex.reserveIds(TableIndexType.dataRowAssign, internalIdsNeeded);
    int currentIndex = 0;
    for (GrouperDataRowAssign grouperDataRowAssign : grouperDataRowAssigns) {
      if (grouperDataRowAssign.getTempInternalIdOnDeck() == null) {
        grouperDataRowAssign.setTempInternalIdOnDeck(ids.get(currentIndex++));
      }
    }

    for (GrouperDataRowAssign grouperDataRowAssign: grouperDataRowAssigns) {      
      grouperDataRowAssign.storePrepare();
    }
        
    new GcDbAccess().storeBatchToDatabase(grouperDataRowAssigns, 1000);
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
  
  public static void delete(GrouperDataRowAssign grouperDataRowAssign) {
    delete(grouperDataRowAssign, true);
  }

  public static void delete(GrouperDataRowAssign grouperDataRowAssign, boolean addHistory) {
    grouperDataRowAssign.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataRowAssign);
  }
  
  public static void delete(Collection<GrouperDataRowAssign> grouperDataRowAssigns) {
    for (GrouperDataRowAssign grouperDataRowAssign: grouperDataRowAssigns) {      
      grouperDataRowAssign.storePrepare();
    }
    
    new GcDbAccess().deleteFromDatabaseMultiple(grouperDataRowAssigns);
  }
  
  public static List<GrouperDataRowAssignView> retrieveDataRowAssignments(Subject subject) {
    
    List<GrouperDataRowAssignView> result = new ArrayList<GrouperDataRowAssignView>();
    
    String sql = "select gdrav.data_row_config_id, gdrfav.data_field_config_id , gdrfav.value_text, gdrfav.value_integer "
        + "from grouper_data_row_assign_v gdrav, grouper_data_row_field_asgn_v gdrfav "
        + "where gdrav.data_row_assign_internal_id  = gdrfav.data_row_assign_internal_id  and gdrav.subject_id = ? and gdrav.subject_source_id = ?";
    List<Object[]> objects = new GcDbAccess().sql(sql).addBindVar(subject.getId()).addBindVar(subject.getSourceId())
        .selectList(Object[].class);
    
    for (Object[] object: objects) {
      result.add(new GrouperDataRowAssignView(GrouperUtil.stringValue(object[0]), GrouperUtil.stringValue(object[1]),
          GrouperUtil.stringValue(object[2]), object[3] == null ? null: GrouperUtil.longValue(object[3])));
    }
    
    return result;
  }
}

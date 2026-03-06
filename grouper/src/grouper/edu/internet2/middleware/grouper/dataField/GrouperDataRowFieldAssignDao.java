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
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

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
  
  public static List<GrouperDataRowFieldAssign> selectByProviderAndMembers(Long dataProviderInternalId, Set<Long> memberInternalIds) {

    if (dataProviderInternalId == null) {
      throw new NullPointerException();
    }
    
    List<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns = new ArrayList<GrouperDataRowFieldAssign>();

    if (memberInternalIds.size() == 0) {
      return grouperDataRowFieldAssigns;
    }

    int batchSize = 200;
    List<Long> memberInternalIdsList = new ArrayList<Long>(memberInternalIds);

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(memberInternalIdsList.size(), batchSize, true);
    for (int i=0;i<numberOfBatches;i++) {
      GcDbAccess gcDbAccess = new GcDbAccess();
      List<Long> batchMemberInternalIds = GrouperUtil.batchList(memberInternalIdsList, batchSize, i);

      StringBuilder sql = new StringBuilder("select * from grouper_data_row_field_assign gdrfa where exists "
          + "(select 1 from grouper_data_row_assign gdra where gdrfa.data_row_assign_internal_id = gdra.internal_id and gdra.data_provider_internal_id = ? and gdra.member_internal_id in (");
      gcDbAccess.addBindVar(dataProviderInternalId);
      GrouperClientUtils.appendQuestions(sql, GrouperUtil.length(batchMemberInternalIds));
      for (Long memberId : batchMemberInternalIds) {
        gcDbAccess.addBindVar(memberId);
      }

      sql.append("))");

      List<GrouperDataRowFieldAssign> currGrouperDataRowFieldAssigns = gcDbAccess.sql(sql.toString()).selectList(GrouperDataRowFieldAssign.class);
      grouperDataRowFieldAssigns.addAll(currGrouperDataRowFieldAssigns);
    }
   
    return grouperDataRowFieldAssigns;
  }

  public static List<GrouperDataRowFieldAssign> selectByDataFieldInternalId(long dataFieldInternalId) {
    return new GcDbAccess().sql("select * from grouper_data_row_field_assign where data_field_internal_id = ?")
        .addBindVar(dataFieldInternalId)
        .selectList(GrouperDataRowFieldAssign.class);

  }
  
  public static List<GrouperDataRowFieldAssign> selectByDataFieldInternalIds(Set<Long> dataFieldInternalIds) {
    
    if (dataFieldInternalIds == null || dataFieldInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_row_field_assign ")
        .selectMultipleColumnName("data_field_internal_id")
        .bindVars(new ArrayList<Long>(dataFieldInternalIds))
        .selectList(GrouperDataRowFieldAssign.class);
  }
  
  public static List<GrouperDataRowFieldAssign> selectByDataRowAssignInternalIds(Set<Long> dataRowAssignInternalIds) {
    
    if (dataRowAssignInternalIds == null || dataRowAssignInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select * from grouper_data_row_field_assign ")
        .selectMultipleColumnName("data_row_assign_internal_id")
        .bindVars(new ArrayList<Long>(dataRowAssignInternalIds))
        .selectList(GrouperDataRowFieldAssign.class);
  }
  
  public static List<GrouperDataRowFieldAssign> selectByDataRowInternalId(long dataRowInternalId) {
    return new GcDbAccess().sql("select gdrfa.* from grouper_data_row_field_assign gdrfa join grouper_data_row_assign gdra on gdrfa.data_row_assign_internal_id = gdra.internal_id where gdra.data_row_internal_id = ?")
        .addBindVar(dataRowInternalId)
        .selectList(GrouperDataRowFieldAssign.class);

  }
  
  public static List<GrouperDataRowFieldAssign> selectByDataRowInternalIds(Set<Long> dataRowInternalIds) {
    if (dataRowInternalIds == null || dataRowInternalIds.size() == 0) {
      return new ArrayList<>();
    }
    
    return new GcDbAccess().sql("select gdrfa.* from grouper_data_row_field_assign gdrfa join grouper_data_row_assign gdra on gdrfa.data_row_assign_internal_id = gdra.internal_id ")
        .selectMultipleColumnName("gdra.data_row_internal_id")
        .bindVars(new ArrayList<Long>(dataRowInternalIds))
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
  
  public static void generateInternalIdsIfNeeded(Collection<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns) {
    if (GrouperUtil.length(grouperDataRowFieldAssigns) == 0) {
      return;
    }
    
    int internalIdsNeeded = 0;
    for (GrouperDataRowFieldAssign grouperDataRowFieldAssign : grouperDataRowFieldAssigns) {
      if (grouperDataRowFieldAssign.getTempInternalIdOnDeck() == null && grouperDataRowFieldAssign.getInternalId() == -1) {
        internalIdsNeeded++;
      }
    }
    
    List<Long> ids = TableIndex.reserveIds(TableIndexType.dataRowFieldAssign, internalIdsNeeded);
    int currentIndex = 0;
    for (GrouperDataRowFieldAssign grouperDataRowFieldAssign : grouperDataRowFieldAssigns) {
      if (grouperDataRowFieldAssign.getTempInternalIdOnDeck() == null && grouperDataRowFieldAssign.getInternalId() == -1) {
        grouperDataRowFieldAssign.setTempInternalIdOnDeck(ids.get(currentIndex++));
      }
    }
  }

  public static void store(Collection<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns) {
    if (GrouperUtil.length(grouperDataRowFieldAssigns) == 0) {
      return;
    }

    for (GrouperDataRowFieldAssign grouperDataRowFieldAssign: grouperDataRowFieldAssigns) {      
      grouperDataRowFieldAssign.storePrepare();
    }
        
    new GcDbAccess().storeBatchToDatabase(grouperDataRowFieldAssigns, 1000);
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

  public static void delete(GrouperDataRowFieldAssign grouperDataRowFieldAssign) {
    grouperDataRowFieldAssign.storePrepare();
    new GcDbAccess().deleteFromDatabase(grouperDataRowFieldAssign);
  }
  
  public static void delete(Collection<GrouperDataRowFieldAssign> grouperDataRowFieldAssigns) {
    for (GrouperDataRowFieldAssign grouperDataRowFieldAssign: grouperDataRowFieldAssigns) {      
      grouperDataRowFieldAssign.storePrepare();
    }
    
    new GcDbAccess().deleteFromDatabaseMultiple(grouperDataRowFieldAssigns);
  }
  
  /**
   * 
   * @param dataFieldInternalIds
   * @param memberInternalIds
   * @return list of array containing data row internal id, data field internal id, data row assign internal id, member internal id, value string, value integer
   */
  public static List<Object[]> selectDataRowFieldAssignValuesByDataFieldInternalIdsAndMemberInternalIds(Set<Long> dataFieldInternalIds, Set<Long> memberInternalIds) {
    List<Object[]> results = new ArrayList<>();

    if (dataFieldInternalIds == null || dataFieldInternalIds.size() == 0 || memberInternalIds == null || memberInternalIds.size() == 0) {
      return results;
    }
    
    List<Long> memberInternalIdsList = new ArrayList<Long>(memberInternalIds);
    int batchSize = 900;
    int numberOfBatches = GrouperClientUtils.batchNumberOfBatches(memberInternalIdsList, batchSize, true);
    for (int batchIndex = 0; batchIndex < numberOfBatches; batchIndex++) {
      List<Long> batchOfMemberInternalIds = GrouperClientUtils.batchList(memberInternalIdsList, batchSize, batchIndex);
      
      // ordered to make it deterministic
      String sql = "select gdra.data_row_internal_id, gdrfa.data_field_internal_id, gdra.internal_id, gdra.member_internal_id, gd.the_text, gdrfa.value_integer " +
          "from grouper_data_row_assign gdra, grouper_data_row_field_assign gdrfa " +
          "left join grouper_dictionary gd on gdrfa.value_dictionary_internal_id = gd.internal_id " +
          "where gdra.internal_id = gdrfa.data_row_assign_internal_id and gdrfa.data_field_internal_id IN (" + GrouperClientUtils.appendQuestions(dataFieldInternalIds.size()) + ") " +
          "and gdra.member_internal_id IN (" + GrouperClientUtils.appendQuestions(batchOfMemberInternalIds.size()) + ") " +
          "order by gdrfa.internal_id";
      
      GcDbAccess gcDbAccess = new GcDbAccess().sql(sql);
      for (Long dataFieldInternalId : dataFieldInternalIds) {
        gcDbAccess.addBindVar(dataFieldInternalId);
      }
      
      for (Long memberInternalId : batchOfMemberInternalIds) {
        gcDbAccess.addBindVar(memberInternalId);
      }
      
      results.addAll(gcDbAccess.selectList(Object[].class));
    }
    
    return results;
  }
}
